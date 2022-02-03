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
import com.nextdoor.bender.deserializer.FieldNotFoundException;
import com.nextdoor.bender.operation.substitution.metadata.MetadataSubstitution;
import com.nextdoor.bender.testutils.DummyDeserializerHelper.DummpyMapEvent;

public class MetadataSubstitutionTest {
  @Test
  public void testExcludeMetadata() throws FieldNotFoundException {
    ArrayList<Substitution> substitutions = new ArrayList<>();
    substitutions.add(new MetadataSubstitution("foo", Collections.emptyList(),
        Arrays.asList("sourceLagMs"), true));

    DummpyMapEvent devent = new DummpyMapEvent();

    InternalEvent ievent = new InternalEvent("", null, 10);
    ievent.setEventObj(devent);
    ievent.setEventTime(20);

    SubstitutionOperation op = new SubstitutionOperation(substitutions);
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
  public void testIncludeMetadata() throws FieldNotFoundException {
    ArrayList<Substitution> substitutions = new ArrayList<>();
    substitutions.add(new MetadataSubstitution("foo", Arrays.asList("eventSha1Hash"),
        Collections.emptyList(), true));

    DummpyMapEvent devent = new DummpyMapEvent();

    InternalEvent ievent = new InternalEvent("", null, 10);
    ievent.setEventObj(devent);
    ievent.setEventTime(20);

    SubstitutionOperation op = new SubstitutionOperation(substitutions);
    op.perform(ievent);

    Map<String, Object> expected = new HashMap<String, Object>() {
      {
        put("eventSha1Hash", "da39a3ee5e6b4b0d3255bfef95601890afd80709");
      }
    };

    assertEquals(expected, devent.getField("foo"));
  }
}
