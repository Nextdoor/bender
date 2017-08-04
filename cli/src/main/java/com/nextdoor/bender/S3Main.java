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

package com.nextdoor.bender;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.s3.event.S3EventNotification;
import com.amazonaws.services.s3.event.S3EventNotification.S3BucketEntity;
import com.amazonaws.services.s3.event.S3EventNotification.S3Entity;
import com.amazonaws.services.s3.event.S3EventNotification.S3EventNotificationRecord;
import com.amazonaws.services.s3.event.S3EventNotification.S3ObjectEntity;
import com.nextdoor.bender.handler.HandlerException;
import com.nextdoor.bender.handler.s3.S3Handler;
import com.nextdoor.bender.ipc.TransportException;
import com.nextdoor.bender.utils.TestContext;

public class S3Main {

  public static void main(String[] args)
      throws UnsupportedEncodingException, IOException, InstantiationException,
      IllegalAccessException, InterruptedException, TransportException, HandlerException {

    TestContext ctx = new TestContext();
    ctx.setFunctionName("cli-main");
    ctx.setInvokedFunctionArn("arn:aws:lambda:us-east-1:123:function:log-pipeline_s3-logs-v001:s3");
    ctx.setAwsRequestId(System.currentTimeMillis() + "");

    /*
     * Create a bare bones S3EventNotification event with two S3 files.
     */
    S3ObjectEntity objEntity = new S3ObjectEntity(
        "syslog/2017/08/03/00/log-pipeline-staging-syslog-delivery-1-2017-08-03-00-02-20-12c5b93b-b5cd-4a26-99de-8ea378bd8e3c.gz",
        1L, null, null);
    // S3ObjectEntity objEntity2 = new S3ObjectEntity("bender.out2", 1L, null, null);

    S3BucketEntity bucketEntity =
        new S3BucketEntity("log-pipeline-staging-processing.analytics.nextdoor.com", null, null);
    S3Entity entity = new S3Entity(null, bucketEntity, objEntity, null);
    // S3Entity entity2 = new S3Entity(null, bucketEntity, objEntity2, null);


    S3EventNotificationRecord rec = new S3EventNotificationRecord(null, null, null,
        "1970-01-01T00:00:00.000Z", null, null, null, entity, null);

    // S3EventNotificationRecord rec2 = new S3EventNotificationRecord(null, null, null,
    // "1970-01-01T00:00:00.000Z", null, null, null, entity2, null);

    List<S3EventNotificationRecord> notifications = new ArrayList<S3EventNotificationRecord>(2);
    notifications.add(rec);
    // notifications.add(rec2);

    S3EventNotification event = new S3EventNotification(notifications);

    /*
     * Call Handler
     */
    S3Handler handler = new S3Handler();
    handler.handler(event, ctx);
    System.exit(0);
  }
}
