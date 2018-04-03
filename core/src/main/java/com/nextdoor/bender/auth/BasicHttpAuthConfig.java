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

package com.nextdoor.bender.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.nextdoor.bender.config.value.ValueConfig;

@JsonTypeName("UserPassAuth")
public class BasicHttpAuthConfig extends HttpAuthConfig<BasicHttpAuthConfig> {
  @JsonProperty(required = true)
  private String username;

  @JsonProperty(required = true)
  private ValueConfig<?> password;

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public ValueConfig<?> getPassword() {
    return password;
  }

  public void setPassword(ValueConfig password) {
    this.password = password;
  }
}
