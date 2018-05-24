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

package com.nextdoor.bender.handler;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Iterator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.amazonaws.services.lambda.runtime.Context;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.InternalEventIterator;
import com.nextdoor.bender.LambdaContext;
import com.nextdoor.bender.aws.TestContext;
import com.nextdoor.bender.config.Source;
import com.nextdoor.bender.testutils.DummyTransportHelper.BufferedTransporter;
import com.oath.cyclops.async.adapters.Queue;

public class BaseHandlerQueueTest {

  public static class DummyEvent {
    public String payload;
    public long timestamp;

    public DummyEvent(String payload, long timestamp) {
      this.payload = payload;
      this.timestamp = timestamp;
    }
  }

  public static class DummyEventIterator implements InternalEventIterator<InternalEvent> {
    private final LambdaContext context;
    private final Iterator<DummyEvent> itr;

    public DummyEventIterator(Iterator<DummyEvent> itr, LambdaContext context) {
      this.itr = itr;
      this.context = context;
    }

    @Override
    public boolean hasNext() {
      return this.itr.hasNext();
    }

    @Override
    public InternalEvent next() {
      DummyEvent ev = itr.next();
      return new InternalEvent(ev.payload, context, ev.timestamp);
    }

    @Override
    public void close() throws IOException {}
  }

  public static class DummyHandler extends BaseHandler<Iterator<DummyEvent>> {
    private DummyEventIterator eventIterator;

    @Override
    public void handler(Iterator<DummyEvent> events, Context context) throws HandlerException {
      if (!initialized) {
        init(context);
      }

      this.eventIterator = new DummyEventIterator(events, new LambdaContext(context));

      this.process(context);
    }

    @Override
    public Source getSource() {
      return this.sources.get(0);
    }

    @Override
    public String getSourceName() {
      return "unittest";
    }

    @Override
    public void onException(Exception e) {

    }

    @Override
    public InternalEventIterator<InternalEvent> getInternalEventIterator() {
      return this.eventIterator;
    }
  }

  @Before
  public void before() {
    handler = new DummyHandler();
    BaseHandler.CONFIG_FILE = null;
  }

  @After
  public void after() {
    BufferedTransporter.output.clear();
    handler.monitor.clearStats();
  }

  private DummyHandler handler;

  @Test
  public void testFastSourceFastConsumer() throws HandlerException {
    BaseHandler.CONFIG_FILE = "/config/handler_config_queue.yaml";

    Queue<DummyEvent> q = new Queue<DummyEvent>();
    Iterator<DummyEvent> dummyEvents = q.stream().iterator();

    for (int i = 0; i < 1000; i++) {
      q.offer(new DummyEvent("" + i, 0));
    }
    q.close();

    TestContext context = new TestContext();
    context.setInvokedFunctionArn("arn:aws:lambda:us-east-1:123:function:test:tag");
    handler.handler(dummyEvents, context);

    /*
     * Verify Events made it all the way through
     */
    assertEquals(1000, BufferedTransporter.output.size());
  }

  @Test
  public void testSlowSourceFastConsumer() throws HandlerException {
    BaseHandler.CONFIG_FILE = "/config/handler_config_queue.yaml";

    Queue<DummyEvent> q = new Queue<DummyEvent>();
    Iterator<DummyEvent> dummyEvents = q.stream().iterator();

    new Thread(new Runnable() {
      @Override
      public void run() {
        for (int i = 0; i < 1000; i++) {
          if (i % 500 == 0) {
            try {
              Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
          }
          q.offer(new DummyEvent("" + i, 0));
        }
        q.close();
      }
    }).start();

    TestContext context = new TestContext();
    context.setInvokedFunctionArn("arn:aws:lambda:us-east-1:123:function:test:tag");
    handler.handler(dummyEvents, context);

    /*
     * Verify Events made it all the way through
     */
    assertEquals(1000, BufferedTransporter.output.size());
  }

  @Test
  public void testSlowSourceSlowConsumer() throws HandlerException {
    BaseHandler.CONFIG_FILE = "/config/handler_config_queue_throttle.yaml";

    Queue<DummyEvent> q = new Queue<DummyEvent>();
    Iterator<DummyEvent> dummyEvents = q.stream().iterator();

    new Thread(new Runnable() {
      @Override
      public void run() {
        for (int i = 0; i < 1000; i++) {
          if (i % 500 == 0) {
            try {
              Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
          }
          q.offer(new DummyEvent("" + i, 0));
        }
        q.close();
      }
    }).start();

    TestContext context = new TestContext();
    context.setInvokedFunctionArn("arn:aws:lambda:us-east-1:123:function:test:tag");
    handler.handler(dummyEvents, context);

    /*
     * Verify Events made it all the way through
     */
    assertEquals(1000, BufferedTransporter.output.size());
  }

  @Test
  public void testFastSourceSlowConsumer() throws HandlerException {
    BaseHandler.CONFIG_FILE = "/config/handler_config_queue_throttle.yaml";

    Queue<DummyEvent> q = new Queue<DummyEvent>();
    Iterator<DummyEvent> dummyEvents = q.stream().iterator();

    for (int i = 0; i < 1000; i++) {
      q.offer(new DummyEvent("" + i, 0));
    }
    q.close();

    TestContext context = new TestContext();
    context.setInvokedFunctionArn("arn:aws:lambda:us-east-1:123:function:test:tag");
    handler.handler(dummyEvents, context);

    /*
     * Verify Events made it all the way through
     */
    assertEquals(1000, BufferedTransporter.output.size());
  }
}
