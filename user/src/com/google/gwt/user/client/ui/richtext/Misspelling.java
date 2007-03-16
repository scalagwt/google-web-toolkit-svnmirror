package com.google.gwt.user.client.ui.richtext;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.Arrays;
import java.util.List;

/**
 * Represents a single misspelled word and the set of suggestions associated
 * with that word.
 */
public class Misspelling implements IsSerializable {
  private String word;
  private List suggestions;

  /**
   * 
   * Constructor for <code>Misspelling</code>.
   * 
   * @param word the misspelled word
   * @param suggestions alternative suggestions.
   */
  public Misspelling(String word, String[] suggestions) {
    this.word = word;
    this.suggestions = Arrays.asList(suggestions);
  }

  /**
   * 
   * Constructor for <code>Misspelling</code>.
   * 
   * @param word the misspelled word
   * @param suggestions alternative suggestions.
   */
  public Misspelling(String word, List suggestions) {
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
  public List getSuggestions() {
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
    this.suggestions = Arrays.asList(suggestions);
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
    return getWord() + getSuggestions();
  }
}