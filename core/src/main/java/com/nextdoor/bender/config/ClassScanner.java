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

package com.nextdoor.bender.config;

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.lukehutch.fastclasspathscanner.scanner.ScanResult;

public class ClassScanner {

  public static Set<Class> getSubtypes(List<Class> clazzes)
      throws InterruptedException, ExecutionException {
    Set<Class> classSet = new HashSet<Class>();
    int threads = Runtime.getRuntime().availableProcessors();
    ExecutorService pool = Executors.newFixedThreadPool(threads);
    Future<ScanResult> futureScan =
        new FastClasspathScanner("com.nextdoor.bender").scanAsync(pool, threads);
    ScanResult s = futureScan.get();

    for (Class<? extends AbstractConfig<?>> clazz : clazzes) {
      s.getNamesOfSubclassesOf(clazz).forEach(c -> {
        try {
          classSet.add((Class<? extends AbstractConfig<?>>) Class.forName(c));
        } catch (ClassNotFoundException e) {
        }
      });
    }
    pool.shutdown();

    /*
     * Remove abstract classes as they are not allowed in the mapper
     */
    classSet.removeIf(p -> Modifier.isAbstract(p.getModifiers()) == true);

    return classSet;
  }
}
