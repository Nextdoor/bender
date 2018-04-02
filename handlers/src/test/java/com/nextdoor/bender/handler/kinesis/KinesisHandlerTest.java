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

import static org.junit.Assert.assertEquals;

import com.amazonaws.services.lambda.runtime.events.KinesisEvent;
import com.nextdoor.bender.aws.TestContext;
import com.nextdoor.bender.handler.BaseHandler;
import com.nextdoor.bender.handler.HandlerMetadata;
import com.nextdoor.bender.handler.HandlerTest;
import com.nextdoor.bender.testutils.TestUtils;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class KinesisHandlerTest extends HandlerTest<KinesisEvent> {

  @Override
  public KinesisHandler getHandler() {
    return new KinesisHandler();
  }

  @Override
  public KinesisEvent getTestEvent() throws Exception {
    return TestUtils.createEvent(this.getClass(), "basic_input.json");
  }

  @Override
  public void setup() {

  }

  @Test
  public void testPrepareMetadata() throws Exception {
    KinesisHandler handler = new KinesisHandler();
    KinesisEvent event = getTestEvent();
    TestContext ctx = new TestContext();
    ctx.setInvokedFunctionArn("arn:aws:lambda:us-east-1:123:function:test:tag");
    ctx.setFunctionName("test");

    handler.handler(event, ctx);

    List<String> expectedMetadataFields = Arrays
        .asList("sequenceNumber", "sourceArn", "partitionKey", "functionVersion", "functionName",
            "arrivalTime", "eventSource", "processingTime");
    assertEquals(expectedMetadataFields, handler.getMetadata().getFields());

    /**
     * Test the KinesisHandler bits were properly set
     */
    assertEquals("1", handler.getMetadata().getField("partitionKey"));
    assertEquals("2", handler.getMetadata().getField("sequenceNumber"));
    assertEquals("arn:aws:kinesis:us-east-1:1234:stream/test-events-stream",
        handler.getMetadata().getField("sourceArn"));
    assertEquals("Wed Nov 09 16:29:50 PST 2016", handler.getMetadata().getField("arrivalTime"));

    /**
     * Test that the BaseHandler bits were also set..
     */
    assertEquals("test", handler.getMetadata().getField("functionName"));
    assertEquals("aws:kinesis", handler.getMetadata().getField("eventSource"));
    assertEquals(null, handler.getMetadata().getField("functionVersion"));
  }

  @Override
  public String getConfigFile() {
    return "/com/nextdoor/bender/handler/config_kinesis.json";
  }

  @Override
  public void teardown() {

  }
}
