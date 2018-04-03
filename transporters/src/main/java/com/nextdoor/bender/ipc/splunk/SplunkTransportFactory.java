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

package com.nextdoor.bender.ipc.splunk;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpHeaders;

import com.nextdoor.bender.ipc.TransportSerializer;
import com.nextdoor.bender.ipc.http.BaseHttpTransportFactory;
import com.nextdoor.bender.ipc.http.HttpTransport;

/**
 * Creates a {@link HttpTransport} from a {@link SplunkTransportConfig}.
 */
public class SplunkTransportFactory extends BaseHttpTransportFactory {

  @Override
  protected String getPath() {
    return "/services/collector";
  }

  @Override
  protected Map<String, String> getHeaders() {
    SplunkTransportConfig config = (SplunkTransportConfig) super.config;
    Map<String,String> parentHeaders = super.getHeaders();
    Map<String,String> myHeaders = new HashMap<String, String>(parentHeaders);
    String authHeader = "Splunk " + config.getAuthToken();
    myHeaders.put(HttpHeaders.AUTHORIZATION, authHeader);

    return myHeaders;
  }

  @Override
  protected TransportSerializer getSerializer() {
    SplunkTransportConfig config = (SplunkTransportConfig) super.config;
    return new SplunkTransportSerializer(config.getIndex());
  }
}
