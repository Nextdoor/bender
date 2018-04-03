/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF
 * ANY KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * Copyright 2018 Nextdoor.com, Inc
 */

package com.nextdoor.bender.testutils;

import java.io.IOException;
import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.config.AbstractConfig;
import com.nextdoor.bender.ipc.TransportBuffer;
import com.nextdoor.bender.ipc.TransportConfig;
import com.nextdoor.bender.ipc.TransportException;
import com.nextdoor.bender.ipc.TransportFactory;
import com.nextdoor.bender.ipc.TransportFactoryInitException;
import com.nextdoor.bender.ipc.UnpartitionedTransport;

public class DummyTransportHelper {
  public static class ArrayTransportBuffer implements TransportBuffer {
    private ArrayList<String> buffer = new ArrayList<String>();
    private int maxSize;

    public ArrayTransportBuffer() {
      this.maxSize = 1;
    }

    public ArrayTransportBuffer(int maxSize) {
      this.maxSize = maxSize;
    }

    @Override
    public boolean add(InternalEvent ievent) throws IllegalStateException, IOException {
      if (buffer.size() >= maxSize) {
        throw new IllegalStateException("buffer is full");
      }

      buffer.add(ievent.getSerialized());
      return true;
    }

    @Override
    public ArrayList<String> getInternalBuffer() {
      return this.buffer;
    }

    @Override
    public boolean isEmpty() {
      return this.buffer.isEmpty();
    }

    @Override
    public void close() {}

    @Override
    public void clear() {
      this.buffer.clear();
    }
  }

  public static class BufferedTransporter implements UnpartitionedTransport {

    public static ArrayList<String> output = new ArrayList<String>();

    @Override
    public void sendBatch(TransportBuffer buffer) throws TransportException {
      ArrayTransportBuffer buf = (ArrayTransportBuffer) buffer;
      output.addAll(buf.getInternalBuffer());
    }
  }

  public static class BufferedTransporterFactory implements TransportFactory {
    @Override
    public UnpartitionedTransport newInstance() throws TransportFactoryInitException {
      return new BufferedTransporter();
    }

    @Override
    public void setConf(AbstractConfig config) {}

    @Override
    public void close() {}

    @Override
    public TransportBuffer newTransportBuffer() throws TransportException {
      return new ArrayTransportBuffer();
    }

    @Override
    public int getMaxThreads() {
      return 1;
    }

    @Override
    public Class<BufferedTransporter> getChildClass() {
      return BufferedTransporter.class;
    }
  }

  @JsonTypeName("DummyTransportHelper$DummyTransporterConfig")
  public static class DummyTransporterConfig extends TransportConfig {

    @Override
    public Class<BufferedTransporterFactory> getFactoryClass() {
      return BufferedTransporterFactory.class;
    }
  }
}
