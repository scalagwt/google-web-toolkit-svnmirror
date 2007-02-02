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
import com.google.gwt.user.client.ui.impl.ClippedImageImpl;

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

  private static final ClippedImageImpl impl = (ClippedImageImpl) GWT.create(ClippedImageImpl.class);

  // NOTE: Compiler bug if ClippedImageImplIE6 doesn't extend ClippedImageImpl.

  /**
   * Creates an image with a specified URL and a viewport.
   */
  public ClippedImage(String url, int left, int top, int width, int height) {
    super(impl.createStructure(url, left, top, width, height));
    setStyleName("gwt-ClippedImage");
  }
}
