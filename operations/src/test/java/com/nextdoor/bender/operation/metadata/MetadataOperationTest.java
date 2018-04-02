/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright $year Nextdoor.com, Inc
 */

package com.nextdoor.bender.operation.metadata;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.AddressNotFoundException;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.deserializer.DeserializedEvent;
import com.nextdoor.bender.handler.HandlerMetadata;
import com.nextdoor.bender.operation.OperationException;
import com.nextdoor.bender.operations.geo.GeoIpOperation;
import com.nextdoor.bender.operations.geo.GeoIpOperationConfig.GeoProperty;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.junit.Test;

public class MetadataOperationTest {
  MetadataOperation op;

  public static class DummpyEvent implements DeserializedEvent {
    public Map<String, Object> payload = new HashMap<String, Object>();

    @Override
    public Object getPayload() {
      return payload;
    }

    @Override
    public String getField(String fieldName) {
      Object o = payload.get(fieldName);
      if (o == null) {
        return null;
      }

      return o.toString();
    }

    @Override
    public void setPayload(Object object) {
      this.payload = (Map<String, Object>) object;
    }

    @Override
    public void setField(String fieldName, Object value) {
      payload.put(fieldName, value);
    }
  }

  @Test
  public void testBaseHandlerMetadata() throws Throwable {
    MetadataOperation op = new MetadataOperation("metadata");
    HandlerMetadata metadata = new HandlerMetadata();

    metadata.setField("test_field", "test_value");
    metadata.setField("test_num", 1234);
    metadata.setImmutable();

    DummpyEvent devent = new DummpyEvent();
    devent.setField("ip_address", "10.0.0.1");

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);
    ievent.setMetadata(metadata);

    op.perform(ievent);

    DeserializedEvent deserializedEvent = ievent.getEventObj();
    assertEquals(deserializedEvent.getField("metadata"), "{test_field=test_value, test_num=1234}");
  }
}
