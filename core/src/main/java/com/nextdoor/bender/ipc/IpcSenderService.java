/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 * Copyright 2017 Nextdoor.com, Inc
 *
 */

package com.nextdoor.bender.ipc;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.amazonaws.services.lambda.runtime.Context;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.monitoring.MonitoredProcess;

/**
 * Manages {@link TransportBuffer}s per event partition and flushes buffers to the specified
 * {@link Transport} when the buffer is full. Note that when using high cardinality of partitions
 * this may cause OOMs as the buffers aren't flushed until they are full.
 */
public class IpcSenderService extends MonitoredProcess {
  private static final Logger logger = Logger.getLogger(IpcSenderService.class);
  /*
   * This thread pool is only for serializing data and invoking batch send. Typically the batch send
   * will be asynchronous and launch another thread.
   */
  private final ExecutorService pool;
  private TransportFactory transportFactory;
  private Context context;

  protected AtomicInteger threadCounter = new AtomicInteger(0);
  protected AtomicBoolean hasUnrecoverableException = new AtomicBoolean(false);

  public final Map<LinkedHashMap<String, String>, TransportBuffer> buffers;

  public IpcSenderService(TransportFactory factory) {
    super(factory.getChildClass());
    this.transportFactory = factory;
    this.pool = Executors.newFixedThreadPool(factory.getMaxThreads());
    this.buffers = new HashMap<>();
  }

  /**
   * Adds single event to internal buffer. If buffer fills up then it is drained and sent
   * asynchronously. Then the event is then added to an empty buffer.
   *
   * @param ievent event to add to buffer.
   * @throws TransportException error while adding to the buffer.
   */
  synchronized public void add(InternalEvent ievent) throws TransportException {
    // Since threads are running concurrently, we need to check this proactively.
    // Even though handler just logs the exception, we're able to avoid starting new
    // multi part uploads and let the handler error out faster so it can re-try the payload.
    // We leave the value as is so it can be used during flush() to throw an exception.
    if (this.hasUnrecoverableException.get()) {
      // TODO: confirm for the s3 case that clients will need a lifecycle policy
      //  that aborts incomplete uploads within a timeframe.
      throw new TransportException("TransportThread was unsuccessful when adding an event.");
    }

    /*
     * Get appropriate buffer for the event's partition values
     */
    LinkedHashMap<String, String> partitions = ievent.getPartitions();

    // TODO: not sure why I made buffers synchronized
    synchronized (buffers) {
      if (partitions == null) {
        partitions = new LinkedHashMap<>(0);
      }

      if (!this.buffers.containsKey(partitions)) {
        partitions = new LinkedHashMap<>(partitions);
        this.buffers.put(partitions, this.transportFactory.newTransportBuffer());
      }

      TransportBuffer buffer = this.buffers.get(partitions);

      /*
       * Attempt to add event. If IllegalStateException occurs the buffer is full and must be
       * flushed/sent.
       */
      try {
        buffer.add(ievent);
        return;
      } catch (IllegalStateException e) {
        send(buffer, partitions);
      } catch (IOException e) {
        throw new TransportException("Exception occurred while adding to buffer", e);
      }

      /*
       * Remove buffer associated with the partitions and create a new one.
       */
      this.buffers.remove(partitions);
      buffer = this.transportFactory.newTransportBuffer();
      partitions = new LinkedHashMap<>(partitions);
      this.buffers.put(partitions, buffer);

      /*
       * Finally add the event now that the buffer is empty
       */
      try {
        buffer.add(ievent);
      } catch (IllegalStateException e) {
        throw new TransportException("Buffer is full despite being newly created", e);
      } catch (IOException e) {
        throw new TransportException("Exception ocurred while adding to buffer", e);
      }
    }
  }

  /**
   * Drains buffer and sends batch asynchronously via {@link Transport}.
   *
   * @param buffer the buffer to send.
   * @param partitions the partitions associated with the buffer.
   */
  private void send(TransportBuffer buffer, LinkedHashMap<String, String> partitions) {
    if (buffer.isEmpty()) {
      return;
    }

    this.threadCounter.incrementAndGet();

    TransportThread tt = new TransportThread(transportFactory, buffer, partitions,
        this.threadCounter, this.hasUnrecoverableException, getRuntimeStat().fork(),
        getErrorCountStat(), getSuccessCountStat(), getContext());

    tt.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
      public void uncaughtException(Thread th, Throwable ex) {
        logger.error(String.format("transport thread %s failed", th.getName()), ex);
      }
    });

    this.pool.execute(tt);
  }

  /**
   * Sends remaining contents of all buffers and waits for all calls to finish.
   *
   * @throws InterruptedException interrupted while waiting for calls to complete.
   * @throws TransportException not all transports succeeded.
   */
  synchronized public void flush() throws InterruptedException, TransportException {
    synchronized (buffers) {
      /*
       * Send what remains in the buffers.
       * If there are errors, we will immediately send an error after this sync block.
       */
      this.buffers.forEach((partition, buffer) -> send(buffer, partition));
      this.buffers.clear();
    }

    /*
     * Wait for transporter to finish
     */
    while (this.threadCounter.get() != 0) {
      Thread.sleep(5);
    }

    /*
     * Some factories keep state on the transports they create. Perform any cleanup that is
     * required.
     */
    transportFactory.close();

    /*
     * Collect runtime of each thread
     */
    this.getRuntimeStat().join();

    /*
     * Fail if there are any errors. These can come from the add() method above or
     * from the send() operation earlier in this method when clearing the buffers.
     */
    if (this.hasUnrecoverableException.getAndSet(false)) {
      throw new TransportException("Not all transports succeeded during the handler.");
    }

  }

  synchronized public void shutdown() {
    try {
      flush();
    } catch (InterruptedException | TransportException e) {
      logger.warn("caught error while flushing", e);
    }

    this.pool.shutdown();
  }

  public boolean hasUnrecoverableException() {
    return this.hasUnrecoverableException.get();
  }

  public void setHasUnrecoverableException(boolean has) {
    this.hasUnrecoverableException.set(true);
  }

  public TransportFactory getTransportFactory() {
    return this.transportFactory;
  }

  public void setTransportFactory(TransportFactory transportFactory) {
    this.transportFactory = transportFactory;
  }

  public Context getContext() {
    return context;
  }

  public void setContext(Context context) {
    this.context = context;
  }
}
