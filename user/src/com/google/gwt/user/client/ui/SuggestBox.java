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

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Suggest.Callback;
import com.google.gwt.user.client.ui.Suggest.Oracle;
import com.google.gwt.user.client.ui.Suggest.Request;
import com.google.gwt.user.client.ui.Suggest.Response;
import com.google.gwt.user.client.ui.impl.ItemPickerDropDownImpl;

import java.util.Collection;

/**
 * Wrapper for a {@link TextBox} or {@link TextArea} with a built in
 * {@link Oracle} that is used to provide the users with a set of items for the
 * text widget.
 * 
 * 
 * <p>
 * By default, the {@link SuggestBox} uses a {@link Suggest.DefaultOracle} as
 * its oracle and a {@link SuggestPicker} as its {@link ItemPicker}. Below we
 * show how a {@link Suggest.DefaultOracle} can be configured:
 * </p>
 * 
 * <pre> 
 *   Suggest.DefaultOracle oracle = new Suggest.DefaultOracle();  
 *   oracle.add("Cat");
 *   oracle.add("Dog");
 *   oracle.add("Horse");
 *   oracle.add("Canary");
 * </pre>
 * 
 * Using the example above, if the user types "C" into the text widget, the
 * oracle will configure the suggestions with the "Cat" and "Canary"
 * suggestions. Specifically, whenever the user types a key into the text
 * widget, the value is submitted to the <code>Suggest.DefaultOracle</code>.
 * <p>
 * The user may optionally provide separators to the <code>SuggestBox</code>
 * widget. When separators are defined, only the text between the nearest
 * separators is sent to the oracle rather than the entire text widget. So, to
 * continue the example above, if "," is a separator and the text widget
 * contains "dog, ca" then again the oracle will configure the suggestions with
 * "Cat" and "Canary".
 * </p>
 * 
 * <p>
 * A user may supply their own {@link ItemPicker} rather than relying on the
 * built in {@link SuggestPicker}; however the {@link ItemPicker} must be able
 * to consume the results of the defined {@link Oracle} and respect the
 * {@link ItemPicker} contract that each value returned is convertible to a
 * human-readable {@link String}.
 * </p>
 */
public class SuggestBox extends Composite implements HasText, HasFocus,
    Suggest.HasOracle, SourcesClickEvents, SourcesFocusEvents,
    SourcesChangeEvents, SourcesKeyboardEvents {

  private int limit = 20;
  private int selectStart;
  private int selectEnd;
  private Oracle oracle;
  private char[] separators;
  private String cachedValue;
  private final ItemPickerDropDownImpl popup;
  private final TextBoxBase box;
  private DelegatingClickListenerCollection clickListeners;
  private DelegatingChangeListenerCollection changeListeners;
  private DelegatingFocusListenerCollection focusListeners;
  private DelgatingKeyboardListenerCollection keyboardListeners;

  private String separatorPadding = " ";

  private final Callback callBack = new Callback() {

    public void onSuggestionsReceived(Request request, Response response) {
      showSuggestions(response.getSuggestions());
    }
  };

  /**
   * Constructor for <code>SuggestBox</code>.
   */
  public SuggestBox() {
    this(new Suggest.DefaultOracle());
  }

  /**
   * 
   * Constructor for <code>SuggestBox</code>.
   * 
   * @param picker picker to use for this <code>SuggestBox</code>
   */
  public SuggestBox(ItemPicker picker) {
    this(new Suggest.DefaultOracle(), picker);
  }

  /**
   * 
   * Constructor for <code>SuggestBox</code>.
   * 
   * @param picker picker to use for this SuggestBox
   * @param box the text widget
   */
  public SuggestBox(ItemPicker picker, TextBoxBase box) {
    this(new Suggest.DefaultOracle(), picker, box);
  }

  /**
   * 
   * Constructor for <code>SuggestBox</code>.
   * 
   * @param oracle the oracle for this <code>SuggestBox</code>
   */

  public SuggestBox(Oracle oracle) {
    this(oracle, new TextBox());
  }

  /**
   * 
   * Constructor for <code>SuggestBox</code>.
   * 
   * @param oracle supplies suggestions based upon the current contents of the
   *          text widget. Note, a single oracle may be used with multiple
   *          suggest boxes.
   * @param picker picker to use for this SuggestBox
   */
  public SuggestBox(Oracle oracle, ItemPicker picker) {
    this(oracle, picker, new TextBox());
  }

  /**
   * 
   * Constructor for <code>SuggestBox</code>.
   * 
   * @param oracle supplies suggestions based upon the current contents of the
   *          text widget. Note, a single oracle may be used with multiple
   *          suggest boxes.
   * @param picker picker to use for this SuggestBox
   * @param box the text widget
   */
  public SuggestBox(Oracle oracle, ItemPicker picker, TextBoxBase box) {
    this.box = box;
    initWidget(box);
    this.popup = new ItemPickerDropDownImpl(this, picker);
    addPopupChangeListener();
    addKeyBoardSupport();
    setOracle(oracle);
  }

  /**
   * 
   * Constructor for <code>SuggestBox</code>.
   * 
   * @param oracle the oracle
   * @param box the box
   */
  public SuggestBox(Oracle oracle, TextBoxBase box) {
    this(oracle, new SuggestPicker(), box);
  }

  /**
   * 
   * Constructor for <code>SuggestBox</code>.
   * 
   * @param box the text widget
   */
  public SuggestBox(TextBoxBase box) {
    this(new SuggestPicker(), box);
  }

  public void addChangeListener(ChangeListener listener) {
    if (changeListeners == null) {
      changeListeners = new DelegatingChangeListenerCollection(this, box);
    }
    changeListeners.add(listener);
  }

  public void addClickListener(ClickListener listener) {
    if (clickListeners == null) {
      clickListeners = new DelegatingClickListenerCollection(this, box);
    }
    clickListeners.add(listener);
  }

  public void addFocusListener(FocusListener listener) {
    if (focusListeners == null) {
      focusListeners = new DelegatingFocusListenerCollection(this, box);
    }
    focusListeners.add(listener);
  }

  public void addKeyboardListener(KeyboardListener listener) {
    if (keyboardListeners == null) {
      keyboardListeners = new DelgatingKeyboardListenerCollection(this, box);
    }
    keyboardListeners.add(listener);
  }

  /**
   * Gets the maximum number of suggestions that should be displayed for this
   * box. It is up to the current {@link Oracle} to enforce this limit.
   * 
   * @return the limit
   */
  public int getLimit() {
    return limit;
  }

  /**
   * Returns the padding that is inserted after a separator. By default, this is
   * a single space.
   * 
   * @return current padding
   */
  public String getSeparatorPadding() {
    return separatorPadding;
  }

  public Oracle getSuggestOracle() {
    return oracle;
  }

  public int getTabIndex() {
    return box.getTabIndex();
  }

  public String getText() {
    return box.getText();
  }

  public void removeChangeListener(ChangeListener listener) {
    if (clickListeners != null) {
      clickListeners.remove(listener);
    }
  }

  public void removeClickListener(ClickListener listener) {
    if (clickListeners != null) {
      clickListeners.remove(listener);
    }
  }

  public void removeFocusListener(FocusListener listener) {
    if (focusListeners != null) {
      focusListeners.remove(listener);
    }
  }

  public void removeKeyboardListener(KeyboardListener listener) {
    if (keyboardListeners != null) {
      keyboardListeners.remove(listener);
    }
  }

  public void setAccessKey(char key) {
    box.setAccessKey(key);
  }

  public void setFocus(boolean focused) {
    box.setFocus(focused);
  }

  /**
   * Sets the limit to the number of suggestions the oracle should provide. It
   * is up to the oracle to enforce this limit.
   * 
   * @param limit the limit to the number of suggestions provided
   */
  public void setLimit(int limit) {
    this.limit = limit;
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
   * Sets the separators for the text. The most common separator is
   * <code>","</code>.
   * <p>
   * Note: Until a KeyboardHandler is introduced that allows keyPress to detect
   * unicode characters over 16 bits, separators are restricted to 16 bit
   * values.
   * </p>
   * 
   * @param separators separators for the text. The separators value is treated
   *          as a array of characters
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

  public void setTabIndex(int index) {
    box.setTabIndex(index);
  }

  public void setText(String text) {
    box.setText(text);
  }

  public String toString() {
    return GWT.getTypeName(this) + this.getText();
  }

  /**
   * Show the given iterator of suggestions in an {@link ItemPicker}. The
   * {@link ItemPicker} must be able to parse the given iterator or a runtime
   * error will result. The default {@link SuggestPicker} only can parse an
   * iterator of objects with valid {@link String#toString()} methods.
   * 
   * @param suggestions suggestions to show
   */
  protected void showSuggestions(Collection suggestions) {
    if (suggestions.size() > 0) {
      popup.setItems(suggestions);
      popup.show();
    }
  }

  private void addKeyBoardSupport() {
    box.addKeyboardListener(new KeyboardListenerAdapter() {
      private boolean pendingCancel;

      public void onKeyDown(Widget sender, char keyCode, int modifiers) {
        pendingCancel = popup.delegateKeyDown(keyCode);
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
            popup.commitSelection();

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
          selectStart = Math.max(
              text.lastIndexOf(separators[i], selectEnd - 1), selectStart);
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
        // If we have no text, let's not show the suggestions.
        if (selection.length() == 0) {
          popup.hide();
        } else {
          showSuggestions(selection);
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
          String newValue = popup.getSelectedValue().toString();
          cachedValue = newValue;
          box.setText(cachedValue);
        }
        if (changeListeners != null) {
          changeListeners.fireChange(SuggestBox.this);
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

  /**
   * Sets the suggestion oracle used to create suggestions.
   * 
   * @param oracle the oracle
   */
  private void setOracle(Oracle oracle) {
    this.oracle = oracle;
  }

  private void showSuggestions(String query) {
    oracle.requestSuggestions(new Request(query, limit), callBack);
  }
}
