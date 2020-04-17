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
 * Copyright 2017 Nextdoor.com, Inc
 *
 */

package com.nextdoor.bender.handler.kinesis;

import com.amazonaws.services.lambda.runtime.events.KinesisEvent;
import com.nextdoor.bender.handler.BaseHandler;
import com.nextdoor.bender.handler.HandlerTest;
import com.nextdoor.bender.testutils.TestUtils;

import java.nio.ByteBuffer;
import java.util.Base64;

public class KinesisHandlerWithGzipTest extends HandlerTest<KinesisEvent> {

    @Override
    public KinesisHandler getHandler() {
        return new KinesisHandler();
    }

    @Override
    public KinesisEvent getTestEvent() throws Exception {
        KinesisEvent kinesisEvent = TestUtils.createEvent(this.getClass(), "basic_input.json");
        kinesisEvent.getRecords().get(0).getKinesis().setData(getBase64DecodedGzipFile());
        return kinesisEvent;
    }

    //this decodes a base64 encoded string from Kinesis (cleaned for identifying data) into a Gzip ByteBuffer
    private ByteBuffer getBase64DecodedGzipFile() {
        String encodedData = "H4sICDZ0i14AA2Jhc2ljX291dHB1dF9raW5lc2lzX2VuY29kZWQgY29weS5qc29uAJ2R3UvDMBTF3/dXXPI8hH5E3N4Ki0Po5mj7IIhI1t7NQNPUJFVk7H836b4QihNf8nDO796Tk+xGAESiMXyLxVeLZApklhTJ64LleTJnZOwB9dmg9lYQRjF7Shar9OjUajvXqmu9abhsa0yPypJLPDO51cjlD+ggXSjTrU2pRWuFau5FbVEbxz87C05TB5k46eW0mH1gYy/grj+dJSofJqqgX95LVrie1i1yTkDvaEgnNIwnt/RMHN/BT+ZFkhWQ4XvnZh6qKemZ/Xg4JbySQoM4GkxREoG3bS1K7nuDa2QApbAWK9hoJcG+IaRcrisOm64pPXXz+2Wi65cZrMyWsz8Xjv+ZkbHV49C7+i8d7b8BCrILk40CAAA=";
        Base64.Decoder base64decoder = Base64.getDecoder();
        byte[] decoded = base64decoder.decode(encodedData);
        return ByteBuffer.wrap(decoded);
    }

    @Override
    public String getExpectedEvent() {
        return "basic_output_cw_logs.json";
    }

    @Override
    public void setup() {

    }

    @Override
    public String getConfigFile() {
        return "/com/nextdoor/bender/handler/config_kinesis_with_gzip.json";
    }

    @Override
    public void teardown() {

    }
}
