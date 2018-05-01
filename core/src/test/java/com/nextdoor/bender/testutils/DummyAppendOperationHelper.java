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

package com.nextdoor.bender.testutils;

import java.util.concurrent.atomic.AtomicInteger;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDefault;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.config.AbstractConfig;
import com.nextdoor.bender.operation.Operation;
import com.nextdoor.bender.operation.OperationConfig;
import com.nextdoor.bender.operation.OperationFactory;

public class DummyAppendOperationHelper {
  public static class DummyAppendOperation implements Operation {

    public String appendStr;

    public DummyAppendOperation(String appendStr) {
      this.appendStr = appendStr;
    }

    @Override
    public InternalEvent perform(InternalEvent ievent) {
      String payloadStr = ievent.getEventObj().getPayload().toString();
      ievent.getEventObj().setPayload(payloadStr + this.appendStr);
      return ievent;
    }
  }

  @JsonTypeName("DummyAppendOperationHelper$DummyAppendOperationConfig")
  public static class DummyAppendOperationConfig extends OperationConfig {

    @JsonSchemaDescription("String to append to payload")
    @JsonProperty(required = true)
    private String appendStr = null;

    public String getAppendStr() {
      return this.appendStr;
    }

    public void setAppendStr(String appendStr) {
      this.appendStr = appendStr;
    }

    @Override
    public Class<DummyAppendOperationFactory> getFactoryClass() {
      return DummyAppendOperationFactory.class;
    }
  }

  public static class DummyAppendOperationFactory implements OperationFactory {
    public Operation op;
    private String appendStr;

    public DummyAppendOperationFactory() {}

    public DummyAppendOperationFactory(Operation op) {
      this.op = op;
    }

    @Override
    public Operation newInstance() {
      return this.op == null ? new DummyAppendOperation(this.appendStr) : this.op;
    }

    @Override
    public Class<DummyAppendOperation> getChildClass() {
      return DummyAppendOperation.class;
    }

    @Override
    public void setConf(AbstractConfig config) {
      DummyAppendOperationConfig conf = (DummyAppendOperationConfig) config;
      this.appendStr = conf.getAppendStr();
    }
  }
}
