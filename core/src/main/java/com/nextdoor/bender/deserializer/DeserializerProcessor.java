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

package com.nextdoor.bender.deserializer;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.NoSuchElementException;

import com.nextdoor.bender.monitoring.MonitoredProcess;
import com.nextdoor.bender.partition.PartitionSpec;
import com.nextdoor.bender.partition.PartitionSpec.Interpreter;

/**
 * Wrapper around {@link Deserializer} that keeps timing information on how long it takes to
 * deserialize events and handles error cases.
 */
public class DeserializerProcessor extends MonitoredProcess {
  private Deserializer deser;
  private final List<PartitionSpec> partitionSpecs;

  public DeserializerProcessor(Deserializer deserializer) {
    super(deserializer.getClass());
    this.deser = deserializer;
    this.partitionSpecs = this.deser.partitionSpecs;
    this.deser.init();
  }

  /**
   * Calls {@link com.nextdoor.bender.deserializer.Deserializer#deserialize(String)} and returns a
   * DeserializedEvent.
   *
   * @param eventString A plain text string which needs to be converted into a
   *        {@link DeserializedEvent}.
   * @return A DeserializedEvent if deserialization succeeded or null if it failed.
   * @throws DeserializationException if event is not able to be deserialized.
   */
  public DeserializedEvent deserialize(String eventString) throws DeserializationException {
    DeserializedEvent dEvent = null;
    this.getRuntimeStat().start();

    try {
      dEvent = this.deser.deserialize(eventString);
      this.getSuccessCountStat().increment();
    } catch (DeserializationException e) {
      this.getErrorCountStat().increment();
    } finally {
      this.getRuntimeStat().stop();
    }

    return dEvent;
  }

  /**
   * Called by the {@link com.nextdoor.bender.handler.BaseHandler} after an event has been
   * deserialized.
   *
   * @param dEvent event to find partitions from.
   * @return ordered map of partition key values.
   */
  protected LinkedHashMap<String, String> getEvaluatedPartitions(DeserializedEvent dEvent) {
    LinkedHashMap<String, String> partitions =
        new LinkedHashMap<String, String>(partitionSpecs.size());

    /*
     * A partition spec may contain an array of fields to look at. Find the first non-null one.
     */
    for (PartitionSpec partSpec : partitionSpecs) {
      if (partSpec.getInterpreter() != Interpreter.STATIC) {
        try {
          String value = null;
          for (String source : partSpec.getSources()) {
            value = dEvent.getField(source);
            if (value != null) {
              break;
            }
          }

          /*
           * Either a field was found or the partition value will be null
           */
          partitions.put(partSpec.getName(), partSpec.interpret(value));
        } catch (NoSuchElementException e) {
          partitions.put(partSpec.getName(), null);
        }
      } else {
        partitions.put(partSpec.getName(), partSpec.getFormat());
      }
    }

    return partitions;
  }

  public List<PartitionSpec> getPartitionSpecs() {
    return this.partitionSpecs;
  }

  public Deserializer getDeserializer() {
    return this.deser;
  }

  public void setDeserializer(Deserializer deserializer) {
    this.deser = deserializer;
  }
}
