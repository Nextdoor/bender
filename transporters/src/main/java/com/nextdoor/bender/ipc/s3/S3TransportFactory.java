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

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AbortMultipartUploadRequest;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.nextdoor.bender.config.AbstractConfig;
import com.nextdoor.bender.ipc.Transport;
import com.nextdoor.bender.ipc.TransportException;
import com.nextdoor.bender.ipc.TransportFactory;
import com.nextdoor.bender.ipc.TransportFactoryInitException;

/**
 * Creates a {@link S3Transport} from a {@link S3TransportConfig}.
 */
public class S3TransportFactory implements TransportFactory {
  private static final Logger logger = Logger.getLogger(S3TransportFactory.class);
  private S3TransportConfig config;

  private Map<String, MultiPartUpload> pendingMultiPartUploads =
      new HashMap<String, MultiPartUpload>();
  private S3TransportSerializer serializer = new S3TransportSerializer();

  @Override
  public Class<S3Transport> getChildClass() {
    return S3Transport.class;
  }

  @Override
  public Transport newInstance() throws TransportFactoryInitException {
    return new S3Transport(new AmazonS3Client(), this.config.getBucketName(),
        this.config.getBasePath(), this.config.getUseCompression(), this.pendingMultiPartUploads);
  }

  @Override
  public void close() {
    AmazonS3Client client = new AmazonS3Client();

    Exception e = null;
    for (MultiPartUpload upload : this.pendingMultiPartUploads.values()) {

      if (e == null) {
        CompleteMultipartUploadRequest req = upload.getCompleteMultipartUploadRequest();
        try {
          client.completeMultipartUpload(req);
        } catch (AmazonS3Exception e1) {
          logger.error("failed to complete multi-part upload for " + upload.getKey() + " parts "
              + upload.getPartCount(), e1);
          e = e1;
        }
      } else {
        logger.warn("aborting upload for: " + upload.getKey());
        AbortMultipartUploadRequest req = upload.getAbortMultipartUploadRequest();
        try {
          client.abortMultipartUpload(req);
        } catch (AmazonS3Exception e1) {
          logger.error("failed to abort multi-part upload", e1);
        }
      }
    }
    this.pendingMultiPartUploads.clear();

    client.shutdown();

    if (e != null) {
      throw new RuntimeException("failed while closing transport", e);
    }
  }

  @Override
  public S3TransportBuffer newTransportBuffer() throws TransportException {
    return new S3TransportBuffer(this.config.getMaxBufferSize(),
        this.config.getUseCompression() && this.config.getCompressBuffer(), this.serializer);
  }

  @Override
  public int getMaxThreads() {
    return this.config.getThreads();
  }

  @Override
  public void setConf(AbstractConfig config) {
    this.config = (S3TransportConfig) config;
  }
}
