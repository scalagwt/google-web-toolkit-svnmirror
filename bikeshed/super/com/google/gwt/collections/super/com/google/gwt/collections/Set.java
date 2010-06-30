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

import com.google.gwt.core.client.JavaScriptObject;

/**
 * The root Set type that provides read-access to an set that might still be
 * mutable by another actor.
 *
 * See {@link MutableSet} for a description of the role 
 * {@link Set#adapt(Object)} plays in the behavior of this class.
 *
 * @param <E> The type stored in the array elements
 */
public abstract class Set<E> extends JavaScriptObject {
  
  /**
   * This is necessary to reach the non-static method contains
   * in the overlay type Set class.
   * @return true if the element is a member of {@code that} set
   */
  protected static boolean staticContains(Set that, Object element) {
    return that.contains(element);
  }
  
  protected Set() {
  }
  
  /**
   * Tests element membership. {@code element} can take any value accepted by
   * {@link Set#adapt(Object)}.
   * 
   * @param element element to test for membership
   * @return true if the element is a member of this set
   */
  public final boolean contains(Object element) {
    String key = adapt(element);
    if (key == null) {
      return false;
    }
    return jsniContains(key);
  }

  /**
   * Tests whether {@code source} is a subset of this set.
   * 
   * @param source set containing elements to test for membership
   * @return true if all elements in {@code source} are in this set
   * @exception NullPointerException if {@code source} is {@code null}
   */
  public final boolean containsAll(Set<E> source) {
    if (source == null) {
      throw new NullPointerException("source == null");
    }
    return jsniContainsAll(source);
  }

  /**
   * Tests whether the intersection of {@code source} and this set is not empty.
   * 
   * @param source set containing elements to test for membership
   * @return true if at least one elements in {@code source} is in this set
   * @exception NullPointerException if {@code source} is {@code null}
   */
  public final boolean containsSome(Set<E> source) {
    if (source == null) {
      throw new NullPointerException("source == null");
    }
    return jsniContainsSome(source);
  }

  /**
   * Tests whether this set contains any element.
   * 
   * @return {@code true} if the set contains at least one element
   */
  public final native boolean isEmpty() /*-{
    for (var k in this) {
      if (k != "adapter" && k != "frozen") {
        return false;
      }
    }
    return true;
  }-*/;

  /**
   * Tests whether {@code source} and {@code this} set contain the same
   * elements.
   * 
   * @param source set containing elements to test for membership
   * @return {@code true} if source != null, all elements in {@code source}
   *         are in {@code this} set and all elements in {@code this} set are
   *         contained in {@code source}; {@code false} otherwise
   */
  public final boolean isEqual(Set<E> source) {
    return source != null 
        && source.containsAll(this) 
        && containsAll(source);
  }
  
  /**
   * Translates the Set domain into String. Actually calls {@code
   * adapter.applyTo(Object)}.
   * 
   * @see MutableSet
   * @param key to adapt
   * @return a String uniquely representing the {@code value}. {@code null}
   *         if the {@code value} cannot be represented as a String
   */
  protected final String adapt(Object key) {
    if (!jsniIsAdapterPresent()) {
      return key == null ? null : (String) key;
    }
    return jsniGetAdapter().applyTo(key);
  }
  
  protected final native boolean jsniIsAdapterPresent() /*-{
    return Boolean(this.adapter);
  }-*/;
  
  private native boolean jsniContains(String key) /*-{
    return this[key] !== undefined;
  }-*/;
  
  private native boolean jsniContainsAll(Set<E> source) /*-{
    for (var k in source) {
      if (k != "adapter" && k != "frozen" && 
          !@com.google.gwt.collections.Set::staticContains(Lcom/google/gwt/collections/Set;Ljava/lang/Object;)(this, source[k])) {
        return false;
      }
    }
    return true;
  }-*/;
  
  private native boolean jsniContainsSome(Set<E> source) /*-{
    for (var k in source) {
      if (k != "adapter" && k != "frozen" && 
          @com.google.gwt.collections.Set::staticContains(Lcom/google/gwt/collections/Set;Ljava/lang/Object;)(this, source[k])) {
        return true;
      }
    }
    return false;
  }-*/;

  private native Relation<Object,String> jsniGetAdapter() /*-{
    return this.adapter;
  }-*/;
  
}
