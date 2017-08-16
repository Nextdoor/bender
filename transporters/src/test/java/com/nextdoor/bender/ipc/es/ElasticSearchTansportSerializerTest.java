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

package com.nextdoor.bender.ipc.es;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;

import org.junit.Test;

import com.amazonaws.services.lambda.runtime.events.KinesisEvent;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.testutils.TestUtils;

public class ElasticSearchTansportSerializerTest {

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
    ElasticSearchTransportSerializer serializer =
        new ElasticSearchTransportSerializer(false, "event", "log");
    InternalEvent record = new DummyEvent("foo", 0);
    record.setSerialized("foo");

    String actual = new String(serializer.serialize(record));
    String expected = TestUtils.getResourceString(this.getClass(), "basic_output.txt");

    /*
     * Verify build output does not contain hash
     */
    assertEquals(expected, actual);
  }

  @Test
  public void testSerializeWithHash() throws UnsupportedEncodingException, IOException {
    ElasticSearchTransportSerializer serializer =
        new ElasticSearchTransportSerializer(true, "event", "log");
    InternalEvent record = new DummyEvent("foo", 0);
    record.setSerialized("foo");

    String actual = new String(serializer.serialize(record));
    String expected = TestUtils.getResourceString(this.getClass(), "basic_hash_output.txt");

    /*
     * Verify build output does contain hash
     */
    assertEquals(expected, actual);
  }

  @Test
  public void testSerializeDateIndexName() throws UnsupportedEncodingException, IOException {
    ElasticSearchTransportSerializer serializer =
        new ElasticSearchTransportSerializer(false, "event", "log-", "yyyy-MM-dd");

    KinesisEvent kevent = TestUtils.createEvent(this.getClass(), "basic_event.json");
    String payload = new String(kevent.getRecords().get(0).getKinesis().getData().array());
    InternalEvent record = new DummyEvent(payload, 1478737790000l);

    String actual = new String(serializer.serialize(record));
    String expected = TestUtils.getResourceString(this.getClass(), "datetime_output.txt");
    assertEquals(expected, actual);
  }
}
