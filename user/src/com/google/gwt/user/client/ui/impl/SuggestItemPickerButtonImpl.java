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

import com.google.gwt.user.client.ui.DefaultSuggestOracle;
import com.google.gwt.user.client.ui.HasSuggestOracle;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.PopupListener;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracleCallback;
import com.google.gwt.user.client.ui.SuggestOracleRequest;
import com.google.gwt.user.client.ui.SuggestOracleResponse;

/**
 * A {@link ItemPickerButtonImpl} with a associated {@link SuggestOracle} to
 * populate the button's items.
 */
public class SuggestItemPickerButtonImpl extends ItemPickerButtonImpl implements
    HasSuggestOracle {

  private StringBuffer accum = new StringBuffer();

  private final SuggestOracle oracle;

  private SuggestOracleCallback callBack = new SuggestOracleCallback() {

    public void onSuggestionsRecieved(SuggestOracleRequest request,
        SuggestOracleResponse response) {
      if (request.getQuery().trim().equals(accum.toString().trim())) {
        getPopup().setItems(response.iterator());
        SuggestItemPickerButtonImpl.super.showPopup();
      } else {
        getPopup().hide();
      }
    }
  };

  {
    // If the popup is closed for any reason, set the button to be depressed.
    getPopup().addPopupListener(new PopupListener() {
      public void onPopupClosed(PopupPanel sender, boolean autoClosed) {
        accum.setLength(0);
      }
    });
  }

  /**
   * 
   * Constructor for <code>SuggestDropDownButton</code>.
   * 
   * @param upText text for up image
   * @param oracle suggestion oracle
   */
  public SuggestItemPickerButtonImpl(String upText, SuggestOracle oracle) {
    this.oracle = oracle;
    getUpFace().setText(upText);
  }

  /**
   * 
   * Default Constructor for <code>SuggestDropDownButton</code>.
   * 
   * 
   */
  public SuggestItemPickerButtonImpl() {
    this.oracle = new DefaultSuggestOracle();
  }

  public SuggestItemPickerButtonImpl(DefaultSuggestOracle oracle) {
    this.oracle = oracle;
  }

  public SuggestOracle getOracle() {
    return oracle;
  }

  /**
   * Shows the popup.
   */
  public void showPopup() {
    SuggestOracleRequest request = new SuggestOracleRequest(accum.toString());
    oracle.requestSuggestions(request, callBack);
  }

  void handleKeyPress(char keyCode) {
    boolean navigate = getPopup().navigate(keyCode);
    if (navigate) {
      return;
    }
    if (Character.isLetterOrDigit(keyCode)) {
      accum.append(keyCode);
    } else if (keyCode == KeyboardListener.KEY_BACKSPACE) {
      accum.setLength(Math.max(0, accum.length() - 1));
    } else {
      accum.setLength(0);
    }
    showPopup();
  }
}
