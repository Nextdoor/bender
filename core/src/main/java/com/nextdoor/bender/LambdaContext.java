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
package com.nextdoor.bender;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.amazonaws.services.lambda.runtime.Client;
import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.Context;

public class LambdaContext {
  private final Context ctx;
  private final Map<String, String> ctxMap = new HashMap<String, String>();

  public LambdaContext(Context ctx) {
    this.ctx = ctx;

    if (ctx == null) {
      return;
    }

    this.ctxMap.put("functionVersion", ctx.getFunctionVersion());
    this.ctxMap.put("functionName", ctx.getFunctionName());
    this.ctxMap.put("awsRequestId", ctx.getAwsRequestId());
    this.ctxMap.put("invokedFunctionArn", ctx.getInvokedFunctionArn());
    this.ctxMap.put("logGroupName", ctx.getLogGroupName());
    this.ctxMap.put("logStreamName", ctx.getLogStreamName());

    ClientContext cc = ctx.getClientContext();
    if (cc != null) {
      this.ctxMap.put("clientContextCustom", cc.getCustom().toString());
      this.ctxMap.put("clientContextEnvironment", cc.getEnvironment().toString());

      Client c = cc.getClient();
      if (c != null) {
        this.ctxMap.put("clientContextClientInstallationId", c.getInstallationId());
        this.ctxMap.put("clientContextClientAppPackageName", c.getAppPackageName());
        this.ctxMap.put("clientContextClientAppTitle", c.getAppTitle());
        this.ctxMap.put("clientContextClientAppVersionCode", c.getAppVersionCode());
        this.ctxMap.put("clientContextClientAppVersionName", c.getAppVersionName());
      }
    }

    this.ctxMap.values().removeIf(Objects::isNull);
  }

  public Context getContext() {
    return ctx;
  }

  public Map<String, String> getContextAsMap() {
    return this.ctxMap;
  }
}
