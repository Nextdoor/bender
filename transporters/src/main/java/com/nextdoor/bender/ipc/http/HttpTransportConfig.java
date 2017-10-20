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

package com.nextdoor.bender.ipc.http;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;

@JsonTypeName("HTTP")
public class HttpTransportConfig extends AbstractHttpTransportConfig {
  @JsonSchemaDescription("HTTP endpoint path and query string including leading slash '/'.")
  @JsonProperty(required = true)
  private String path = null;

  @JsonSchemaDescription("Separator used between serialized records.")
  @JsonProperty(required = false)
  private Character separator = '\n';

  public void setPath(String path) {
    this.path = path;
  }

  public String getPath() {
    return this.path;
  }

  public void setSeparator(Character separator) {
    this.separator = separator;
  }

  public Character getSeparator() {
    return this.separator;
  }

  @Override
  public Class<?> getFactoryClass() {
    return HttpTransportFactory.class;
  }
}
