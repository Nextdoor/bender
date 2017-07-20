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

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.io.output.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.output.CountingOutputStream;
import org.apache.log4j.Logger;

import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.ipc.TransportBuffer;
import com.nextdoor.bender.ipc.TransportException;

public class S3TransportBuffer implements TransportBuffer {
  private static final Logger logger = Logger.getLogger(S3TransportBuffer.class);
  private S3TransportSerializer serializer;
  private long maxBytes;
  private CountingOutputStream cos;
  private ByteArrayOutputStream baos;
  private OutputStream os;
  private boolean isCompressed;

  public S3TransportBuffer(long maxBytes, boolean useCompression, S3TransportSerializer serializer)
      throws TransportException {
    this.maxBytes = maxBytes;
    this.serializer = serializer;

    baos = new ByteArrayOutputStream();
    cos = new CountingOutputStream(baos);

    if (useCompression) {
      this.isCompressed = true;
      try {
        os = new BZip2CompressorOutputStream(cos);
      } catch (IOException e) {
        throw new TransportException("unable to create BZip2CompressorOutputStream", e);
      }
    } else {
      this.isCompressed = false;
      os = cos;
    }
  }

  @Override
  public boolean add(InternalEvent ievent) throws IllegalStateException, IOException {
    byte[] payload = serializer.serialize(ievent);
    if (cos.getByteCount() + payload.length > this.maxBytes) {
      throw new IllegalStateException("buffer is full");
    }

    os.write(payload);
    return true;
  }

  @Override
  public ByteArrayOutputStream getInternalBuffer() {
    return this.baos;
  }

  @Override
  public boolean isEmpty() {
    return this.cos.getByteCount() == 0;
  }

  @Override
  public void close() {
    if (this.os != this.cos) {
      try {
        this.os.flush();
      } catch (IOException e) {
        logger.warn("unable to flush os");
      }
      try {
        this.os.close();
      } catch (IOException e) {
        logger.warn("unable to close os");
      }
    }

    try {
      this.cos.flush();
    } catch (IOException e) {
      logger.warn("unable to flush cos");
    }

    try {
      this.cos.close();
    } catch (IOException e) {
      logger.warn("unable to close cos");
    }
  }

  @Override
  public void clear() {
    this.baos.reset();
    this.cos.resetByteCount();
  }

  public boolean isCompressed() {
    return this.isCompressed;
  }
}
