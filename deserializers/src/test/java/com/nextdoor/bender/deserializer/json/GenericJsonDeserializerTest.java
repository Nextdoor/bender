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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.NoSuchElementException;

import org.junit.Test;

import com.google.gson.JsonObject;
import com.nextdoor.bender.deserializer.DeserializationException;
import com.nextdoor.bender.deserializer.DeserializedEvent;
import com.nextdoor.bender.testutils.TestUtils;

public class GenericJsonDeserializerTest {

  private DeserializedEvent getEvent(String filename)
      throws UnsupportedEncodingException, IOException {
    return getEvent(filename, null);
  }

  private DeserializedEvent getEvent(String filename, String path)
      throws UnsupportedEncodingException, IOException {
    String input = TestUtils.getResourceString(this.getClass(), filename);
    GenericJsonDeserializerConfig.FieldConfig fconfig =
        new GenericJsonDeserializerConfig.FieldConfig();
    fconfig.setField("MESSAGE");
    GenericJsonDeserializer deser =
        new GenericJsonDeserializer(Collections.emptyList(), Arrays.asList(fconfig), path);
    deser.init();
    return deser.deserialize(input);
  }

  @Test
  public void testBasicJson() throws UnsupportedEncodingException, IOException {
    DeserializedEvent devent = getEvent("basic.json");

    /*
     * Verify payload type
     */
    assertNotNull(devent.getPayload());
    assertEquals(devent.getPayload().getClass(), JsonObject.class);

    /*
     * Verify payload data
     */
    JsonObject obj = (JsonObject) devent.getPayload();

    assertTrue(obj.has("a_string"));
    assertTrue(obj.get("a_string").isJsonPrimitive());
    assertTrue(obj.get("a_string").getAsJsonPrimitive().isString());
    assertEquals("foo", obj.get("a_string").getAsString());

    assertTrue(obj.has("a_bool"));
    assertTrue(obj.get("a_bool").isJsonPrimitive());
    assertTrue(obj.get("a_bool").getAsJsonPrimitive().isBoolean());
    assertEquals(true, obj.get("a_bool").getAsBoolean());

    assertTrue(obj.has("a_number"));
    assertTrue(obj.get("a_number").isJsonPrimitive());
    assertTrue(obj.get("a_number").getAsJsonPrimitive().isNumber());
    assertEquals(1, obj.get("a_number").getAsInt());

    assertTrue(obj.has("an_obj"));
    assertTrue(obj.get("an_obj").isJsonObject());
    assertTrue(obj.get("an_obj").getAsJsonObject().has("foo"));
  }

  @Test
  public void testValidNestedJson() throws UnsupportedEncodingException, IOException {
    DeserializedEvent devent = getEvent("nested.json");
    JsonObject obj = (JsonObject) devent.getPayload();

    /*
     * Verify MESSAGE was converted to a json object
     */
    assertTrue(obj.has("MESSAGE"));
    assertTrue(obj.get("MESSAGE").isJsonObject());

    /*
     * Check members of MESSAGE object
     */
    JsonObject nested = obj.get("MESSAGE").getAsJsonObject();
    assertTrue(nested.has("a_string"));
    assertTrue(nested.has("an_obj"));
    assertTrue(nested.get("an_obj").isJsonObject());

    /*
     * Verify sub nested json was also converted
     */
    nested = nested.get("an_obj").getAsJsonObject();
    assertTrue(nested.has("a_num"));
    assertEquals(123, nested.get("a_num").getAsInt());
  }

  @Test
  public void testInvalidNestedJson() throws UnsupportedEncodingException, IOException {
    DeserializedEvent devent = getEvent("nested_invalid.json");
    JsonObject obj = (JsonObject) devent.getPayload();

    /*
     * Verify that when there is invalid json MESSAGE is kept as a string
     */
    assertTrue(obj.has("MESSAGE"));
    assertTrue(obj.get("MESSAGE").isJsonPrimitive());
    assertTrue(obj.get("MESSAGE").getAsJsonPrimitive().isString());
    assertEquals("{\"a_string: \"foo\"}}", obj.get("MESSAGE").getAsString());

    /*
     * Double check another member wasn't dropped
     */
    assertTrue(obj.has("a_num"));
    assertTrue(obj.get("a_num").isJsonPrimitive());
    assertTrue(obj.get("a_num").getAsJsonPrimitive().isNumber());
    assertEquals(123, obj.get("a_num").getAsInt());
  }

  @Test(expected = DeserializationException.class)
  public void testInvalidJson() throws UnsupportedEncodingException, IOException {
    getEvent("invalid.json");
  }

  @Test(expected = DeserializationException.class)
  public void testMalformedJson() throws UnsupportedEncodingException, IOException {
    getEvent("malformed.json");
  }

  @Test
  public void testNestedOffsetJson() throws UnsupportedEncodingException, IOException {
    DeserializedEvent devent = getEvent("nested_offset.json");
    JsonObject obj = (JsonObject) devent.getPayload();

    /*
     * Verify that nested string json is found when there is an offset
     */
    assertTrue(obj.has("MESSAGE"));
    assertTrue(obj.get("MESSAGE").isJsonObject());

    JsonObject nested = obj.get("MESSAGE").getAsJsonObject();
    assertTrue(nested.has("a_string"));
  }

  @Test
  public void testEmptyBrace() throws UnsupportedEncodingException, IOException {
    DeserializedEvent devent = getEvent("nested_brace.json");
    JsonObject obj = (JsonObject) devent.getPayload();

    /*
     * Verify nested parser does not break when there is just a single {
     */
    assertTrue(obj.has("MESSAGE"));
    assertTrue(obj.get("MESSAGE").isJsonPrimitive());
    assertTrue(obj.get("MESSAGE").getAsJsonPrimitive().isString());
    assertEquals("{", obj.get("MESSAGE").getAsString());
  }

  @Test
  public void testNestedPrefix() throws UnsupportedEncodingException, IOException {
    String input = TestUtils.getResourceString(this.getClass(), "nested_prefix.json");

    GenericJsonDeserializerConfig.FieldConfig fconfig =
        new GenericJsonDeserializerConfig.FieldConfig();
    fconfig.setField("MESSAGE");
    fconfig.setPrefixField("MESSAGE_PREFIX");
    GenericJsonDeserializer deser =
        new GenericJsonDeserializer(Collections.emptyList(), Arrays.asList(fconfig));
    deser.init();

    DeserializedEvent devent = deser.deserialize(input);
    JsonObject obj = (JsonObject) devent.getPayload();

    /*
     * Nested message is there along with the prefix of the string
     */
    assertTrue(obj.has("MESSAGE"));
    assertTrue(obj.get("MESSAGE").isJsonObject());

    assertTrue(obj.has("MESSAGE_PREFIX"));
    assertTrue(obj.get("MESSAGE_PREFIX").isJsonPrimitive());
    assertTrue(obj.get("MESSAGE_PREFIX").getAsJsonPrimitive().isString());
    assertEquals("this is a prefix ", obj.get("MESSAGE_PREFIX").getAsString());
  }

  @Test
  public void testGetField() throws UnsupportedEncodingException, IOException {
    String input = TestUtils.getResourceString(this.getClass(), "basic.json");

    GenericJsonDeserializer deser =
        new GenericJsonDeserializer(Collections.emptyList(), Collections.emptyList());
    deser.init();
    DeserializedEvent event = deser.deserialize(input);

    assertEquals("foo", event.getField("$.a_string"));
  }

  @Test
  public void testGetNestedField() throws UnsupportedEncodingException, IOException {
    String input = TestUtils.getResourceString(this.getClass(), "basic.json");

    GenericJsonDeserializer deser =
        new GenericJsonDeserializer(Collections.emptyList(), Collections.emptyList());
    deser.init();
    DeserializedEvent event = deser.deserialize(input);

    assertEquals("bar", event.getField("$.an_obj.foo"));
  }

  @Test
  public void testGetMissingField() throws UnsupportedEncodingException, IOException {
    String input = TestUtils.getResourceString(this.getClass(), "basic.json");

    GenericJsonDeserializer deser =
        new GenericJsonDeserializer(Collections.emptyList(), Collections.emptyList());
    deser.init();
    DeserializedEvent event = deser.deserialize(input);

    assertEquals(null, event.getField("$.not_a_member"));
  }

  @Test
  public void testGetMissingNestedField() throws UnsupportedEncodingException, IOException {
    String input = TestUtils.getResourceString(this.getClass(), "basic.json");

    GenericJsonDeserializer deser =
        new GenericJsonDeserializer(Collections.emptyList(), Collections.emptyList());
    deser.init();
    DeserializedEvent event = deser.deserialize(input);

    assertEquals(null, event.getField("$.an_obj.baz"));
  }

  @Test(expected = NoSuchElementException.class)
  public void testGetNestedObjField() throws UnsupportedEncodingException, IOException {
    String input = TestUtils.getResourceString(this.getClass(), "basic.json");

    GenericJsonDeserializer deser =
        new GenericJsonDeserializer(Collections.emptyList(), Collections.emptyList());
    deser.init();
    DeserializedEvent event = deser.deserialize(input);

    event.getField("$.an_obj");
  }

  @Test
  public void testRootNodeChange() throws UnsupportedEncodingException, IOException {
    DeserializedEvent devent = getEvent("nested.json", "$.MESSAGE");
    JsonObject obj = (JsonObject) devent.getPayload();

    /*
     * Check members of MESSAGE object
     */
    assertTrue(obj.has("a_string"));
    assertTrue(obj.has("an_obj"));
    assertTrue(obj.get("an_obj").isJsonObject());

    /*
     * Verify sub nested json was also converted
     */
    obj = obj.get("an_obj").getAsJsonObject();
    assertTrue(obj.has("a_num"));
    assertEquals(123, obj.get("a_num").getAsInt());
  }

  @Test(expected = DeserializationException.class)
  public void testRootNodeChangeException() throws UnsupportedEncodingException, IOException {
    DeserializedEvent devent = getEvent("nested.json", "$.FOO");
  }

  @Test
  public void testRootNodeChangeNested() throws UnsupportedEncodingException, IOException {
    DeserializedEvent devent = getEvent("nested.json", "$.MESSAGE.an_obj");
    JsonObject obj = (JsonObject) devent.getPayload();

    /*
     * Verify sub nested json was also converted
     */
    assertTrue(obj.has("a_num"));
    assertEquals(123, obj.get("a_num").getAsInt());
  }
}
