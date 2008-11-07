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
 * Represents a native key press event.
 */
public class KeyPressEvent extends KeyEvent<KeyPressHandler> {

  /**
   * Event type for key press events. Represents the meta-data associated with
   * this event.
   */
  private static final Type<KeyPressHandler> TYPE = new Type<KeyPressHandler>(
      Event.ONKEYPRESS, "keypress", new KeyPressEvent());

  /**
   * Gets the event type associated with key press events.
   * 
   * @return the handler type
   */
  public static Type<KeyPressHandler> getType() {
    return TYPE;
  }

  /**
   * Protected constructor, use
   * {@link DomEvent#fireNativeEvent(Event, com.google.gwt.event.shared.HandlerManager)
   * to fire key press events.
   */
  protected KeyPressEvent() {
  }

  /**
   * Gets the char code for this event.
   * 
   * @return the char code
   */
  public char getCharCode() {
    return getCharCode(getNativeEvent());
  }

  @Override
  public String toDebugString() {
    return super.toDebugString() + "[" + getCharCode() + "]";
  }

  @Override
  protected void dispatch(KeyPressHandler handler) {
    handler.onKeyPress(this);
  }

  @Override
  protected final Type<KeyPressHandler> getAssociatedType() {
    return TYPE;
  }

  private native char getCharCode(Event e)/*-{
      return e.charCode || e.keyCode;
    }-*/;

}
