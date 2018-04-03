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

package com.nextdoor.bender.operation.gelf;

import java.util.ArrayList;

import com.nextdoor.bender.config.AbstractConfig;
import com.nextdoor.bender.operation.OperationFactory;
import com.nextdoor.bender.operation.substitution.SubstitutionSpec;
import com.nextdoor.bender.operation.substitution.SubstitutionSpec.Interpreter;

/**
 * Create a {@link GelfOperation} with GELF defaults.
 */
public class GelfOperationFactory implements OperationFactory {
  private GelfOperationConfig config;
  private ArrayList<SubstitutionSpec> subSpecs;

  @Override
  public GelfOperation newInstance() {
    return new GelfOperation(this.subSpecs);
  }

  @Override
  public Class<GelfOperation> getChildClass() {
    return GelfOperation.class;
  }

  @Override
  public void setConf(AbstractConfig config) {
    this.config = (GelfOperationConfig) config;

    ArrayList<SubstitutionSpec> subSpecs = new ArrayList<SubstitutionSpec>();
    this.subSpecs = subSpecs;

    subSpecs.add(new SubstitutionSpec("version", "1.1", Interpreter.STATIC));

    if (this.config.getSrcHostField() != null) {
      subSpecs.add(new SubstitutionSpec("host", this.config.getSrcHostField(), Interpreter.FIELD));
    }

    if (this.config.getSrcShortMessageField() != null) {
      subSpecs.add(new SubstitutionSpec("short_message", this.config.getSrcShortMessageField(),
          Interpreter.FIELD));
    }

    if (this.config.getSrcFullMessageField() != null) {
      subSpecs.add(new SubstitutionSpec("full_message", this.config.getSrcFullMessageField(),
          Interpreter.FIELD));
    }

    if (this.config.getSrcTimestampField() != null) {
      subSpecs.add(
          new SubstitutionSpec("timestamp", this.config.getSrcTimestampField(), Interpreter.FIELD));
    }

    if (this.config.getSrcLevelField() != null) {
      subSpecs
          .add(new SubstitutionSpec("level", this.config.getSrcLevelField(), Interpreter.FIELD));
    }


    if (this.config.getSrcFacilityField() != null) {
      subSpecs.add(
          new SubstitutionSpec("facility", this.config.getSrcFacilityField(), Interpreter.FIELD));
    }

    if (this.config.getSrcLineNumberField() != null) {
      subSpecs.add(
          new SubstitutionSpec("line", this.config.getSrcLineNumberField(), Interpreter.FIELD));
    }

    if (this.config.getSrcFileField() != null) {
      subSpecs.add(new SubstitutionSpec("file", this.config.getSrcFileField(), Interpreter.FIELD));
    }
  }
}
