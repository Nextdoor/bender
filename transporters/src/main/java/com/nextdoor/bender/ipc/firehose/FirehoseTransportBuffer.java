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

package com.nextdoor.bender.ipc.firehose;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.apache.commons.io.output.CountingOutputStream;
import org.apache.log4j.Logger;

import com.amazonaws.services.kinesisfirehose.model.Record;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.ipc.TransportBuffer;

/**
 * A buffer that batches serialized events into four 1000kb {@link Record}s. This is done because
 * AWS rounds each {@link Record} to the nearest 5kb and it is more cost efficient to have multiple
 * serialized records per {@link Record}. Since records are \n separated it does not matter if they
 * are batched up. The limit of a single put to Firehose is 4000kb.
 */
public class FirehoseTransportBuffer implements TransportBuffer {
  private static final Logger logger = Logger.getLogger(FirehoseTransportBuffer.class);
  public static int MAX_RECORDS = 4;
  public static int MAX_RECORD_SIZE = 1000 * 1000; // 1000kb

  private ArrayList<Record> dataRecords = new ArrayList<Record>(MAX_RECORDS);

  private ByteArrayOutputStream baos = new ByteArrayOutputStream();
  private CountingOutputStream cos = new CountingOutputStream(baos);
  private FirehoseTransportSerializer serializer;

  public FirehoseTransportBuffer(FirehoseTransportSerializer serializer) {
    this.serializer = serializer;
  }

  @Override
  public boolean add(InternalEvent ievent) throws IllegalStateException, IOException {
    byte[] record = serializer.serialize(ievent);

    /*
     * Write record if there's room in buffer
     */
    if (dataRecords.size() == MAX_RECORDS) {
      logger.debug("hit record index max");
      throw new IllegalStateException("reached max payload size");
    } else {
      if (cos.getByteCount() + record.length < MAX_RECORD_SIZE) {
        cos.write(record);
        return true;
      }

      /*
       * If current record is full then flush buffer to a Firehose Record and create a new buffer
       */
      logger.debug("creating new datarecord");
      ByteBuffer data = ByteBuffer.wrap(baos.toByteArray());
      this.dataRecords.add(new Record().withData(data));
      baos.reset();
      cos.resetByteCount();
      cos.resetCount();

      /*
       * If we hit the max number of Firehose Records (4) then notify IPC service that this buffer
       * needs to be sent.
       */
      if (dataRecords.size() == MAX_RECORDS) {
        logger.debug("hit record index max");
        throw new IllegalStateException("reached max payload size");
      }

      /*
       * Otherwise write the record to the empty internal buffer
       */
      cos.write(record);
    }

    return true;
  }

  @Override
  public ArrayList<Record> getInternalBuffer() {
    return this.dataRecords;
  }

  @Override
  public boolean isEmpty() {
    return this.cos.getByteCount() == 0 && this.dataRecords.isEmpty();
  }

  @Override
  public void close() {
    if (this.cos.getByteCount() != 0 && this.dataRecords.size() < MAX_RECORDS) {
      logger.debug("flushing remainder of buffer");
      ByteBuffer data = ByteBuffer.wrap(baos.toByteArray());
      this.dataRecords.add(new Record().withData(data));
    }

    try {
      this.baos.close();
    } catch (IOException e) {
    }
  }

  @Override
  public void clear() {
    this.dataRecords.clear();
    this.baos.reset();
  }
}
