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

package com.nextdoor.bender.ipc.splunk;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;

import org.junit.Test;

import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.testutils.TestUtils;

public class SplunkTansportSerializerTest {

  private static class DummyEvent extends InternalEvent {
    private LinkedHashMap<String, String> partitions;

    public DummyEvent(String record, long timestamp, LinkedHashMap<String, String> partitions) {
      super(record, null, timestamp);
      this.partitions = partitions;
    }

    public DummyEvent(String record, long timestamp) {
      super(record, null, timestamp);
      this.partitions = new LinkedHashMap<String, String>(0);
    }

    @Override
    public LinkedHashMap<String, String> getPartitions() {
      return this.partitions;
    }
  }

  @Test
  public void testSerialize() throws UnsupportedEncodingException, IOException {
    SplunkTransportSerializer serializer = new SplunkTransportSerializer("log");
    InternalEvent record = new DummyEvent("foo", 0);

    record.setEventTime(1505927823123l);
    record.setSerialized("foo");

    String actual = new String(serializer.serialize(record));
    String expected = TestUtils.getResourceString(this.getClass(), "basic_output.txt");

    assertEquals(expected, actual);
  }

  @Test
  public void testSerializeNoIndex() throws UnsupportedEncodingException, IOException {
    SplunkTransportSerializer serializer = new SplunkTransportSerializer(null);
    InternalEvent record = new DummyEvent("foo", 0);

    record.setEventTime(1505927823123l);
    record.setSerialized("foo");

    String actual = new String(serializer.serialize(record));
    String expected = TestUtils.getResourceString(this.getClass(), "basic_output_no_index.txt");

    assertEquals(expected, actual);
  }
}
