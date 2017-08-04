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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.amazonaws.services.kinesisfirehose.model.Record;
import com.nextdoor.bender.InternalEvent;

public class FirehoseTransportBufferSimple extends FirehoseTransportBuffer {
  private static final Logger logger = Logger.getLogger(FirehoseTransportBufferSimple.class);
  public static int MAX_RECORDS = 500;

  private ArrayList<Record> dataRecords = new ArrayList<Record>(MAX_RECORDS);

  private FirehoseTransportSerializer serializer;

  public FirehoseTransportBufferSimple(FirehoseTransportSerializer serializer) {
    this.serializer = serializer;
  }

  @Override
  public boolean add(InternalEvent ievent) throws IllegalStateException, IOException {
    if (dataRecords.size() == MAX_RECORDS) {
      logger.trace("hit record index max");
      throw new IllegalStateException("reached max payload size");
    }

    byte[] record = this.serializer.serialize(ievent);
    ByteBuffer data = ByteBuffer.wrap(record);
    dataRecords.add(new Record().withData(data));

    return true;
  }

  @Override
  public ArrayList<Record> getInternalBuffer() {
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
  }
}
