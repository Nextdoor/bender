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

package com.nextdoor.bender.handler;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.amazonaws.services.lambda.runtime.Context;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.InternalEventIterator;
import com.nextdoor.bender.aws.TestContext;
import com.nextdoor.bender.config.Source;
import com.nextdoor.bender.deserializer.DeserializationException;
import com.nextdoor.bender.deserializer.Deserializer;
import com.nextdoor.bender.deserializer.DeserializerProcessor;
import com.nextdoor.bender.ipc.IpcSenderService;
import com.nextdoor.bender.ipc.TransportBuffer;
import com.nextdoor.bender.ipc.TransportException;
import com.nextdoor.bender.ipc.TransportFactory;
import com.nextdoor.bender.operation.BaseOperation;
import com.nextdoor.bender.operation.OperationException;
import com.nextdoor.bender.operation.OperationProcessor;
import com.nextdoor.bender.serializer.SerializationException;
import com.nextdoor.bender.serializer.Serializer;
import com.nextdoor.bender.testutils.DummyTransportHelper.ArrayTransportBuffer;
import com.nextdoor.bender.testutils.DummyTransportHelper.BufferedTransporter;

public class BaseHandlerTest {

  public static class DummyEvent {
    public String payload;
    public long timestamp;

    public DummyEvent(String payload, long timestamp) {
      this.payload = payload;
      this.timestamp = timestamp;
    }
  }

  public static class DummyEventIterator implements InternalEventIterator<InternalEvent> {
    private final Iterator<DummyEvent> events;
    private final Context context;

    public DummyEventIterator(List<DummyEvent> events, Context context) {
      this.events = events.iterator();
      this.context = context;
    }

    @Override
    public boolean hasNext() {
      return events.hasNext();
    }

    @Override
    public InternalEvent next() {
      DummyEvent ev = events.next();
      return new InternalEvent(ev.payload, context, ev.timestamp);
    }

    @Override
    public void close() throws IOException {}
  }

  public static class DummyHandler extends BaseHandler<List<DummyEvent>> {
    private DummyEventIterator eventIterator;

    /*
     * Created outside of getHandlerMetadata() for the purpose of test validation only
     */
    private HandlerMetadata metadata = new HandlerMetadata();

    @Override
    public void handler(List<DummyEvent> events, Context context) throws HandlerException {
      if (!initialized) {
        init(context);
      }

      this.eventIterator = new DummyEventIterator(events, context);

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

    @Override
    public HandlerMetadata getHandlerMetadata() {
      return metadata;
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
  public void testEndToEnd() throws HandlerException {
    BaseHandler.CONFIG_FILE = "/config/handler_config.json";

    List<DummyEvent> events = new ArrayList<DummyEvent>(2);
    events.add(new DummyEvent("foo", 0));
    events.add(new DummyEvent("bar", 0));

    TestContext context = new TestContext();
    context.setInvokedFunctionArn("arn:aws:lambda:us-east-1:123:function:test:tag");
    context.setFunctionName("test");
    handler.handler(events, context);

    /**
     * Verify that the metadata context is populated with something.
     */
    List<String> expectedMetadataFields = Arrays.asList("functionVersion", "functionName",
        "eventSource", "processingTime");
    assertEquals(expectedMetadataFields, handler.getHandlerMetadata().getFields());
    assertEquals("test", handler.getHandlerMetadata().getField("functionName"));

    /*
     * Verify Events made it all the way through
     */
    assertEquals(2, BufferedTransporter.output.size());
    assertEquals("foo", BufferedTransporter.output.get(0));
    assertEquals("bar", BufferedTransporter.output.get(1));
  }

  @Test
  public void testEndToEndWithNoOperations() throws HandlerException {
    BaseHandler.CONFIG_FILE = "/config/handler_config_no_operations.json";

    List<DummyEvent> events = new ArrayList<DummyEvent>(2);
    events.add(new DummyEvent("foo", 0));
    events.add(new DummyEvent("bar", 0));

    TestContext context = new TestContext();
    context.setInvokedFunctionArn("arn:aws:lambda:us-east-1:123:function:test:tag");
    handler.handler(events, context);

    /*
     * Verify Events made it all the way through
     */
    assertEquals(2, BufferedTransporter.output.size());
    assertEquals("foo", BufferedTransporter.output.get(0));
    assertEquals("bar", BufferedTransporter.output.get(1));
  }

  @Test
  public void testLatestTagConfig() throws HandlerException {
    List<DummyEvent> events = new ArrayList<DummyEvent>(1);

    TestContext context = new TestContext();
    context.setInvokedFunctionArn("arn:aws:lambda:us-east-1:123:function:test");
    handler.handler(events, context);

    assertEquals("/config/$LATEST.json", handler.config.getConfigFile());
  }

  @Test
  public void testTagConfig() throws HandlerException {
    List<DummyEvent> events = new ArrayList<DummyEvent>(1);

    TestContext context = new TestContext();
    context.setInvokedFunctionArn("arn:aws:lambda:us-east-1:123:function:test:staging");
    handler.handler(events, context);

    assertEquals("/config/staging.json", handler.config.getConfigFile());
  }

  @Test(expected = HandlerException.class)
  public void testBadConfig() throws HandlerException {
    BaseHandler.CONFIG_FILE = "/config/handler_config_bad.json";

    List<DummyEvent> events = new ArrayList<DummyEvent>(1);

    TestContext context = new TestContext();
    context.setInvokedFunctionArn("arn:aws:lambda:us-east-1:123:function:test:tag");
    handler.handler(events, context);
  }

  @Test(expected = HandlerException.class)
  public void testMissingConfig() throws HandlerException {
    BaseHandler.CONFIG_FILE = "/config/missing.json";

    List<DummyEvent> events = new ArrayList<DummyEvent>(1);

    TestContext context = new TestContext();
    context.setInvokedFunctionArn("arn:aws:lambda:us-east-1:123:function:test:tag");
    handler.handler(events, context);
  }

  @Test(expected = TransportException.class)
  public void testGeneralTransportExceptionOnShutdown() throws Throwable {
    BaseHandler.CONFIG_FILE = "/config/handler_config.json";

    List<DummyEvent> events = new ArrayList<DummyEvent>(1);
    events.add(new DummyEvent("foo", 0));

    TestContext context = new TestContext();
    context.setInvokedFunctionArn("arn:aws:lambda:us-east-1:123:function:test:tag");
    handler.init(context);

    IpcSenderService spy = spy(handler.getIpcService());
    handler.setIpcService(spy);

    doThrow(new TransportException("expected")).when(spy).shutdown();

    try {
      handler.handler(events, context);
    } catch (Exception e) {
      throw e.getCause().getCause();
    }
  }

  @Test(expected = InterruptedException.class)
  public void testInterruptedExceptionOnShutdown() throws Throwable {
    BaseHandler.CONFIG_FILE = "/config/handler_config.json";

    List<DummyEvent> events = new ArrayList<DummyEvent>(1);
    events.add(new DummyEvent("foo", 0));

    TestContext context = new TestContext();
    context.setInvokedFunctionArn("arn:aws:lambda:us-east-1:123:function:test:tag");
    handler.init(context);

    IpcSenderService spy = spy(handler.getIpcService());
    handler.setIpcService(spy);

    doThrow(new InterruptedException("expected")).when(spy).shutdown();

    try {
      handler.handler(events, context);
    } catch (Exception e) {
      throw e.getCause().getCause();
    }
  }

  @Test(expected = TransportException.class)
  public void testTransportOnSendFailure() throws Throwable {
    BaseHandler.CONFIG_FILE = "/config/handler_config.json";

    List<DummyEvent> events = new ArrayList<DummyEvent>(2);
    events.add(new DummyEvent("foo", 0));
    events.add(new DummyEvent("bar", 0));

    TestContext context = new TestContext();
    context.setInvokedFunctionArn("arn:aws:lambda:us-east-1:123:function:test:tag");
    handler.init(context);

    IpcSenderService spyIpc = spy(handler.getIpcService());
    TransportFactory tf = spy(handler.getIpcService().getTransportFactory());
    BufferedTransporter mockTransport = mock(BufferedTransporter.class);
    doThrow(new TransportException("expected")).when(mockTransport).sendBatch(any());
    when(tf.newInstance()).thenReturn(mockTransport);
    spyIpc.setTransportFactory(tf);

    handler.setIpcService(spyIpc);

    try {
      handler.handler(events, context);
    } catch (Exception e) {
      throw e.getCause().getCause();
    }
  }

  @Test
  public void testIpcOnAddFailure() throws Throwable {
    BaseHandler.CONFIG_FILE = "/config/handler_config.json";
    handler.skipWriteStats = true;

    List<DummyEvent> events = new ArrayList<DummyEvent>(2);
    events.add(new DummyEvent("foo", 0));
    events.add(new DummyEvent("bar", 0));

    TestContext context = new TestContext();
    context.setInvokedFunctionArn("arn:aws:lambda:us-east-1:123:function:test:tag");
    handler.init(context);

    TransportBuffer tbSpy1 = spy(new ArrayTransportBuffer());
    TransportBuffer tbSpy2 = spy(new ArrayTransportBuffer());

    doCallRealMethod().doCallRealMethod().when(tbSpy1).add(any());
    doThrow(new IllegalStateException("expected")).when(tbSpy2).add(any());

    IpcSenderService spyIpc = spy(handler.getIpcService());
    TransportFactory tfSpy = spy(spyIpc.getTransportFactory());
    when(tfSpy.newTransportBuffer()).thenReturn(tbSpy1, tbSpy2);
    spyIpc.setTransportFactory(tfSpy);

    handler.setIpcService(spyIpc);
    handler.handler(events, context);

    assertEquals(1, spyIpc.getSuccessCountStat().getValue());
  }

  @Test
  public void testDeserializationException() throws HandlerException {
    BaseHandler.CONFIG_FILE = "/config/handler_config.json";
    handler.skipWriteStats = true;

    List<DummyEvent> events = new ArrayList<DummyEvent>(1);
    events.add(new DummyEvent("foo", 0));

    TestContext context = new TestContext();
    context.setInvokedFunctionArn("arn:aws:lambda:us-east-1:123:function:test:tag");
    handler.init(context);

    DeserializerProcessor proc = handler.sources.get(0).getDeserProcessor();

    Deserializer deserSpy = spy(proc.getDeserializer());
    doThrow(new DeserializationException("expected")).when(deserSpy).deserialize(anyString());
    proc.setDeserializer(deserSpy);

    handler.handler(events, context);
    assertEquals(1, proc.getErrorCountStat().getValue());
  }

  @Test
  public void testFilterFailedDeserialization() throws HandlerException {
    BaseHandler.CONFIG_FILE = "/config/handler_config.json";
    handler.skipWriteStats = true;

    List<DummyEvent> events = new ArrayList<DummyEvent>(1);
    events.add(new DummyEvent("foo", 0));

    TestContext context = new TestContext();
    context.setInvokedFunctionArn("arn:aws:lambda:us-east-1:123:function:test:tag");
    handler.init(context);

    DeserializerProcessor proc = handler.sources.get(0).getDeserProcessor();

    Deserializer deserSpy = spy(proc.getDeserializer());
    doThrow(new DeserializationException("expected")).when(deserSpy).deserialize(anyString());
    proc.setDeserializer(deserSpy);

    handler.handler(events, context);
    assertEquals(0, BufferedTransporter.output.size());
  }

  @Test
  public void testFilterNullDeserialization() throws HandlerException {
    BaseHandler.CONFIG_FILE = "/config/handler_config.json";
    handler.skipWriteStats = true;

    List<DummyEvent> events = new ArrayList<DummyEvent>(1);
    events.add(new DummyEvent("foo", 0));

    TestContext context = new TestContext();
    context.setInvokedFunctionArn("arn:aws:lambda:us-east-1:123:function:test:tag");
    handler.init(context);

    DeserializerProcessor proc = handler.sources.get(0).getDeserProcessor();

    Deserializer deserSpy = spy(proc.getDeserializer());
    when(deserSpy.deserialize(anyString())).thenReturn(null);
    proc.setDeserializer(deserSpy);

    handler.handler(events, context);
    assertEquals(0, BufferedTransporter.output.size());
  }

  @Test
  public void testSerializationException() throws HandlerException, SerializationException {
    BaseHandler.CONFIG_FILE = "/config/handler_config.json";
    handler.skipWriteStats = true;

    List<DummyEvent> events = new ArrayList<DummyEvent>(1);
    events.add(new DummyEvent("foo", 0));

    TestContext context = new TestContext();
    context.setInvokedFunctionArn("arn:aws:lambda:us-east-1:123:function:test:tag");
    handler.init(context);

    Serializer serSpy = spy(handler.ser.getSerializer());
    doThrow(new DeserializationException("")).when(serSpy).serialize(any());

    handler.ser.setSerializer(serSpy);

    handler.handler(events, context);
    assertEquals(1, handler.ser.getErrorCountStat().getValue());
  }

  @Test
  public void testOperationException() throws HandlerException {
    BaseHandler.CONFIG_FILE = "/config/handler_config.json";
    handler.skipWriteStats = true;

    List<DummyEvent> events = new ArrayList<DummyEvent>(1);
    events.add(new DummyEvent("foo", 0));

    TestContext context = new TestContext();
    context.setInvokedFunctionArn("arn:aws:lambda:us-east-1:123:function:test:tag");
    handler.init(context);

    List<OperationProcessor> operationProcessors = handler.sources.get(0).getOperationProcessors();
    for (OperationProcessor operationProcessor : operationProcessors) {
      BaseOperation operation = spy(operationProcessor.getOperation());
      doThrow(new OperationException("expected")).when(operation).perform(any());
      operationProcessor.setOperation(operation);
    }

    handler.handler(events, context);
    assertEquals(1, operationProcessors.get(0).getErrorCountStat().getValue());
  }

  @Test
  public void testMultipleOperationsConfig() throws HandlerException {
    BaseHandler.CONFIG_FILE = "/config/handler_config_two_operations.json";

    List<DummyEvent> events = new ArrayList<DummyEvent>(1);
    events.add(new DummyEvent("foo", 0));

    TestContext context = new TestContext();
    context.setInvokedFunctionArn("arn:aws:lambda:us-east-1:123:function:test:tag");
    handler.init(context);

    List<OperationProcessor> operationProcessores = handler.sources.get(0).getOperationProcessors();

    for (int i = 0; i < operationProcessores.size(); i++) {
      OperationProcessor operationProcessor = spy(operationProcessores.get(i));
      operationProcessores.set(i, operationProcessor);
    }

    handler.handler(events, context);

    /*
     * 2 operations specified in the config file
     */
    verify(operationProcessores.get(0), times(1)).perform(any());
    verify(operationProcessores.get(1), times(1)).perform(any());
  }

  @Test
  public void testContains() throws HandlerException {
    BaseHandler.CONFIG_FILE = "/config/handler_config_contains.json";

    List<DummyEvent> events = new ArrayList<DummyEvent>(2);
    events.add(new DummyEvent("foo", 0));
    events.add(new DummyEvent("bar", 0));
    events.add(new DummyEvent("loop", 0));

    TestContext context = new TestContext();
    context.setInvokedFunctionArn("arn:aws:lambda:us-east-1:123:function:test:tag");
    handler.handler(events, context);

    /*
     * Verify Events made it all the way through
     */
    assertEquals(1, BufferedTransporter.output.size());
    assertEquals("bar", BufferedTransporter.output.get(0));
  }

  @Test
  public void testRegex() throws HandlerException {
    BaseHandler.CONFIG_FILE = "/config/handler_config_regex.json";

    List<DummyEvent> events = new ArrayList<DummyEvent>(2);
    events.add(new DummyEvent("foo", 0));
    events.add(new DummyEvent("bar", 0));
    events.add(new DummyEvent("loop", 0));

    TestContext context = new TestContext();
    context.setInvokedFunctionArn("arn:aws:lambda:us-east-1:123:function:test:tag");
    handler.handler(events, context);

    /*
     * Verify Events made it all the way through
     */
    assertEquals(1, BufferedTransporter.output.size());
    assertEquals("bar", BufferedTransporter.output.get(0));
  }
}
