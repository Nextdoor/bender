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
 */

package com.nextdoor.bender.ipc.scalyr;

import java.io.IOException;

import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import com.nextdoor.bender.config.AbstractConfig;
import com.nextdoor.bender.ipc.TransportBuffer;
import com.nextdoor.bender.ipc.TransportException;
import com.nextdoor.bender.ipc.TransportFactory;
import com.nextdoor.bender.ipc.TransportFactoryInitException;
import com.nextdoor.bender.ipc.UnpartitionedTransport;
import com.nextdoor.bender.ipc.generic.BenderHttpClientBuilder;
import com.nextdoor.bender.ipc.generic.GenericHttpTransport;
import com.nextdoor.bender.ipc.generic.GenericTransportBuffer;

/**
 * Creates a {@link GenericHttpTransport} from a {@link ScalyrTransportConfig}.
 */
public class ScalyrTransportFactory implements TransportFactory {

  private ScalyrTransportConfig config;
  private ScalyrTransportSerializer serializer;
  private CloseableHttpClient client;
  private String url;

  @Override
  public Class<GenericHttpTransport> getChildClass() {
    return GenericHttpTransport.class;
  }

  @Override
  public void close() {}

  @Override
  public UnpartitionedTransport newInstance() throws TransportFactoryInitException {
    return new GenericHttpTransport(this.client, this.url, this.config.isUseGzip(),
        this.config.getRetryCount(), this.config.getRetryDelay());
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



  private CloseableHttpClient getHttpClient() throws TransportFactoryInitException {
    HttpClientBuilder cb = BenderHttpClientBuilder.create();

    if (this.config.isUseSSL()) {
      ((BenderHttpClientBuilder) (cb)).withSSL();
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

    confUrl += this.config.getHostname() + ":" + this.config.getPort() + "/api/uploadLogs?parser="
        + this.config.getParser() + "&token=" + this.config.getToken();

    this.url = confUrl;
  }
}
