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

package com.google.gwt.user.client.ui.impl;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.FocusListenerAdapter;
import com.google.gwt.user.client.ui.HasFocus;
import com.google.gwt.user.client.ui.ItemPicker;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;

import java.util.Iterator;

/**
 * Shared utility class for {@link com.google.gwt.user.client.ui.SuggestBox},
 * {@link com.google.gwt.user.client.ui.impl.ItemPickerButtonImpl}, and
 * {@link SpellCheck} which all use
 * {@link com.google.gwt.user.client.ui.ItemPicker} objects in styled drop
 * downs.
 */
public class ItemPickerDropDownImpl extends PopupPanel implements ItemPicker {
  private final ItemPicker picker;
  private final HasFocus owner;

  private boolean justChanged;

  public ItemPickerDropDownImpl(final HasFocus owner, ItemPicker picker) {
    super(true);
    setWidget((Widget) picker);
    this.picker = picker;
    this.owner = owner;
    owner.addFocusListener(new FocusListenerAdapter() {
      public void onLostFocus(Widget sender) {
        hide();
      }
    });

    picker.addChangeListener(new ChangeListener() {
      public void onChange(Widget sender) {
        hide();
      }
    });
  }

  public void addChangeListener(ChangeListener listener) {
    picker.addChangeListener(listener);
  }

  public void click() {
    picker.click();
  }

  public int getItemCount() {
    return picker.getItemCount();
  }

  public ItemPicker getPicker() {
    return picker;
  }

  public int getSelectedIndex() {
    return picker.getSelectedIndex();
  }

  public Object getSelectedValue() {
    return picker.getSelectedValue();
  }

  public Object getValue(int index) {
    return picker.getValue(index);
  }

  public boolean navigate(char keyCode) {
    if (isAttached()) {
      switch (keyCode) {
        case KeyboardListener.KEY_ESCAPE:
          hide();
          return true;
        case KeyboardListener.KEY_ENTER:
          picker.click();
          return true;
        default:
          // Avoid shared post processing.
          return picker.navigate(keyCode);
      }
    }
    return false;
  }

  public void removeChangeListener(ChangeListener listener) {
    picker.removeChangeListener(listener);
  }

  public void setItems(Iterator items) {
    picker.setItems(items);
  }

  public void setSelectedIndex(int index) {
    picker.setSelectedIndex(index);
  }

  public void shiftSelection(int offset) {
    picker.shiftSelection(offset);
  }

  /**
   * Shows the popup, by default <code>show</code> selects the first item and
   * displays itself under it's owner.
   */
  public void show() {
    showBelow((UIObject) owner);
  }

  /**
   * Shows the popup below the given UI object. By default, first item is
   * selected in the item picker.
   * <p>
   * Note, if the popup would not be visible on the browser, than the popup's
   * position may be adjusted.
   * 
   * @param showBelow the <code>UIObject</code> beneath which the popup should
   *          be shown.
   */
  public void showBelow(UIObject showBelow) {
    if (picker.getItemCount() == 0) {
      // A drop down with 0 items should never show itself.
      hide();
      return;
    }
    picker.setSelectedIndex(0);
    // show must be called first, as otherwise getOffsetWidth is not correct.
    super.show();
    final int left = showBelow.getAbsoluteLeft();
    int overshootLeft = Math.max(0, (left + getOffsetWidth())
        - Window.getClientWidth());
    final int top = showBelow.getAbsoluteTop() + showBelow.getOffsetHeight();
    final int overshootTop = Math.max(0, (top + getOffsetHeight())
        - Window.getClientHeight());
    setPopupPosition(left - overshootLeft, top - overshootTop);
    super.show();
  }
}
