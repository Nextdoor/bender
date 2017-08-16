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

package com.nextdoor.bender.ipc.es;

import java.util.List;

/**
 * POJO used to interpret ES json responses to bulk api calls.
 */
public class EsResponse {
  public int took;
  public boolean errors;
  public List<Item> items;

  public static class Item {
    public Index index;
  }

  public static class Index {
    public String _index;
    public String _type;
    public String _id;
    public int _version;
    public int status;
    public Error error;
    public Shards _shards;
  }

  public static class Error {
    public String type;
    public String reason;
    public Cause caused_by;
  }

  public static class Cause {
    public String type;
    public String reason;
  }

  public static class Shards {
    public int total;
    public int successful;
    public int failed;
  }
}
