/*
 * Copyright 2009 Google Inc.
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
package com.google.gwt.dom.client;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * The native dom event.
 */
public class NativeEvent extends JavaScriptObject {

  /**
   * The left mouse button.
   */
  public static final int BUTTON_LEFT = 1;

  /**
   * The middle mouse button.
   */
  public static final int BUTTON_MIDDLE = 4;

  /**
   * The right mouse button.
   */
  public static final int BUTTON_RIGHT = 2;

  /**
   * Required constructor for GWT compiler to function.
   */
  protected NativeEvent() {
  }

  /**
   * Gets whether the ALT key was depressed when the given event occurred.
   * 
   * @return <code>true</code> if ALT was depressed when the event occurred
   */
  public final boolean getAltKey() {
    return DOMImpl.impl.eventGetAltKey(this);
  }

  /**
   * Gets the mouse buttons that were depressed when the given event occurred.
   * 
   * @return a bit-field, defined by {@link NativeEvent#BUTTON_LEFT},
   *         {@link NativeEvent#BUTTON_MIDDLE}, and
   *         {@link NativeEvent#BUTTON_RIGHT}
   */
  public final int getButton() {
    return DOMImpl.impl.eventGetButton(this);
  }

  /**
   * Gets the mouse x-position within the browser window's client area.
   * 
   * @return the mouse x-position
   */
  public final int getClientX() {
    return DOMImpl.impl.eventGetClientX(this);
  }

  /**
   * Gets the mouse y-position within the browser window's client area.
   * 
   * @return the mouse y-position
   */
  public final int getClientY() {
    return DOMImpl.impl.eventGetClientY(this);
  }

  /**
   * Gets whether the CTRL key was depressed when the given event occurred.
   * 
   * @return <code>true</code> if CTRL was depressed when the event occurred
   */
  public final boolean getCtrlKey() {
    return DOMImpl.impl.eventGetCtrlKey(this);
  }

  /**
   * Gets the current target element of this event. This is the element whose
   * listener fired last, not the element which fired the event initially.
   * 
   * @return the event's current target element
   */
  public final EventTarget getCurrentEventTarget() {
    return DOMImpl.impl.eventGetCurrentTarget(this);
  }

  /**
   * Gets the key code associated with this event.
   * 
   * <p>
   * For key press events, this method returns the Unicode value of the
   * character generated. For key down and key up events, it returns the code
   * associated with the physical key.
   * </p>
   * 
   * @return the Unicode character or key code.
   * @see com.google.gwt.event.dom.client.KeyCodes
   */
  public final int getKeyCode() {
    return DOMImpl.impl.eventGetKeyCode(this);
  }

  /**
   * Gets whether the META key was depressed when the given event occurred.
   * 
   * @return <code>true</code> if META was depressed when the event occurred
   */
  public final boolean getMetaKey() {
    return DOMImpl.impl.eventGetMetaKey(this);
  }

  /**
   * Gets the velocity of the mouse wheel associated with the event along the Y
   * axis.
   * <p>
   * The velocity of the event is an artifical measurement for relative
   * comparisons of wheel activity. It is affected by some non-browser factors,
   * including choice of input hardware and mouse acceleration settings. The
   * sign of the velocity measurement agrees with the screen coordinate system;
   * negative values are towards the origin and positive values are away from
   * the origin. Standard scrolling speed is approximately ten units per event.
   * </p>
   * 
   * @return The velocity of the mouse wheel.
   */
  public final int getMouseWheelVelocityY() {
    return DOMImpl.impl.eventGetMouseWheelVelocityY(this);
  }

  /**
   * Gets the related target for this event.
   * 
   * @return the related target
   */
  public final EventTarget getRelatedEventTarget() {
    return DOMImpl.impl.eventGetRelatedTarget(this);
  }

  /**
   * Gets the mouse x-position on the user's display.
   * 
   * @return the mouse x-position
   */
  public final int getScreenX() {
    return DOMImpl.impl.eventGetScreenX(this);
  }

  /**
   * Gets the mouse y-position on the user's display.
   * 
   * @return the mouse y-position
   */
  public final int getScreenY() {
    return DOMImpl.impl.eventGetScreenY(this);
  }

  /**
   * Gets whether the shift key was depressed when the given event occurred.
   * 
   * @return <code>true</code> if shift was depressed when the event occurred
   */
  public final boolean getShiftKey() {
    return DOMImpl.impl.eventGetShiftKey(this);
  }

  /**
   * Gets a string representation of this event.
   * 
   * We do not override {@link #toString()} because it is final in
   * {@link com.google.gwt.core.client.JavaScriptObject }.
   * 
   * @return the string representation of this event
   */
  public final String getString() {
    return DOMImpl.impl.eventToString(this);
  }

  /**
   * Returns the element that was the actual target of the given event.
   * 
   * @return the target element
   */
  public final EventTarget getEventTarget() {
    return DOMImpl.impl.eventGetTarget(this);
  }

  /**
   * Gets the enumerated type of this event.
   * 
   * @return the event's enumerated type
   */
  public final String getType() {
    return DOMImpl.impl.eventGetType(this);
  }

  /**
   * Prevents the browser from taking its default action for the given event.
   */
  public final void preventDefault() {
    DOMImpl.impl.eventPreventDefault(this);
  }

  /**
   * Stops the event from being propagated to parent elements.
   */
  public final void stopPropagation() {
    DOMImpl.impl.eventStopPropagation(this);
  }
}
