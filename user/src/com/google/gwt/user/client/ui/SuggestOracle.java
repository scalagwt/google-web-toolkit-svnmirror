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
   * Creates the suggestions associated with the given query.
   * 
   * @param query the query string
   * @param callback the callback
   */
  public abstract void requestSuggestions(SuggestOracleRequest request,
      SuggestOracleCallback callback);

}