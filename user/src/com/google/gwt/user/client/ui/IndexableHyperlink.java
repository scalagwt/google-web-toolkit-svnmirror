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

/**
 * Indexable version of Hyperlink class. This class will produce hyperlinks that
 * contain the special "indexable" token.
 */
public class IndexableHyperlink extends Hyperlink {

  /**
   * Creates an empty hyperlink.
   */
  public IndexableHyperlink() {
    super();
  }

  /**
   * Creates an indexable hyperlink with its text and target history token
   * specified. The target history token will be preceded by a special "!" token
   * to indicate that this state should be indexed.
   * 
   * @param text the hyperlink's text
   * @param asHTML <code>true</code> to treat the specified text as html
   * @param targetHistoryToken the history token to which it will link
   * @see #setTargetHistoryToken
   */
  public IndexableHyperlink(String text, boolean asHTML,
      String targetHistoryToken) {
    super(text, asHTML, "!" + targetHistoryToken);
  }

  /**
   * Creates an indexable hyperlink with its text and target history token
   * specified. specified. The target history token will be preceded by a
   * special "!" token to indicate that this state should be indexed.
   * 
   * @param text the hyperlink's text
   * @param targetHistoryToken the history token to which it will link, which
   *          may not be null (use {@link Anchor} instead if you don't need
   *          history processing)
   */
  public IndexableHyperlink(String text, String targetHistoryToken) {
    super(text, "!" + targetHistoryToken);
  }

  /**
   * Sets the history token referenced by this hyperlink. This is the history
   * token that will be passed to {@link History#newItem} when this link is
   * clicked.
   * 
   * @param targetHistoryToken the new history token, which may not be null (use
   *          {@link Anchor} instead if you don't need history processing)
   */
  @Override
  public void setTargetHistoryToken(String targetHistoryToken) {
    super.setTargetHistoryToken("!" + targetHistoryToken);
  }
}
