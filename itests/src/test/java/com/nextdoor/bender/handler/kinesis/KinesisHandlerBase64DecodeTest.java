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
                "arn:aws:kinesis:us-east-1:5678:stream/test-events-stream");
        String encodedData = "H4sIAAAAAAAAAOVW227bRhD9FYLooygt9758kyPGcWEphkWnBSxBWJErhw1Fsrw0dgz/e2dJOZbsCGnQPMVv5JydmTNzhsu5d7emrvWNie5K4wbuZByNV9NwPh+fhu7ALT7npgIz4VRRzBGhmII5K25Oq6ItARnpz/Uo09t1okfn70+9i7OL8PxsFno20gQM80bHn7wJmrwdkwv2Z3R+6Z2YPDGV589//xDJkykn7yK/jzpvKqO3EBYjjEaIjDAfXf92Po7CebSkay3ohm42RGCKUawJ36iEa0YlU5wgCFG36zqu0rJJi/xtmjWmqt3g2r36otMqvNXbMjO92V12+cJ/TN7YI/dumtg6GWGYcimIElL6HGGmsMASKamQQhILipCQFDHmI19SziQjlNrUTQqdbCCFG/hgBUeIpDgbPHYYws+j8WXkXJq/Wzh6lgQORdrECSbeZu0rjzKqPC0I9TYQlqw550Jx5wNUAfUEzq4Ri9x9GDwnTIlggiusEOW+D1JJIIh8TnwJsglfMUU4ZwQRLqhURwgzn5J9wvcLd9PmsW3njsXCDRbuIxF38ITP9NZ04P+Zgi5iZTKj6z6YMCpeE9zZjRVruL5rzKpOv1jcJ0wCkEAdaa6bR3qf0tzUaV13wxSAzF6ZliYDq1ff1fDehdNlubqxU9z57J/q4LIq/jJx822wLtoq7hnC/Ae7hD1UFm0zzPTNcFtbilIpS1E3erXndYxUf+TJnRNMfcvma/agD5bsHKo2t0IO8y6ZwEIRISgAKyAGJlAwehR6cTCaAsGpN1nRJn/oJv44NU2VxtblGnysmnWpd2xn5rZJiqIarTvNusQTiJrbiehcrvcpDr4xNS8mZV/o5xoeqrMvxvNW7mmxXMLLyyq6AvrRiQtoVudzlae9srPCEn5Ywgf1yxV9OImvpurDL+i1lL1/D7yWml/8EF5L4bWpUp1BydXw62Oyso04pj604dktGPDBdwIFGGP5cGTbkAhjRYliCEmCMWKcSCoQwYor+A0xWEAUo5hgix7fNg7Wo3A2+dHl6CewI/+R3WV48f7Ht7dFM2mrbg4CxxdsyJQD11JzkmaZSZwnDCPUAVOzLao7Zw5CgAPC1JmegFXfOjvkqjaQGhbgDrD1Lx/+BYGRY5PGCwAA";
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
