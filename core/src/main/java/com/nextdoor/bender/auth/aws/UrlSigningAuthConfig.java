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

package com.nextdoor.bender.auth.aws;

import org.apache.hc.core5.http.HttpRequestInterceptor;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.http.AWSRequestSigningApacheInterceptor;
import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.regions.Regions;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.nextdoor.bender.auth.AuthConfig;


@JsonTypeName("UrlSigningAuth")
public class UrlSigningAuthConfig extends AuthConfig<UrlSigningAuthConfig> {
  @JsonProperty(required = true)
  private Regions region;

  @JsonProperty(required = true)
  private String service;

  public String getService() {
    return this.service;
  }

  public void setService(String service) {
    this.service = service;
  }

  public Regions getRegion() {
    return this.region;
  }

  public void setRegion(Regions region) {
    this.region = region;
  }

  public HttpRequestInterceptor getHttpInterceptor() {
    DefaultAWSCredentialsProviderChain cp = new DefaultAWSCredentialsProviderChain();

    AWS4Signer signer = new AWS4Signer();
    signer.setServiceName(this.service);
    signer.setRegionName(this.region.getName());

    return new AWSRequestSigningApacheInterceptor(this.service, signer, cp);
  }
}
