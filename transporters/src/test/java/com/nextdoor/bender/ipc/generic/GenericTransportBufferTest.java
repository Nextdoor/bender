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

package com.nextdoor.bender.ipc.generic;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;

import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.ipc.TransportException;
import com.nextdoor.bender.ipc.generic.GenericTransportBuffer;
import com.nextdoor.bender.ipc.scalyr.ScalyrTransportSerializer;

public class GenericTransportBufferTest {

  @Test
  public void testAdd() throws IOException {
    ScalyrTransportSerializer serializer = mock(ScalyrTransportSerializer.class);
    doReturn("foo".getBytes()).when(serializer).serialize(any(InternalEvent.class));
    GenericTransportBuffer buffer = new GenericTransportBuffer(1, false, serializer);

    InternalEvent mockEvent = mock(InternalEvent.class);
    buffer.add(mockEvent);

    ByteArrayOutputStream baos = buffer.getInternalBuffer();
    baos.close();

    String actual = new String(baos.toByteArray());
    assertEquals("foo", actual);
    assertEquals(false, buffer.isEmpty());
  }

  @Test(expected = IllegalStateException.class)
  public void testAddBufferFull() throws IOException {
    ScalyrTransportSerializer serializer = mock(ScalyrTransportSerializer.class);
    doReturn("foo".getBytes()).when(serializer).serialize(any(InternalEvent.class));
    GenericTransportBuffer buffer = new GenericTransportBuffer(1, false, serializer);

    InternalEvent mockEvent = mock(InternalEvent.class);
    buffer.add(mockEvent);
    buffer.add(mockEvent);
  }

  @Test
  public void testEmptyBuffer() throws IOException {
    ScalyrTransportSerializer serializer = mock(ScalyrTransportSerializer.class);
    doReturn("foo".getBytes()).when(serializer).serialize(any(InternalEvent.class));
    GenericTransportBuffer buffer = new GenericTransportBuffer(1, false, serializer);

    assertEquals(true, buffer.isEmpty());
  }

  @Test
  public void testClear() throws IOException {
    ScalyrTransportSerializer serializer = mock(ScalyrTransportSerializer.class);
    doReturn("foo".getBytes()).when(serializer).serialize(any(InternalEvent.class));
    GenericTransportBuffer buffer = new GenericTransportBuffer(1, false, serializer);

    InternalEvent mockEvent = mock(InternalEvent.class);
    buffer.add(mockEvent);
    buffer.close();

    String actual = new String(buffer.getInternalBuffer().toByteArray());
    assertEquals("foo", actual);

    buffer.clear();
    assertEquals(true, buffer.isEmpty());
  }

  @Test
  public void testGzip() throws IOException {
    ScalyrTransportSerializer serializer = mock(ScalyrTransportSerializer.class);
    doReturn("foo".getBytes()).when(serializer).serialize(any(InternalEvent.class));
    GenericTransportBuffer buffer = new GenericTransportBuffer(1, true, serializer);

    InternalEvent mockEvent = mock(InternalEvent.class);
    buffer.add(mockEvent);
    buffer.close();

    byte[] actual = buffer.getInternalBuffer().toByteArray();
    byte[] expected =
        {31, -117, 8, 0, 0, 0, 0, 0, 0, 0, 75, -53, -49, 7, 0, 33, 101, 115, -116, 3, 0, 0, 0};

    assertArrayEquals(expected, actual);
  }

  @Test
  public void testDoubleClose() throws IOException, TransportException {
    ScalyrTransportSerializer serializer = mock(ScalyrTransportSerializer.class);
    GenericTransportBuffer buffer = new GenericTransportBuffer(1, true, serializer);

    buffer.close();
    buffer.close();
  }
}
