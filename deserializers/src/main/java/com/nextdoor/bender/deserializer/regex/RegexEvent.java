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

package com.nextdoor.bender.deserializer.regex;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.NotImplementedException;

import com.nextdoor.bender.deserializer.DeserializedEvent;

public class RegexEvent implements DeserializedEvent {

  private Map<String, Object> payload;

  public RegexEvent(Map<String, Object> payload) {
    this.payload = payload;
  }

  @Override
  public Object getPayload() {
    return this.payload;
  }

  @Override
  public void setPayload(Object object) {
    this.payload = (Map<String, Object>) object;
  }

  @Override
  public Object getField(String fieldName) throws NoSuchElementException {
    if (this.payload == null) {
      throw new NoSuchElementException(fieldName + " is not in payload because payload is null");
    }

    return this.payload.getOrDefault(fieldName, null);
  }

  @Override
  public void setField(String fieldName, Object value) throws NotImplementedException {
    if (this.payload == null) {
      this.payload = new HashMap<String, Object>();
    }

    this.payload.put(fieldName, value);
  }

  @Override
  public String getFieldAsString(String fieldName) throws NoSuchElementException {
    Object o = getField(fieldName);

    if (o == null) {
      return null;
    }

    if (o instanceof String) {
      return (String) o;
    } else {
      return o.toString();
    }
  }
}
