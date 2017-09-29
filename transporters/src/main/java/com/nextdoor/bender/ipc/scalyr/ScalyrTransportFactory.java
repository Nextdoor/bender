/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright $year Nextdoor.com, Inc
 */

package com.nextdoor.bender.ipc.scalyr;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;

import com.nextdoor.bender.config.AbstractConfig;
import com.nextdoor.bender.ipc.TransportBuffer;
import com.nextdoor.bender.ipc.TransportException;
import com.nextdoor.bender.ipc.TransportFactory;
import com.nextdoor.bender.ipc.TransportFactoryInitException;
import com.nextdoor.bender.ipc.UnpartitionedTransport;

/**
 * Creates a {@link ScalyrTransport} from a {@link ScalyrTransportConfig}.
 */
public class ScalyrTransportFactory implements TransportFactory {

  private ScalyrTransportConfig config;
  private ScalyrTransportSerializer serializer;
  private CloseableHttpClient client;
  private String url;

  @Override
  public Class<ScalyrTransport> getChildClass() {
    return ScalyrTransport.class;
  }

  @Override
  public void close() {}

  @Override
  public UnpartitionedTransport newInstance() throws TransportFactoryInitException {
    return new ScalyrTransport(this.client, this.url,
        this.config.isUseGzip(), this.config.getRetryCount(), this.config.getRetryDelay());
  }

  @Override
  public TransportBuffer newTransportBuffer() throws TransportException {
    try {
      return new ScalyrTransportBuffer(this.config.getBatchSize(), this.config.isUseGzip(),
          this.serializer);
    } catch (IOException e) {
      throw new TransportException("error creating ElasticSearchTransportBuffer", e);
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

  private CloseableHttpClient getHttpClient() throws TransportFactoryInitException {
    HttpClientBuilder cb = HttpClients.custom();

    if (this.config.isUseSSL()) {
      try {
        cb.setSslcontext(getSSLContext());
        cb = cb.setHostnameVerifier(SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
      } catch (Exception e) {
      }
    }

    cb.setMaxConnTotal(this.config.getThreads());

    SocketConfig sc = SocketConfig.custom().setSoTimeout(this.config.getTimeout()).build();
    cb.setDefaultSocketConfig(sc);

    return cb.build();
  }

  @Override
  public int getMaxThreads() {
    return this.config.getThreads();
  }

  @Override
  public void setConf(AbstractConfig config) {
    this.config = (ScalyrTransportConfig) config;
    this.serializer = new ScalyrTransportSerializer();
    this.client = getHttpClient();

    String confUrl = "";

    if (this.config.isUseSSL()) {
      confUrl += "https://";
    } else {
      confUrl += "http://";
    }

    confUrl += this.config.getHostname() + ":" + this.config.getPort() + "/api/uploadLogs?parser=json&token=" + this.config.getToken();

    this.url = confUrl;
  }
}
