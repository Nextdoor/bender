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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.internal.InternalUtils;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent.DynamodbStreamRecord;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class DynamodbEventSerializer {

    /**
     * DateAdapter serializes dates in epoch format.
     */
    static class DateAdapter extends TypeAdapter<Date> {
        public Date read(JsonReader r) throws IOException {
            throw new IOException("Not implemented yet");
        }

        public void write(JsonWriter w, Date d) throws IOException {
            w.value(d.getTime());
        }
    }

    /**
     * AttributeValueMapAdapter serializes DynamoDB JSON into standard JSON format.
     */
    class AttributeValueMapAdapter extends TypeAdapter<Map<String, AttributeValue>> {
        public Map<String, AttributeValue> read(JsonReader r) throws IOException {
            throw new IOException("Not implemented yet");
        }

        public void write(JsonWriter w, Map<String, AttributeValue> map) throws IOException {
            if (map == null) {
                w.nullValue();
            } else {
                // Uses the outer class's gson instance
                String json = gson.toJson(attributeValueMapToItem(map).asMap());
                w.jsonValue(json);
            }
        }

        private Item attributeValueMapToItem(Map<String, AttributeValue> map) {
            ArrayList<Map<String, AttributeValue>> listOfMaps = new ArrayList<>();
            listOfMaps.add(map);
            List<Item> listOfItem = InternalUtils.toItemList(listOfMaps);
            return listOfItem.get(0);
        }
    }

    private Gson gson;

    DynamodbEventSerializer() {
        gson = createGson();
    }

    private Gson createGson() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Date.class, new DateAdapter());
        builder.registerTypeAdapter(
                new TypeToken<Map<String, AttributeValue>>() {}.getType(),
                new AttributeValueMapAdapter());
        return builder.create();
    }

    String serialize(DynamodbStreamRecord record) {
        return gson.toJson(record);
    }

    String serialize(Map<String, AttributeValue> map) {
        return gson.toJson(map);
    }
}
