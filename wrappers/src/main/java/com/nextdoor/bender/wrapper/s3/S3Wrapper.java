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

package com.nextdoor.bender.wrapper.s3;

import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.handler.s3.S3InternalEvent;
import com.nextdoor.bender.wrapper.Wrapper;

/**
 * Wrapper that wraps the deserailized payload with information about the S3 file in which the
 * payload arrived in.
 */
public class S3Wrapper implements Wrapper {
  private String functionName;
  private String functionVersion;
  private long processingTime;
  private long processingDelay;
  private long timestamp;
  private String s3Key;
  private String s3Bucket;
  private String s3KeyVersion;
  private String sha1Hash;

  private Object payload;

  public S3Wrapper() {}

  private S3Wrapper(final InternalEvent internal) {
    S3InternalEvent s3Event = ((S3InternalEvent) internal);
    this.s3Key = s3Event.getS3Key();
    this.s3Bucket = s3Event.getS3Bucket();
    this.s3KeyVersion = s3Event.getS3KeyVersion();
    this.functionName = internal.getCtx().getFunctionName();
    this.functionVersion = internal.getCtx().getFunctionVersion();
    this.processingTime = System.currentTimeMillis();
    this.timestamp = internal.getEventTime();
    this.processingDelay = processingTime - timestamp;
    this.sha1Hash = internal.getEventSha1Hash();

    if (internal.getEventObj() != null) {
      this.payload = internal.getEventObj().getPayload();
    } else {
      this.payload = null;
    }
  }

  public String getFunctionName() {
    return functionName;
  }

  public String getFunctionVersion() {
    return functionVersion;
  }

  public Object getPayload() {
    return payload;
  }

  public S3Wrapper getWrapped(final InternalEvent internal) {
    return new S3Wrapper(internal);
  }

  public long getProcessingTime() {
    return processingTime;
  }

  public long getProcessingDelay() {
    return processingDelay;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public String getS3Key() {
    return s3Key;
  }

  public String getS3Bucket() {
    return s3Bucket;
  }

  public String getS3KeyVersion() {
    return s3KeyVersion;
  }

  public String getSha1Hash() {
    return sha1Hash;
  }
}
