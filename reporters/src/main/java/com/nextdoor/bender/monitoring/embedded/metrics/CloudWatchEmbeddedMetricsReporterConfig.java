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

package com.nextdoor.bender.monitoring.embedded.metrics;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDefault;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.nextdoor.bender.monitoring.RegionalReporterConfig;

@JsonTypeName("CloudWatchEmbeddedMetrics")
@JsonSchemaDescription("Writes metrics to Cloudwatch by printing to stdout. It is important to consider costs when "
        + "using this reporter see https://aws.amazon.com/cloudwatch/pricing/.")
public class CloudWatchEmbeddedMetricsReporterConfig extends RegionalReporterConfig {
    @JsonSchemaDefault("Nextdoor/bender")
    @JsonSchemaDescription("Cloudwatch namespace to write metrics under.")
    private String namespace = "Nextdoor/bender";

    @Override
    public Class<CloudWatchEmbeddedMetricsReporterFactory> getFactoryClass() {
        return CloudWatchEmbeddedMetricsReporterFactory.class;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
}
