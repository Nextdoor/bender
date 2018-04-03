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

package com.nextdoor.bender.wrapper.s3;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.nextdoor.bender.wrapper.WrapperConfig;

@JsonTypeName("S3Wrapper")
@JsonSchemaDescription("Adds information about function name, function version, processing "
    + "time (in ms), processing delay (in ms), processing timestsamp (ms since epoch), sha1 "
    + "hash of the original event, and s3 key, bucket, and key version from the source S3"
    + "file read. Only works with the S3Handler and SNSS3Handler.")
public class S3WrapperConfig extends WrapperConfig {

  @Override
  public Class<S3WrapperFactory> getFactoryClass() {
    return S3WrapperFactory.class;
  }
}
