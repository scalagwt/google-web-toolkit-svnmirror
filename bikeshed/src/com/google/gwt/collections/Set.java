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

import java.util.HashMap;

/**
 * The root Set type that provides read-access to an set that might still be
 * mutable by another actor.
 *
 * See {@link MutableSet} for a description of the role 
 * {@link Set#adapt(Object)} plays in the behavior of this class.
 *
 * @param <E> The type stored in the array elements
 */
public abstract class Set<E> {

  /**
   * Encapsulates the logic to transform <E> typed elements into String. This is
   * used to index into the set elements into a Map.
   */
  protected Relation<Object, String> adapter;

  /**
   * Backing store for the set elements.
   */
  HashMap<String, E> elements = new HashMap<String, E>();
  
  /**
   * Tests element membership. {@code element} can take any value accepted by
   * {@link Set#adapt(Object)}.
   * 
   * @param element element to test for membership
   * @return true if the element is a member of this set
   */
  public abstract boolean contains(Object element);

  /**
   * Tests whether {@code source} is a subset of this set.
   * 
   * @param source set containing elements to test for membership
   * @return true if all elements in {@code source} are in this set
   * @exception NullPointerException if {@code source} is {@code null}
   */
  public abstract boolean containsAll(Set<E> source);

  /**
   * Tests whether the intersection of {@code source} and this set is not empty.
   * 
   * @param source set containing elements to test for membership
   * @return true if at least one elements in {@code source} is in this set
   * @exception NullPointerException if {@code source} is {@code null}
   */
  public abstract boolean containsSome(Set<E> source);

  /**
   * Tests whether this set contains any element.
   * 
   * @return {@code true} if the set contains at least one element
   */
  public abstract boolean isEmpty();

  /**
   * Tests whether {@code source} and {@code this} set contain the same
   * elements.
   * 
   * @param source set containing elements to test for membership
   * @return {@code true} if source != null, all elements in {@code source}
   *         are in {@code this} set and all elements in {@code this} set are
   *         contained in {@code source}; {@code false} otherwise
   */
  public abstract boolean isEqual(Set<E> source);
  
  /**
   * Translates the Set domain into String. Actually calls {@code
   * adapter.applyTo(Object)}.
   * 
   * @see MutableSet
   * @param value to adapt
   * @return a String uniquely representing the {@code value}. {@code null}
   *         if the {@code value} cannot be represented as a String
   */
  protected final String adapt(Object value) {
    return adapter.applyTo(value);
  }

}
