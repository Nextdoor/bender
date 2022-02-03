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
import org.junit.Test;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.deserializer.FieldNotFoundException;
import com.nextdoor.bender.operation.substitution.ztatic.StaticSubstitution;
import com.nextdoor.bender.testutils.DummyDeserializerHelper.DummpyMapEvent;

public class StaticSubstitutionTest {

  @Test
  public void testStaticField() throws FieldNotFoundException {
    ArrayList<Substitution> substitutions = new ArrayList<>();
    substitutions.add(new StaticSubstitution("foo", "1234", true));

    DummpyMapEvent devent = new DummpyMapEvent();

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    SubstitutionOperation op = new SubstitutionOperation(substitutions);
    op.perform(ievent);

    assertEquals("1234", devent.getField("foo"));
  }
}
