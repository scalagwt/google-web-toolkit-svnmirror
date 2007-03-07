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

package com.google.gwt.user.client.ui.richtext;

import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.ClippedImage;
import com.google.gwt.user.client.ui.FocusListenerAdapter;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.PopupListener;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SelectablePopup;
import com.google.gwt.user.client.ui.SourcesChangeEvents;
import com.google.gwt.user.client.ui.SuggestionsPopup;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;

/**
 * {@link DropDownButton} A button with an associated {@link SelectablePopup}.
 */
public class DropDownButton extends ToggleButton implements SourcesChangeEvents {
  /**
   * Key press.
   */
  private class KeyPressListener extends KeyboardListenerAdapter {
    public void onKeyDown(Widget sender, char keyCode, int modifiers) {
      if (getPopup().isAttached() == false) {
        showPopup();
      }
      handleKeyPress(keyCode);
    }
  }

  private SelectablePopup popup;

  /**
   * 
   * Constructor for <code>DropDownButton</code>. Creates a
   * {@link SuggestionsPopup} for use with this button.
   */
  public DropDownButton() {
    this(new SuggestionsPopup());
  }

  /**
   * 
   * Constructor for <code>SelectablePopupButton</code>.
   * 
   * @param upImage up image for button
   * @param downImage down image for button
   * @param popup selectable popup
   */
  public DropDownButton(ClippedImage upImage, ClippedImage downImage,
      SelectablePopup popup) {
    this(popup);
    getUpFace().setImage(upImage);
    getDownFace().setImage(downImage);
  }

  /**
   * 
   * Constructor for <code>DropDownPushButton</code>.
   * 
   * @param popup selectable popup
   */
  public DropDownButton(SelectablePopup popup) {
    if (popup == null) {
      throw new IllegalArgumentException(
          "A Selectable popup button may not have a null popup");
    }
    setPopup(popup);
  }

  public void addChangeListener(ChangeListener listener) {
    popup.addChangeListener(listener);
  }

  /**
   * Gets the popup.
   * 
   * @return the popup
   */
  public SelectablePopup getPopup() {
    return popup;
  }

  public void removeChangeListener(ChangeListener listener) {
    popup.removeChangeListener(listener);
  }

  /**
   * Shows the popup.
   */
  public void showPopup() {
    setDown(true);
    popup.showBelow(this);
  };

  void handleKeyPress(char keyCode) {
    getPopup().navigate(keyCode);
  }

  private void setPopup(final SelectablePopup popup) {
    this.popup = popup;

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

    // If the popup is closed for any reason, set the button to be depressed.
    popup.addPopupListener(new PopupListener() {
      public void onPopupClosed(PopupPanel sender, boolean autoClosed) {
        setDown(false);
      }
    });

    // If the button loses focus, hide the popup.
    addFocusListener(new FocusListenerAdapter() {
      public void onLostFocus(Widget sender) {
        popup.hide();
      }
    });
  }
}
