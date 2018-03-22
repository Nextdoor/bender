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

package com.nextdoor.bender.ipc.datadog;

import static org.junit.Assert.assertEquals;

import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.config.value.StringValueConfig;
import java.nio.charset.StandardCharsets;
import org.junit.Test;

public class DatadogTransportSerializerTest {

  @Test
  public void shouldSerialize() {
    StringValueConfig apiKey = new StringValueConfig("foo");
    DatadogTransportSerializer serializer = new DatadogTransportSerializer(apiKey);
    InternalEvent record = new InternalEvent("", null, 0);
    record.setEventTime(1521645289128L);
    record.setSerialized("bar");
    String actual = new String(serializer.serialize(record), StandardCharsets.UTF_8);
    assertEquals("foo bar\n", actual);
  }

}
