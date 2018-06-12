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

package com.nextdoor.bender.monitoring;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.util.collections.Sets;

import com.nextdoor.bender.testutils.DummyReporterHelper.DummyReporter;

public class MonitorTest {
  @Before
  public void before() {
    MonitorFactory mf = new MonitorFactory();
    Monitor monitor = mf.getInstance();
    monitor.reset();
  }

  @Test
  public void testStatFilterName() {
    MonitorFactory mf = new MonitorFactory();
    Monitor monitor = mf.getInstance();

    StatFilter filter = new StatFilter();
    filter.setName("bender.test.fail");
    List<StatFilter> filters = new ArrayList<StatFilter>(1);
    filters.add(filter);

    DummyReporter reporter = new DummyReporter(filters);
    monitor.addReporters(Arrays.asList(reporter));

    Stat statFail = new Stat("bender.test.fail");
    Stat statPass = new Stat("bender.test.pass");

    monitor.addInvocationStat(statFail);
    monitor.addInvocationStat(statPass);

    monitor.writeStats();

    assertEquals(1, reporter.buffer.size());
    assertEquals(true, reporter.buffer.contains("bender.test.pass  0"));
    assertEquals(false, reporter.buffer.contains("bender.test.fail  0"));
  }

  @Test
  public void testStatFilterTags() {
    MonitorFactory mf = new MonitorFactory();
    Monitor monitor = mf.getInstance();

    StatFilter filter = new StatFilter();
    filter.setName("bender.test.tags");
    filter.setTags(Sets.newSet(new Tag("foo", "3")));
    List<StatFilter> filters = new ArrayList<StatFilter>(1);
    filters.add(filter);

    DummyReporter reporter = new DummyReporter(filters);
    monitor.addReporters(Arrays.asList(reporter));

    Stat statFail = new Stat("bender.test.tags");
    statFail.addTag("foo", "3");
    Stat statPass = new Stat("bender.test.tags");
    statPass.addTag("bar", "4");

    monitor.addInvocationStat(statFail);
    monitor.addInvocationStat(statPass);

    monitor.writeStats();

    assertEquals(1, reporter.buffer.size());
    assertEquals(true, reporter.buffer.contains("bender.test.tags bar:4 0"));
    assertEquals(false, reporter.buffer.contains("bender.test.tags foo:3 0"));
  }

  @Test
  public void testMultipleTags() {
    MonitorFactory mf = new MonitorFactory();
    Monitor monitor = mf.getInstance();

    List<StatFilter> filters = new ArrayList<StatFilter>(1);

    DummyReporter reporter = new DummyReporter(filters);
    monitor.addReporters(Arrays.asList(reporter));

    Stat stat = new Stat("bender.test.tags");
    stat.addTag("foo", "3");
    stat.addTag("foo", "3");
    stat.addTag("foo", "4");

    monitor.addInvocationStat(stat);

    monitor.writeStats();

    assertEquals(1, reporter.buffer.size());
    assertEquals(true, reporter.buffer.contains("bender.test.tags foo:3 0"));
  }

  @Test
  public void testEmptyFilter() {
    MonitorFactory mf = new MonitorFactory();
    Monitor monitor = mf.getInstance();

    List<StatFilter> filters = new ArrayList<StatFilter>(1);

    DummyReporter reporter = new DummyReporter(filters);
    monitor.addReporters(Arrays.asList(reporter));

    Stat statPass1 = new Stat("bender.test.pass1");
    statPass1.addTag("foo", "3");
    Stat statPass2 = new Stat("bender.test.pass2");
    statPass2.addTag("foo", "4");

    monitor.addInvocationStat(statPass1);
    monitor.addInvocationStat(statPass2);

    monitor.writeStats();

    assertEquals(2, reporter.buffer.size());
    assertEquals(true, reporter.buffer.contains("bender.test.pass1 foo:3 0"));
    assertEquals(true, reporter.buffer.contains("bender.test.pass2 foo:4 0"));
  }

  @Test
  public void testFilterZeros() {
    MonitorFactory mf = new MonitorFactory();
    Monitor monitor = mf.getInstance();

    StatFilter filter = new StatFilter();
    filter.setReportZeros(false);
    List<StatFilter> filters = new ArrayList<StatFilter>(1);
    filters.add(filter);

    DummyReporter reporter = new DummyReporter(filters);
    monitor.addReporters(Arrays.asList(reporter));

    Stat statFail = new Stat("bender.test.fail");
    statFail.setValue(0);
    Stat statPass = new Stat("bender.test.pass");
    statPass.setValue(1);

    monitor.addInvocationStat(statFail);
    monitor.addInvocationStat(statPass);

    monitor.writeStats();

    assertEquals(1, reporter.buffer.size());
    assertEquals(true, reporter.buffer.contains("bender.test.pass  1"));
    assertEquals(false, reporter.buffer.contains("bender.test.fail  0"));
  }

  @Test
  public void testCompoundFilters() {
    MonitorFactory mf = new MonitorFactory();
    Monitor monitor = mf.getInstance();

    StatFilter filterFooZero = new StatFilter();
    filterFooZero.setName("bender.test.foo");
    filterFooZero.setReportZeros(false);

    StatFilter filterBarTags = new StatFilter();
    filterBarTags.setName("bender.test.bar");
    filterBarTags.setReportZeros(true);
    filterBarTags.setTags(Sets.newSet(new Tag("t0", "7")));

    List<StatFilter> filters = new ArrayList<StatFilter>(2);
    filters.add(filterFooZero);
    filters.add(filterBarTags);

    DummyReporter reporter = new DummyReporter(filters);
    monitor.addReporters(Arrays.asList(reporter));

    Stat s1 = new Stat("bender.test.foo");
    s1.setValue(0);
    Stat s2 = new Stat("bender.test.foo");
    s1.setValue(5);
    Stat s3 = new Stat("bender.test.bar");
    s3.addTag("t0", "7");
    Stat s4 = new Stat("bender.test.bar");
    s4.addTag("t1", "3");

    monitor.addInvocationStat(s1);
    monitor.addInvocationStat(s2);
    monitor.addInvocationStat(s3);
    monitor.addInvocationStat(s4);

    monitor.writeStats();

    assertEquals(2, reporter.buffer.size());
    assertEquals(false, reporter.buffer.contains("bender.test.foo  0"));
    assertEquals(true, reporter.buffer.contains("bender.test.foo  5"));
    assertEquals(false, reporter.buffer.contains("bender.test.bar t0:7 0"));
    assertEquals(true, reporter.buffer.contains("bender.test.bar t1:3 0"));
  }
}
