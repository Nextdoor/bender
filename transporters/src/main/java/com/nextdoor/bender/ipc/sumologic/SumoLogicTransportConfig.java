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

package com.nextdoor.bender.ipc.sumologic;

import java.io.UnsupportedEncodingException;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.nextdoor.bender.ipc.http.AbstractHttpTransportConfig;
import com.nextdoor.bender.utils.Passwords;

@JsonTypeName("SumoLogic")
@JsonSchemaDescription("Writes events to a SumoLogic endpoint.")
public class SumoLogicTransportConfig extends AbstractHttpTransportConfig {
  @JsonSchemaDescription("Sumo Logic auth token. This is suffix of the http source url "
      + "starting after '/receiver/v1/http/'. If value is kms encrypted prefix with 'KMS='.")
  @JsonProperty(required = true)
  private String authToken = null;

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

  public void setAuthToken(String authToken) {
    this.authToken = authToken;
  }

  @Override
  public Class<?> getFactoryClass() {
    return SumoLogicTransportFactory.class;
  }
}
