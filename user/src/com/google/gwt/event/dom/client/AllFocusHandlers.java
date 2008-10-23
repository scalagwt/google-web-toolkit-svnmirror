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
 * Adaptor used to create and add all focus events at once.
 */
public abstract class AllFocusHandlers implements FocusHandler, BlurHandler {

  /**
   * Convenience method to add both focus handlers at once to an event source.
   * 
   * @param <EventHandlerType> handler type that implements both focus and blur
   *          events.
   * @param source the event source
   * @param handlers the focus and blur handlers
   */
  public static <EventHandlerType extends BlurHandler & FocusHandler> void addHandlers(
      HasAllFocusHandlers source, EventHandlerType handlers) {
    source.addBlurHandler(handlers);
    source.addFocusHandler(handlers);
  }

  /**
   * Constructor.
   */
  public AllFocusHandlers() {
  }

  /**
   * Convenience method to add both focus handlers at once.
   * 
   * @param source the event source
   */
  public void addFocusHandlersTo(HasAllFocusHandlers source) {
    addHandlers(source, this);
  }
}