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
 * Copyright 2018 Nextdoor.com, Inc
 *
 */
package com.nextdoor.bender.operation.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Test;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.deserializer.json.GenericJsonEvent;
import com.nextdoor.bender.operation.OperationException;
import com.nextdoor.bender.operations.json.OperationTest;

public class FilterOperationTest extends OperationTest {

  @Test
  public void testFilterMatches() throws IOException {
    JsonParser parser = new JsonParser();
    JsonElement input = parser.parse(getResourceString("filter_input.json"));
    GenericJsonEvent devent = new GenericJsonEvent(input.getAsJsonObject());

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    String regexMatch = "(val)";
    String regexFail = "(fail)";
    String path = "$.simple_object.key";
    Boolean match = true;

    assertNotNull(devent.getFieldAsString(path));

    FilterOperation opMatch = new FilterOperation(regexMatch, path, match);
    FilterOperation opFail = new FilterOperation(regexFail, path, match);

    assertTrue(opMatch.filterEvent(ievent.getEventObj()));
    assertNull(opMatch.perform(ievent));
    assertFalse(opFail.filterEvent(ievent.getEventObj()));
    assertNotNull(opFail.perform(ievent));
  }

  @Test
  public void testFilterNonMatches() throws IOException {
    JsonParser parser = new JsonParser();
    JsonElement input = parser.parse(getResourceString("filter_input.json"));
    GenericJsonEvent devent = new GenericJsonEvent(input.getAsJsonObject());

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    String regexMatch = "(val)";
    String regexFail = "(fail)";
    String path = "$.simple_object.key";
    Boolean match = false;

    assertNotNull(devent.getFieldAsString(path));

    FilterOperation opMatch = new FilterOperation(regexMatch, path, match);
    FilterOperation opFail = new FilterOperation(regexFail, path, match);

    assertFalse(opMatch.filterEvent(ievent.getEventObj()));
    assertNotNull(opMatch.perform(ievent));
    assertTrue(opFail.filterEvent(ievent.getEventObj()));
    assertNull(opFail.perform(ievent));

  }

  @Test
  public void testInvalidRegex() throws IOException {
    JsonParser parser = new JsonParser();
    JsonElement input = parser.parse(getResourceString("filter_input.json"));
    GenericJsonEvent devent = new GenericJsonEvent(input.getAsJsonObject());

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    String regex = "(val";
    String path = "$.simple_object.key";
    Boolean match = false;
    String expectedErrorMessage = "Invalid regex";

    assertNotNull(devent.getFieldAsString(path));

    FilterOperation op = new FilterOperation(regex, path, match);
    try {
      op.filterEvent(ievent.getEventObj());
      fail();
    } catch (OperationException e) {
      assertEquals(e.getMessage(), expectedErrorMessage);
    }
  }

  @Test
  public void testInvalidPath() throws IOException {
    JsonParser parser = new JsonParser();
    JsonElement input = parser.parse(getResourceString("filter_input.json"));
    GenericJsonEvent devent = new GenericJsonEvent(input.getAsJsonObject());

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    String regex = "(val)";
    String path = "[]";
    Boolean match = false;
    String expectedErrorMessage = "Invalid JsonPath";

    FilterOperation op = new FilterOperation(regex, path, match);
    try {
      op.filterEvent(ievent.getEventObj());
      fail();
    } catch (OperationException e) {
      assertEquals(expectedErrorMessage, e.getMessage());
    }
  }

  @Test
  public void testFieldDoesNotExist() throws IOException {
    JsonParser parser = new JsonParser();
    JsonElement input = parser.parse(getResourceString("filter_input.json"));
    GenericJsonEvent devent = new GenericJsonEvent(input.getAsJsonObject());

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    String regex = "(val)";
    String path = "$.cookie";
    Boolean match = false;

    FilterOperation op = new FilterOperation(regex, path, match);
    boolean filter = op.filterEvent(ievent.getEventObj());
    InternalEvent result = op.perform(ievent);

    assertNull(devent.getFieldAsString(path));
    assertTrue(filter);
    assertNull(result);
  }

  @Test
  public void testNullDeserializedEvent() {
    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(null);

    String regex = "(val)";
    String path = "$.simple_object.key";
    Boolean match = false;
    String expectedErrorMessage = "Deserialized object is null";

    FilterOperation op = new FilterOperation(regex, path, match);
    try {
      op.perform(ievent);
      fail();
    } catch(OperationException e) {
      assertEquals(e.getMessage(), expectedErrorMessage);
    }
  }
}
