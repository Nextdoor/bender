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

package com.nextdoor.bender.operation.substitution;

import java.util.Collections;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.nextdoor.bender.operation.OperationConfig;

@JsonTypeName("SubstitutionOperation")
@JsonSchemaDescription("This operation allows substituting event fields with different sources "
    + "such as other fields, static values, or metadata.")
public class SubstitutionOperationConfig extends OperationConfig {
  @JsonSchemaDescription("List of substitutions to perform.")
  @JsonProperty(required = true)
  private List<SubSpecConfig<?>> substitutions = Collections.emptyList();

  public List<SubSpecConfig<?>> getSubstitutions() {
    return this.substitutions;
  }

  public void setSubstitutions(List<SubSpecConfig<?>> substitutions) {
    this.substitutions = substitutions;
  }

  @Override
  public Class<SubstitutionOperationFactory> getFactoryClass() {
    return SubstitutionOperationFactory.class;
  }
}
