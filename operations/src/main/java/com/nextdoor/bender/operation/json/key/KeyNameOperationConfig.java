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

package com.nextdoor.bender.operation.json.key;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.nextdoor.bender.operation.OperationConfig;

@JsonTypeName("JsonKeyNameOperation")
@JsonSchemaDescription("Provided a JSON object it will recursively append the primitive type of "
    + "the value to the key name. For example {\"foo\": \"one\", \"bar\": 2} will become "
    + "{\"foo__str\": \"one\", \"bar__long\": 2}. The mapping is string:__str, boolean:__boolean, "
    + "array:__arr, number:__long or __float. It also repalces \".\" with \"_\" in key names. "
    + "This operation is particularily useful for modifying JSON which will be written to "
    + "ElasticSearch which does not allow conflicting value types for keys or \".\" in key names.")
public class KeyNameOperationConfig extends OperationConfig {

  @Override
  public Class<KeyNameOperationFactory> getFactoryClass() {
    return KeyNameOperationFactory.class;
  }
}
