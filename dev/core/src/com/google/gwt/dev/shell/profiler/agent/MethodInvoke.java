/*
 * Copyright 2006 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.dev.shell.profiler.agent;

/**
 * Aggregate statistics about invocations for a method. We roll up all
 * invocations for a single method type when they share the same parent.
 *
 */
public class MethodInvoke {
    String klass;
    String name;
    String signature;
    long lastEntryTimeNanos;
    long aggregateExecutionTimeNanos;
    long numInvocations = 1;

    public MethodInvoke( String klass, String name, String signature, long entryTimeNanos ) {
      this.klass = klass;
      this.name = name;
      this.signature = signature;
      this.lastEntryTimeNanos = entryTimeNanos;
    }

    public String getKey() {
      return klass + "." + name + signature;
    }
}
