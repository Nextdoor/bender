package com.nextdoor.bender.testutils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.amazonaws.services.lambda.runtime.events.KinesisEvent;
import com.amazonaws.services.lambda.runtime.events.KinesisEvent.KinesisEventRecord;
import com.amazonaws.services.lambda.runtime.events.KinesisEvent.Record;

/**
 * Common test helper methods that are used by other modules.
 */
public class TestUtils {

  public static KinesisEvent createEvent(Class clazz, String resource) throws IOException {
    return createEvent(clazz, resource, "arn:aws:kinesis:us-east-1:1234:stream/test-events-stream");
  }

  public static KinesisEvent createEvent(Class clazz, String resource, String eventSourceArn) throws IOException {
    /*
     * Create a kinesis record from a sample JSON file
     */
    String json =
        IOUtils.toString(new InputStreamReader(clazz.getResourceAsStream(resource), "UTF-8"));

    Date approximateArrivalTimestamp = new Date();
    approximateArrivalTimestamp.setTime(1478737790000l);

    Record rec = new Record();
    rec.withPartitionKey("1").withSequenceNumber("2").withData(ByteBuffer.wrap(json.getBytes()))
        .withApproximateArrivalTimestamp(approximateArrivalTimestamp);

    /*
     * Create a KinesisEventRecord and add single Record
     */
    KinesisEventRecord krecord = new KinesisEventRecord();
    krecord.setKinesis(rec);
    krecord.setEventSourceARN(eventSourceArn);
    krecord.setEventID("shardId-000000000000:1234");

    /*
     * Add single KinesisEventRecord to a KinesisEvent
     */
    KinesisEvent kevent = new KinesisEvent();
    List<KinesisEventRecord> events = new ArrayList<KinesisEventRecord>(1);
    events.add(krecord);
    kevent.setRecords(events);

    return kevent;
  }

  public static String getResourceString(Class clazz, String resource)
      throws UnsupportedEncodingException, IOException {
    return IOUtils.toString(new InputStreamReader(clazz.getResourceAsStream(resource), "UTF-8"));
  }
}
