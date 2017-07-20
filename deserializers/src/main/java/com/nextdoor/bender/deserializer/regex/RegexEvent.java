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

import java.util.Map;
import java.util.NoSuchElementException;

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
  public String getField(String fieldName) throws NoSuchElementException {
    if (this.payload == null) {
      throw new NoSuchElementException(fieldName + " is not in payload because payload is null");
    }

    Object o = this.payload.getOrDefault(fieldName, null);

    return o != null ? o.toString() : null;
  }
}
