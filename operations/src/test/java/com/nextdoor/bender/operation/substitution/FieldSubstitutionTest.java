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
import org.junit.Test;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.deserializer.FieldNotFoundException;
import com.nextdoor.bender.operation.OperationException;
import com.nextdoor.bender.operation.substitution.field.FieldSubstitution;
import com.nextdoor.bender.testutils.DummyDeserializerHelper.DummpyMapEvent;

public class FieldSubstitutionTest {

  @Test
  public void testKnownField() throws FieldNotFoundException {
    ArrayList<Substitution> substitutions = new ArrayList<>();
    substitutions.add(new FieldSubstitution("bar", Arrays.asList("foo"), false, true, true));

    DummpyMapEvent devent = new DummpyMapEvent();
    devent.setField("foo", "1234");

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    SubstitutionOperation op = new SubstitutionOperation(substitutions);
    op.perform(ievent);

    assertEquals(2, devent.payload.size());
    assertEquals("1234", devent.getField("bar"));
    assertEquals("1234", devent.getField("foo"));
  }

  @Test
  public void testRemoveField() throws FieldNotFoundException {
    ArrayList<Substitution> substitutions = new ArrayList<>();
    substitutions.add(new FieldSubstitution("bar", Arrays.asList("foo"), true, true, true));

    DummpyMapEvent devent = new DummpyMapEvent();
    devent.setField("foo", "1234");

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    SubstitutionOperation op = new SubstitutionOperation(substitutions);
    op.perform(ievent);

    assertEquals(1, devent.payload.size());
    assertEquals("1234", devent.getField("bar"));
  }

  @Test
  public void testRemoveFieldReplace() throws FieldNotFoundException {
    ArrayList<Substitution> substitutions = new ArrayList<>();
    substitutions.add(new FieldSubstitution("foo", Arrays.asList("foo"), true, true, true));

    DummpyMapEvent devent = new DummpyMapEvent();
    devent.setField("foo", "1234");

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    SubstitutionOperation op = new SubstitutionOperation(substitutions);
    op.perform(ievent);

    assertEquals(1, devent.payload.size());
    assertEquals("1234", devent.getField("foo"));
  }

  public void testUnknownField() {
    ArrayList<Substitution> substitutions = new ArrayList<>();
    substitutions.add(new FieldSubstitution("bar", Arrays.asList("foo"), false, false, true));

    DummpyMapEvent devent = new DummpyMapEvent();

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    SubstitutionOperation op = new SubstitutionOperation(substitutions);
    op.perform(ievent);
  }

  @Test(expected = OperationException.class)
  public void testUnknownFieldFail() {
    ArrayList<Substitution> substitutions = new ArrayList<>();
    substitutions.add(new FieldSubstitution("bar", Arrays.asList("foo"), false, true, true));

    DummpyMapEvent devent = new DummpyMapEvent();

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    SubstitutionOperation op = new SubstitutionOperation(substitutions);
    op.perform(ievent);
  }

  @Test
  public void testFieldList() throws FieldNotFoundException {
    ArrayList<Substitution> substitutions = new ArrayList<>();
    substitutions.add(
        new FieldSubstitution("bar", Arrays.asList("foo0", "foo1", "foo2"), false, true, true));

    DummpyMapEvent devent = new DummpyMapEvent();
    devent.setField("foo2", "1234");

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    SubstitutionOperation op = new SubstitutionOperation(substitutions);
    op.perform(ievent);

    assertEquals(2, devent.payload.size());
    assertEquals("1234", devent.getField("bar"));
    assertEquals("1234", devent.getField("foo2"));
  }
}
