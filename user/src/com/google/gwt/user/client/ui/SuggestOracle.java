package com.google.gwt.user.client.ui;

/**
 * 
 * A {@link SuggestOracle} can be used to create suggestions associated with a
 * specific query string. {@link SuggestOracle} objects are often final, so
 * objects implementing this interface do not need to supply an explicit
 * {@link SuggestOracle} setter.
 * 
 */
public abstract class SuggestOracle {
  private int limit = 20;

  /**
   * Generate a {@link SuggestOracleResponse} based on a specific
   * {@link SuggestOracleRequest}.
   * 
   * @param request the request
   * @param callback the callback to use for the response
   */
  public abstract void requestSuggestions(SuggestOracleRequest request,
      SuggestOracleCallback callback);
}