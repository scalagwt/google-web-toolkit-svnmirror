/*
 * Copyright 2007 Google Inc.
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

import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.ClippedImage;
import com.google.gwt.user.client.ui.ItemPicker;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.PopupListener;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SourcesChangeEvents;
import com.google.gwt.user.client.ui.SuggestPicker;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;

/**
 * {@link ItemPickerButtonImpl} A button with a drop down list of pickable items.
 */
public class ItemPickerButtonImpl extends ToggleButton implements
    SourcesChangeEvents {
  /**
   * Key press.
   */
  private class KeyPressListener extends KeyboardListenerAdapter {
    public void onKeyDown(Widget sender, char keyCode, int modifiers) {
      handleKeyPress(keyCode);
    }
  }

  private ItemPickerDropDownImpl popup;

  {
    // Adds a keyboard listener.
    addKeyboardListener(new KeyPressListener());

    // Toggles menu based on button click.
    addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        if (isDown()) {
          showPopup();
        } else {
          popup.hide();
        }
      }
    });
  }

  /**
   * 
   * Constructor for <code>DropDownButton</code>. Creates a
   * {@link SuggestPicker} for use with this button.
   */
  public ItemPickerButtonImpl() {
    this(new SuggestPicker());
  }

  /**
   * 
   * Constructor for <code>SelectablePopupButton</code>.
   * 
   * @param upImage up image for button
   * @param downImage down image for button
   * @param picker item picker to use in drop down list
   */
  public ItemPickerButtonImpl(ClippedImage upImage, ClippedImage downImage,
      ItemPicker picker) {
    this(picker);
    getUpFace().setImage(upImage);
    getDownFace().setImage(downImage);
  }

  /**
   * 
   * Constructor for <code>DropDownPushButton</code>.
   * 
   * @param picker item picker
   */
  public ItemPickerButtonImpl(ItemPicker picker) {
    if (picker == null) {
      throw new IllegalArgumentException(
          "A Selectable popup button may not have a null popup");
    }
    setPicker(picker);
  }

  public void addChangeListener(ChangeListener listener) {
    popup.addChangeListener(listener);
  }

  /**
   * Gets the current item picker.
   * 
   * @return item picker
   */
  public ItemPicker getItemPicker() {
    return popup.getPicker();
  }

  public void removeChangeListener(ChangeListener listener) {
    popup.removeChangeListener(listener);
  }

  /**
   * Shows the popup.
   */
  public void showPopup() {
    setDown(true);
    popup.show();
  };

  ItemPickerDropDownImpl getPopup() {
    return popup;
  }

  void handleKeyPress(char keyCode) {
    if (popup.isAttached() == false) {
      showPopup();
    }
    popup.navigate(keyCode);
  }

  private void setPicker(final ItemPicker picker) {
    this.popup = new ItemPickerDropDownImpl(this, picker);
    popup.addPopupListener(new PopupListener() {
      public void onPopupClosed(PopupPanel sender, boolean autoClosed) {
        setDown(false);
      }
    });
  }
}
