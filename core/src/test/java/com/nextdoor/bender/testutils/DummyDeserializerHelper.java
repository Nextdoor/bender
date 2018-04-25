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
import java.util.NoSuchElementException;

import org.apache.commons.lang3.NotImplementedException;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.nextdoor.bender.config.AbstractConfig;
import com.nextdoor.bender.deserializer.DeserializedEvent;
import com.nextdoor.bender.deserializer.Deserializer;
import com.nextdoor.bender.deserializer.DeserializerConfig;
import com.nextdoor.bender.deserializer.DeserializerFactory;

public class DummyDeserializerHelper {
  public static class DummpyMapEvent implements DeserializedEvent {
    public Map<String, Object> payload = new HashMap<String, Object>();

    @Override
    public Object getPayload() {
      return payload;
    }

    @Override
    public Object getField(String fieldName) {
      return payload.get(fieldName);
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
    public String getFieldAsString(String fieldName) throws NoSuchElementException {
      Object o = getField(fieldName);

      if (o == null) {
        return null;
      }

      return payload.get(fieldName).toString();
    }

    @Override
    public Object removeField(String fieldName) throws IllegalArgumentException {
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
    public Object removeField(String fieldName) throws IllegalArgumentException {
      return null;
    }
  }

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
    public Object getField(String fieldName) throws NoSuchElementException {
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
    public String getFieldAsString(String fieldName) throws NoSuchElementException {
      return (String) payload;
    }

    @Override
    public Object removeField(String fieldName) throws IllegalArgumentException {
      return null;
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
      return new DummyDeserializedEvent(raw);
    }

    @Override
    public void init() {

    }
  }

}
