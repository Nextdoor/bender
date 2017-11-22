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

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDefault;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;

import com.nextdoor.bender.ipc.TransportConfig;
import com.nextdoor.bender.utils.Passwords;

public abstract class AbstractHttpTransportConfig extends TransportConfig {

  @JsonSchemaDescription("HTTP endpoint hostname.")
  @JsonProperty(required = true)
  private String hostname = null;

  @JsonSchemaDescription("HTTP endpoint port.")
  @JsonSchemaDefault(value = "443")
  @JsonProperty(required = false)
  @Min(1)
  @Max(65535)
  private Integer port = 443;

  @JsonSchemaDescription("Use SSL connections (certificates are not validated).")
  @JsonSchemaDefault(value = "false")
  @JsonProperty(required = false)
  private Boolean useSSL = true;

  @JsonSchemaDescription("Use GZIP compression on HTTP calls.")
  @JsonSchemaDefault(value = "false")
  @JsonProperty(required = false)
  private Boolean useGzip = false;

  @JsonSchemaDescription("Maximum number of documents in api call.")
  @JsonSchemaDefault(value = "500")
  @JsonProperty(required = false)
  @Min(500)
  @Max(100000)
  private Integer batchSize = 500;

  @JsonSchemaDescription("Number of retries to make when a put failure occurs.")
  @JsonSchemaDefault(value = "0")
  @JsonProperty(required = false)
  @Min(0)
  @Max(10)
  private Integer retryCount = 0;

  @JsonSchemaDescription("Initial delay between retries. If more than one retries specified exponential backoff is used.")
  @JsonSchemaDefault(value = "1000")
  @JsonProperty(required = false)
  @Min(1)
  @Max(60000)
  private Long retryDelay = 1000l;

  @JsonSchemaDescription("Socket timeout on HTTP connection.")
  @JsonSchemaDefault(value = "40000")
  @JsonProperty(required = false)
  @Min(1000)
  @Max(300000)
  private Integer timeout = 40000;

  @JsonSchemaDescription("HTTP headers to include. If header value starts with KMS= it will be decrypted.")
  @JsonSchemaDefault(value = "{}")
  @JsonProperty(required = false)
  private Map<String,String> httpHeaders = new HashMap<String,String>(0);

  public Boolean isUseSSL() {
    return useSSL;
  }

  public void setUseSSL(Boolean useSSL) {
    this.useSSL = useSSL;
  }

  public Boolean isUseGzip() {
    return useGzip;
  }

  public void setUseGzip(Boolean useGzip) {
    this.useGzip = useGzip;
  }

  public Integer getBatchSize() {
    return batchSize;
  }

  public void setBatchSize(Integer batchSize) {
    this.batchSize = batchSize;
  }

  public Integer getRetryCount() {
    return retryCount;
  }

  public void setRetryCount(Integer retryCount) {
    this.retryCount = retryCount;
  }

  public Long getRetryDelay() {
    return retryDelay;
  }

  public void setRetryDelay(Long retryDelay) {
    this.retryDelay = retryDelay;
  }

  public Integer getTimeout() {
    return timeout;
  }

  public void setTimeout(Integer timeout) {
    this.timeout = timeout;
  }

  public String getHostname() {
    return hostname;
  }

  public void setHostname(String hostname) {
    this.hostname = hostname;
  }

  public Integer getPort() {
    return port;
  }

  public void setPort(Integer port) {
    this.port = port;
  }

  public Map<String,String> getHttpHeaders() {
    if (this.httpHeaders != null) {
      this.httpHeaders.replaceAll((k, v) -> {
        try {
          return Passwords.getPassword(v);
        } catch (UnsupportedEncodingException e) {
          return v;
        }
      });
    }
    return this.httpHeaders;
  }

  public void setHttpHeaders(Map<String,String> httpHeaders) {
    this.httpHeaders = httpHeaders;
  }
}
