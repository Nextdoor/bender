/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF
 * ANY KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * Copyright 2018 Nextdoor.com, Inc
 */

package com.nextdoor.bender.operation.substitution;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.deserializer.DeserializedEvent;
import com.nextdoor.bender.operation.substitution.SubstitutionSpec.Interpreter;

public class SubstitutionOperationTest {

  public static class DummpyEvent implements DeserializedEvent {
    public Map<String, Object> payload = new HashMap<String, Object>();

    @Override
    public Object getPayload() {
      return payload;
    }

    @Override
    public String getField(String fieldName) {
      Object o = payload.get(fieldName);
      if (o == null) {
        return null;
      }

      return o.toString();
    }

    @Override
    public void setPayload(Object object) {
      this.payload = (Map<String, Object>) object;
    }

    @Override
    public void setField(String fieldName, Object value) {
      payload.put(fieldName, value);
    }
  }


  @Test
  public void testKnownField() {
    ArrayList<SubstitutionSpec> subSpecs = new ArrayList<SubstitutionSpec>();
    subSpecs.add(new SubstitutionSpec("bar", "foo", Interpreter.FIELD));

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
    ArrayList<SubstitutionSpec> subSpecs = new ArrayList<SubstitutionSpec>();
    subSpecs.add(new SubstitutionSpec("bar", "foo", Interpreter.FIELD));

    DummpyEvent devent = new DummpyEvent();

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    SubstitutionOperation op = new SubstitutionOperation(subSpecs);
    op.perform(ievent);

    assertEquals(null, devent.getField("bar"));
  }

  @Test
  public void testStaticField() {
    ArrayList<SubstitutionSpec> subSpecs = new ArrayList<SubstitutionSpec>();
    subSpecs.add(new SubstitutionSpec("foo", "1234", Interpreter.STATIC));

    DummpyEvent devent = new DummpyEvent();

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    SubstitutionOperation op = new SubstitutionOperation(subSpecs);
    op.perform(ievent);

    assertEquals("1234", devent.getField("foo"));
  }
}
