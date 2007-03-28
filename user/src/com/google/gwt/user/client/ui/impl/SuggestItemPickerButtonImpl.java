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

import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.PopupListener;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Suggest;
import com.google.gwt.user.client.ui.Suggest.Callback;
import com.google.gwt.user.client.ui.Suggest.HasOracle;
import com.google.gwt.user.client.ui.Suggest.Oracle;
import com.google.gwt.user.client.ui.Suggest.Request;
import com.google.gwt.user.client.ui.Suggest.Response;

import java.util.Collection;

/**
 * A {@link ItemPickerButtonImpl} with a associated {@link Oracle} to populate
 * the button's items.
 */
public class SuggestItemPickerButtonImpl extends ItemPickerButtonImpl implements
    HasOracle {

  private StringBuffer accum = new StringBuffer();

  private final Oracle oracle;

  private Collection defaultSuggestions;

  private Callback callBack = new Callback() {

    public void onSuggestionsReceived(Request request, Response response) {
      if (request.getQuery().trim().equals(accum.toString().trim())) {
        Collection suggestions = response.getSuggestions();
        if (suggestions.size() == 0) {
          suggestions = defaultSuggestions;
        }
        getPopup().setItems(suggestions);
        // Compiler bug requires us to inline the following call.
        // SuggestItemPickerButtonImpl.super.showPopup();

        // Start inline.
        setDown(true);
        getPopup().show();
        // End inline.

      } else {
        getPopup().hide();
      }
    }
  };

  {
    // If the popup is closed for any reason, clear the accum.
    getPopup().addPopupListener(new PopupListener() {
      public void onPopupClosed(PopupPanel sender, boolean autoClosed) {
        accum.setLength(0);
      }
    });
  }

  /**
   * Default Constructor.
   */
  public SuggestItemPickerButtonImpl() {
    this.oracle = new Suggest.DefaultOracle();
  }

  /**
   * Constructor for {@link SuggestItemPickerButtonImpl}.
   * 
   * @param upText text for up image
   * @param oracle suggestion oracle
   * @param defaultSuggestions to show when the oracle comes up empty
   */
  public SuggestItemPickerButtonImpl(String upText, Oracle oracle,
      Collection defaultSuggestions) {
    this.oracle = oracle;
    getUpFace().setText(upText);
    setDefaultSuggestions(defaultSuggestions);
  }

  /**
   * 
   * Constructor for {@SuggestItemPickerButtonImpl}.
   * 
   * @param oracle the oracle
   */
  public SuggestItemPickerButtonImpl(Oracle oracle) {
    this.oracle = oracle;
  }

  /**
   * Gets the suggest oracle.
   */
  public Oracle getSuggestOracle() {
    return oracle;
  }

  /**
   * Sets the default items to show when there are no valid suggestions.
   * 
   * @param defaultSuggestions default suggestions
   * 
   */
  public void setDefaultSuggestions(Collection defaultSuggestions) {
    this.defaultSuggestions = defaultSuggestions;
  }

  /**
   * Shows the popup.
   */
  protected void showItemPicker() {
    Request request = new Request(accum.toString());
    oracle.requestSuggestions(request, callBack);
  }

  /**
   * Delegates key down.
   */
  void handleKeyDown(char keyCode) {

    // Adjust accum.
    if (Character.isLetterOrDigit(keyCode)) {
      accum.append(keyCode);
    } else if (keyCode == KeyboardListener.KEY_BACKSPACE) {
      accum.setLength(Math.max(0, accum.length() - 1));
    } else {
      accum.setLength(0);
    }
    if (keyCode != KeyboardListener.KEY_TAB) {
      showItemPicker();
    }
  }

}
