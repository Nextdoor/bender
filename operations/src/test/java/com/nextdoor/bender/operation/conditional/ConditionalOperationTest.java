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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.monitoring.Monitor;
import com.nextdoor.bender.operation.FilterOperation;
import com.nextdoor.bender.operation.OperationProcessor;
import com.nextdoor.bender.operation.conditional.ConditionalOperation;
import com.nextdoor.bender.operation.filter.BasicFilterOperation;
import com.nextdoor.bender.operation.filter.BasicFilterOperationConfig;
import com.nextdoor.bender.operation.filter.BasicFilterOperationFactory;
import com.nextdoor.bender.testutils.DummyAppendOperationHelper.DummyAppendOperationConfig;
import com.nextdoor.bender.testutils.DummyAppendOperationHelper.DummyAppendOperationFactory;
import com.nextdoor.bender.testutils.DummyDeserializerHelper.DummyStringEvent;
import com.oath.cyclops.async.adapters.Queue;

public class ConditionalOperationTest {

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
  public void testSingleConditionMatch() {
    /*
     * Setup the pipeline of operation processors
     */
    List<Pair<FilterOperation, List<OperationProcessor>>> conditions =
        new ArrayList<>();
    List<OperationProcessor> case1Ops = new ArrayList<>();

    DummyAppendOperationFactory pos = new DummyAppendOperationFactory();
    DummyAppendOperationConfig posConf = new DummyAppendOperationConfig();
    posConf.setAppendStr("+");
    pos.setConf(posConf);
    case1Ops.add(new OperationProcessor(pos));
    FilterOperation filter = new BasicFilterOperation(true);
    conditions.add(new ImmutablePair<>(filter, case1Ops));

    ConditionalOperation op = new ConditionalOperation(conditions, false);

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
    List<String> expected = Arrays.asList("0+", "1+");

    assertEquals(2, actual.size());
    assertTrue(expected.containsAll(actual));
  }

  @Test
  public void testSingleConditionNoMatch() {
    /*
     * Setup the pipeline of operation processors
     */
    List<Pair<FilterOperation, List<OperationProcessor>>> conditions =
        new ArrayList<>();
    List<OperationProcessor> case1Ops = new ArrayList<>();

    DummyAppendOperationFactory pos = new DummyAppendOperationFactory();
    DummyAppendOperationConfig posConf = new DummyAppendOperationConfig();
    posConf.setAppendStr("+");
    pos.setConf(posConf);
    case1Ops.add(new OperationProcessor(pos));
    FilterOperation filter = new BasicFilterOperation(false);
    conditions.add(new ImmutablePair<>(filter, case1Ops));

    ConditionalOperation op = new ConditionalOperation(conditions, false);

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
    List<String> expected = Arrays.asList("0", "1");

    assertEquals(2, actual.size());
    assertTrue(expected.containsAll(actual));
  }

  @Test
  public void testTwoConditions() {
    List<Pair<FilterOperation, List<OperationProcessor>>> conditions =
        new ArrayList<>();
    /*
     * Case 1
     */
    List<OperationProcessor> case1Ops = new ArrayList<>();

    DummyAppendOperationFactory pos = new DummyAppendOperationFactory();
    DummyAppendOperationConfig posConf = new DummyAppendOperationConfig();
    posConf.setAppendStr("+");
    pos.setConf(posConf);
    case1Ops.add(new OperationProcessor(pos));
    FilterOperation case1Filter = new BasicFilterOperation(false);
    conditions
        .add(new ImmutablePair<>(case1Filter, case1Ops));

    /*
     * Case 2
     */
    List<OperationProcessor> case2Ops = new ArrayList<>();

    DummyAppendOperationFactory neg = new DummyAppendOperationFactory();
    DummyAppendOperationConfig negConf = new DummyAppendOperationConfig();
    negConf.setAppendStr("-");
    neg.setConf(negConf);
    case2Ops.add(new OperationProcessor(neg));
    FilterOperation case2Filter = new BasicFilterOperation(true);
    conditions
        .add(new ImmutablePair<>(case2Filter, case2Ops));

    ConditionalOperation op = new ConditionalOperation(conditions, false);

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
    List<String> expected = Arrays.asList("0-", "1-");

    assertEquals(2, actual.size());
    assertTrue(expected.containsAll(actual));
  }

  @Test
  public void testTwoConditionsNoMatch() {
    List<Pair<FilterOperation, List<OperationProcessor>>> conditions =
        new ArrayList<>();
    /*
     * Case 1
     */
    List<OperationProcessor> case1Ops = new ArrayList<>();

    DummyAppendOperationFactory pos = new DummyAppendOperationFactory();
    DummyAppendOperationConfig posConf = new DummyAppendOperationConfig();
    posConf.setAppendStr("+");
    pos.setConf(posConf);
    case1Ops.add(new OperationProcessor(pos));
    FilterOperation case1Filter = new BasicFilterOperation(false);
    conditions
        .add(new ImmutablePair<>(case1Filter, case1Ops));

    /*
     * Case 2
     */
    List<OperationProcessor> case2Ops = new ArrayList<>();

    DummyAppendOperationFactory neg = new DummyAppendOperationFactory();
    DummyAppendOperationConfig negConf = new DummyAppendOperationConfig();
    negConf.setAppendStr("-");
    neg.setConf(negConf);
    case2Ops.add(new OperationProcessor(neg));
    FilterOperation case2Filter = new BasicFilterOperation(false);
    conditions
        .add(new ImmutablePair<>(case2Filter, case2Ops));

    ConditionalOperation op = new ConditionalOperation(conditions, false);

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
    List<String> expected = Arrays.asList("0", "1");

    assertEquals(2, actual.size());
    assertTrue(expected.containsAll(actual));
  }

  @Test
  public void testFilterCondition() {
    List<Pair<FilterOperation, List<OperationProcessor>>> conditions =
        new ArrayList<>();
    /*
     * Case 1
     */
    List<OperationProcessor> case1Ops = new ArrayList<>();
    BasicFilterOperationFactory fOp = new BasicFilterOperationFactory();
    BasicFilterOperationConfig fOpConf = new BasicFilterOperationConfig();
    fOpConf.setPass(false);
    fOp.setConf(fOpConf);

    case1Ops.add(new OperationProcessor(fOp));


    FilterOperation case1Filter = new BasicFilterOperation(true);
    conditions
        .add(new ImmutablePair<>(case1Filter, case1Ops));


    ConditionalOperation op = new ConditionalOperation(conditions, false);

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

    assertEquals(0, actual.size());
  }
  
  public void testFilterNonMatch() {
    List<Pair<FilterOperation, List<OperationProcessor>>> conditions =
        new ArrayList<>();
    /*
     * Case 1
     */
    List<OperationProcessor> case1Ops = new ArrayList<>();

    FilterOperation case1Filter = new BasicFilterOperation(false);
    conditions
        .add(new ImmutablePair<>(case1Filter, case1Ops));


    ConditionalOperation op = new ConditionalOperation(conditions, true);

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

    assertEquals(0, actual.size());
  }
}
