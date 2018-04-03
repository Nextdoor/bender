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

package com.nextdoor.bender.operation.metadata;

import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.deserializer.DeserializedEvent;
import com.nextdoor.bender.handler.HandlerMetadata;
import com.nextdoor.bender.operation.Operation;

public class MetadataOperation implements Operation {
  String metadataField;

  public MetadataOperation(String metadataField) {
    this.metadataField = metadataField;
  }

  @Override
  public InternalEvent perform(InternalEvent ievent) {
    HandlerMetadata metadata = ievent.getMetadata();
    DeserializedEvent devent = ievent.getEventObj();
    devent.setField(metadataField, metadata.getPayload());
    return ievent;
  }
}
