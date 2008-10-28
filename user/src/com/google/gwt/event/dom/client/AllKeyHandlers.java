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
 * Adaptor used to create and add all the Key handlers at once.
 * 
 * <br/> WARNING, PLEASE READ: As this class is intended for developers who wish
 * to create all key events in GWT, new key handlers will be added to it.
 * Therefore, updates to GWT could cause breaking API changes.
 * 
 */
public abstract class AllKeyHandlers implements KeyDownHandler, KeyUpHandler,
    KeyPressHandler {

  /**
   * Convenience method to add all key handlers at once.
   * 
   * @param <EventHandler> event handler type
   * @param source event source
   * @param handlers handlers to add
   */
  public static <EventHandler extends KeyDownHandler & KeyUpHandler & KeyPressHandler> void addHandlers(
      HasAllKeyHandlers source, EventHandler handlers) {
    source.addKeyDownHandler(handlers);
    source.addKeyPressHandler(handlers);
    source.addKeyUpHandler(handlers);
  }

  /**
   * Constructor.
   */
  public AllKeyHandlers() {
  }

  /**
   * Adds all the key handlers to the given event source.
   * 
   * @param source the event source
   */
  public final void addKeyHandlersTo(HasAllKeyHandlers source) {
    addHandlers(source, this);
  }
}
