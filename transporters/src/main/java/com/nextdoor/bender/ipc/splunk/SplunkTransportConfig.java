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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.nextdoor.bender.config.value.ValueConfig;
import com.nextdoor.bender.ipc.http.AbstractHttpTransportConfig;

@JsonTypeName("Splunk")
@JsonSchemaDescription("Writes events to a Splunk HEC endpoint.")
public class SplunkTransportConfig extends AbstractHttpTransportConfig {

  @JsonSchemaDescription("Splunk auth token.")
  @JsonProperty(required = true)
  private ValueConfig<?> authToken = null;

  @JsonSchemaDescription("Splunk data index.")
  @JsonProperty(required = false)
  private String index = null;

  public void setAuthToken(ValueConfig<?> authToken) {
    this.authToken = authToken;
  }

  public ValueConfig<?> getAuthToken() {
    return authToken;
  }

  public void setIndex(String index) {
    this.index = index;
  }

  public String getIndex() {
    return this.index;
  }

  @Override
  public Class<?> getFactoryClass() {
    return SplunkTransportFactory.class;
  }
}
