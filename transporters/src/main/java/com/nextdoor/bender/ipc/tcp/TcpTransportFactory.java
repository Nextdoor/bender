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

package com.nextdoor.bender.ipc.tcp;

import com.nextdoor.bender.config.AbstractConfig;
import com.nextdoor.bender.ipc.Transport;
import com.nextdoor.bender.ipc.TransportBuffer;
import com.nextdoor.bender.ipc.TransportFactory;
import com.nextdoor.bender.ipc.TransportFactoryInitException;
import com.nextdoor.bender.ipc.generic.GenericTransportSerializer;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLSocketFactory;
import okio.Okio;
import okio.Sink;

/**
 * Creates a {@link TcpTransport} from a {@link TcpTransportConfig}.
 */
public class TcpTransportFactory implements TransportFactory {

  private TcpTransportConfig config;

  @Override
  public void setConf(AbstractConfig config) {
    this.config = (TcpTransportConfig) config;
  }

  public TcpTransportConfig getConfig() {
    return config;
  }

  @Override
  public Transport newInstance() throws TransportFactoryInitException {
    try {
      Socket socket;
      if (config.getUseSSL()) {
        socket = SSLSocketFactory.getDefault().createSocket(config.getHostname(), config.getPort());
      } else {
        socket = new Socket(config.getHostname(), config.getPort());
      }
      socket.setReuseAddress(true);
      Sink sink = Okio.sink(socket);
      sink.timeout().timeout(config.getTimeout(), TimeUnit.MILLISECONDS);
      return new TcpTransport(sink, config.getRetryCount(), config.getRetryDelay());
    } catch (IOException ex) {
      throw new TransportFactoryInitException("Error while creating tcp transport", ex);
    }
  }

  @Override
  public TransportBuffer newTransportBuffer() {
    return new TcpTransportBuffer(config.getMaxBufferSize(), new GenericTransportSerializer());
  }

  @Override
  public void close() {
  }

  @Override
  public int getMaxThreads() {
    return config.getThreads();
  }

  @Override
  public Class<?> getChildClass() {
    return TcpTransport.class;
  }

}
