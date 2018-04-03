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

import java.util.LinkedHashMap;

import org.apache.commons.codec.digest.DigestUtils;

import com.amazonaws.services.lambda.runtime.Context;
import com.nextdoor.bender.InternalEvent;

/**
 * InternalEvent that contains meta information about the S3 file that the event came from as well
 * as the event string itself.
 */
public class S3InternalEvent extends InternalEvent {
  public static final String FILENAME_PARTITION = "__filename__";
  private final String s3Key;
  private final String s3Bucket;
  private final String s3KeyVersion;


  public S3InternalEvent(String eventString, Context context, long arrivalTime, String s3Key,
      String s3Bucket, String s3KeyVersion) {
    super(eventString, context, arrivalTime);
    this.s3Key = s3Key;
    this.s3Bucket = s3Bucket;
    this.s3KeyVersion = s3KeyVersion;
  }

  @Override
  public LinkedHashMap<String, String> getPartitions() {
    LinkedHashMap<String, String> partitions = super.getPartitions();
    if (partitions == null) {
      partitions = new LinkedHashMap<String, String>(1);
      super.setPartitions(partitions);
    }

    partitions.put(FILENAME_PARTITION, DigestUtils.sha1Hex(this.s3Key));
    return partitions;
  }

  public String getS3Key() {
    return this.s3Key;
  }

  public String getS3Bucket() {
    return s3Bucket;
  }

  public String getS3KeyVersion() {
    return s3KeyVersion;
  }
}
