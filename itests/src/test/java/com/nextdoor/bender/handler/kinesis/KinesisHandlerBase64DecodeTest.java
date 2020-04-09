package com.nextdoor.bender.handler.kinesis;

import com.amazonaws.services.lambda.runtime.events.KinesisEvent;
import com.nextdoor.bender.handler.HandlerTest;
import com.nextdoor.bender.testutils.TestUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class KinesisHandlerBase64DecodeTest extends HandlerTest<KinesisEvent> {

    @Override
    public KinesisHandler getHandler() {
        return new KinesisHandler();
    }

    @Override
    public KinesisEvent getTestEvent() throws Exception {
        KinesisEvent kinesisEvent = TestUtils.createEvent(this.getClass(),
                "basic_input.json",
                "arn:aws:kinesis:us-east-1:2341:stream/test-events-stream");
        String encodedData = "H4sICDZ0i14AA2Jhc2ljX291dHB1dF9raW5lc2lzX2VuY29kZWQgY29weS5qc29uAJ2R3UvDMBTF3/dXXPI8hH5E3N4Ki0Po5mj7IIhI1t7NQNPUJFVk7H836b4QihNf8nDO796Tk+xGAESiMXyLxVeLZApklhTJ64LleTJnZOwB9dmg9lYQRjF7Shar9OjUajvXqmu9abhsa0yPypJLPDO51cjlD+ggXSjTrU2pRWuFau5FbVEbxz87C05TB5k46eW0mH1gYy/grj+dJSofJqqgX95LVrie1i1yTkDvaEgnNIwnt/RMHN/BT+ZFkhWQ4XvnZh6qKemZ/Xg4JbySQoM4GkxREoG3bS1K7nuDa2QApbAWK9hoJcG+IaRcrisOm64pPXXz+2Wi65cZrMyWsz8Xjv+ZkbHV49C7+i8d7b8BCrILk40CAAA=";
        kinesisEvent.getRecords().get(0).getKinesis().setData(ByteBuffer.wrap(encodedData.getBytes()));
        return kinesisEvent;
    }

    @Override
    public void setup() {

    }

    @Override
    public void teardown() {

    }

    @Override
    public String getConfigFile() {
        return "/com/nextdoor/bender/handler/config_kinesis_base64_decode.yaml";
    }

    @Override
    public String getExpectedOutputFile() {
        return "multiple_outputs_list.txt";
    }

    @Override
    public void validateOutput(List<String> output) throws IOException {
        assertEquals(4, output.size());

        /*
         * Load expected
         */
        BufferedReader br = new BufferedReader(new InputStreamReader(
                this.getClass().getResourceAsStream(getExpectedOutputFile()),
                "UTF-8"));
        String event;
        int index = 0;
        while ((event = br.readLine()) != null) {
            assertEquals(event, output.get(index));
            index += 1;
        }

        br.close();
    }
}
