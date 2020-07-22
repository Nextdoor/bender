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
 * Copyright 2018 Nextdoor.com, Inc
 *
 */

package com.nextdoor.bender.handler;

import java.util.Collections;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDefault;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.nextdoor.bender.config.AbstractConfig;
import com.nextdoor.bender.monitoring.Tag;

public abstract class HandlerConfig extends AbstractConfig<HandlerConfig> {
  @JsonSchemaDescription("If an uncaught exception occurs fail the function")
  @JsonProperty(required = false)
  @JsonSchemaDefault(value = "true")
  private Boolean failOnException = true;

  @JsonSchemaDescription("Adds Lambda function resource tags to reporters' metrics. Note that "
      + "the lambda:ListTags IAM permission is required.")
  @JsonSchemaDefault("false")
  @JsonProperty(required = false)
  private Boolean includeFunctionTags = false;

  @JsonSchemaDescription("Additional tags to add to reporters' metrics. Note Lambda function "
      + "resource tags take precedence.")
  @JsonSchemaDefault("{}")
  @JsonProperty(required = false)
  private Set<Tag> metricTags = Collections.emptySet();

  @JsonSchemaDescription("Maximum queue size used to buffer raw data prior to deserialization. "
      + "This adds back pressure that ensures Bender does not read quicker than it can process "
      + "and send to your desination (transport). Increasing the buffer will increase memory "
      + "pressure and risk of OOM but will have the benefit of increasing throughput.")
  @JsonProperty(required = false)
  @JsonSchemaDefault(value = "500")
  private Integer queueSize = 500;

  public Boolean getFailOnException() {
    return failOnException;
  }

  public void setFailOnException(Boolean failOnException) {
    this.failOnException = failOnException;
  }

  public Integer getQueueSize() {
    return this.queueSize;
  }

  public void setQueueSize(Integer queueSize) {
    this.queueSize = queueSize;
  }

  public Boolean getIncludeFunctionTags() {
    return this.includeFunctionTags;
  }

  public void setIncludeFunctionTags(Boolean includeFunctionTags) {
    this.includeFunctionTags = includeFunctionTags;
  }

  public Set<Tag> getMetricTags() {
    return this.metricTags;
  }

  public void setMetricTags(Set<Tag> metricTags) {
    this.metricTags = metricTags;
  }

}
