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

package com.nextdoor.bender.operation.substitution;

import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.LambdaContext;
import com.nextdoor.bender.aws.TestContext;
import com.nextdoor.bender.deserializer.FieldNotFoundException;
import com.nextdoor.bender.operation.substitution.context.ContextSubstitution;
import com.nextdoor.bender.testutils.DummyDeserializerHelper.DummpyMapEvent;

public class ContextSubstitutionTest {
  @Test
  public void testExcludesContext() throws FieldNotFoundException {
    ArrayList<Substitution> substitutions = new ArrayList<Substitution>();
    substitutions.add(new ContextSubstitution("foo", Collections.emptyList(),
        Arrays.asList("functionName"), true));

    DummpyMapEvent devent = new DummpyMapEvent();
    TestContext ctx = new TestContext();
    ctx.setFunctionName("fun name");
    ctx.setInvokedFunctionArn("some arn");

    InternalEvent ievent = new InternalEvent("", new LambdaContext(ctx), 10);
    ievent.setEventObj(devent);
    ievent.setEventTime(20);

    SubstitutionOperation op = new SubstitutionOperation(substitutions);
    op.perform(ievent);

    Map<String, Object> expected = new HashMap<String, Object>() {
      {
        put("invokedFunctionArn", "some arn");
      }
    };

    assertEquals(expected, devent.getField("foo"));
  }

  @Test
  public void testIncludesContext() throws FieldNotFoundException {
    ArrayList<Substitution> substitutions = new ArrayList<Substitution>();
    substitutions.add(new ContextSubstitution("foo", Arrays.asList("functionName"),
        Collections.emptyList(), true));

    DummpyMapEvent devent = new DummpyMapEvent();
    TestContext ctx = new TestContext();
    ctx.setFunctionName("fun name");
    ctx.setInvokedFunctionArn("some arn");

    InternalEvent ievent = new InternalEvent("", new LambdaContext(ctx), 10);
    ievent.setEventObj(devent);
    ievent.setEventTime(20);

    SubstitutionOperation op = new SubstitutionOperation(substitutions);
    op.perform(ievent);

    Map<String, Object> expected = new HashMap<String, Object>() {
      {
        put("functionName", "fun name");
      }
    };

    assertEquals(expected, devent.getField("foo"));
  }
}
