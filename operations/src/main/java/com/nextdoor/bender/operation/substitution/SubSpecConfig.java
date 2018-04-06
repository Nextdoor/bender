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


import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.nextdoor.bender.config.AbstractConfig;

public abstract class SubSpecConfig<T> extends AbstractConfig<T> {
  public SubSpecConfig() {}

  public SubSpecConfig(String key) {
    this.key = key;
  }

  @JsonSchemaDescription("Name of the new field")
  @JsonProperty(required = true)
  private String key;

  public void setKey(String key) {
    this.key = key;
  }

  public String getKey() {
    return this.key;
  }

  public String toString() {
    return this.key;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof SubSpecConfig)) {
      return false;
    }

    SubSpecConfig<?> other = (SubSpecConfig<?>) o;

    if (this.key != other.getKey()) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.key);
  }
}
