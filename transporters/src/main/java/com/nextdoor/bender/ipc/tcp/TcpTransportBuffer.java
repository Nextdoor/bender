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

import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.ipc.TransportBuffer;
import com.nextdoor.bender.ipc.TransportSerializer;
import java.io.Closeable;
import okio.Buffer;

public class TcpTransportBuffer implements TransportBuffer, Closeable {

  private final TransportSerializer serializer;
  private final Buffer buffer;
  private final long maxBufferSize;

  public TcpTransportBuffer(TcpTransportConfig config, TransportSerializer serializer) {
    this.serializer = serializer;
    this.buffer = new Buffer();
    this.maxBufferSize = config.getMaxBufferSize();
  }

  @Override
  public boolean add(InternalEvent ievent) throws IllegalStateException {
    byte[] data = serializer.serialize(ievent);
    if (buffer.size() + data.length >= maxBufferSize) {
      throw new IllegalStateException();
    }
    buffer.write(data);
    return true;
  }

  @Override
  public Buffer getInternalBuffer() {
    return buffer;
  }

  @Override
  public boolean isEmpty() {
    return buffer.size() == 0;
  }

  @Override
  public void close() {
    buffer.close();
  }

  @Override
  public void clear() {
    buffer.clear();
  }

}
