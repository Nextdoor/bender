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
import java.util.Arrays;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;

import com.nextdoor.bender.aws.auth.UrlSigningAuthConfig;
import com.nextdoor.bender.aws.auth.UserPassAuthConfig;
import com.nextdoor.bender.config.AbstractConfig;
import com.nextdoor.bender.ipc.TransportBuffer;
import com.nextdoor.bender.ipc.TransportException;
import com.nextdoor.bender.ipc.TransportFactory;
import com.nextdoor.bender.ipc.TransportFactoryInitException;
import com.nextdoor.bender.ipc.UnpartitionedTransport;
import com.nextdoor.bender.ipc.generic.BenderHttpClientBuilder;
import com.nextdoor.bender.ipc.generic.GenericTransportBuffer;

import vc.inreach.aws.request.AWSSigningRequestInterceptor;

/**
 * Creates a {@link ElasticSearchTransport} from a {@link ElasticSearchTransportConfig}.
 */
public class ElasticSearchTransportFactory implements TransportFactory {

  private ElasticSearchTransportConfig config;
  private ElasticSearchTransportSerializer serializer;
  private RestClient client;

  @Override
  public Class<ElasticSearchTransport> getChildClass() {
    return ElasticSearchTransport.class;
  }

  @Override
  public void close() {}

  @Override
  public UnpartitionedTransport newInstance() throws TransportFactoryInitException {
    return new ElasticSearchTransport(this.client, this.config.isUseGzip(),
        this.config.getRetryCount(), this.config.getRetryDelay());
  }

  @Override
  public TransportBuffer newTransportBuffer() throws TransportException {
    try {
      return new GenericTransportBuffer(this.config.getBatchSize(), this.config.isUseGzip(),
          this.serializer);
    } catch (IOException e) {
      throw new TransportException("error creating ElasticSearchTransportBuffer", e);
    }
  }

  private RestClient getHttpClient() throws TransportFactoryInitException {
    HttpHost httpHost;

    if (this.config.isUseSSL()) {
      httpHost = new HttpHost(this.config.getHostname(), this.config.getPort(), "https");
    } else {
      httpHost = new HttpHost(this.config.getHostname(), this.config.getPort(), "http");
    }

    RestClientBuilder rcb = RestClient.builder(httpHost);

    rcb.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
      @Override
      public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder cb) {
        if (config.isUseSSL()) {
          cb.setSSLContext(BenderHttpClientBuilder.getSSLContext());
          cb.setSSLHostnameVerifier(new HostnameVerifier() {
            public boolean verify(String s, SSLSession sslSession) {
              return true;
            }
          });
        }

        if (config.getAuthConfig() != null) {
          if (config.getAuthConfig() instanceof UserPassAuthConfig) {
            cb = addUserPassAuth(cb, (UserPassAuthConfig) config.getAuthConfig());
          } else if (config.getAuthConfig() instanceof UrlSigningAuthConfig) {
            cb = addSigningAuth(cb, (UrlSigningAuthConfig) config.getAuthConfig());
          }
        }

        return cb;
      }
    });


    /*
     * Client default is 10 seconds. The default this factory has is 40 seconds.
     */
    rcb = rcb.setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
      @Override
      public RequestConfig.Builder customizeRequestConfig(
          RequestConfig.Builder requestConfigBuilder) {
        return requestConfigBuilder.setConnectTimeout(5000).setSocketTimeout(config.getTimeout());
      }
    });
    rcb = rcb.setMaxRetryTimeoutMillis(config.getTimeout());
    return rcb.build();
  }

  private HttpAsyncClientBuilder addUserPassAuth(HttpAsyncClientBuilder cb,
      UserPassAuthConfig auth) {
    /*
     * Send auth via headers as the credentials provider method of auth does not work when using
     * SSL.
     */
    byte[] encodedAuth =
        Base64.encodeBase64((auth.getUsername() + ":" + auth.getPassword()).getBytes());
    String authHeader = "Basic " + new String(encodedAuth);

    cb.setDefaultHeaders(Arrays.asList(new BasicHeader(HttpHeaders.AUTHORIZATION, authHeader)));

    return cb;
  }

  private HttpAsyncClientBuilder addSigningAuth(HttpAsyncClientBuilder cb,
      UrlSigningAuthConfig auth) {
    return cb.addInterceptorLast(new AWSSigningRequestInterceptor(auth.getAWSSigner()));
  }

  @Override
  public int getMaxThreads() {
    return this.config.getThreads();
  }

  @Override
  public void setConf(AbstractConfig config) {
    this.config = (ElasticSearchTransportConfig) config;
    this.serializer = new ElasticSearchTransportSerializer(this.config.isUseHashId(),
        this.config.getDocumentType(), this.config.getIndex(), this.config.getIndexTimeFormat());
    this.client = getHttpClient();
  }
}
