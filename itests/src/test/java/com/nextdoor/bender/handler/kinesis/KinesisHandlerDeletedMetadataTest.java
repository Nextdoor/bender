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

import com.amazonaws.services.lambda.runtime.events.KinesisEvent;
import com.nextdoor.bender.handler.HandlerTest;
import com.nextdoor.bender.testutils.TestUtils;

public class KinesisHandlerDeletedMetadataTest extends HandlerTest<KinesisEvent> {

    @Override
    public KinesisHandler getHandler() {
        return new KinesisHandler();
    }

    @Override
    public KinesisEvent getTestEvent() throws Exception {
        return TestUtils.createEvent(this.getClass(),
                "basic_input.json",
                "arn:aws:kinesis:us-east-1:5678:stream/test-events-stream");
    }

    @Override
    public void setup() {

    }

    @Override
    public void teardown() {

    }

    @Override
    public String getConfigFile() {
        return "/com/nextdoor/bender/handler/config_kinesis_with_delete.yaml";
    }

    @Override
    public String getExpectedOutputFile() {
        return "basic_output_with_delete.json";
    }
}
