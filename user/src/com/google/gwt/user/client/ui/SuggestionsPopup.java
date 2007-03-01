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

import com.google.gwt.user.client.ui.PrefixTree;

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
   * SuggestPopup keeps a cached list of items. When the displaying contents are
   * less then the items, the last items are invisible.
   * 
   */
  /**
   * Return potential suggestions based on substrings of words. Also modifies
   * the returned text to show which prefix matched the search term.
   */
  public static class SuggestionsController extends ItemController {
    /**
     * Convenience class to hold word offsets.
     */
    private class OffsetHolder implements Comparable {
      private int start;
      private int finish;

      public OffsetHolder(int start, int finish) {
        this.start = start;
        this.finish = finish;
      }

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

      public int getFinish() {
        return finish;
      }

      public int getStart() {
        return start;
      }
    }

    /**
     * Regex used to collapse all whitespace in a search string.
     */
    private static final String NORMALIZE_TO_SPACE = "\\s+";

    /*
     * The suggest tree works by breaking suggestions down into their component
     * words using whitespace, finding all suggestions that are potential
     * matches, and then using substring matching to confirm the match.
     * 
     */
    private final PrefixTree tree = new PrefixTree();
    private HashMap toCandidates = new HashMap();
    private HashMap toFormattedSuggestions = new HashMap();

    /**
     * Prevent matching and replacing of the given substrings.
     */
    private String[] masks;

    private boolean showAllOnEmpty;

    /**
     * Adds a suggestion to the controller.
     * 
     * @param suggestion the suggestion
     */
    public void add(String suggestion) {
      String candidate = normalizeSearchValue(suggestion);
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

    public List computeItemsFor(String search) {
      search = normalizeSearchKey(search);
      List candidates = createCandidatesFromSearch(search, getLimit());
      // All candidates eliminated.
      if (candidates.size() == 0) {
        if (isShowAllOnEmpty()) {
          candidates.addAll(toFormattedSuggestions.values());
        }
        return candidates;
      }
      Collections.sort(candidates);
      return getFormattedSuggestions(search, candidates);
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
     * When manipulating suggestions, we want to ignore certain HTML tags and
     * characters, setting a mask allows us to do so.
     * 
     * @param masks the HTML tags and other strings to ignore
     */
    public void setMasks(String[] masks) {
      this.masks = masks;
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
     * Find the list of candidates that are matches for the given search.
     */
    private List createCandidatesFromSearch(String search, int limit) {
      List candidates = new ArrayList();
      // Find the suggestion set.
      String[] searchWords = search.split(NORMALIZE_TO_SPACE);
      HashSet suggestionSet = null;
      for (int i = 0; i < searchWords.length; i++) {
        String word = searchWords[i];

        // Eliminate bogus word choices.
        if (word.length() == 0 || word.matches(NORMALIZE_TO_SPACE)) {
          searchWords[i] = null;
          continue;
        }

        // Find the set of answers that are associated with all the searchWords.
        HashSet thisWordChoices = createCandidatesFromWord(limit, word);
        if (suggestionSet == null) {
          suggestionSet = thisWordChoices;
        } else {
          suggestionSet.retainAll(thisWordChoices);
          if (suggestionSet.size() < 2) {
            break;
          }
        }
      }
      if (suggestionSet != null) {
        candidates.addAll(suggestionSet);
      }
      return candidates;
    }

    private HashSet createCandidatesFromWord(int limit, String thisWord) {
      HashSet answerChoices = new HashSet();
      List words = tree.getSuggestions(thisWord, limit);
      if (words != null) {
        for (int i = 0; i < words.size(); i++) {
          Collection belongsTo = (Collection) toCandidates.get(words.get(i));
          if (belongsTo != null) {
            answerChoices.addAll(belongsTo);
          }
        }
      }
      return answerChoices;
    }

    private List findSearchOffsets(String search, String suggestion) {
      List offsets = new ArrayList();
      int index = 0;
      while (true) {
        index = suggestion.indexOf(search, index);
        if (index == -1) {
          break;
        }
        int endIndex = index + search.length();
        if (index == 0
            || !Character.isLetterOrDigit(suggestion.charAt(index - 1))) {
          OffsetHolder holder = new OffsetHolder(index, endIndex);
          offsets.add(holder);
        }
        index = endIndex;
      }
      return offsets;
    }

    private List getFormattedSuggestions(String search, List candidates) {
      List suggestions = new ArrayList();

      for (int i = 0; i < candidates.size(); i++) {
        String suggestion = (String) candidates.get(i);
        List offsets = findSearchOffsets(search, suggestion);

        // search was not found, do not include this suggestion.
        if (offsets.size() == 0) {
          break;
        }

        int start = 0;
        StringBuffer accum = new StringBuffer();

        // Use correctly formatted suggestion for assembly.
        String formattedSuggestion = (String) toFormattedSuggestions.get(suggestion);
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
     * Normalize the search key.
     */
    private String normalizeSearchKey(String search) {

      search = normalizeSearchValue(search);

      // We want to remove all excess whitespace.
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
    private String normalizeSearchValue(String formattedSuggestion) {
      StringBuffer suggestionToSearch = new StringBuffer();
      // Formatted address should already have normalized whitespace.
      suggestionToSearch.append(formattedSuggestion.toLowerCase());
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
  }

  private int startInvisible = Integer.MAX_VALUE;

  /**
   * Constructor for <code>SuggestPopup</code>.
   */
  public SuggestionsPopup() {
    setStyleName("gwt-SuggestPopup");
  }

  public void setItems(List suggestions) {
    // Render Visible all needed cells.
    int min = Math.min(suggestions.size(), getItemCount());
    for (int i = startInvisible; i < min; i++) {
      setVisible(i, true);
    }
    startInvisible = suggestions.size();

    for (int i = suggestions.size(); i < getItemCount(); i++) {
      setVisible(i, false);
    }

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
   * Control the popup based upon the supplied key code.
   * 
   * @param keyCode supplied key code
   * @return whether the keyCode effected navigation
   */
  boolean navigate(char keyCode) {
    if (isAttached()) {
      switch (keyCode) {
        case KeyboardListener.KEY_DOWN:
          shiftSelection(1);
          break;
        case KeyboardListener.KEY_UP:
          shiftSelection(-1);
          break;
        case KeyboardListener.KEY_ENTER:
          fireChange();
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

  private Item ensureItem(int itemIndex) {
    for (int i = getItemCount(); i <= itemIndex; i++) {
      Item item = new Item(i);
      getLayout().setWidget(i, 0, item);
    }
    return getItem(itemIndex);
  }

  private void setVisible(int itemIndex, boolean visible) {
    UIObject.setVisible(getLayout().getRowFormatter().getElement(itemIndex),
        visible);
  }
}