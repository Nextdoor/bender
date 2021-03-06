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

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.jayway.jsonpath.InvalidPathException;
import com.nextdoor.bender.deserializer.DeserializedEvent;
import com.nextdoor.bender.deserializer.FieldNotFoundException;

/**
 * Basic wrapper around an arbitrary JSON object.
 */
public class GenericJsonEvent implements DeserializedEvent {
  private JsonObject payload;

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
  public Object getField(String field) throws FieldNotFoundException {
    if (this.payload == null) {
      throw new FieldNotFoundException(field + " is not in payload because payload is null");
    }

    JsonObject json = this.payload.getAsJsonObject();
    Object obj;
    try {
      obj = JsonPathProvider.read(json, field);
    } catch(InvalidPathException e) {
      throw new FieldNotFoundException("Field cannot be found because " + field
          + " is an invalid path");
    }

    if (obj == null || obj instanceof JsonNull) {
      throw new FieldNotFoundException(field + " is not in payload.");
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
    this.payload = (JsonObject) object;
  }

  @Override
  public void setField(String fieldName, Object value) throws FieldNotFoundException {
    if (this.payload == null) {
      throw new FieldNotFoundException("payload is null");
    }

    if (!fieldName.startsWith("$.")) {
      fieldName = "$." + fieldName;
    }

    JsonPathProvider.setField(this.payload, value, fieldName);
  }

  @Override
  public String getFieldAsString(String fieldName) throws FieldNotFoundException {
    Object obj = getField(fieldName);

    if (obj == null) {
      return null;
    }

    if (obj instanceof String) {
      return (String) obj;
    } else if (obj instanceof JsonPrimitive) {
      return ((JsonPrimitive) obj).getAsString();
    } else if (obj instanceof JsonElement) {
      return obj.toString();
    }

    return obj.toString();
  }

  @Override
  public Object removeField(String fieldName) throws FieldNotFoundException {
    if (this.payload == null) {
      throw new FieldNotFoundException(fieldName + " is not in payload because payload is null");
    }

    Object o = getField(fieldName);
    JsonPathProvider.delete(this.payload, fieldName);
    return o;
  }

  @Override
  public void deleteField(String fieldName) {
    if (this.payload == null) {
      return;
    }

    JsonPathProvider.delete(this.payload, fieldName);
  }

  @Override
  public GenericJsonEvent copy() {
    if (this.payload != null) {
      return new GenericJsonEvent(this.payload.deepCopy());
    } else {
      return new GenericJsonEvent(null);
    }
  }
}
