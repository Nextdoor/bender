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

package com.nextdoor.bender.utils;

public class Time {
  public static long toMilliseconds(long ts) throws IllegalArgumentException {
    /*
     * Sanity check just in case event time format is different from user specified format.
     */
    int length = (int) (Math.log10(ts) + 1);
    switch (length) {
      case 10: // seconds
        return ts * 1000;
      case 13: // milliseconds
        return ts;
      case 16: // microseconds
        return ts / 1000;
      case 19: // nanoseconds
        return ts / 1000000;
      default:
        throw new IllegalArgumentException("Timestamp is out of valid range");
    }
  }
}
