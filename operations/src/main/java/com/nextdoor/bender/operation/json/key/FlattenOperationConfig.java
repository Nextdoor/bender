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
 * Copyright 2018 Nextdoor.com, Inc
 *
 */

package com.nextdoor.bender.operation.json.key;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDefault;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.nextdoor.bender.operation.OperationConfig;

@JsonTypeName("FlattenOperation")
@JsonSchemaDescription("Provided a deeply nested JSON Object, it will flatten out the object "
    + "into keys with a specific separator (dot by default). For example, if the input is "
    + "{\"foo\": {\"bar\": {\"baz\": 1}}} the operation will produce "
    + "{\"foo.bar.baz\": 1} as the new payload.")
public class FlattenOperationConfig extends OperationConfig {

    @JsonSchemaDescription("Separator to be used between nested key names (typically a dot(.))")
    @JsonSchemaDefault(value = ".")
    private String separator = ".";

    @Override
    public Class<FlattenOperationFactory> getFactoryClass() {
        return FlattenOperationFactory.class;
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }
}
