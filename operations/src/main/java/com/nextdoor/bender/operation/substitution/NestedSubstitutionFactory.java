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

package com.nextdoor.bender.operation.substitution;

import java.util.ArrayList;
import java.util.List;
import com.nextdoor.bender.config.AbstractConfig;

public class NestedSubstitutionFactory implements SubstitutionFactory {
  private SubstitutionFactoryFactory sff = new SubstitutionFactoryFactory();
  private List<Substitution> substitutions;
  private NestedSubstitutionConfig config;

  @Override
  public void setConf(AbstractConfig config) {
    this.config = (NestedSubstitutionConfig) config;

    List<Substitution> substitutions =
        new ArrayList<Substitution>(this.config.getSubstitutions().size());

    for (SubstitutionConfig subConfig : this.config.getSubstitutions()) {
      try {
        substitutions.add(this.sff.getFactory(subConfig).newInstance());
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
    }

    this.substitutions = substitutions;
  }

  @Override
  public Class<?> getChildClass() {
    return NestedSubstitution.class;
  }

  @Override
  public Substitution newInstance() {
    return new NestedSubstitution(this.config.getKey(), this.substitutions,
        this.config.getFailDstNotFound());
  }
}
