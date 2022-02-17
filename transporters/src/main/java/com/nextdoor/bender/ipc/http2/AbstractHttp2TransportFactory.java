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
 * Copyright 2022 Nextdoor.com, Inc
 *
 */

package com.nextdoor.bender.ipc.http2;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Map;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.ssl.TLS;
import org.apache.hc.core5.http2.HttpVersionPolicy;
import org.apache.hc.core5.pool.PoolConcurrencyPolicy;
import com.nextdoor.bender.config.AbstractConfig;
import com.nextdoor.bender.ipc.TransportBuffer;
import com.nextdoor.bender.ipc.TransportException;
import com.nextdoor.bender.ipc.TransportFactory;
import com.nextdoor.bender.ipc.TransportFactoryInitException;
import com.nextdoor.bender.ipc.TransportSerializer;
import com.nextdoor.bender.ipc.UnpartitionedTransport;
import com.nextdoor.bender.ipc.generic.GenericTransportBuffer;

public abstract class AbstractHttp2TransportFactory implements TransportFactory {
  protected AbstractHttp2TransportConfig config;
  protected TransportSerializer serializer;
  protected CloseableHttpAsyncClient client;

  abstract protected String getPath();

  abstract protected TransportSerializer getSerializer();

  @Override
  public void setConf(AbstractConfig config) {
    this.config = (AbstractHttp2TransportConfig) config;
    this.serializer = getSerializer();
    this.client = getClient(this.config.isUseSSL(), this.getUrl(), this.getHeaders());
  }

  protected String getUrl() {
    String url = "";
    if (this.config.isUseSSL()) {
      url += "https://";
    } else {
      url += "http://";
    }
    url += this.config.getHostname() + ":" + this.config.getPort() + this.getPath();

    return url;
  }

  protected Map<String, String> getHeaders() {
    return this.config.getHttpStringHeaders();
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

  protected HttpAsyncClientBuilder getClientBuilder(boolean useSSL, String url,
      Map<String, String> stringHeaders) {

    HttpAsyncClientBuilder cb = HttpAsyncClients.custom();
    PoolingAsyncClientConnectionManagerBuilder cmb =
        PoolingAsyncClientConnectionManagerBuilder.create();

    /*
     * Setup SSL
     */
    if (useSSL) {
      final ClientTlsStrategyBuilder tsb =
          ClientTlsStrategyBuilder.create().setSslContext(getSSLContext())
              .setTlsVersions(TLS.V_1_3, TLS.V_1_2).setHostnameVerifier(new HostnameVerifier() {
                public boolean verify(String s, SSLSession sslSession) {
                  return true;
                }
              });

      cmb = cmb.setTlsStrategy(tsb.build());
    }

    /*
     * Add default headers
     */
    ArrayList<BasicHeader> headers = new ArrayList<BasicHeader>(stringHeaders.size());
    stringHeaders.forEach((k, v) -> headers.add(new BasicHeader(k, v)));
    cb = cb.setDefaultHeaders(headers);

    /*
     * Pool concurrency settings
     */
    cmb = cmb.setMaxConnPerRoute(this.config.getThreads()).setMaxConnTotal(this.config.getThreads())
        .setPoolConcurrencyPolicy(PoolConcurrencyPolicy.STRICT);

    /*
     * Negotiate on HTTP version with the server
     */
    cb = cb.setVersionPolicy(HttpVersionPolicy.NEGOTIATE);

    return cb.setConnectionManager(cmb.build());
  }

  protected CloseableHttpAsyncClient getClient(boolean useSSL, String url,
      Map<String, String> stringHeaders) {
    return getClientBuilder(useSSL, url, stringHeaders).build();
  }

  @Override
  public int getMaxThreads() {
    return this.config.getThreads();
  }

  @Override
  public void close() {}

  @Override
  public UnpartitionedTransport newInstance() throws TransportFactoryInitException {
    URL url;
    try {
      url = new URL(this.getUrl());
    } catch (MalformedURLException e) {
      throw new TransportFactoryInitException("failed parsing url: " + e.toString());
    }

    return new Http2Transport(this.client, url, this.config.isUseGzip(), this.config.getRetryCount(),
        this.config.getRetryDelay(), this.config.getTimeout());
  }

  @Override
  public TransportBuffer newTransportBuffer() throws TransportException {
    try {
      return new GenericTransportBuffer(this.config.getBatchSize(), this.config.isUseGzip(),
          this.serializer);
    } catch (IOException e) {
      throw new TransportException("error creating GenericTransportBuffer", e);
    }
  }
}
