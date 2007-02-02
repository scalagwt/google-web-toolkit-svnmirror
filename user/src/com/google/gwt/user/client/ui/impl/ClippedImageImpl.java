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
package com.google.gwt.user.client.ui.impl;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;

/**
 * Uses a combination of a clear image and a background image to clip all
 * except a desired portion of an underlying image.
 */
public class ClippedImageImpl {

  public Element createStructure(String url, int left, int top, int width,
      int height) {
    Element img = DOM.createImg();

    // The actual 'src' attribute is a transparent pixel, resized.
    DOM.setAttribute(img, "src", "clear.cache.gif");

    // The image we want to really display is the background image.
    DOM.setStyleAttribute(img, "backgroundImage", "url(" + url + ")");

    // Specify the coordinates for the viewport.
    String pos = (-left + "px ") + (-top + "px");
    DOM.setStyleAttribute(img, "backgroundPosition", pos);
    DOM.setStyleAttribute(img, "width", width + "px");
    DOM.setStyleAttribute(img, "height", height + "px");

    return img;
  }
}