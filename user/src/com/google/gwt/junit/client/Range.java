/*
 * Copyright 2007 Google Inc.
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
package com.google.gwt.junit.client;

import java.util.Iterator;

/**
 * A range of values for a Benchmark parameter.
 *
 * A Range produces an Iterator that contains all of the values that a Benchmark
 * parameter should be tested over.
 *
 * Range is unlikely to provide any extra semantics above what you would get
 * with java.util.Iterable, but it was introduced before GWT's JDK 1.5 support.
 *
 */
public interface Range {
  Iterator iterator();
}
