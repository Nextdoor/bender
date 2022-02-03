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

package com.nextdoor.bender.operation.conditional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.operation.FilterOperation;
import com.nextdoor.bender.operation.OperationProcessor;
import com.nextdoor.bender.operation.StreamOperation;
import com.nextdoor.bender.operation.fork.ForkOperation.StreamToQueue;
import com.oath.cyclops.async.adapters.Queue;

public class ConditionalOperation implements StreamOperation {
  private List<Pair<FilterOperation, Queue<InternalEvent>>> filtersAndQueues;
  private final List<Pair<FilterOperation, List<OperationProcessor>>> conditionsAndProcs;
  private final ExecutorService es;
  private final boolean filterNonMatch;

  public ConditionalOperation(
      List<Pair<FilterOperation, List<OperationProcessor>>> conditionsAndProcs,
      boolean filterNonMatch) {
    this.conditionsAndProcs = conditionsAndProcs;
    this.es = Executors.newFixedThreadPool(conditionsAndProcs.size());
    this.filterNonMatch = filterNonMatch;
  }

  /*-
   * This operation takes in an input Stream of events and checks the event against
   * each condition in an if elseif manner. The first matching condition is send the 
   * event. If no conditions match and filter non-match is specified true then the
   * event is filtered out. If false the event is sent to the output queue. 
   * 
   *           +--------------+
   *           | Input Stream |
   *           +------+-------+
   *                  |
   *                  v
   *       +----------+-----------+
   *       | Input Consumer Thread |
   *       +----------+------------+
   *                  |
   *        +---------+
   *        |
   *        v
   *  +-----+-------+    +-------------+      +-------------+
   *  | Condition 1 | No | Condition 2 |  No  |    Filter   |
   *  |   Filter    +--->+   Filter    +----->+  Non-Match  |
   *  +-----+-------+    +-----+-------+      +------+------+
   *        |                  |                     |
   *    Yes |             Yes  |                     | No
   *        v                  v                     |
   *   +----+-----+       +----+-----+               |
   *   |  Queue 1 |       |  Queue 2 |               |
   *   +----+-----+       +----+-----+               |
   *        |                  |                     |
   *        v                  v                     |
   *    +---+----+         +---+----+                |
   *    | Stream |         | Stream |                |
   *    +---+----+         +---+----+                |
   *        |                  |                     |
   *        v                  v                     |
   *  +-----+------+     +-----+------+              |
   *  | Operations |     | Operations |              |
   *  +-----+------+     +-----+------+              |
   *        |                  |                     |
   *        v                  v                     |
   *   +----+-----+       +----+-----+               |
   *   | Consumer |       | Consumer |               |
   *   |  Thread  |       |  Thread  |               |
   *   +--+-------+       +-----+----+               |
   *      |                     |                    |
   *      |   +--------------+  |                    |
   *      +-->+ Output Queue +<-+--------------------+
   *          +------+-------+
   *                 |
   *                 v
   *     +-----------+------------+
   *     | Output Consumer Thread |
   *     +-----------+------------+
   *                 |
   *                 v
   *         +-------+-------+
   *         | Output Stream |
   *         +---------------+
   *  
   * 
   */
  public Stream<InternalEvent> getOutputStream(Stream<InternalEvent> input) {
    /*
     * outputStreams keeps track of the output Stream of each Condition.
     */
    List<Stream<InternalEvent>> outputStreams =
        new ArrayList<>(this.conditionsAndProcs.size());

    /*
     * From a list of operation configurations in each condition construct queues and streams.
     */
    this.filtersAndQueues =
        new ArrayList<>(this.conditionsAndProcs.size());
    for (Pair<FilterOperation, List<OperationProcessor>> filterAndProcs : this.conditionsAndProcs) {

      FilterOperation filter = filterAndProcs.getLeft();
      List<OperationProcessor> procs = filterAndProcs.getRight();

      /*
       * Construct a Queue for each conditional. This is the input to each Condition.
       */
      Queue<InternalEvent> queue =
          new Queue<>(new LinkedBlockingQueue<>(procs.size()));

      this.filtersAndQueues
          .add(new ImmutablePair<>(filter, queue));

      /*
       * Connect the condition's input Queue with operations. Each operation returns a stream with its
       * operation concatenated on.
       */
      Stream<InternalEvent> conditionInput = queue.jdkStream();
      for (OperationProcessor proc : procs) {
        conditionInput = proc.perform(conditionInput);
      }

      /*
       * Last input is the output.
       */
      outputStreams.add(conditionInput);
    }

    /*
     * Condition Consumer Threads
     * 
     * Combine each condition's output stream and write to the output Queue. When all data is consumed
     * the last condition closes the output Queue.
     */
    Queue<InternalEvent> outputQueue = new Queue<>(
        new LinkedBlockingQueue<>(this.conditionsAndProcs.size()));
    AtomicInteger lock = new AtomicInteger(outputStreams.size());

    outputStreams.forEach(stream -> {
      this.es.execute(new StreamToQueue(stream, outputQueue, lock));
    });

    /*
     * Consume input Stream in a thread and publish to each condition's Queue.
     */
    new Thread(new Runnable() {
      @Override
      public void run() {
        input.forEach(ievent -> {
          boolean matches = false;

          for (Pair<FilterOperation, Queue<InternalEvent>> filterAndQueue : filtersAndQueues) {
            FilterOperation filter = filterAndQueue.getLeft();

            /*
             * If event passes the filter offer event to queue.
             */
            if (filter.test(ievent)) {
              filterAndQueue.getRight().offer(ievent);
              matches = true;
              break;
            }
          }

          /*
           * Send to output queue if no case matches
           */
          if (!matches && !filterNonMatch) {
            outputQueue.offer(ievent);
          }
        });

        /*
         * Close queues when source queue is consumed.
         */
        for (Pair<FilterOperation, Queue<InternalEvent>> filterAndQueue : filtersAndQueues) {
          filterAndQueue.getRight().close();
        }
      }
    }).start();

    return outputQueue.jdkStream();
  }
}
