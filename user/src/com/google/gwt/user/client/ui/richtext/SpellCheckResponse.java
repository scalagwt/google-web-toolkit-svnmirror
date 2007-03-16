package com.google.gwt.user.client.ui.richtext;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.Arrays;
import java.util.List;

/**
 * Spell check response. Created by {@link SpellCheckOracle}. Comprises all
 * misspelled words represented in the corresponding {@link SpellCheckRequest}.
 */
public class SpellCheckResponse implements IsSerializable {
  private List misspellings;

  /**
   * Gets the misspelled words.
   * 
   * @return list of misspellings
   */
  public List getMisspellings() {
    return misspellings;
  }

  /**
   * Sets the misspelled words.
   * 
   * @param misspellings mispellings
   */
  public void setMisspellings(Misspelling[] misspellings) {
    this.misspellings = Arrays.asList(misspellings);
  }

  /**
   * Sets the misspelled words.
   * 
   * @param misspellings misspellings
   */
  public void setMisspellings(List misspellings) {
    this.misspellings = misspellings;
  }

  public String toString() {
    return getMisspellings().toString();
  }
}