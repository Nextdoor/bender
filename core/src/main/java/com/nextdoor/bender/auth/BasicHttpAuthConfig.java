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

package com.nextdoor.bender.auth;

import java.io.UnsupportedEncodingException;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.nextdoor.bender.utils.Passwords;

@JsonTypeName("UserPassAuth")
public class BasicHttpAuthConfig extends HttpAuthConfig<BasicHttpAuthConfig> {
  @JsonProperty(required = true)
  private String username;

  @JsonProperty(required = true)
  private String password;

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    /*
     * If password uses KMS then decrypt it.
     */
    if (this.password != null) {
      try {
        return Passwords.getPassword(this.password);
      } catch (UnsupportedEncodingException e) {
        throw new RuntimeException(e);
      }
    }
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}
