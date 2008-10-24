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
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.AbstractEvent;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;

/**
 * Native implementation associated with
 * {@link com.google.gwt.user.client.History}.
 * 
 * User classes should not use this class directly.
 */
public abstract class HistoryImpl {

  private static HandlerManager handlerManager;

  /**
   * Adds a {@link ValueChangeEvent} handler to be informed of changes to the
   * browser's history stack.
   * 
   * @param handler the handler
   */
  public static HandlerRegistration addValueChangeHandler(
      ValueChangeHandler<String> handler) {
    return addHandler(ValueChangeEvent.TYPE, handler);
  }

  /**
   * Fires the {@link ValueChangeEvent} to all handlers with the given tokens.
   */
  public static void fireHistoryChangedImpl(String oldToken, String newToken) {
    fireEvent(new ValueChangeEvent<String>(oldToken, newToken));
  }

  /**
   * Returns the {@link HandlerManager} used for event management.
   * 
   * @return the handler manager
   */
  public static HandlerManager getHandlers() {
    return handlerManager;
  }

  public static native String getToken() /*-{
    return $wnd.__gwt_historyToken || "";
  }-*/;

  protected static native void setToken(String token) /*-{
    $wnd.__gwt_historyToken = token;
  }-*/;

  /**
   * Adds this handler to the History.
   * 
   * @param <HandlerType> the type of handler to add
   * @param type the event type
   * @param handler the handler
   * @return {@link HandlerRegistration} used to remove the handler
   */
  private static <HandlerType extends EventHandler> HandlerRegistration addHandler(
      AbstractEvent.Type<?, HandlerType> type, final HandlerType handler) {
    return ensureHandlers().addHandler(type, handler);
  }

  /**
   * Returns the {@link HandlerManager}, ensuring it exists.
   * 
   * @return the handler manager
   */
  private static HandlerManager ensureHandlers() {
    if (handlerManager == null) {
      handlerManager = new HandlerManager(null);
    }
    return handlerManager;
  }

  /**
   * Fires an event.
   * 
   * @param event the event
   */
  private static void fireEvent(AbstractEvent event) {
    if (handlerManager != null) {
      handlerManager.fireEvent(event);
    }
  }

  private static void fireHistoryChanged(String oldToken, String newToken) {
    UncaughtExceptionHandler handler = GWT.getUncaughtExceptionHandler();
    if (handler != null) {
      fireHistoryChangedAndCatch(oldToken, newToken, handler);
    } else {
      fireHistoryChangedImpl(oldToken, newToken);
    }
  }

  private static void fireHistoryChangedAndCatch(String oldToken,
      String newToken, UncaughtExceptionHandler handler) {
    try {
      fireHistoryChangedImpl(oldToken, newToken);
    } catch (Throwable e) {
      handler.onUncaughtException(e);
    }
  }

  public abstract boolean init();

  public final void newItem(String historyToken, boolean issueEvent) {
    String oldToken = getToken();
    historyToken = (historyToken == null) ? "" : historyToken;
    if (!historyToken.equals(getToken())) {
      setToken(historyToken);
      nativeUpdate(historyToken);
      if (issueEvent) {
        fireHistoryChangedImpl(oldToken, historyToken);
      }
    }
  }

  public final void newItemOnEvent(String historyToken) {
    String oldToken = getToken();
    historyToken = (historyToken == null) ? "" : historyToken;
    if (!historyToken.equals(getToken())) {
      setToken(historyToken);
      nativeUpdateOnEvent(historyToken);
      fireHistoryChanged(oldToken, historyToken);
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

  protected abstract void nativeUpdate(String historyToken);

  protected abstract void nativeUpdateOnEvent(String historyToken);
}
