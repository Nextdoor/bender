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

package com.nextdoor.bender.mutator.key;

import java.util.EnumSet;
import java.util.Set;

import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.GsonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.GsonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import com.nextdoor.bender.config.AbstractConfig;
import com.nextdoor.bender.mutator.Mutator;
import com.nextdoor.bender.mutator.MutatorFactory;

/**
 * Create a {@link JsonRootNodeMutator}.
 */
public class JsonRootNodeMutatorFactory implements MutatorFactory {

  private JsonRootNodeMutatorConfig config;

  public JsonRootNodeMutatorFactory() {
    /*
     * Set static configuration for JsonPath
     */
    com.jayway.jsonpath.Configuration.setDefaults(new com.jayway.jsonpath.Configuration.Defaults() {
      private final JsonProvider jsonProvider = new GsonJsonProvider();
      private final MappingProvider mappingProvider = new GsonMappingProvider();

      @Override
      public JsonProvider jsonProvider() {
          return jsonProvider;
      }

      @Override
      public MappingProvider mappingProvider() {
          return mappingProvider;
      }

      @Override
      public Set<Option> options() {
          return EnumSet.of(Option.SUPPRESS_EXCEPTIONS);
      }
    });
  }

  @Override
  public Mutator newInstance() {
    return new JsonRootNodeMutator(this.config.getRootPath());
  }

  @Override
  public Class getChildClass() {
    return JsonRootNodeMutator.class;
  }

  @Override
  public void setConf(AbstractConfig config) {
    this.config = (JsonRootNodeMutatorConfig) config;
  }
}
