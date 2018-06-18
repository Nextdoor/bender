package com.nextdoor.bender.handler.dynamodb;

import java.io.InputStreamReader;

import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent.DynamodbStreamRecord;
import com.nextdoor.bender.handler.HandlerTest;
import com.nextdoor.bender.handler.dynamodb.DynamodbHandler;
import com.nextdoor.bender.testutils.TestUtils;

import org.apache.commons.io.IOUtils;

public class DynamodbHandlerTest extends HandlerTest<DynamodbEvent> {

    @Override
    public DynamodbHandler getHandler() {
        return new DynamodbHandler();
    }

    @Override
    public DynamodbEvent getTestEvent() throws Exception {
        String json = IOUtils.toString(new InputStreamReader(
                this.getClass().getResourceAsStream("dynamodb_records.json"),
                "UTF-8"));
        return DynamodbEventDeserializer.deserialize(json);
    }

    @Override
    public String getExpectedEvent() {
        return "dynamodb_output.json";
    }

    @Override
    public void setup() {
    }

    @Override
    public String getConfigFile() {
        return "/com/nextdoor/bender/handler/config_dynamodb.json";
    }

    @Override
    public void teardown() {
    }
}
