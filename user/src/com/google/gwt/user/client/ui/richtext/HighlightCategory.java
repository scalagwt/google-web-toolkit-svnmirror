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
 * A category of highlights. Each {@link Highlight} is associated with a single
 * {@link HighlightCategory}. Events cascade from the {@link Highlight} to the
 * {@link HighlightCategory}.
 */
public class HighlightCategory implements FiresHighlightEvents {
  private HighlightKeyboardHandlerCollection keyboardHandlers;
  private HighlightClickHandlerCollection clickHandlers;
  private HighlightMouseHandlerCollection mouseHandlers;
  private final String cssClass;

  /**
   * Creates a new {@link HighlightCategory}.
   * 
   * @param classname the name of the CSS class used to format highlights
   *          belonging to this category.
   */
  public HighlightCategory(String classname) {
    cssClass = classname;
  }

  public void addClickHandler(HighlightClickHandler clickHandler) {
    if (clickHandlers == null) {
      clickHandlers = new HighlightClickHandlerCollection();
    }
    clickHandlers.add(clickHandler);
  }

  public void addKeyboardHandler(HighlightKeyboardHandler keyboardHandler) {
    if (keyboardHandlers == null) {
      keyboardHandlers = new HighlightKeyboardHandlerCollection();
    }
    keyboardHandlers.add(keyboardHandler);
  }

  public void addMouseHandler(HighlightMouseHandler mouseHandler) {
    if (mouseHandlers == null) {
      mouseHandlers = new HighlightMouseHandlerCollection();
    }
    mouseHandlers.add(mouseHandler);
  }

  /**
   * Gets the CSS class used to format highlights that belong to this category.
   * 
   * @return the CSS class used to format highlights that belong to this category
   */
  public String getCssClass() {
    return cssClass;
  }

  public void removeClickHandler(HighlightClickHandler clickHandler) {
    if (clickHandlers != null) {
      clickHandlers.remove(clickHandler);
    }
  }

  public void removeKeyboardHandler(HighlightKeyboardHandler keyboardHandler) {
    if (keyboardHandlers != null) {
      keyboardHandlers.remove(keyboardHandler);
    }
  }

  public void removeMouseHandler(HighlightMouseHandler handler) {
    if (mouseHandlers != null) {
      mouseHandlers.remove(handler);
    }
  }

  /**
   * Sends a {@link HighlightClickEvent} on all click handlers on this
   * highlight.
   * <p>
   * Note: The fire methods must be exposed as highlights are not responsible
   * for firing their own events.
   * 
   * @param event the {@link HighlightClickEvent} being sent
   */
  void fireClickEvent(HighlightClickEvent event) {
    if (clickHandlers != null) {
      clickHandlers.fireClickEvent(event);
    }
  }

  /**
   * Sends a {@link HighlightKeyboardEvent} on all keyboard handlers on this
   * highlight.
   * <p>
   * Note: The fire methods must be exposed as highlights are not responsible
   * for firing their own events.
   * 
   * @param event the {@link HighlightKeyboardEvent} being sent
   */
  void fireKeyboardEvent(HighlightKeyboardEvent event) {
    if (keyboardHandlers != null) {
      keyboardHandlers.fireKeyboardEvent(event);
    }
  }

  /**
   * Sends a {@link HighlightMouseEvent} on all mouse handlers on this
   * highlight.
   * <p>
   * Note: The fire methods must be exposed as highlights are not responsible
   * for firing their own events.
   * 
   * @param event the {@link HighlightMouseEvent} being sent
   */
  void fireMouseEvent(HighlightMouseEvent event) {
    if (mouseHandlers != null) {
      mouseHandlers.fireMouseEvent(event);
    }
  }

}
