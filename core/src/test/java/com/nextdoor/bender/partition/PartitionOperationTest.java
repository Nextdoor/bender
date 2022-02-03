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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Test;

import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.deserializer.FieldNotFoundException;
import com.nextdoor.bender.operation.OperationException;
import com.nextdoor.bender.operation.OperationProcessor;
import com.nextdoor.bender.testutils.DummyDeserializerHelper.DummyStringEvent;
import com.nextdoor.bender.testutils.DummyOperationHelper.DummyOperationFactory;

public class PartitionOperationTest {

  @Test
  public void testGetEvaluatedPartitionsString() throws FieldNotFoundException {
    List<PartitionSpec> partitionSpecs = new ArrayList<>(1);
    List<String> sources = Arrays.asList("foo");
    PartitionSpec spec = new PartitionSpec("foo", sources, PartitionSpec.Interpreter.STRING);
    partitionSpecs.add(spec);

    PartitionOperation op = new PartitionOperation(partitionSpecs);
    InternalEvent ievent = new InternalEvent("foo", null, 1);
    DummyStringEvent devent = spy(new DummyStringEvent(""));
    ievent.setEventObj(devent);
    doReturn("baz").when(devent).getFieldAsString("foo");

    op.perform(ievent);

    LinkedHashMap<String, String> actual = ievent.getPartitions();
    LinkedHashMap<String, String> expected = new LinkedHashMap<>(1);
    expected.put("foo", "baz");

    assertEquals(expected, actual);
  }

  @Test
  public void testGetEvaluatedPartitionsStatic() {
    List<PartitionSpec> partitionSpecs = new ArrayList<>(1);
    PartitionSpec spec = new PartitionSpec("foo", Collections.emptyList(), PartitionSpec.Interpreter.STATIC, "123", 0);
    partitionSpecs.add(spec);

    PartitionOperation op = new PartitionOperation(partitionSpecs);
    InternalEvent ievent = new InternalEvent("foo", null, 1);
    DummyStringEvent devent = new DummyStringEvent("");
    ievent.setEventObj(devent);

    op.perform(ievent);

    LinkedHashMap<String, String> actual = ievent.getPartitions();
    LinkedHashMap<String, String> expected = new LinkedHashMap<>(1);
    expected.put("foo", "123");

    assertEquals(expected, actual);
  }

  @Test
  public void testGetEvaluatedPartitionsStringMultipleFields() throws FieldNotFoundException {
    List<PartitionSpec> partitionSpecs = new ArrayList<>(1);
    List<String> sources = Arrays.asList("one", "two");
    PartitionSpec spec = new PartitionSpec("foo", sources, PartitionSpec.Interpreter.STRING);
    partitionSpecs.add(spec);

    PartitionOperation op = new PartitionOperation(partitionSpecs);
    InternalEvent ievent = new InternalEvent("foo", null, 1);
    DummyStringEvent devent = spy(new DummyStringEvent(""));
    ievent.setEventObj(devent);
    doThrow(FieldNotFoundException.class).doReturn("5").when(devent).getFieldAsString(any());

    op.perform(ievent);

    LinkedHashMap<String, String> actual = ievent.getPartitions();
    LinkedHashMap<String, String> expected = new LinkedHashMap<>(1);
    expected.put("foo", "5");

    assertEquals(expected, actual);
  }

  @Test(expected = OperationException.class)
  public void testGetEvaluatedPartitionsStringMultipleFieldsNull() throws FieldNotFoundException {
    List<PartitionSpec> partitionSpecs = new ArrayList<>(1);
    List<String> sources = Arrays.asList("one", "two");
    PartitionSpec spec = new PartitionSpec("foo", sources, PartitionSpec.Interpreter.STRING);
    partitionSpecs.add(spec);

    PartitionOperation op = new PartitionOperation(partitionSpecs);
    InternalEvent ievent = new InternalEvent("foo", null, 1);
    DummyStringEvent devent = spy(new DummyStringEvent("baz"));
    ievent.setEventObj(devent);
    doThrow(FieldNotFoundException.class).doThrow(FieldNotFoundException.class).when(devent).getFieldAsString(any());

    try {
      op.perform(ievent);
    } catch (OperationException e) {
      assertEquals("unable to find value for partition 'foo'", e.getMessage());
      throw e;
    }
  }

  @Test(expected = OperationException.class)
  public void testGetEvaluatedPartitionsFieldNotFoundException() throws FieldNotFoundException {
    List<PartitionSpec> partitionSpecs = new ArrayList<>(1);
    List<String> sources = Arrays.asList("one");
    PartitionSpec spec = new PartitionSpec("foo", sources, PartitionSpec.Interpreter.STRING);
    partitionSpecs.add(spec);

    PartitionOperation op = new PartitionOperation(partitionSpecs);
    InternalEvent ievent = new InternalEvent("foo", null, 1);
    DummyStringEvent devent = spy(new DummyStringEvent("baz"));
    ievent.setEventObj(devent);

    doThrow(FieldNotFoundException.class).when(devent).getFieldAsString(any());

    try {
      op.perform(ievent);
    } catch (OperationException e) {
      assertEquals("unable to find value for partition 'foo'", e.getMessage());
      throw e;
    }
  }

  @Test
  public void testOperationThroughProcessor() throws FieldNotFoundException {
    List<PartitionSpec> partitionSpecs = new ArrayList<>(1);
    List<String> sources = Arrays.asList("foo");
    PartitionSpec spec = new PartitionSpec("foo", sources, PartitionSpec.Interpreter.STRING);
    partitionSpecs.add(spec);

    PartitionOperation op = new PartitionOperation(partitionSpecs);
    InternalEvent ievent = new InternalEvent("foo", null, 1);
    DummyStringEvent devent = spy(new DummyStringEvent(""));
    ievent.setEventObj(devent);
    doReturn("baz").when(devent).getFieldAsString("foo");

    DummyOperationFactory opFact = new DummyOperationFactory(op);
    OperationProcessor opProc = new OperationProcessor(opFact);

    opProc.perform(Stream.of(ievent)).count();

    LinkedHashMap<String, String> actual = ievent.getPartitions();
    LinkedHashMap<String, String> expected = new LinkedHashMap<>(1);
    expected.put("foo", "baz");

    assertEquals(expected, actual);
  }
}
