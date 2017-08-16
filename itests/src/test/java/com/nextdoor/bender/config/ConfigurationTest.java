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
 * Copyright $year Nextdoor.com, Inc
 *
 */

package com.nextdoor.bender.config;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.nextdoor.bender.handler.BaseHandler;

public class ConfigurationTest {

  @Test
  public void testConfigurationFile() throws Exception {
    BenderConfig config = BenderConfig.load(BaseHandler.class, "config.json");

    HandlerResources resources;
    try {
      resources = new HandlerResources(config);
    } catch (Exception e) {
      throw new Exception("Error loading: " + config, e);
    }

    assertNotNull(resources.getWrapperFactory());
    assertNotNull(resources.getTransportFactory());
    assertTrue(resources.getSources().size() > 0);

    for (Source s : resources.getSources().values()) {
      assertNotNull(s.getDeserProcessor());
      assertNotNull(s.getSourceRegex());
    }
  }

}
