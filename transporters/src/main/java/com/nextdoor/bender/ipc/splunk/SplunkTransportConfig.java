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

package com.nextdoor.bender.ipc.splunk;

import java.io.UnsupportedEncodingException;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.nextdoor.bender.ipc.generic.GenericHttpTransportConfig;
import com.nextdoor.bender.utils.Passwords;

@JsonTypeName("Splunk")
@JsonSchemaDescription("Writes events to a Splunk HEC endpoint.")
public class SplunkTransportConfig extends GenericHttpTransportConfig {

  @JsonSchemaDescription("Splunk auth token. If value is kms encrypted prefix with 'KMS='.")
  @JsonProperty(required = true)
  private String authToken = null;

  @JsonSchemaDescription("Splunk data index.")
  @JsonProperty(required = false)
  private String index = null;

  public void setAuthToken(String authToken) {
    this.authToken = authToken;
  }

  public String getAuthToken() {
    /*
     * Decrypt token using KMS automatically.
     */
    if (this.authToken != null) {
      try {
        return Passwords.getPassword(this.authToken);
      } catch (UnsupportedEncodingException e) {
        throw new RuntimeException(e);
      }
    }

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
