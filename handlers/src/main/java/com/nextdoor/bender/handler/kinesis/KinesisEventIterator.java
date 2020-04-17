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

package com.nextdoor.bender.handler.kinesis;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPInputStream;

import com.amazonaws.services.lambda.runtime.events.KinesisEvent.KinesisEventRecord;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.InternalEventIterator;
import com.nextdoor.bender.LambdaContext;
import com.nextdoor.bender.handler.KinesisIteratorException;
import org.apache.avro.util.ByteBufferInputStream;
import org.apache.commons.io.IOUtils;

/**
 * Wraps KinesisEventRecords with an iterator that constructs {@link KinesisInternalEvent}s.
 */
public class KinesisEventIterator implements InternalEventIterator<InternalEvent> {
  private final Iterator<KinesisEventRecord> iterator;
  private final LambdaContext context;
  private String shardId = null;
  private final boolean decompress;
  private ByteArrayOutputStream byteArrayOutputStream;
  private final int bufferSize;

  public KinesisEventIterator(LambdaContext context,
                              List<KinesisEventRecord> records,
                              Boolean addShardidToPartitions,
                              Boolean decompress,
                              int bufferSize) {
    this.iterator = records.iterator();
    this.context = context;
    this.decompress = decompress;
    this.byteArrayOutputStream = new ByteArrayOutputStream();
    this.bufferSize = bufferSize;

    /*
     * All events in a batch will come from the same shard. So we only need to query this once.
     * 
     * eventid = shardId:sequenceId
     */
    if (addShardidToPartitions) {
      this.shardId = records.get(0).getEventID().split(":")[0];
    }
  }

  @Override
  public boolean hasNext() {
    return this.iterator.hasNext();
  }

  @Override
  public InternalEvent next() {
    KinesisEventRecord nextRecord = this.iterator.next();
    if (decompress) {
      ByteBuffer gzip = nextRecord.getKinesis().getData();
      ByteBufferInputStream byteBufferInputStream = new ByteBufferInputStream(Collections.singletonList(gzip));
      try (GZIPInputStream gzipInputStream = new GZIPInputStream(byteBufferInputStream)) {
        IOUtils.copy(gzipInputStream, byteArrayOutputStream, bufferSize);
        nextRecord.getKinesis().setData(ByteBuffer.wrap(byteArrayOutputStream.toByteArray()));
      } catch (IOException e) {
        throw new KinesisIteratorException("Kinesis iterator was not able to expand the data gzip successfully.", e);
      } finally {
        byteArrayOutputStream.reset(); //clears output so it can be used again later
      }
    }
    return new KinesisInternalEvent(nextRecord, this.context, this.shardId);
  }

  @Override
  public void close() throws IOException {

  }
}
