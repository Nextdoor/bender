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
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.nextdoor.bender.aws.TestContext;
import com.nextdoor.bender.handler.BaseHandler;
import com.nextdoor.bender.handler.HandlerException;
import com.nextdoor.bender.handler.BaseHandlerQueueTest.DummyEvent;
import com.nextdoor.bender.handler.BaseHandlerQueueTest.DummyHandler;
import com.nextdoor.bender.monitoring.Monitor;
import com.nextdoor.bender.testutils.DummyTransportHelper.BufferedTransporter;
import com.oath.cyclops.async.adapters.Queue;

public class ConditionalOperationFactoryTest {
  private DummyHandler handler;

  @Before
  public void before() {
    handler = new DummyHandler();
    BaseHandler.CONFIG_FILE = null;
  }

  @After
  public void after() {
    BufferedTransporter.output.clear();
    Monitor.getInstance().clearStats();
  }

  private Iterator<DummyEvent> getEvents(int count) {
    Queue<DummyEvent> q = new Queue<>();
    Iterator<DummyEvent> dummyEvents = q.stream().iterator();

    for (int i = 0; i < count; i++) {
      q.offer(new DummyEvent("" + i, 0));
    }
    q.close();

    return dummyEvents;
  }

  @Test
  public void testOneConditionSmallInput() throws HandlerException {
    BaseHandler.CONFIG_FILE = "/config/handler_config_one_condition.yaml";

    TestContext context = new TestContext();
    context.setInvokedFunctionArn("arn:aws:lambda:us-east-1:123:function:test:tag");
    handler.handler(getEvents(2), context);

    List<String> expected = Arrays.asList("0-", "1-");

    /*
     * Verify Events made it all the way through
     */
    assertEquals(2, BufferedTransporter.output.size());
    List<String> actual = BufferedTransporter.output;
    assertTrue(expected.containsAll(actual));
  }

  @Test
  public void testOneConditionLargeInput() throws HandlerException {
    BaseHandler.CONFIG_FILE = "/config/handler_config_one_condition.yaml";

    TestContext context = new TestContext();
    context.setInvokedFunctionArn("arn:aws:lambda:us-east-1:123:function:test:tag");
    handler.handler(getEvents(10000), context);

    List<String> expected = new LinkedList<>();
    for (int i = 0; i < 10000; i++) {
      expected.add(i + "-");
    }

    /*
     * Verify Events made it all the way through
     */
    assertEquals(10000, BufferedTransporter.output.size());
    List<String> actual = BufferedTransporter.output;
    assertTrue(expected.containsAll(actual));
  }

  @Test
  public void testTwoConditionsSmallInput() throws HandlerException {
    BaseHandler.CONFIG_FILE = "/config/handler_config_two_conditions.yaml";

    TestContext context = new TestContext();
    context.setInvokedFunctionArn("arn:aws:lambda:us-east-1:123:function:test:tag");
    handler.handler(getEvents(2), context);

    List<String> expected = Arrays.asList("0+", "1+");

    /*
     * Verify Events made it all the way through
     */
    assertEquals(2, BufferedTransporter.output.size());
    List<String> actual = BufferedTransporter.output;
    assertTrue(expected.containsAll(actual));
  }

  @Test
  public void testTwoConditionsLargeInput() throws HandlerException {
    BaseHandler.CONFIG_FILE = "/config/handler_config_two_conditions.yaml";

    TestContext context = new TestContext();
    context.setInvokedFunctionArn("arn:aws:lambda:us-east-1:123:function:test:tag");
    handler.handler(getEvents(10000), context);

    List<String> expected = new LinkedList<>();
    for (int i = 0; i < 10000; i++) {
      expected.add(i + "+");
    }

    /*
     * Verify Events made it all the way through
     */
    assertEquals(10000, BufferedTransporter.output.size());
    List<String> actual = BufferedTransporter.output;
    assertTrue(expected.containsAll(actual));
  }

  @Test
  public void testFilterOut() throws HandlerException {
    BaseHandler.CONFIG_FILE = "/config/handler_config_filterout.yaml";

    TestContext context = new TestContext();
    context.setInvokedFunctionArn("arn:aws:lambda:us-east-1:123:function:test:tag");
    handler.handler(getEvents(2), context);

    /*
     * Verify Events made it all the way through
     */
    assertEquals(0, BufferedTransporter.output.size());
  }

  @Test
  public void testSlowConditionBranch() throws HandlerException {
    BaseHandler.CONFIG_FILE = "/config/handler_config_one_condition_slow.yaml";

    TestContext context = new TestContext();
    context.setInvokedFunctionArn("arn:aws:lambda:us-east-1:123:function:test:tag");
    handler.handler(getEvents(1000), context);

    List<String> expected = new LinkedList<>();
    for (int i = 0; i < 1000; i++) {
      if (i % 2 == 0) {
        expected.add(i + "");
      } else {
        expected.add(i + "+");
      }
    }
    /*
     * Verify Events made it all the way through
     */
    List<String> actual = BufferedTransporter.output;

    assertEquals(1000, BufferedTransporter.output.size());
    assertTrue(expected.containsAll(actual));
  }
}
