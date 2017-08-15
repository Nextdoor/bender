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

package com.nextdoor.bender.wrapper.basic;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.nextdoor.bender.wrapper.WrapperConfig;

@JsonTypeName("BasicWrapper")
@JsonSchemaDescription("Includes the original payload, sha1 hash of the original "
    + "event and event timestamp.")
public class BasicWrapperConfig extends WrapperConfig {

  @Override
  public Class<BasicWrapperFactory> getFactoryClass() {
    return BasicWrapperFactory.class;
  }
}
