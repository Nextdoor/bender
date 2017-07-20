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

package com.nextdoor.bender.handler;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDefault;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.nextdoor.bender.config.AbstractConfig;

public abstract class HandlerConfig extends AbstractConfig<HandlerConfig> {
  @JsonSchemaDescription("If an uncaught exception occurs fail the function")
  @JsonProperty(required=false)
  @JsonSchemaDefault(value = "true")
  private Boolean failOnException = true;

  public Boolean getFailOnException() {
    return failOnException;
  }

  public void setFailOnException(Boolean failOnException) {
    this.failOnException = failOnException;
  }
}
