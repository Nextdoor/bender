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

package com.nextdoor.bender.time;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import org.junit.Test;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.deserializer.FieldNotFoundException;
import com.nextdoor.bender.operation.OperationException;
import com.nextdoor.bender.testutils.DummyDeserializerHelper.DummyStringEvent;
import com.nextdoor.bender.time.TimeOperationConfig.TimeFieldType;

public class TimeOperationTest {

  @Test
  public void testValidTime() throws FieldNotFoundException {
    InternalEvent ievent = new InternalEvent("foo", null, 1);
    DummyStringEvent devent = spy(new DummyStringEvent(""));
    ievent.setEventObj(devent);
    doReturn("1504728473").when(devent).getFieldAsString("foo");

    TimeOperation op = new TimeOperation("foo", TimeFieldType.SECONDS);
    op.perform(ievent);

    assertEquals(1504728473000l, ievent.getEventTime());
  }

  @Test
  public void testValidISO8601Ms() throws FieldNotFoundException {
    InternalEvent ievent = new InternalEvent("foo", null, 1);
    DummyStringEvent devent = spy(new DummyStringEvent(""));
    ievent.setEventObj(devent);
    doReturn("2020-05-04T10:45:01.480Z").when(devent).getFieldAsString("foo");

    TimeOperation op = new TimeOperation("foo", TimeFieldType.ISO8601);
    op.perform(ievent);

    assertEquals(1588589101480l, ievent.getEventTime());
  }

  @Test
  public void testValidISO8601Ds() throws FieldNotFoundException {
    InternalEvent ievent = new InternalEvent("foo", null, 1);
    DummyStringEvent devent = spy(new DummyStringEvent(""));
    ievent.setEventObj(devent);
    doReturn("2020-05-04T10:45:01Z").when(devent).getFieldAsString("foo");

    TimeOperation op = new TimeOperation("foo", TimeFieldType.ISO8601);
    op.perform(ievent);

    assertEquals(1588589101000l, ievent.getEventTime());
  }

  @Test(expected = OperationException.class)
  public void testInvalidISO8601() throws FieldNotFoundException {
    InternalEvent ievent = new InternalEvent("foo", null, 1);
    DummyStringEvent devent = spy(new DummyStringEvent(""));
    ievent.setEventObj(devent);
    doReturn("note a date string").when(devent).getFieldAsString("foo");

    TimeOperation op = new TimeOperation("foo", TimeFieldType.ISO8601);
    op.perform(ievent);
  }

  @Test(expected = OperationException.class)
  public void testInvalidTime() throws FieldNotFoundException {
    InternalEvent ievent = new InternalEvent("foo", null, 1);
    DummyStringEvent devent = spy(new DummyStringEvent(""));
    ievent.setEventObj(devent);
    doReturn("-1").when(devent).getFieldAsString("foo");

    TimeOperation op = new TimeOperation("foo", TimeFieldType.SECONDS);
    op.perform(ievent);
  }

  @Test(expected = OperationException.class)
  public void testNullField() throws FieldNotFoundException {
    InternalEvent ievent = new InternalEvent("foo", null, 1);
    DummyStringEvent devent = spy(new DummyStringEvent(""));
    ievent.setEventObj(devent);
    doThrow(FieldNotFoundException.class).when(devent).getFieldAsString("foo");

    TimeOperation op = new TimeOperation("foo", TimeFieldType.SECONDS);
    op.perform(ievent);
  }
}
