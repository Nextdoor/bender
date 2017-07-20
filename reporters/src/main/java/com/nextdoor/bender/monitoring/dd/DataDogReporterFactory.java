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

package com.nextdoor.bender.monitoring.dd;

import com.nextdoor.bender.config.AbstractConfig;
import com.nextdoor.bender.monitoring.Reporter;
import com.nextdoor.bender.monitoring.ReporterFactory;

/**
 * Crate a {@link DataDogReporter}.
 */
public class DataDogReporterFactory implements ReporterFactory {
  private DataDogReporterConfig config;

  @Override
  public Reporter newInstance() {
    return new DataDogReporter(this.config.getPrefix(), this.config.getStatFilters());
  }

  @Override
  public void setConf(AbstractConfig config) {
    this.config = (DataDogReporterConfig) config;
  }

  @Override
  public Class<DataDogReporter> getChildClass() {
    return DataDogReporter.class;
  }
}
