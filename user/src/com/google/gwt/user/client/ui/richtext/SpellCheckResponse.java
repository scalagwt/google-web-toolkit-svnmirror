package com.google.gwt.user.client.ui.richtext;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.Arrays;

/**
 * Spell check response. Created by {@link SpellCheckOracle}. Comprises all
 * misspelled words represented in the corresponding {@link SpellCheckRequest}.
 */
public class SpellCheckResponse implements IsSerializable {
  private Misspelling[] misspellings;

  /**
   * Gets the misspelled words.
   */
  public Misspelling[] getMisspellings() {
    return misspellings;
  }

  /**
   * Sets the misspelled words.
   */
  public void setMisspellings(Misspelling[] entries) {
    this.misspellings = entries;
  }

  public String toString() {
    return Arrays.asList(getMisspellings()).toString();
  }
}