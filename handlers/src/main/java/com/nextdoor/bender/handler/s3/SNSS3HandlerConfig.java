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

package com.nextdoor.bender.handler.s3;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.nextdoor.bender.handler.HandlerConfig;

@JsonTypeName("SNSS3Handler")
@JsonSchemaDescription("Similar to the S3Handler but reads SNS notifications which contain S3 "
    + "Object creation events. This is may be required if you have multiple functions running "
    + "against the same bucket. For more information about S3 SNS triggers see "
    + "https://aws.amazon.com/blogs/compute/fanout-s3-event-notifications-to-multiple-endpoints/."
    + "You will need to set the function handler to \"com.nextdoor.bender.handler.s3.SNSS3Handler::handler\". "
    + "The following IAM permissions are required: s3:GetObject and lambda:InvokeFunction. Note you "
    + "will also need to provide permissions to S3 bucket to publish to SNS and SNS to invoke "
    + "your function.")
public class SNSS3HandlerConfig extends HandlerConfig {
  @JsonSchemaDescription("SNS Topic to publish function falures to. Do not use this for function "
      + "retries. Instead use DLQs http://docs.aws.amazon.com/lambda/latest/dg/dlq.html. This "
      + "requires IAM permission SNS:Publish.")
  @JsonProperty(required = false)
  private String snsNotificationArn = null;

  public String getSnsNotificationArn() {
    return snsNotificationArn;
  }

  public void setSnsNotificationArn(String snsNotificationArn) {
    this.snsNotificationArn = snsNotificationArn;
  }
}
