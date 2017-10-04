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

import java.io.UnsupportedEncodingException;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDefault;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.nextdoor.bender.ipc.http.AbstractHttpTransportConfig;
import com.nextdoor.bender.utils.Passwords;

@JsonTypeName("Scalyr")
@JsonSchemaDescription("Writes events to a Scalyr endpoint.")
public class ScalyrTransportConfig extends AbstractHttpTransportConfig {

  @JsonSchemaDescription("Scalyr HTTP endpoint hostname.")
  @JsonSchemaDefault(value = "www.scalyr.com")
  @JsonProperty(required = false)
  private String hostname = "www.scalyr.com";

  @JsonSchemaDescription("Scalyr auth token. If value is kms encrypted prefix with 'KMS='.")
  @JsonProperty(required = true)
  private String token = null;

  @JsonSchemaDescription("Scalyr String Parser.")
  @JsonSchemaDefault(value = "json")
  @JsonProperty(required = false)
  private String parser = "json";

  public String getHostname() {
    return hostname;
  }

  public void setHostname(String hostname) {
    this.hostname = hostname;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public String getToken() {
    /*
     * Decrypt token using KMS automatically.
     */
    if (this.token != null) {
      try {
        return Passwords.getPassword(this.token);
      } catch (UnsupportedEncodingException e) {
        throw new RuntimeException(e);
      }
    }

    return token;
  }

  public String getParser() {
    return parser;
  }

  public void setParser(String parser) {
    this.parser = parser;
  }

  @Override
  public Class<?> getFactoryClass() {
    return ScalyrTransportFactory.class;
  }
}
