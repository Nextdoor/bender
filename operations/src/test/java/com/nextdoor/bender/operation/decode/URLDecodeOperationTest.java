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
package com.nextdoor.bender.operation.decode;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.testutils.DummyDeserializerHelper.DummpyMapEvent;

public class URLDecodeOperationTest {
  @Test
  public void testDecodeOnce() {
    DummpyMapEvent devent = new DummpyMapEvent();
    devent.setField("foo", "%3Ffoo%3Dbar%26baz%3Dqux");

    URLDecodeOperation op = new URLDecodeOperation(Arrays.asList("foo"), 1);
    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    op.perform(ievent);

    assertEquals(1, devent.payload.size());
    assertEquals("?foo=bar&baz=qux", devent.getFieldAsString("foo"));
  }

  @Test
  public void testDecodeTwice() {
    DummpyMapEvent devent = new DummpyMapEvent();
    devent.setField("foo", "%253Ffoo%253Dbar%2526baz%253Dqux");

    URLDecodeOperation op = new URLDecodeOperation(Arrays.asList("foo"), 2);
    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    op.perform(ievent);

    assertEquals(1, devent.payload.size());
    assertEquals("?foo=bar&baz=qux", devent.getFieldAsString("foo"));
  }

  @Test
  public void testDecodeTwoThings() {
    DummpyMapEvent devent = new DummpyMapEvent();
    devent.setField("foo", "%3Ffoo%3Dbar%26baz%3Dqux");
    devent.setField("bar", "bar%3D%28abc%29");

    URLDecodeOperation op = new URLDecodeOperation(Arrays.asList("foo", "bar"), 1);
    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    op.perform(ievent);

    assertEquals(2, devent.payload.size());
    assertEquals("?foo=bar&baz=qux", devent.getFieldAsString("foo"));
    assertEquals("bar=(abc)", devent.getFieldAsString("bar"));
  }

  @Test
  public void testDecodeTwoThingsEmpty() {
    DummpyMapEvent devent = new DummpyMapEvent();
    devent.setField("foo", "%3Ffoo%3Dbar%26baz%3Dqux");

    URLDecodeOperation op = new URLDecodeOperation(Arrays.asList("foo", "bar"), 1);
    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    op.perform(ievent);

    assertEquals(1, devent.payload.size());
    assertEquals("?foo=bar&baz=qux", devent.getFieldAsString("foo"));
  }
}
