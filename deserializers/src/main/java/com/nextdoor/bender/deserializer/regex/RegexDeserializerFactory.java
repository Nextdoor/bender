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

package com.nextdoor.bender.deserializer.regex;

import com.nextdoor.bender.config.AbstractConfig;
import com.nextdoor.bender.deserializer.Deserializer;
import com.nextdoor.bender.deserializer.DeserializerFactory;

public class RegexDeserializerFactory implements DeserializerFactory {

  private RegexDeserializerConfig config;

  @Override
  public void setConf(AbstractConfig config) {
    this.config = (RegexDeserializerConfig) config;
  }

  @Override
  public Class<?> getChildClass() {
    return RegexDeserializer.class;
  }

  @Override
  public Deserializer newInstance() {
    if (this.config.isUseRe2j()) {
      com.google.re2j.Pattern p =
          com.google.re2j.Pattern.compile(this.config.getRegex(), com.google.re2j.Pattern.DOTALL);
      return new Re2jRegexDeserializer(p, this.config.getFields());
    } else {
      java.util.regex.Pattern p =
          java.util.regex.Pattern.compile(this.config.getRegex(), java.util.regex.Pattern.DOTALL);
      return new RegexDeserializer(p, this.config.getFields());
    }
  }
}
