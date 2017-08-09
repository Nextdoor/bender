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

package com.nextdoor.bender.operations.json;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.google.gson.JsonPrimitive;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.operation.OperationException;
import com.nextdoor.bender.operation.json.key.KeyNameOperation;

public class PayloadOperationTest extends OperationTest {

  @Test(expected = OperationException.class)
  public void testNonJsonObject() {
    KeyNameOperation operation = new KeyNameOperation();

    DummpyEvent devent = new DummpyEvent();
    devent.payload = new JsonPrimitive("foo");

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    operation.perform(ievent);
  }

  @Test
  public void testNullPayload() {
    KeyNameOperation operation = new KeyNameOperation();

    DummpyEvent devent = new DummpyEvent();
    devent.payload = null;

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    InternalEvent output = operation.perform(ievent);
    assertEquals(null, output);
  }
}
