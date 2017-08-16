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

package com.nextdoor.bender.monitoring;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDefault;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.nextdoor.bender.config.ConfigurableFactoryConfig;

public abstract class ReporterConfig extends ConfigurableFactoryConfig {

  @JsonSchemaDescription("List of filter to apply to stats. If a stat matches the filter it is excluded when being published.")
  @JsonSchemaDefault("[]")
  @JsonProperty(required = false)
  private List<StatFilter> statFilters = Collections.emptyList();

  public List<StatFilter> getStatFilters() {
    return statFilters;
  }

  public void setStatFilters(List<StatFilter> statFilters) {
    this.statFilters = statFilters;
  }
}
