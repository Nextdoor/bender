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
}
