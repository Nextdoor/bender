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
import com.amazonaws.services.lambda.runtime.events.KinesisEvent.KinesisEventRecord;
import com.nextdoor.bender.InternalEvent;

public class KinesisInternalEvent extends InternalEvent {
  private KinesisEventRecord record;

  public KinesisInternalEvent(KinesisEventRecord record, Context context) {
    super(new String(record.getKinesis().getData().array()), context,
        record.getKinesis().getApproximateArrivalTimestamp().getTime());
    this.record = record;
  }

  public KinesisInternalEvent(String record, long timestamp) {
    super(record, null, timestamp);
  }

  public KinesisEventRecord getRecord() {
    return record;
  }
}
