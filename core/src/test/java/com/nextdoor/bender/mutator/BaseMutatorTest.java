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
 * Copyright $year Nextdoor.com, Inc
 */

package com.nextdoor.bender.mutator;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.nextdoor.bender.deserializer.DeserializedEvent;
import com.nextdoor.bender.testutils.DummyDeserializerHelper.DummyDeserializedEvent;
import com.nextdoor.bender.utils.TestContext;

public class BaseMutatorTest {

  public static class DummyMutator extends BaseMutator {

    @Override
    public DeserializedEvent mutateEvent(DeserializedEvent event) throws UnsupportedMutationException {
      event.setPayload("bogusdata");
      return event;
    }
  }

  @Before
  public void before() {
    mutator = new DummyMutator();
  }

  private DummyMutator mutator;

  @Test
  public void testEventsAllMutated() throws UnsupportedMutationException {
    List<DeserializedEvent> events = new ArrayList<>(2);
    events.add(new DummyDeserializedEvent("foo"));
    events.add(new DummyDeserializedEvent("bar"));

    TestContext context = new TestContext();
    context.setInvokedFunctionArn("arn:aws:lambda:us-east-1:123:function:test:tag");
    List<DeserializedEvent> returned_events = mutator.mutateEvent(events);

    /*
     * Verify Events made it all the way through
     */
    assertEquals(2, returned_events.size());
    assertEquals("bogusdata", returned_events.get(0).getPayload());
    assertEquals("bogusdata", returned_events.get(1).getPayload());
  }

}
