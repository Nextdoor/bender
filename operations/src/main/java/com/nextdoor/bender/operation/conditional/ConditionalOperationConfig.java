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

package com.nextdoor.bender.operation.conditional;

import java.util.Collections;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDefault;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.nextdoor.bender.operation.FilterOperationConfig;
import com.nextdoor.bender.operation.OperationConfig;
import com.nextdoor.bender.operation.fork.ForkOperationConfig.Fork;

@JsonTypeName("ConditionalOperation")
@JsonSchemaDescription("The conditional operation allows for 'if else' style "
    + "branches in the flow of Bender operations. Conditions are evaluated in order of appearance "
    + "and the first matching condition will receive the event.")
public class ConditionalOperationConfig extends OperationConfig {

  @JsonSchemaDescription("List of conditions and their operations.")
  @JsonSchemaDefault("[]")
  @JsonProperty(required = false)
  private List<Condition> conditions = Collections.emptyList();

  @JsonSchemaDescription("When true events that don't match any conditions will be filtered out.")
  @JsonSchemaDefault(value = "false")
  private Boolean filterNonMatch = false;

  public static class Condition extends Fork {
    @JsonSchemaDescription("Filter operation to evaluate events against.")
    @JsonProperty(required = true)
    private FilterOperationConfig condition;

    public FilterOperationConfig getCondition() {
      return this.condition;
    }

    public void setCondition(FilterOperationConfig condition) {
      this.condition = condition;
    }
  }

  public List<Condition> getConditions() {
    return this.conditions;
  }

  public void setConditions(List<Condition> conditions) {
    this.conditions = conditions;
  }

  public Boolean getFilterNonMatch() {
    return this.filterNonMatch;
  }

  public void setFilterNonMatch(Boolean filterNonMatch) {
    this.filterNonMatch = filterNonMatch;
  }

  @Override
  public Class<ConditionalOperationFactory> getFactoryClass() {
    return ConditionalOperationFactory.class;
  }
}
