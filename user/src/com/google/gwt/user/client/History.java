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
package com.google.gwt.user.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.impl.HistoryImpl;

/**
 * This class allows you to interact with the browser's history stack. Each
 * "item" on the stack is represented by a single string, referred to as a
 * "token". You can create new history items (which have a token associated with
 * them when they are created), and you can programmatically force the current
 * history to move back or forward.
 * 
 * <p>
 * In order to receive notification of user-directed changes to the current
 * history item, implement the
 * {@link com.google.gwt.user.client.HistoryChangeHandler} interface and attach
 * it via {@link #addHistoryListener}.
 * </p>
 * 
 * <p>
 * <h3>Example</h3>
 * {@example com.google.gwt.examples.HistoryExample}
 * </p>
 * 
 * <p>
 * <h3>URL Encoding</h3>
 * Any valid characters may be used in the history token and will survive
 * round-trips through {@link #newItem(String)} to {@link #getToken()}/
 * {@link HistoryChangeHandler#onHistoryChanged(HistoryChangeEvent)},
 * but most will be encoded in the user-visible URL. The following US-ASCII
 * characters are not encoded on any currently supported browser (but may be in
 * the future due to future browser changes):
 * <ul>
 * <li>a-z
 * <li>A-Z
 * <li>0-9
 * <li>;,/?:@&=+$-_.!~*()
 * </ul>
 * </p>
 */
public class History {

  private static HistoryImpl impl;

  static {
    impl = GWT.create(HistoryImpl.class);
    if (!impl.init()) {
      // Set impl to null as a flag to no-op future calls.
      impl = null;

      // Tell the user.
      GWT.log("Unable to initialize the history subsystem; did you "
          + "include the history frame in your host page? Try "
          + "<iframe src=\"javascript:''\" id='__gwt_historyFrame' "
          + "style='position:absolute;width:0;height:0;border:0'>"
          + "</iframe>", null);
    }
  }

  /**
   * Adds a {@link HistoryChangeEvent} handler to be informed of changes to the
   * browser's history stack.
   * 
   * @param handler the handler
   */
  public static HandlerRegistration addHistoryChangeHandler(
      HistoryChangeHandler handler) {
    return HistoryImpl.addHistoryChangeHandler(handler);
  }

  /**
   * Adds a listener to be informed of changes to the browser's history stack.
   * 
   * @param listener the listener to be added
   */
  @Deprecated
  public static void addHistoryListener(HistoryListener listener) {
    L.HistoryChange.add(listener);
  }

  /**
   * Programmatic equivalent to the user pressing the browser's 'back' button.
   * 
   * Note that this does not work correctly on Safari 2.
   */
  public static native void back() /*-{
    $wnd.history.back();
  }-*/;

  /**
   * Fire {@link HistoryChangeHandler#onHistoryChanged(HistoryChangeEvent)}
   * events with the current history state. This is most often called at the end
   * of an application's
   * {@link com.google.gwt.core.client.EntryPoint#onModuleLoad()} to inform
   * history listeners of the initial application state.
   */
  public static void fireCurrentHistoryState() {
    HistoryImpl.fireHistoryChangedImpl(getToken());
  }

  /**
   * Programmatic equivalent to the user pressing the browser's 'forward'
   * button.
   */
  public static native void forward() /*-{
    $wnd.history.forward();
  }-*/;

  /**
   * Gets the current history token. The listener will not receive a
   * {@link HistoryChangeHandler#onHistoryChanged(HistoryChangeEvent)} event for
   * the initial token; requiring that an application request the token
   * explicitly on startup gives it an opportunity to run different
   * initialization code in the presence or absence of an initial token.
   * 
   * @return the initial token, or the empty string if none is present.
   */
  public static String getToken() {
    return impl != null ? HistoryImpl.getToken() : "";
  }

  /**
   * Adds a new browser history entry. In hosted mode, the 'back' and 'forward'
   * actions are accessible via the standard Alt-Left and Alt-Right keystrokes.
   * Calling this method will cause
   * {@link HistoryChangeHandler#onHistoryChanged(HistoryChangeEvent)} to be
   * called as well.
   * 
   * @param historyToken the token to associate with the new history item
   */
  public static void newItem(String historyToken) {
    newItem(historyToken, true);
  }

  /**
   * Adds a new browser history entry. In hosted mode, the 'back' and 'forward'
   * actions are accessible via the standard Alt-Left and Alt-Right keystrokes.
   * Calling this method will cause
   * {@link HistoryChangeHandler#onHistoryChanged(HistoryChangeEvent)} to be
   * called as well if and only if issueEvent is true.
   * 
   * @param historyToken the token to associate with the new history item
   * @param issueEvent true if a
   *          {@link HistoryChangeHandler#onHistoryChanged(HistoryChangeEvent)}
   *          event should be issued
   */
  public static void newItem(String historyToken, boolean issueEvent) {
    if (impl != null) {
      impl.newItem(historyToken, issueEvent);
    }
  }

  /**
   * Call all history listeners with the specified token. Note that this does
   * not change the history system's idea of the current state and is only kept
   * for backward compatibility. To fire history events for the initial state of
   * the application, instead call {@link #fireCurrentHistoryState()} from the
   * application {@link com.google.gwt.core.client.EntryPoint#onModuleLoad()}
   * method.
   * 
   * @param historyToken history token to fire events for
   * @deprecated Use {@link #fireCurrentHistoryState()} instead.
   */
  @Deprecated
  public static void onHistoryChanged(String historyToken) {
    HistoryImpl.fireHistoryChangedImpl(historyToken);
  }

  /**
   * Removes a history listener.
   * 
   * @param listener the listener to be removed
   */
  @Deprecated
  public static void removeHistoryListener(HistoryListener listener) {
    L.HistoryChange.remove(HistoryImpl.getHandlers(), listener);
  }
}
