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
import com.nextdoor.bender.config.AbstractConfig;
import com.nextdoor.bender.operation.OperationFactory;
import com.nextdoor.bender.operation.substitution.FieldSubSpecConfig;
import com.nextdoor.bender.operation.substitution.StaticSubSpecConfig;
import com.nextdoor.bender.operation.substitution.SubSpecConfig;

/**
 * Create a {@link GelfOperation} with GELF defaults.
 */
public class GelfOperationFactory implements OperationFactory {
  private GelfOperationConfig config;
  private ArrayList<SubSpecConfig<?>> subSpecs;

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

    ArrayList<SubSpecConfig<?>> subSpecs = new ArrayList<SubSpecConfig<?>>();
    this.subSpecs = subSpecs;

    subSpecs.add(new StaticSubSpecConfig("version", "1.1", true));

    if (this.config.getSrcHostField() != null) {
      subSpecs
          .add(new FieldSubSpecConfig("host", this.config.getSrcHostField(), false, true, true));
    }

    if (this.config.getSrcShortMessageField() != null) {
      subSpecs.add(new FieldSubSpecConfig("short_message", this.config.getSrcShortMessageField(),
          false, true, true));
    }

    if (this.config.getSrcFullMessageField() != null) {
      subSpecs.add(new FieldSubSpecConfig("full_message", this.config.getSrcFullMessageField(),
          false, false, true));
    }

    if (this.config.getSrcTimestampField() != null) {
      subSpecs.add(new FieldSubSpecConfig("timestamp", this.config.getSrcTimestampField(), false,
          false, true));
    }

    if (this.config.getSrcLevelField() != null) {
      subSpecs
          .add(new FieldSubSpecConfig("level", this.config.getSrcLevelField(), false, false, true));
    }

    if (this.config.getSrcFacilityField() != null) {
      subSpecs.add(new FieldSubSpecConfig("facility", this.config.getSrcFacilityField(), false,
          false, true));
    }

    if (this.config.getSrcLineNumberField() != null) {
      subSpecs.add(
          new FieldSubSpecConfig("line", this.config.getSrcLineNumberField(), false, false, true));
    }

    if (this.config.getSrcFileField() != null) {
      subSpecs
          .add(new FieldSubSpecConfig("file", this.config.getSrcFileField(), false, false, true));
    }
  }
}
