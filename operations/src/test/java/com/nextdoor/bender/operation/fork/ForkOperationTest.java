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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Test;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.monitoring.Monitor;
import com.nextdoor.bender.monitoring.Stat;
import com.nextdoor.bender.operation.OperationProcessor;
import com.nextdoor.bender.testutils.DummyDeserializerHelper.DummyStringEvent;
import com.nextdoor.bender.testutils.DummyOperationHelper.DummyNullOperation;
import com.nextdoor.bender.testutils.DummyOperationHelper.DummyOperationFactory;
import com.nextdoor.bender.testutils.DummyThrottleOperationHelper.DummyThrottleOperationFactory;
import com.nextdoor.bender.testutils.DummyAppendOperationHelper.DummyAppendOperationConfig;
import com.nextdoor.bender.testutils.DummyAppendOperationHelper.DummyAppendOperationFactory;
import com.oath.cyclops.async.adapters.Queue;

public class ForkOperationTest {

  private void supply(int count, Queue<InternalEvent> input) {
    new Thread(new Runnable() {
      @Override
      public void run() {
        for (int i = 0; i < count; i++) {
          InternalEvent ievent = new InternalEvent("" + i, null, 1);
          ievent.setEventObj(new DummyStringEvent("" + i));
          input.add(ievent);
        }
        input.close();
      }
    }).start();
  }

  @Before
  public void before() {
    Monitor.getInstance().reset();
  }

  @Test
  public void testSingleFork() {
    /*
     * Setup the pipeline of operation processors
     */
    List<List<OperationProcessor>> forks = new ArrayList<>();

    List<OperationProcessor> fork1 = new ArrayList<>();
    fork1.add(new OperationProcessor(new DummyOperationFactory()));
    forks.add(fork1);

    ForkOperation op = new ForkOperation(forks);

    /*
     * Create thread that supplies input events
     */
    Queue<InternalEvent> inputQueue = new Queue<>();
    supply(2, inputQueue);

    /*
     * Process
     */
    Stream<InternalEvent> input = inputQueue.stream();
    Stream<InternalEvent> output = op.getOutputStream(input);

    List<String> actual = output.map(InternalEvent::getEventString).collect(Collectors.toList());
    List<String> expected = Arrays.asList("0", "1");

    assertEquals(2, actual.size());
    assertTrue(expected.containsAll(actual));
  }

  @Test
  public void testTwoForks() {
    /*
     * Setup the pipeline of operation processors
     */
    List<List<OperationProcessor>> forks = new ArrayList<>();

    List<OperationProcessor> fork1 = new ArrayList<>();
    fork1.add(new OperationProcessor(new DummyOperationFactory()));
    forks.add(fork1);

    List<OperationProcessor> fork2 = new ArrayList<>();
    fork2.add(new OperationProcessor(new DummyOperationFactory()));
    forks.add(fork2);

    ForkOperation op = new ForkOperation(forks);

    /*
     * Create thread that supplies input events
     */
    Queue<InternalEvent> inputQueue = new Queue<>();
    supply(2, inputQueue);

    /*
     * Process
     */
    Stream<InternalEvent> input = inputQueue.stream();
    Stream<InternalEvent> output = op.getOutputStream(input);

    List<String> actual = output.map(InternalEvent::getEventString).collect(Collectors.toList());
    List<String> expected = Arrays.asList("0", "1", "0", "1");

    assertEquals(4, actual.size());
    assertTrue(expected.containsAll(actual));
  }

  @Test
  public void testTwoForksSlow() {
    /*
     * Setup the pipeline of operation processors
     */
    List<List<OperationProcessor>> forks = new ArrayList<>();

    List<OperationProcessor> fork1 = new ArrayList<>();
    fork1.add(new OperationProcessor(new DummyThrottleOperationFactory()));
    forks.add(fork1);

    List<OperationProcessor> fork2 = new ArrayList<>();
    fork2.add(new OperationProcessor(new DummyOperationFactory()));
    forks.add(fork2);

    ForkOperation op = new ForkOperation(forks);

    /*
     * Create thread that supplies input events
     */
    Queue<InternalEvent> inputQueue = new Queue<>();
    supply(1000, inputQueue);

    /*
     * Process
     */
    Stream<InternalEvent> input = inputQueue.stream();
    Stream<InternalEvent> output = op.getOutputStream(input);

    List<String> actual = output.map(InternalEvent::getEventString).collect(Collectors.toList());

    assertEquals(2000, actual.size());
  }

  @Test
  public void testThreeForks() {
    /*
     * Setup the pipeline of operation processors
     */
    List<List<OperationProcessor>> forks = new ArrayList<>();

    List<OperationProcessor> fork1 = new ArrayList<>();
    fork1.add(new OperationProcessor(new DummyThrottleOperationFactory()));
    forks.add(fork1);

    List<OperationProcessor> fork2 = new ArrayList<>();
    fork2.add(new OperationProcessor(new DummyOperationFactory()));
    forks.add(fork2);

    List<OperationProcessor> fork3 = new ArrayList<>();
    fork3.add(new OperationProcessor(new DummyOperationFactory()));
    forks.add(fork3);

    ForkOperation op = new ForkOperation(forks);

    /*
     * Create thread that supplies input events
     */
    Queue<InternalEvent> inputQueue = new Queue<>();
    supply(10, inputQueue);

    /*
     * Process
     */
    Stream<InternalEvent> input = inputQueue.stream();
    Stream<InternalEvent> output = op.getOutputStream(input);

    List<String> actual = output.map(InternalEvent::getEventString).collect(Collectors.toList());

    assertEquals(30, actual.size());
  }

  @Test
  public void testNullOpFork1() {
    /*
     * Setup the pipeline of operation processors
     */
    List<List<OperationProcessor>> forks = new ArrayList<>();

    List<OperationProcessor> fork1 = new ArrayList<>();
    fork1.add(new OperationProcessor(new DummyOperationFactory()));
    forks.add(fork1);

    List<OperationProcessor> fork2 = new ArrayList<>();
    DummyNullOperation nullOp = new DummyNullOperation();
    fork2.add(new OperationProcessor(new DummyOperationFactory(nullOp)));
    forks.add(fork2);

    ForkOperation op = new ForkOperation(forks);

    /*
     * Create thread that supplies input events
     */
    Queue<InternalEvent> inputQueue = new Queue<>();
    supply(10, inputQueue);

    /*
     * Process
     */
    Stream<InternalEvent> input = inputQueue.stream();
    Stream<InternalEvent> output = op.getOutputStream(input);

    List<String> actual = output.map(InternalEvent::getEventString).collect(Collectors.toList());

    assertEquals(10, actual.size());
  }

  @Test
  public void testNullOpFork() {
    /*
     * Setup the pipeline of operation processors
     */
    List<List<OperationProcessor>> forks = new ArrayList<>();

    List<OperationProcessor> fork1 = new ArrayList<>();
    DummyNullOperation nullOp = new DummyNullOperation();
    fork1.add(new OperationProcessor(new DummyOperationFactory(nullOp)));
    forks.add(fork1);

    ForkOperation op = new ForkOperation(forks);

    /*
     * Create thread that supplies input events
     */
    Queue<InternalEvent> inputQueue = new Queue<>();
    supply(10, inputQueue);

    /*
     * Process
     */
    Stream<InternalEvent> input = inputQueue.stream();
    Stream<InternalEvent> output = op.getOutputStream(input);

    List<String> actual = output.map(InternalEvent::getEventString).collect(Collectors.toList());

    assertEquals(0, actual.size());
  }

  @Test
  public void testForkMonitoringSeries() {
    /*
     * Setup the pipeline of operation processors
     */
    List<List<OperationProcessor>> forks = new ArrayList<>();

    List<OperationProcessor> fork1 = new ArrayList<>();
    fork1.add(new OperationProcessor(new DummyThrottleOperationFactory()));
    fork1.add(new OperationProcessor(new DummyOperationFactory()));
    forks.add(fork1);


    ForkOperation op = new ForkOperation(forks);

    /*
     * Create thread that supplies input events
     */
    Queue<InternalEvent> inputQueue = new Queue<>();
    supply(10, inputQueue);

    /*
     * Process
     */
    Stream<InternalEvent> input = inputQueue.stream();
    Stream<InternalEvent> output = op.getOutputStream(input);

    List<String> outputEvents =
        output.map(InternalEvent::getEventString).collect(Collectors.toList());

    Monitor m = Monitor.getInstance();

    List<Stat> stats = m.getStats();

    Map<String, String> actual = new HashMap<>();
    for (Stat stat : stats) {
      String key =
          String.format("%s.%s", stat.getTags().iterator().next().getValue(), stat.getName());
      actual.put(key, "" + stat.getValue());
    }

    assertEquals("0", actual
        .get("com.nextdoor.bender.testutils.DummyOperationHelper.DummyOperation.error.count"));
    assertEquals("10", actual
        .get("com.nextdoor.bender.testutils.DummyOperationHelper.DummyOperation.success.count"));
    assertEquals("10", actual.get(
        "com.nextdoor.bender.testutils.DummyThrottleOperationHelper.DummyThrottleOperation.success.count"));
    assertEquals("0", actual.get(
        "com.nextdoor.bender.testutils.DummyThrottleOperationHelper.DummyThrottleOperation.error.count"));
  }

  @Test
  public void testForkMonitoringParallel() {
    /*
     * Setup the pipeline of operation processors
     */
    List<List<OperationProcessor>> forks = new ArrayList<>();

    List<OperationProcessor> fork1 = new ArrayList<>();
    fork1.add(new OperationProcessor(new DummyOperationFactory()));
    forks.add(fork1);

    List<OperationProcessor> fork2 = new ArrayList<>();
    fork2.add(new OperationProcessor(new DummyThrottleOperationFactory()));
    forks.add(fork2);

    ForkOperation op = new ForkOperation(forks);

    /*
     * Create thread that supplies input events
     */
    Queue<InternalEvent> inputQueue = new Queue<>();
    supply(10, inputQueue);

    /*
     * Process
     */
    Stream<InternalEvent> input = inputQueue.stream();
    Stream<InternalEvent> output = op.getOutputStream(input);

    List<String> outputEvents =
        output.map(InternalEvent::getEventString).collect(Collectors.toList());

    Monitor m = Monitor.getInstance();

    List<Stat> stats = m.getStats();

    Map<String, String> actual = new HashMap<>();
    for (Stat stat : stats) {
      String key =
          String.format("%s.%s", stat.getTags().iterator().next().getValue(), stat.getName());
      actual.put(key, "" + stat.getValue());
    }

    assertEquals("0", actual
        .get("com.nextdoor.bender.testutils.DummyOperationHelper.DummyOperation.error.count"));
    assertEquals("10", actual
        .get("com.nextdoor.bender.testutils.DummyOperationHelper.DummyOperation.success.count"));
    assertEquals("10", actual.get(
        "com.nextdoor.bender.testutils.DummyThrottleOperationHelper.DummyThrottleOperation.success.count"));
    assertEquals("0", actual.get(
        "com.nextdoor.bender.testutils.DummyThrottleOperationHelper.DummyThrottleOperation.error.count"));
  }

  @Test
  public void testEventCloning() {
    /*
     * Setup the pipeline of operation processors
     */
    List<List<OperationProcessor>> forks = new ArrayList<>();

    /*
     * Fork 1 that adds a "+"
     */
    List<OperationProcessor> fork1 = new ArrayList<>();

    DummyAppendOperationFactory pos = new DummyAppendOperationFactory();
    DummyAppendOperationConfig posConf = new DummyAppendOperationConfig();
    posConf.setAppendStr("+");
    pos.setConf(posConf);
    fork1.add(new OperationProcessor(pos));
    forks.add(fork1);

    /*
     * Fork 2 that adds a "-"
     */
    List<OperationProcessor> fork2 = new ArrayList<>();

    DummyAppendOperationFactory neg = new DummyAppendOperationFactory();
    DummyAppendOperationConfig negConf = new DummyAppendOperationConfig();
    negConf.setAppendStr("-");
    neg.setConf(negConf);
    fork2.add(new OperationProcessor(neg));
    forks.add(fork2);

    ForkOperation op = new ForkOperation(forks);

    /*
     * Create thread that supplies input events
     */
    Queue<InternalEvent> inputQueue = new Queue<>();
    supply(2, inputQueue);

    /*
     * Process
     */
    Stream<InternalEvent> input = inputQueue.stream();
    Stream<InternalEvent> output = op.getOutputStream(input);

    List<String> actual = output.map(m -> {
      return m.getEventObj().getPayload().toString();
    }).collect(Collectors.toList());

    List<String> expected = Arrays.asList("0+", "1+", "0-", "1-");

    assertEquals(4, actual.size());
    assertTrue(expected.containsAll(actual));
  }
}
