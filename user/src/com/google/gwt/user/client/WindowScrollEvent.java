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

import com.google.gwt.event.shared.AbstractEvent;

/**
 * Fired when the browser window is scrolled.
 */
public class WindowScrollEvent extends AbstractEvent {
  /**
   * The event type.
   */
  public static final Type<WindowScrollEvent, WindowScrollHandler> TYPE = new Type<WindowScrollEvent, WindowScrollHandler>() {
    @Override
    protected void fire(WindowScrollHandler handler, WindowScrollEvent event) {
      handler.onWindowScroll(event);
    }
  };

  private int scrollLeft;
  private int scrollTop;

  /**
   * Construct a new {@link WindowScrollEvent}.
   * 
   * @param scrollLeft the left scroll position
   * @param scrollTop the top scroll position
   */
  public WindowScrollEvent(int scrollLeft, int scrollTop) {
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
