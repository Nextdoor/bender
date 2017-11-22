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

package com.nextdoor.bender.ipc.http;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;

import com.nextdoor.bender.auth.BasicHttpAuthConfig;
import com.nextdoor.bender.config.AbstractConfig;
import com.nextdoor.bender.ipc.TransportSerializer;
import com.nextdoor.bender.ipc.generic.GenericTransportSerializer;

public class HttpTransportFactory extends BaseHttpTransportFactory {
  @Override
  protected String getPath() {
    HttpTransportConfig config = (HttpTransportConfig) super.config;
    return config.getPath();
  }

  @Override
  protected TransportSerializer getSerializer() {
    HttpTransportConfig config = (HttpTransportConfig) super.config;
    return new GenericTransportSerializer(config.getSeparator());
  }

  @Override
  public void setConf(AbstractConfig config) {
    HttpTransportConfig httpConfig = (HttpTransportConfig) config;

    BasicHttpAuthConfig auth = (BasicHttpAuthConfig) httpConfig.getBasicHttpAuth();
    if (auth != null) {
      Map<String, String> headers = new HashMap<String, String>(httpConfig.getHttpHeaders());
      byte[] encodedAuth =
          Base64.encodeBase64((auth.getUsername() + ":" + auth.getPassword()).getBytes());

      headers.put(HttpHeaders.AUTHORIZATION, "Basic " + new String(encodedAuth));
      httpConfig.setHttpHeaders(headers);
    }

    super.setConf(config);
  }
}
