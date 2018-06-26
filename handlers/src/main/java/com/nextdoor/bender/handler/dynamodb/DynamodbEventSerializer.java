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
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.internal.InternalUtils;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.StreamRecord;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent.DynamodbStreamRecord;
import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;

public class DynamodbEventSerializer {

    private JsonWriter writer;

    DynamodbEventSerializer(JsonWriter jw) {
        writer = jw;
    }

    static String serialize(DynamodbStreamRecord record) throws IOException {
        StringWriter sw = new StringWriter();
        DynamodbEventSerializer ser = new DynamodbEventSerializer(new JsonWriter(sw));
        ser.write(record);
        return sw.toString();
    }

    static String serialize(Map<String, AttributeValue> map) throws IOException {
        StringWriter sw = new StringWriter();
        DynamodbEventSerializer ser = new DynamodbEventSerializer(new JsonWriter(sw));
        ser.write(map);
        return sw.toString();
    }

    void write(DynamodbStreamRecord record) throws IOException {
        writer.beginObject();

        writeProperty("eventID", record.getEventID());
        writeProperty("eventName", record.getEventName());
        writeProperty("eventVersion", record.getEventVersion());

        write(record.getDynamodb());

        writer.endObject();
    }

    void write(Map<String, AttributeValue> map) throws IOException {
        writeProperty(null, map);
    }

    void write(StreamRecord record) throws IOException {
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

            Gson gson = new Gson();
            String json = gson.toJson(attributeValueMapToItem(map).asMap());
            writer.jsonValue(json);
        }
    }

    void writeProperty(String name, String value) throws IOException {
        writeName(name);
        writer.value(value);
    }

    void writeProperty(String name, long value) throws IOException {
        writeName(name);
        writer.value(value);
    }

    void writeName(String name) throws IOException {
        if (name != null)
            writer.name(name);
    }

    static Item attributeValueMapToItem(Map<String, AttributeValue> map) {
        ArrayList<Map<String, AttributeValue>> listOfMaps = new ArrayList<>();
        listOfMaps.add(map);
        List<Item> listOfItem = InternalUtils.toItemList(listOfMaps);
        return listOfItem.get(0);
    }
}
