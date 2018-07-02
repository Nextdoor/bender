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

import com.amazonaws.services.dynamodbv2.model.OperationType;
import com.amazonaws.services.dynamodbv2.model.StreamViewType;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import java.io.IOException;

public class DynamodbEventDeserializer {

    private static ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.addMixIn(com.amazonaws.services.dynamodbv2.model.Record.class, RecordIgnoreDuplicateMethods.class);
        mapper.addMixIn(com.amazonaws.services.dynamodbv2.model.StreamRecord.class, StreamRecordIgnoreDuplicateMethods.class);
        mapper.setPropertyNamingStrategy(new PropertyNamingFix());
    }

    interface RecordIgnoreDuplicateMethods {
        @JsonIgnore
        public void setEventName(OperationType eventName);
        @JsonProperty("eventName")
        public void setEventName(String eventName);
    }

    interface StreamRecordIgnoreDuplicateMethods {
        @JsonIgnore
        public void setStreamViewType(StreamViewType streamViewType);
        @JsonProperty("StreamViewType")
        public void setStreamViewType(String streamViewType);
    }

    public static class PropertyNamingFix extends PropertyNamingStrategy.PropertyNamingStrategyBase {
        @Override
        public String translate(String propertyName) {
            switch(propertyName) {
                case "eventID":
                    return "eventID";
                case "eventVersion":
                    return "eventVersion";
                case "eventSource":
                    return "eventSource";
                case "awsRegion":
                    return "awsRegion";
                case "dynamodb":
                    return "dynamodb";
                case "eventSourceARN":
                    return "eventSourceARN";
                case "bool":
                    return "BOOL";
                case "ss":
                    return "SS";
                case "ns":
                    return "NS";
                case "bs":
                    return "BS";
                default:
                    String first = propertyName.substring(0, 1);
                    String rest = propertyName.substring(1);
                    return first.toUpperCase() + rest;
            }
        }
    }

    public static DynamodbEvent deserialize(String json) throws IOException {
        return mapper.readValue(json, DynamodbEvent.class);
    }
}
