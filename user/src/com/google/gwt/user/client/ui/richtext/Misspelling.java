package com.google.gwt.user.client.ui.richtext;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.Arrays;

/**
 * Represents a single misspelled word and the set of suggestions associated
 * with that word.
 */
public class Misspelling implements IsSerializable {
  private String word;
  private String[] suggestions;

  /**
   * 
   * Constructor for <code>Misspelling</code>.
   * 
   * @param word the misspelled word
   * @param suggestions alternative suggestions.
   */
  public Misspelling(String word, String[] suggestions) {
    this.word = word;
    this.suggestions = suggestions;
  }

  /**
   * 
   * Constructor for <code>Misspelling</code>.
   */
  public Misspelling() {
  }

  /**
   * Gets the suggestions associated with the current misspelling.
   * 
   * @return the suggestions
   */
  public String[] getSuggestions() {
    return suggestions;
  }

  /**
   * Gets the misspelled word.
   * 
   * @return misspelled word.
   */
  public String getWord() {
    return word;
  }

  /**
   * Sets the suggestions associated with the current misspelling.
   * 
   * @param suggestions
   */
  public void setSuggestions(String[] suggestions) {
    this.suggestions = suggestions;
  }

  /**
   * Sets the misspelled word.
   * 
   * @param word the word
   */
  public void setWord(String word) {
    this.word = word;
  }

  public String toString() {
    return getWord() + Arrays.asList(getSuggestions()).toString();
  }
}