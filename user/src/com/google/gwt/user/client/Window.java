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
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.AbstractEvent;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.AbstractEvent.Type;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.impl.WindowImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class provides access to the browser window's methods, properties, and
 * events.
 */
public class Window {

  /**
   * This class provides access to the browser's location's object. The location
   * object contains information about the current URL and methods to manipulate
   * it. <code>Location</code> is a very simple wrapper, so not all browser
   * quirks are hidden from the user.
   * 
   */
  public static class Location {
    private static Map<String, String> paramMap;
    private static Map<String, List<String>> listParamMap;

    /**
     * Assigns the window to a new URL. All GWT state will be lost.
     * 
     * @param newURL the new URL
     */
    public static native void assign(String newURL) /*-{
      $wnd.location.assign(newURL);
    }-*/;

    /**
     * Gets the string to the right of the URL's hash.
     * 
     * @return the string to the right of the URL's hash.
     */
    public static String getHash() {
      return impl.getHash();
    }

    /**
     * Gets the URL's host and port name.
     * 
     * @return the host and port name
     */
    public static native String getHost() /*-{
      return $wnd.location.host;
    }-*/;

    /**
     * Gets the URL's host name.
     * 
     * @return the host name
     */
    public static native String getHostName() /*-{
      return $wnd.location.hostname;
    }-*/;

    /**
     * Gets the entire URL.
     * 
     * @return the URL
     */
    public static native String getHref() /*-{
      return $wnd.location.href;
    }-*/;

    /**
     * Gets the URL's parameter of the specified name. Note that if multiple
     * parameters have been specified with the same name, the last one will be
     * returned.
     * 
     * @param name the name of the URL's parameter
     * @return the value of the URL's parameter
     */
    public static String getParameter(String name) {
      ensureParameterMap();
      return paramMap.get(name);
    }

    /**
     * Returns a Map of the URL query parameters for the host page; since
     * changing the map would not change the window's location, the map returned
     * is immutable.
     * 
     * @return a map from URL query parameter names to a list of values
     */
    public static Map<String, List<String>> getParameterMap() {
      if (listParamMap == null) {
        listParamMap = buildListParamMap(getQueryString());
      }
      return listParamMap;
    }

    /**
     * Gets the path to the URL.
     * 
     * @return the path to the URL.
     */
    public static native String getPath() /*-{
      return $wnd.location.pathname;
    }-*/;

    /**
     * Gets the URL's port.
     * 
     * @return the URL's port
     */
    public static native String getPort() /*-{
      return $wnd.location.port;
    }-*/;

    /**
     * Gets the URL's protocol.
     * 
     * @return the URL's protocol.
     */
    public static native String getProtocol() /*-{
      return $wnd.location.protocol;
    }-*/;

    /**
     * Gets the URL's query string.
     * 
     * @return the URL's query string
     */
    public static String getQueryString() {
      return impl.getQueryString();
    }

    /**
     * Reloads the current browser window. All GWT state will be lost.
     */
    public static native void reload() /*-{
      $wnd.location.reload();
    }-*/;

    /**
     * Replaces the current URL with a new one. All GWT state will be lost. In
     * the browser's history, the current URL will be replaced by the new URL.
     * 
     * @param newURL the new URL
     */
    public static native void replace(String newURL) /*-{
      $wnd.location.replace(newURL);
    }-*/;

    /**
     * Builds the immutable map from String to List<String> that we'll return in
     * getParameterMap(). Package-protected for testing.
     * 
     * @return a map from the
     */
    static Map<String, List<String>> buildListParamMap(String queryString) {
      Map<String, List<String>> out = new HashMap<String, List<String>>();

      if (queryString != null && queryString.length() > 1) {
        String qs = queryString.substring(1);

        for (String kvPair : qs.split("&")) {
          String[] kv = kvPair.split("=", 2);
          if (kv[0].length() == 0) {
            continue;
          }

          List<String> values = out.get(kv[0]);
          if (values == null) {
            values = new ArrayList<String>();
            out.put(kv[0], values);
          }
          values.add(kv.length > 1 ? URL.decode(kv[1]) : "");
        }
      }

      for (Map.Entry<String, List<String>> entry : out.entrySet()) {
        entry.setValue(Collections.unmodifiableList(entry.getValue()));
      }

      out = Collections.unmodifiableMap(out);

      return out;
    }

    private static void ensureParameterMap() {
      if (paramMap == null) {
        paramMap = new HashMap<String, String>();
        String queryString = getQueryString();
        if (queryString != null && queryString.length() > 1) {
          String qs = queryString.substring(1);
          for (String kvPair : qs.split("&")) {
            String[] kv = kvPair.split("=", 2);
            if (kv.length > 1) {
              paramMap.put(kv[0], URL.decode(kv[1]));
            } else {
              paramMap.put(kv[0], "");
            }
          }
        }
      }
    }

    private Location() {
    }
  }
  
  /**
   * Fired just before the browser window closes or navigates to a different site.
   */
  public static class ClosingEvent extends AbstractEvent {
    /**
     * The event type.
     */
    public static final Type<Window.ClosingEvent, Window.ClosingHandler> TYPE = new Type<Window.ClosingEvent, Window.ClosingHandler>() {
      @Override
      protected void fire(Window.ClosingHandler handler, Window.ClosingEvent event) {
        handler.onWindowClosing(event);
      }
    };

    /**
     * The message to display to the user in an attempt to keep them on the page.
     */
    private String message = null;

    /**
     * Get the message that will be presented to the user in a confirmation dialog
     * that asks the user whether or not she wishes to navigate away from the
     * page.
     * 
     * @return the message to display to the user, or null
     */
    public String getMessage() {
      return message;
    }

    /**
     * Set the message to a <code>non-null</code> value to present a confirmation
     * dialog that asks the user whether or not she wishes to navigate away from
     * the page. If multiple handlers set the message, the last message will be
     * displayed; all others will be ignored.
     * 
     * @param message the message to display to the user, or null
     */
    public void setMessage(String message) {
      this.message = message;
    }

    @Override
    protected Type getType() {
      return TYPE;
    }
  }

  
  /**
   * Handler for {@link Window.ClosingEvent} events.
   */
  public interface ClosingHandler extends EventHandler {
    /**
     * Fired just before the browser window closes or navigates to a different
     * site. No user-interface may be displayed during shutdown.
     * 
     * @param event the event
     */
    void onWindowClosing(Window.ClosingEvent event);
  }

  /**
   * Fired when the browser window is scrolled.
   */
  public static class ScrollEvent extends AbstractEvent {
    /**
     * The event type.
     */
    public static final Type<Window.ScrollEvent, Window.ScrollHandler> TYPE = new Type<Window.ScrollEvent, Window.ScrollHandler>() {
      @Override
      protected void fire(Window.ScrollHandler handler, Window.ScrollEvent event) {
        handler.onWindowScroll(event);
      }
    };

    private int scrollLeft;
    private int scrollTop;

    /**
     * Construct a new {@link Window.ScrollEvent}.
     * 
     * @param scrollLeft the left scroll position
     * @param scrollTop the top scroll position
     */
    public ScrollEvent(int scrollLeft, int scrollTop) {
      this.scrollLeft = scrollLeft;
      this.scrollTop = scrollTop;
    }

    /**
     * Gets the window's scroll left.
     * 
     * @return window's scroll left
     */
    public int getScrollLeft() {
      return scrollLeft;
    }

    /**
     * Get the window's scroll top.
     * 
     * @return the window's scroll top
     */
    public int getScrollTop() {
      return scrollTop;
    }

    @Override
    protected Type getType() {
      return TYPE;
    }
  }
  
  /**
   * Handler for {@link Window.ScrollEvent} events.
   */
  public interface ScrollHandler extends EventHandler {
    /**
     * Fired when the browser window is scrolled.
     * 
     * @param event the event
     */
    void onWindowScroll(Window.ScrollEvent event);
  }
  
  private static boolean closeHandlersInitialized;
  private static boolean scrollHandlersInitialized;
  private static boolean resizeHandlersInitialized;
  private static final WindowImpl impl = GWT.create(WindowImpl.class);

  private static HandlerManager handlerManager;

  /**
   * Adds a {@link CloseEvent} handler.
   * 
   * @param handler the handler
   */
  public static HandlerRegistration addCloseHandler(CloseHandler<Window> handler) {
    maybeInitializeCloseHandlers();
    return addHandler(CloseEvent.TYPE, handler);
  }

  /**
   * Adds a {@link ResizeEvent} handler.
   * 
   * @param handler the handler
   */
  public static HandlerRegistration addResizeHandler(ResizeHandler handler) {
    maybeInitializeCloseHandlers();
    maybeInitializeResizeHandlers();
    return addHandler(ResizeEvent.TYPE, handler);
  }

  /**
   * Adds a listener to receive window closing events.
   * 
   * @param listener the listener to be informed when the window is closing
   */
  @Deprecated
  public static void addWindowCloseListener(WindowCloseListener listener) {
    L.WindowClose.add(listener);
  }

  /**
   * Adds a {@link Window.ClosingEvent} handler.
   * 
   * @param handler the handler
   */
  public static HandlerRegistration addWindowClosingHandler(
      ClosingHandler handler) {
    maybeInitializeCloseHandlers();
    return addHandler(Window.ClosingEvent.TYPE, handler);
  }

  /**
   * Adds a listener to receive window resize events.
   * 
   * @param listener the listener to be informed when the window is resized
   */
  @Deprecated
  public static void addWindowResizeListener(WindowResizeListener listener) {
    L.WindowResize.add(listener);
  }

  /**
   * Adds a {@link Window.ScrollEvent} handler.
   * 
   * @param handler the handler
   */
  public static HandlerRegistration addWindowScrollHandler(
      Window.ScrollHandler handler) {
    maybeInitializeCloseHandlers();
    maybeInitializeScrollHandlers();
    return addHandler(Window.ScrollEvent.TYPE, handler);
  }

  /**
   * Adds a listener to receive window scroll events.
   * 
   * @param listener the listener to be informed when the window is scrolled
   */
  @Deprecated
  public static void addWindowScrollListener(WindowScrollListener listener) {
    L.WindowScroll.add(listener);
  }

  /**
   * Displays a message in a modal dialog box.
   * 
   * @param msg the message to be displayed.
   */
  public static native void alert(String msg) /*-{
    $wnd.alert(msg);
  }-*/;

  /**
   * Displays a message in a modal dialog box, along with the standard 'OK' and
   * 'Cancel' buttons.
   * 
   * @param msg the message to be displayed.
   * @return <code>true</code> if 'OK' is clicked, <code>false</code> if
   *         'Cancel' is clicked.
   */
  public static native boolean confirm(String msg) /*-{
    return $wnd.confirm(msg);
  }-*/;

  /**
   * Use this method to explicitly disable the window's scrollbars. Applications
   * that choose to resize their user-interfaces to fit within the window's
   * client area will normally want to disable window scrolling.
   * 
   * @param enable <code>false</code> to disable window scrolling
   */
  public static void enableScrolling(boolean enable) {
    impl.enableScrolling(enable);
  }

  /**
   * Gets the height of the browser window's client area excluding the scroll
   * bar.
   * 
   * @return the window's client height
   */
  public static int getClientHeight() {
    return impl.getClientHeight();
  }

  /**
   * Gets the width of the browser window's client area excluding the vertical
   * scroll bar.
   * 
   * @return the window's client width
   */
  public static int getClientWidth() {
    return impl.getClientWidth();
  }

  /**
   * Gets the window's scroll left.
   * 
   * @return window's scroll left
   */
  public static int getScrollLeft() {
    return impl.getScrollLeft();
  }

  /**
   * Get the window's scroll top.
   * 
   * @return the window's scroll top
   */
  public static int getScrollTop() {
    return impl.getScrollTop();
  }

  /**
   * Gets the browser window's current title.
   * 
   * @return the window's title.
   */
  public static native String getTitle() /*-{
    return $doc.title;
  }-*/;

  /**
   * Opens a new browser window. The "name" and "features" arguments are
   * specified <a href=
   * 'http://developer.mozilla.org/en/docs/DOM:window.open'>here</a>.
   * 
   * @param url the URL that the new window will display
   * @param name the name of the window (e.g. "_blank")
   * @param features the features to be enabled/disabled on this window
   */
  public static native void open(String url, String name, String features) /*-{
    $wnd.open(url, name, features);
  }-*/;

  /**
   * Prints the document in the window, as if the user had issued a "Print"
   * command.
   */
  public static native void print() /*-{
    $wnd.print();
  }-*/;

  /**
   * Displays a request for information in a modal dialog box, along with the
   * standard 'OK' and 'Cancel' buttons.
   * 
   * @param msg the message to be displayed
   * @param initialValue the initial value in the dialog's text field
   * @return the value entered by the user if 'OK' was pressed, or
   *         <code>null</code> if 'Cancel' was pressed
   */
  public static native String prompt(String msg, String initialValue) /*-{
    return $wnd.prompt(msg, initialValue);
  }-*/;

  /**
   * Removes a window closing listener.
   * 
   * @param listener the listener to be removed
   */
  @Deprecated
  public static void removeWindowCloseListener(WindowCloseListener listener) {
    L.WindowClose.remove(getHandlers(), listener);
  }

  /**
   * Removes a window resize listener.
   * 
   * @param listener the listener to be removed
   */
  @Deprecated
  public static void removeWindowResizeListener(WindowResizeListener listener) {
    L.WindowResize.remove(getHandlers(), listener);
  }

  /**
   * Removes a window scroll listener.
   * 
   * @param listener the listener to be removed
   */
  @Deprecated
  public static void removeWindowScrollListener(WindowScrollListener listener) {
    L.WindowScroll.remove(getHandlers(), listener);
  }

  /**
   * Scroll the window to the specified position.
   * 
   * @param left the left scroll position
   * @param top the top scroll position
   */
  public static native void scrollTo(int left, int top) /*-{
    $wnd.scrollTo(left, top);
  }-*/;

  /**
   * Sets the size of the margins used within the window's client area. It is
   * sometimes necessary to do this because some browsers, such as Internet
   * Explorer, add margins by default, which can confound attempts to resize
   * panels to fit exactly within the window.
   * 
   * @param size the window's new margin size, in CSS units.
   */
  public static native void setMargin(String size) /*-{
    $doc.body.style.margin = size;
  }-*/;

  /**
   * Sets the status text for the window. Calling this method in Firefox has no
   * effect.
   * 
   * @param status the new message to display.
   */
  public static native void setStatus(String status) /*-{
    $wnd.status = status;
  }-*/;

  /**
   * Sets the browser window's title.
   * 
   * @param title the new window title.
   */
  public static native void setTitle(String title) /*-{
    $doc.title = title;
  }-*/;

  static void onClosed() {
    UncaughtExceptionHandler handler = GWT.getUncaughtExceptionHandler();
    if (handler != null) {
      fireClosedAndCatch(handler);
    } else {
      fireClosedImpl();
    }
  }

  static String onClosing() {
    UncaughtExceptionHandler handler = GWT.getUncaughtExceptionHandler();
    if (handler != null) {
      return fireClosingAndCatch(handler);
    } else {
      return fireClosingImpl();
    }
  }

  static void onResize() {
    UncaughtExceptionHandler handler = GWT.getUncaughtExceptionHandler();
    if (handler != null) {
      fireResizedAndCatch(handler);
    } else {
      fireResizedImpl();
    }
  }

  static void onScroll() {
    UncaughtExceptionHandler handler = GWT.getUncaughtExceptionHandler();
    if (handler != null) {
      fireScrollAndCatch(handler);
    } else {
      fireScrollImpl();
    }
  }

  /**
   * Adds this handler to the Window.
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

  private static void fireClosedAndCatch(UncaughtExceptionHandler handler) {
    try {
      fireClosedImpl();
    } catch (Throwable e) {
      handler.onUncaughtException(e);
    }
  }

  private static void fireClosedImpl() {
    fireEvent(new CloseEvent<Window>(null));
  }

  private static String fireClosingAndCatch(UncaughtExceptionHandler handler) {
    try {
      return fireClosingImpl();
    } catch (Throwable e) {
      handler.onUncaughtException(e);
      return null;
    }
  }

  private static String fireClosingImpl() {
    Window.ClosingEvent event = new Window.ClosingEvent();
    fireEvent(event);
    return event.getMessage();
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

  private static void fireResizedAndCatch(UncaughtExceptionHandler handler) {
    try {
      fireResizedImpl();
    } catch (Throwable e) {
      handler.onUncaughtException(e);
    }
  }

  private static void fireResizedImpl() {
    fireEvent(new ResizeEvent(getClientWidth(), getClientHeight()));
  }

  private static void fireScrollAndCatch(UncaughtExceptionHandler handler) {
    try {
      fireScrollImpl();
    } catch (Throwable e) {
      handler.onUncaughtException(e);
    }
  }

  private static void fireScrollImpl() {
    fireEvent(new Window.ScrollEvent(getScrollLeft(), getScrollTop()));
  }

  /**
   * Returns the {@link HandlerManager} used for event management.
   * 
   * @return the handler manager
   */
  private static HandlerManager getHandlers() {
    return handlerManager;
  }

  private static void maybeInitializeCloseHandlers() {
    if (GWT.isClient() && !closeHandlersInitialized) {
      closeHandlersInitialized = true;
      impl.initWindowCloseHandler();
    }
  }

  private static void maybeInitializeResizeHandlers() {
    if (GWT.isClient() && !resizeHandlersInitialized) {
      resizeHandlersInitialized = true;
      impl.initWindowResizeHandler();
    }
  }

  private static void maybeInitializeScrollHandlers() {
    if (GWT.isClient() && !scrollHandlersInitialized) {
      scrollHandlersInitialized = true;
      impl.initWindowScrollHandler();
    }
  }

  private Window() {
  }
}
