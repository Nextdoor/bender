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

package com.nextdoor.bender.operation.decode;

import java.util.List;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.URLCodec;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.deserializer.FieldNotFoundException;
import com.nextdoor.bender.operation.EventOperation;
import com.nextdoor.bender.operation.OperationException;

/**
 * URL Decodes specified fields.
 */
public class URLDecodeOperation implements EventOperation {
  public static final URLCodec codec = new URLCodec();
  private final List<String> fieldNames;
  private final int times;

  public URLDecodeOperation(List<String> fieldNames, int times) {
    this.fieldNames = fieldNames;
    this.times = times;
  }

  @Override
  public InternalEvent perform(InternalEvent ievent) {
    if (ievent == null || ievent.getEventObj() == null
        || ievent.getEventObj().getPayload() == null) {
      return ievent;
    }

    for (String fieldName : fieldNames) {
      /*
       * Get field value
       */
      String value = null;
      try {
        value = ievent.getEventObj().getFieldAsString(fieldName);
      } catch (FieldNotFoundException e) {
        continue;
      }

      if (value == null) {
        return ievent;
      }

      /*
       * Perform decode multiple times if required.
       */
      try {
        for (int i = 0; i < this.times; i++) {
          value = codec.decode(value);
        }
      } catch (DecoderException e) {
        continue;
      }

      /*
       * Update value
       */
      try {
        ievent.getEventObj().setField(fieldName, value);
      } catch (FieldNotFoundException e) {
        throw new OperationException("failed to update original field in URL decode");
      }
    }

    return ievent;
  }
}
