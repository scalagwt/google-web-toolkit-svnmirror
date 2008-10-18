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

/**
 * A simple checkbox widget, with no label.
 * 
 * <h3>CSS Style Rules</h3>
 * <ul class='css'>
 * <li>.gwt-SimpleCheckBox { }</li>
 * <li>.gwt-SimpleCheckBox-disabled { Applied when checkbox is disabled }</li>
 * </ul>
 */
public class SimpleCheckBox extends FocusWidget implements HasName {

  /**
   * Creates a SimpleCheckBox widget that wraps an existing &lt;input
   * type='checkbox'&gt; element.
   * 
   * This element must already be attached to the document. If the element is
   * removed from the document, you must call
   * {@link RootPanel#detachNow(Widget)}.
   * 
   * @param element the element to be wrapped
   */
  public static SimpleCheckBox wrap(Element element) {
    // Assert that the element is attached.
    assert Document.get().getBody().isOrHasChild(element);

    SimpleCheckBox checkBox = new SimpleCheckBox(element);

    // Mark it attached and remember it for cleanup.
    checkBox.onAttach();
    RootPanel.detachOnWindowClose(checkBox);

    return checkBox;
  }

  /**
   * Creates a new simple checkbox.
   */
  public SimpleCheckBox() {
    this(Document.get().createCheckInputElement(), "gwt-SimpleCheckBox");
  }

  /**
   * This constructor may be used by subclasses to explicitly use an existing
   * element. This element must be an &lt;input&gt; element whose type is either
   * 'checkbox'.
   * 
   * @param element the element to be used
   */
  protected SimpleCheckBox(Element element) {
    assert InputElement.as(element).getType().equalsIgnoreCase("checkbox");
    setElement(element);
  }

  SimpleCheckBox(Element element, String styleName) {
    setElement(element);
    if (styleName != null) {
      setStyleName(styleName);
    }
  }

  public String getName() {
    return getInputElement().getName();
  }

  /**
   * Determines whether this check box is currently checked.
   * 
   * @return <code>true</code> if the check box is checked
   */
  public boolean isChecked() {
    String propName = isAttached() ? "checked" : "defaultChecked";
    return getInputElement().getPropertyBoolean(propName);
  }

  /**
   * Checks or unchecks this check box.
   * 
   * @param checked <code>true</code> to check the check box
   */
  public void setChecked(boolean checked) {
    getInputElement().setChecked(checked);
    getInputElement().setDefaultChecked(checked);
  }

  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    if (enabled) {
      removeStyleDependentName("disabled");
    } else {
      addStyleDependentName("disabled");
    }
  }

  public void setName(String name) {
    getInputElement().setName(name);
  }

  /**
   * This method is called when a widget is detached from the browser's
   * document. Overridden because of IE bug that throws away checked state and
   * in order to clear the event listener off of the <code>inputElem</code>.
   */
  @Override
  protected void onUnload() {
    setChecked(isChecked());
  }

  private InputElement getInputElement() {
    return InputElement.as(getElement());
  }
}
