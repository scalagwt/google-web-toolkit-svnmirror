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

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.ClippedImage;
import com.google.gwt.user.client.ui.FocusListenerAdapter;
import com.google.gwt.user.client.ui.ItemPicker;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.PopupListener;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SuggestPicker;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;

/**
 * {@link ItemPickerButtonImpl} A button with a drop down {@link ItemPicker}.
 * Used by {@link com.google.gwt.user.client.ui.RichTextEditor}.
 */
public class ItemPickerButtonImpl extends ToggleButton {

  private ItemPickerDropDownImpl popup;

  {
    // Adds a keyboard listener.
    addKeyboardListener(new KeyboardListenerAdapter() {
      public void onKeyDown(Widget sender, char keyCode, int modifiers) {
        if (keyCode != KeyboardListener.KEY_TAB && popup.isAttached() == false) {
          showItemPicker();
          return;
        }
        handleKeyDown(keyCode);
      }

    });

    // Toggles menu based on button click.
    addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        if (isDown()) {
          showItemPicker();
        } else {
          popup.hide();
        }
      }
    });

    addFocusListener(new FocusListenerAdapter() {
      public void onLostFocus(Widget sender) {
        popup.hide();
      }
    });
  }

  /**
   * 
   * Constructor for {@link ItemPickerButtonImpl}. Creates a
   * {@link SuggestPicker} for use with this button.
   */
  public ItemPickerButtonImpl() {
    this(new SuggestPicker());
  }

  /**
   * 
   * Constructor for {@link ItemPickerButtonImpl}.
   * 
   * @param upImage up image for button
   * @param downImage down image for button
   * @param picker item picker to use in drop down list
   */
  public ItemPickerButtonImpl(ClippedImage upImage, ClippedImage downImage,
      ItemPicker picker) {
    super(upImage, downImage);
    setPicker(picker);
  }

  /**
   * 
   * Constructor for {@link ItemPickerButtonImpl}.
   * 
   * @param picker item picker
   */
  public ItemPickerButtonImpl(ItemPicker picker) {
    setPicker(picker);
  }

  /**
   * 
   * Constructor for {@link ItemPickerButtonImpl}.
   * 
   * @param upText the text for the default (up) face of the button.
   * @param picker item picker
   */
  public ItemPickerButtonImpl(String upText, ItemPicker picker) {
    super(upText);
    setPicker(picker);
  }

  /**
   * Gets the current item picker.
   * 
   * @return item picker
   */
  public ItemPicker getItemPicker() {
    return popup;
  }

  /**
   * Shows the popup.
   */
  protected void showItemPicker() {
    setDown(true);
    popup.show();
  }

  ItemPickerDropDownImpl getPopup() {
    return popup;
  };

    void handleKeyDown(char keyCode) {
    if (keyCode != KeyboardListener.KEY_TAB && popup.isAttached() == false) {
      showItemPicker();
    }
    popup.delegateKeyDown(keyCode);
  }

  private void setPicker(final ItemPicker picker) {
    if (picker == null) {
      throw new IllegalArgumentException(
          "An ItemPickerButtonImpl may not have a null popup");
    }
    this.popup = new ItemPickerDropDownImpl(this, picker);
    popup.addPopupListener(new PopupListener() {
      public void onPopupClosed(PopupPanel sender, boolean autoClosed) {
        setDown(false);
      }
    });
  }
}
