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
 * Wrapper for a <code>TextBox</code> or <code>TextArea</code> with a built
 * in <code>SuggestionPopup</code>.
 * 
 * <p>
 * A user must supply a <code>ItemController</code> to configure the
 * <code>SuggestionPopup</code>. For convenience, most users use an instance
 * of <code>SuggestionsController</code> as their controller. Below we show
 * how a <code>SuggestionsController</code> can be configured:
 * 
 * <pre> 
 *   SuggestionsController controller = new SuggestionsController();  
 *   controller.add("Cat");
 *   controller.add("Dog");
 *   controller.add("Horse");
 *   controller.add("Canary");
 * </pre>
 * 
 * Using the example above, if the user types "C" into the text widget, the
 * controller will configure the suggestions popup with the "Cat" and "Canary"
 * suggestions. Specifically, whenever the user types a key into the text
 * widget, the value is submitted to the <code>SuggestionsController</code>.
 * <p>
 * The user may optionally provide separators to the <code>SuggestBox</code>
 * widget. When separators are defined, only the text between the nearest
 * separators is sent to the controller rather than the entire text widget. So,
 * to continue the example above, if "," is a separator and the text widget
 * contains "dog, ca" then again the controller will configure the suggestion
 * popup with "Cat" and "Canary".
 * 
 */
public class SuggestBox extends Composite {
  private int limit = 15;
  private int selectStart;
  private int selectEnd;
  private SuggestionsPopup popup;
  private ItemController itemController;
  private char[] seperators;
  private String cachedValue;
  private TextBoxBase box;

  /**
   * 
   * Constructor for <code>SuggestBox</code>.
   * 
   * @param itemController supplies suggestions based upon the current contents
   *          of the text widget. Note, a single itemController may be used with
   *          multiple suggest boxes.
   * @param textWidget the text widget
   */
  public SuggestBox(ItemController itemController, TextBoxBase textWidget) {
    initPopup();
    this.box = textWidget;
    initWidget(textWidget);
    addKeyBoardSupport();
    setController(itemController);
  }

  /**
   * Gets the maximum number of suggestions that can be displayed by the
   * suggestion popup.
   * 
   * @return maximum number of suggestions
   */
  public int getLimit() {
    return limit;
  }

  /**
   * Sets the controller used to configure suggestions.
   * 
   * @param controller the controller
   */
  public void setController(ItemController controller) {
    this.itemController = controller;
  }

  /**
   * Sets the maximum number of suggestions that can be displayed.
   * 
   * @param limit maximum number of suggestions
   */
  public void setLimit(int limit) {
    this.limit = limit;
  }

  /**
   * Sets the separators for the text area.
   * 
   * @param seperators separators for the text are
   */
  public void setSeperators(char[] seperators) {
    this.seperators = seperators;
  }

  private void addKeyBoardSupport() {
    box.addKeyboardListener(new KeyboardListenerAdapter() {
      private boolean pendingCancel;

      public void onKeyDown(Widget sender, char keyCode, int modifiers) {
        pendingCancel = popup.navigate(keyCode);
      }

      public void onKeyPress(Widget sender, char keyCode, int modifiers) {
        if (pendingCancel) {
          box.cancelKey();
          pendingCancel = false;
        } else if (popup.isAttached()) {
          if (seperators != null && isSeperator(keyCode)) {
            // onKeyDown/onKeyUps's keyCode for ',' comes back '1/4' so must use
            // onKeyPress instead.
            popup.fireChange();

            // The separator will be added after the popup is activated, so
            // manually suppressing the popup's creation of the separator.
            box.cancelKey();
          }
        }
      }

      public void onKeyUp(Widget sender, char keyCode, int modifiers) {
        modifyText();
      }

      private String findChangeWithSeperators(String text) {
        String completeMe;
        int getCursor = Math.min(box.getCursorPos(), text.length());
        selectStart = 0;
        for (int i = getCursor - 1; i >= 0; i--) {
          char last = text.charAt(i);
          if (SuggestBox.this.isSeperator(last)) {
            selectStart = i + 1;
            break;
          }
        }
        selectEnd = getCursor;
        completeMe = text.substring(selectStart, selectEnd).trim();
        return completeMe;
      }

      private void modifyText() {
        // Find text.
        String text = box.getText().trim();
        if (text.equals(cachedValue)) {
          return;
        } else {
          cachedValue = text;
        }

        // Find candidate to replace.
        String completeMe;
        if (seperators == null) {
          completeMe = text;
        } else {
          completeMe = findChangeWithSeperators(text);
        }

        if (completeMe.length() > 0) {
          itemController.showBelow(popup, completeMe, SuggestBox.this);
        } else {
          popup.hide();
        }
      }

    });
  }

  private void initPopup() {
    popup = new SuggestionsPopup();
    popup.addChangeListener(new ChangeListener() {

      public void onChange(Widget sender) {
        String newValue = (String) popup.getSelectedValue();
        if (seperators != null) {
          onChangeWithSeperators(newValue);
        } else {
          cachedValue = newValue;
          box.setText(cachedValue);
        }
      }

      private void onChangeWithSeperators(String newValue) {
        String text = box.getText().trim();
        StringBuffer accum = new StringBuffer();
        accum.append(text.substring(0, selectStart));

        // Add one space if not at start.
        if (selectStart > 0) {
          accum.append(" ");
        }
        accum.append(newValue);
        String ender = text.substring(selectEnd).trim();
        int savedCursorPos = accum.length();
        // If there is more content after the entry, insert it.
        if (ender.length() == 0 || !isSeperator(ender.charAt(0))) {
          // Add a separator if the first char of the ender is not already a
          // separator.
          accum.append(seperators[0] + " ");
        }
        savedCursorPos = accum.length();

        accum.append(ender);

        cachedValue = accum.toString();
        box.setText(cachedValue);
        box.setCursorPos(savedCursorPos);
      }
    });
  }

  private boolean isSeperator(char candidate) {
    // An int map would be very handy right here...
    for (int i = 0; i < seperators.length; i++) {
      if (candidate == seperators[i]) {
        return true;
      }
    }
    return false;
  }
}
