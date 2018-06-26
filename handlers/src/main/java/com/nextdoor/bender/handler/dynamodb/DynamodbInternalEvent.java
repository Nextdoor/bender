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

import java.io.IOException;
import java.util.LinkedHashMap;

import com.amazonaws.services.lambda.runtime.events.DynamodbEvent.DynamodbStreamRecord;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.LambdaContext;

public class DynamodbInternalEvent extends InternalEvent {
    public static final String DYNAMODB_KEYS = "__keys__";
    private DynamodbStreamRecord record;

    public DynamodbInternalEvent(
            DynamodbStreamRecord record, String stringRecord, LambdaContext context) {
        super(stringRecord, context,
                record.getDynamodb().getApproximateCreationDateTime().getTime());

        super.addMetadata("eventName", record.getEventName());
        super.addMetadata("eventSource", record.getEventSource());
        super.addMetadata("eventSourceArn", record.getEventSourceARN());
        super.addMetadata("eventID", record.getEventID());
        super.addMetadata("awsRegion", record.getAwsRegion());
        super.addMetadata("sequenceNumber", record.getDynamodb().getSequenceNumber());

        this.record = record;
    }

    public DynamodbInternalEvent(String record, long timestamp) {
        super(record, null, timestamp);
    }

    public DynamodbStreamRecord getRecord() {
        return record;
    }

    @Override
    public LinkedHashMap<String, String> getPartitions() {
        LinkedHashMap<String, String> partitions = super.getPartitions();
        if (partitions == null) {
            partitions = new LinkedHashMap<String, String>(1);
            super.setPartitions(partitions);
        }

        try {
            partitions.put(DYNAMODB_KEYS, serializeKeys(record));
        }
        catch (IOException e) {

        }

        return partitions;
    }

    private static String serializeKeys(DynamodbStreamRecord record) throws IOException {
        return DynamodbEventSerializer.serialize(record.getDynamodb().getKeys());
    }
}
