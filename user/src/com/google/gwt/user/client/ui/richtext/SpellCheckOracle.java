package com.google.gwt.user.client.ui.richtext;


/**
 * Allows the spell check to communicate with a user's data source. Most
 * implementations of {@link SpellCheckOracle} should be a simple delegations
 * to the user's server communication infrastructure, such as an RPC Service.
 */
public interface SpellCheckOracle {

  /**
   * Requests a new spell check. The model is responsible for creating a spell
   * check <code>Response</code> object and passing it into
   * {@link SpellCheckCallback#onSpellCheckResponseRecieved(com.google.gwt.user.client.ui.richtext.SpellCheck.SpellCheckRequest, com.google.gwt.user.client.ui.richtext.SpellCheck.SpellCheckResponse)}
   */
  public void spellCheck(SpellCheckRequest request,
      SpellCheckCallback callback);
}