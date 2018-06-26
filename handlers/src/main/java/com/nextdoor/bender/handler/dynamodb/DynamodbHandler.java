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

package com.nextdoor.bender.handler.dynamodb;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent.DynamodbStreamRecord;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.InternalEventIterator;
import com.nextdoor.bender.LambdaContext;
import com.nextdoor.bender.config.Source;
import com.nextdoor.bender.handler.BaseHandler;
import com.nextdoor.bender.handler.Handler;
import com.nextdoor.bender.handler.HandlerException;
import com.nextdoor.bender.utils.SourceUtils;

public class DynamodbHandler extends BaseHandler<DynamodbEvent> implements Handler<DynamodbEvent> {
    private InternalEventIterator<InternalEvent> recordIterator = null;
    private Source source = null;

    public void handler(DynamodbEvent event, Context context) throws HandlerException {
        if (!initialized) {
            init(context);
        }

        this.recordIterator = new DynamodbEventIterator(
                new LambdaContext(context), event.getRecords());

        DynamodbStreamRecord firstRecord = event.getRecords().get(0);
        this.source = SourceUtils.getSource(firstRecord.getEventSourceARN(), sources);

        super.process(context);
    }

    @Override
    public Source getSource() {
        return this.source;
    }

    @Override
    public String getSourceName() {
        return "aws:dynamodb";
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
}
