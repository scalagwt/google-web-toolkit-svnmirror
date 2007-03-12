package com.google.gwt.user.client.ui.richtext;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Spell check request. The request fields represent a snapshot of the spell
 * check state when the request was generated.
 * 
 */
public class SpellCheckRequest implements IsSerializable {
  /**
   * Locale used for spell checking engine.
   */
  private String locale;

  /**
   * Text to extract misspelled words from.
   */
  private String text;

  /**
   * Requests should only be created by {@link SpellCheck}.
   */
  SpellCheckRequest() {
  }

  /**
   * Gets the locale for spell checking.
   * 
   * @return spell check locale
   */
  public String getLocale() {
    return locale;
  }

  /**
   * Gets the text to extract misspelled words from.
   * 
   * @return returned text
   */
  public String getText() {
    return text;
  }

  /**
   * Sets the locale for spell checking. As there may be an arbitrary delay
   * between the request for a spell check and its activation, we must cache
   * the spell check driver's locale.
   * 
   * @param locale spell check locale
   */
  public void setLocale(String locale) {
    this.locale = locale;
  }

  /**
   * Sets the text to extract misspelled words from. As there may be an
   * arbitrary delay between the request for a spell check and it's
   * activation, we must cache the spellcheckable's text.
   * 
   * @param text the text
   */
  public void setText(String text) {
    this.text = text;
  }
}