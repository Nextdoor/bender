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

import java.util.LinkedHashMap;

import com.amazonaws.services.lambda.runtime.events.KinesisEvent.KinesisEventRecord;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.LambdaContext;

public class KinesisInternalEvent extends InternalEvent {
  public static final String SHARD_ID = "__shardid__";
  private KinesisEventRecord record;
  private String shardId;

  public KinesisInternalEvent(KinesisEventRecord record, LambdaContext context, String shardId) {
    super(new String(record.getKinesis().getData().array()), context,
        record.getKinesis().getApproximateArrivalTimestamp().getTime());

    super.addMetadata("eventSource", record.getEventSource());
    super.addMetadata("eventSourceArn", record.getEventSourceARN());
    super.addMetadata("eventID", record.getEventID());
    super.addMetadata("awsRegion", record.getAwsRegion());
    super.addMetadata("partitionKey", record.getKinesis().getPartitionKey());
    super.addMetadata("sequenceNumber", record.getKinesis().getSequenceNumber());

    this.record = record;
    this.shardId = shardId;
  }

  public KinesisInternalEvent(String record, long timestamp) {
    super(record, null, timestamp);
  }

  public KinesisEventRecord getRecord() {
    return record;
  }

  @Override
  public LinkedHashMap<String, String> getPartitions() {
    LinkedHashMap<String, String> partitions = super.getPartitions();
    if (partitions == null) {
      partitions = new LinkedHashMap<>(1);
      super.setPartitions(partitions);
    }

    if (this.shardId != null) {
      partitions.put(SHARD_ID, shardId);
    }
    return partitions;
  }
}
