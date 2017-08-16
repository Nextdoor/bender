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

package com.nextdoor.bender.serializer.json;

import com.nextdoor.bender.config.AbstractConfig;
import com.nextdoor.bender.serializer.Serializer;
import com.nextdoor.bender.serializer.SerializerFactory;

/**
 * Creates a {@link JsonSerializer}.
 */
public class JsonSerializerFactory implements SerializerFactory {

  public JsonSerializerConfig config;

  @Override
  public Serializer newInstance() {
    return new JsonSerializer(config.getFieldNamingPolicy());
  }

  @Override
  public void setConf(AbstractConfig config) {
    this.config = (JsonSerializerConfig) config;
  }

  @Override
  public Class<JsonSerializer> getChildClass() {
    return JsonSerializer.class;
  }
}
