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

import java.util.Arrays;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.client.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.http.message.BasicHeader;

import com.nextdoor.bender.auth.BasicHttpAuthConfig;
import com.nextdoor.bender.auth.aws.UrlSigningAuthConfig;
import com.nextdoor.bender.ipc.TransportFactoryInitException;
import com.nextdoor.bender.ipc.TransportSerializer;
import com.nextdoor.bender.ipc.UnpartitionedTransport;
import com.nextdoor.bender.ipc.http.AbstractHttpTransportFactory;

public class ElasticSearchTransportFactory extends AbstractHttpTransportFactory {

  @Override
  public UnpartitionedTransport newInstance() throws TransportFactoryInitException {
    return new ElasticSearchTransport(this.client, super.getUrl(), this.config.isUseGzip(),
        this.config.getRetryCount(), this.config.getRetryDelay());
  }

  @Override
  protected String getPath() {
    ElasticSearchTransportConfig config = (ElasticSearchTransportConfig) super.config;
    return config.getBulkApiPath();
  }

  @Override
  protected TransportSerializer getSerializer() {
    ElasticSearchTransportConfig config = (ElasticSearchTransportConfig) super.config;

    return new ElasticSearchTransportSerializer(config.isUseHashId(), config.getDocumentType(),
        config.getIndex(), config.getIndexTimeFormat(), config.isUsePartitionsForRouting(),
        config.getRoutingFieldName());
  }

  @Override
  public Class<ElasticSearchTransport> getChildClass() {
    return ElasticSearchTransport.class;
  }

  @Override
  protected CloseableHttpClient getClient(boolean useSSL, String url,
      Map<String, String> stringHeaders, int socketTimeout) {
    HttpClientBuilder cb = super.getClientBuilder(useSSL, url, stringHeaders, socketTimeout);
    ElasticSearchTransportConfig config = (ElasticSearchTransportConfig) super.config;

    if (config.getAuthConfig() != null) {
      if (config.getAuthConfig() instanceof BasicHttpAuthConfig) {
        cb = addUserPassAuth(cb, (BasicHttpAuthConfig) config.getAuthConfig());
      } else if (config.getAuthConfig() instanceof UrlSigningAuthConfig) {
        cb = addSigningAuth(cb, (UrlSigningAuthConfig) config.getAuthConfig());
      }
    }

    RequestConfig rc = RequestConfig.custom().setConnectTimeout(5000)
        .setSocketTimeout(config.getTimeout()).build();
    cb.setDefaultRequestConfig(rc);

    return cb.build();
  }

  private HttpClientBuilder addUserPassAuth(HttpClientBuilder cb, BasicHttpAuthConfig auth) {
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

  private HttpClientBuilder addSigningAuth(HttpClientBuilder cb, UrlSigningAuthConfig auth) {
    return cb.addInterceptorLast(auth.getHttpInterceptor());
  }
}
