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

/**
 * Adaptor used to create and add all the mouse events at once.
 * 
 * <br/> WARNING, PLEASE READ: As this class is intended for developers who wish
 * to create all mouse handlers, new mouse handler events will be added to it.
 * Therefore, updates can cause breaking API changes.
 * 
 */
public abstract class AllMouseHandlers implements HasMouseDownHandlers,
    HasMouseUpHandlers, HasMouseOutHandlers, HasMouseOverHandlers,
    HasMouseMoveHandlers, HasMouseWheelHandlers {

  /**
   * Convenience method to add all key handlers at once.
   * 
   * @param <EventHandler> event handler type
   * @param source event source
   * @param handlers handlers to add
   */
  public static <EventHandler extends MouseDownHandler & MouseUpHandler & MouseOutHandler & MouseOverHandler & MouseMoveHandler & MouseWheelHandler> void addHandlers(
      HasAllMouseHandlers source, EventHandler handlers) {
    source.addMouseDownHandler(handlers);
    source.addMouseUpHandler(handlers);
    source.addMouseOutHandler(handlers);
    source.addMouseOverHandler(handlers);
    source.addMouseMoveHandler(handlers);
    source.addMouseWheelHandler(handlers);
  }

  /**
   * Creates an adaptor to implement all the {@link HasAllKeyHandlers} handler
   * types.
   */
  public AllMouseHandlers() {
  }
}