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

import com.amazonaws.regions.Regions;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;

public abstract class RegionalReporterConfig extends ReporterConfig {
  @JsonSchemaDescription("Region of remote AWS service. Not required to be set if you are "
      + "using a service within the Lambda's current region. Ensure a service is supported "
      + "in the remote region.")
  private Regions region = null;

  public Regions getRegion() {
    return region;
  }

  public void setRegion(Regions region) {
    this.region = region;
  } 
}
