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

import com.nextdoor.bender.handler.HandlerMetadata;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.amazonaws.services.lambda.runtime.events.SNSEvent.SNSRecord;
import com.amazonaws.services.s3.event.S3EventNotification;
import com.amazonaws.services.s3.event.S3EventNotification.S3EventNotificationRecord;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.google.gson.Gson;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.InternalEventIterator;
import com.nextdoor.bender.aws.AmazonS3ClientFactory;
import com.nextdoor.bender.aws.AmazonSNSClientFactory;
import com.nextdoor.bender.config.Source;
import com.nextdoor.bender.handler.BaseHandler;
import com.nextdoor.bender.handler.Handler;
import com.nextdoor.bender.handler.HandlerException;
import com.nextdoor.bender.utils.SourceUtils;
import com.nextdoor.bender.utils.SourceUtils.SourceNotFoundException;

public class SNSS3Handler extends BaseHandler<SNSEvent> implements Handler<SNSEvent> {
  private static final Logger logger = Logger.getLogger(SNSS3Handler.class);
  private static final Gson gson = new Gson();
  private InternalEventIterator<InternalEvent> recordIterator;
  private List<String> inputFiles;
  private Source source;
  private boolean logTrigger = false;
  protected AmazonS3ClientFactory s3ClientFactory = new AmazonS3ClientFactory();
  protected AmazonSNSClientFactory snsClientFactory = new AmazonSNSClientFactory();
  private SNSEvent event = null;

  @Override
  public void handler(SNSEvent event, Context context) throws HandlerException {
    /*
     * Store the event for getHandlerMetadata()
     */
    this.event = event;

    if (!initialized) {
      init(context);
      SNSS3HandlerConfig handlerConfig = (SNSS3HandlerConfig) this.config.getHandlerConfig();
      this.logTrigger = handlerConfig.getLogSnsTrigger();
    }

    this.source = this.sources.get(0);
    this.inputFiles = new ArrayList<String>(0);

    if (this.logTrigger) {
      logger.info("trigger: " + gson.toJson(event));
    }

    for (SNSRecord record : event.getRecords()) {
      /*
       * Parse SNS as a S3 notification
       */
      String json = record.getSNS().getMessage();
      S3EventNotification s3Event = S3EventNotification.parseJson(json);

      /*
       * Validate the S3 file matches the regex
       */
      List<S3EventNotificationRecord> toProcess =
          new ArrayList<S3EventNotificationRecord>(s3Event.getRecords());
      for (S3EventNotificationRecord s3Record : s3Event.getRecords()) {
        String s3Path = String.format("s3://%s/%s", s3Record.getS3().getBucket().getName(),
            s3Record.getS3().getObject().getKey());
        try {
          this.source = SourceUtils.getSource(s3Path, this.sources);
        } catch (SourceNotFoundException e) {
          logger.warn("skipping processing " + s3Path);
          toProcess.remove(s3Record);
        }
      }

      if (toProcess.size() == 0) {
        logger.warn("Nothing to process");
        return;
      }

      this.inputFiles.addAll(toProcess.stream().map(m -> {
        return m.getS3().getObject().getKey();
      }).collect(Collectors.toList()));

      this.recordIterator = new S3EventIterator(context, toProcess, s3ClientFactory);

      super.process(context);
    }
  }

  @Override
  public Source getSource() {
    return this.source;
  }

  @Override
  public String getSourceName() {
    return "aws:sns";
  }

  @Override
  public void onException(Exception e) {
    /*
     * Always close the iterator to prevent connection leaking
     */
    try {
      if (this.recordIterator != null) {
        this.recordIterator.close();
      }
    } catch (IOException e1) {
      logger.error("unable to close record iterator", e);
    }

    if (this.config == null || this.config.getHandlerConfig() == null) {
      return;
    }

    /*
     * Notify SNS topic
     */
    SNSS3HandlerConfig handlerConfig = (SNSS3HandlerConfig) this.config.getHandlerConfig();
    if (handlerConfig.getSnsNotificationArn() != null) {
      AmazonSNSClient snsClient = this.snsClientFactory.newInstance();
      snsClient.publish(handlerConfig.getSnsNotificationArn(),
          this.inputFiles.stream().map(Object::toString).collect(Collectors.joining(",")),
          "SNSS3Handler Failed");
    }
  }

  @Override
  public InternalEventIterator<InternalEvent> getInternalEventIterator() {
    return this.recordIterator;
  }

  @Override
  public HandlerMetadata getHandlerMetadata() {
    HandlerMetadata metadata = new HandlerMetadata();

    /*
     * TODO: Figure out what metadata I can get here..
     */
    //metadata.setField("sha1hash", firstRecord.getEventSourceARN());
    //this.timestamp = internal.getEventTime();
    //this.processingDelay = processingTime - timestamp;

    return metadata;

  }
}
