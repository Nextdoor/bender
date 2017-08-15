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

package com.nextdoor.bender.ipc.file;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.nextdoor.bender.ipc.TransportConfig;

@JsonTypeName("File")
@JsonSchemaDescription("Writes events to a file on the local file system. Typically only used for "
    + "testing and debugging.")
public class FileTransportConfig extends TransportConfig {

  @JsonSchemaDescription("Local filesystem file to write data to")
  @JsonProperty(required = true)
  private String filename;

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  @Override
  public Class<FileTransportFactory> getFactoryClass() {
    return FileTransportFactory.class;
  }
}
