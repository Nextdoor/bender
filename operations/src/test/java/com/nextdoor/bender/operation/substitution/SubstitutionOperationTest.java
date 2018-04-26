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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.junit.Test;

import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.LambdaContext;
import com.nextdoor.bender.aws.TestContext;
import com.nextdoor.bender.operation.substitution.RegexSubSpecConfig.RegexSubField;
import com.nextdoor.bender.testutils.DummyDeserializerHelper.DummpyMapEvent;

public class SubstitutionOperationTest {

  @Test
  public void testKnownField() {
    ArrayList<SubSpecConfig<?>> subSpecs = new ArrayList<SubSpecConfig<?>>();
    subSpecs.add(new FieldSubSpecConfig("bar", Arrays.asList("foo"), false));

    DummpyMapEvent devent = new DummpyMapEvent();
    devent.setField("foo", "1234");

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    SubstitutionOperation op = new SubstitutionOperation(subSpecs);
    op.perform(ievent);

    assertEquals("1234", devent.getField("bar"));
    assertEquals("1234", devent.getField("foo"));
  }

  @Test
  public void testRemoveField() {
    ArrayList<SubSpecConfig<?>> subSpecs = new ArrayList<SubSpecConfig<?>>();
    subSpecs.add(new FieldSubSpecConfig("bar", Arrays.asList("foo"), true));

    DummpyMapEvent devent = new DummpyMapEvent();
    devent.setField("foo", "1234");

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    SubstitutionOperation op = new SubstitutionOperation(subSpecs);
    op.perform(ievent);

    assertEquals("1234", devent.getField("bar"));
    assertEquals(null, devent.getField("foo"));
  }

  @Test
  public void testUnknownField() {
    ArrayList<SubSpecConfig<?>> subSpecs = new ArrayList<SubSpecConfig<?>>();
    subSpecs.add(new FieldSubSpecConfig("bar", Arrays.asList("foo"), false));

    DummpyMapEvent devent = new DummpyMapEvent();

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    SubstitutionOperation op = new SubstitutionOperation(subSpecs);
    op.perform(ievent);

    assertEquals(null, devent.getField("bar"));
  }

  @Test
  public void testFieldList() {
    ArrayList<SubSpecConfig<?>> subSpecs = new ArrayList<SubSpecConfig<?>>();
    subSpecs.add(new FieldSubSpecConfig("bar", Arrays.asList("foo0", "foo1", "foo2"), false));

    DummpyMapEvent devent = new DummpyMapEvent();
    devent.setField("foo2", "1234");

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    SubstitutionOperation op = new SubstitutionOperation(subSpecs);
    op.perform(ievent);

    assertEquals("1234", devent.getField("bar"));
    assertEquals("1234", devent.getField("foo2"));
  }

  @Test
  public void testStaticField() {
    ArrayList<SubSpecConfig<?>> subSpecs = new ArrayList<SubSpecConfig<?>>();
    subSpecs.add(new StaticSubSpecConfig("foo", "1234"));

    DummpyMapEvent devent = new DummpyMapEvent();

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    SubstitutionOperation op = new SubstitutionOperation(subSpecs);
    op.perform(ievent);

    assertEquals("1234", devent.getField("foo"));
  }

  @Test
  public void testExcludeMetadata() {
    ArrayList<SubSpecConfig<?>> subSpecs = new ArrayList<SubSpecConfig<?>>();
    subSpecs.add(
        new MetadataSubSpecConfig("foo", Collections.emptyList(), Arrays.asList("sourceLagMs")));

    DummpyMapEvent devent = new DummpyMapEvent();

    InternalEvent ievent = new InternalEvent("", null, 10);
    ievent.setEventObj(devent);
    ievent.setEventTime(20);

    SubstitutionOperation op = new SubstitutionOperation(subSpecs);
    op.perform(ievent);

    Map<String, Object> expected = new HashMap<String, Object>() {
      {
        put("arrivalEpochMs", new Long(10));
        put("eventSha1Hash", "da39a3ee5e6b4b0d3255bfef95601890afd80709");
        put("eventEpochMs", new Long(20));
      }
    };

    assertEquals(expected, devent.getField("foo"));
  }

  @Test
  public void testIncludeMetadata() {
    ArrayList<SubSpecConfig<?>> subSpecs = new ArrayList<SubSpecConfig<?>>();
    subSpecs.add(
        new MetadataSubSpecConfig("foo", Arrays.asList("eventSha1Hash"), Collections.emptyList()));

    DummpyMapEvent devent = new DummpyMapEvent();

    InternalEvent ievent = new InternalEvent("", null, 10);
    ievent.setEventObj(devent);
    ievent.setEventTime(20);

    SubstitutionOperation op = new SubstitutionOperation(subSpecs);
    op.perform(ievent);

    Map<String, Object> expected = new HashMap<String, Object>() {
      {
        put("eventSha1Hash", "da39a3ee5e6b4b0d3255bfef95601890afd80709");
      }
    };

    assertEquals(expected, devent.getField("foo"));
  }

  @Test
  public void testExcludesContext() {
    ArrayList<SubSpecConfig<?>> subSpecs = new ArrayList<SubSpecConfig<?>>();
    subSpecs.add(
        new ContextSubSpecConfig("foo", Collections.emptyList(), Arrays.asList("functionName")));

    DummpyMapEvent devent = new DummpyMapEvent();
    TestContext ctx = new TestContext();
    ctx.setFunctionName("fun name");
    ctx.setInvokedFunctionArn("some arn");

    InternalEvent ievent = new InternalEvent("", new LambdaContext(ctx), 10);
    ievent.setEventObj(devent);
    ievent.setEventTime(20);

    SubstitutionOperation op = new SubstitutionOperation(subSpecs);
    op.perform(ievent);

    Map<String, Object> expected = new HashMap<String, Object>() {
      {
        put("invokedFunctionArn", "some arn");
      }
    };

    assertEquals(expected, devent.getField("foo"));
  }

  @Test
  public void testIncludesContext() {
    ArrayList<SubSpecConfig<?>> subSpecs = new ArrayList<SubSpecConfig<?>>();
    subSpecs.add(
        new ContextSubSpecConfig("foo", Arrays.asList("functionName"), Collections.emptyList()));

    DummpyMapEvent devent = new DummpyMapEvent();
    TestContext ctx = new TestContext();
    ctx.setFunctionName("fun name");
    ctx.setInvokedFunctionArn("some arn");

    InternalEvent ievent = new InternalEvent("", new LambdaContext(ctx), 10);
    ievent.setEventObj(devent);
    ievent.setEventTime(20);

    SubstitutionOperation op = new SubstitutionOperation(subSpecs);
    op.perform(ievent);

    Map<String, Object> expected = new HashMap<String, Object>() {
      {
        put("functionName", "fun name");
      }
    };

    assertEquals(expected, devent.getField("foo"));
  }

  @Test
  public void testBasicNested() {
    ArrayList<SubSpecConfig<?>> nestedSubSpecs = new ArrayList<SubSpecConfig<?>>();
    nestedSubSpecs.add(new FieldSubSpecConfig("bar", Arrays.asList("foo0", "foo1", "foo2"), false));
    nestedSubSpecs.add(new StaticSubSpecConfig("static", "value"));


    ArrayList<SubSpecConfig<?>> subSpecs = new ArrayList<SubSpecConfig<?>>();
    subSpecs.add(new NestedSubSpecConfig("a", nestedSubSpecs));

    DummpyMapEvent devent = new DummpyMapEvent();
    devent.setField("foo2", "1234");

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    SubstitutionOperation op = new SubstitutionOperation(subSpecs);
    op.perform(ievent);

    Map<String, Object> expectedNested = new HashMap<String, Object>() {
      {
        put("bar", "1234");
        put("static", "value");
      }
    };

    assertEquals(expectedNested, devent.getField("a"));
    assertEquals("1234", devent.getField("foo2"));
  }

  @Test
  public void testNestedNested() {
    /*
     * Expected output {a={b={bar=1234, static=value}}, foo2=1234}
     */
    ArrayList<SubSpecConfig<?>> nest2SubSpecs = new ArrayList<SubSpecConfig<?>>();
    nest2SubSpecs.add(new FieldSubSpecConfig("bar", Arrays.asList("foo0", "foo1", "foo2"), false));
    nest2SubSpecs.add(new StaticSubSpecConfig("static", "value"));

    ArrayList<SubSpecConfig<?>> nest1SubSpecs = new ArrayList<SubSpecConfig<?>>();
    nest1SubSpecs.add(new NestedSubSpecConfig("b", nest2SubSpecs));

    ArrayList<SubSpecConfig<?>> subSpecs = new ArrayList<SubSpecConfig<?>>();
    subSpecs.add(new NestedSubSpecConfig("a", nest1SubSpecs));

    DummpyMapEvent devent = new DummpyMapEvent();
    devent.setField("foo2", "1234");

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    SubstitutionOperation op = new SubstitutionOperation(subSpecs);
    op.perform(ievent);


    Map<String, Object> expectedNest2 = new HashMap<String, Object>() {
      {
        put("bar", "1234");
        put("static", "value");
      }
    };

    Map<String, Object> expectedNest1 = new HashMap<String, Object>() {
      {
        put("b", expectedNest2);
      }
    };

    assertEquals(expectedNest1, devent.getField("a"));
    assertEquals("1234", devent.getField("foo2"));
  }


  @Test
  public void testBasicRegex() {
    ArrayList<SubSpecConfig<?>> subSpecs = new ArrayList<SubSpecConfig<?>>();
    List<RegexSubField> regexSubFields = Arrays.asList(
        new RegexSubField("protocol", RegexSubField.RegexSubFieldType.STRING, "http_protocol"),
        new RegexSubField("host", RegexSubField.RegexSubFieldType.STRING, "http_host"),
        new RegexSubField("port", RegexSubField.RegexSubFieldType.NUMBER, "http_port"),
        new RegexSubField("path", RegexSubField.RegexSubFieldType.STRING, "http_path"),
        new RegexSubField("page", RegexSubField.RegexSubFieldType.STRING, "http_page"),
        new RegexSubField("args", RegexSubField.RegexSubFieldType.STRING, "http_args"));

    String pattern = "(?:(?<protocol>http[s]):\\/\\/)?"
        + "(?<host>((?:www.)?(?:[^\\W\\s]|\\.|-)+[\\.][^\\W\\s]{2,4}|localhost(?=\\/)|\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}))"
        + "(?::(?<port>\\d*))?" + "(?<path>([\\/]?[^\\s\\?]*[\\/]{1})*)"
        + "(?<page>(?:\\/?([^\\s\\n\\?\\[\\]\\{\\}\\#]*(?:(?=\\.)){1}|[^\\s\\n\\?\\[\\]\\{\\}\\.\\#]*)?)([\\.]{1}[^\\s\\?\\#]*)?)?"
        + "(?<args>(?:\\?{1}([^\\s\\n\\#\\[\\]]*))?)";

    subSpecs.add(new RegexSubSpecConfig(Arrays.asList("url"), pattern, regexSubFields, false));

    DummpyMapEvent devent = new DummpyMapEvent();
    devent.setField("url", "https://www.example.com:443/p1/p2/index.html?q=abc");

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    SubstitutionOperation op = new SubstitutionOperation(subSpecs);
    op.perform(ievent);

    assertEquals("https://www.example.com:443/p1/p2/index.html?q=abc", devent.getField("url"));
    assertEquals("https", devent.getField("http_protocol"));
    assertEquals("www.example.com", devent.getField("http_host"));
    assertEquals(443, devent.getField("http_port"));
    assertEquals("/p1/p2/", devent.getField("http_path"));
    assertEquals("index.html", devent.getField("http_page"));
    assertEquals("?q=abc", devent.getField("http_args"));
  }

  @Test
  public void testNonMatchingRegex() {
    ArrayList<SubSpecConfig<?>> subSpecs = new ArrayList<SubSpecConfig<?>>();
    List<RegexSubField> regexSubFields =
        Arrays.asList(new RegexSubField("q", RegexSubField.RegexSubFieldType.STRING, "q"));

    String pattern = "(?<q>(expectedstring))";

    subSpecs.add(new RegexSubSpecConfig(Arrays.asList("foo"), pattern, regexSubFields, false));

    DummpyMapEvent devent = new DummpyMapEvent();
    devent.setField("foo", "actualstring");

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    SubstitutionOperation op = new SubstitutionOperation(subSpecs);
    op.perform(ievent);

    assertEquals(1, devent.payload.size());
    assertEquals(null, devent.getField("q"));
  }

  @Test(expected = java.util.regex.PatternSyntaxException.class)
  public void testInvalidRegex() {
    String pattern = "(?q>(expectedstring))";
    new RegexSubSpecConfig(Arrays.asList("foo"), pattern, null, false);
  }

  @Test
  public void testRegexRemoveField() {
    ArrayList<SubSpecConfig<?>> subSpecs = new ArrayList<SubSpecConfig<?>>();
    List<RegexSubField> regexSubFields =
        Arrays.asList(new RegexSubField("q", RegexSubField.RegexSubFieldType.STRING, "q"));

    String pattern = "(?<q>(expectedstring))";

    subSpecs.add(new RegexSubSpecConfig(Arrays.asList("foo"), pattern, regexSubFields, true));

    DummpyMapEvent devent = new DummpyMapEvent();
    devent.setField("foo", "expectedstring");

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    SubstitutionOperation op = new SubstitutionOperation(subSpecs);
    op.perform(ievent);

    assertEquals(1, devent.payload.size());
    assertEquals(null, devent.getField("foo"));
    assertEquals("expectedstring", devent.getField("q"));
  }

  @Test
  public void testRegexFieldCoercionDrop() {
    ArrayList<SubSpecConfig<?>> subSpecs = new ArrayList<SubSpecConfig<?>>();
    List<RegexSubField> regexSubFields =
        Arrays.asList(new RegexSubField("q", RegexSubField.RegexSubFieldType.NUMBER, "q"));

    String pattern = "(?<q>(expectedstring))";

    subSpecs.add(new RegexSubSpecConfig(Arrays.asList("foo"), pattern, regexSubFields, false));

    DummpyMapEvent devent = new DummpyMapEvent();
    devent.setField("foo", "expectedstring");

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    SubstitutionOperation op = new SubstitutionOperation(subSpecs);
    op.perform(ievent);

    assertEquals(1, devent.payload.size());
    assertEquals("expectedstring", devent.getField("foo"));
  }

  @Test
  public void testRegexMultipleSources() {
    ArrayList<SubSpecConfig<?>> subSpecs = new ArrayList<SubSpecConfig<?>>();
    List<RegexSubField> regexSubFields =
        Arrays.asList(new RegexSubField("q", RegexSubField.RegexSubFieldType.STRING, "q"));

    String pattern = "(?<q>(expectedstring))";

    subSpecs.add(new RegexSubSpecConfig(Arrays.asList("foo", "foo1", "foo2"), pattern,
        regexSubFields, false));

    DummpyMapEvent devent = new DummpyMapEvent();
    devent.setField("foo2", "expectedstring");

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    SubstitutionOperation op = new SubstitutionOperation(subSpecs);
    op.perform(ievent);

    assertEquals(2, devent.payload.size());
    assertEquals("expectedstring", devent.getField("foo2"));
    assertEquals("expectedstring", devent.getField("q"));
  }

  @Test
  public void testRegexMultipleSourcesNonMatch() {
    ArrayList<SubSpecConfig<?>> subSpecs = new ArrayList<SubSpecConfig<?>>();
    List<RegexSubField> regexSubFields =
        Arrays.asList(new RegexSubField("q", RegexSubField.RegexSubFieldType.STRING, "q"));

    String pattern = "(?<q>(\\d+))";

    subSpecs
        .add(new RegexSubSpecConfig(Arrays.asList("foo", "foo1"), pattern, regexSubFields, false));

    DummpyMapEvent devent = new DummpyMapEvent();
    devent.setField("foo", "aaa");
    devent.setField("foo1", "123");

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    SubstitutionOperation op = new SubstitutionOperation(subSpecs);
    op.perform(ievent);

    assertEquals(3, devent.payload.size());
    assertEquals("123", devent.getField("q"));
  }
}
