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

package com.nextdoor.bender.ipc.s3;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.IOException;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Test;

import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.ipc.TransportException;

public class S3TransportBufferTest {

  @Test
  public void testAdd() throws IOException, TransportException {
    S3TransportSerializer serializer = mock(S3TransportSerializer.class);
    doReturn("foo".getBytes()).when(serializer).serialize(any(InternalEvent.class));
    S3TransportBuffer buffer = new S3TransportBuffer(5, false, serializer);

    InternalEvent mockEvent = mock(InternalEvent.class);
    buffer.add(mockEvent);

    ByteArrayOutputStream baos = buffer.getInternalBuffer();
    baos.close();

    String actual = new String(baos.toByteArray());
    assertEquals("foo", actual);
    assertEquals(false, buffer.isEmpty());
  }

  @Test(expected = IllegalStateException.class)
  public void testAddBufferFull() throws IOException, TransportException {
    S3TransportSerializer serializer = mock(S3TransportSerializer.class);
    doReturn("foo".getBytes()).when(serializer).serialize(any(InternalEvent.class));
    S3TransportBuffer buffer = new S3TransportBuffer(5, false, serializer);

    InternalEvent mockEvent = mock(InternalEvent.class);
    buffer.add(mockEvent);
    buffer.add(mockEvent);
  }

  @Test
  public void testEmptyBuffer() throws IOException, TransportException {
    S3TransportSerializer serializer = mock(S3TransportSerializer.class);
    S3TransportBuffer buffer = new S3TransportBuffer(5, false, serializer);

    assertEquals(true, buffer.isEmpty());
  }

  @Test
  public void testClear() throws IOException, TransportException {
    S3TransportSerializer serializer = mock(S3TransportSerializer.class);
    doReturn("foo".getBytes()).when(serializer).serialize(any(InternalEvent.class));
    S3TransportBuffer buffer = new S3TransportBuffer(5, false, serializer);

    InternalEvent mockEvent = mock(InternalEvent.class);
    buffer.add(mockEvent);
    buffer.close();

    String actual = new String(buffer.getInternalBuffer().toByteArray());
    assertEquals("foo", actual);

    buffer.clear();
    assertEquals(true, buffer.isEmpty());
  }

  @Test
  public void testBz2() throws IOException, TransportException {
    S3TransportSerializer serializer = mock(S3TransportSerializer.class);
    doReturn("foo".getBytes()).when(serializer).serialize(any(InternalEvent.class));
    S3TransportBuffer buffer = new S3TransportBuffer(100, true, serializer);

    InternalEvent mockEvent = mock(InternalEvent.class);
    buffer.add(mockEvent);
    buffer.close();

    byte[] actual = buffer.getInternalBuffer().toByteArray();
    byte[] expected = {66, 90, 104, 57, 49, 65, 89, 38, 83, 89, 73, -2, -60, -91, 0, 0, 0, 1, 0, 1,
        0, -96, 0, 48, -128, 65, 22, 46, -28, -118, 112, -95, 32, -109, -3, -119, 74};

    assertArrayEquals(expected, actual);
  }

  @Test
  public void testDoubleClose() throws IOException, TransportException {
    S3TransportSerializer serializer = mock(S3TransportSerializer.class);
    S3TransportBuffer buffer = new S3TransportBuffer(5, false, serializer);

    buffer.close();
    buffer.close();
  }
}
