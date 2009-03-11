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

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.shared.EventHandler;

/**
 * Abstract class representing mouse events.
 * 
 * @param <H> handler type
 * 
 */
public abstract class MouseEvent<H extends EventHandler> extends DomEvent<H> {

  /**
   * Gets the mouse x-position within the browser window's client area.
   * 
   * @return the mouse x-position
   */
  public int getClientX() {
    return getNativeEvent().getClientX();
  }

  /**
   * Gets the mouse y-position within the browser window's client area.
   * 
   * @return the mouse y-position
   */
  public int getClientY() {
    return getNativeEvent().getClientY();
  }

  /**
   * Gets the button value. Compare it to
   * {@link com.google.gwt.dom.client.NativeEvent#BUTTON_LEFT},
   * {@link com.google.gwt.dom.client.NativeEvent#BUTTON_RIGHT},
   * {@link com.google.gwt.dom.client.NativeEvent#BUTTON_MIDDLE}
   * 
   * @return the button value
   */
  public int getNativeButton() {
    return getNativeEvent().getButton();
  }

  /**
   * Gets the mouse x-position relative to a given element.
   * 
   * @param target the element whose coordinate system is to be used
   * @return the relative x-position
   */
  public int getRelativeX(Element target) {
    NativeEvent e = getNativeEvent();

    return e.getClientX() - target.getAbsoluteLeft() + target.getScrollLeft() +
      target.getOwnerDocument().getScrollLeft();
  }

  /**
   * Gets the mouse y-position relative to a given element.
   * 
   * @param target the element whose coordinate system is to be used
   * @return the relative y-position
   */
  public int getRelativeY(Element target) {
    NativeEvent e = getNativeEvent();

    return e.getClientY() - target.getAbsoluteTop() + target.getScrollTop() +
      target.getOwnerDocument().getScrollTop();
  }

  /**
   * Gets the mouse x-position on the user's display.
   * 
   * @return the mouse x-position
   */
  public int getScreenX() {
    return getNativeEvent().getScreenX();
  }

  /**
   * Gets the mouse y-position on the user's display.
   * 
   * @return the mouse y-position
   */
  public int getScreenY() {
    return getNativeEvent().getScreenY();
  }

  /**
   * Gets the mouse x-position relative to the event's target element.
   * 
   * @return the relative x-position
   */
  public int getTargetX() {
    return getRelativeX(getNativeEvent().getTarget());
  }

  /**
   * Gets the mouse y-position relative to the event's target element.
   * 
   * @return the relative y-position
   */
  public int getTargetY() {
    return getRelativeY(getNativeEvent().getTarget());
  }

  /**
   * Is <code>alt</code> key down.
   * 
   * @return whether the alt key is down
   */
  public boolean isAltKeyDown() {
    return getNativeEvent().getAltKey();
  }

  /**
   * Is <code>control</code> key down.
   * 
   * @return whether the control key is down
   */
  public boolean isControlKeyDown() {
    return getNativeEvent().getCtrlKey();
  }

  /**
   * Is <code>meta</code> key down.
   * 
   * @return whether the meta key is down
   */
  public boolean isMetaKeyDown() {
    return getNativeEvent().getMetaKey();
  }

  /**
   * Is <code>shift</code> key down.
   * 
   * @return whether the shift key is down
   */
  public boolean isShiftKeyDown() {
    return getNativeEvent().getShiftKey();
  }
}
