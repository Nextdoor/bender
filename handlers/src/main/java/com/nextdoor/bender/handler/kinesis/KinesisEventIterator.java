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

package com.nextdoor.bender.handler.kinesis;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.KinesisEvent.KinesisEventRecord;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.InternalEventIterator;

/**
 * Wraps KinesisEventRecords with an iterator that constructs {@link KinesisInternalEvent}s.
 */
public class KinesisEventIterator implements InternalEventIterator<InternalEvent> {
  private final Iterator<KinesisEventRecord> iterator;
  private final Context context;

  public KinesisEventIterator(Context context, List<KinesisEventRecord> records) {
    this.iterator = records.iterator();
    this.context = context;
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }

  @Override
  public InternalEvent next() {
    return new KinesisInternalEvent(iterator.next(), context);
  }

  @Override
  public void close() throws IOException {

  }
}
