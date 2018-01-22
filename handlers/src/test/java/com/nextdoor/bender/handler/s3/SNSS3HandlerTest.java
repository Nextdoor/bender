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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.amazonaws.services.lambda.runtime.events.SNSEvent.SNSRecord;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.event.S3EventNotification;
import com.amazonaws.services.s3.event.S3EventNotification.S3BucketEntity;
import com.amazonaws.services.s3.event.S3EventNotification.S3Entity;
import com.amazonaws.services.s3.event.S3EventNotification.S3EventNotificationRecord;
import com.amazonaws.services.s3.event.S3EventNotification.S3ObjectEntity;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.nextdoor.bender.handler.BaseHandler;
import com.nextdoor.bender.handler.Handler;
import com.nextdoor.bender.handler.HandlerTest;
import com.nextdoor.bender.ipc.IpcSenderService;
import com.nextdoor.bender.ipc.TransportException;
import com.nextdoor.bender.testutils.DummyTransportHelper;
import com.nextdoor.bender.aws.AmazonSNSClientFactory;
import com.nextdoor.bender.aws.S3MockClientFactory;
import com.nextdoor.bender.aws.TestContext;

public class SNSS3HandlerTest extends HandlerTest<SNSEvent> {

  private static final String S3_BUCKET = "testbucket";
  private S3MockClientFactory clientFactory;
  private AmazonS3Client client;

  @Override
  public String getConfigFile() {
    return "/com/nextdoor/bender/handler/config_unittest.json";
  }

  @Rule
  public TemporaryFolder tmpFolder = new TemporaryFolder();

  @Override
  public Handler<SNSEvent> getHandler() {
    SNSS3Handler handler = new SNSS3Handler();
    handler.s3ClientFactory = this.clientFactory;

    return handler;
  }

  @Override
  public SNSEvent getTestEvent() throws Exception {
    return getTestEvent(S3_BUCKET, true);
  }

  private SNSEvent getTestEvent(String bucket, boolean doPut) throws Exception {
    /*
     * Upload a test resoruce to the mock S3
     */
    if (doPut) {
      String payload = IOUtils.toString(
          new InputStreamReader(this.getClass().getResourceAsStream("basic_input.log"), "UTF-8"));
      this.client.putObject(bucket, "basic_input.log", payload);
    }
    /*
     * Create a S3EventNotification event
     */
    S3ObjectEntity objEntity = new S3ObjectEntity("basic_input.log", 1L, null, null);
    S3BucketEntity bucketEntity = new S3BucketEntity(bucket, null, null);
    S3Entity entity = new S3Entity(null, bucketEntity, objEntity, null);

    S3EventNotificationRecord rec = new S3EventNotificationRecord(null, null, null,
        "1970-01-01T00:00:00.000Z", null, null, null, entity, null);

    List<S3EventNotificationRecord> notifications = new ArrayList<S3EventNotificationRecord>(2);
    notifications.add(rec);

    /*
     * Wrap as an SNS Event
     */
    S3EventNotification event = new S3EventNotification(notifications);
    SNSEvent.SNS sns = new SNSEvent.SNS();
    sns.setMessage(event.toJson());

    SNSEvent snsEvent = new SNSEvent();

    ArrayList<SNSRecord> snsRecords = new ArrayList<SNSRecord>(1);
    SNSRecord snsRecord = new SNSRecord();
    snsRecord.setEventSource("aws:sns");
    snsRecord.setEventVersion("1.0");
    snsRecord.setEventSubscriptionArn("arn");

    snsRecord.setSns(sns);
    snsRecords.add(snsRecord);
    snsEvent.setRecords(snsRecords);

    return snsEvent;
  }

  @Override
  public void setup() {
    /*
     * Patch the handler to use this test's factory which produces a mock client.
     */
    S3MockClientFactory f;
    try {
      f = new S3MockClientFactory(tmpFolder);
    } catch (Exception e) {
      throw new RuntimeException("unable to start s3proxy", e);
    }

    this.clientFactory = f;
    this.client = f.newInstance();
  }

  @Override
  public void teardown() {
    this.clientFactory.shutdown();
  }

  @Test
  public void testExceptionHandlingd() throws Throwable {
    BaseHandler.CONFIG_FILE = "/com/nextdoor/bender/handler/config_test_sns.json";

    TestContext ctx = new TestContext();
    ctx.setFunctionName("unittest");
    ctx.setInvokedFunctionArn("arn:aws:lambda:us-east-1:123:function:test-function:staging");

    /*
     * Invoke handler
     */
    SNSS3Handler fhandler = (SNSS3Handler) getHandler();
    fhandler.init(ctx);

    IpcSenderService ipcSpy = spy(fhandler.getIpcService());
    doThrow(new TransportException("expected")).when(ipcSpy).shutdown();
    fhandler.setIpcService(ipcSpy);

    AmazonSNSClient mockClient = mock(AmazonSNSClient.class);
    AmazonSNSClientFactory mockClientFactory = mock(AmazonSNSClientFactory.class);
    doReturn(mockClient).when(mockClientFactory).newInstance();

    fhandler.snsClientFactory = mockClientFactory;

    SNSEvent event = getTestEvent();

    try {
      fhandler.handler(event, ctx);
    } catch (Exception e) {
    }
    verify(mockClient, times(1)).publish("foo", "basic_input.log", "SNSS3Handler Failed");
  }

  @Test
  public void testSourceRegexFail() throws Throwable {
    BaseHandler.CONFIG_FILE = "/com/nextdoor/bender/handler/config_s3_source.json";

    TestContext ctx = new TestContext();
    ctx.setFunctionName("unittest");
    ctx.setInvokedFunctionArn("arn:aws:lambda:us-east-1:123:function:test-function:staging");

    SNSS3Handler handler = (SNSS3Handler) getHandler();
    handler.init(ctx);

    handler.handler(getTestEvent("foo", false), ctx);
    assertEquals(0, DummyTransportHelper.BufferedTransporter.output.size());
  }

  @Test
  public void testSourceRegex() throws Throwable {
    BaseHandler.CONFIG_FILE = "/com/nextdoor/bender/handler/config_s3_source.json";

    TestContext ctx = new TestContext();
    ctx.setFunctionName("unittest");
    ctx.setInvokedFunctionArn("arn:aws:lambda:us-east-1:123:function:test-function:staging");

    SNSS3Handler handler = (SNSS3Handler) getHandler();
    handler.init(ctx);

    handler.handler(getTestEvent(), ctx);
    assertEquals(1, DummyTransportHelper.BufferedTransporter.output.size());
  }
}
