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
 * Copyright 2018 Nextdoor.com, Inc
 *
 */

package com.nextdoor.bender.ipc.kinesis;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import org.apache.log4j.Logger;
import com.amazonaws.services.kinesis.model.PutRecordsRequestEntry;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.ipc.TransportBuffer;
import com.nextdoor.bender.ipc.generic.GenericTransportSerializer;

public class KinesisTransportBuffer implements TransportBuffer {
  private static final Logger logger = Logger.getLogger(KinesisTransportBuffer.class);
  public static int MAX_RECORD_SIZE = 1000 * 1000; // 1000kb
  public static int MAX_BUFFER_SIZE = MAX_RECORD_SIZE * 5; // 5000kb

  private final ArrayList<PutRecordsRequestEntry> dataRecords;
  private final int maxRecords;
  private final GenericTransportSerializer serializer;
  private int bufferSizeBytes = 0;

  public KinesisTransportBuffer(GenericTransportSerializer serializer, int maxRecords) {
    this.serializer = serializer;
    this.maxRecords = maxRecords;
    this.dataRecords = new ArrayList<PutRecordsRequestEntry>(maxRecords);
  }

  @Override
  public boolean add(InternalEvent ievent) throws IllegalStateException, IOException {
    if (this.dataRecords.size() >= this.maxRecords) {
      logger.trace("hit record index max");
      throw new IllegalStateException("reached max payload size");
    }

    byte[] record = this.serializer.serialize(ievent);

    /*
     * Restrict size of individual record
     */
    if (record.length > MAX_RECORD_SIZE) {
      throw new IOException(
          "serialized event is " + record.length + " larger than max of " + MAX_RECORD_SIZE);
    }

    /*
     * Restrict overall size
     */
    if (record.length + this.bufferSizeBytes > MAX_BUFFER_SIZE) {
      throw new IllegalStateException("reached max payload size");
    }

    this.bufferSizeBytes += record.length;
    ByteBuffer data = ByteBuffer.wrap(record);
    this.dataRecords.add(
        new PutRecordsRequestEntry().withData(data).withPartitionKey(ievent.getEventSha1Hash()));

    return true;
  }

  @Override
  public ArrayList<PutRecordsRequestEntry> getInternalBuffer() {
    return this.dataRecords;
  }

  @Override
  public boolean isEmpty() {
    return this.dataRecords.isEmpty();
  }

  @Override
  public void close() {

  }

  @Override
  public void clear() {
    this.dataRecords.clear();
    this.bufferSizeBytes = 0;
  }
}
