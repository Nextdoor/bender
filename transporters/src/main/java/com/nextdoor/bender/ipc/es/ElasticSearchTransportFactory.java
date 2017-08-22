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

package com.nextdoor.bender.ipc.es;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.function.Supplier;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.nextdoor.bender.config.AbstractConfig;
import com.nextdoor.bender.ipc.TransportBuffer;
import com.nextdoor.bender.ipc.TransportException;
import com.nextdoor.bender.ipc.TransportFactory;
import com.nextdoor.bender.ipc.TransportFactoryInitException;
import com.nextdoor.bender.ipc.UnpartitionedTransport;

import vc.inreach.aws.request.AWSSigner;
import vc.inreach.aws.request.AWSSigningRequestInterceptor;

/**
 * Creates a {@link ElasticSearchTransport} from a {@link ElasticSearchTransportConfig}.
 */
public class ElasticSearchTransportFactory implements TransportFactory {

  private ElasticSearchTransportConfig config;
  private ElasticSearchTransportSerializer serializer;
  private String password;

  @Override
  public Class<ElasticSearchTransport> getChildClass() {
    return ElasticSearchTransport.class;
  }

  @Override
  public void close() {}

  @Override
  public UnpartitionedTransport newInstance() throws TransportFactoryInitException {
    return new ElasticSearchTransport(getHttpClient(), this.config.isUseGzip(),
        this.config.getRetryCount(), this.config.getRetryDelay());
  }

  @Override
  public TransportBuffer newTransportBuffer() throws TransportException {
    try {
      return new ElasticSearchTransportBuffer(this.config.getBatchSize(), this.config.isUseGzip(),
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

  private RestClient getHttpClient() throws TransportFactoryInitException {
    HttpHost httpHost;
    boolean additionalConfig = false;

    if (this.config.isUseSSL()) {
      httpHost = new HttpHost(this.config.getHostname(), this.config.getPort(), "https");
      additionalConfig = true;
    } else {
      httpHost = new HttpHost(this.config.getHostname(), this.config.getPort(), "http");
    }

    if (this.config.getUsername() != null && this.password != null) {
      additionalConfig = true;
    }

    RestClientBuilder cb = RestClient.builder(httpHost);

    if (additionalConfig) {
      cb.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
        @Override
        public HttpAsyncClientBuilder customizeHttpClient(
            HttpAsyncClientBuilder httpClientBuilder) {
          if (config.isUseSSL()) {
            httpClientBuilder.setSSLContext(getSSLContext());
            httpClientBuilder
                .setHostnameVerifier(SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
          }

          if (config.getUsername() != null && password != null) {
            /*
             * Send auth via headers as the credentials provider method of auth does not work when
             * using SSL.
             */
            byte[] encodedAuth =
                Base64.encodeBase64((config.getUsername() + ":" + password).getBytes());
            String authHeader = "Basic " + new String(encodedAuth);

            httpClientBuilder.setDefaultHeaders(
                Arrays.asList(new BasicHeader(HttpHeaders.AUTHORIZATION, authHeader)));
          }

          return httpClientBuilder;
        }
      });
    }

    /*
     * Client default is 10 seconds. The default this factory has is 40 seconds.
     */
    cb.setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
      @Override
      public RequestConfig.Builder customizeRequestConfig(
          RequestConfig.Builder requestConfigBuilder) {
        return requestConfigBuilder.setConnectTimeout(5000).setSocketTimeout(config.getTimeout());
      }
    });
    cb.setHttpClientConfigCallback(new SigningHttpClientConfigCallback());
    cb.setMaxRetryTimeoutMillis(config.getTimeout());
    return cb.build();
  }

  public class SigningHttpClientConfigCallback
      implements RestClientBuilder.HttpClientConfigCallback {
    final AWSSigningRequestInterceptor requestInterceptor;

    public SigningHttpClientConfigCallback() {
      final com.google.common.base.Supplier<LocalDateTime> clock =
          () -> LocalDateTime.now(ZoneOffset.UTC);
      DefaultAWSCredentialsProviderChain cp = new DefaultAWSCredentialsProviderChain();

      AWSSigner awsSigner = new AWSSigner(cp, "us-east-1", "es", clock);
      this.requestInterceptor = new AWSSigningRequestInterceptor(awsSigner);
    }

    @Override
    public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
      return httpClientBuilder.addInterceptorLast(this.requestInterceptor);
    }
  }

  @Override
  public int getMaxThreads() {
    return this.config.getThreads();
  }

  @Override
  public void setConf(AbstractConfig config) {
    this.config = (ElasticSearchTransportConfig) config;
    this.password = ((ElasticSearchTransportConfig) config).getPassword();
    this.serializer = new ElasticSearchTransportSerializer(this.config.isUseHashId(),
        this.config.getType(), this.config.getIndex(), this.config.getIndexTimeFormat());
  }
}
