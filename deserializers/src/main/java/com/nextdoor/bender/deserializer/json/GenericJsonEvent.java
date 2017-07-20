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

import java.util.NoSuchElementException;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.jayway.jsonpath.JsonPath;
import com.nextdoor.bender.deserializer.DeserializedEvent;

/**
 * Basic wrapper around an arbitrary JSON object.
 */
public class GenericJsonEvent implements DeserializedEvent {
  private JsonElement payload;

  protected GenericJsonEvent(JsonObject payload) {
    this.payload = payload;
  }

  @Override
  public Object getPayload() {
    return payload;
  }

  /**
   * Should only ever be called by deserializer.
   * 
   * @param payload payload object as deserialized by Gson.
   */
  protected void setPayload(JsonObject payload) {
    this.payload = payload;
  }

  @Override
  public String getField(String field) {
    if (this.payload == null) {
      throw new NoSuchElementException(field + " is not in payload because payload is null");
    }

    JsonObject json = this.payload.getAsJsonObject();
    Object obj = JsonPath.read(json, field);

    if (obj == null) {
      return null;
    }

    if (!(obj instanceof JsonPrimitive)) {
      throw new NoSuchElementException(field + " is not a primitive type");
    }

    return ((JsonPrimitive) obj).getAsString();
  }

  @Override
  public void setPayload(Object object) {
    this.payload = (JsonElement) object;
  }
}
