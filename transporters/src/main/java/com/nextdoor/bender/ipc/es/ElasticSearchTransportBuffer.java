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

package com.nextdoor.bender.ipc.es;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.log4j.Logger;

import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.ipc.TransportBuffer;

public class ElasticSearchTransportBuffer implements TransportBuffer {
  private static final Logger logger = Logger.getLogger(ElasticSearchTransportBuffer.class);
  private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
  private final OutputStream os;
  private final long maxBatchSize;
  private final ElasticSearchTransportSerializer serializer;
  private long batchSize = 0;

  public ElasticSearchTransportBuffer(long maxBatchSize, boolean useCompression,
      ElasticSearchTransportSerializer serializer) throws IOException {
    this.maxBatchSize = maxBatchSize;
    this.serializer = serializer;

    if (useCompression) {
      os = new GZIPOutputStream(baos);
    } else {
      os = baos;
    }
  }

  @Override
  public boolean add(InternalEvent ievent) throws IllegalStateException, IOException {
    if (batchSize >= maxBatchSize) {
      throw new IllegalStateException("max batch size was hit");
    }

    os.write(serializer.serialize(ievent));

    batchSize++;
    return true;
  }

  @Override
  public ByteArrayOutputStream getInternalBuffer() {
    return baos;
  }

  @Override
  public boolean isEmpty() {
    return batchSize == 0;
  }

  @Override
  public void close() {
    try {
      os.flush();
    } catch (IOException e) {
      logger.warn("unable to flush os");
    }

    try {
      os.close();
    } catch (IOException e) {
      logger.warn("unable to close os");
    }
  }

  @Override
  public void clear() {
    this.batchSize = 0;
    this.baos.reset();
  }
}
