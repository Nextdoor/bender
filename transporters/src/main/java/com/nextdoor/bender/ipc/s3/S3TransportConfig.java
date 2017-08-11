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

package com.nextdoor.bender.ipc.s3;

import javax.validation.constraints.Min;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDefault;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.nextdoor.bender.ipc.TransportConfig;

@JsonTypeName("S3")
@JsonSchemaDescription("Writes batches of events to S3. The output filename will either "
    + "be the unique function inocation request id as specified by Lambda or a hash of the input "
    + "filename when using the S3 handler. Required IAM permissions are: "
    + "s3:AbortMultipartUpload, s3:PutObject, s3:ListMultipartUploadParts, "
    + "s3:ListBucketMultipartUploads")

public class S3TransportConfig extends TransportConfig {

  @JsonSchemaDescription("S3 bucket name.")
  @JsonProperty(required = true)
  private String bucketName;

  @JsonSchemaDescription("Path to append to S3 keys.")
  @JsonProperty(required = false)
  private String basePath;

  @JsonSchemaDescription("Compress files with bz2 compression.")
  @JsonSchemaDefault("true")
  @JsonProperty(required = false)
  private Boolean useCompression = true;

  @JsonSchemaDescription("When using compression optionally compress buffered data as it is serialized. "
      + "This is optimal for no or low cardinality partitioning. When writing high cardinality data "
      + "this may result in OOMs due to high overhead of having a compressor for each partition.")
  @JsonSchemaDefault("false")
  @JsonProperty(required = false)
  private Boolean compressBuffer = false;

  @JsonSchemaDescription("Amount of serialized data to hold in memory before forcing a write to S3. "
      + "It is important to note that this value is per partition and having too many or too large "
      + "buffers will lead to OOMs. Also, S3 requires multi-part files have parts of more than 5mb. "
      + "When using compression and NOT compressing buffers you must take compression ratio into "
      + "account. Meaning that when compressed the contents of your buffer must be more than 5mb or "
      + "the put to S3 will fail. Set this value accordingly high to account for compression.")
  @JsonSchemaDefault("6291456")
  @Min(5 * 1024 * 1024)
  @JsonProperty(required = false)
  private Integer maxBufferSize = 6 * 1024 * 1024;

  public String getBasePath() {
    return basePath;
  }

  public String getBucketName() {
    return bucketName;
  }

  public Boolean getUseCompression() {
    return useCompression;
  }

  public Integer getMaxBufferSize() {
    return this.maxBufferSize;
  }

  public Boolean getCompressBuffer() {
    return this.compressBuffer;
  }

  public void setBasePath(String basePath) {
    this.basePath = basePath;
  }

  public void setBucketName(String bucketName) {
    this.bucketName = bucketName;
  }

  public void setUseCompression(Boolean useCompression) {
    this.useCompression = useCompression;
  }

  public void setMaxBufferSize(Integer maxBufferSize) {
    this.maxBufferSize = maxBufferSize;
  }

  public void setCompressBuffer(Boolean compressBuffer) {
    this.compressBuffer = compressBuffer;
  }

  @Override
  public Class<S3TransportFactory> getFactoryClass() {
    return S3TransportFactory.class;
  }
}
