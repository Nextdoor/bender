package com.nextdoor.bender.partition;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.NoSuchElementException;

import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.deserializer.DeserializedEvent;
import com.nextdoor.bender.operation.Operation;

public class PartitionOperation implements Operation {
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
          key = devent.getField(source);
          if (key != null) {
            break;
          }
        } catch (NoSuchElementException e) {
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
