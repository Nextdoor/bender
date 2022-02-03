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

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.amazonaws.services.lambda.runtime.events.SNSEvent.SNSRecord;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.event.S3EventNotification;
import com.amazonaws.services.s3.event.S3EventNotification.S3BucketEntity;
import com.amazonaws.services.s3.event.S3EventNotification.S3Entity;
import com.amazonaws.services.s3.event.S3EventNotification.S3EventNotificationRecord;
import com.amazonaws.services.s3.event.S3EventNotification.S3ObjectEntity;
import com.nextdoor.bender.aws.S3MockClientFactory;
import com.nextdoor.bender.handler.Handler;
import com.nextdoor.bender.handler.HandlerTest;

public class SNSS3HandlerTest extends HandlerTest<SNSEvent> {

  private static final String S3_BUCKET = "testbucket";
  private S3MockClientFactory clientFactory;
  private AmazonS3Client client;

  @Override
  public String getConfigFile() {
    return "/com/nextdoor/bender/handler/config_unittest.yaml";
  }

  @Override
  public String getExpectedOutputFile() {
    return "basic_output.json";
  }

  @Override
  public Handler<SNSEvent> getHandler() {
    SNSS3Handler handler = new SNSS3Handler();
    handler.s3ClientFactory = this.clientFactory;

    return handler;
  }

  @Override
  public SNSEvent getTestEvent() throws Exception {
    /*
     * Upload a test resoruce to the mock S3
     */
    String payload = IOUtils.toString(
        new InputStreamReader(this.getClass().getResourceAsStream("basic_input.log"), "UTF-8"));
    this.client.putObject(S3_BUCKET, "basic_input.log", payload);

    /*
     * Create a S3EventNotification event
     */
    S3ObjectEntity objEntity = new S3ObjectEntity("basic_input.log", 1L, null, null);
    S3BucketEntity bucketEntity = new S3BucketEntity(S3_BUCKET, null, null);
    S3Entity entity = new S3Entity(null, bucketEntity, objEntity, null);

    S3EventNotificationRecord rec = new S3EventNotificationRecord(null, null, null,
        "1970-01-01T00:00:00.000Z", null, null, null, entity, null);

    List<S3EventNotificationRecord> notifications = new ArrayList<>(2);
    notifications.add(rec);

    /*
     * Wrap as an SNS Event
     */
    S3EventNotification event = new S3EventNotification(notifications);
    SNSEvent.SNS sns = new SNSEvent.SNS();
    sns.setMessage(event.toJson());

    SNSEvent snsEvent = new SNSEvent();

    ArrayList<SNSRecord> snsRecords = new ArrayList<>(1);
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
      f = new S3MockClientFactory();
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
}
