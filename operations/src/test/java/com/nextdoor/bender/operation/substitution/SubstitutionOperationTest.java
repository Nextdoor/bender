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
import java.util.NoSuchElementException;

import org.junit.Test;

import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.LambdaContext;
import com.nextdoor.bender.aws.TestContext;
import com.nextdoor.bender.deserializer.DeserializedEvent;

public class SubstitutionOperationTest {

  public static class DummpyEvent implements DeserializedEvent {
    public Map<String, Object> payload = new HashMap<String, Object>();

    @Override
    public Object getPayload() {
      return payload;
    }

    @Override
    public Object getField(String fieldName) {
      return payload.get(fieldName);
    }

    @Override
    public void setPayload(Object object) {
      this.payload = (Map<String, Object>) object;
    }

    @Override
    public void setField(String fieldName, Object value) {
      payload.put(fieldName, value);
    }

    @Override
    public String getFieldAsString(String fieldName) throws NoSuchElementException {
      return payload.get(fieldName).toString();
    }
  }


  @Test
  public void testKnownField() {
    ArrayList<SubSpecConfig<?>> subSpecs = new ArrayList<SubSpecConfig<?>>();
    subSpecs.add(new FieldSubSpecConfig("bar", "foo"));

    DummpyEvent devent = new DummpyEvent();
    devent.setField("foo", "1234");

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    SubstitutionOperation op = new SubstitutionOperation(subSpecs);
    op.perform(ievent);

    assertEquals("1234", devent.getField("bar"));
    assertEquals("1234", devent.getField("foo"));
  }

  @Test
  public void testUnknownField() {
    ArrayList<SubSpecConfig<?>> subSpecs = new ArrayList<SubSpecConfig<?>>();
    subSpecs.add(new FieldSubSpecConfig("bar", "foo"));

    DummpyEvent devent = new DummpyEvent();

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    SubstitutionOperation op = new SubstitutionOperation(subSpecs);
    op.perform(ievent);

    assertEquals(null, devent.getField("bar"));
  }

  @Test
  public void testStaticField() {
    ArrayList<SubSpecConfig<?>> subSpecs = new ArrayList<SubSpecConfig<?>>();
    subSpecs.add(new StaticSubSpecConfig("foo", "1234"));

    DummpyEvent devent = new DummpyEvent();

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    SubstitutionOperation op = new SubstitutionOperation(subSpecs);
    op.perform(ievent);

    assertEquals("1234", devent.getField("foo"));
  }

  @Test
  public void testExcludeMetadata() {
    ArrayList<SubSpecConfig<?>> subSpecs = new ArrayList<SubSpecConfig<?>>();
    subSpecs.add(
        new MetadataSubSpecConfig("foo", Collections.emptyList(), Arrays.asList("sourceLagMs")));

    DummpyEvent devent = new DummpyEvent();

    InternalEvent ievent = new InternalEvent("", null, 10);
    ievent.setEventObj(devent);
    ievent.setEventTime(20);

    SubstitutionOperation op = new SubstitutionOperation(subSpecs);
    op.perform(ievent);

    Map<String, Object> expected = new HashMap<String, Object>() {
      {
        put("arrivalEpochMs", new Long(10));
        put("eventSha1Hash", "da39a3ee5e6b4b0d3255bfef95601890afd80709");
        put("eventEpochMs", new Long(20));
      }
    };

    assertEquals(expected, devent.getField("foo"));
  }

  @Test
  public void testIncludeMetadata() {
    ArrayList<SubSpecConfig<?>> subSpecs = new ArrayList<SubSpecConfig<?>>();
    subSpecs.add(
        new MetadataSubSpecConfig("foo", Arrays.asList("eventSha1Hash"), Collections.emptyList()));

    DummpyEvent devent = new DummpyEvent();

    InternalEvent ievent = new InternalEvent("", null, 10);
    ievent.setEventObj(devent);
    ievent.setEventTime(20);

    SubstitutionOperation op = new SubstitutionOperation(subSpecs);
    op.perform(ievent);

    Map<String, Object> expected = new HashMap<String, Object>() {
      {
        put("eventSha1Hash", "da39a3ee5e6b4b0d3255bfef95601890afd80709");
      }
    };

    assertEquals(expected, devent.getField("foo"));
  }

  @Test
  public void testExcludesContext() {
    ArrayList<SubSpecConfig<?>> subSpecs = new ArrayList<SubSpecConfig<?>>();
    subSpecs.add(
        new ContextSubSpecConfig("foo", Collections.emptyList(), Arrays.asList("functionName")));

    DummpyEvent devent = new DummpyEvent();
    TestContext ctx = new TestContext();
    ctx.setFunctionName("fun name");
    ctx.setInvokedFunctionArn("some arn");

    InternalEvent ievent = new InternalEvent("", new LambdaContext(ctx), 10);
    ievent.setEventObj(devent);
    ievent.setEventTime(20);

    SubstitutionOperation op = new SubstitutionOperation(subSpecs);
    op.perform(ievent);

    Map<String, Object> expected = new HashMap<String, Object>() {
      {
        put("invokedFunctionArn", "some arn");
      }
    };

    assertEquals(expected, devent.getField("foo"));
  }

  @Test
  public void testIncludesContext() {
    ArrayList<SubSpecConfig<?>> subSpecs = new ArrayList<SubSpecConfig<?>>();
    subSpecs.add(
        new ContextSubSpecConfig("foo", Arrays.asList("functionName"), Collections.emptyList()));

    DummpyEvent devent = new DummpyEvent();
    TestContext ctx = new TestContext();
    ctx.setFunctionName("fun name");
    ctx.setInvokedFunctionArn("some arn");

    InternalEvent ievent = new InternalEvent("", new LambdaContext(ctx), 10);
    ievent.setEventObj(devent);
    ievent.setEventTime(20);

    SubstitutionOperation op = new SubstitutionOperation(subSpecs);
    op.perform(ievent);

    Map<String, Object> expected = new HashMap<String, Object>() {
      {
        put("functionName", "fun name");
      }
    };

    assertEquals(expected, devent.getField("foo"));
  }
}
