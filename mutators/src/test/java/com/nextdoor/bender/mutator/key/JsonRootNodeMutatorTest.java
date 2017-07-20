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

package com.nextdoor.bender.mutator.key;

import static org.junit.Assert.assertEquals;

import com.nextdoor.bender.mutator.MutatorTest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.junit.Test;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.nextdoor.bender.mutator.UnsupportedMutationException;

public class JsonRootNodeMutatorTest extends MutatorTest {
  @Test
  public void testMutateRootNode() throws JsonSyntaxException, UnsupportedEncodingException, IOException,
      UnsupportedMutationException {
    JsonParser parser = new JsonParser();
    JsonElement input = parser.parse(getResourceString("basic_input.json"));

    DummpyEvent devent = new DummpyEvent();
    devent.payload = input.getAsJsonObject();

    JsonRootNodeMutatorFactory f = new JsonRootNodeMutatorFactory();
    JsonRootNodeMutator mutator = new JsonRootNodeMutator("$.i.ia");
    mutator.mutateEvent(devent);
    assertEquals("{\"iaa\":\"bar\"}", devent.payload.toString());
  }
}
