package com.google.gwt.user.client.ui;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.History;

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
