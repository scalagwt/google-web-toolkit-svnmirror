/*
 * Copyright 2007 Google Inc.
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

package com.google.gwt.user.client.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Popup list of suggestions.
 */
public class SuggestionsPopup extends SelectablePopup {

  /*
   * Implementation note:
   * 
   * SuggestPopup keeps a cached list of items. When the displayed contents are
   * less then the items, the last items are invisible.
   * 
   */
  /**
   * Return potential suggestions based on substrings of words. Also modifies
   * the returned text to show which prefix matched the search term.
   */
  public static class SuggestionsController extends ItemController {
    /*
     * Implementation note: The SuggestionsController works by breaking
     * suggestions down into their component words using whitespace, finding all
     * suggestions that are potential matches, and then using substring matching
     * to confirm the match.
     * 
     */
    /**
     * Convenience class to hold word offsets.
     */
    private class OffsetHolder implements Comparable {
      private int start;
      private int finish;

      /**
       * Constructor for <code>OffsetHolder</code>.
       * 
       * @param start offset start
       * @param finish offset finish
       */

      public OffsetHolder(int start, int finish) {
        this.start = start;
        this.finish = finish;
      }

      /**
       * Use <code>start</code> to sort offsets.
       */
      public int compareTo(Object arg0) {
        OffsetHolder other = (OffsetHolder) arg0;
        if (start < other.getStart()) {
          return -1;
        } else if (start > other.getStart()) {
          return 1;
        } else {
          return 0;
        }
      }

      /**
       * Gets the offset's finish.
       * 
       * @return the finish
       */
      public int getFinish() {
        return finish;
      }

      /**
       * Gets the offset's start.
       * 
       * @return
       */
      public int getStart() {
        return start;
      }
    }

    /**
     * Regular expression used to collapse all whitespace in a search string.
     */
    private static final String NORMALIZE_TO_SPACE = "\\s+";

    /**
     * Associates substrings with words.
     */
    private final PrefixTree tree = new PrefixTree();

    /**
     * Associates individual words with candidates.
     */
    private HashMap toCandidates = new HashMap();

    /**
     * Associates candidates with their formatted suggestions.
     */
    private HashMap toFormattedSuggestions = new HashMap();

    /**
     * The set of masks used to prevent matching and replacing of the given
     * substrings.
     */
    private String[] masks;

    /**
     * Should all suggestions be returned if there is no valid subset for a
     * given search term?
     */
    private boolean showAllOnEmpty;

    /**
     * 
     * Constructor for <code>SuggestionsController</code>.
     */
    public SuggestionsController() {
    }

    /**
     * Constructor for <code>SuggestPopup</code> which takes in a set of masks
     * that filter its input.
     * <p>
     * Example: If <code>&lt;b&gt;,&lt;/b&gt; </code> are passed in as masks,
     * then the string <code>&lt;b&gt;Dog&lt;/b&gt</code> would <strong>not</strong>
     * match the search string "b".
     * 
     * @param masks the HTML tags and other strings to ignore for the purpose of
     *          matching
     */
    public SuggestionsController(String[] masks) {
      setMasks(masks);
    }

    /**
     * Adds a suggestion to the controller.
     * 
     * @param suggestion the suggestion
     */
    public void add(String suggestion) {
      String candidate = normalizeSuggestion(suggestion);
      toFormattedSuggestions.put(candidate, suggestion);
      String[] words = candidate.split(" ");

      for (int i = 0; i < words.length; i++) {
        String word = words[i];
        tree.add(word);
        HashSet l = (HashSet) toCandidates.get(word);
        if (l == null) {
          l = new HashSet();
          toCandidates.put(word, l);
        }
        l.add(candidate);
      }
    }

    /**
     * Returns the suggestion behavior when the valid suggestion set is empty.
     * 
     * @return whether to show all suggestions when the valid suggestion set is
     *         empty
     */
    public boolean isShowAllOnEmpty() {
      return showAllOnEmpty;
    }

    /**
     * Sets the suggestion behavior when the valid suggestion set is empty.
     * 
     * @param showAllOnEmpty should all suggestions be shown when the valid
     *          suggestion set is empty? Defaults to <code>false</code>.
     */
    public void setShowAllOnEmpty(boolean showAllOnEmpty) {
      this.showAllOnEmpty = showAllOnEmpty;
    }

    /**
     * Configures and displays a popup.
     * 
     * @param popup popup to show
     * @param input represents the input to the controller
     * @param showBelow <code>UIObject</code> to show the popup below
     */
    public void showBelow(SelectablePopup popup, String input,
        UIObject showBelow) {
      List filteredItems = computeItemsFor(input);
      if (filteredItems.size() > 0) {
        popup.setItems(filteredItems);
        popup.showBelow(showBelow);
      } else {
        popup.hide();
      }
    }

    /**
     * Compute the suggestions that are matches for a given search string.
     * 
     * @param search search string
     * @return matching suggestions
     */
    private List computeItemsFor(String search) {
      search = normalizeSearch(search);
      List candidates = createCandidatesFromSearch(search, getLimit());

      if (candidates.size() == 0) {
        // All candidates eliminated.
        List suggestions = candidates;
        if (isShowAllOnEmpty()) {
          suggestions.addAll(toFormattedSuggestions.values());
        }
        return suggestions;
      } else {
        // Convert candidates to suggestions.
        return convertToFormattedSuggestions(search, candidates);
      }
    }

    /**
     * Returns formatted suggestions with the given search string in
     * <code>strong</code> html font.
     * 
     * @param search search string
     * @param candidates candidates
     * @return formatted suggestions
     */
    private List convertToFormattedSuggestions(String search, List candidates) {
      List suggestions = new ArrayList();
      for (int i = 0; i < candidates.size(); i++) {
        String candidate = (String) candidates.get(i);
        List offsets = findSearchOffsets(search, candidate);

        // Exact search string was not found, this can happen when the search
        // string has multiple words but the candidate does not contain those
        // words in the correct order. In this case,do not include this
        // suggestion.
        if (offsets.size() == 0) {
          continue;
        }

        int start = 0;
        StringBuffer accum = new StringBuffer();

        // Use correctly formatted suggestion for assembly.
        String formattedSuggestion = (String) toFormattedSuggestions.get(candidate);
        for (int k = 0; k < offsets.size(); k += 1) {
          OffsetHolder holder = (OffsetHolder) offsets.get(k);
          String part1 = formattedSuggestion.substring(start, holder.getStart());
          String part2 = formattedSuggestion.substring(holder.getStart(),
              holder.getFinish());
          start = holder.getFinish();

          accum.append(part1).append("<strong>").append(part2).append(
              "</strong>");
        }
        String end = formattedSuggestion.substring(start);
        accum.append(end);
        suggestions.add(accum.toString());
      }
      return suggestions;
    }

    /**
     * Creates a set of potential candidates that match on a single search
     * string.
     * 
     * @param limit number of candidates to return
     * @param search search string
     * @return possible candidates
     */
    private HashSet createCandidatesFromSearch(int limit, String search) {
      HashSet candidateSet = new HashSet();
      List words = tree.getSuggestions(search, limit);
      if (words != null) {
        // Find all candidates that contain the given word the search is a
        // subset of.
        for (int i = 0; i < words.size(); i++) {
          Collection belongsTo = (Collection) toCandidates.get(words.get(i));
          if (belongsTo != null) {
            candidateSet.addAll(belongsTo);
          }
        }
      }
      return candidateSet;
    }

    /**
     * Find the sorted list of candidates that are matches for the given search.
     */
    private List createCandidatesFromSearch(String search, int limit) {
      List candidates = new ArrayList();

      // Find all words to search for.
      String[] searchWords = search.split(NORMALIZE_TO_SPACE);
      HashSet candidateSet = null;
      for (int i = 0; i < searchWords.length; i++) {
        String word = searchWords[i];

        // Eliminate bogus word choices.
        if (word.length() == 0 || word.matches(NORMALIZE_TO_SPACE)) {
          continue;
        }

        // Find the set of candidates that are associated with all the
        // searchWords.
        HashSet thisWordChoices = createCandidatesFromSearch(limit, word);
        if (candidateSet == null) {
          candidateSet = thisWordChoices;
        } else {
          candidateSet.retainAll(thisWordChoices);

          if (candidateSet.size() < 2) {
            // If there is only one candidate, on average it is cheaper to
            // check if that candidate contains our search string than to
            // continue intersecting suggestion sets.
            break;
          }
        }
      }
      if (candidateSet != null) {
        candidates.addAll(candidateSet);
        Collections.sort(candidates);
      }
      return candidates;
    }

    /**
     * Returns the offset position of the search string within the suggestion.
     * 
     * @param search search string
     * @param suggestion suggestion
     * @return offsets of search string within suggestion
     */
    private List findSearchOffsets(String search, String suggestion) {
      List offsets = new ArrayList();
      int index = 0;
      while (true) {
        index = suggestion.indexOf(search, index);
        if (index == -1) {
          break;
        }
        int endIndex = index + search.length();
        if (index == 0 || (' ' == suggestion.charAt(index - 1))) {
          OffsetHolder holder = new OffsetHolder(index, endIndex);
          offsets.add(holder);
        }
        index = endIndex;
      }
      return offsets;
    }

    /**
     * Normalize the search key by making it lower case, removing multiple
     * spaces, apply masks, and make it lower case.
     */
    private String normalizeSearch(String search) {
      // Use the same masks and case normalization for the search string as was
      // used with the candidate values.
      search = normalizeSuggestion(search);

      // Remove all excess whitespace from the search string.
      while (true) {
        String newSearch = search.replaceAll(NORMALIZE_TO_SPACE, " ");
        if (newSearch.equals(search)) {
          break;
        } else {
          search = newSearch;
        }
      }

      return search.trim();
    }

    /**
     * Takes the formatted suggestion, makes it lower case and applies any
     * available masks.
     */
    private String normalizeSuggestion(String formattedSuggestion) {
      StringBuffer suggestionToSearch = new StringBuffer();

      // Formatted address should already have normalized whitespace. So we can
      // skip this step.

      // Lower case suggestion.
      suggestionToSearch.append(formattedSuggestion.toLowerCase());

      // Apply masks.
      if (masks != null) {
        for (int i = 0; i < masks.length; i++) {
          String ignore = masks[i];

          int ignoreStart = 0;
          while (true) {
            ignoreStart = formattedSuggestion.indexOf(ignore, ignoreStart);
            if (ignoreStart == -1) {
              break;
            }
            int endChar = ignoreStart + ignore.length();
            for (int j = ignoreStart; j < endChar; j++) {
              suggestionToSearch.setCharAt(j, ' ');
            }
            ignoreStart = endChar;
          }
        }
      }
      String suggestion = suggestionToSearch.toString();
      return suggestion;
    }

    /**
     * When manipulating suggestions, we want to ignore certain HTML tags and
     * characters, setting a mask allows us to do so.
     * <p>
     * Example: If <code>&lt;b&gt;,&lt;/b&gt; </code> are passed in as masks,
     * then the string <code>&lt;b&gt;Dog&lt;/b&gt</code> would <strong>not</strong>
     * match the search string "b".
     * 
     * @param masks the HTML tags and other strings to ignore for the purpose of
     *          matching
     */
    private void setMasks(String[] masks) {
      this.masks = masks;
    }
  }

  /**
   * Default style for the suggestion popup.
   */
  private static final String STYLE_DEFAULT = "gwt-SuggestionsPopup";

  private int startInvisible = Integer.MAX_VALUE;

  /**
   * Constructor for <code>SuggestPopup</code>.
   */
  public SuggestionsPopup() {
    setStyleName(STYLE_DEFAULT);
  }

  public boolean navigate(char keyCode) {
    if (isAttached()) {
      switch (keyCode) {
        case KeyboardListener.KEY_DOWN:
          shiftSelection(1);
          break;
        case KeyboardListener.KEY_UP:
          shiftSelection(-1);
          break;
        case KeyboardListener.KEY_ENTER:
          click();
          break;
        case KeyboardListener.KEY_ESCAPE:
          hide();
          break;
        default:
          // Avoid shared post processing.
          return false;
      }
      return true;
    } else {
      return false;
    }
  }

  public void setItems(List suggestions) {
    // Render visible all needed cells.
    int min = Math.min(suggestions.size(), getItemCount());
    for (int i = startInvisible; i < min; i++) {
      setVisible(i, true);
    }

    // Render invisible all useless cells.
    startInvisible = suggestions.size();
    for (int i = suggestions.size(); i < getItemCount(); i++) {
      setVisible(i, false);
    }

    // Ensure all needed items exist and set each item's html to the given
    // suggestion.
    for (int i = 0; i < suggestions.size(); i++) {
      Item item = ensureItem(i);
      String suggestion = (String) suggestions.get(i);
      item.setHTML(suggestion);
    }
  }

  /**
   * Shifts the current selection by the given amount, unless that would put the
   * user past the beginning or end of the list.
   * 
   * @param shift the amount to shift the current selection by.
   */
  public void shiftSelection(int shift) {
    int newSelect = getSelectedIndex() + shift;
    if (newSelect >= getItemCount() || newSelect < 0
        || newSelect >= startInvisible) {
      return;
    }
    setSelection(getItem(newSelect));
  }

  /**
   * Ensures the existence of the given item and returns it.
   * 
   * @param itemIndex item index to ensure
   * @return associated item
   */
  private Item ensureItem(int itemIndex) {
    for (int i = getItemCount(); i <= itemIndex; i++) {
      Item item = new Item(i);
      getLayout().setWidget(i, 0, item);
    }
    return getItem(itemIndex);
  }

  /**
   * Sets whether the given item is visible.
   * 
   * @param itemIndex item index
   * @param visible visible boolean
   */
  private void setVisible(int itemIndex, boolean visible) {
    UIObject.setVisible(getLayout().getRowFormatter().getElement(itemIndex),
        visible);
  }
}