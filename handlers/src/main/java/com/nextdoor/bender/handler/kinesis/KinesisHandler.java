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


import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.KinesisEvent;
import com.amazonaws.services.lambda.runtime.events.KinesisEvent.KinesisEventRecord;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.InternalEventIterator;
import com.nextdoor.bender.config.Source;
import com.nextdoor.bender.handler.BaseHandler;
import com.nextdoor.bender.handler.Handler;
import com.nextdoor.bender.handler.HandlerException;
import com.nextdoor.bender.utils.SourceUtils;

public class KinesisHandler extends BaseHandler<KinesisEvent> implements Handler<KinesisEvent> {
  private InternalEventIterator<InternalEvent> recordIterator = null;
  private Source source = null;
  private KinesisEvent event = null;

  public void handler(KinesisEvent event, Context context) throws HandlerException {
    if (!initialized) {
      init(context);
    }

    /**
     * Expose the KinesisEvent to the overall class so that when getInternalMetadata() is called,
     * it has access to some of the data and can populate things properly.
     */
    this.event = event;

    KinesisHandlerConfig handlerConfig = (KinesisHandlerConfig) this.config.getHandlerConfig();
    this.recordIterator = new KinesisEventIterator(context, event.getRecords(),
        handlerConfig.getAddShardIdToPartitions());

    /*
     * Get processors based on the source stream ARN
     */
    KinesisEventRecord firstRecord = event.getRecords().get(0);
    this.source = SourceUtils.getSource(firstRecord.getEventSourceARN(), sources);

    super.process(context);
  }

  @Override
  public Source getSource() {
    return this.source;
  }

  @Override
  public String getSourceName() {
    return "aws:kinesis";
  }

  @Override
  public void onException(Exception e) {
    /*
     * No special handling needed as state is not kept.
     */
  }

  @Override
  public InternalEventIterator<InternalEvent> getInternalEventIterator() {
    return this.recordIterator;
  }

  @Override
  public void prepareMetadata() {
    /**
     * Instantiate the HandlerMetadata object
     */
    super.prepareMetadata();

    /**
     * Access this.event, which is exposed to us during the run of handler() above. Use the first
     * record from the event to populate some metadata about the run.
     */
    KinesisEventRecord firstRecord = event.getRecords().get(0);
    this.source = SourceUtils.getSource(firstRecord.getEventSourceARN(), sources);

    metadata.setField("partitionKey", firstRecord.getKinesis().getPartitionKey());
    metadata.setField("sequenceNumber", firstRecord.getKinesis().getSequenceNumber());
    metadata.setField("arrivalTime", firstRecord.getKinesis()
        .getApproximateArrivalTimestamp());
    metadata.setField("eventSource", firstRecord.getEventSource());
    metadata.setField("sourceArn", firstRecord.getEventSourceARN());
    //this.timestamp = internal.getEventTime();
    //this.processingDelay = processingTime - timestamp;
  }
}
