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

import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.model.PutRecordsRequest;
import com.amazonaws.services.kinesis.model.PutRecordsResult;
import com.nextdoor.bender.ipc.TransportBuffer;
import com.nextdoor.bender.ipc.TransportException;
import com.nextdoor.bender.ipc.UnpartitionedTransport;

/**
 * Writes events to AWS Kinesis.
 */
public class KinesisTransport implements UnpartitionedTransport {
  private final String streamName;
  private final AmazonKinesis client;

  protected KinesisTransport(AmazonKinesis client, String streamName) {
    this.client = client;
    this.streamName = streamName;
  }

  public void sendBatch(TransportBuffer buffer) throws TransportException {
    KinesisTransportBuffer tb = (KinesisTransportBuffer) buffer;
    /*
     * Create batch put request with given records
     */
    PutRecordsRequest batch =
        new PutRecordsRequest().withRecords(tb.getInternalBuffer()).withStreamName(this.streamName);

    /*
     * Put records
     */
    PutRecordsResult res;
    try {
      res = this.client.putRecords(batch);
    } catch (Exception e) {
      /*
       * putRecords throws a lot of different unchecked exceptions. Just catch everything
       * and wrap it.
       */
      throw new TransportException(e);
    }

    if (res.getFailedRecordCount() != 0) {
      throw new TransportException("had " + res.getFailedRecordCount() + " record failures");
    }
  }
}
