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

package com.nextdoor.bender.handler.dynamodb;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import com.amazonaws.services.lambda.runtime.events.DynamodbEvent.DynamodbStreamRecord;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.InternalEventIterator;
import com.nextdoor.bender.LambdaContext;

/**
 * Wraps DynamodbStreamRecords with an iterator that constructs {@link DynamodbInternalEvent}s.
 */
public class DynamodbEventIterator implements InternalEventIterator<InternalEvent> {
    private final Iterator<DynamodbStreamRecord> iterator;
    private final LambdaContext context;

    public DynamodbEventIterator(LambdaContext context, List<DynamodbStreamRecord> records) {
        this.iterator = records.iterator();
        this.context = context;
    }

    @Override
    public boolean hasNext() {
        return this.iterator.hasNext();
    }

    @Override
    public InternalEvent next() {
        try {
            return new DynamodbInternalEvent(this.iterator.next(), this.context);
        }
        catch (IOException e) {
            return null;
        }
    }

    @Override
    public void close() throws IOException {

    }
}
