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
package com.google.gwt.event.dom.client;

import com.google.gwt.user.client.Event;

/**
 * Represents a native key down event.
 */
public class KeyDownEvent extends KeyCodeEvent<KeyDownHandler> {

  /**
   * Event type for key down events. Represents the meta-data associated with
   * this event.
   */
  private static Type<KeyDownHandler> TYPE = new Type<KeyDownHandler>(
      Event.ONKEYDOWN, "keydown", new KeyDownEvent());

  /**
   * Ensures the existence of the handler TYPE, so the system knows to start
   * firing events and then returns it.
   * 
   * @return the handler TYPE
   */
  public static Type<KeyDownHandler> getType() {
    return TYPE;
  }

  /**
   * Protected constructor, use
   * {@link DomEvent#fireNativeEvent(Event, com.google.gwt.event.shared.HandlerManager)}
   * to fire click events.
   */
  protected KeyDownEvent() {
  }

  @Override
  protected void dispatch(KeyDownHandler handler) {
    handler.onKeyDown(this);
  }

  @Override
  protected final Type<KeyDownHandler> getAssociatedType() {
    return TYPE;
  }

}
