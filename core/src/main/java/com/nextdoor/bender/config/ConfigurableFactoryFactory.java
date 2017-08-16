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

package com.nextdoor.bender.config;

import org.apache.log4j.Logger;

import com.nextdoor.bender.utils.ReflectionUtils;

/**
 * Helper class that allows for ConfigurableFactory to be loaded.
 *
 * @param <T> ConfigurableFactor.
 */
public class ConfigurableFactoryFactory<T extends ConfigurableFactory> {
  private static final Logger logger = Logger.getLogger(ConfigurableFactoryFactory.class);

  @SuppressWarnings("unchecked")
  public T getFactory(ConfigurableFactoryConfig<?> factoryConfig) throws ClassNotFoundException {
    ConfigurableFactory factory =
        (ConfigurableFactory) ReflectionUtils.newInstance(factoryConfig.getFactoryClass());
    factory.setConf(factoryConfig);

    logger.trace(factory.getClass().getName() + " initialized");
    return (T) factory;
  }
}
