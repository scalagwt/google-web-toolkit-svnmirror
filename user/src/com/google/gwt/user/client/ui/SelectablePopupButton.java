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
 * Drop down push button. Wrapper for a <code>ComplexButton</code> with a built
 * in <code>SelectablePopup</code>.
 */
public class SelectablePopupButton extends Composite implements
    SourcesChangeEvents {
  private class MyListener extends KeyboardListenerAdapter {
    StringBuffer accum = new StringBuffer();

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
  private ComplexButton button;

  private MyListener listener;

  private ItemController controller;

  /**
   * 
   * Constructor for <code>SelectablePopupButton</code>.
   * 
   * @param button push button
   * @param popup selectable popup
   */
  public SelectablePopupButton(final ComplexButton button, final SelectablePopup popup) {
    initWidget(button);
    this.popup = popup;
    this.button = button;

    button.addClickListener(new ClickListener() {

      public void onClick(Widget sender) {
        showPopup();
      }

    });

    popup.addPopupListener(new PopupListener() {
      public void onPopupClosed(PopupPanel sender, boolean autoClosed) {
        button.setPressed(false);
        listener.accum.setLength(0);
      }
    });

    button.addFocusListener(new FocusListener() {

      public void onFocus(Widget sender) {
        // Do nothing
      }

      public void onLostFocus(Widget sender) {
        popup.hide();
      }

    });
    listener = new MyListener();
    button.addKeyboardListener(listener);
  }

  public void addChangeListener(ChangeListener listener) {
    popup.addChangeListener(listener);
  }

  /**
   * Gets the push button.
   * 
   * @return the push button
   */
  public ComplexButton getButton() {
    return button;
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
  };

  public void removeChangeListener(ChangeListener listener) {
    popup.removeChangeListener(listener);
  }

  /**
   * Sets the optional item controller. If a <code>ItemController</code> is
   * given a <code>SelectablePopup</code> subtype it does not expect, runtime
   * behavior may be somewhat strange.
   * 
   * @param controller controller to set
   */
  public void setController(final ItemController controller) {
    this.controller = controller;
  }

  /**
   * Shows the popup.
   */
  public void showPopup() {
    button.setPressed(true);
    if (controller != null) {
      controller.showBelow(popup, listener.accum.toString(), button);
    } else {
      popup.showBelow(this);
    }
  }
}
