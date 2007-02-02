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
 * Implements the clipped image as a IMG inside a SPAN because we can't use the
 * IE PNG transparency filter on background-image images.
 */
public class ClippedImageImplIE6 extends ClippedImageImpl {

  public Element createStructure(String url, int left, int top, int width,
      int height) {
    // Create a span that can clip.
    Element span = DOM.createDiv();
    DOM.setStyleAttribute(span, "overflow", "hidden");
    DOM.setStyleAttribute(span, "width", width + "px");
    DOM.setStyleAttribute(span, "height", height + "px");

    // Create an image capable of showing transparent PNGs.
    Element img = DOM.createImg();
    DOM.setAttribute(img, "src", "clear.cache.gif");
    DOM.setStyleAttribute(img, "filter",
        "progid:DXImageTransform.Microsoft.AlphaImageLoader(src='" + url
            + "',sizingMethod='crop')");
    DOM.setStyleAttribute(img, "marginLeft", -left + "px");
    DOM.setStyleAttribute(img, "marginTop", -top + "px");
    // AlphaImageLoader requires that we size the image explicitly.
    // It really only needs to be enough to show the revealed portion.
    int imgWidth = left + width;
    int imgHeight = top + height;
    DOM.setIntAttribute(img, "width", imgWidth);
    DOM.setIntAttribute(img, "height", imgHeight);

    DOM.appendChild(span, img);
    return span;
  }
}
