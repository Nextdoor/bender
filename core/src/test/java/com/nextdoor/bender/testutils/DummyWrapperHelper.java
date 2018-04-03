/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF
 * ANY KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * Copyright 2018 Nextdoor.com, Inc
 */

package com.nextdoor.bender.testutils;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.config.AbstractConfig;
import com.nextdoor.bender.wrapper.Wrapper;
import com.nextdoor.bender.wrapper.WrapperConfig;
import com.nextdoor.bender.wrapper.WrapperFactory;

public class DummyWrapperHelper {
  public static class DummyWrapper implements Wrapper {
    public Object getWrapped(final InternalEvent internal) {
      if (internal == null || internal.getEventObj() == null
          || internal.getEventObj().getPayload() == null) {
        return null;
      } else {
        return internal.getEventObj().getPayload();
      }
    }
  }

  public static class DummyWrapperFactory implements WrapperFactory {
    @Override
    public void setConf(AbstractConfig<?> config) {}

    @Override
    public Wrapper newInstance() {
      return new DummyWrapper();
    }

    @Override
    public Class<?> getChildClass() {
      return DummyWrapper.class;
    }
  }

  @JsonTypeName("DummyWrapperHelper$DummyWrapperConfig")
  public static class DummyWrapperConifg extends WrapperConfig {

    @Override
    public Class<DummyWrapperFactory> getFactoryClass() {
      return DummyWrapperFactory.class;
    }
  }
}
