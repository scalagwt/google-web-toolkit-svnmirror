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

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.HasHTML;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.UIObject;

/**
 * Represents a highlighted area which can receive and react to events.
 * Currently {@link Highlight} objects are only allowed to be created by
 * {@link RichTextArea} objects.
 */
public class Highlight extends UIObject implements FiresHighlightEvents,
    HasText, HasHTML {

  private final Object highlighted;
  private final HasHighlights owner;
  private final HighlightCategory category;
  private HighlightKeyboardHandlerCollection keyboardHandlers;
  private HighlightClickHandlerCollection clickHandlers;
  private HighlightMouseHandlerCollection mouseHandlers;

  /**
   * Creates a new highlight with the given text, of the type specified by
   * highlightType. Currently, only rich text objects are allowed to create
   * highlights.
   * 
   * @param owner the highlight's owner
   * @param category the {@link HighlightCategory} to give the new highlight
   * @param elem the element comprising the highlight
   * @param highlighted the object to be highlighted
   */
  Highlight(HasHighlights owner, Object highlighted,
      HighlightCategory category, Element elem) {
    this.category = category;
    if (category == null) {
      throw new IllegalArgumentException("Category cannot be null");
    }
    this.owner = owner;
    this.highlighted = highlighted;
    setElement(elem);
    setStyleName(category.getCssClass());
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

  public int getAbsoluteLeft() {
    // The absoluteLeft and absoluteTop are local to their iframe, hence the
    // addition, and the document-specific getAbsoluteLeft. DOM methods cannot
    // be used here because of the iframe.
    return owner.getAbsoluteLeft(this.getElement());
  }

  public int getAbsoluteTop() {
    // The absoluteTop and absoluteTop are local to their iframe, hence the
    // addition, and the document-specific getAbsoluteTop. DOM methods cannot be
    // used here because of the iframe.
    return owner.getAbsoluteTop(this.getElement());
  }

  /**
   * Gets the category associated with this highlight.
   * 
   * @return the category associated with this highlight
   */
  public HighlightCategory getCategory() {
    return category;
  }

  /**
   * Gets the original highlighted item associated with this highlight when it
   * was created.
   * 
   * @return the original highlighted item associated with this highlight
   */
  public Object getHighlighted() {
    return highlighted;
  }

  public String getHTML() {
    return DOM.getInnerHTML(getElement());
  }

  /**
   * Gets the owner of this highlight.
   * 
   * @return the owner of this highlight
   */
  public HasHighlights getOwner() {
    return owner;
  }

  public String getText() {
    return DOM.getInnerText(getElement());
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

  public void setHTML(String html) {
    DOM.setInnerHTML(getElement(), html);
  }

  public void setText(String text) {
    DOM.setInnerText(getElement(), text);
  }

  /**
   * Replaces this highlight with its current contents, making it no longer
   * respond to events specially.
   */
  public void unhighlight() {
    owner.disownHighlight(this);
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
    HighlightCategory c = getCategory();
    c.fireClickEvent(event);
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
    HighlightCategory c = getCategory();
    c.fireKeyboardEvent(event);
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
    HighlightCategory c = getCategory();
    c.fireMouseEvent(event);
  }
}