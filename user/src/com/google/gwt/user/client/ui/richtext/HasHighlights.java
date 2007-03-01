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

import java.util.Iterator;
import java.util.List;

/**
 * Represents a widget which can have highlighted sections that react to user
 * input (e.g. spell check markings). This is used in handling events crossing
 * iframe boundaries. Highlights are owned and created by the
 * {@link HasHighlights} object.
 */
interface HasHighlights {

  /**
   *Adds highlights of a specific category. The highlights
   * created are returned in the resulting {@link Iterator}.
   * 
   * @param items the items to be highlighted
   * @param category the category to be used for the {@link Highlight}
   * @return the new {@link Highlight} objects as an {@link Iterator}
   */
  Iterator addHighlights(List items, HighlightCategory category);

  /**
   * Creates a new, unattached highlight from the supplied item, giving it the
   * category supplied.
   * 
   * @param item the item to be highlighted
   * @param category the category to be used for the {@link Highlight}
   * @return the new {@link Highlight} objects as an {@link Iterator}
   */
  Highlight createHighlight(Object item, HighlightCategory category);

  /**
   * Makes the owner no longer know about this highlight, and ensures that the
   * highlight is replaced with its current highlighted item within owner.
   * 
   * @param highlight the highlight to be no longer owned.
   */
  void disownHighlight(Highlight highlight);

  /**
   * Gets the highlight's absolute left position in pixels, as measured from the
   * owner's body. The highlight's owner may not be part of the same document,
   * which may necessitate the use of methods not in
   * {@link com.google.gwt.user.client.DOM}.
   * 
   * @param highlight the highlight to report the position of
   * @return the highlight's absolute left position
   */
  int getAbsoluteLeft(Highlight highlight);

  /**
   * Gets the highlight's top position in pixels, as measured from the owner's
   * body. The highlight's owner may not be part of the same document, which may
   * necessitate the use of methods not in
   * {@link com.google.gwt.user.client.DOM}.
   * 
   * @param highlight the highlight to report the position of
   * @return the highlight's relative top position
   */
  int getAbsoluteTop(Highlight highlight);

}
