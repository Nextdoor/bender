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
 * Copyright 2016 Nextdoor.com, Inc
 *
 */

package com.nextdoor.bender.utils;

import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.CognitoIdentity;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

/**
 * Mocks out the {@link Context}. Due to unit test configuration limitation this must reside in src
 * instead of unit test path.
 */
public class TestContext implements Context {
  private String functionName;
  private String invokedFunctionArn;
  private String awsRequestId;

  @Override
  public String getAwsRequestId() {
    return this.awsRequestId;
  }

  @Override
  public ClientContext getClientContext() {
    return null;
  }

  @Override
  public String getFunctionName() {
    return this.functionName;
  }

  @Override
  public String getFunctionVersion() {
    return null;
  }

  @Override
  public CognitoIdentity getIdentity() {
    return null;
  }

  @Override
  public String getInvokedFunctionArn() {
    return this.invokedFunctionArn;
  }

  @Override
  public String getLogGroupName() {
    return null;
  }

  @Override
  public String getLogStreamName() {
    return null;
  }

  @Override
  public LambdaLogger getLogger() {
    return null;
  }

  @Override
  public int getMemoryLimitInMB() {
    return 0;
  }

  @Override
  public int getRemainingTimeInMillis() {
    return 0;
  }

  public void setFunctionName(String functionName) {
    this.functionName = functionName;
  }

  public void setInvokedFunctionArn(String invokedFunctionArn) {
    this.invokedFunctionArn = invokedFunctionArn;
  }

  public void setAwsRequestId(String awsRequestId) {
    this.awsRequestId = awsRequestId;
  }
}
