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

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;

/**
 * An image that reveals a rectangle within a larger image, used to implement
 * efficient image downloading by combing multiple subimages into a single
 * shared resource.
 * 
 * <h3>CSS Style Rules</h3>
 * <ul class="css">
 * <li>.gwt-ClippedImage { }</li>
 * </ul>
 * 
 * <h3>Caveats</h3>
 * This class is similar to {@link Image} and is sometimes interchangeable.
 * However, there is no direct inheritance relationship between the two due to
 * certain incompatible semantics (e.g. absence of "loaded" events and
 * resizing).
 */
public class ClippedImage extends AbstractImage {

  /**
   * Uses a combination of a clear image and a background image to clip all
   * except a desired portion of an underlying image.
   */
  private static class Impl {

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

  /**
   * Implements the clipped image as a IMG inside a SPAN because we can't use
   * the IE PNG transparency filter on background-image images.
   */
  private static class ImplIE {

    public Element createStructure(String url, int left, int top, int width,
        int height) {
      debugger();
      
      // Create a span that can clip.
      Element span = DOM.createSpan();
      DOM.setStyleAttribute(span, "overflow", "hidden");
      DOM.setStyleAttribute(span, "width", width + "px");
      DOM.setStyleAttribute(span, "height", height + "px");

      // Create an image capable of showing transparent PNGs.
      Element img = DOM.createImg();
      DOM.setStyleAttribute(img, "filter",
          "progid:DXImageTransform.Microsoft.AlphaImageLoader(src='" + url
              + "',sizingMethod='crop')");
      DOM.setStyleAttribute(img, "width", width + "px");
      DOM.setStyleAttribute(img, "height", height + "px");
      DOM.setStyleAttribute(img, "marginLeft", -left + "px");
      DOM.setStyleAttribute(img, "marginTop", -left + "px");

      DOM.appendChild(span, img);
      return span;
    }
    
    private native void debugger() /*-{ debugger; }-*/; 
  }

  private static final Impl impl = (Impl) GWT.create(Impl.class);

  /**
   * Creates an image with a specified URL and a viewport.
   */
  public ClippedImage(String url, int left, int top, int width, int height) {
    super(impl.createStructure(url, left, top, width, height));
    setStyleName("gwt-ClippedImage");
  }
}
