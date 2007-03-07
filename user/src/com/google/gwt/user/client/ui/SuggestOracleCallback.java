package com.google.gwt.user.client.ui;

/**
 * Objects that can consume {@link SuggestOracle} suggestions must implement
 * their own instance of {@link SuggestOracleCallback}.
 * 
 */
public abstract interface SuggestOracleCallback {
  /**
   * Consume the suggestions created by a {@link SuggestOracle}.
   * 
   * @param suggestions the suggestions
   */
  public abstract void onSuggestionsRecieved(SuggestOracleRequest request,
      SuggestOracleResponse response);
}