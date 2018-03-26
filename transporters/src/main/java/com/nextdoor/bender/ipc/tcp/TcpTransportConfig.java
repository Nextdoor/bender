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

package com.nextdoor.bender.ipc.tcp;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDefault;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.nextdoor.bender.ipc.TransportConfig;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@JsonTypeName("TCP")
@JsonSchemaDescription("Writes events to a TCP endpoint.")
public abstract class TcpTransportConfig extends TransportConfig {

  @JsonSchemaDescription("TCP endpoint hostname.")
  @JsonProperty(required = true)
  private String hostname;

  @JsonSchemaDescription("TCP endpoint port.")
  @JsonProperty(required = true)
  @Min(1)
  @Max(65535)
  private Integer port;

  @JsonSchemaDescription("Use SSL connections (certificates are not validated).")
  @JsonSchemaDefault(value = "false")
  @JsonProperty(required = false)
  private Boolean useSSL = true;

  @JsonSchemaDescription("Maximum size (in bytes) in memory before triggering a write.")
  @JsonSchemaDefault("10240")
  @JsonProperty(required = false)
  @Min(1024)
  @Max(10485760L)
  private Long maxBufferSize = 10240L;

  @JsonSchemaDescription("Number of retries to make when a write failure occurs.")
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
  private Long retryDelay = 1000L;

  @JsonSchemaDescription("Socket timeout (in milliseconds) on TCP connection.")
  @JsonSchemaDefault(value = "30000")
  @JsonProperty(required = false)
  @Min(1000)
  @Max(60000)
  private Integer timeout = 30000;

  public String getHostname() {
    return hostname;
  }

  public Integer getPort() {
    return port;
  }

  public Boolean getUseSSL() {
    return useSSL;
  }

  public Long getMaxBufferSize() {
    return maxBufferSize;
  }

  public Integer getRetryCount() {
    return retryCount;
  }

  public Long getRetryDelay() {
    return retryDelay;
  }

  public Integer getTimeout() {
    return timeout;
  }

  public Class<?> getFactoryClass() {
    return TcpTransportFactory.class;
  }

}
