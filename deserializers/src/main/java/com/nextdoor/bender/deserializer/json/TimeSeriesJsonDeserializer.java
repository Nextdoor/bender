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

package com.nextdoor.bender.deserializer.json;

import java.util.List;

import com.nextdoor.bender.deserializer.DeserializationException;
import com.nextdoor.bender.deserializer.DeserializedEvent;
import com.nextdoor.bender.deserializer.json.AbstractJsonDeserializerConfig.FieldConfig;
import com.nextdoor.bender.deserializer.json.TimeSeriesJsonEvent.TimeFieldType;
import com.nextdoor.bender.partition.PartitionSpec;

/**
 * Similar to the {@link GenericJsonDeserializer} but creates a {@link TimeSeriesJsonEvent}.
 */
public class TimeSeriesJsonDeserializer extends GenericJsonDeserializer {
  private final String timeField;
  private final TimeFieldType timeFieldType;

  public TimeSeriesJsonDeserializer(List<PartitionSpec> partitionSpecs,
      List<FieldConfig> nestedFieldConfigs, String rootNodeOverridePath, String timeField,
      TimeFieldType timeFieldType) {
    super(partitionSpecs, nestedFieldConfigs, rootNodeOverridePath);
    this.timeField = timeField;
    this.timeFieldType = timeFieldType;
  }

  @Override
  public DeserializedEvent deserialize(String raw) {
    try {
      return new TimeSeriesJsonEvent((GenericJsonEvent) super.deserialize(raw), this.timeField,
          this.timeFieldType);
    } catch (NullPointerException e) {
      throw new DeserializationException("unable to find time field");
    } catch (NumberFormatException e) {
      throw new DeserializationException("time field was not the expected type");
    }
  }
}
