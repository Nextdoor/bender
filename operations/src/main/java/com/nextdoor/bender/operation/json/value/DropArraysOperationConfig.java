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

package com.nextdoor.bender.operation.json.value;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.nextdoor.bender.operation.OperationConfig;

@JsonTypeName("JsonDropArraysOperation")
@JsonSchemaDescription("Provided a JSON object it will remove any keys which have array values. "
    + "This is helpful for use with ElasticSearch which indexes arrays in a particular manner "
    + "not conducive to exploration. See: "
    + "https://www.elastic.co/guide/en/elasticsearch/guide/current/complex-core-fields.html")
public class DropArraysOperationConfig extends OperationConfig {

  @Override
  public Class<DropArraysOperationFactory> getFactoryClass() {
    return DropArraysOperationFactory.class;
  }
}
