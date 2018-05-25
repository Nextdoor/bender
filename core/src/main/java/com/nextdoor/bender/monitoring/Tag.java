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

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Tag {
  @JsonProperty(required = true)
  private String key;

  @JsonProperty(required = true)
  private String value;

  public Tag(String key, String value) {
    this.key = key;
    this.value = value;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String toString() {
    return key + ":" + value;
  }

  public int hashCode() {
    return Objects.hash(this.key);
  }

  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || !(o instanceof Tag)) {
      return false;
    }

    Tag t = (Tag) o;

    /*
     * Only check key because allowing duplicate keys doesn't make sense.
     */
    boolean k = (this.key == null ? t.key == null : this.key.equals(t.key));

    return k;
  }
}
