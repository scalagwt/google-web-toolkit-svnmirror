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
 * The handler for keyboard events on highlights.
 */
public interface HighlightMouseHandler {

  /**
   * Fired when a mouse button goes down over the {@link Highlight}.
   * 
   * @param event a {@link HighlightMouseEvent} object containing information
   *          about the {@link Highlight} mouse button down
   */
  void onMouseDown(HighlightMouseEvent event);

  /**
   * Fired when mouse is moved over the {@link Highlight}.
   * 
   * @param event a {@link HighlightMouseEvent} object containing information
   *          about the {@link Highlight} mouse move
   */
  void onMouseMove(HighlightMouseEvent event);

  /**
   * Fired when a mouse button goes up over the {@link Highlight}.
   * 
   * @param event a {@link HighlightMouseEvent} object containing information
   *          about the {@link Highlight} mouse button up
   */
  void onMouseUp(HighlightMouseEvent event);
}