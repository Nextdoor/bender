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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.zip.GZIPInputStream;

import org.apache.log4j.Logger;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.event.S3EventNotification.S3Entity;
import com.amazonaws.services.s3.event.S3EventNotification.S3EventNotificationRecord;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.evanlennick.retry4j.CallExecutor;
import com.evanlennick.retry4j.CallResults;
import com.evanlennick.retry4j.RetryConfig;
import com.evanlennick.retry4j.RetryConfigBuilder;
import com.evanlennick.retry4j.exception.RetriesExhaustedException;
import com.evanlennick.retry4j.exception.UnexpectedException;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.InternalEventIterator;
import com.nextdoor.bender.LambdaContext;
import com.nextdoor.bender.aws.AmazonS3ClientFactory;

/**
 * Creates a contiguous iterator backed by files in S3. Each file is opened and streamed to an
 * internal iterator which outputs individual lines (records) from the file. When a file has no more
 * lines the next file is automatically opened on the following next() or hasNext() call.
 *
 * It is important for the user to always call the close() method as otherwise connection leaking
 * may occur.
 */
public class S3EventIterator implements InternalEventIterator<InternalEvent> {
  private static final long NOTIFICATION_DELAY_GRACE_PERIOD = 300000; // 5 mins
  private static final Logger logger = Logger.getLogger(S3EventIterator.class);
  private final AmazonS3Client client;
  private final List<S3EventNotificationRecord> records;
  private final LambdaContext context;
  private long arrivalTime;
  private int currentIndex = 0;
  private Iterator<String> lineIterator;
  private BufferedReader reader;
  private S3Entity currentS3Entity;
  private RetryConfig config;

  public S3EventIterator(LambdaContext context, List<S3EventNotificationRecord> records,
      AmazonS3ClientFactory s3ClientFactory) {
    this.records = records;
    this.context = context;
    this.client = s3ClientFactory.newInstance();

    this.config = new RetryConfigBuilder()
        .retryOnSpecificExceptions(SocketTimeoutException.class, UncheckedIOException.class)
        .withMaxNumberOfTries(3).withDelayBetweenTries(100, ChronoUnit.MILLIS)
        .withExponentialBackoff().build();
  }

  @Override
  public boolean hasNext() {
    if (this.currentIndex < this.records.size()) {
      return true;
    }

    /*
     * Wrap has next row in retry logic. This is because there is intermittent socket timeouts when
     * reading from S3 that cause the function to hang/fail.
     */
    Callable<Boolean> callable = () -> {
      return this.lineIterator.hasNext();
    };

    boolean hasNext;

    try {
      CallResults<Object> results = new CallExecutor(this.config).execute(callable);
      hasNext = (boolean) results.getResult();
    } catch (RetriesExhaustedException ree) {
      throw new RuntimeException(ree.getCallResults().getLastExceptionThatCausedRetry());
    } catch (UnexpectedException ue) {
      throw ue;
    }

    /*
     * If there are no lines then the reader from which the lines came from should be closed.
     */
    if (!hasNext) {
      closeCurrentReader();
    }

    return hasNext;
  }

  @Override
  public InternalEvent next() {
    updateCursor();

    /*
     * Wrap reading next row in retry logic. This is because there is intermittent socket timeouts
     * when reading from S3 that cause the function to hang/fail.
     */
    Callable<String> callable = () -> {
      return this.lineIterator.next();
    };

    String nextRow;
    try {
      CallResults<Object> results = new CallExecutor(this.config).execute(callable);
      nextRow = (String) results.getResult();
    } catch (RetriesExhaustedException ree) {
      throw new RuntimeException(ree.getCallResults().getLastExceptionThatCausedRetry());
    } catch (UnexpectedException ue) {
      throw ue;
    }

    /*
     * Construct the internal event
     */
    return new S3InternalEvent(nextRow, this.context, this.arrivalTime,
        currentS3Entity.getObject().getKey(), currentS3Entity.getBucket().getName(),
        currentS3Entity.getObject().getVersionId());
  }

  @Override
  public void close() throws IOException {
    closeCurrentReader();
  }

  private void updateCursor() {
    if (this.currentIndex == 0
        || (this.currentIndex < this.records.size() && !this.lineIterator.hasNext())) {
      /*
       * The previous reader must be closed in order to prevent S3 connection leaking
       */
      closeCurrentReader();

      /*
       * Use the S3 trigger event time for arrival time of records in file. This is less precise but
       * avoids making a call to the S3 api to find file creation time. Note that if the
       * deserializer creates a {@link com.nextdoor.bender.deserializer.DeserializedTimeSeriesEvent}
       * then this arrival time is not used.
       */
      S3EventNotificationRecord event = this.records.get(currentIndex);
      this.arrivalTime = event.getEventTime().toDate().getTime();
      this.currentS3Entity = event.getS3();

      /*
       * The S3 Object key is URL encoded and must be decoded before it can be used by the
       * AmazonS3Client
       */
      String key;
      try {
        key = URLDecoder.decode(this.currentS3Entity.getObject().getKey(), "UTF-8");
      } catch (UnsupportedEncodingException e) {
        throw new RuntimeException(e);
      }

      /*
       * Stream object back from S3 into a reader
       */
      String bucketName = this.currentS3Entity.getBucket().getName();
      logger.debug("opening s3://" + bucketName + "/" + key);
      GetObjectRequest req = new GetObjectRequest(bucketName, key);
      S3Object obj = client.getObject(req);
      logger.trace("s3 get request id: " + client.getCachedResponseMetadata(req).getRequestId()
          + " host: " + client.getCachedResponseMetadata(req).getHostId() + " cloudfrontid: "
          + client.getCachedResponseMetadata(req).getCloudFrontId());
      long notificationDelay = obj.getObjectMetadata().getLastModified().getTime()
          - this.arrivalTime;
      if(notificationDelay > NOTIFICATION_DELAY_GRACE_PERIOD) {
        /*
         * Notification delay is measured from when the object was last modified time in S3 to when
         * the SNS message was actually recieved by Bender. (If the producer only writes objects
         * once, then this is effectively the created time.)
         */
        logger.debug("Notification for s3://" + bucketName + "/" + key + " was received at"
            + event.getEventTime().toDate() + " - " + (notificationDelay / 1000) + " sec after the file"
            + " landed in S3 (" + obj.getObjectMetadata().getLastModified() + ").");

      /*
       * If the file is compressed run it through the GZIP decompressor
       */
      // TODO: support different types of compressions
      if (key.endsWith(".gz")) {
        GZIPInputStream gzip;
        try {
          gzip = new GZIPInputStream(obj.getObjectContent());
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
        reader = new BufferedReader(new InputStreamReader(gzip));
      } else {
        reader = new BufferedReader(new InputStreamReader(obj.getObjectContent()));
      }

      /*
       * Note the BufferedReader is lazy and so is the iterator. The object is directly streamed
       * from S3, fed into an input stream and consumed line by line by the iterator.
       */
      this.lineIterator = reader.lines().iterator();

      currentIndex++;
    }
  }

  private void closeCurrentReader() {
    if (this.reader != null) {
      try {
        this.reader.close();
        logger.trace("closed reader");
        this.reader = null;
      } catch (IOException e) {
        logger.warn("Unable to close S3 reader", e);
      }
    }
  }
}
