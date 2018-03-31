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

import com.nextdoor.bender.config.AbstractConfig;
import com.nextdoor.bender.ipc.Transport;
import com.nextdoor.bender.ipc.TransportBuffer;
import com.nextdoor.bender.ipc.TransportFactory;
import com.nextdoor.bender.ipc.TransportFactoryInitException;
import com.nextdoor.bender.ipc.generic.GenericTransportSerializer;
import java.io.IOException;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

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
        if (config.getVerifySSL()) {
          socket =
              SSLSocketFactory.getDefault().createSocket(config.getHostname(), config.getPort());
        } else {
          socket = getSSLContext().getSocketFactory().createSocket(config.getHostname(),
              config.getPort());
        }
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

  /**
   * There isn't an easy way in java to trust non-self signed certs. Just allow all until java
   * KeyStore functionality is added to Bender.
   *
   * @return a context that trusts all SSL certs
   */
  private SSLContext getSSLContext() {
    /*
     * Create SSLContext and TrustManager that will trust all SSL certs.
     *
     * Copy pasta from http://stackoverflow.com/a/4837230
     */
    TrustManager tm = new X509TrustManager() {
      public void checkClientTrusted(X509Certificate[] chain, String authType)
          throws CertificateException {}

      public void checkServerTrusted(X509Certificate[] chain, String authType)
          throws CertificateException {}

      public X509Certificate[] getAcceptedIssuers() {
        return null;
      }
    };

    SSLContext ctx;
    try {
      ctx = SSLContext.getInstance("TLS");
    } catch (NoSuchAlgorithmException e) {
      throw new TransportFactoryInitException("JVM does not have proper libraries for TSL");
    }

    try {
      ctx.init(null, new TrustManager[] {tm}, new java.security.SecureRandom());
    } catch (KeyManagementException e) {
      throw new TransportFactoryInitException("Unable to init SSLContext with TrustManager", e);
    }
    return ctx;
  }


  @Override
  public TransportBuffer newTransportBuffer() {
    return new TcpTransportBuffer(config.getMaxBufferSize(), new GenericTransportSerializer());
  }

  @Override
  public void close() {}

  @Override
  public int getMaxThreads() {
    return config.getThreads();
  }

  @Override
  public Class<?> getChildClass() {
    return TcpTransport.class;
  }

}
