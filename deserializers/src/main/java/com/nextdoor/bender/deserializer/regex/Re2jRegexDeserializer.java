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

package com.nextdoor.bender.deserializer.regex;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.re2j.Matcher;
import com.google.re2j.Pattern;
import com.nextdoor.bender.deserializer.DeserializationException;
import com.nextdoor.bender.deserializer.DeserializedEvent;
import com.nextdoor.bender.deserializer.Deserializer;

public class Re2jRegexDeserializer extends Deserializer {
  private final Pattern pattern;
  private final List<ReFieldConfig> fields;

  public Re2jRegexDeserializer(final Pattern pattern, final List<ReFieldConfig> fields) {
    this.pattern = pattern;
    this.fields = fields;
  }

  @Override
  public DeserializedEvent deserialize(String raw) {
    Matcher m = this.pattern.matcher(raw);

    if (!m.matches()) {
      throw new DeserializationException("raw event does not match string");
    }

    int groups = m.groupCount();
    Map<String, Object> mapping = new HashMap<>(groups);
    for (int i = 0; i < groups && i < fields.size(); i++) {
      String str = m.group(i + 1);

      ReFieldConfig field = this.fields.get(i);
      mapping.put(field.getName(), RegexDeserializer.parse(str, field.getType()));
    }

    return new RegexEvent(mapping);
  }

  @Override
  public void init() {

  }
}
