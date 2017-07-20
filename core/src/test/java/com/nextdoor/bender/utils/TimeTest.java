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

package com.nextdoor.bender.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TimeTest {

  @Test
  public void testFromSeconds() {
    assertEquals(1484802359000L, Time.toMilliseconds(1484802359L));
  }

  @Test
  public void testFromMilliseconds() {
    assertEquals(1484802359000L, Time.toMilliseconds(1484802359000L));
  }

  @Test
  public void testFromMicroseconds() {
    assertEquals(1484802359000L, Time.toMilliseconds(1484802359000000L));
  }

  @Test
  public void testFromNanoseconds() {
    assertEquals(1484802359000L, Time.toMilliseconds(1484802359000000000L));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalid() {
    assertEquals(1484802359000L, Time.toMilliseconds(1484802L));
  }
}
