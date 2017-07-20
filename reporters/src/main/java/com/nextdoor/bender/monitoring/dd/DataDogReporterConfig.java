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

package com.nextdoor.bender.monitoring.dd;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDefault;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.nextdoor.bender.monitoring.ReporterConfig;

@JsonTypeName("DataDog")
public class DataDogReporterConfig extends ReporterConfig {
  @JsonSchemaDefault("lambda.bender")
  @JsonSchemaDescription("Prefix to append to metric names")
  private String prefix = "lambda.bender";

  @Override
  public Class<DataDogReporterFactory> getFactoryClass() {
    return DataDogReporterFactory.class;
  }

  public String getPrefix() {
    return prefix;
  }

  public void setPrefixName(String prefix) {
    this.prefix = prefix;
  }
}
