/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF
 * ANY KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * Copyright 2018 Nextdoor.com, Inc
 */

package com.nextdoor.bender.monitoring;

import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDefault;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;

@JsonTypeName("StatFilter")
public class StatFilter {

  @JsonSchemaDescription("Name of the Stat to match against")
  @JsonProperty(required = true)
  private String name;

  @JsonSchemaDescription("Report zero-valued stats")
  @JsonProperty(required = false)
  @JsonSchemaDefault("true")
  private Boolean reportZeros = true;

  @JsonSchemaDescription("Map of tag names and values to match")
  @JsonSchemaDefault("{}")
  @JsonProperty(required = false)
  private Set<Tag> tags = Collections.emptySet();

  public StatFilter() {

  }

  public void setName(String name) {
    this.name = name;
  }

  public void setTags(Set<Tag> tags) {
    this.tags = tags;
  }

  public String getName() {
    return this.name;
  }

  public Set<Tag> getTags() {
    return this.tags;
  }

  public Boolean getReportZeros() {
    return this.reportZeros;
  }

  public void setReportZeros(Boolean reportZeros) {
    this.reportZeros = reportZeros;
  }

  public static Predicate<Stat> isMatch(StatFilter filter) {
    return stat -> {
      if ((stat.getValue() != 0 && !filter.reportZeros)) {
        return false;
      }

      if (filter.name != null && !stat.getName().equals(filter.name)) {
        return false;
      }

      if (!stat.getTags().containsAll(filter.tags)) {
        return false;
      }

      return true;
    };
  }
}
