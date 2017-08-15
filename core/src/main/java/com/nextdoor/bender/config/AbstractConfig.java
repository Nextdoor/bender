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

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY,
    property = "type")
public abstract class AbstractConfig<T> {
  private String type;

  @JsonProperty("type")
  public String getType() {
    return type;
  }

  @JsonProperty("type")
  public void getType(String type) {
    this.type = type;
  }

  public static Class<?>[] getSubtypes(Class clazz) {
    Set<Class<? extends AbstractConfig>> classSet = new HashSet();
    FastClasspathScanner s = new FastClasspathScanner();
    s.scan().getNamesOfSubclassesOf(clazz).forEach(c -> {
      try {
        classSet.add((Class<? extends AbstractConfig>) Class.forName(c));
      } catch (ClassNotFoundException e) {
      }
    });

    /*
     * Remove abstract classes as they are not allowed in the mapper
     */
    classSet.removeIf(p -> Modifier.isAbstract(p.getModifiers()) == true);

    return classSet.toArray(new Class<?>[classSet.size()]);
  }
}
