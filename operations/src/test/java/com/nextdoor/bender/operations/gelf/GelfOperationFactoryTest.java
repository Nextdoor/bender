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

package com.nextdoor.bender.operations.gelf;

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
import com.nextdoor.bender.operation.substitution.FieldSubSpecConfig;
import com.nextdoor.bender.operation.substitution.StaticSubSpecConfig;
import com.nextdoor.bender.operation.substitution.SubSpecConfig;

public class GelfOperationFactoryTest {

  @Test
  public void foo() {
    GelfOperationConfig config = new GelfOperationConfig();
    config.setSrcHostField(Arrays.asList("foo_host"));
    config.setSrcShortMessageField(Arrays.asList("foo_short_message"));
    config.setSrcFileField(Arrays.asList("filename"));

    GelfOperationFactory factory = new GelfOperationFactory();
    factory.setConf(config);

    GelfOperation op = factory.newInstance();
    List<SubSpecConfig<?>> actual = op.getSubSpecs();

    ArrayList<SubSpecConfig<?>> expected = new ArrayList<SubSpecConfig<?>>();
    expected.add(new FieldSubSpecConfig("host", Arrays.asList("foo_host")));
    expected.add(new FieldSubSpecConfig("file", Arrays.asList("filename")));
    expected.add(new FieldSubSpecConfig("short_message", Arrays.asList("foo_short_message")));
    expected.add(new StaticSubSpecConfig("version", "1.1"));

    Collections.sort(expected, Comparator.comparingInt(Object::hashCode));
    Collections.sort(actual, Comparator.comparingInt(Object::hashCode));

    assertEquals(expected, actual);
  }
}
