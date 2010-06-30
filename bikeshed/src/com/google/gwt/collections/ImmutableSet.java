/*
 * Copyright 2010 Google Inc.
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
package com.google.gwt.collections;

/**
 * A set that is guaranteed not to change, thus making it safe for disparate
 * portions of code to maintain references to a shared instance, rather than
 * feeling the need to make defensive copies.
 * 
 * @param <E> the type stored in the set elements
 */
public abstract class ImmutableSet<E> extends Set<E> {
  
  @SuppressWarnings("unchecked")
  private static final ImmutableSet EMPTY = new ImmutableEmptySetImpl();
  
  /**
   * Provides an empty set of type {@code T}.
   */
  @SuppressWarnings("unchecked")
  static <T> ImmutableSet<T> getEmptyInstance() {
    return EMPTY;
  }

}
