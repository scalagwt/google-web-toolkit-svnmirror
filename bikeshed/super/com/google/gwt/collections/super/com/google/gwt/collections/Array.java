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

import com.google.gwt.core.client.JavaScriptObject;

/**
 * The root Array type that provides read-access to an array that might still
 * be mutable by another actor.
 * 
 * @param <E> The type stored in the array elements
 */
public abstract class Array<E> extends JavaScriptObject {

  protected Array() {
  }

  @ConstantTime
  public final E get(int index) {
    Assertions.assertIndexInRange(index, 0, size());
    return jsniGet(index);
  }

  @ConstantTime
  public final native int size() /*-{
    return this.length;
  }-*/;
  
  @ConstantTime
  private native E jsniGet(int index) /*-{
    return this[index];
  }-*/;
  
}
