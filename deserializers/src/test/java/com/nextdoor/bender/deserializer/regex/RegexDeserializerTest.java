/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF
 * ANY KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * Copyright 2018 Nextdoor.com, Inc
 */

package com.nextdoor.bender.deserializer.regex;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.Test;

import com.nextdoor.bender.deserializer.DeserializationException;
import com.nextdoor.bender.deserializer.DeserializedEvent;
import com.nextdoor.bender.deserializer.regex.ReFieldConfig.ReFieldType;

public class RegexDeserializerTest {

  @Test
  public void testSingleMatch() {
    List<ReFieldConfig> fields = new ArrayList<>();
    fields.add(new ReFieldConfig("foo", ReFieldType.STRING));

    Pattern p = Pattern.compile("(.*)");
    RegexDeserializer deser = new RegexDeserializer(p, fields);
    DeserializedEvent event = deser.deserialize("test i am");

    assertEquals("test i am", event.getField("foo"));
  }

  @Test(expected = DeserializationException.class)
  public void testNoMatches() {
    List<ReFieldConfig> fields = new ArrayList<>();
    fields.add(new ReFieldConfig("foo", ReFieldType.STRING));

    Pattern p = Pattern.compile("(test i am)");
    RegexDeserializer deser = new RegexDeserializer(p, fields);
    DeserializedEvent event = deser.deserialize("i am a test");
  }

  @Test
  public void testTestAlbLog() {
    List<ReFieldConfig> fields = new ArrayList<>();
    fields.add(new ReFieldConfig("type", ReFieldType.STRING));
    fields.add(new ReFieldConfig("timestamp", ReFieldType.STRING));
    fields.add(new ReFieldConfig("elb", ReFieldType.STRING));
    fields.add(new ReFieldConfig("client_ip", ReFieldType.STRING));
    fields.add(new ReFieldConfig("client_port", ReFieldType.NUMBER));
    fields.add(new ReFieldConfig("target_ip", ReFieldType.STRING));
    fields.add(new ReFieldConfig("target_port", ReFieldType.NUMBER));
    fields.add(new ReFieldConfig("request_processing_time", ReFieldType.NUMBER));
    fields.add(new ReFieldConfig("target_processing_time", ReFieldType.NUMBER));
    fields.add(new ReFieldConfig("response_processing_time", ReFieldType.NUMBER));
    fields.add(new ReFieldConfig("elb_status_code", ReFieldType.NUMBER));
    fields.add(new ReFieldConfig("target_status_code", ReFieldType.NUMBER));
    fields.add(new ReFieldConfig("received_bytes", ReFieldType.NUMBER));
    fields.add(new ReFieldConfig("sent_bytes", ReFieldType.NUMBER));
    fields.add(new ReFieldConfig("request_verb", ReFieldType.STRING));
    fields.add(new ReFieldConfig("url", ReFieldType.STRING));
    fields.add(new ReFieldConfig("protocol", ReFieldType.STRING));
    fields.add(new ReFieldConfig("user_agent", ReFieldType.STRING));
    fields.add(new ReFieldConfig("ssl_cipher", ReFieldType.STRING));
    fields.add(new ReFieldConfig("ssl_protocol", ReFieldType.STRING));
    fields.add(new ReFieldConfig("target_group_arn", ReFieldType.STRING));
    fields.add(new ReFieldConfig("trace_id", ReFieldType.STRING));

    Pattern p = Pattern.compile(
        "([^ ]*) ([^ ]*) ([^ ]*) ([^ ]*):([0-9]*) ([^ ]*):([0-9]*) ([.0-9]*) ([.0-9]*) ([.0-9]*) (-|[0-9]*) (-|[0-9]*) ([-0-9]*) ([-0-9]*) \\\"([^ ]*) ([^ ]*) (- |[^ ]*)\\\" (\\\"[^\\\"]*\\\") ([A-Z0-9-]+) ([A-Za-z0-9.-]*) ([^ ]*) ([^ ]*)$",
        Pattern.DOTALL);
    RegexDeserializer deser = new RegexDeserializer(p, fields);
    DeserializedEvent event = deser.deserialize(
        "https 2017-06-15T04:55:00.142369Z app/foo/1234 127.12.12.12:1337 127.13.13.13:7331 1.001 2.002 3.003 201 200 687 461 \"GET https://foo.com:443/bar/123?baz=1 HTTP/1.1\" \"123-123-123-123-123\" ECDHE-RSA-AES128-GCM-SHA256 TLSv1.2 arn:aws:elasticloadbalancing:us-west-1:123:targetgroup/foo-bar-https/123 \"Root=1-123-123\"");

    assertEquals("https", event.getField("type"));
    assertEquals("2017-06-15T04:55:00.142369Z", event.getField("timestamp"));
    assertEquals("app/foo/1234", event.getField("elb"));
    assertEquals("127.12.12.12", event.getField("client_ip"));
    assertEquals("1337", event.getField("client_port"));
    assertEquals("127.13.13.13", event.getField("target_ip"));
    assertEquals("7331", event.getField("target_port"));
    assertEquals("1.001", event.getField("request_processing_time"));
    assertEquals("2.002", event.getField("target_processing_time"));
    assertEquals("3.003", event.getField("response_processing_time"));
    assertEquals("201", event.getField("elb_status_code"));
    assertEquals("200", event.getField("target_status_code"));
    assertEquals("687", event.getField("received_bytes"));
    assertEquals("461", event.getField("sent_bytes"));
    assertEquals("GET", event.getField("request_verb"));
    assertEquals("https://foo.com:443/bar/123?baz=1", event.getField("url"));
    assertEquals("HTTP/1.1", event.getField("protocol"));
    assertEquals("\"123-123-123-123-123\"", event.getField("user_agent"));
    assertEquals("ECDHE-RSA-AES128-GCM-SHA256", event.getField("ssl_cipher"));
    assertEquals("TLSv1.2", event.getField("ssl_protocol"));
    assertEquals("arn:aws:elasticloadbalancing:us-west-1:123:targetgroup/foo-bar-https/123",
        event.getField("target_group_arn"));
    assertEquals("\"Root=1-123-123\"", event.getField("trace_id"));
  }
}
