/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright 2017 Nextdoor.com, Inc
 */

package com.nextdoor.bender.config;

import static junit.framework.TestCase.assertEquals;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

public class ConfigurationTest {

  @Rule
  public final EnvironmentVariables envVars = new EnvironmentVariables();

  @Test
  public void testStringTemplatesInConfig() throws ConfigurationException, ClassNotFoundException {
    envVars.set("CUSTOM_SOURCE_NAME", "TestSourceName");
    BenderConfig config = BenderConfig.load("/config/config_with_env.json");

    List<SourceConfig> sources = config.getSources();
    assertEquals(sources.size(), 1);
    assertEquals(sources.get(0).getName(), "TestSourceName");
  }

  @Test(expected = ConfigurationException.class)
  public void testMissingStringTemplatesInConfig() throws ConfigurationException, ClassNotFoundException {
    BenderConfig.load("/config/config_with_env.json");
  }

  @Test
  public void testYaml() throws ConfigurationException, ClassNotFoundException {
    envVars.set("CUSTOM_SOURCE_NAME", "TestSourceName");
    BenderConfig config = BenderConfig.load("/config/config_with_env.yaml");

    List<SourceConfig> sources = config.getSources();
    assertEquals(sources.size(), 1);
    assertEquals(sources.get(0).getName(), "TestSourceName");
  }
}
