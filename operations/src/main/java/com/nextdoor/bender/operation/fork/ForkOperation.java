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
 * Copyright 2018 Nextdoor.com, Inc
 *
 */

package com.nextdoor.bender.operation.fork;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.operation.OperationProcessor;
import com.nextdoor.bender.operation.StreamOperation;
import com.oath.cyclops.async.adapters.Queue;

public class ForkOperation implements StreamOperation {
  private List<Queue<InternalEvent>> queues;
  private final List<List<OperationProcessor>> opProcsInForks;
  private final ExecutorService es;

  public ForkOperation(List<List<OperationProcessor>> opProcsInForks) {
    this.opProcsInForks = opProcsInForks;
    this.es = Executors.newFixedThreadPool(opProcsInForks.size());
  }

  public static class StreamToQueue implements Runnable {
    private final Stream<InternalEvent> input;
    private final Queue<InternalEvent> output;
    public final AtomicInteger countdown;

    public StreamToQueue(Stream<InternalEvent> input, Queue<InternalEvent> output,
        AtomicInteger countdown) {
      this.input = input;
      this.output = output;
      this.countdown = countdown;
    }

    @Override
    public void run() {
      this.input.forEach(ievent -> {
        this.output.offer(ievent);
      });

      this.input.close();

      /*
       * When all consumers are done have the last one close the queue.
       */
      if (countdown.decrementAndGet() <= 0) {
        this.output.close();
      }
    }
  }

  /*-
   * This operation takes in an input Stream, copies each event from that Stream, and
   * writes each copy to a fork. Each fork has a consumer thread that pulls events
   * through the fork's Stream and outputs to a Queue. Finally a single thread consumes
   * the output Queue and writes to the output Stream. Visually this is what happens:
   * 
   *                       +--------------+
   *                       | Input Stream |
   *                       +-------+------+
   *                               |
   *                               v
   *                   +-----------+-----------+
   *                   | Input Consumer Thread |
   *                   +-----------+-----------+
   *                               |
   *                               v
   *                   +-----------+-----------+
   *                   |                       |
   *                   v                       v
   *            +------+-------+        +------+-------+
   *            | Fork 1 Queue |        | Fork 2 Queue |
   *            +------+-------+        +------+-------+
   *                   |                       |
   *                   v                       v
   *           +-------+-------+        +------+--------+
   *           | Fork 1 Stream |        | Fork 2 Stream |
   *           +-------+-------+        +------+--------+
   *                   |                       |
   *                   v                       v
   *       +-----------+-------+        +------+------------+
   *       | Fork 1 Operations |        | Fork 2 Operations |
   *       +-----------+-------+        +------+------------+
   *                   |                       |
   *                   v                       v
   *  +----------------+-------+        +------+-----------------+
   *  | Fork 1 Consumer Thread |        | Fork 2 Consumer Thread |
   *  +----------------+-------+        +------+-----------------+
   *                   |                       |
   *                   +-----------+-----------+
   *                               |
   *                               v
   *                       +-------+------+
   *                       | Output Queue |
   *                       +-------+------+
   *                               |
   *                               v
   *                  +------------+-----------+
   *                  | Output Consumer Thread |
   *                  +------------+-----------+
   *                               |
   *                               v
   *                       +-------+-------+
   *                       | Output Stream |
   *                       +---------------+
   * 
   */
  public Stream<InternalEvent> getOutputStream(Stream<InternalEvent> input) {
    /*
     * forkOutputStreams keeps track of the output Stream of each fork.
     */
    List<Stream<InternalEvent>> forkOutputStreams =
        new ArrayList<Stream<InternalEvent>>(opProcsInForks.size());

    /*
     * From a list of operation configurations in each fork construct queues and streams.
     */
    this.queues = new ArrayList<Queue<InternalEvent>>(opProcsInForks.size());
    for (List<OperationProcessor> opProcsInFork : opProcsInForks) {
      /*
       * Construct a Queue for each fork. This is the input to each Fork.
       */
      Queue<InternalEvent> queue =
          new Queue<InternalEvent>(new LinkedBlockingQueue<InternalEvent>(opProcsInFork.size()));
      this.queues.add(queue);

      /*
       * Connect the fork's input Queue with operations. Each operation returns a stream with its
       * operation concatenated on.
       */
      Stream<InternalEvent> forkInput = queue.jdkStream();
      for (OperationProcessor opProcInFork : opProcsInFork) {
        forkInput = opProcInFork.perform(forkInput);
      }

      /*
       * Last input is the output.
       */
      forkOutputStreams.add(forkInput);
    }

    /*
     * Fork Consumer Threads
     * 
     * Combine each fork's output stream and write to the output Queue. When all data is consumed
     * the last fork closes the output Queue.
     */
    Queue<InternalEvent> outputQueue =
        new Queue<InternalEvent>(new LinkedBlockingQueue<InternalEvent>(this.queues.size()));
    AtomicInteger lock = new AtomicInteger(forkOutputStreams.size());

    forkOutputStreams.forEach(stream -> {
      this.es.execute(new StreamToQueue(stream, outputQueue, lock));
    });

    /*
     * Consume input Stream in a thread and publish to each fork's Queue.
     */
    new Thread(new Runnable() {
      @Override
      public void run() {
        input.forEach(ievent -> {
          queues.forEach(queue -> {
            /*
             * The original event is NOT sent to each fork. Rather a copy of the event is sent to
             * each fork. This ensures that there is no contention between the operations performed
             * on each event. Caveat is that when the forks join there will be two events produced.
             */
            queue.offer(ievent.copy());
          });
        });

        for (Queue<InternalEvent> queue : queues) {
          queue.close();
        }
      }
    }).start();

    return outputQueue.jdkStream();
  }
}
