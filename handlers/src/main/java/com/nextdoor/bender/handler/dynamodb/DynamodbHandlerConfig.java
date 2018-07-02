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

package com.nextdoor.bender.handler.dynamodb;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.nextdoor.bender.handler.HandlerConfig;

@JsonTypeName("DynamodbHandler")
@JsonSchemaDescription("For use with DynamoDB stream triggers. Set the function handler to "
    + "\"com.nextdoor.bender.handler.dynamodb.DynamodbHandler::handler\". The following IAM "
    + "permissions are also required: dynamodb:DescribeStream, dynamodb:GetRecords, "
    + "dynamodb:GetShardIterator, and dynamodb:ListStreams. DynamoDB stream events use "
    + "AttributeValues (see "
    + "https://docs.aws.amazon.com/amazondynamodb/latest/APIReference/API_AttributeValue.html) "
    + "which uses data type as name, but such construct is converted to standard JSON by "
    + "DynamodbHandler. For example, {\"value\":{\"N\":\"123\"}} becomes {\"value\":123}.")
public class DynamodbHandlerConfig extends HandlerConfig {

}
