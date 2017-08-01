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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.log4j.Logger;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.UploadPartRequest;
import com.amazonaws.services.s3.model.UploadPartResult;
import com.gc.iotools.stream.is.InputStreamFromOutputStream;
import com.nextdoor.bender.ipc.PartitionedTransport;
import com.nextdoor.bender.ipc.TransportBuffer;
import com.nextdoor.bender.ipc.TransportException;

public class S3Transport implements PartitionedTransport {
  private final AmazonS3Client client;
  private final String bucketName;
  private final String basePath;
  private final boolean compress;
  private final Map<String, MultiPartUpload> multiPartUploads;

  public static final String FILENAME_KEY = "__filename__";
  private static final Logger logger = Logger.getLogger(S3Transport.class);

  public S3Transport(AmazonS3Client client, String bucketName, String basePath, boolean compress,
      Map<String, MultiPartUpload> multiPartUploads) {
    this.client = client;
    this.bucketName = bucketName;
    this.basePath = basePath;
    this.compress = compress;
    this.multiPartUploads = multiPartUploads;
  }

  protected void sendStream(InputStream input, String key, long streamSize)
      throws TransportException {
    /*
     * Create metadata
     */
    ObjectMetadata metadata = new ObjectMetadata();

    /*
     * Find if a multipart upload has already begun or start a new one.
     */
    MultiPartUpload upload;

    synchronized (multiPartUploads) {
      if (!multiPartUploads.containsKey(key)) {
        InitiateMultipartUploadRequest uploadRequest =
            new InitiateMultipartUploadRequest(bucketName, key);
        uploadRequest.setObjectMetadata(metadata);

        InitiateMultipartUploadResult res = client.initiateMultipartUpload(uploadRequest);
        upload = new MultiPartUpload(bucketName, key, res.getUploadId());
        multiPartUploads.put(key, upload);
      } else {
        upload = multiPartUploads.get(key);
      }
    }

    /*
     * Write out to S3. Note that the S3 client auto closes the input stream.
     */
    UploadPartRequest req =
        upload.getUploadPartRequest().withInputStream(input).withPartSize(streamSize);

    try {
      UploadPartResult res = client.uploadPart(req);
      upload.addPartETag(res.getPartETag());
    } catch (AmazonClientException e) {
      client.abortMultipartUpload(upload.getAbortMultipartUploadRequest());
      throw new TransportException("unable to put file" + e, e);
    } finally {
      try {
        input.close();
      } catch (IOException e) {
        logger.warn("error encountered while closing input stream", e);
      }
    }
  }

  protected ByteArrayOutputStream compress(ByteArrayOutputStream raw) throws TransportException {
    ByteArrayOutputStream compressed = new ByteArrayOutputStream();
    BZip2CompressorOutputStream bcos = null;
    try {
      bcos = new BZip2CompressorOutputStream(compressed);
    } catch (IOException e) {
      throw new TransportException("unable to open compressed stream", e);
    }

    try {
      raw.writeTo(bcos);
      bcos.flush();
    } catch (IOException e) {
      throw new TransportException("unable to compress data", e);
    } finally {
      try {
        bcos.close();
      } catch (IOException e) {
      }
    }

    return compressed;
  }

  @Override
  public void sendBatch(TransportBuffer buffer, LinkedHashMap<String, String> partitions)
      throws TransportException {
    S3TransportBuffer buf = (S3TransportBuffer) buffer;

    /*
     * Create s3 key (filepath + filename)
     */
    LinkedHashMap<String, String> parts = new LinkedHashMap<String, String>(partitions);
    String filename = parts.remove(FILENAME_KEY);
    String key = parts.entrySet().stream().map(s -> s.getKey() + "=" + s.getValue())
        .collect(Collectors.joining("/"));

    key = (key.equals("") ? filename : key + '/' + filename);

    if (this.basePath.endsWith("/")) {
      key = this.basePath + key;
    } else {
      key = this.basePath + '/' + key;
    }

    // TODO: make this dynamic
    if (key.endsWith(".gz")) {
      key = key.substring(0, key.length() - 3);
    }

    /*
     * Add or strip out compression format extension
     *
     * TODO: get this based on the compression codec
     */
    if (this.compress || buf.isCompressed()) {
      key += ".bz2";
    }

    ByteArrayOutputStream os = buf.getInternalBuffer();

    /*
     * Compress stream if needed. Don't compress a compressed stream.
     */
    ByteArrayOutputStream payload;
    if (this.compress && !buf.isCompressed()) {
      payload = compress(os);
    } else {
      payload = os;
    }

    /*
     * For memory efficiency convert the output stream into an InputStream. This is done using the
     * easystream library but under the hood it uses piped streams to facilitate this process. This
     * avoids copying the entire contents of the OutputStream to populate the InputStream. Note that
     * this process creates another thread to consume from the InputStream.
     */
    final String s3Key = key;

    /*
     * Write to OutputStream
     */
    final InputStreamFromOutputStream<String> isos = new InputStreamFromOutputStream<String>() {
      public String produce(final OutputStream dataSink) throws Exception {
        /*
         * Note this is executed in a different thread
         */
        payload.writeTo(dataSink);
        return null;
      }
    };

    /*
     * Consume InputStream
     */
    try {
      sendStream(isos, s3Key, payload.size());
    } finally {
      try {
        isos.close();
      } catch (IOException e) {
        throw new TransportException(e);
      } finally {
        buf.close();
      }
    }
  }
}
