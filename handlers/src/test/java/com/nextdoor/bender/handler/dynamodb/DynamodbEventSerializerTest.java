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

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;

public class DynamodbEventSerializerTest {

    static String RECORD_FORMAT =
            "{" +
            "  \"Records\":[" +
            "    {" +
            "      \"eventID\":\"1\"," +
            "      \"eventName\":\"INSERT\"," +
            "      \"eventVersion\":\"1.0\"," +
            "      \"eventSource\":\"aws:dynamodb\"," +
            "      \"awsRegion\":\"us-east-1\"," +
            "      \"dynamodb\":{" +
            "        \"ApproximateCreationDateTime\": 1479499740," +
            "        %s," +
            "        \"SequenceNumber\":\"111\"," +
            "        \"StreamViewType\":\"NEW_AND_OLD_IMAGES\"" +
            "      }," +
            "      \"eventSourceARN\":\"stream-ARN\"" +
            "    }" +
            "  ]" +
            "}";

    static String EXPECTED_FORMAT = "{\"eventSourceARN\":\"stream-ARN\",\"eventID\":\"1\",\"eventName\":\"INSERT\",\"eventVersion\":\"1.0\",\"eventSource\":\"aws:dynamodb\",\"awsRegion\":\"us-east-1\",\"dynamodb\":{\"approximateCreationDateTime\":1479499740,%s,\"sequenceNumber\":\"111\",\"streamViewType\":\"NEW_AND_OLD_IMAGES\"}}";

    @Test
    public void testRecord() throws IOException {
        String input =
                "{" +
                "  \"Records\":[" +
                "    {" +
                "      \"eventID\":\"1\"," +
                "      \"eventName\":\"INSERT\"," +
                "      \"eventVersion\":\"1.0\"," +
                "      \"eventSource\":\"aws:dynamodb\"," +
                "      \"awsRegion\":\"us-east-1\"," +
                "      \"dynamodb\":{" +
                "        \"ApproximateCreationDateTime\": 1479499740," +
                "        \"SequenceNumber\":\"111\"" +
                "      }," +
                "      \"eventSourceARN\":\"stream-ARN\"" +
                "    }" +
                "  ]" +
                "}";
        DynamodbEvent event = DynamodbEventDeserializer.deserialize(input);
        String result = new DynamodbEventSerializer().serialize(event.getRecords().get(0));

        String expected = "{\"eventSourceARN\":\"stream-ARN\",\"eventID\":\"1\",\"eventName\":\"INSERT\",\"eventVersion\":\"1.0\",\"eventSource\":\"aws:dynamodb\",\"awsRegion\":\"us-east-1\",\"dynamodb\":{\"approximateCreationDateTime\":1479499740,\"sequenceNumber\":\"111\"}}";
        assertEquals(expected, result);
    }

    @Test
    public void testKeys() throws IOException {
        String input = String.format(RECORD_FORMAT,
                "\"Keys\":{" +
                "  \"Type\":{" +
                "    \"S\":\"content\"" +
                "  }," +
                "  \"Id\":{" +
                "    \"N\":\"101\"" +
                "  }" +
                "}");
        DynamodbEvent event = DynamodbEventDeserializer.deserialize(input);
        String result = new DynamodbEventSerializer().serialize(event.getRecords().get(0));

        String expected = String.format(EXPECTED_FORMAT, "\"keys\":{\"Type\":\"content\",\"Id\":101}");
        assertEquals(expected, result);
    }

    @Test
    public void testNewImage() throws IOException {
        String input = String.format(RECORD_FORMAT,
                "\"NewImage\":{" +
                "  \"Message\":{" +
                "    \"S\":\"New item!\"" +
                "  }," +
                "  \"Bytes\":{" +
                "    \"B\":\"aGVsbG8=\"" +
                "  }," +
                "  \"IsSold\":{" +
                "    \"BOOL\":1" +
                "  }," +
                "  \"Id\":{" +
                "    \"N\":\"101\"" +
                "  }," +
                "  \"Tags\":{" +
                "    \"SS\":[\"message\",\"sold\"]" +
                "  }," +
                "  \"Metrics\":{" +
                "    \"NS\":[\"42.2\",\"3.14\"]" +
                "  }," +
                "  \"Blobs\":{" +
                "    \"BS\":[\"Zm9v\",\"YmFy\"]" +
                "  }" +
                "}");
        DynamodbEvent event = DynamodbEventDeserializer.deserialize(input);
        String result = new DynamodbEventSerializer().serialize(event.getRecords().get(0));

        String expected = String.format(EXPECTED_FORMAT, "\"newImage\":{\"Message\":\"New item!\",\"Bytes\":[104,101,108,108,111],\"IsSold\":true,\"Id\":101,\"Tags\":[\"message\",\"sold\"],\"Metrics\":[42.2,3.14],\"Blobs\":[[102,111,111],[98,97,114]]}");
        assertEquals(expected, result);
    }

    @Test
    public void testOldImage() throws IOException {
        String input = String.format(RECORD_FORMAT,
                "\"OldImage\":{" +
                "  \"Message\":{" +
                "    \"S\":\"New item!\"" +
                "  }," +
                "  \"Id\":{" +
                "    \"N\":\"101\"" +
                "  }" +
                "}");
        DynamodbEvent event = DynamodbEventDeserializer.deserialize(input);
        String result = new DynamodbEventSerializer().serialize(event.getRecords().get(0));

        String expected = String.format(EXPECTED_FORMAT, "\"oldImage\":{\"Message\":\"New item!\",\"Id\":101}");
        assertEquals(expected, result);
    }
}
