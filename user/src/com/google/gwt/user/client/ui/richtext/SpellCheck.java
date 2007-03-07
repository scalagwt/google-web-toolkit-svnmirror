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
import com.google.gwt.user.client.ui.SuggestionsPopup;
import com.google.gwt.user.client.ui.Widget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Spell Check for {@link RichTextArea}.
 */
public class SpellCheck {

  /**
   * Represents a single misspelled word and the set of suggestions associated
   * with that word.
   */
  public static class Misspelling implements IsSerializable {
    private String word;
    private String[] suggestions;

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

  /**
   * Allows the spell check to communicate with a user's data source. Most
   * implementations of {@link Model} should be a simple delegations to the
   * user's server communication infrastructure, such as an RPC Service.
   */
  public interface Model {

    /**
     * Requests a new spell check. The model is responsible for creating a spell
     * check <code>Response</code> object and passing it into
     * {@link CallBack#processSpellCheckResponse(com.google.gwt.user.client.ui.richtext.SpellCheck.Request, com.google.gwt.user.client.ui.richtext.SpellCheck.Response)}
     */
    public void spellCheck(Request request, CallBack callback);
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

  /**
   * Spell check response. Created by {@link Model}. Comprises all misspelled
   * words represented in the corresponding {@link Request}.
   */
  public static class Response implements IsSerializable {
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
  /**
   * Provides the labels needed for spell check.
   */
  interface LabelProvider {
    String noSuggestions();
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

  /**
   * Callback used by {@link Model} for spell checking.
   */
  public class CallBack {
    private CallBack() {
    }

    /**
     * CallBack used by {@link Model} once the {@link Response} has been
     * created.
     * 
     * @param response response
     */
    public void processSpellCheckResponse(Request request, Response response) {
      startSpellCheck(response.getMisspellings());
    }
  }

  private static final int MILLISECONDS_DELAY = 500;
  private Model model;
  private List highlights;
  private String locale = "en";
  private StateListener stateListener;

  private RichTextArea target;

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
   * Request a new spell check from the spell check model.
   */
  void requestSpellCheck() {
    stateListener.onChange(State.CHECKING);
    clearHighlights();
    Request request = new Request();
    request.setText(target.getText());
    request.setLocale(locale);
    final CallBack callBack = new CallBack();

    if (model == null) {
      throw new IllegalStateException(
          "Before Requesting a SpellCheck, you must provide a SpellCheckModel.");
    }
    model.spellCheck(request, callBack);
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
   * Sets the spell check model.
   * 
   * @param model the model
   */
  void setModel(Model model) {
    this.model = model;
  }

  /**
   * Starts a new spell check with the supplied set of misspellings.
   * 
   * @param misspellings supplied misspellings
   */
  void startSpellCheck(Misspelling[] misspellings) {
    if (misspellings.length == 0) {
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

  private SuggestionsPopup createNoSuggestionsPopup() {
    SuggestionsPopup noSuggestions = new SuggestionsPopup();
    List noSuggestionsList = new ArrayList();
    noSuggestionsList.add(labels.noSuggestions());
    noSuggestions.setItems(noSuggestionsList.iterator());
    noSuggestions.setStyleName("gwt-RichTextEditor-NoSuggestions");
    return noSuggestions;
  }

  private void process(Misspelling[] entries) {
    List mispelledWords = new ArrayList();
    final Map wordsToSuggestions = new HashMap();
    for (int i = 0; i < entries.length; i++) {
      mispelledWords.add(entries[i].getWord());
      wordsToSuggestions.put(entries[i].getWord(),
          Arrays.asList(entries[i].getSuggestions()));
    }
    HighlightCategory category = new HighlightCategory("misspelledWord");
    Iterator i = target.addHighlights(mispelledWords, category);
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
      final SuggestionsPopup noSuggestions = createNoSuggestionsPopup();
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
