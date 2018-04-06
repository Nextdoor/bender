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

package com.nextdoor.bender.handler.s3;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;

import com.nextdoor.bender.LambdaContext;
import com.nextdoor.bender.aws.TestContext;

public class S3InternalEventTest {

  @Test
  public void testAppendFilename() {
    TestContext context = new TestContext();
    context.setAwsRequestId("req_id");
    S3InternalEvent ievent =
        new S3InternalEvent("foo", new LambdaContext(context), 0, "file", "bucket", "v1");

    ievent.setEventObj(null);

    Map<String, String> expected = new HashMap<String, String>(1);
    expected.put(S3InternalEvent.FILENAME_PARTITION, DigestUtils.sha1Hex("file"));

    assertEquals(expected, ievent.getPartitions());
  }
}
