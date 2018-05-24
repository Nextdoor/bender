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

package com.nextdoor.bender.operation.filter;

import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.operation.FilterOperation;
import com.nextdoor.bender.operation.OperationException;

public class BasicFilterOperation implements FilterOperation  {
  final boolean pass;

  public BasicFilterOperation(boolean pass) {
    this.pass  = pass;
  }

  @Override
  public boolean test(InternalEvent ievent) throws OperationException {
    return this.pass;
  }
}
