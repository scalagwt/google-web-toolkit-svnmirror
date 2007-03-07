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

package com.google.gwt.user.client.ui;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;

/**
 * A normal push button with custom styling.
 * 
 */
public class PushButton extends CustomButton {

  private static final String STYLENAME_DEFAULT = "gwt-PushButton";

  /**
   * 
   * Constructor for <code>PushButton</code>. The supplied image is used to
   * construct the default face.
   * 
   * @param upImage image for the default face of the button
   */
  public PushButton(AbstractImage upImage) {
    this();
    getUpFace().setImage(upImage);
  }

  /**
   * 
   * Constructor for <code>PushButton</code>.
   * 
   * @param upImage image for the default(up) face of the button
   * @param downImage image for the down face of the button
   */
  public PushButton(AbstractImage upImage, AbstractImage downImage) {
    this(upImage);
    getDownFace().setImage(downImage);
  }

  /**
   * Constructor for <code>PushButton</code>.
   * 
   * @param upImage image for the default(up) face of the button
   * @param downImage image for the down face of the button
   * @param listener clickListener
   */
  public PushButton(AbstractImage upImage, AbstractImage downImage,
      ClickListener listener) {
    this(upImage, listener);
    getDownFace().setImage(downImage);
  }

  /**
   * 
   * Constructor for <code>PushButton</code>. The supplied image is used to
   * construct the default face of the button.
   * 
   * @param upImage image for the default (up) face of the button
   * @param listener the click listener
   */
  public PushButton(AbstractImage upImage, ClickListener listener) {
    this(upImage);
    addClickListener(listener);
  }

  /**
   * 
   * Constructor for <code>PushButton</code>. The supplied text is used to
   * construct the default face of the button.
   * 
   * @param upText the text for the default (up) face of the button.
   */
  public PushButton(String upText) {
    this();
    getUpFace().setText(upText);
  }

  /**
   * Constructor for <code>PushButton</code>. The supplied text is used to
   * construct the default face of the button.
   * 
   * @param upText the text for the default (up) face of the button
   * @param listener the click listener
   */
  public PushButton(String upText, ClickListener listener) {
    this(upText);
    addClickListener(listener);
  }

  /**
   * 
   * Constructor for <code>PushButton</code>.
   * 
   * @param upText the text for the default (up) face of the button
   * @param downText the text for down face of the button
   */
  public PushButton(String upText, String downText) {
    this(upText);
  }

  /**
   * Constructor for <code>PushButton</code>.
   */
  protected PushButton() {
    super();
    setStyleName(STYLENAME_DEFAULT);
  }

  public void onBrowserEvent(Event event) {
    // Should not act on button if disabled.
    if (isEnabled() == false) {
      return;
    }
    int type = DOM.eventGetType(event);
    switch (type) {

      case Event.ONMOUSEDOWN:
        setDown(true);
        break;
      case Event.ONMOUSEUP:
        setDown(false);
        break;
    }
    super.onBrowserEvent(event);
  }
}
