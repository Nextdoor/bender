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

package com.nextdoor.bender.operation.fork;

import java.util.Collections;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDefault;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.nextdoor.bender.operation.OperationConfig;

@JsonTypeName("ForkOperation")
@JsonSchemaDescription("The fork operation allows nesting multiple operation pipelines within "
    + "Bender. Each fork (pipeline) has its own operations and operates independently on a "
    + "clone of the original event. If no filters are applied then this can result in multiple "
    + "output events for each input event.")
public class ForkOperationConfig extends OperationConfig {

  @JsonSchemaDescription("List of forks.")
  @JsonSchemaDefault("[]")
  @JsonProperty(required = false)
  private List<Fork> forks = Collections.emptyList();

  public static class Fork {
    @JsonSchemaDescription("List of operations to perform.")
    @JsonSchemaDefault("[]")
    @JsonProperty(required = false)
    private List<OperationConfig> operations = Collections.emptyList();

    public List<OperationConfig> getOperations() {
      return this.operations;
    }

    public void setOperations(List<OperationConfig> operations) {
      this.operations = operations;
    }
  }

  public List<Fork> getForks() {
    return this.forks;
  }

  public void setForks(List<Fork> forks) {
    this.forks = forks;
  }

  @Override
  public Class<ForkOperationFactory> getFactoryClass() {
    return ForkOperationFactory.class;
  }
}
