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
 * The root Map type that provides read-access to a dictionary that might still
 * be mutable by another actor.
 *
 * See {@link MutableMap} for a description of the role
 * {@link Map#adapt(Object)} plays in the behavior of this class.
 *
 * @param <K> the type used to access values stored in the Map
 * @param <V> the type of values stored in the Map
 */
public abstract class Map<K, V> {

  /**
   * Encapsulates the logic to transform <K> typed keys into String. This is
   * used to index into the map elements into the underlying String indexed Map.
   */
  protected Relation<Object, String> adapter;

  Map() {
  }

  /**
   * Determines if a key is in the set of keys contained in the map. {@code key}
   * can take any value that allow {@link Map#adapt(Object)} to successfully
   * complete execution.
   * 
   * @param key to use for testing membership
   * @return {@code true} if the key is contained in the map
   */
  public abstract boolean containsKey(K key);

  /**
   * Get a value indexed by a key.
   * 
   * Notice that if the Map contains {@code null} values, a returned {@code
   * null} value does not guarantee that there is no such mapping. Use {@code
   * containsKey(K)} to determine key membership. {@code key}
   * can take any value that allow {@link Map#adapt(Object)} to successfully
   * complete execution.
   * 
   * @param key index to use for retrieval
   * @return value associated to the key or {@code null} otherwise
   */
  public abstract V get(K key);

  /**
   * Tests whether this Map contains any element.
   * 
   * @return {@code true} if the map contains no entries
   */
  public abstract boolean isEmpty();
  
  /**
   * Translates the Map key set domain into String. Actually calls
   * {@code adapter.applyTo(Object)}.
   * @see MutableMap
   * @param key to adapt
   * @return a String uniquely representing the {@code key}; {@code null}
   * if the {@code value} cannot be represented as a String
   */
  protected final String adapt(Object key) {
    return adapter.applyTo(key);
  }

}
