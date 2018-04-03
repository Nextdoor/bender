/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF
 * ANY KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * Copyright 2018 Nextdoor.com, Inc
 */

package com.nextdoor.bender.operation;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import com.google.gson.JsonSyntaxException;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.monitoring.Stat;
import com.nextdoor.bender.testutils.DummyDeserializerHelper;
import com.nextdoor.bender.testutils.DummyOperationHelper.DummyOperation;
import com.nextdoor.bender.testutils.DummyOperationHelper.DummyOperationFactory;

public class OperationProcessorTest {

  @Test
  public void testStatsLogging()
      throws JsonSyntaxException, UnsupportedEncodingException, IOException, OperationException {
    DummyOperationFactory mutatorFactory = new DummyOperationFactory();
    OperationProcessor processor = new OperationProcessor(mutatorFactory);

    /*
     * Mock the Stat object
     */
    Stat runtimeStat = mock(Stat.class);
    Stat successStat = mock(Stat.class);
    Stat errorStat = mock(Stat.class);

    processor.setRuntimeStat(runtimeStat);
    processor.setSuccessCountStat(successStat);
    processor.setErrorCountStat(errorStat);

    InternalEvent ievent = new InternalEvent("foo", null, 1);
    ievent.setEventObj(new DummyDeserializerHelper.DummyDeserializedEvent("test"));
    Stream<InternalEvent> stream = processor.perform(Stream.of(ievent));
    List<InternalEvent> output = stream.collect(Collectors.toList());

    /*
     * Verify start, stop, increment success count, and never increment error count.
     */
    verify(runtimeStat, times(1)).start();
    verify(runtimeStat, times(1)).stop();
    verify(successStat, times(1)).increment();
    verify(errorStat, never()).increment();

    /*
     * Verify contents of output stream
     */
    assertEquals(1, output.size());
  }

  @Test
  public void testStatsLoggingOnError() {
    DummyOperation operation = mock(DummyOperation.class);
    DummyOperationFactory mutatorFactory = new DummyOperationFactory(operation);
    OperationProcessor processor = new OperationProcessor(mutatorFactory);

    InternalEvent ievent = new InternalEvent("a", null, 1);
    doThrow(new OperationException("Expceted")).when(operation).perform(ievent);

    /*
     * Mock the Stat object
     */
    Stat runtimeStat = mock(Stat.class);
    Stat successStat = mock(Stat.class);
    Stat errorStat = mock(Stat.class);

    processor.setRuntimeStat(runtimeStat);
    processor.setSuccessCountStat(successStat);
    processor.setErrorCountStat(errorStat);

    Stream<InternalEvent> stream = processor.perform(Stream.of(ievent));
    List<InternalEvent> output = stream.collect(Collectors.toList());

    /*
     * Verify start, stop are called, increment error count and never increment success count.
     */
    verify(runtimeStat, times(1)).start();
    verify(runtimeStat, times(1)).stop();
    verify(successStat, never()).increment();
    verify(errorStat, times(1)).increment();

    /*
     * Verify contents of output stream
     */
    assertEquals(0, output.size());
  }

  @Test
  public void testNullInternalEventFiltering()
      throws JsonSyntaxException, UnsupportedEncodingException, IOException, OperationException {
    /*
     * Setup mocks for test
     */
    DummyOperation op = spy(new DummyOperation());
    when(op.perform(any(InternalEvent.class))).thenReturn(null);
    DummyOperationFactory operationFactory = new DummyOperationFactory(op);
    OperationProcessor processor = new OperationProcessor(operationFactory);

    /*
     * Do call
     */
    Stream<InternalEvent> stream = processor.perform(Stream.of(new InternalEvent("foo", null, 1)));
    List<InternalEvent> output = stream.collect(Collectors.toList());

    /*
     * Verify nothing came out
     */
    assertEquals(0, output.size());
  }

  @Test
  public void testNullDeserializedEventFiltering()
      throws JsonSyntaxException, UnsupportedEncodingException, IOException, OperationException {
    /*
     * Setup mocks for test
     */
    DummyOperation op = spy(new DummyOperation());
    InternalEvent retEvent = new InternalEvent("foo", null, 1);
    retEvent.setEventObj(null);
    when(op.perform(any(InternalEvent.class))).thenReturn(retEvent);
    DummyOperationFactory operationFactory = new DummyOperationFactory(op);
    OperationProcessor processor = new OperationProcessor(operationFactory);

    /*
     * Do call
     */
    Stream<InternalEvent> stream = processor.perform(Stream.of(new InternalEvent("foo", null, 1)));
    List<InternalEvent> output = stream.collect(Collectors.toList());

    /*
     * Verify nothing came out
     */
    assertEquals(0, output.size());
  }

  @Test
  public void testNullPayloadFiltering()
      throws JsonSyntaxException, UnsupportedEncodingException, IOException, OperationException {
    /*
     * Setup mocks for test
     */
    DummyOperation op = spy(new DummyOperation());
    InternalEvent retEvent = new InternalEvent("foo", null, 1);
    retEvent.setEventObj(new DummyDeserializerHelper.DummyDeserializedEvent(null));

    when(op.perform(any(InternalEvent.class))).thenReturn(retEvent);
    DummyOperationFactory operationFactory = new DummyOperationFactory(op);
    OperationProcessor processor = new OperationProcessor(operationFactory);

    /*
     * Do call
     */
    Stream<InternalEvent> stream = processor.perform(Stream.of(new InternalEvent("foo", null, 1)));
    List<InternalEvent> output = stream.collect(Collectors.toList());

    /*
     * Verify nothing came out
     */
    assertEquals(0, output.size());
  }
}
