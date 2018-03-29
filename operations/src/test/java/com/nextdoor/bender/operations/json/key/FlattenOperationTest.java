/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright 2018 Nextdoor.com, Inc
 */

package com.nextdoor.bender.operations.json.key;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.operation.OperationException;
import com.nextdoor.bender.operation.json.key.FlattenOperation;
import com.nextdoor.bender.operations.json.OperationTest;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class FlattenOperationTest extends OperationTest {

  @Test
  public void testMutatePayload() throws JsonSyntaxException, IOException, OperationException {
    JsonParser parser = new JsonParser();

    JsonElement input = parser.parse(getResourceString("flatten_input.json"));
    String expectedOutput = getResourceString("flatten_output.json");

    DummpyEvent devent = new DummpyEvent();
    devent.payload = input.getAsJsonObject();

    FlattenOperation operation = new FlattenOperation(".");

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);
    operation.perform(ievent);

    assertEquals(parser.parse(expectedOutput), input);
  }
}
