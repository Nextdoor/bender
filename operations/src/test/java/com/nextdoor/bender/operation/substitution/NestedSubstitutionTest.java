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
import java.util.List;
import java.util.Map;
import org.junit.Test;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.deserializer.FieldNotFoundException;
import com.nextdoor.bender.operation.substitution.metadata.MetadataSubstitution;
import com.nextdoor.bender.operation.substitution.regex.RegexSubstitution;
import com.nextdoor.bender.operation.substitution.ztatic.StaticSubstitution;
import com.nextdoor.bender.operation.substitution.ztatic.StaticSubstitutionConfig;
import com.nextdoor.bender.operation.substitution.field.FieldSubstitution;
import com.nextdoor.bender.operation.substitution.field.FieldSubstitutionConfig;
import com.nextdoor.bender.operation.substitution.context.ContextSubstitution;
import com.nextdoor.bender.testutils.DummyDeserializerHelper.DummpyMapEvent;

public class NestedSubstitutionTest {
  @Test
  public void testBasicNested() throws FieldNotFoundException {
    /*
     * Substitutions in nest
     */
    FieldSubstitutionConfig fieldSubConfig = new FieldSubstitutionConfig("bar",
        Arrays.asList("foo0", "foo1", "foo2"), false, true, true);
    StaticSubstitutionConfig staticSubConfig =
        new StaticSubstitutionConfig("static", "value", true);

    /*
     * Nested substitution
     */
    List<SubstitutionConfig> nsc = Arrays.asList(fieldSubConfig, staticSubConfig);
    NestedSubstitutionFactory nsf = new NestedSubstitutionFactory();
    nsf.setConf(new NestedSubstitutionConfig("a", nsc, true));

    DummpyMapEvent devent = new DummpyMapEvent();
    devent.setField("foo2", "1234");

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    SubstitutionOperation op = new SubstitutionOperation(Arrays.asList(nsf.newInstance()));
    op.perform(ievent);

    Map<String, Object> expectedNested = new HashMap<String, Object>() {
      {
        put("bar", "1234");
        put("static", "value");
      }
    };

    assertEquals(expectedNested, devent.getField("a"));
    assertEquals("1234", devent.getField("foo2"));
  }

  @Test
  public void testNestedNested() throws FieldNotFoundException {
    /*
     * Expected output {a={b={bar=1234, static=value}}, foo2=1234}
     */

    /*
     * Inner nest substitutions
     */
    FieldSubstitutionConfig fieldSubConfig = new FieldSubstitutionConfig("bar",
        Arrays.asList("foo0", "foo1", "foo2"), false, true, true);
    StaticSubstitutionConfig staticSubConfig =
        new StaticSubstitutionConfig("static", "value", true);


    /*
     * Substitutions in outer nest
     */
    List<SubstitutionConfig> nscInner = Arrays.asList(fieldSubConfig, staticSubConfig);
    NestedSubstitutionConfig nsfInner = new NestedSubstitutionConfig("b", nscInner, true);

    /*
     * Substitution
     */
    NestedSubstitutionConfig nsfOuter =
        new NestedSubstitutionConfig("a", Arrays.asList(nsfInner), true);
    NestedSubstitutionFactory subFactory = new NestedSubstitutionFactory();
    subFactory.setConf(nsfOuter);

    DummpyMapEvent devent = new DummpyMapEvent();
    devent.setField("foo2", "1234");

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    List<Substitution> sub = Arrays.asList(subFactory.newInstance());
    SubstitutionOperation op = new SubstitutionOperation(sub);
    op.perform(ievent);

    Map<String, Object> expectedNest2 = new HashMap<String, Object>() {
      {
        put("bar", "1234");
        put("static", "value");
      }
    };

    Map<String, Object> expectedNest1 = new HashMap<String, Object>() {
      {
        put("b", expectedNest2);
      }
    };

    assertEquals(expectedNest1, devent.getField("a"));
    assertEquals("1234", devent.getField("foo2"));
  }
}
