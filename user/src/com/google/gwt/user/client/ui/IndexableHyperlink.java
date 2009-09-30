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

import com.google.gwt.user.client.Element;

/**
 * Indexable version of Hyperlink class.  
 * This class will produce hyperlinks that contain the special
 * "indexable" token.
 *
 */
public class IndexableHyperlink extends Hyperlink {

  public IndexableHyperlink() {
    // TODO Auto-generated constructor stub
  }

  public IndexableHyperlink(String text, boolean asHTML,
      String targetHistoryToken) {
    super(text, asHTML, targetHistoryToken);
    // TODO Auto-generated constructor stub
  }

  public IndexableHyperlink(String text, String targetHistoryToken) {
    super(text, targetHistoryToken);
    // TODO Auto-generated constructor stub
  }

  public IndexableHyperlink(Element elem) {
    super(elem);
    // TODO Auto-generated constructor stub
  }
  
  /**
   * Sets the history token referenced by this hyperlink. This is the history
   * token that will be passed to {@link History#newItem} when this link is
   * clicked.
   * 
   * @param targetHistoryToken the new history token, which may not be null (use
   *        {@link Anchor} instead if you don't need history processing)
   */
  @Override
  public void setTargetHistoryToken(String targetHistoryToken) {
    super.setTargetHistoryToken("!" + targetHistoryToken);
  }
}
