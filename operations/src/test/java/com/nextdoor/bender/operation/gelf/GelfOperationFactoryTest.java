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

package com.nextdoor.bender.operation.gelf;

import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.junit.Test;
import com.nextdoor.bender.operation.gelf.GelfOperation;
import com.nextdoor.bender.operation.gelf.GelfOperationConfig;
import com.nextdoor.bender.operation.gelf.GelfOperationFactory;
import com.nextdoor.bender.operation.substitution.SubstitutionConfig;
import com.nextdoor.bender.operation.substitution.field.FieldSubstitutionConfig;
import com.nextdoor.bender.operation.substitution.ztatic.StaticSubstitutionConfig;

public class GelfOperationFactoryTest {

  @Test
  public void foo() {
    GelfOperationConfig config = new GelfOperationConfig();
    config.setSrcHostField(Arrays.asList("foo_host"));
    config.setSrcShortMessageField(Arrays.asList("foo_short_message", "bar"));
    config.setSrcFileField(Arrays.asList("filename"));

    GelfOperationFactory factory = new GelfOperationFactory();
    factory.setConf(config);

    GelfOperation op = factory.newInstance();
    List<SubstitutionConfig> actual = factory.getSubConfigs();

    ArrayList<SubstitutionConfig> expected = new ArrayList<>();
    expected.add(new FieldSubstitutionConfig("host", Arrays.asList("foo_host"), false, true, true));
    expected
        .add(new FieldSubstitutionConfig("file", Arrays.asList("filename"), false, false, false));
    expected.add(new FieldSubstitutionConfig("short_message",
        Arrays.asList("foo_short_message", "bar"), false, true, true));
    expected.add(new StaticSubstitutionConfig("version", "1.1", true));

    Collections.sort(expected, Comparator.comparingInt(Object::hashCode));
    Collections.sort(actual, Comparator.comparingInt(Object::hashCode));

    assertEquals(expected, actual);
  }
}
