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

package com.nextdoor.bender.handler.s3;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDefault;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.nextdoor.bender.handler.HandlerConfig;

@JsonTypeName("S3Handler")
@JsonSchemaDescription("For use with S3 Object creation Lambda triggers. You will also need to set "
    + "the function handler to \"com.nextdoor.bender.handler.s3.S3Handler::handler\". The "
    + "following IAM permissions are required: s3:GetObject and lambda:InvokeFunction. Note you "
    + "will also need to provide permissoins to the S3 bucket to invoke your function.")
public class S3HandlerConfig extends HandlerConfig {
  @JsonSchemaDescription("SNS Topic to publish function falures to. Do not use this for function "
      + "retries. Instead use DLQs http://docs.aws.amazon.com/lambda/latest/dg/dlq.html. This "
      + "requires IAM permission SNS:Publish.")
  @JsonProperty(required = false)
  private String snsNotificationArn = null;

  @JsonSchemaDescription("Logs the original S3 notification that triggered the function.")
  @JsonProperty(required = false)
  @JsonSchemaDefault(value = "false")
  private Boolean logS3Trigger = false;

  public String getSnsNotificationArn() {
    return snsNotificationArn;
  }

  public void setSnsNotificationArn(String snsNotificationArn) {
    this.snsNotificationArn = snsNotificationArn;
  }

  public Boolean getLogS3Trigger() {
    return this.logS3Trigger;
  }

  public void setLogS3Trigger(Boolean logS3Trigger) {
    this.logS3Trigger = logS3Trigger;
  }
}
