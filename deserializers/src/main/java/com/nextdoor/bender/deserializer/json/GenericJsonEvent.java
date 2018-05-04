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

package com.nextdoor.bender.deserializer.json;

import java.util.NoSuchElementException;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.jayway.jsonpath.DocumentContext;
import com.nextdoor.bender.deserializer.DeserializedEvent;

/**
 * Basic wrapper around an arbitrary JSON object.
 */
public class GenericJsonEvent implements DeserializedEvent {
  private JsonElement payload;

  public GenericJsonEvent(JsonObject payload) {
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
  public Object getField(String field) {
    if (this.payload == null) {
      throw new NoSuchElementException(field + " is not in payload because payload is null");
    }

    JsonObject json = this.payload.getAsJsonObject();
    Object obj = JsonPathProvider.read(json, field);

    if (obj == null) {
      // TODO: this should really throw NoSuchElementException
      return null;
    }

    if (obj instanceof JsonPrimitive) {
      if (((JsonPrimitive) obj).isString()) {
        return ((JsonPrimitive) obj).getAsString();
      }
    }

    return obj;
  }

  @Override
  public void setPayload(Object object) {
    this.payload = (JsonElement) object;
  }

  @Override
  public void setField(String fieldName, Object value) throws IllegalArgumentException {
    if (this.payload == null) {
      throw new IllegalArgumentException("payload is null");
    }

    if (!fieldName.startsWith("$.")) {
      fieldName = "$." + fieldName;
    }

    int lastDot = fieldName.lastIndexOf('.');
    DocumentContext json = JsonPathProvider.parse(this.payload.getAsJsonObject());
    String path = fieldName.substring(0, lastDot);
    String field = fieldName.substring(lastDot + 1);
    json.put(path, field, value);
  }

  @Override
  public String getFieldAsString(String fieldName) throws NoSuchElementException {
    Object obj = getField(fieldName);

    if (obj == null) {
      return null;
    }

    if (obj instanceof String) {
      return (String) obj;
    } else if (obj instanceof JsonElement) {
      return ((JsonElement) obj).getAsString();
    }

    return obj.toString();
  }

  @Override
  public Object removeField(String fieldName) throws IllegalArgumentException {
    if (this.payload == null) {
      // TODO: this should really throw NoSuchElementException
      // throw new NoSuchElementException(fieldName + " is not in payload because payload is null");
      return null;
    }

    JsonObject json = this.payload.getAsJsonObject();
    JsonElement elm = JsonPathProvider.read(json, fieldName);

    if (elm == null) {
      return null;
    }
    JsonPathProvider.delete(json, fieldName);
    return elm;
  }
}
