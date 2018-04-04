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

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import com.amazonaws.services.lambda.runtime.events.KinesisEvent.KinesisEventRecord;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.InternalEventIterator;
import com.nextdoor.bender.LambdaContext;

/**
 * Wraps KinesisEventRecords with an iterator that constructs {@link KinesisInternalEvent}s.
 */
public class KinesisEventIterator implements InternalEventIterator<InternalEvent> {
  private final Iterator<KinesisEventRecord> iterator;
  private final LambdaContext context;
  private String shardId = null;

  public KinesisEventIterator(LambdaContext context, List<KinesisEventRecord> records,
      Boolean addShardidToPartitions) {
    this.iterator = records.iterator();
    this.context = context;

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
    return new KinesisInternalEvent(this.iterator.next(), this.context, this.shardId);
  }

  @Override
  public void close() throws IOException {

  }
}
