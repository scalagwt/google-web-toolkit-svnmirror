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

  public static <E> MutableArray<E> createMutableArray() {
    return new MutableArray<E>();
  }
  
  public static <E> MutableArray<E> createMutableArray(int size, E fillValue) {
    MutableArray<E> r = new MutableArray<E>();
    r.setSize(size, fillValue);
    return r;
  }
  
  public static <V> MutableStringMap<V> createMutableStringMap() {
    return new MutableStringMap<V>();
  }

}
