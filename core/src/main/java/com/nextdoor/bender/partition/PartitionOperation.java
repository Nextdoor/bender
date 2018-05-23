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

package com.nextdoor.bender.partition;

import java.util.LinkedHashMap;
import java.util.List;

import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.deserializer.DeserializedEvent;
import com.nextdoor.bender.deserializer.FieldNotFoundException;
import com.nextdoor.bender.operation.EventOperation;
import com.nextdoor.bender.operation.OperationException;

public class PartitionOperation implements EventOperation {
  private final List<PartitionSpec> partitionSpecs;

  public PartitionOperation(final List<PartitionSpec> partitionSpecs) {
    this.partitionSpecs = partitionSpecs;
  }


  /**
   * Provided a PartitionSpec this method attempts to retrieve each field from the deserialized
   * event object.
   *
   * @param devent deserialized event to extract partitions from
   */
  protected LinkedHashMap<String, String> getPartitions(DeserializedEvent devent) {
    int numPartSpecs = partitionSpecs.size();

    /*
     * Loop through each partition spec fetching the associated field from the event. Set to null if
     * field does not exist.
     */
    LinkedHashMap<String, String> partitions = new LinkedHashMap<String, String>(numPartSpecs);
    for (PartitionSpec spec : partitionSpecs) {
      String key = null;
      for (String source : spec.getSources()) {
        try {
          key = devent.getFieldAsString(source);
          if (key != null) {
            break;
          }
        } catch (FieldNotFoundException e) {
          continue;
        }
      }

      partitions.put(spec.getName(), spec.interpret(key));
    }

    return partitions;
  }

  @Override
  public InternalEvent perform(InternalEvent ievent) {
    ievent.setPartitions(getPartitions(ievent.getEventObj()));
    return ievent;
  }
}
