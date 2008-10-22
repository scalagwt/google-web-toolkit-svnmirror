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
import com.google.gwt.user.client.Event;

/**
 * Represents a native mouse over event.
 */
public class MouseOverEvent extends MouseEvent {

  /**
   * Event type for mouse over events. Represents the meta-data associated with
   * this event.
   */
  public static final Type<MouseOverEvent, MouseOverHandler> TYPE = new Type<MouseOverEvent, MouseOverHandler>(
      Event.ONMOUSEOVER, "mouseover", new MouseOverEvent()) {
    @Override
    public void fire(MouseOverHandler handler, MouseOverEvent event) {
      handler.onMouseOver(event);
    }
  };

  /**
   * Gets the element from which the mouse pointer was moved.
   * 
   * @return the element from which the mouse pointer was moved
   */
  public Element getFromElement() {
    // Use a deferred binding instead of DOMImpl's inefficient switch statement
    return getNativeEvent().getFromElement();
  }

  /**
   * Gets the element to which the mouse pointer was moved.
   * 
   * @return the element to which the mouse pointer was moved
   */
  public Element getToElement() {
    // Use a deferred binding instead of DOMImpl's inefficient switch statement
    return getNativeEvent().getToElement();
  }

  @Override
  protected Type getType() {
    return TYPE;
  }

}
