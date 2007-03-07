/*
 * Copyright 2006 Google Inc.
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
package com.google.gwt.user.client.ui.richtext;

/**
 * Objects that that implement this interface fire the events defined by the
 * {@link com.google.gwt.user.client.ui.richtext.HighlightMouseHandler}
 * interface, the
 * {@link com.google.gwt.user.client.ui.richtext.HighlightKeyboardEvent}, and
 * the {@link com.google.gwt.user.client.ui.richtext.HighlightClickHandler}
 * interface.
 */
interface FiresHighlightEvents {

  /**
   * Adds a handler for click events.
   * 
   * @param clickHandler the handler to add
   */
  void addClickHandler(HighlightClickHandler clickHandler);

  /**
   * Adds a handler for keyboard events.
   * 
   * @param keyboardHandler the handler to add
   */
  void addKeyboardHandler(HighlightKeyboardHandler keyboardHandler);

  /**
   * Adds a handler for mouse events.
   * 
   * @param mouseHandler the handler to add
   */
  void addMouseHandler(HighlightMouseHandler mouseHandler);

  /**
   * Removes a handler which formerly received click events.
   * 
   * @param clickHandler the handler to remove
   */
  void removeClickHandler(HighlightClickHandler clickHandler);

  /**
   * Removes a handler which formerly received keyboard events.
   * 
   * @param keyboardHandler the handler to remove
   */
  void removeKeyboardHandler(HighlightKeyboardHandler keyboardHandler);

  /**
   * Removes a handler which formerly received mouse events.
   * 
   * @param handler the handler to remove
   */
  void removeMouseHandler(HighlightMouseHandler handler);

}