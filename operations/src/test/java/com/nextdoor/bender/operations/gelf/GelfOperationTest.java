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

package com.nextdoor.bender.operations.gelf;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.junit.Test;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.operation.gelf.GelfOperation;
import com.nextdoor.bender.operations.json.OperationTest;

public class GelfOperationTest extends OperationTest {
  @Test
  public void testPrefixing()
      throws JsonSyntaxException, UnsupportedEncodingException, IOException {
    JsonParser parser = new JsonParser();

    JsonElement input = parser.parse(getResourceString("prefixed_input.json"));
    String expectedOutput = getResourceString("prefixed_output.json");

    DummpyEvent devent = new DummpyEvent();
    devent.payload = input.getAsJsonObject();

    GelfOperation op = new GelfOperation(new ArrayList<>());

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);
    op.perform(ievent);

    assertEquals(parser.parse(expectedOutput), input);
  }

  @Test
  public void testFlattenPrefix()
      throws JsonSyntaxException, UnsupportedEncodingException, IOException {
    JsonParser parser = new JsonParser();

    JsonElement input = parser.parse(getResourceString("flatten_prefixed_input.json"));
    String expectedOutput = getResourceString("flatten_prefixed_output.json");

    DummpyEvent devent = new DummpyEvent();
    devent.payload = input.getAsJsonObject();

    GelfOperation op = new GelfOperation(new ArrayList<>());

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);
    op.perform(ievent);

    assertEquals(parser.parse(expectedOutput), input);
  }
}
