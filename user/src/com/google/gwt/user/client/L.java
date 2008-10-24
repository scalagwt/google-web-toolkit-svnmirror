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

import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.AbstractEvent.Type;

import java.util.EventListener;

/**
 * Root of legacy listener support hierarchy.
 * 
 * @param <ListenerType> listener type
 */
@Deprecated
abstract class L<ListenerType> implements EventHandler {
  public static class HistoryChange extends L<HistoryListener> implements
      ValueChangeHandler<String> {
    @Deprecated
    public static void add(HistoryListener listener) {
      History.addValueChangeHandler(new HistoryChange(listener));
    }

    public static void remove(HandlerManager manager, HistoryListener listener) {
      baseRemove(manager, listener, ValueChangeEvent.TYPE);
    }

    protected HistoryChange(HistoryListener listener) {
      super(listener);
    }

    public void onValueChange(ValueChangeEvent<String> event) {
      listener.onHistoryChanged(event.getNewValue());
    }
  }

  public static class WindowClose extends L<WindowCloseListener> implements
      Window.ClosingHandler, CloseHandler<Window> {
    @Deprecated
    public static void add(WindowCloseListener listener) {
      WindowClose handler = new WindowClose(listener);
      Window.addWindowClosingHandler(handler);
      Window.addCloseHandler(handler);
    }

    public static void remove(HandlerManager manager,
        WindowCloseListener listener) {
      baseRemove(manager, listener, Window.ClosingEvent.TYPE, CloseEvent.TYPE);
    }

    protected WindowClose(WindowCloseListener listener) {
      super(listener);
    }

    public void onClose(CloseEvent<Window> event) {
      listener.onWindowClosed();
    }

    public void onWindowClosing(Window.ClosingEvent event) {
      String message = listener.onWindowClosing();
      if (event.getMessage() == null) {
        event.setMessage(message);
      }
    }
  }

  public static class WindowResize extends L<WindowResizeListener> implements
      ResizeHandler {
    @Deprecated
    public static void add(WindowResizeListener listener) {
      Window.addResizeHandler(new WindowResize(listener));
    }

    public static void remove(HandlerManager manager,
        WindowResizeListener listener) {
      baseRemove(manager, listener, ResizeEvent.TYPE);
    }

    protected WindowResize(WindowResizeListener listener) {
      super(listener);
    }

    public void onResize(ResizeEvent event) {
      listener.onWindowResized(event.getWidth(), event.getHeight());
    }
  }

  public static class WindowScroll extends L<WindowScrollListener> implements
      Window.ScrollHandler {
    @Deprecated
    public static void add(WindowScrollListener listener) {
      Window.addWindowScrollHandler(new WindowScroll(listener));
    }

    public static void remove(HandlerManager manager,
        WindowScrollListener listener) {
      baseRemove(manager, listener, Window.ScrollEvent.TYPE);
    }

    protected WindowScroll(WindowScrollListener listener) {
      super(listener);
    }

    public void onWindowScroll(Window.ScrollEvent event) {
      listener.onWindowScrolled(event.getScrollLeft(), event.getScrollTop());
    }
  }

  static void baseRemove(HandlerManager manager, EventListener listener,
      Type... keys) {
    if (manager != null) {
      for (Type key : keys) {
        int handlerCount = manager.getHandlerCount(key);
        for (int i = 0; i < handlerCount; i++) {
          EventHandler handler = manager.getHandler(key, i);
          if (handler instanceof L && ((L) handler).listener.equals(listener)) {
            manager.removeHandler(key, handler);
          }
        }
      }
    }
  }

  /**
   * Listener being wrapped.
   */
  protected final ListenerType listener;

  protected L(ListenerType listener) {
    this.listener = listener;
  }
}
