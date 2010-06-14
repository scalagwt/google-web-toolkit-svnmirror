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

  /**
   * Creates an empty {@link MutableArray}.
   * @param <E> type of elements in the array
   * @return an empty {@code MutableArray}
   */
  public static <E> MutableArray<E> createMutableArray() {
    return new MutableArray<E>();
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
   * Creates an empty {@link MutableStringMap}.
   * @param <V> type of elements in the map
   * @return an empty {@code MutableStringMap}
   */
  public static <V> MutableStringMap<V> createMutableStringMap() {
    return new MutableStringMap<V>();
  }

}
