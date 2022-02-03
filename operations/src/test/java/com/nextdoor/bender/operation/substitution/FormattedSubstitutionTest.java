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
import org.apache.commons.text.ExtendedMessageFormat;
import org.junit.Test;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.deserializer.FieldNotFoundException;
import com.nextdoor.bender.operation.OperationException;
import com.nextdoor.bender.operation.substitution.formatted.FormattedSubstitution;
import com.nextdoor.bender.testutils.DummyDeserializerHelper.DummpyMapEvent;

public class FormattedSubstitutionTest {
  @Test
  public void testStringSubKnown() throws FieldNotFoundException {
    Variable.FieldVariable v = new Variable.FieldVariable();
    v.setFailSrcNotFound(true);
    v.setSrcFields(Arrays.asList("foo"));
    ArrayList<Substitution> substitutions = new ArrayList<>();
    substitutions.add(new FormattedSubstitution("bar", new ExtendedMessageFormat("foo = {0}"),
        Arrays.asList(v), true));

    DummpyMapEvent devent = new DummpyMapEvent();
    devent.setField("foo", "1234");

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    SubstitutionOperation op = new SubstitutionOperation(substitutions);
    op.perform(ievent);

    assertEquals(2, devent.payload.size());
    assertEquals("foo = 1234", devent.getField("bar"));
    assertEquals("1234", devent.getField("foo"));
  }

  @Test
  public void testStringReplace() throws FieldNotFoundException {
    Variable.FieldVariable v = new Variable.FieldVariable();
    v.setFailSrcNotFound(true);
    v.setSrcFields(Arrays.asList("foo"));
    ArrayList<Substitution> substitutions = new ArrayList<>();
    substitutions.add(new FormattedSubstitution("foo", new ExtendedMessageFormat("foo = {0}"),
        Arrays.asList(v), true));

    DummpyMapEvent devent = new DummpyMapEvent();
    devent.setField("foo", "1234");

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    SubstitutionOperation op = new SubstitutionOperation(substitutions);
    op.perform(ievent);

    assertEquals(1, devent.payload.size());
    assertEquals("foo = 1234", devent.getField("foo"));
  }

  @Test
  public void testStringSubNumberKnown() throws FieldNotFoundException {
    Variable.FieldVariable v = new Variable.FieldVariable();
    v.setFailSrcNotFound(true);
    v.setSrcFields(Arrays.asList("foo"));

    ArrayList<Substitution> substitutions = new ArrayList<>();
    substitutions.add(new FormattedSubstitution("bar", new ExtendedMessageFormat("number = {0}"),
        Arrays.asList(v), true));

    DummpyMapEvent devent = new DummpyMapEvent();
    devent.setField("foo", new Float(1.234f));

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    SubstitutionOperation op = new SubstitutionOperation(substitutions);
    op.perform(ievent);

    assertEquals(2, devent.payload.size());
    assertEquals("number = 1.234", devent.getField("bar"));
  }

  @Test
  public void testStringSubUnknown() throws FieldNotFoundException {
    Variable.FieldVariable v = new Variable.FieldVariable();
    v.setFailSrcNotFound(false);
    v.setSrcFields(Arrays.asList("baz"));

    ArrayList<Substitution> substitutions = new ArrayList<>();
    substitutions.add(new FormattedSubstitution("bar", new ExtendedMessageFormat("foo = {0}"),
        Arrays.asList(v), true));

    DummpyMapEvent devent = new DummpyMapEvent();
    devent.setField("foo", "1234");

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    SubstitutionOperation op = new SubstitutionOperation(substitutions);
    op.perform(ievent);

    assertEquals(2, devent.payload.size());
    assertEquals("foo = null", devent.getField("bar"));
    assertEquals("1234", devent.getField("foo"));
  }

  @Test(expected = OperationException.class)
  public void testStringSubUnknownFail() throws FieldNotFoundException {
    Variable.FieldVariable v = new Variable.FieldVariable();
    v.setFailSrcNotFound(true);
    v.setSrcFields(Arrays.asList("baz"));

    ArrayList<Substitution> substitutions = new ArrayList<>();
    substitutions.add(new FormattedSubstitution("bar", new ExtendedMessageFormat("foo = {0}"),
        Arrays.asList(v), true));

    DummpyMapEvent devent = new DummpyMapEvent();
    devent.setField("foo", "1234");

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    SubstitutionOperation op = new SubstitutionOperation(substitutions);
    op.perform(ievent);
  }

  @Test
  public void testStringSubRemove() throws FieldNotFoundException {
    Variable.FieldVariable v = new Variable.FieldVariable();
    v.setFailSrcNotFound(true);
    v.setRemoveSrcField(true);
    v.setSrcFields(Arrays.asList("foo"));

    ArrayList<Substitution> substitutions = new ArrayList<>();
    substitutions.add(new FormattedSubstitution("bar", new ExtendedMessageFormat("foo = {0}"),
        Arrays.asList(v), true));

    DummpyMapEvent devent = new DummpyMapEvent();
    devent.setField("foo", "1234");

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    SubstitutionOperation op = new SubstitutionOperation(substitutions);
    op.perform(ievent);

    assertEquals(1, devent.payload.size());
    assertEquals("foo = 1234", devent.getField("bar"));
  }

  @Test
  public void testStringSubStatic() throws FieldNotFoundException {
    Variable.StaticVariable v = new Variable.StaticVariable();
    v.setValue("1234");

    ArrayList<Substitution> substitutions = new ArrayList<>();
    substitutions.add(new FormattedSubstitution("bar", new ExtendedMessageFormat("static = {0}"),
        Arrays.asList(v), true));

    DummpyMapEvent devent = new DummpyMapEvent();
    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    SubstitutionOperation op = new SubstitutionOperation(substitutions);
    op.perform(ievent);

    assertEquals(1, devent.payload.size());
    assertEquals("static = 1234", devent.getField("bar"));
  }
}
