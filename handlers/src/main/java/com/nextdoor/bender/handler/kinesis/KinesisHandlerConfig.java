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

package com.nextdoor.bender.handler.kinesis;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.nextdoor.bender.handler.HandlerConfig;

@JsonTypeName("KinesisHandler")
@JsonSchemaDescription("For use with Kinesis triggers. Set the function handler to "
    + "\"com.nextdoor.bender.handler.s3.SNSS3Handler::handler\". The following IAM permissions "
    + "are also required: kinesis:DescribeStream, kinesis:ListStreams, kinesis:GetShardIterator, "
    + "kinesis:GetRecords, and kinesis:ListTagsForStream.")
public class KinesisHandlerConfig extends HandlerConfig {

}
