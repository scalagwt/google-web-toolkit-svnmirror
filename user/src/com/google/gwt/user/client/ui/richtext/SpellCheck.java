/*
 * Copyright 2006 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.gwt.user.client.ui.richtext;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.SuggestPicker;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.impl.ItemPickerDropDownImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Spell Check for {@link RichTextArea}.
 */
public class SpellCheck {

  /**
   * Spell check callback. Each {@link Oracle} should call
   * {@link com.google.gwt.user.client.ui.richtext.SpellCheck.Callback#onSpellCheckResponseRecieved(com.google.gwt.user.client.ui.richtext.SpellCheck.Request, com.google.gwt.user.client.ui.richtext.SpellCheck.Response)}.
   * 
   */
  public abstract class Callback {

    /**
     * Callback used by {@link Oracle} once the {@link Response} has been
     * created.
     * 
     * @param request spell check request
     * @param response spell check response
     */
    public abstract void onSpellCheckResponseRecieved(Request request,
        Response response);
  }

  /**
   * Has a spell check oracle.
   */
  public interface HasOracle {
    /**
     * Get the associated spellcheck {@link Oracle}.
     * 
     * @return the oracle
     */
    public SpellCheck.Oracle getSpellCheckOracle();
  }

  /**
   * 
   * A {@link Oracle} can be used to create {@link Response} objects from the
   * supplied {@link Request} object. It is the oracle's responsibility to call
   * {@link Callback#onSpellCheckResponseRecieved(com.google.gwt.user.client.ui.richtext.SpellCheck.Request, com.google.gwt.user.client.ui.richtext.SpellCheck.Response)}
   * on the supplied {@link Callback} object.
   * 
   */
  public abstract static class Oracle {

    /**
     * Requests a new spell check. The responsible is responsible for creating a
     * spell check <code>Response</code> and passing it into the method
     * {@link Callback#onSpellCheckResponseRecieved(com.google.gwt.user.client.ui.richtext.SpellCheck.Request, com.google.gwt.user.client.ui.richtext.SpellCheck.Response)}
     * 
     * @param request spell check request
     * @param callback spell check callback
     */
    public abstract void requestSpellCheck(Request request, Callback callback);
  }

  /**
   * Spell check request. The request fields represent a snapshot of the spell
   * check state when the request was generated.
   * 
   */
  public static class Request implements IsSerializable {
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
    Request() {
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
     * @return text
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
     * Sets the text to extract misspelled words from.
     * 
     * @param text the text
     */
    public void setText(String text) {
      this.text = text;
    }
  }

  /**
   * Spell check response. Created by {@link Oracle}. Comprises all misspelled
   * words represented in the corresponding {@link Request}.
   */
  public static class Response implements IsSerializable {

    private Collection misspellings;

    /**
     * Gets the misspelled words.
     * 
     * @return collection of misspellings
     */
    public Collection getMisspellings() {
      return misspellings;
    }

    /**
     * Sets the misspelled words.
     * 
     * @param misspellings misspellings
     */
    public void setMisspellings(Collection misspellings) {
      this.misspellings = misspellings;
    }

    /**
     * Sets the misspelled words.
     * 
     * @param misspellings mispellings
     */
    public void setMisspellings(Misspelling[] misspellings) {
      this.misspellings = Arrays.asList(misspellings);
    }

    public String toString() {
      return getMisspellings().toString();
    }
  }
  /**
   * Provides the labels needed for spell check.
   */
  interface LabelProvider {
    String noSuggestions();
  }

  /**
   * Callback used by {@link Oracle} for spell checking.
   */
  class MyCallback extends Callback {

    /**
     * CallBack used by {@link Oracle} once the {@link Response} has been
     * created.
     * 
     * @param request spell check request
     * @param response spell check response
     */
    public void onSpellCheckResponseRecieved(Request request, Response response) {
      SpellCheck.this.startSpellCheck(response.getMisspellings());
    }
  }

  /**
   * Set of spell check states used to communicate with the
   * {@link RichTextEditor}.
   */
  static class State {
    /**
     * Checking spelling ...
     */
    static final State CHECKING = new State();
    /**
     * Ready to re-check ...
     */
    static final State RECHECK = new State();
    /**
     * Ready to spell check ...
     */
    static final State SPELLCHECK = new State();

    /**
     * Spell check completed with no misspellings...
     */
    static final State NO_MISSPELLING = new State();
  }

  /**
   * Listener for spell check state changes.
   */
  interface StateListener {
    /**
     * Notified the listener when the spell check state has changed.
     * 
     * @param state current spell check state
     */
    void onChange(State state);
  }

  private static final String MISSPELLED_WORD = "misspelledWord";

  private static final int MILLISECONDS_DELAY = 1000;
  private Oracle oracle;
  private List highlights;
  private String locale = "en";
  private StateListener stateListener;

  private final RichTextArea target;

  private LabelProvider labels;

  /**
   * 
   * Constructor for <code>SpellCheck</code>.
   * 
   * @param target Spell Check target
   * @param labels labels used for spell check
   * @param stateListener
   */
  SpellCheck(RichTextArea target, LabelProvider labels,
      StateListener stateListener) {
    this.stateListener = stateListener;
    this.target = target;
    this.labels = labels;
  }

  public Oracle getSpellCheckOracle() {
    return this.oracle;
  }

  /**
   * Finish currently active spell check.
   */
  void finishSpellCheck() {
    clearHighlights();
    stateListener.onChange(State.SPELLCHECK);
  }

  /**
   * Gets the spell check locale.
   * 
   * @return spell check locale
   */
  String getLocale() {
    return locale;
  }

  /**
   * Request a new spell check from the spell check oracle.
   */
  void requestSpellCheck() {
    stateListener.onChange(State.CHECKING);
    clearHighlights();
    Request request = new Request();
    request.setText(target.getText());
    request.setLocale(locale);
    final Callback callBack = new MyCallback();

    if (oracle == null) {
      throw new IllegalStateException(
          "Before Requesting a SpellCheck, you must provide a SpellCheckModel.");
    }
    oracle.requestSpellCheck(request, callBack);
  }

  /**
   * Sets the spell check locale.
   * 
   * @param locale the locale
   */
  void setLocale(String locale) {
    this.locale = locale;
  }

  /**
   * Sets the spell check oracle.
   * 
   * @param oracle the oracle
   */
  void setOracle(Oracle oracle) {
    this.oracle = oracle;
  }

  /**
   * Starts a new spell check with the supplied set of misspellings.
   * 
   * @param misspellings supplied misspellings
   */
  void startSpellCheck(Collection misspellings) {
    if (misspellings.size() == 0) {
      stateListener.onChange(State.NO_MISSPELLING);
      Timer t = new Timer() {
        public void run() {
          stateListener.onChange(State.SPELLCHECK);
        }
      };
      t.schedule(MILLISECONDS_DELAY);
    } else {
      process(misspellings);
      stateListener.onChange(State.RECHECK);
    }
  }

  private void clearHighlights() {
    if (highlights != null) {
      for (int i = 0; i < highlights.size(); i++) {
        Highlight highlight = (Highlight) highlights.get(i);
        highlight.unhighlight();
      }
      highlights = null;
    }
  }

  private ItemPickerDropDownImpl createNoSuggestionsPopup() {
    SuggestPicker noSuggestions = new SuggestPicker();
    List noSuggestionsList = new ArrayList();
    noSuggestionsList.add(labels.noSuggestions());
    noSuggestions.setItems(noSuggestionsList.iterator());
    noSuggestions.setStyleName("gwt-RichTextEditor-NoSuggestions");
    return new ItemPickerDropDownImpl(target, noSuggestions);
  }

  private void process(Collection misspelledWords) {

    final Map wordsToSuggestions = new HashMap();

    for (Iterator iter = misspelledWords.iterator(); iter.hasNext();) {
      Misspelling miss = (Misspelling) iter.next();
      wordsToSuggestions.put(miss.getWord(), miss.getSuggestions());
    }

    HighlightCategory category = new HighlightCategory(MISSPELLED_WORD);
    Iterator i = target.addHighlights(wordsToSuggestions.keySet().iterator(),
        category);
    // This code could be removed if we created a target.unhighlight(category);
    highlights = new ArrayList();
    while (i.hasNext()) {
      highlights.add(i.next());
    }

    abstract class HighlightClickAndKeyboardHandler implements
        HighlightClickHandler, HighlightKeyboardHandler {

      public void onKeyPress(HighlightKeyboardEvent e) {
      }

      public void onKeyUp(HighlightKeyboardEvent e) {
      }
    }

    HighlightClickAndKeyboardHandler spellCheck = new HighlightClickAndKeyboardHandler() {
      Highlight selected;
      final ItemPickerDropDownImpl noSuggestions = createNoSuggestionsPopup();
      {
        noSuggestions.addChangeListener(new ChangeListener() {
          public void onChange(Widget sender) {
            selected.setText((String) noSuggestions.getSelectedValue());
            selected.unhighlight();
          }

        });
      }

      public void onClick(HighlightClickEvent event) {
        selected = event.getHighlight();
        select();
      }

      public void onKeyDown(HighlightKeyboardEvent e) {
        if (e.getControlKey() || e.getKeyCode() == '1') {
          System.err.println("got here");
        }
      }

      private void select() {
        Object key = selected.getHighlighted();
        List suggestions = (List) wordsToSuggestions.get(key);
        if (suggestions.size() > 0) {
          noSuggestions.hide();
          noSuggestions.setItems(suggestions.iterator());
          noSuggestions.showBelow(selected);
        } else {
          noSuggestions.hide();
          noSuggestions.showBelow(selected);
        }
      }
    };

    category.addClickHandler(spellCheck);
    category.addKeyboardHandler(spellCheck);
  }
}
