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

import com.evanlennick.retry4j.CallExecutor;
import com.evanlennick.retry4j.RetryConfig;
import com.evanlennick.retry4j.RetryConfigBuilder;
import com.evanlennick.retry4j.exception.RetriesExhaustedException;
import com.evanlennick.retry4j.exception.UnexpectedException;
import com.nextdoor.bender.ipc.TransportBuffer;
import com.nextdoor.bender.ipc.TransportException;
import com.nextdoor.bender.ipc.UnpartitionedTransport;
import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;
import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLSocketFactory;
import okio.Buffer;
import okio.Okio;
import okio.Sink;
import org.apache.log4j.Logger;

/**
 * Generic TCP Transport.
 */
public class TcpTransport implements UnpartitionedTransport, Closeable {

  private static final Logger logger = Logger.getLogger(TcpTransport.class);

  private final Sink sink;
  private final RetryConfig retryConfig;

  TcpTransport(TcpTransportConfig config) throws IOException {

    Socket socket;
    if (config.getUseSSL()) {
      socket = SSLSocketFactory.getDefault().createSocket(config.getHostname(), config.getPort());
    } else {
      socket = new Socket(config.getHostname(), config.getPort());
    }
    socket.setReuseAddress(true);
    sink = Okio.sink(socket);
    sink.timeout().timeout(config.getTimeout(), TimeUnit.MILLISECONDS);

    retryConfig = new RetryConfigBuilder()
        .retryOnSpecificExceptions(IOException.class)
        .withDelayBetweenTries(Duration.ofMillis(config.getRetryDelay()))
        .withMaxNumberOfTries(config.getRetryCount())
        .withExponentialBackoff()
        .build();

  }

  @Override
  public void sendBatch(TransportBuffer buffer) throws TransportException {

    Buffer internalBuffer = ((TcpTransportBuffer) buffer).getInternalBuffer();

    Callable<Void> write = () -> {
      try {
        sink.write(internalBuffer, internalBuffer.size());
        return null;
      } catch (IOException ex) {
        throw new TransportException("Error while sending in tcp transport", ex);
      }
    };

    try {
      new CallExecutor(retryConfig).execute(write);
    } catch (RetriesExhaustedException | UnexpectedException ue) {
      throw new TransportException(ue);
    }

  }

  @Override
  public void close() {
    if (sink != null) {
      try {
        sink.close();
      } catch (Exception ex) {
        logger.error("Error while closing tcp transport", ex);
      }
    }
  }

}
