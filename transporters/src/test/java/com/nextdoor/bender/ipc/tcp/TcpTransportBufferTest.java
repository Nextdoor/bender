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

package com.nextdoor.bender.ipc.tcp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.ipc.TransportSerializer;
import org.junit.Test;

public class TcpTransportBufferTest {

  @Test
  public void shouldThrowIfBufferSizeOverflow() {

    TcpTransportConfig config = mock(TcpTransportConfig.class);
    when(config.getMaxBufferSize()).thenReturn(10L);

    TransportSerializer serializer = mock(TransportSerializer.class);

    TcpTransportBuffer buffer = new TcpTransportBuffer(config, serializer);

    InternalEvent event = new InternalEvent("", null, 0);
    byte[] bytes = "123456789".getBytes();
    when(serializer.serialize(eq(event))).thenReturn(bytes);

    assertTrue(buffer.add(event));
    assertEquals(bytes.length, buffer.getInternalBuffer().size());

    try {
      buffer.add(event);
      fail("Should throw IllegalStateException");
    } catch (IllegalStateException ex) {
    }

  }

}
