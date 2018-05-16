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

package com.nextdoor.bender.ipc.kinesis;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import java.io.IOException;
import org.junit.Test;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.ipc.generic.GenericTransportSerializer;

public class KinesisTransportBufferTest {

  @Test
  public void testAdd() throws IOException {
    GenericTransportSerializer serializer = mock(GenericTransportSerializer.class);
    doReturn("foo".getBytes()).when(serializer).serialize(any(InternalEvent.class));
    KinesisTransportBuffer buffer = new KinesisTransportBuffer(serializer, 1);

    InternalEvent mockEvent = mock(InternalEvent.class);
    buffer.add(mockEvent);

    assertEquals(1, buffer.getInternalBuffer().size());
    assertEquals(false, buffer.isEmpty());
  }

  @Test(expected = IllegalStateException.class)
  public void testAddBufferFull() throws IOException {
    GenericTransportSerializer serializer = mock(GenericTransportSerializer.class);
    doReturn("foo".getBytes()).when(serializer).serialize(any(InternalEvent.class));
    KinesisTransportBuffer buffer = new KinesisTransportBuffer(serializer, 1);

    InternalEvent mockEvent = mock(InternalEvent.class);
    buffer.add(mockEvent);
    buffer.add(mockEvent);
  }

  @Test(expected = IOException.class)
  public void testJumboRecord() throws IOException {
    GenericTransportSerializer serializer = mock(GenericTransportSerializer.class);

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 1001 * 1000; i++) {
      sb.append('*');
    }

    byte[] payload = sb.toString().getBytes();
    doReturn(payload).when(serializer).serialize(any(InternalEvent.class));
    KinesisTransportBuffer buffer = new KinesisTransportBuffer(serializer, 500);

    InternalEvent mockEvent = mock(InternalEvent.class);
    buffer.add(mockEvent);
  }

  @Test(expected = IllegalStateException.class)
  public void testBufferMaxBytes() throws IOException {
    GenericTransportSerializer serializer = mock(GenericTransportSerializer.class);

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 1000 * 999; i++) {
      sb.append('*');
    }

    byte[] payload = sb.toString().getBytes();
    doReturn(payload).when(serializer).serialize(any(InternalEvent.class));
    KinesisTransportBuffer buffer = new KinesisTransportBuffer(serializer, 500);

    InternalEvent mockEvent = mock(InternalEvent.class);
    buffer.add(mockEvent);
    buffer.add(mockEvent);
    buffer.add(mockEvent);
    buffer.add(mockEvent);
    buffer.add(mockEvent);
    buffer.add(mockEvent);
  }

  @Test
  public void testEmptyBuffer() throws IOException {
    GenericTransportSerializer serializer = mock(GenericTransportSerializer.class);
    KinesisTransportBuffer buffer = new KinesisTransportBuffer(serializer, 1);

    assertEquals(true, buffer.isEmpty());
  }

  @Test
  public void testClear() throws IOException {
    GenericTransportSerializer serializer = mock(GenericTransportSerializer.class);
    doReturn("foo".getBytes()).when(serializer).serialize(any(InternalEvent.class));
    KinesisTransportBuffer buffer = new KinesisTransportBuffer(serializer, 1);

    InternalEvent mockEvent = mock(InternalEvent.class);
    buffer.add(mockEvent);
    buffer.close();

    assertEquals(1, buffer.getInternalBuffer().size());

    buffer.clear();
    assertEquals(true, buffer.isEmpty());
  }

  @Test
  public void testClearResetCounter() throws IOException {
    GenericTransportSerializer serializer = mock(GenericTransportSerializer.class);
    doReturn("foo".getBytes()).when(serializer).serialize(any(InternalEvent.class));
    KinesisTransportBuffer buffer = new KinesisTransportBuffer(serializer, 1);

    InternalEvent mockEvent = mock(InternalEvent.class);
    buffer.add(mockEvent);

    assertEquals(1, buffer.getInternalBuffer().size());

    buffer.clear();
    assertEquals(true, buffer.isEmpty());
    assertEquals(0, buffer.getInternalBuffer().size());

    buffer.add(mockEvent);
    assertEquals(1, buffer.getInternalBuffer().size());
  }
}
