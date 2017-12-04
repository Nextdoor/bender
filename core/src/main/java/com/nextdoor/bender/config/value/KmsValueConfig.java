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

package com.nextdoor.bender.config.value;

import java.io.UnsupportedEncodingException;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.nextdoor.bender.config.ConfigurationException;
import com.nextdoor.bender.utils.Passwords;

@JsonTypeName("KmsValue")
public class KmsValueConfig extends ValueConfig<KmsValueConfig> {
  @JsonProperty(required = true)
  private Regions region;

  @JsonIgnore
  private boolean decrypted = false;

  public String getValue() {
    if (this.value == null) {
      return value;
    }

    /*
     * Ensure that the value is only decrypted once regardless of usage. This prevents KMS from
     * being called over and over from within the function to decrypt the same field.
     */
    if (!this.decrypted) {
      try {
        this.value = Passwords.decrypt(this.value, Region.getRegion(this.region));
      } catch (UnsupportedEncodingException e) {
        throw new ConfigurationException("Unable to decrypt", e);
      }
      this.decrypted = true;
    }

    return this.value;
  }

  public Regions getRegion() {
    return this.region;
  }

  public void setRegion(Regions region) {
    this.region = region;
  }
}
