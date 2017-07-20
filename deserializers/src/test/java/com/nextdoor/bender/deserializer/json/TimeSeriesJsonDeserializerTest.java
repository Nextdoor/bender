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

package com.nextdoor.bender.deserializer.json;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

import com.nextdoor.bender.deserializer.DeserializationException;
import com.nextdoor.bender.deserializer.DeserializedEvent;
import com.nextdoor.bender.deserializer.json.TimeSeriesJsonEvent.TimeFieldType;
import com.nextdoor.bender.testutils.TestUtils;

public class TimeSeriesJsonDeserializerTest {

  private DeserializedEvent getEvent(String filename, TimeFieldType timeFieldType)
      throws UnsupportedEncodingException, IOException {
    String input = TestUtils.getResourceString(this.getClass(), filename);

    GenericJsonDeserializerConfig.FieldConfig fconfig =
        new GenericJsonDeserializerConfig.FieldConfig();
    fconfig.setField("MESSAGE");
    fconfig.setPrefixField("MESSAGE_PREFIX");
    TimeSeriesJsonDeserializer deser = new TimeSeriesJsonDeserializer(Collections.emptyList(),
        Arrays.asList(fconfig), null, "$.EPOCH", timeFieldType);
    deser.init();
    return deser.deserialize(input);
  }

  @Test
  public void testVerifyTimeFieldMs() throws UnsupportedEncodingException, IOException {
    TimeSeriesJsonEvent devent =
        (TimeSeriesJsonEvent) getEvent("timeseries.json", TimeFieldType.MILLISECONDS);

    assertEquals(1478714988998l, devent.timestamp);
  }

  @Test
  public void testVerifyTimeField() throws UnsupportedEncodingException, IOException {
    TimeSeriesJsonEvent devent =
        (TimeSeriesJsonEvent) getEvent("timeseries_sec.json", TimeFieldType.SECONDS);

    assertEquals(1478714988998l, devent.timestamp);
  }

  @Test(expected = DeserializationException.class)
  public void testMissingTimeField() throws UnsupportedEncodingException, IOException {
    getEvent("basic.json", TimeFieldType.MILLISECONDS);
  }

  @Test(expected = DeserializationException.class)
  public void testInvalidTimeField() throws UnsupportedEncodingException, IOException {
    getEvent("timeseries_invalid.json", TimeFieldType.MILLISECONDS);
  }

  @Test
  public void testSanity() {
    assertEquals(1478901115000l,
        TimeSeriesJsonEvent.getTimestamp("1478901115", TimeFieldType.MILLISECONDS));
    assertEquals(1478901115000l,
        TimeSeriesJsonEvent.getTimestamp("1478901115", TimeFieldType.SECONDS));
    assertEquals(1478901115000l,
        TimeSeriesJsonEvent.getTimestamp("1478901115000", TimeFieldType.MILLISECONDS));
    assertEquals(1478901115000l,
        TimeSeriesJsonEvent.getTimestamp("1478901115000", TimeFieldType.SECONDS));
    assertEquals(1478901115000l,
        TimeSeriesJsonEvent.getTimestamp("1478901115000000", TimeFieldType.MILLISECONDS));
    assertEquals(1478901115000l,
        TimeSeriesJsonEvent.getTimestamp("1478901115000000", TimeFieldType.SECONDS));
  }
}
