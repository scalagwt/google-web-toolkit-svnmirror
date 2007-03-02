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
  private int selectStart;
  private int selectEnd;
  private ItemController itemController;
  private char[] separators;
  private String cachedValue;
  private final SuggestionsPopup popup;
  private final TextBoxBase box;
  private String separatorPadding = " ";

  /**
   * 
   * Constructor for <code>SuggestBox</code>.
   * 
   * @param itemController supplies suggestions based upon the current contents
   *          of the text widget. Note, a single itemController may be used with
   *          multiple suggest boxes.
   * @param box the text widget
   */
  public SuggestBox(ItemController itemController, TextBoxBase box) {
    popup = new SuggestionsPopup();
    this.box = box;
    box.addFocusListener(new FocusListener() {
      public void onFocus(Widget sender) {
      }

      public void onLostFocus(Widget sender) {
        popup.hide();
      }

    });
    addPopupChangeListener();
    initWidget(box);
    addKeyBoardSupport();
    setController(itemController);
  }

  /**
   * Returns the current padding that is inserted after a separator. In most
   * cases this is a single space. However it can be overridden by the user.
   * 
   * @return current padding
   */
  public String getSeparatorPadding() {
    return separatorPadding;
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
   * In general, separators are followed by a single whitespace. However, it is
   * possible to override this behavior by substituting in another string,
   * including the empty string, for the separator padding value.
   * 
   * @param padding new separator padding
   */
  public void setSeparatorPadding(String padding) {
    if (padding == null) {
      throw new NullPointerException();
    }
    this.separatorPadding = padding;
  }

  /**
   * Sets the separators for the text area. The most common separator is
   * <code>","</code>.
   * <p>
   * Note: Until a KeyboardHandler is introduced that allows keyPress to detect
   * unicode characters over 16 bits, separators are restricted to 16 bit
   * values.
   * 
   * @param separators separators for the text. The String is treated as a array
   *          of characters
   */

  public void setSeparators(String separators) {
    /*
     * Implementation note: As currently we cannot support non-char unicode
     * separators, we use the more efficient char[] separators to store our
     * separators, this will change in the future.
     */
    this.separators = new char[separators.length()];
    for (int i = 0; i < separators.length(); i++) {
      this.separators[i] = separators.charAt(i);
    }
  }

  private void addKeyBoardSupport() {
    box.addKeyboardListener(new KeyboardListenerAdapter() {
      private boolean pendingCancel;

      public void onKeyDown(Widget sender, char keyCode, int modifiers) {
        pendingCancel = popup.navigate(keyCode);
      }

      public void onKeyPress(Widget sender, char keyCode, int modifiers) {
        if (pendingCancel) {
          // IE does not allow cancel key on key down, so we have pended the
          // cancellation of the key until the associated key press.
          box.cancelKey();
          pendingCancel = false;
        } else if (popup.isAttached()) {
          if (separators != null && isSeparator(keyCode)) {
            // onKeyDown/onKeyUps's keyCode for ',' comes back '1/4', so unlike
            // navigation, we use key press events to determine when the user
            // wants to simulate clicking on the popup.
            popup.click();

            // The separator will be added after the popup is activated, so the
            // popup will have already added a new separator. Therefore, the
            // original separator should not be added as well.
            box.cancelKey();
          }
        }
      }

      public void onKeyUp(Widget sender, char keyCode, int modifiers) {
        // After every user key input, refresh the popup's suggestions.
        refreshSuggestions();
      }

      /**
       * In the presence of separators, Returns the active search selection.
       */
      private String getActiveSelection(String text) {
        selectEnd = box.getCursorPos();

        // Find the last instance of a separator.
        selectStart = -1;
        for (int i = 0; i < separators.length; i++) {
          selectStart = Math.max(text.lastIndexOf(separators[i], selectEnd),
              selectStart);
        }
        ++selectStart;

        String selection = text.substring(selectStart, selectEnd).trim();
        return selection;
      }

      private void refreshSuggestions() {
        // Get the raw text.
        String text = box.getText();
        if (text.equals(cachedValue)) {
          return;
        } else {
          cachedValue = text;
        }

        // Find selection to replace.
        String selection;
        if (separators == null) {
          selection = text;
        } else {
          selection = getActiveSelection(text);
        }

        // Activate the item controller with the given selection in order to
        // show the correct items.
        if (selection.length() > 0) {
          itemController.showBelow(popup, selection, SuggestBox.this);
        } else {
          popup.hide();
        }
      }
    });
  }

  /**
   * Adds a standard popup listener to the suggest box's popup.
   */
  private void addPopupChangeListener() {
    popup.addChangeListener(new ChangeListener() {
      public void onChange(Widget sender) {
        if (separators != null) {
          onChangeWithSeparators();
        } else {
          String newValue = (String) popup.getSelectedValue();
          cachedValue = newValue;
          box.setText(cachedValue);
        }
      }

      private void onChangeWithSeparators() {
        String newValue = (String) popup.getSelectedValue();

        StringBuffer accum = new StringBuffer();
        String text = box.getText();

        // Add all text up to the selection start.
        accum.append(text.substring(0, selectStart));

        // Add one space if not at start.
        if (selectStart > 0) {
          accum.append(separatorPadding);
        }
        // Add the new value.
        accum.append(newValue);

        // Find correct cursor position.
        int savedCursorPos = accum.length();

        // Add all text after the selection end
        String ender = text.substring(selectEnd).trim();
        if (ender.length() == 0 || !isSeparator(ender.charAt(0))) {
          // Add a separator if the first char of the ender is not already a
          // separator.
          accum.append(separators[0] + separatorPadding);
          savedCursorPos = accum.length();
        }
        accum.append(ender);

        // Set the text and cursor pos to correct location.
        String replacement = accum.toString();
        cachedValue = replacement.trim();
        box.setText(replacement);
        box.setCursorPos(savedCursorPos);
      }
    });
  }

  /**
   * Convenience method for identifying if a character is a separator.
   */
  private boolean isSeparator(char candidate) {
    // An int map would be very handy right here...
    for (int i = 0; i < separators.length; i++) {
      if (candidate == separators[i]) {
        return true;
      }
    }
    return false;
  }
}
