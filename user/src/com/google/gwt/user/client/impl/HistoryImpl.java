/*
 * Copyright 2008 Google Inc.
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
package com.google.gwt.user.client.impl;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

/**
 * Native implementation associated with
 * {@link com.google.gwt.user.client.History}. User classes should not use this
 * class directly.
 * 
 * <p>
 * This base version uses the HTML5 standard window.onhashchange event to
 * determine when the URL hash identifier changes.
 * </p>
 */
public class HistoryImpl implements HasValueChangeHandlers<String>, HasHandlers {

  public static native String getToken() /*-{
     return $wnd.__gwt_historyToken || "";
   }-*/;

  protected static native void setToken(String token) /*-{
     $wnd.__gwt_historyToken = token;
   }-*/;

  private HandlerManager handlers = new HandlerManager(null);

  /**
   * Adds a {@link ValueChangeEvent} handler to be informed of changes to the
   * browser's history stack.
   * 
   * @param handler the handler
   */
  public HandlerRegistration addValueChangeHandler(
      ValueChangeHandler<String> handler) {
    return handlers.addHandler(ValueChangeEvent.getType(), handler);
  }

  public void fireEvent(GwtEvent<?> event) {
    handlers.fireEvent(event);
  }

  /**
   * Fires the {@link ValueChangeEvent} to all handlers with the given tokens.
   */
  public void fireHistoryChangedImpl(String newToken) {
    if ((newToken.length() > 0) && (newToken.charAt(0) == '!')) {
      newToken = newToken.substring(1);
    }
    ValueChangeEvent.fire(this, newToken);
  }

  public HandlerManager getHandlers() {
    return handlers;
  }

  public native boolean init() /*-{
     var token = '';

     // Get the initial token from the url's hash component.
     var hash = $wnd.location.hash;
     if (hash.length > 0) {
       token = this.@com.google.gwt.user.client.impl.HistoryImpl::decodeFragment(Ljava/lang/String;)(hash.substring(1));
     }

     @com.google.gwt.user.client.impl.HistoryImpl::setToken(Ljava/lang/String;)(token);

     var historyImpl = this;
     $wnd.onhashchange = function() {
       var token = '', hash = $wnd.location.hash;
       if (hash.length > 0) {
         token = historyImpl.@com.google.gwt.user.client.impl.HistoryImpl::decodeFragment(Ljava/lang/String;)(hash.substring(1));
       }

       historyImpl.@com.google.gwt.user.client.impl.HistoryImpl::newItemOnEvent(Ljava/lang/String;)(token);
     };

     return true;
   }-*/;

  public final void newItem(String historyToken, boolean issueEvent) {
    historyToken = (historyToken == null) ? "" : historyToken;
    if (!historyToken.equals(getToken())) {
      setToken(historyToken);
      nativeUpdate(historyToken);
      if (issueEvent) {
        fireHistoryChangedImpl(historyToken);
      }
    }
  }

  public final void newItemOnEvent(String historyToken) {
    historyToken = (historyToken == null) ? "" : historyToken;
    if (!historyToken.equals(getToken())) {
      setToken(historyToken);
      nativeUpdateOnEvent(historyToken);
      fireHistoryChanged(historyToken);
    }
  }

  protected native String decodeFragment(String encodedFragment) /*-{
     // decodeURI() does *not* decode the '#' character.
     return decodeURI(encodedFragment.replace("%23", "#"));
   }-*/;

  protected native String encodeFragment(String fragment) /*-{
     // encodeURI() does *not* encode the '#' character.
     return encodeURI(fragment).replace("#", "%23");
   }-*/;

  /**
   * The standard updateHash implementation assigns to location.hash() with an
   * encoded history token.
   */
  protected native void nativeUpdate(String historyToken) /*-{
     $wnd.location.hash = this.@com.google.gwt.user.client.impl.HistoryImpl::encodeFragment(Ljava/lang/String;)(historyToken);
   }-*/;

  protected void nativeUpdateOnEvent(String historyToken) {
    // Do nothing, the hash is already updated.
  }

  private void fireHistoryChanged(String newToken) {
    UncaughtExceptionHandler handler = GWT.getUncaughtExceptionHandler();
    if (handler != null) {
      fireHistoryChangedAndCatch(newToken, handler);
    } else {
      fireHistoryChangedImpl(newToken);
    }
  }

  private void fireHistoryChangedAndCatch(String newToken,
      UncaughtExceptionHandler handler) {
    try {
      fireHistoryChangedImpl(newToken);
    } catch (Throwable e) {
      handler.onUncaughtException(e);
    }
  }
}
