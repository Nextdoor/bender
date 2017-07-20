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

package com.nextdoor.bender.wrapper.basic;


import com.nextdoor.bender.config.AbstractConfig;
import com.nextdoor.bender.wrapper.Wrapper;
import com.nextdoor.bender.wrapper.WrapperFactory;

/**
 * Creates a {@link BasicWrapper}.
 */
public class BasicWrapperFactory implements WrapperFactory {

  private BasicWrapperConfig config;

  @Override
  public Wrapper newInstance() {
    return new BasicWrapper();
  }

  @Override
  public void setConf(AbstractConfig<?> config) {
    this.config = (BasicWrapperConfig) config;
  }

  @Override
  public Class<?> getChildClass() {
    return BasicWrapper.class;
  }
}
