/*
 * Copyright 2009 Google Inc.
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
 * Shared assertions and related messages.
 */
class Assertions {
  
  /**
   * Message produced when asserting that access into a set or map  uses a value
   * not in the domain. 
   */
  static final String ACCESS_UNSUPPORTED_VALUE = "Unsupported value";
  
  /**
   * Message for asserting that access to an empty array is an illegal operation.
   */
  public static final String ACCESS_EMPTY_ARRAY_MESSAGE = 
    "Attempt to access an element in an empty array";

  /**
   * Message for asserting that adapters can only be set inly once after 
   * creation. 
   */
  public static final String INIT_ADAPTER_TWICE = "Attempt to call " +
    "setAdapter(Relation) a second time";

  public static final String INIT_ADAPTER_NON_EMPTY = "Attempt to call " +
    "setAdapter(Relation) with non-empty collection";

  public static final String ADAPTER_NULL = "Attempt to call " +
  "setAdapter(Relation) with null parameter";

  static void assertIndexInRange(int index, int minInclusive, int maxExclusive) {
    assert minInclusive < maxExclusive : ACCESS_EMPTY_ARRAY_MESSAGE;
    assert (index >= minInclusive && index < maxExclusive) : "Index " + index 
        + " was not in the acceptable range [" + minInclusive + ", " 
        + maxExclusive + ")";
  }

  static <E> void assertNotFrozen(MutableArray<E> a) {
    assert !a.isFrozen() :  "This operation is illegal on a frozen collection";
  }

  static <K,V> void assertNotFrozen(MutableMap<K,V> a) {
    assert !a.isFrozen() :  "This operation is illegal on a frozen collection";
  }
  
  static <E> void assertNotFrozen(MutableSet<E> s) {
    assert !s.isFrozen() :  "This operation is illegal on a frozen collection";
  }
  
  static void assertNotNull(Object ref) {
    assert (ref != null) : "A null reference is not allowed here";
  }
  
  static <E> void markFrozen(MutableArray<E> a) {
    if (Assertions.class.desiredAssertionStatus()) {
      a.markFrozen();
    }
  }
  
  static <K,V> void markFrozen(MutableMap<K,V> a) {
    if (Assertions.class.desiredAssertionStatus()) {
      a.markFrozen();
    }
  }

  static <E> void markFrozen(MutableSet<E> s) {
    if (Assertions.class.desiredAssertionStatus()) {
      s.markFrozen();
    }
  }
}
