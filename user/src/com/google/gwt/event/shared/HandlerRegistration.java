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
package com.google.gwt.event.shared;

import com.google.gwt.event.shared.AbstractEvent.Type;

/**
 * HandlerRegistration instances are returned by
 * HandlerManager.addEventHandler() and used to remove event handler
 * registrations.
 */
public class HandlerRegistration {

  private HandlerManager manager;
  EventHandler handler;
  private Type<?> type;

  /**
   * Creates a new handler registration.
   * 
   * @param <H> Handler type
   * 
   * @param manager the handler manager
   * @param type the event type
   * @param handler the handler
   */
  protected <H extends EventHandler> HandlerRegistration(
      HandlerManager manager, Type<H> type, H handler) {
    this.manager = manager;
    this.handler = handler;
    this.type = type;
  }

  /**
   * Removes the given handler from its manager.
   * 
   * @param <H> handler type
   */
  // This is safe because when the elements were passed in they conformed to
  // Type<H>,H.
  @SuppressWarnings("unchecked")
  public <H extends EventHandler> void removeHandler() {
    Type<H> myType = (Type<H>) type;
    H myHandler = (H) handler;
    manager.removeHandler(myType, myHandler);
  }

  EventHandler getHandler() {
    return handler;
  }
}
