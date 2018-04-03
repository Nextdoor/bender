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

package com.nextdoor.bender.config;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.nextdoor.bender.deserializer.DeserializerFactory;
import com.nextdoor.bender.deserializer.DeserializerFactoryFactory;
import com.nextdoor.bender.deserializer.DeserializerProcessor;
import com.nextdoor.bender.operation.OperationConfig;
import com.nextdoor.bender.operation.OperationFactoryFactory;
import com.nextdoor.bender.operation.OperationProcessor;

public class Source {
  private final String sourceName;
  private final Pattern sourceRegex;
  private DeserializerProcessor deserProcessor;
  private List<OperationProcessor> operationProcessors = new ArrayList<OperationProcessor>(0);
  private List<Pattern> regexPatterns = new ArrayList<Pattern>(0);
  private List<String> containsStrings = new ArrayList<String>(0);

  private final DeserializerFactoryFactory dff = new DeserializerFactoryFactory();

  public Source(SourceConfig config) throws ClassNotFoundException {
    this.sourceRegex = Pattern.compile(config.getSourceRegex());
    this.sourceName = config.getName();

    DeserializerFactory dFactory = dff.getFactory(config.getDeserializerConfig());

    this.deserProcessor = new DeserializerProcessor(dFactory.newInstance());

    List<OperationConfig> operationConfigs = config.getOperationConfigs();
    if (operationConfigs.size() > 0) {
      OperationFactoryFactory off = new OperationFactoryFactory();
      for (OperationConfig operationConfig : operationConfigs) {
        this.operationProcessors.add(new OperationProcessor(off.getFactory(operationConfig)));
      }
    }

    this.containsStrings.addAll(config.getContainsStrings());
    for (String strRegex : config.getRegexPatterns()) {
      this.regexPatterns.add(Pattern.compile(strRegex));
    }
  }

  public DeserializerProcessor getDeserProcessor() {
    return this.deserProcessor;
  }

  public void setDeserProcessor(DeserializerProcessor deserProcessor) {
    this.deserProcessor = deserProcessor;
  }

  public String getSourceName() {
    return this.sourceName;
  }

  public Pattern getSourceRegex() {
    return this.sourceRegex;
  }

  public List<OperationProcessor> getOperationProcessors() {
    return this.operationProcessors;
  }

  public void setOperationProcessors(List<OperationProcessor> operationProcessors) {
    this.operationProcessors = operationProcessors;
  }

  public List<String> getContainsStrings() {
    return this.containsStrings;
  }

  public List<Pattern> getRegexPatterns() {
    return this.regexPatterns;
  }

  public String toString() {
    String patterns = this.regexPatterns.stream().map(c -> {
      return c.toString();
    }).collect(Collectors.joining(", "));

    String operations = this.operationProcessors.stream().map(c -> {
      return c.getOperation().getClass().getSimpleName();
    }).collect(Collectors.joining(", "));

    return this.sourceName + "[" + "sourceRegex=" + this.sourceRegex + ", containsStrings=["
        + StringUtils.join(this.containsStrings, ',') + "], regexPatterns=[" + patterns + "]"
        + "], deserializers=[" + this.deserProcessor + "]" + "], operations=[" + operations + "]]";
  }
}
