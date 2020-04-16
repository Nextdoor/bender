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

package com.nextdoor.bender.deserializer;

import java.nio.ByteBuffer;

public abstract class Deserializer {
  /**
   * @param raw original event string.
   * @return the deserialized version of the event.
   */
  public abstract DeserializedEvent deserialize(String raw);

  public abstract DeserializedEvent deserialize(ByteBuffer raw);

  /**
   * Performs any deserializer initialization that needs to be done.
   */
  public abstract void init();

  public abstract boolean getIsZipped();
}
