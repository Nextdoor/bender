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

package com.nextdoor.bender.wrapper.kinesis;

import com.amazonaws.services.kinesis.model.Record;
import com.amazonaws.services.lambda.runtime.events.KinesisEvent.KinesisEventRecord;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.handler.kinesis.KinesisInternalEvent;
import com.nextdoor.bender.wrapper.Wrapper;

/**
 * Wrapper that wraps the deserailized payload with information about the Kinesis event in which the
 * payload arrived in.
 */
public class KinesisWrapper implements Wrapper {
  private String partitionKey;
  private String sequenceNumber;
  private String sourceArn;
  private String eventSource;
  private String functionName;
  private String functionVersion;
  private long processingTime;
  private long arrivalTime;
  private long processingDelay;
  private long timestamp;
  private Object payload;

  public KinesisWrapper() {}

  private KinesisWrapper(final InternalEvent internal) {
    KinesisEventRecord eventRecord = ((KinesisInternalEvent) internal).getRecord();
    Record record = eventRecord.getKinesis();

    this.partitionKey = record.getPartitionKey();
    this.sequenceNumber = record.getSequenceNumber();
    this.eventSource = eventRecord.getEventSource();
    this.sourceArn = eventRecord.getEventSourceARN();
    this.functionName = internal.getCtx().getFunctionName();
    this.functionVersion = internal.getCtx().getFunctionVersion();
    this.processingTime = System.currentTimeMillis();
    this.arrivalTime = record.getApproximateArrivalTimestamp().getTime();
    this.timestamp = internal.getEventTime();
    this.processingDelay = processingTime - timestamp;

    if (internal.getEventObj() != null) {
      this.payload = internal.getEventObj().getPayload();
    } else {
      this.payload = null;
    }
  }

  public String getPartitionKey() {
    return partitionKey;
  }

  public String getSequenceNumber() {
    return sequenceNumber;
  }

  public String getSourceArn() {
    return sourceArn;
  }

  public String getEventSource() {
    return eventSource;
  }

  public String getFunctionName() {
    return functionName;
  }

  public String getFunctionVersion() {
    return functionVersion;
  }

  public Object getPayload() {
    return payload;
  }

  public KinesisWrapper getWrapped(final InternalEvent internal) {
    return new KinesisWrapper(internal);
  }

  public long getProcessingTime() {
    return processingTime;
  }

  public long getArrivalTime() {
    return arrivalTime;
  }

  public long getProcessingDelay() {
    return processingDelay;
  }

  public long getTimestamp() {
    return timestamp;
  }
}
