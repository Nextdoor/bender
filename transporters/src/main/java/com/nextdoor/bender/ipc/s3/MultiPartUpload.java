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

package com.nextdoor.bender.ipc.s3;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.amazonaws.services.s3.model.AbortMultipartUploadRequest;
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.PartETag;
import com.amazonaws.services.s3.model.UploadPartRequest;

public class MultiPartUpload {
  private final String uploadId;
  private final String key;
  private final String bucketName;
  private AtomicInteger partCount = new AtomicInteger(0);

  List<PartETag> partETags = new ArrayList<>();

  public MultiPartUpload(String bucketName, String key, String uploadId) {
    this.bucketName = bucketName;
    this.uploadId = uploadId;
    this.key = key;
  }

  public String getUploadId() {
    return this.uploadId;
  }

  public void addPartETag(PartETag tag) {
    this.partETags.add(tag);
  }

  public List<PartETag> getPartEtags() {
    return this.partETags;
  }

  public String getKey() {
    return key;
  }

  public String getBucketName() {
    return bucketName;
  }

  public CompleteMultipartUploadRequest getCompleteMultipartUploadRequest() {
    return new CompleteMultipartUploadRequest(this.bucketName, this.key, this.uploadId,
        this.partETags);
  }

  public int getPartCount() {
    return this.partCount.get();
  }

  public UploadPartRequest getUploadPartRequest() {
    return new UploadPartRequest().withBucketName(this.bucketName).withKey(this.key)
        .withPartNumber(this.partCount.incrementAndGet()).withUploadId(this.uploadId);
  }

  public AbortMultipartUploadRequest getAbortMultipartUploadRequest() {
    return new AbortMultipartUploadRequest(this.bucketName, this.key, this.uploadId);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof MultiPartUpload)) {
      return false;
    }

    MultiPartUpload other = (MultiPartUpload) obj;

    if (!other.getKey().equals(this.key)) {
      return false;
    }

    if (!other.getBucketName().equals(this.bucketName)) {
      return false;
    }

    return true;
  }
}
