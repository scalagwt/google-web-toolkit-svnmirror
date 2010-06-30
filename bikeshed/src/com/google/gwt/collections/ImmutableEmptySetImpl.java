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
 * A set that is always empty. Byte code implementation.
 * 
 * @param <E> type of elements contained in the set
 */
public class ImmutableEmptySetImpl<E> extends ImmutableSet<E> {

  @Override
  public boolean contains(Object element) {
    return false;
  }

  @Override
  public boolean containsAll(Set<E> source) {
    if (source == null) {
      throw new NullPointerException("source == null");
    }
    return source.isEmpty();
  }

  @Override
  public boolean containsSome(Set<E> source) {
    if (source == null) {
      throw new NullPointerException("source == null");
    }
    return false;
  }

  @Override
  public boolean isEmpty() {
    return true;
  }

  @Override
  public boolean isEqual(Set<E> source) {
    if (source == null) {
      return false;
    }
    return source.isEmpty();
  }

}
