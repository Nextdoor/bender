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

package com.nextdoor.bender.partition;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import org.junit.Test;

import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.operation.OperationProcessor;
import com.nextdoor.bender.testutils.DummyDeserializerHelper.DummyDeserializedEvent;
import com.nextdoor.bender.testutils.DummyOperationHelper.DummyOperationFactory;

public class PartitionOperationTest {

  @Test
  public void testGetEvaluatedPartitionsString() {
    List<PartitionSpec> partitionSpecs = new ArrayList<PartitionSpec>(1);
    List<String> sources = Arrays.asList("foo");
    PartitionSpec spec = new PartitionSpec("foo", sources, PartitionSpec.Interpreter.STRING);
    partitionSpecs.add(spec);

    PartitionOperation op = new PartitionOperation(partitionSpecs);
    InternalEvent ievent = new InternalEvent("foo", null, 1);
    DummyDeserializedEvent devent = spy(new DummyDeserializedEvent(""));
    ievent.setEventObj(devent);
    doReturn("baz").when(devent).getFieldAsString("foo");

    op.perform(ievent);

    LinkedHashMap<String, String> actual = ievent.getPartitions();
    LinkedHashMap<String, String> expected = new LinkedHashMap<String, String>(1);
    expected.put("foo", "baz");

    assertEquals(expected, actual);
  }

  @Test
  public void testGetEvaluatedPartitionsStatic() {
    List<PartitionSpec> partitionSpecs = new ArrayList<PartitionSpec>(1);
    List<String> sources = Arrays.asList("foo");
    PartitionSpec spec = new PartitionSpec("foo", sources, PartitionSpec.Interpreter.STATIC, "123", 0);
    partitionSpecs.add(spec);

    PartitionOperation op = new PartitionOperation(partitionSpecs);
    InternalEvent ievent = new InternalEvent("foo", null, 1);
    DummyDeserializedEvent devent = new DummyDeserializedEvent("");
    ievent.setEventObj(devent);

    op.perform(ievent);

    LinkedHashMap<String, String> actual = ievent.getPartitions();
    LinkedHashMap<String, String> expected = new LinkedHashMap<String, String>(1);
    expected.put("foo", "123");

    assertEquals(expected, actual);
  }

  @Test
  public void testGetEvaluatedPartitionsStringMultipleFields() {
    List<PartitionSpec> partitionSpecs = new ArrayList<PartitionSpec>(1);
    List<String> sources = Arrays.asList("one", "two");
    PartitionSpec spec = new PartitionSpec("foo", sources, PartitionSpec.Interpreter.STRING);
    partitionSpecs.add(spec);

    PartitionOperation op = new PartitionOperation(partitionSpecs);
    InternalEvent ievent = new InternalEvent("foo", null, 1);
    DummyDeserializedEvent devent = spy(new DummyDeserializedEvent(""));
    ievent.setEventObj(devent);
    doReturn(null).doReturn("5").when(devent).getFieldAsString(any());

    op.perform(ievent);

    LinkedHashMap<String, String> actual = ievent.getPartitions();
    LinkedHashMap<String, String> expected = new LinkedHashMap<String, String>(1);
    expected.put("foo", "5");

    assertEquals(expected, actual);
  }

  @Test
  public void testGetEvaluatedPartitionsStringMultipleFieldsNull() {
    List<PartitionSpec> partitionSpecs = new ArrayList<PartitionSpec>(1);
    List<String> sources = Arrays.asList("one", "two");
    PartitionSpec spec = new PartitionSpec("foo", sources, PartitionSpec.Interpreter.STRING);
    partitionSpecs.add(spec);

    PartitionOperation op = new PartitionOperation(partitionSpecs);
    InternalEvent ievent = new InternalEvent("foo", null, 1);
    DummyDeserializedEvent devent = spy(new DummyDeserializedEvent("baz"));
    ievent.setEventObj(devent);
    doReturn(null).doReturn(null).when(devent).getFieldAsString(any());

    op.perform(ievent);

    LinkedHashMap<String, String> actual = ievent.getPartitions();
    LinkedHashMap<String, String> expected = new LinkedHashMap<String, String>(1);
    expected.put("foo", null);

    assertEquals(expected, actual);
  }

  @Test
  public void testGetEvaluatedPartitionsNoSuchElementException() {
    List<PartitionSpec> partitionSpecs = new ArrayList<PartitionSpec>(1);
    List<String> sources = Arrays.asList("one");
    PartitionSpec spec = new PartitionSpec("foo", sources, PartitionSpec.Interpreter.STRING);
    partitionSpecs.add(spec);

    PartitionOperation op = new PartitionOperation(partitionSpecs);
    InternalEvent ievent = new InternalEvent("foo", null, 1);
    DummyDeserializedEvent devent = spy(new DummyDeserializedEvent("baz"));
    ievent.setEventObj(devent);

    doThrow(new NoSuchElementException()).when(devent).getFieldAsString(any());

    op.perform(ievent);

    LinkedHashMap<String, String> actual = ievent.getPartitions();
    LinkedHashMap<String, String> expected = new LinkedHashMap<String, String>(1);
    expected.put("foo", null);

    assertEquals(expected, actual);
  }

  @Test
  public void testOperationThroughProcessor() {
    List<PartitionSpec> partitionSpecs = new ArrayList<PartitionSpec>(1);
    List<String> sources = Arrays.asList("foo");
    PartitionSpec spec = new PartitionSpec("foo", sources, PartitionSpec.Interpreter.STRING);
    partitionSpecs.add(spec);

    PartitionOperation op = new PartitionOperation(partitionSpecs);
    InternalEvent ievent = new InternalEvent("foo", null, 1);
    DummyDeserializedEvent devent = spy(new DummyDeserializedEvent(""));
    ievent.setEventObj(devent);
    doReturn("baz").when(devent).getFieldAsString("foo");

    DummyOperationFactory opFact = new DummyOperationFactory(op);
    OperationProcessor opProc = new OperationProcessor(opFact);

    opProc.perform(Stream.of(ievent)).count();

    LinkedHashMap<String, String> actual = ievent.getPartitions();
    LinkedHashMap<String, String> expected = new LinkedHashMap<String, String>(1);
    expected.put("foo", "baz");

    assertEquals(expected, actual);
  }
}
