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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.util.regex.Pattern;
import org.junit.Test;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.deserializer.FieldNotFoundException;
import com.nextdoor.bender.deserializer.json.GenericJsonEvent;
import com.nextdoor.bender.operations.json.OperationTest;

public class RegexFilterOperationTest extends OperationTest {

  @Test
  public void testFilterMatches() throws IOException, FieldNotFoundException {
    JsonParser parser = new JsonParser();
    JsonElement input = parser.parse(getResourceString("filter_input.json"));
    GenericJsonEvent devent = new GenericJsonEvent(input.getAsJsonObject());

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    String path = "$.simple_object.key";
    Pattern patternMatch = Pattern.compile("(val)");
    Pattern patternFail = Pattern.compile("(fail)");
    Boolean match = true;

    assertNotNull(devent.getFieldAsString(path));

    RegexFilterOperation opMatch = new RegexFilterOperation(patternMatch, path, match);
    RegexFilterOperation opFail = new RegexFilterOperation(patternFail, path, match);

    assertFalse(opMatch.test(ievent));
    assertTrue(opFail.test(ievent));
  }

  @Test
  public void testFilterNonMatches() throws IOException, FieldNotFoundException {
    JsonParser parser = new JsonParser();
    JsonElement input = parser.parse(getResourceString("filter_input.json"));
    GenericJsonEvent devent = new GenericJsonEvent(input.getAsJsonObject());

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    String path = "$.simple_object.key";
    Pattern patternMatch = Pattern.compile("(val)");
    Pattern patternFail = Pattern.compile("(fail)");
    Boolean match = false;

    assertNotNull(devent.getFieldAsString(path));

    RegexFilterOperation opMatch = new RegexFilterOperation(patternMatch, path, match);
    RegexFilterOperation opFail = new RegexFilterOperation(patternFail, path, match);

    assertTrue(opMatch.test(ievent));
    assertFalse(opFail.test(ievent));
  }

  @Test
  public void testFieldDoesNotExist() throws IOException {
    JsonParser parser = new JsonParser();
    JsonElement input = parser.parse(getResourceString("filter_input.json"));
    GenericJsonEvent devent = new GenericJsonEvent(input.getAsJsonObject());

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    String path = "$.cookie";
    Pattern pattern = Pattern.compile("(val)");
    Boolean match = false;

    RegexFilterOperation op = new RegexFilterOperation(pattern, path, match);
    boolean filter = op.test(ievent);

    assertFalse(filter);
  }

  @Test
  public void testNullDeserializedEvent() {
    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(null);

    String path = "$.simple_object.key";
    Pattern pattern = Pattern.compile("(val)");
    Boolean match = false;

    RegexFilterOperation op = new RegexFilterOperation(pattern, path, match);

    boolean filter = op.test(ievent);

    assertFalse(filter);
  }

  @Test
  public void testInvalidPath() throws IOException {
    JsonParser parser = new JsonParser();
    JsonElement input = parser.parse(getResourceString("filter_input.json"));
    GenericJsonEvent devent = new GenericJsonEvent(input.getAsJsonObject());

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    Pattern pattern = Pattern.compile("(val)");
    String path = "[]";
    Boolean match = false;

    RegexFilterOperation op = new RegexFilterOperation(pattern, path, match);
    boolean filter = op.test(ievent);

    assertFalse(filter);
  }
}
