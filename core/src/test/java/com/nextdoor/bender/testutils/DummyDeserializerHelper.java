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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.NotImplementedException;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.nextdoor.bender.config.AbstractConfig;
import com.nextdoor.bender.deserializer.DeserializedEvent;
import com.nextdoor.bender.deserializer.Deserializer;
import com.nextdoor.bender.deserializer.DeserializerConfig;
import com.nextdoor.bender.deserializer.DeserializerFactory;
import com.nextdoor.bender.deserializer.FieldNotFoundException;

public class DummyDeserializerHelper {
  public static class DummpyMapEvent implements DeserializedEvent {
    public Map<String, Object> payload = new HashMap<String, Object>();

    @Override
    public Object getPayload() {
      return payload;
    }

    @Override
    public Object getField(String fieldName) throws FieldNotFoundException {
      Object o = payload.get(fieldName);

      if (o == null) {
        throw new FieldNotFoundException();
      }

      return o;
    }

    @Override
    public void setPayload(Object object) {
      this.payload = (Map<String, Object>) object;
    }

    @Override
    public void setField(String fieldName, Object value) {
      payload.put(fieldName, value);
    }

    @Override
    public String getFieldAsString(String fieldName) throws FieldNotFoundException {
      Object o = getField(fieldName);

      return payload.get(fieldName).toString();
    }

    @Override
    public Object removeField(String fieldName) throws FieldNotFoundException {
      return payload.remove(fieldName);
    }
  }

  public static class DummpyEvent implements DeserializedEvent {
    public Object payload;

    @Override
    public Object getPayload() {
      return payload;
    }

    @Override
    public String getField(String fieldName) {
      return null;
    }

    @Override
    public void setPayload(Object object) {
      this.payload = object;
    }

    @Override
    public void setField(String fieldName, Object value) {

    }

    @Override
    public String getFieldAsString(String fieldName) {
      return null;
    }

    @Override
    public Object removeField(String fieldName) throws FieldNotFoundException {
      throw new FieldNotFoundException();
    }
  }

  public static class DummyStringEvent implements DeserializedEvent {
    public String payload;

    public DummyStringEvent(String payload) {
      this.payload = payload;
    }

    @Override
    public Object getPayload() {
      return payload;
    }

    @Override
    public Object getField(String fieldName) throws FieldNotFoundException {
      return payload;
    }

    @Override
    public void setPayload(Object object) {
      this.payload = (String) object;
    }

    @Override
    public void setField(String fieldName, Object value) throws NotImplementedException {
      throw new NotImplementedException("setField not supported by DummyDeserializedEvent");
    }

    @Override
    public String getFieldAsString(String fieldName) throws FieldNotFoundException {
      return (String) payload;
    }

    @Override
    public Object removeField(String fieldName) throws FieldNotFoundException {
      throw new FieldNotFoundException("field not found");
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
    public DummyDeserializer() {}

    @Override
    public DeserializedEvent deserialize(String raw) {
      return new DummyStringEvent(raw);
    }

    @Override
    public void init() {

    }
  }

}
