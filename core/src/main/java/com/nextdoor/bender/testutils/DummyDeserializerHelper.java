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

package com.nextdoor.bender.testutils;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.nextdoor.bender.config.AbstractConfig;
import com.nextdoor.bender.deserializer.DeserializedEvent;
import com.nextdoor.bender.deserializer.Deserializer;
import com.nextdoor.bender.deserializer.DeserializerConfig;
import com.nextdoor.bender.deserializer.DeserializerFactory;
import com.nextdoor.bender.partition.PartitionSpec;

public class DummyDeserializerHelper {
  public static class DummyDeserializedEvent implements DeserializedEvent {
    public String payload;

    public DummyDeserializedEvent(String payload) {
      this.payload = payload;
    }

    @Override
    public Object getPayload() {
      return payload;
    }

    @Override
    public String getField(String fieldName) throws NoSuchElementException {
      return payload;
    }

    @Override
    public void setPayload(Object object) {
      this.payload = (String) object;
    }
  }

  @JsonTypeName("DummyDeserializerHelper$DummyDeserializerConfig")
  public static class DummyDeserializerConfig extends DeserializerConfig {

    @Override
    public Class<DummyDeserializerFactory> getFactoryClass() {
      return DummyDeserializerFactory.class;
    }
  }

  public static class DummyDeserializerFactory implements DeserializerFactory {

    @Override
    public void setConf(AbstractConfig config) {}

    @Override
    public Deserializer newInstance() {
      return new DummyDeserializer();
    }

    @Override
    public Class<DummyDeserializer> getChildClass() {
      return DummyDeserializer.class;
    }
  }

  public static class DummyDeserializer extends Deserializer {
    public DummyDeserializer() {
      super(Collections.emptyList());
    }

    public DummyDeserializer(List<PartitionSpec> partitionSpecs) {
      super(partitionSpecs);
    }

    @Override
    public DeserializedEvent deserialize(String raw) {
      return new DummyDeserializedEvent(raw);
    }

    @Override
    public void init() {

    }
  }

}
