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
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.nextdoor.bender.handler.BaseHandlerQueueTest.DummyHandler;
import com.nextdoor.bender.monitoring.Monitor;
import com.nextdoor.bender.testutils.DummyTransportHelper.BufferedTransporter;
import com.oath.cyclops.async.adapters.Queue;
import com.nextdoor.bender.aws.TestContext;
import com.nextdoor.bender.handler.BaseHandler;
import com.nextdoor.bender.handler.HandlerException;
import com.nextdoor.bender.handler.BaseHandlerQueueTest.DummyEvent;


public class ForkOperationFactoryTest {

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
  public void testTwoForksSmallInput() throws HandlerException {
    BaseHandler.CONFIG_FILE = "/config/handler_config_two_fork.yaml";

    TestContext context = new TestContext();
    context.setInvokedFunctionArn("arn:aws:lambda:us-east-1:123:function:test:tag");
    handler.handler(getEvents(2), context);

    List<String> expected = Arrays.asList("0+-", "0-+", "1+-", "1-+");

    /*
     * Verify Events made it all the way through
     */
    assertEquals(4, BufferedTransporter.output.size());
    assertTrue(expected.containsAll(BufferedTransporter.output));
  }

  @Test
  public void testTwoForksLargeInput() throws HandlerException {
    BaseHandler.CONFIG_FILE = "/config/handler_config_two_fork.yaml";

    TestContext context = new TestContext();
    context.setInvokedFunctionArn("arn:aws:lambda:us-east-1:123:function:test:tag");
    handler.handler(getEvents(10000), context);

    List<String> expected = new LinkedList<>();
    for (int i = 0; i < 10000; i++) {
      expected.add(i + "+-");
      expected.add(i + "-+");
    }

    /*
     * Verify Events made it all the way through
     */
    assertEquals(20000, BufferedTransporter.output.size());
    assertTrue(expected.containsAll(BufferedTransporter.output));
  }

  @Test
  public void testThreeForksSmallInput() throws HandlerException {
    BaseHandler.CONFIG_FILE = "/config/handler_config_three_fork.yaml";

    TestContext context = new TestContext();
    context.setInvokedFunctionArn("arn:aws:lambda:us-east-1:123:function:test:tag");
    handler.handler(getEvents(10), context);

    List<String> expected = new LinkedList<>();
    for (int i = 0; i < 10; i++) {
      expected.add(i + "+-");
      expected.add(i + "-+");
      expected.add(i + "_");
    }

    /*
     * Verify Events made it all the way through
     */
    assertEquals(30, BufferedTransporter.output.size());
    assertTrue(expected.containsAll(BufferedTransporter.output));
  }

  @Test
  public void testNestedForks() throws HandlerException {
    BaseHandler.CONFIG_FILE = "/config/handler_config_nested_fork.yaml";

    TestContext context = new TestContext();
    context.setInvokedFunctionArn("arn:aws:lambda:us-east-1:123:function:test:tag");
    handler.handler(getEvents(10), context);

    List<String> expected = new LinkedList<>();
    for (int i = 0; i < 10; i++) {
      expected.add(i + "+");
      expected.add(i + "-");
    }

    /*
     * Verify Events made it all the way through
     */
    assertEquals(20, BufferedTransporter.output.size());
    assertTrue(expected.containsAll(BufferedTransporter.output));
  }

  @Test
  public void testOperationReuse() throws HandlerException {
    BaseHandler.CONFIG_FILE = "/config/handler_config_two_fork.yaml";

    TestContext context = new TestContext();
    context.setInvokedFunctionArn("arn:aws:lambda:us-east-1:123:function:test:tag");
    handler.handler(getEvents(2), context);
    handler.handler(getEvents(2), context);

    List<String> expected = Arrays.asList("0+-", "0-+", "1+-", "1-+", "0+-", "0-+", "1+-", "1-+");

    /*
     * Verify Events made it all the way through
     */
    assertEquals(8, BufferedTransporter.output.size());
    assertTrue(expected.containsAll(BufferedTransporter.output));
  }

  @Test
  public void testSlowFork() throws HandlerException {
    BaseHandler.CONFIG_FILE = "/config/handler_config_slower_fork.yaml";

    TestContext context = new TestContext();
    context.setInvokedFunctionArn("arn:aws:lambda:us-east-1:123:function:test:tag");
    handler.handler(getEvents(1000), context);

    List<String> expected = new LinkedList<>();
    for (int i = 0; i < 1000; i++) {
      expected.add(i + "+");
      expected.add(i + "-");
    }

    /*
     * Verify Events made it all the way through
     */
    assertEquals(2000, BufferedTransporter.output.size());
    assertTrue(expected.containsAll(BufferedTransporter.output));
  }
}
