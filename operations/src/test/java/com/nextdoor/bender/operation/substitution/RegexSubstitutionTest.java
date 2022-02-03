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
import java.util.List;
import java.util.regex.Pattern;
import org.junit.Test;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.deserializer.FieldNotFoundException;
import com.nextdoor.bender.operation.OperationException;
import com.nextdoor.bender.operation.substitution.regex.RegexSubstitution;
import com.nextdoor.bender.operation.substitution.regex.RegexSubstitutionConfig.RegexSubField;
import com.nextdoor.bender.testutils.DummyDeserializerHelper.DummpyMapEvent;


public class RegexSubstitutionTest {
  @Test
  public void testBasicRegex() throws FieldNotFoundException {
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
    
    ArrayList<Substitution> substitutions = new ArrayList<>();
    substitutions.add(new RegexSubstitution(Arrays.asList("url"), Pattern.compile(pattern), regexSubFields, false, true, true));

    DummpyMapEvent devent = new DummpyMapEvent();
    devent.setField("url", "https://www.example.com:443/p1/p2/index.html?q=abc");

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    SubstitutionOperation op = new SubstitutionOperation(substitutions);
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
  public void testNonMatchingRegex() throws FieldNotFoundException {
    List<RegexSubField> regexSubFields =
        Arrays.asList(new RegexSubField("q", RegexSubField.RegexSubFieldType.STRING, "q"));
    String pattern = "(?<q>(expectedstring))";
    
    ArrayList<Substitution> substitutions = new ArrayList<>();
    substitutions.add(new RegexSubstitution(Arrays.asList("foo"), Pattern.compile(pattern), regexSubFields, false, false, true));

    DummpyMapEvent devent = new DummpyMapEvent();
    devent.setField("foo", "actualstring");

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    SubstitutionOperation op = new SubstitutionOperation(substitutions);
    op.perform(ievent);

    /*
     * Original field should still be there
     */
    assertEquals(1, devent.payload.size());
    assertEquals("actualstring", devent.getField("foo"));
  }

// TODO: do this somewhere else
//  @Test(expected = java.util.regex.PatternSyntaxException.class)
//  public void testInvalidRegex() {
//    String pattern = "(?q>(expectedstring))";
//    new RegexSubSpecConfig(Arrays.asList("foo"), pattern, null, false, true, true);
//  }

  @Test
  public void testRegexRemoveField() throws FieldNotFoundException {
    List<RegexSubField> regexSubFields =
        Arrays.asList(new RegexSubField("q", RegexSubField.RegexSubFieldType.STRING, "q"));
    String pattern = "(?<q>(expectedstring))";
    
    ArrayList<Substitution> substitutions = new ArrayList<>();
    substitutions.add(new RegexSubstitution(Arrays.asList("foo"), Pattern.compile(pattern), regexSubFields, true, true, true));

    DummpyMapEvent devent = new DummpyMapEvent();
    devent.setField("foo", "expectedstring");

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    SubstitutionOperation op = new SubstitutionOperation(substitutions);
    op.perform(ievent);

    assertEquals(1, devent.payload.size());
    assertEquals("expectedstring", devent.getField("q"));
  }

  @Test
  public void testRegexRemoveFieldReplace() throws FieldNotFoundException {
    List<RegexSubField> regexSubFields =
        Arrays.asList(new RegexSubField("foo", RegexSubField.RegexSubFieldType.STRING, "foo"));
    String pattern = "(?<foo>(expectedstring))";

    ArrayList<Substitution> substitutions = new ArrayList<>();
    substitutions.add(new RegexSubstitution(Arrays.asList("foo"), Pattern.compile(pattern), regexSubFields, true, true, true));

    DummpyMapEvent devent = new DummpyMapEvent();
    devent.setField("foo", "expectedstring");

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    SubstitutionOperation op = new SubstitutionOperation(substitutions);
    op.perform(ievent);

    assertEquals(1, devent.payload.size());
    assertEquals("expectedstring", devent.getField("foo"));
  }

  @Test
  public void testRegexFieldCoercionNoFail() throws FieldNotFoundException {
    List<RegexSubField> regexSubFields =
        Arrays.asList(new RegexSubField("q", RegexSubField.RegexSubFieldType.NUMBER, "q"));
    String pattern = "(?<q>(expectedstring))";
    
    ArrayList<Substitution> substitutions = new ArrayList<>();
    substitutions.add(new RegexSubstitution(Arrays.asList("foo"), Pattern.compile(pattern), regexSubFields, false, false, true));

    DummpyMapEvent devent = new DummpyMapEvent();
    devent.setField("foo", "expectedstring");

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    SubstitutionOperation op = new SubstitutionOperation(substitutions);
    op.perform(ievent);

    assertEquals(1, devent.payload.size());
    assertEquals("expectedstring", devent.getField("foo"));
  }

  @Test
  public void testRegexFieldCoercionRemove() throws FieldNotFoundException {
    List<RegexSubField> regexSubFields =
        Arrays.asList(new RegexSubField("q", RegexSubField.RegexSubFieldType.NUMBER, "q"));
    String pattern = "(?<q>(expectedstring))";
    
    ArrayList<Substitution> substitutions = new ArrayList<>();
    substitutions.add(new RegexSubstitution(Arrays.asList("foo"), Pattern.compile(pattern), regexSubFields, true, false, true));

    DummpyMapEvent devent = new DummpyMapEvent();
    devent.setField("foo", "expectedstring");

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    SubstitutionOperation op = new SubstitutionOperation(substitutions);
    op.perform(ievent);

    assertEquals(1, devent.payload.size());
    assertEquals("expectedstring", devent.getField("foo"));
  }

  @Test(expected = OperationException.class)
  public void testRegexFieldCoercionSrcNotFound() throws FieldNotFoundException {
    List<RegexSubField> regexSubFields =
        Arrays.asList(new RegexSubField("q", RegexSubField.RegexSubFieldType.NUMBER, "q"));
    String pattern = "(?<q>(expectedstring))";

    ArrayList<Substitution> substitutions = new ArrayList<>();
    substitutions.add(new RegexSubstitution(Arrays.asList("foo"), Pattern.compile(pattern), regexSubFields, false, true, true));

    DummpyMapEvent devent = new DummpyMapEvent();
    devent.setField("foo", "expectedstring");

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    SubstitutionOperation op = new SubstitutionOperation(substitutions);
    op.perform(ievent);
  }

  @Test
  public void testRegexMultipleSources() throws FieldNotFoundException {
    List<RegexSubField> regexSubFields =
        Arrays.asList(new RegexSubField("q", RegexSubField.RegexSubFieldType.STRING, "q"));
    String pattern = "(?<q>(expectedstring))";
    
    ArrayList<Substitution> substitutions = new ArrayList<>();
    substitutions.add(new RegexSubstitution(Arrays.asList("foo", "foo1", "foo2"), Pattern.compile(pattern), regexSubFields, false, true, true));


    DummpyMapEvent devent = new DummpyMapEvent();
    devent.setField("foo2", "expectedstring");

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    SubstitutionOperation op = new SubstitutionOperation(substitutions);
    op.perform(ievent);

    assertEquals(2, devent.payload.size());
    assertEquals("expectedstring", devent.getField("foo2"));
    assertEquals("expectedstring", devent.getField("q"));
  }

  @Test
  public void testRegexMultipleSourcesNonMatch() throws FieldNotFoundException {
    List<RegexSubField> regexSubFields =
        Arrays.asList(new RegexSubField("q", RegexSubField.RegexSubFieldType.STRING, "q"));
    String pattern = "(?<q>(\\d+))";
    
    ArrayList<Substitution> substitutions = new ArrayList<>();
    substitutions.add(new RegexSubstitution(Arrays.asList("foo", "foo1"), Pattern.compile(pattern), regexSubFields, false, true, true));


    DummpyMapEvent devent = new DummpyMapEvent();
    devent.setField("foo", "aaa");
    devent.setField("foo1", "123");

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    SubstitutionOperation op = new SubstitutionOperation(substitutions);
    op.perform(ievent);

    assertEquals(3, devent.payload.size());
    assertEquals("123", devent.getField("q"));
  }

  @Test
  public void testRegexSrcNotFoundRemove() throws FieldNotFoundException {
    List<RegexSubField> regexSubFields =
        Arrays.asList(new RegexSubField("q", RegexSubField.RegexSubFieldType.STRING, "q"));
    String pattern = "(?<q>(\\d+))";
    
    ArrayList<Substitution> substitutions = new ArrayList<>();
    substitutions.add(new RegexSubstitution(Arrays.asList("foo", "foo1"), Pattern.compile(pattern), regexSubFields, false, false, true));


    DummpyMapEvent devent = new DummpyMapEvent();
    devent.setField("foo", "aaa");
    devent.setField("foo1", "bbb");

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    SubstitutionOperation op = new SubstitutionOperation(substitutions);
    op.perform(ievent);

    assertEquals(2, devent.payload.size());
  }

  @Test(expected = OperationException.class)
  public void testRegexSrcNotFoundFail() throws FieldNotFoundException {
    List<RegexSubField> regexSubFields =
        Arrays.asList(new RegexSubField("q", RegexSubField.RegexSubFieldType.STRING, "q"));

    String pattern = "(?<q>(\\d+))";
    
    ArrayList<Substitution> substitutions = new ArrayList<>();
    substitutions.add(new RegexSubstitution(Arrays.asList("foo", "foo1"), Pattern.compile(pattern), regexSubFields, false, true, true));


    DummpyMapEvent devent = new DummpyMapEvent();
    devent.setField("foo", "aaa");
    devent.setField("foo1", "bbb");

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    SubstitutionOperation op = new SubstitutionOperation(substitutions);
    op.perform(ievent);
  }
}
