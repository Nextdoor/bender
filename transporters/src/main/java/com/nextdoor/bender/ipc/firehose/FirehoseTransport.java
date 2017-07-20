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

import com.amazonaws.ClientConfiguration;
import com.amazonaws.services.kinesisfirehose.AmazonKinesisFirehoseClient;
import com.amazonaws.services.kinesisfirehose.model.PutRecordBatchRequest;
import com.nextdoor.bender.ipc.TransportBuffer;
import com.nextdoor.bender.ipc.UnpartitionedTransport;

/**
 * Writes events to AWS Firehose.
 */
public class FirehoseTransport implements UnpartitionedTransport {
  private final String deliveryStreamName;
  private AmazonKinesisFirehoseClient client =
      new AmazonKinesisFirehoseClient(new ClientConfiguration().withGzip(true));

  protected FirehoseTransport(String deliveryStreamName) {
    this.deliveryStreamName = deliveryStreamName;
  }

  public void sendBatch(TransportBuffer buffer) {
    FirehoseTransportBuffer tb = (FirehoseTransportBuffer) buffer;

    /*
     * Create batch put request with given records
     */
    PutRecordBatchRequest batch = new PutRecordBatchRequest()
        .withDeliveryStreamName(this.deliveryStreamName).withRecords(tb.getInternalBuffer());

    /*
     * Put recored
     */
    client.putRecordBatch(batch);
  }
}
