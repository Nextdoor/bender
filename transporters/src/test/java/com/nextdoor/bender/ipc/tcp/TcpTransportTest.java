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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.evanlennick.retry4j.exception.RetriesExhaustedException;
import com.nextdoor.bender.ipc.TransportException;
import java.io.IOException;
import okio.Buffer;
import okio.Sink;
import org.junit.Test;

public class TcpTransportTest {

  @Test
  public void shouldTryAtLeastOnce() throws TransportException, IOException {

    Sink sink = mock(Sink.class);
    TcpTransport transport = new TcpTransport(sink, 0, 0);

    TcpTransportBuffer transportBuffer = mock(TcpTransportBuffer.class);
    Buffer buffer = new Buffer();
    when(transportBuffer.getInternalBuffer()).thenReturn(buffer);

    transport.sendBatch(transportBuffer);

    verify(sink).write(eq(buffer), eq(0L));

  }

  @Test
  public void shouldRetry() throws TransportException, IOException {

    Sink sink = mock(Sink.class);
    TcpTransport transport = new TcpTransport(sink, 1, 0);

    TcpTransportBuffer transportBuffer = mock(TcpTransportBuffer.class);
    Buffer buffer = new Buffer();
    when(transportBuffer.getInternalBuffer()).thenReturn(buffer);

    doThrow(new IOException()).doNothing().when(sink).write(eq(buffer), eq(0L));
    transport.sendBatch(transportBuffer);

    verify(sink, times(2)).write(eq(buffer), eq(0L));

  }

  @Test
  public void shouldExhaustRetries() throws TransportException, IOException {

    Sink sink = mock(Sink.class);
    TcpTransport transport = new TcpTransport(sink, 4, 0);

    TcpTransportBuffer transportBuffer = mock(TcpTransportBuffer.class);
    Buffer buffer = new Buffer();
    when(transportBuffer.getInternalBuffer()).thenReturn(buffer);

    doThrow(new IOException()).when(sink).write(eq(buffer), eq(0L));

    try {
      transport.sendBatch(transportBuffer);
      fail("Should exhaust retries");
    } catch (TransportException ex) {
      assertTrue(ex.getCause() instanceof RetriesExhaustedException);
    }

    verify(sink, times(5)).write(eq(buffer), eq(0L));

  }

}
