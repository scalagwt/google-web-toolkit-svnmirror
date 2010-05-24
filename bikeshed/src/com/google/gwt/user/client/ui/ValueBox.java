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
package com.google.gwt.user.client.ui;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.i18n.client.BidiUtils;
import com.google.gwt.i18n.client.HasDirection;
import com.google.gwt.input.shared.Parser;
import com.google.gwt.input.shared.Renderer;

/**
 * A standard single-line text box.
 * 
 * <p>
 * <img class='gallery' src='doc-files/TextBox.png'/>
 * </p>
 * 
 * <h3>CSS Style Rules</h3>
 * <ul class='css'>
 * <li>.gwt-TextBox { primary style }</li>
 * <li>.gwt-TextBox-readonly { dependent style set when the text box is
 * read-only }</li>
 * </ul>
 * 
 * <p>
 * <h3>Example</h3>
 * {@example com.google.gwt.examples.TextBoxExample}
 * </p>
 * 
 * @param <T> the value type
 */
public class ValueBox<T> extends ValueBoxBase<T> implements HasDirection {

  /**
   * Creates a TextBox widget that wraps an existing &lt;input type='text'&gt;
   * element.
   * 
   * This element must already be attached to the document. If the element is
   * removed from the document, you must call
   * {@link RootPanel#detachNow(Widget)}.
   * 
   * @param element the element to be wrapped
   */
  public static <T> ValueBox<T> wrap(Element element, Renderer<T> renderer, Parser<T> parser) {
    // Assert that the element is attached.
    assert Document.get().getBody().isOrHasChild(element);

    ValueBox<T> valueBox = new ValueBox<T>(element, renderer, parser);

    // Mark it attached and remember it for cleanup.
    valueBox.onAttach();
    RootPanel.detachOnWindowClose(valueBox);

    return valueBox;
  }

  /**
   * This constructor may be used by subclasses to explicitly use an existing
   * element. This element must be an &lt;input&gt; element whose type is
   * 'text'.
   * 
   * @param element the element to be used
   */
  protected ValueBox(Element element, Renderer<T> renderer, Parser<T> parser) {
    super(element, renderer, parser);
    assert InputElement.as(element).getType().equalsIgnoreCase("text");
  }

  public Direction getDirection() {
    return BidiUtils.getDirectionOnElement(getElement());
  }

  /**
   * Gets the maximum allowable length of the text box.
   * 
   * @return the maximum length, in characters
   */
  public int getMaxLength() {
    return getInputElement().getMaxLength();
  }

  /**
   * Gets the number of visible characters in the text box.
   * 
   * @return the number of visible characters
   */
  public int getVisibleLength() {
    return getInputElement().getSize();
  }

  public void setDirection(Direction direction) {
    BidiUtils.setDirectionOnElement(getElement(), direction);
  }

  /**
   * Sets the maximum allowable length of the text box.
   * 
   * @param length the maximum length, in characters
   */
  public void setMaxLength(int length) {
    getInputElement().setMaxLength(length);
  }

  /**
   * Sets the number of visible characters in the text box.
   * 
   * @param length the number of visible characters
   */
  public void setVisibleLength(int length) {
    getInputElement().setSize(length);
  }

  private InputElement getInputElement() {
    return getElement().cast();
  }
}
