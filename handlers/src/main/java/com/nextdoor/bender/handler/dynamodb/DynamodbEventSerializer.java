package com.nextdoor.bender.handler.dynamodb;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.StreamRecord;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent.DynamodbStreamRecord;
import com.google.gson.stream.JsonWriter;

public class DynamodbEventSerializer {

    private JsonWriter writer;

    DynamodbEventSerializer(JsonWriter jw) {
        writer = jw;
    }

    void serialize(DynamodbStreamRecord record) throws IOException {
        writer.beginObject();

        writeProperty("eventID", record.getEventID());
        writeProperty("eventName", record.getEventName());
        writeProperty("eventVersion", record.getEventVersion());

        writeRecord(record.getDynamodb());

        writer.endObject();
    }

    void serialize(Map<String, AttributeValue> map) throws IOException {
        writeProperty(null, map);
    }

    void writeRecord(StreamRecord record) throws IOException {
        writeName("dynamodb");
        writer.beginObject();

        writeProperty(
                "ApproximateCreationDateTime",
                record.getApproximateCreationDateTime().getTime());
        writeProperty("SequenceNumber", record.getSequenceNumber());
        writeProperty("Keys", record.getKeys());
        writeProperty("OldImage", record.getOldImage());
        writeProperty("NewImage", record.getNewImage());

        writer.endObject();
    }

    void writeProperty(String name, Map<String, AttributeValue> map) throws IOException {
        if (map != null) {
            writeName(name);

            writer.beginObject();
            for (Map.Entry<String, AttributeValue> entry : map.entrySet()) {
                writeProperty(entry.getKey(), entry.getValue());
            }
            writer.endObject();
        }
    }

    void writeProperty(String name, AttributeValue value) throws IOException {
        if (value.getBOOL() != null)
            writeProperty(name, value.getBOOL());
        else if (value.getNULL() != null)
            writeProperty(name, value.getNULL());
        else if (value.getN() != null)
            writeProperty(name, value.getN());
        else if (value.getS() != null)
            writeProperty(name, value.getS());
        else if (value.getB() != null) {
            writeProperty(name, value.getB());
        }
        else if (value.getSS() != null) {
            writer.name(name);
            writer.beginArray();
            for (String s : value.getSS())
                writer.value(s);
            writer.endArray();
        }
        else if (value.getNS() != null) {
            writer.name(name);
            writer.beginArray();
            for (String s : value.getNS())
                writer.value(s);
            writer.endArray();
        }
        else if (value.getBS() != null) {
            writer.name(name);
            writer.beginArray();
            for (ByteBuffer b : value.getBS())
                writeProperty(null, b);
            writer.endArray();
        }
        else if (value.getL() != null) {
            writer.name(name);
            writer.beginArray();
            for (AttributeValue v : value.getL())
                writeProperty(null, v);
            writer.endArray();
        }
        else if (value.getM() != null) {
            writeProperty(name, value.getM());
        }
    }

    void writeProperty(String name, String value) throws IOException {
        writeName(name);
        writer.value(value);
    }

    void writeProperty(String name, Boolean value) throws IOException {
        writeName(name);
        writer.value(value);
    }

    void writeProperty(String name, long value) throws IOException {
        writeName(name);
        writer.value(value);
    }

    void writeProperty(String name, ByteBuffer value) throws IOException {
        writeName(name);
        if (value != null) {
            writer.beginArray();
            for (byte b : value.array())
                writer.value(b);
            writer.endArray();
        }
    }
    
    void writeName(String name) throws IOException {
        if (name != null)
            writer.name(name);
    }
}
