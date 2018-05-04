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

package com.nextdoor.bender.operations.json.key;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.regex.Pattern;

import org.junit.Test;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.operation.OperationException;
import com.nextdoor.bender.operation.json.key.KeyNameReplacementOperation;
import com.nextdoor.bender.operations.json.OperationTest;
import com.nextdoor.bender.testutils.DummyDeserializerHelper.DummpyEvent;

public class KeyNameReplacementOperationTest extends OperationTest {

  @Test
  public void testReplaceKeys() throws JsonSyntaxException, IOException, OperationException {
    JsonParser parser = new JsonParser();
    JsonElement input = parser.parse(getResourceString("basic_input_replacement.json"));
    String expectedOutput = getResourceString("basic_output_replacement.json");

    DummpyEvent devent = new DummpyEvent();
    devent.payload = input.getAsJsonObject();

    Pattern p = Pattern.compile("[^a-z]");
    KeyNameReplacementOperation operation = new KeyNameReplacementOperation(p, "_", false);

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);
    operation.perform(ievent);

    assertEquals(parser.parse(expectedOutput), input);
  }
  
  @Test
  public void testDropKeys() throws JsonSyntaxException, IOException, OperationException {
    JsonParser parser = new JsonParser();
    JsonElement input = parser.parse(getResourceString("basic_input_replacement_drop.json"));
    String expectedOutput = getResourceString("basic_output_replacement_drop.json");

    DummpyEvent devent = new DummpyEvent();
    devent.payload = input.getAsJsonObject();

    Pattern p = Pattern.compile("[^a-z]");
    KeyNameReplacementOperation operation = new KeyNameReplacementOperation(p, "", true);

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);
    operation.perform(ievent);
    
    assertEquals(parser.parse(expectedOutput), input);
  }
}
