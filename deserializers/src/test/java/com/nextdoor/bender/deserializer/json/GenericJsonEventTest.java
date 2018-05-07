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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;

import org.junit.Test;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.nextdoor.bender.deserializer.FieldNotFoundException;

public class GenericJsonEventTest {

  private GenericJsonEvent getEmptyEvent() {
    JsonParser parser = new JsonParser();
    JsonElement elm = parser.parse("{\"a\": \"b\"}");
    JsonObject obj = elm.getAsJsonObject();

    return new GenericJsonEvent(obj);
  }

  @Test
  public void testGetAsStringFromString() throws FieldNotFoundException {
    GenericJsonEvent event = getEmptyEvent();
    event.setField("$.foo", "bar");
    event.setField("$.meaningoflife", 42);
    assertEquals("bar", event.getFieldAsString("$.foo"));
    assertEquals("42", event.getFieldAsString("$.meaningoflife"));
  }

  @Test
  public void testGetAsStringFromArray() throws FieldNotFoundException {
    GenericJsonEvent event = getEmptyEvent();
    JsonParser parser = new JsonParser();
    JsonElement elm = parser.parse("[\"foo\", \"bar\"]");
    event.setField("$.foo", elm.getAsJsonArray());
    assertEquals("[\"foo\",\"bar\"]", event.getFieldAsString("$.foo"));
  }

  @Test
  public void testGetAsStringFromObj() throws FieldNotFoundException {
    GenericJsonEvent event = getEmptyEvent();
    JsonParser parser = new JsonParser();
    JsonElement elm = parser.parse("{\"key\": [\"foo\", \"bar\"]}");
    event.setField("$.foo", elm.getAsJsonObject());
    assertEquals("{\"key\":[\"foo\",\"bar\"]}", event.getFieldAsString("$.foo"));
  }

  @Test
  public void testSetField() throws FieldNotFoundException {
    GenericJsonEvent event = getEmptyEvent();
    event.setField("$.foo", "bar");
    JsonObject payload = (JsonObject) event.getPayload();

    assertEquals("bar", payload.get("foo").getAsString());
    assertEquals("b", payload.get("a").getAsString());
  }

  @Test
  public void testOverrideField() throws FieldNotFoundException {
    GenericJsonEvent event = getEmptyEvent();
    event.setField("$.a", "bar");

    JsonObject payload = (JsonObject) event.getPayload();
    assertEquals("bar", payload.get("a").getAsString());
  }

  @Test
  public void testInvalidRootPath() throws FieldNotFoundException {
    GenericJsonEvent event = getEmptyEvent();
    event.setField("foo", "bar");

    assertEquals("bar", event.getField("$.foo"));
  }

  @Test
  public void testNestedMissingPath() throws FieldNotFoundException {
    GenericJsonEvent event = getEmptyEvent();
    event.setField("$.foo.bar", "baz");

    JsonObject payload = (JsonObject) event.getPayload();
    assertEquals(null, payload.get("foo"));
  }

  @Test
  public void testNestedPath() throws FieldNotFoundException {
    GenericJsonEvent event = getEmptyEvent();
    event.setField("$.foo", new HashMap<String, Object>());
    event.setField("$.foo.bar", "baz");

    JsonObject payload = (JsonObject) event.getPayload();
    assertTrue(payload.get("foo") instanceof JsonObject);
    assertTrue(payload.get("foo").getAsJsonObject().get("bar") instanceof JsonPrimitive);
    assertEquals("baz", payload.get("foo").getAsJsonObject().get("bar").getAsString());
  }

  @Test
  public void testRemoveField() throws FieldNotFoundException {
    GenericJsonEvent event = getEmptyEvent();
    event.setField("$.foo", "bar");
    event.setField("$.baz", "qux");
    JsonObject payload = (JsonObject) event.getPayload();

    event.removeField("$.foo");

    assertEquals(2, payload.size());
    assertEquals(null, payload.get("foo"));
    assertEquals("qux", payload.get("baz").getAsString());
    assertEquals("b", payload.get("a").getAsString());
  }

  @Test
  public void testRemoveMissingField() throws FieldNotFoundException {
    GenericJsonEvent event = getEmptyEvent();
    JsonObject payload = (JsonObject) event.getPayload();

    Object obj = event.removeField("$.foo");

    assertEquals(1, payload.size());
    assertEquals(null, obj);
    assertEquals("b", payload.get("a").getAsString());
  }

  public void testGetInvalidPath() throws FieldNotFoundException {
    GenericJsonEvent event = getEmptyEvent();
    String invalidPath = "[$.";
    event.setField("$.foo", new HashMap<String, Object>());
    event.setField("$.foo.bar", "baz");
    String expectedErrorMessage = "Field cannot be found because " + invalidPath
        + " is an invalid path";

    try {
      event.getField(invalidPath);
      fail();
    } catch(FieldNotFoundException e) {
      assertEquals(e.getMessage(), expectedErrorMessage);
    }
  }

  @Test
  public void testGetMissingField() throws FieldNotFoundException {
    GenericJsonEvent event = getEmptyEvent();
    String missingField = "$.cookie";
    event.setField("$.foo", new HashMap<String, Object>());
    event.setField("$.foo.bar", "baz");
    String expectedErrorMessage = missingField + " is not in payload.";

    try {
      event.getField(missingField);
      fail();
    } catch(FieldNotFoundException e) {
      assertEquals(expectedErrorMessage, e.getMessage());
    }
  }
}
