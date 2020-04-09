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

package com.nextdoor.bender.operation.json.array;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.Test;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.LambdaContext;
import com.nextdoor.bender.aws.TestContext;
import com.nextdoor.bender.deserializer.json.GenericJsonEvent;
import com.nextdoor.bender.operation.OperationException;
import com.nextdoor.bender.operation.json.array.ArraySplitOperation;
import com.nextdoor.bender.operations.json.OperationTest;

public class ArraySplitOperationTest extends OperationTest {
  @Test
  public void testEventString()
      throws JsonSyntaxException, UnsupportedEncodingException, IOException {
    JsonParser parser = new JsonParser();
    JsonElement input = parser.parse(getResourceString("array_input.json"));

    GenericJsonEvent devent = new GenericJsonEvent(input.getAsJsonObject());
    ArraySplitOperation operation = new ArraySplitOperation("$.arr");

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);
    List<String> actual = operation.perform(ievent).stream().map(InternalEvent::getEventString)
        .collect(Collectors.toList());

    List<String> expected = Arrays.asList("{\"foo\":1}", "{\"foo\":2}", "{\"foo\":3}");
    assertEquals(expected, actual);
  }

  @Test
  public void testEventObject()
      throws JsonSyntaxException, UnsupportedEncodingException, IOException {
    JsonParser parser = new JsonParser();
    JsonElement input = parser.parse(getResourceString("array_input.json"));

    GenericJsonEvent devent = new GenericJsonEvent(input.getAsJsonObject());
    ArraySplitOperation operation = new ArraySplitOperation("$.arr");

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    List<String> actual = operation.perform(ievent).stream().map(i -> {
      return i.getEventObj().getPayload().toString();
    }).collect(Collectors.toList());

    List<String> expected = Arrays.asList("{\"foo\":1}", "{\"foo\":2}", "{\"foo\":3}");
    assertEquals(expected, actual);
  }

  @Test
  public void testContextMatch()
      throws JsonSyntaxException, UnsupportedEncodingException, IOException {
    JsonParser parser = new JsonParser();
    TestContext t = new TestContext();
    t.setFunctionName("foo");
    LambdaContext lctx = new LambdaContext(t);

    JsonElement input = parser.parse(getResourceString("array_input.json"));

    GenericJsonEvent devent = new GenericJsonEvent(input.getAsJsonObject());
    ArraySplitOperation operation = new ArraySplitOperation("$.arr");

    InternalEvent ievent = new InternalEvent("", lctx, 1);
    ievent.setEventObj(devent);
    List<InternalEvent> actual = operation.perform(ievent);

    assertEquals(lctx, actual.get(0).getCtx());
    assertEquals(lctx, actual.get(1).getCtx());
    assertEquals(lctx, actual.get(2).getCtx());
  }

  @Test
  public void testMetadataMatch()
      throws JsonSyntaxException, UnsupportedEncodingException, IOException {
    JsonParser parser = new JsonParser();
    TestContext t = new TestContext();
    t.setFunctionName("foo");
    LambdaContext lctx = new LambdaContext(t);

    JsonElement input = parser.parse(getResourceString("array_input.json"));

    GenericJsonEvent devent = new GenericJsonEvent(input.getAsJsonObject());
    ArraySplitOperation operation = new ArraySplitOperation("$.arr");

    InternalEvent ievent = new InternalEvent("", lctx, 1);
    ievent.setEventObj(devent);
    List<InternalEvent> events = operation.perform(ievent);


    assertEquals(new Long(1), events.get(0).getEventMetadata().get("arrivalEpochMs"));
    assertEquals("6a00541bfa24fd59884de557be71f3a5a1344613",
        events.get(0).getEventMetadata().get("eventSha1Hash"));

    assertEquals(new Long(1), events.get(1).getEventMetadata().get("arrivalEpochMs"));
    assertEquals("5cbc013a144777d9ce51385ac24a95ce05f77075",
        events.get(1).getEventMetadata().get("eventSha1Hash"));

    assertEquals(new Long(1), events.get(2).getEventMetadata().get("arrivalEpochMs"));
    assertEquals("6c3023db7ef03721c27df9f33b26e0d2cbc84d6b",
        events.get(2).getEventMetadata().get("eventSha1Hash"));
  }

  @Test
  public void testTimeMatch()
      throws JsonSyntaxException, UnsupportedEncodingException, IOException {
    JsonParser parser = new JsonParser();
    TestContext t = new TestContext();
    t.setFunctionName("foo");
    LambdaContext lctx = new LambdaContext(t);

    JsonElement input = parser.parse(getResourceString("array_input.json"));

    GenericJsonEvent devent = new GenericJsonEvent(input.getAsJsonObject());
    ArraySplitOperation operation = new ArraySplitOperation("$.arr");

    InternalEvent ievent = new InternalEvent("", lctx, 123);
    ievent.setEventObj(devent);
    ievent.setEventTime(124);
    List<InternalEvent> events = operation.perform(ievent);

    assertEquals(ievent.getArrivalTime(), events.get(0).getArrivalTime());
    assertEquals(ievent.getEventTime(), events.get(0).getEventTime());

    assertEquals(ievent.getArrivalTime(), events.get(1).getArrivalTime());
    assertEquals(ievent.getEventTime(), events.get(1).getEventTime());

    assertEquals(ievent.getArrivalTime(), events.get(2).getArrivalTime());
    assertEquals(ievent.getEventTime(), events.get(2).getEventTime());
  }

  @Test(expected = OperationException.class)
  public void testNonArray() throws JsonSyntaxException, UnsupportedEncodingException, IOException {
    JsonParser parser = new JsonParser();
    TestContext t = new TestContext();
    t.setFunctionName("foo");
    LambdaContext lctx = new LambdaContext(t);

    JsonElement input = parser.parse(getResourceString("array_input.json"));

    GenericJsonEvent devent = new GenericJsonEvent(input.getAsJsonObject());
    ArraySplitOperation operation = new ArraySplitOperation("$.arr[0]");

    InternalEvent ievent = new InternalEvent("", lctx, 123);
    ievent.setEventObj(devent);
    ievent.setEventTime(124);
    List<InternalEvent> events = operation.perform(ievent);
  }

  @Test(expected = OperationException.class)
  public void testMissingField()
      throws JsonSyntaxException, UnsupportedEncodingException, IOException {
    JsonParser parser = new JsonParser();
    TestContext t = new TestContext();
    t.setFunctionName("foo");
    LambdaContext lctx = new LambdaContext(t);

    JsonElement input = parser.parse(getResourceString("array_input.json"));

    GenericJsonEvent devent = new GenericJsonEvent(input.getAsJsonObject());
    ArraySplitOperation operation = new ArraySplitOperation("$.bar]");

    InternalEvent ievent = new InternalEvent("", lctx, 123);
    ievent.setEventObj(devent);
    ievent.setEventTime(124);
    List<InternalEvent> events = operation.perform(ievent);
  }

  @Test
  public void testArraySplitWithFieldsToKeep() throws IOException {
    TestContext t = new TestContext();
    t.setFunctionName("foo");
    LambdaContext lctx = new LambdaContext(t);

    JsonElement input = JsonParser.parseString(getResourceString("array_input_kinesis_format.json"));
    GenericJsonEvent devent = new GenericJsonEvent(input.getAsJsonObject());
    List<String> fieldsToKeep = Arrays.asList("owner", "logGroup", "logStream");
    ArraySplitOperation operation = new ArraySplitOperation("$.logEvents", fieldsToKeep);

    InternalEvent ievent = new InternalEvent("", lctx, 123);
    ievent.setEventObj(devent);
    ievent.setEventTime(124);
    List<InternalEvent> events = operation.perform(ievent);

    for (InternalEvent event : events) {
      JsonElement actual = JsonParser.parseString(event.getEventString());
      assertTrue(actual.getAsJsonObject().has("owner"));
      assertTrue(actual.getAsJsonObject().has("logGroup"));
      assertTrue(actual.getAsJsonObject().has("logStream"));
      assertTrue(actual.getAsJsonObject().has("id"));
      assertTrue(actual.getAsJsonObject().has("message"));
      assertTrue(actual.getAsJsonObject().has("timestamp"));
    }
  }
}
