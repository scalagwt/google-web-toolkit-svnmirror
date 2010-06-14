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
 * An array whose content and length can change over time. This implementation
 * is used in web mode.
 * 
 * @param <E> The type stored in the array elements
 */
public final class MutableArray<E> extends Array<E> {

  /**
   * Can only be constructed via {@link CollectionFactory}.
   */
  protected MutableArray() {
  }

  @ConstantTime
  public void add(E elem) {
    Assertions.assertNotFrozen(this);
    jsniAdd(elem);
  }

  @ConstantTime
  public void clear() {
    Assertions.assertNotFrozen(this);
    jsniClear();
  }

  /**
   * Creates an {@link ImmutableArray} with the contents of this {@code
   * MutableArray}. Also marks this {@link MutableArray} as read-only. After
   * calling {@code freeze()}, only use read-only methods to access the elements
   * in the array.
   */
  public ImmutableArray<E> freeze() {
    Assertions.markFrozen(this);
    return this.<ImmutableArray<E>>cast();
  }

  /**
   * Inserts {@code elem} before the element residing at {@code index}.
   * 
   * @param index in the range [0, this.size()], inclusive; if index is equal to
   *          the array's current size, the result is equivalent to calling
   *          {@link #add(Object)}
   * @param elem the element to insert or {@code null}
   */
  @LinearTime
  public void insert(int index, E elem) {
    Assertions.assertNotFrozen(this);
    // TODO: fix gwtc to optimize away Assertions.assertIndexInRange  
    assert (index >= 0 && index < size() + 1) : "Index " + index 
        + " was not in the acceptable range [" + 0 + ", " + (size() + 1) + ")";
    jsniInsert(index, elem);
  }

  /**
   * Removes the element at the specified index.
   */
  @LinearTime
  public void remove(int index) {
    Assertions.assertNotFrozen(this);
    // TODO: fix gwtc to optimize away Assertions.assertIndexInRange  
    assert 0 < size() : Assertions.ACCESS_EMPTY_ARRAY_MESSAGE;
    assert (index >= 0 && index < size()) : "Index " + index 
        + " was not in the acceptable range [" + 0 + ", " + size() + ")";
    jsniRemove(index);
  }

  /**
   * Replaces the element at the specified index.
   * 
   * @param index in the range [0, this.size()), exclusive
   * @param elem the element to insert or {@code null}
   */
  @ConstantTime
  public void set(int index, E elem) {
    Assertions.assertNotFrozen(this);
    // TODO: fix gwtc to optimize away Assertions.assertIndexInRange  
    assert 0 < size() : Assertions.ACCESS_EMPTY_ARRAY_MESSAGE;
    assert (index >= 0 && index < size()) : "Index " + index 
        + " was not in the acceptable range [" + 0 + ", " + size() + ")";

    jsniSet(index, elem);
  }

  /**
   * Changes the array size. If {@code newSize} is less than the current size,
   * the array is truncated. If {@code newSize} is greater than the current size
   * the array is grown and the new elements of the array filled up with {@code
   * fillValue}.
   */
  @LinearTime
  public void setSize(int newSize, E fillValue) {
    Assertions.assertNotFrozen(this);
    jsniSetSize(newSize, fillValue);
  }

  // Only meant to be called from within Assertions
  native boolean isFrozen() /*-{
    return !!this.frozen;
  }-*/;

  // Only meant to be called from within Assertions
  native void markFrozen() /*-{
    this.frozen = true;
  }-*/;

  @ConstantTime
  private native void jsniAdd(E elem) /*-{
    this.push(elem);
  }-*/;

  @ConstantTime
  private native void jsniClear() /*-{
    this.length = 0;
  }-*/;

  /**
   * Inserts {@code element} before the element residing at {@code index}.
   * 
   * @param index in the range [0, this.size()], inclusive; if index is equal to
   *          the array's current size, the result is equivalent to calling
   *          {@link #add(Object)}
   * @param elem the element to insert or {@code null}
   */
  @LinearTime
  private native void jsniInsert(int index, E elem) /*-{
    this.splice(index, 0, elem);
  }-*/;

  /**
   * Removes the element at the specified index.
   */
  @LinearTime
  private native void jsniRemove(int index) /*-{
    this.splice(index, 1);
  }-*/;

  /**
   * Replaces the element at the specified index.
   * 
   * @param index in the range [0, this.size()), exclusive
   * @param elem the element to insert or {@code null}
   */
  @ConstantTime
  private native void jsniSet(int index, E elem) /*-{
    this[index] = elem;
  }-*/;

  /**
   * Changes the array size. If {@code newSize} is less than the current size,
   * the array is truncated. If {@code newSize} is greater than the current size
   * the array is grown and the new elements of the array filled up with {@code
   * fillValue}.
   */
  @LinearTime
  private native void jsniSetSize(int newSize, E fillValue) /*-{
    if (fillValue == null) {
      this.length = newSize;
    } else {  
      for (var i = this.length; i < newSize; ++i) {
        this[i] = fillValue;
      }
    }
  }-*/;

}
