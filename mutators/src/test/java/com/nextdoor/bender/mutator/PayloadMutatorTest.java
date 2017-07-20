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

package com.nextdoor.bender.mutator;

import com.google.gson.JsonPrimitive;
import com.nextdoor.bender.mutator.key.KeyNameMutator;
import org.junit.Test;

public class PayloadMutatorTest extends MutatorTest {

  @Test(expected = UnsupportedMutationException.class)
  public void testNonJsonObject() throws UnsupportedMutationException {
    KeyNameMutator mutator = new KeyNameMutator();

    DummpyEvent devent = new DummpyEvent();
    devent.payload = new JsonPrimitive("foo");

    mutator.mutateEvent(devent);
  }

  @Test
  public void testNullPayload() throws UnsupportedMutationException {
    KeyNameMutator mutator = new KeyNameMutator();

    DummpyEvent devent = new DummpyEvent();
    devent.payload = null;

    mutator.mutateEvent(devent);
  }
}
