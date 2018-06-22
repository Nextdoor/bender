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

package com.nextdoor.bender.operation.gelf;

import java.util.ArrayList;
import java.util.List;
import com.nextdoor.bender.config.AbstractConfig;
import com.nextdoor.bender.operation.OperationFactory;
import com.nextdoor.bender.operation.substitution.Substitution;
import com.nextdoor.bender.operation.substitution.SubstitutionConfig;
import com.nextdoor.bender.operation.substitution.SubstitutionFactoryFactory;
import com.nextdoor.bender.operation.substitution.field.FieldSubstitutionConfig;
import com.nextdoor.bender.operation.substitution.ztatic.StaticSubstitutionConfig;

/**
 * Create a {@link GelfOperation} with GELF defaults.
 */
public class GelfOperationFactory implements OperationFactory {
  private GelfOperationConfig config;
  private List<Substitution> substitutions;
  private ArrayList<SubstitutionConfig> subConfigs;

  @Override
  public GelfOperation newInstance() {
    return new GelfOperation(this.substitutions);
  }

  @Override
  public Class<GelfOperation> getChildClass() {
    return GelfOperation.class;
  }

  @Override
  public void setConf(AbstractConfig config) {
    this.config = (GelfOperationConfig) config;

    ArrayList<SubstitutionConfig> subConfigs = new ArrayList<SubstitutionConfig>();
    subConfigs.add(new StaticSubstitutionConfig("version", "1.1", true));

    if (this.config.getSrcHostField() != null) {
      subConfigs.add(
          new FieldSubstitutionConfig("host", this.config.getSrcHostField(), false, true, true));
    }

    if (this.config.getSrcShortMessageField() != null) {
      subConfigs.add(new FieldSubstitutionConfig("short_message",
          this.config.getSrcShortMessageField(), false, true, true));
    }

    if (this.config.getSrcFullMessageField() != null) {
      subConfigs.add(new FieldSubstitutionConfig("full_message",
          this.config.getSrcFullMessageField(), false, false, true));
    }

    if (this.config.getSrcTimestampField() != null) {
      subConfigs.add(new FieldSubstitutionConfig("timestamp", this.config.getSrcTimestampField(),
          false, false, true));
    }

    if (this.config.getSrcLevelField() != null) {
      subConfigs.add(
          new FieldSubstitutionConfig("level", this.config.getSrcLevelField(), false, false, true));
    }

    if (this.config.getSrcFacilityField() != null) {
      subConfigs.add(new FieldSubstitutionConfig("facility", this.config.getSrcFacilityField(),
          false, false, true));
    }

    if (this.config.getSrcLineNumberField() != null) {
      subConfigs.add(new FieldSubstitutionConfig("line", this.config.getSrcLineNumberField(), false,
          false, true));
    }

    if (this.config.getSrcFileField() != null) {
      subConfigs.add(
          new FieldSubstitutionConfig("file", this.config.getSrcFileField(), false, false, true));
    }

    List<Substitution> substitutions = new ArrayList<Substitution>(subConfigs.size());
    SubstitutionFactoryFactory sff = new SubstitutionFactoryFactory();

    for (SubstitutionConfig subConfig : subConfigs) {
      try {
        substitutions.add(sff.getFactory(subConfig).newInstance());
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
    }

    this.substitutions = substitutions;
    this.subConfigs = subConfigs;
  }

  protected List<SubstitutionConfig> getSubConfigs() {
    return this.subConfigs;
  }
}
