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

import java.io.InputStreamReader;

import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.nextdoor.bender.handler.HandlerTest;

import org.apache.commons.io.IOUtils;

public class DynamodbHandlerTest extends HandlerTest<DynamodbEvent> {

  @Override
  public DynamodbHandler getHandler() {
    return new DynamodbHandler();
  }

  @Override
  public DynamodbEvent getTestEvent() throws Exception {
    String json = IOUtils.toString(new InputStreamReader(
        this.getClass().getResourceAsStream("dynamodb_records.json"),
        "UTF-8"));
    return DynamodbEventDeserializer.deserialize(json);
  }

  @Override
  public String getExpectedEvent() {
    return "dynamodb_output.json";
  }

  @Override
  public void setup() {
  }

  @Override
  public String getConfigFile() {
    return "/com/nextdoor/bender/handler/config_dynamodb.json";
  }

  @Override
  public void teardown() {
  }
}
