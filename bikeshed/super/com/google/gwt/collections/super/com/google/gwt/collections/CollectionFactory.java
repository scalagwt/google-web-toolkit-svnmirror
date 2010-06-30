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
 * Made to be switched out using super source even while Collections itself isn't.
 */
public class CollectionFactory {
  
  static final Relation<Object, String> defaultAdapter = 
      new Relation<Object, String>() {
    public String applyTo(Object value) {
      if (value == null) {
        return null;
      }
      return (String) value;
    }
  };

  public static native <E> MutableArray<E> createMutableArray() /*-{
    return Array();
  }-*/;
  
  public static native <E> MutableArray<E> createMutableArray(int size) /*-{
    return Array(size);
  }-*/;
  
  public static native <E> MutableArray<E> createMutableArray(int size, 
      E fillValue) /*-{
    var r = Array(size);
    if (fillValue != null) {
      for (i = 0; i < size; ++i) {
        r[i] = fillValue;
      }
    }
    return r;
  }-*/;

  public static native <K,V> MutableMap<K,V> createMutableMap() /*-{
    return Object();
  }-*/;

  public static <K,V> MutableMap<K,V> createMutableMap(
      Relation<Object, String> adapter) {
    if (adapter == null) {
      throw new NullPointerException("adapter == null");
    }
    MutableMap<K,V> result = createMutableMap();
    result.setAdapter(adapter);
    return result;
  }
  
  public static native <E> MutableSet<E> createMutableSet() /*-{
    return Object();
  }-*/;

  public static <E> MutableSet<E> createMutableSet(
      Relation<Object, String> adapter) {
    
    if (adapter == null) {
      throw new NullPointerException("adapter == null");
    }
    MutableSet<E> set = createMutableSet();
    set.setAdapter(adapter);
    return set;
  }
  
}
