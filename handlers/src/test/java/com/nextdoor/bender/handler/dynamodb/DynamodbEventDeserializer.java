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
                default:
                    String first = propertyName.substring(0, 1);
                    String rest = propertyName.substring(1);
                    return first.toUpperCase() + rest;
            }
        }
    }

    public static DynamodbEvent deserialize(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        mapper.addMixIn(com.amazonaws.services.dynamodbv2.model.Record.class, RecordIgnoreDuplicateMethods.class);
        mapper.addMixIn(com.amazonaws.services.dynamodbv2.model.StreamRecord.class, StreamRecordIgnoreDuplicateMethods.class);
        mapper.setPropertyNamingStrategy(new PropertyNamingFix());

        DynamodbEvent event = mapper.readValue(json, DynamodbEvent.class);
        return event;
    }
}
