/*
 * Copyright 2006 Google Inc.
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

package com.google.gwt.user.client.ui;

import java.util.Iterator;

/**
 * Represents a pickable list of items. Each {@link ItemPicker} should be
 * selectable via mouse and keyboard events.
 * 
 */
public interface ItemPicker extends SourcesChangeEvents {
  /**
   * 
   * Programmatic equivalent of the user clicking on the selected item in the
   * popup. Note, this does not call the raw JavaScript <code>click</code>
   * method.
   */
  public void click();

  /**
   * Gets the number of items in this selectable popup.
   * 
   * @return number of items
   */
  public int getItemCount();

  /**
   * Gets the currently selected index.
   * 
   * @return selected index, or -1 if no index is selected
   */
  public int getSelectedIndex();

  /**
   * Gets the value associated with the currently selected index.
   * 
   * @return current selected value, or null if no value is selected
   */
  public Object getSelectedValue();

  /**
   * Gets the value associated with the ith index.
   * 
   * @param index index
   * @return the value associated with the ith index.
   */
  public Object getValue(int index);

  /**
   * Navigate through the popup based upon a key code. This method should be
   * used to hook up to an appropriate {@link KeyboardListener} in order to
   * allow the item picker to be navigated through via the keyboard.
   * 
   * 
   * @param keyCode key code for navigation
   * @return <code>true</code> if the key code was used to navigate through
   *         the popup, <code>false</code> otherwise
   */
  public boolean navigate(char keyCode);

  /**
   * Sets the items to be displayed.
   * 
   * @param items items to be displayed
   */
  public void setItems(Iterator items);

  /**
   * Sets the currently selected index
   * 
   * @param new selected index
   */
  public void setSelectedIndex(int index);

  /**
   * Shifts the current selection by the given amount, unless that would make
   * the selected index invalid.
   * 
   * @param shift the amount to shift the current selection by.
   */
  public void shiftSelection(int offset);

}