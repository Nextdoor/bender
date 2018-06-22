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
 * Copyright 2018 Nextdoor.com, Inc
 *
 */

package com.nextdoor.bender.operation.substitution.formatted;

import org.apache.commons.text.ExtendedMessageFormat;
import com.nextdoor.bender.config.AbstractConfig;
import com.nextdoor.bender.operation.substitution.Substitution;
import com.nextdoor.bender.operation.substitution.SubstitutionFactory;

public class FormattedSubstitutionFactory implements SubstitutionFactory {

  private FormattedSubstitutionConfig config;
  private ExtendedMessageFormat format;

  @Override
  public void setConf(AbstractConfig config) {
    this.config = (FormattedSubstitutionConfig) config;
    this.format = new ExtendedMessageFormat(this.config.getFormat());
  }

  @Override
  public Class<?> getChildClass() {
    return FormattedSubstitution.class;
  }

  @Override
  public Substitution newInstance() {
    return new FormattedSubstitution(this.config.getKey(), this.format, this.config.getVariables(),
        this.config.getFailDstNotFound());
  }
}
