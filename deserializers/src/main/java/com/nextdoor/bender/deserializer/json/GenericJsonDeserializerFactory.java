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

package com.nextdoor.bender.deserializer.json;

import com.nextdoor.bender.config.AbstractConfig;
import com.nextdoor.bender.deserializer.Deserializer;
import com.nextdoor.bender.deserializer.DeserializerFactory;

/**
 * Builds a {@link GenericJsonDeserializer} from a {@link AbstractConfig}.
 */
public class GenericJsonDeserializerFactory implements DeserializerFactory {

  private GenericJsonDeserializerConfig config;

  @Override
  public Deserializer newInstance() {
    return new GenericJsonDeserializer(this.config.getNestedFieldConfigs(),
            this.config.getRootNodeOverridePath(),
            this.config.getPerformBase64DecodeAndExpandGzip(),
            this.config.getBufferSize());
  }

  @Override
  public Class<?> getChildClass() {
    return GenericJsonDeserializer.class;
  }

  @Override
  public void setConf(AbstractConfig config) {
    this.config = (GenericJsonDeserializerConfig) config;
  }
}
