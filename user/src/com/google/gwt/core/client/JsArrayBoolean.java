/*
 * Copyright 2008 Google Inc.
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
package com.google.gwt.core.client;

/**
 * A simple wrapper around a homogeneous native array of boolean values.
 * 
 * This class may not be directly instantiated, and can only be returned from a
 * native method. For example,
 * 
 * <code>
 * native JsArrayBoolean getNativeArray() /*-{
 *   return [true, false, true];
 * }-* /;
 * </code>
 */
public class JsArrayBoolean extends JavaScriptObject {

  protected JsArrayBoolean() {
  }

  /**
   * Gets the value at a given index.
   * 
   * If an undefined or non-boolean value exists at the given index, a
   * type-conversion error will occur in hosted mode and unpredictable behavior
   * may occur in web mode.
   * 
   * @param index the index to be retrieved
   * @return the value at the given index
   */
  public final native boolean get(int index) /*-{
    return this[index];
  }-*/;

  /**
   * Gets the length of the array.
   * 
   * @return the array length
   */
  public final native int length() /*-{
    return this.length;
  }-*/;

  /**
   * Sets the value value at a given index.
   * 
   * If the index is out of bounds, the value will still be set. The array's
   * length will be updated to encompass the bounds implied by the added value.
   * 
   * @param index the index to be set
   * @param value the value to be stored
   */
  public final native void set(int index, boolean value) /*-{
    this[index] = value;
  }-*/;
}
