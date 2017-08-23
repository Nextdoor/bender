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

package com.nextdoor.bender.aws.auth;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import vc.inreach.aws.request.AWSSigner;

@JsonTypeName("UrlSigningAuth")
public class UrlSigningAuthConfig extends AuthConfig<UrlSigningAuthConfig> {
  @JsonProperty(required = true)
  private String region;

  @JsonProperty(required = true)
  private String service;

  public String getService() {
    return this.service;
  }

  public void setService(String service) {
    this.service = service;
  }

  public String getRegion() {
    return region;
  }

  public void setRegion(String region) {
    this.region = region;
  }

  public AWSSigner getAWSSigner() {
    final com.google.common.base.Supplier<LocalDateTime> clock =
        () -> LocalDateTime.now(ZoneOffset.UTC);
    DefaultAWSCredentialsProviderChain cp = new DefaultAWSCredentialsProviderChain();

    return new AWSSigner(cp, this.region, this.service, clock);
  }
}
