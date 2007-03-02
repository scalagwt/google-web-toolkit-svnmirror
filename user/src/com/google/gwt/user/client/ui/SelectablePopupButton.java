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

package com.google.gwt.user.client.ui;

import com.google.gwt.user.client.ui.SelectablePopup.ItemController;

/**
 * {@link CustomButton} with a selectable popup.
 */
public class SelectablePopupButton extends CustomButton implements
    SourcesChangeEvents {

  /**
   * Default listener class.
   */
  private class MyListener extends KeyboardListenerAdapter {
    private StringBuffer accum = new StringBuffer();

    public void onKeyDown(Widget sender, char keyCode, int modifiers) {
      if (popup.isAttached() == false) {
        showPopup();
        // If the char will not filter, than it should be ignored after
        // triggering the popup.
        if (!Character.isLetterOrDigit(keyCode)) {
          return;
        }
      }
      boolean navigate = popup.navigate(keyCode);
      if (navigate) {
        return;
      }
      if (controller != null) {
        if (Character.isLetterOrDigit(keyCode)) {
          accum.append(keyCode);
        } else if (keyCode == KEY_BACKSPACE) {
          accum.setLength(accum.length() - 1);
        } else {
          accum.setLength(0);
        }
        showPopup();
      }
    }
  }

  private SelectablePopup popup;
  private MyListener listener;
  private ItemController controller;

  /**
   * 
   * Constructor for <code>DropDownPushButton</code>.
   * 
   * @param popup selectable popup
   */
  public SelectablePopupButton(SelectablePopup popup) {
    this.popup = popup;
    setToggleBehavior(true);
    addDefaultListeners();
  }

  /**
   * 
   * Constructor for <code>SelectablePopupButton</code>.
   * 
   * @param upImage up image for button
   * @param downImage down image for button
   */
  public SelectablePopupButton(ClippedImage upImage, ClippedImage downImage,
      SelectablePopup popup) {
    this(popup);
    getUp().setFace(upImage);
    getDown().setFace(downImage);
  }

  public void addChangeListener(ChangeListener listener) {
    popup.addChangeListener(listener);
  }

  /**
   * Gets the current item controller.
   * 
   * @return the current item controller or <code>null</code> if it is not
   *         defined
   */
  public ItemController getController() {
    return this.controller;
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
   * Sets the optional item controller. Once a item controller is installed, the
   * item controller will be responsible for displaying the popup.
   * 
   * <p>
   * Note, if a <code>ItemController</code> is given a
   * <code>SelectablePopup</code> subtype it does not expect, runtime behavior
   * may be somewhat strange.
   * 
   * @param controller controller to set
   */
  public void setController(final ItemController controller) {
    this.controller = controller;
  };

  /**
   * Sets the current popup.
   * 
   * @param popup popup to set
   */
  public void setPopup(SelectablePopup popup) {
    popup = this.popup;
  }

  /**
   * Shows the popup.
   */
  public void showPopup() {
    setPressed(true);
    if (controller != null) {
      controller.showBelow(popup, listener.accum.toString(), this);
    } else {
      popup.showBelow(this);
    }
  }

  private void addDefaultListeners() {
    // Toggles menu based on button click.
    addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        if (isPressed()) {
          showPopup();
        } else {
          popup.hide();
        }
      }
    });

    // If the popup is closed for any reason, set the button to be depressed.
    popup.addPopupListener(new PopupListener() {
      public void onPopupClosed(PopupPanel sender, boolean autoClosed) {
        setPressed(false);
        listener.accum.setLength(0);
      }
    });

    // If the button loses focus, hide the popup.
    addFocusListener(new FocusListenerAdapter() {
      public void onLostFocus(Widget sender) {
        popup.hide();
      }
    });
    listener = new MyListener();
    addKeyboardListener(listener);
  }
}
