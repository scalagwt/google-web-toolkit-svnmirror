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
 * Fired just before the browser window closes or navigates to a different site.
 */
public class WindowClosingEvent extends AbstractEvent {
  /**
   * The event type.
   */
  public static final Type<WindowClosingEvent, WindowClosingHandler> TYPE = new Type<WindowClosingEvent, WindowClosingHandler>() {
    @Override
    protected void fire(WindowClosingHandler handler, WindowClosingEvent event) {
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
