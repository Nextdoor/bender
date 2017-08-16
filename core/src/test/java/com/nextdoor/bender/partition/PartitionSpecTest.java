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
 * Copyright 2017 Nextdoor.com, Inc
 *
 */

package com.nextdoor.bender.partition;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.nextdoor.bender.config.BenderConfig;
import com.nextdoor.bender.partition.PartitionSpec.Interpreter;
import com.nextdoor.bender.partition.PartitionSpec.StringFormat;

import edu.emory.mathcs.backport.java.util.Collections;

public class PartitionSpecTest {

  @Test
  public void testLoadConfg() {
    BenderConfig config = BenderConfig.load(PartitionSpecTest.class, "partition_config.json");
    PartitionOperationConfig op =
        (PartitionOperationConfig) config.getSources().get(0).getOperationConfigs().get(0);
    PartitionSpec spec = op.getPartitionSpecs().get(0);

    assertEquals("type", spec.getName());
    assertEquals(Interpreter.STRING, spec.getInterpreter());

    List<String> expected = Arrays.asList("one", "two", "three");
    assertEquals(expected, spec.getSources());
  }

  @Test
  public void testLoadConfgTime() {
    BenderConfig config = BenderConfig.load(PartitionSpecTest.class, "partition_config_time.json");
    PartitionOperationConfig op =
        (PartitionOperationConfig) config.getSources().get(0).getOperationConfigs().get(0);
    PartitionSpec spec = op.getPartitionSpecs().get(0);

    assertEquals("dt", spec.getName());
    assertEquals(Interpreter.SECONDS, spec.getInterpreter());

    List<String> expected = Arrays.asList("epoch");
    assertEquals(expected, spec.getSources());
    assertEquals("2017-01-19 05:05:59", spec.interpret("1484802359"));
  }

  @Test
  public void testEmptyDate() {
    PartitionSpec spec = new PartitionSpec("test", Collections.emptyList(), Interpreter.SECONDS,
        "YYYY-MM-dd HH:mm:ss");
    assertEquals(null, spec.interpret(""));
    assertEquals(null, spec.interpret(null));
  }

  @Test
  public void testDateFormatterSeconds() {
    PartitionSpec spec = new PartitionSpec("test", Collections.emptyList(), Interpreter.SECONDS,
        "YYYY-MM-dd HH:mm:ss");
    assertEquals("2017-01-19 05:05:59", spec.interpret("1484802359"));
  }

  @Test
  public void testDateFormatterMilliseconds() {
    PartitionSpec spec = new PartitionSpec("test", Collections.emptyList(),
        Interpreter.MILLISECONDS, "YYYY-MM-dd HH:mm:ss");
    assertEquals("2017-01-19 05:05:59", spec.interpret("1484802359000"));
  }

  @Test
  public void testStatic() {
    PartitionSpec spec =
        new PartitionSpec("test", Collections.emptyList(), Interpreter.STATIC, "static");
    assertEquals("static", spec.interpret(""));
  }

  @Test
  public void testString() {
    PartitionSpec spec =
        new PartitionSpec("test", Collections.emptyList(), Interpreter.STRING, "string");
    assertEquals("a_string", spec.interpret("a_string"));
  }

  @Test
  public void testStringConstructor() {
    PartitionSpec spec = new PartitionSpec("test", Collections.emptyList());
    assertEquals("a_string", spec.interpret("a_string"));
  }

  @Test(expected = RuntimeException.class)
  public void testInvalidInterpreter() {
    PartitionSpec spec =
        new PartitionSpec("test", Collections.emptyList(), Interpreter.STATIC, "static");
    spec.getFormattedTime("will fail");
  }

  @Test
  public void testStringFormatUpper() {
    BenderConfig config =
        BenderConfig.load(PartitionSpecTest.class, "partition_config_format.json");
    PartitionOperationConfig op =
        (PartitionOperationConfig) config.getSources().get(0).getOperationConfigs().get(0);
    PartitionSpec spec = op.getPartitionSpecs().get(0);

    assertEquals("type", spec.getName());
    assertEquals(Interpreter.STRING, spec.getInterpreter());
    assertEquals(StringFormat.TOUPPER, spec.getStringFormat());

    assertEquals("THISISATEST", spec.interpret("ThisIsATest"));
  }

  @Test
  public void testStringFormatLower() {
    BenderConfig config =
        BenderConfig.load(PartitionSpecTest.class, "partition_config_format.json");
    PartitionOperationConfig op =
        (PartitionOperationConfig) config.getSources().get(0).getOperationConfigs().get(0);
    PartitionSpec spec = op.getPartitionSpecs().get(0);
    spec.setStringFormat(StringFormat.TOLOWER);

    assertEquals("type", spec.getName());
    assertEquals(Interpreter.STRING, spec.getInterpreter());

    assertEquals("thisisatest", spec.interpret("ThisIsATest"));
  }
}
