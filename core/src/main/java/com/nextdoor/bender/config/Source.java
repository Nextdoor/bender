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
 * Copyright 2016 Nextdoor.com, Inc
 *
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
import com.nextdoor.bender.mutator.MutatorConfig;
import com.nextdoor.bender.mutator.MutatorFactoryFactory;
import com.nextdoor.bender.mutator.MutatorProcessor;

public class Source {
  private final String sourceName;
  private final Pattern sourceRegex;
  private DeserializerProcessor deserProcessor;
  private List<MutatorProcessor> mutatorProcessors = new ArrayList<MutatorProcessor>(0);
  private List<Pattern> regexPatterns = new ArrayList<Pattern>(0);
  private List<String> containsStrings = new ArrayList<String>(0);

  private final DeserializerFactoryFactory dff = new DeserializerFactoryFactory();

  public Source(SourceConfig config) throws ClassNotFoundException {
    this.sourceRegex = Pattern.compile(config.getSourceRegex());
    this.sourceName = config.getName();

    DeserializerFactory dFactory = dff.getFactory(config.getDeserializerConfig());

    this.deserProcessor = new DeserializerProcessor(dFactory.newInstance());

    List<MutatorConfig> mutatorConfigs = config.getMutatorConfigs();
    if (mutatorConfigs.size() > 0) {
      MutatorFactoryFactory mff = new MutatorFactoryFactory();
      for (MutatorConfig mutatorConfig : mutatorConfigs) {
    	  this.mutatorProcessors.add(new MutatorProcessor(mff.getFactory(mutatorConfig)));
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

  public List<MutatorProcessor> getMutatorProcessors() {
    return this.mutatorProcessors;
  }

  public void setMutatorProcessors(List<MutatorProcessor> mutatorProcessors) {
    this.mutatorProcessors = mutatorProcessors;
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

    String mutators = this.mutatorProcessors.stream().map(c -> {
      return c.getMutator().getClass().getSimpleName();
    }).collect(Collectors.joining(", "));

    return this.sourceName + "[" + "sourceRegex=" + this.sourceRegex
        + ", containsStrings=["+ StringUtils.join(this.containsStrings, ',')
        + "], regexPatterns=[" + patterns + "]"
        + "], deserializers=[" + this.deserProcessor + "]"
        + "], mutators=[" +mutators + "]]";
  }
}
