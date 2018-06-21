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

package com.nextdoor.bender.operation.substitution.metadata;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDefault;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.nextdoor.bender.operation.substitution.SubstitutionConfig;

@JsonTypeName("MetadataSubstitution")
@JsonSchemaDescription("Substitutes event field value for list of event metadata proprties.")
public class MetadataSubstitutionConfig extends SubstitutionConfig {
  public MetadataSubstitutionConfig() {}
  
  public MetadataSubstitutionConfig(String key, List<String> includes, List<String> excludes, boolean failDstNotFound) {
    super(key, failDstNotFound);
    this.includes = includes;
    this.excludes = excludes;
  }

  @JsonSchemaDescription("List of metadata fields to add. If non-specified then all are added.")
  @JsonProperty(required = false)
  @JsonSchemaDefault(value = "{}")
  private List<String> includes = Collections.emptyList();

  @JsonSchemaDescription("List of metadata fields to exclude. If non-specified then none are excluded.")
  @JsonProperty(required = false)
  @JsonSchemaDefault(value = "{}")
  private List<String> excludes = Collections.emptyList();

  public List<String> getIncludes() {
    return this.includes;
  }

  public void setIncludes(List<String> includes) {
    this.includes = includes;
  }

  public List<String> getExcludes() {
    return this.excludes;
  }

  public void setExcludes(List<String> excludes) {
    this.excludes = excludes;
  }

  @Override
  public boolean equals(Object o) {
    if (!super.equals(o)) {
      return false;
    }

    if (!(o instanceof MetadataSubstitutionConfig)) {
      return false;
    }

    MetadataSubstitutionConfig other = (MetadataSubstitutionConfig) o;

    if (this.includes.equals(other.getIncludes())) {
      return false;
    }

    if (this.excludes.equals(other.getExcludes())) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), this.includes, this.excludes);
  }

  @Override
  public Class<MetadataSubstitutionFactory> getFactoryClass() {
    return MetadataSubstitutionFactory.class;
  }
}
