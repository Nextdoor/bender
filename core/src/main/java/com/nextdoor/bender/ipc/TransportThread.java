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

import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.nextdoor.bender.monitoring.Stat;

public class TransportThread extends Thread {
  private TransportFactory tf;
  private TransportBuffer buffer;
  private AtomicInteger threadCounter;
  private AtomicBoolean hasUnrecoverableException;
  private Stat threadStat;
  private Stat errorStat;
  private Stat successStat;
  private LinkedHashMap<String, String> partitions;

  public TransportThread(TransportFactory tf, TransportBuffer buffer,
      LinkedHashMap<String, String> partitions, AtomicInteger threadCounter,
      AtomicBoolean hasUnrecoverableException, Stat threadStat, Stat errorStat, Stat successStat) {
    this.tf = tf;
    this.buffer = buffer;
    this.partitions = partitions;
    this.threadCounter = threadCounter;
    this.hasUnrecoverableException = hasUnrecoverableException;
    this.threadStat = threadStat;
    this.errorStat = errorStat;
    this.successStat = successStat;
  }

  @Override
  public void run() {
    if (hasUnrecoverableException.get()) {
      threadCounter.decrementAndGet();
      return;
    }

    threadStat.start();
    Transport transport;

    /*
     * Create new {@link Transport} from the {@link TransportFactory}
     */
    try {
      transport = tf.newInstance();
    } catch (TransportFactoryInitException e) {
      hasUnrecoverableException.set(true);
      threadCounter.decrementAndGet();
      threadStat.stop();
      throw new RuntimeException(e);
    }

    buffer.close();

    /*
     * Attempt to send the buffer. If a TransportException occurs then signal to the Handler that it
     * failed and function should be killed.
     */
    try {
      if (transport instanceof UnpartitionedTransport) {
        ((UnpartitionedTransport) transport).sendBatch(buffer);
      } else if (transport instanceof PartitionedTransport) {
        ((PartitionedTransport) transport).sendBatch(buffer, partitions);
      } else {
        throw new TransportException("unknown type of transport");
      }
    } catch (TransportException e) {
      errorStat.increment();
      hasUnrecoverableException.set(true);
      throw new RuntimeException(e);
    } finally {
      threadStat.stop();
      threadCounter.decrementAndGet();
      buffer.clear();
    }

    successStat.increment();
  }
}
