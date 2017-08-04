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

package com.nextdoor.bender.handler.s3;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.s3.event.S3EventNotification;
import com.amazonaws.services.s3.event.S3EventNotification.S3EventNotificationRecord;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.InternalEventIterator;
import com.nextdoor.bender.aws.AmazonS3ClientFactory;
import com.nextdoor.bender.config.Source;
import com.nextdoor.bender.handler.BaseHandler;
import com.nextdoor.bender.handler.Handler;
import com.nextdoor.bender.handler.HandlerException;
import com.nextdoor.bender.utils.SourceUtils;
import com.nextdoor.bender.utils.SourceUtils.SourceNotFoundException;

public class S3Handler extends BaseHandler<S3EventNotification>
    implements Handler<S3EventNotification> {
  private static final Logger logger = Logger.getLogger(S3Handler.class);
  private InternalEventIterator<InternalEvent> recordIterator;
  private Source source;
  protected AmazonS3ClientFactory s3ClientFactory = new AmazonS3ClientFactory();

  public void handler(S3EventNotification event, Context context) throws HandlerException {
    if (!initialized) {
      init(context);
    }

    /*
     * Validate the S3 file matches the regex
     */
    List<S3EventNotificationRecord> toProcess =
        new ArrayList<S3EventNotificationRecord>(event.getRecords());
    for (S3EventNotificationRecord record : event.getRecords()) {
      String s3Path = String.format("s3://%s/%s", record.getS3().getBucket().getName(),
          record.getS3().getObject().getKey());
      try {
        this.source = SourceUtils.getSource(s3Path, this.sources);
      } catch (SourceNotFoundException e) {
        logger.warn("Skipping processing " + s3Path);
        toProcess.remove(record);
      }
    }

    if (toProcess.size() == 0) {
      logger.warn("Nothing to process");
      return;
    }

    this.recordIterator = new S3EventIterator(context, toProcess, s3ClientFactory);

    super.process(context);
  }

  @Override
  public Source getSource() {
    return this.source;
  }

  @Override
  public String getSourceName() {
    return "aws:s3";
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
      logger.error("unable to close record iterator", e1);
    }
  }

  @Override
  public InternalEventIterator<InternalEvent> getInternalEventIterator() {
    return this.recordIterator;
  }
}
