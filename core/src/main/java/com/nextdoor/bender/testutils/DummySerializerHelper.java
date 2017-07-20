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

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.nextdoor.bender.config.AbstractConfig;
import com.nextdoor.bender.serializer.Serializer;
import com.nextdoor.bender.serializer.SerializerConfig;
import com.nextdoor.bender.serializer.SerializerFactory;

public class DummySerializerHelper {
  public static class DummySerializerFactory implements SerializerFactory {
    @Override
    public Serializer newInstance() {
      return new DummySerializer();
    }

    @Override
    public void setConf(AbstractConfig config) {}

    @Override
    public Class<DummySerializer> getChildClass() {
      return DummySerializer.class;
    }
  }

  public static class DummySerializer implements Serializer {
    @Override
    public String serialize(Object obj) {
      return obj.toString();
    }
  }

  @JsonTypeName("DummySerializerHelper$DummySerializerConfig")
  public static class DummySerializerConfig extends SerializerConfig {

    @Override
    public Class<DummySerializerFactory> getFactoryClass() {
      return DummySerializerFactory.class;
    }
  }
}
