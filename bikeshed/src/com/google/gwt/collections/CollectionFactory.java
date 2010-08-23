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
 * Made to be switched out using super source even while Collections itself
 * isn't.
 */
public class CollectionFactory {
  
  static final Relation<Object, String> defaultAdapter = 
      new Relation<Object, String>() {
    public String applyTo(Object value) {
      if (value == null) {
        return null;
      }
      return value.toString();
    }
  };

  /**
   * Creates an empty {@link MutableArray}.
   * @param <E> type of elements in the array
   * @return an empty {@code MutableArray}
   */
  public static <E> MutableArray<E> createMutableArray() {
    return new MutableArray<E>();
  }
  
  /**
   * Creates a {@link MutableArray} with an initial {@code size} and null 
   * elements.
   * 
   * @param <E> type of elements in the array
   * @param size size of the array
   * @return a {@code MutableArray}
   */
  public static <E> MutableArray<E> createMutableArray(Integer size) {
    return createMutableArray(size, null);
  }
  
  /**
   * Creates a {@link MutableArray} with {@code size} references to {@code
   * fillValue}.
   * 
   * @param <E> type of elements in the array
   * @param size size of the array
   * @param fillValue initial value to use for the elements
   * @return a {@code MutableArray}
   */
  public static <E> MutableArray<E> createMutableArray(int size, E fillValue) {
    MutableArray<E> r = new MutableArray<E>();
    r.setSize(size, fillValue);
    return r;
  }
  
  /**
   * Creates an empty {@link MutableMap}.
   * 
   * @param <K> type of keys in the map
   * @param <V> type of elements in the map
   * @return an empty {@code MutableStringMap}
   */
  public static <K,V> MutableMap<K,V> createMutableMap() {
    MutableMap<K,V> result = new MutableMap<K,V>();
    result.setAdapter(defaultAdapter);
    return result;
  }

  /**
   * Creates an empty {@link MutableMap} that uses a provided adapter.
   * 
   * @param adapter Relation<Object, String> to use to convert the map domain to
   *        strings
   * @param <K> type of keys in the map
   * @param <V> type of elements in the map
   * @return an empty {@code MutableStringMap}
   * @throws NullPointerException if {@code adapter == null}
   */
  public static <K,V> MutableMap<K,V> createMutableMap(
      Relation<Object, String> adapter) {
    if (adapter == null) {
      throw new NullPointerException("adapter == null");
    }
    MutableMap<K,V> result = new MutableMap<K,V>();
    result.setAdapter(adapter);
    return result;
  }

  /**
   * Creates an empty {@link MutableSet}.
   * @param <E> type of elements in the map
   * @return an empty {@code MutableSet}
   */
  public static <E> MutableSet<E> createMutableSet() {
    return createMutableSet(defaultAdapter);
  }

  /**
   * Creates an empty {@link MutableSet} that uses a specific adapter.
   * @param <E> type of elements in the map
   * @return an empty {@code MutableSet}
   * @throws NullPointerException if {@code adapter == null}
   */
  public static <E> MutableSet<E> createMutableSet(
      Relation<Object, String> adapter) {
    if (adapter == null) {
      throw new NullPointerException("adapter == null");
    }
    MutableSet<E> set = new MutableSet<E>();
    set.setAdapter(adapter);
    return set;
  }
  
}
