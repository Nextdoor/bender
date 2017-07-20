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
 * Copyright 2016 Nextdoor.com, Inc
 *
 */

package com.nextdoor.bender;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.nextdoor.bender.deserializer.DeserializedEvent;
import com.nextdoor.bender.partition.PartitionSpec;

public class InternalEventTest {

  @Test
  public void testNullObjectPartition() {
    InternalEvent ievent = new InternalEvent("foo", null, 0);
    ievent.setEventObj(null);

    List<PartitionSpec> partSpecs = Arrays.asList(new PartitionSpec("part1", "part1"));
    Map<String, String> expected = new HashMap<String, String>(1);
    expected.put("part1", null);

    ievent.setPartitions(partSpecs);
    assertEquals(expected, ievent.getPartitions());
  }

  @Test
  public void testPartition() {
    InternalEvent ievent = new InternalEvent("foo", null, 0);

    DeserializedEvent mockEvent = mock(DeserializedEvent.class);
    when(mockEvent.getField("s1")).thenReturn("foo");
    when(mockEvent.getField("s2")).thenReturn("bar");

    LinkedHashMap<String, String> expected = new LinkedHashMap<String, String>(2);
    List<PartitionSpec> partSpecs =
        Arrays.asList(new PartitionSpec("part1", "s1"), new PartitionSpec("part2", "s2"));

    expected.put("part1", "foo");
    expected.put("part2", "bar");

    ievent.setEventObj(mockEvent);

    ievent.setPartitions(partSpecs);
    assertEquals(expected, ievent.getPartitions());
  }

  @Test
  public void testMissingPartition() {
    InternalEvent ievent = new InternalEvent("foo", null, 0);

    DeserializedEvent mockEvent = mock(DeserializedEvent.class);
    when(mockEvent.getField("s1")).thenReturn("foo");
    when(mockEvent.getField("s2")).thenReturn(null);

    List<PartitionSpec> partSpecs =
        Arrays.asList(new PartitionSpec("part1", "s1"), new PartitionSpec("part2", "s2"));

    Map<String, String> expected = new HashMap<String, String>(2);
    expected.put("part1", "foo");
    expected.put("part2", null);

    ievent.setEventObj(mockEvent);

    ievent.setPartitions(partSpecs);
    assertEquals(expected, ievent.getPartitions());
  }
}
