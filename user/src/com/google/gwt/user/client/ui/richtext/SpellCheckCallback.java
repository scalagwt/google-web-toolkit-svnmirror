package com.google.gwt.user.client.ui.richtext;

/**
 * Callback used by {@link SpellCheckOracle} for spell checking.
 */
public class SpellCheckCallback {
  /**
   * 
   */
  private final SpellCheck check;

  SpellCheckCallback(SpellCheck check) {
    this.check = check;
  }

  /**
   * CallBack used by {@link SpellCheckOracle} once the
   * {@link SpellCheckResponse} has been created.
   * 
   * @param response response
   */
  public void onSpellCheckResponseRecieved(SpellCheckRequest request,
      SpellCheckResponse response) {
    this.check.startSpellCheck(response.getMisspellings());
  }
}