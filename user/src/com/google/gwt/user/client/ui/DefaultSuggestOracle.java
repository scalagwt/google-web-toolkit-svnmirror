package com.google.gwt.user.client.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * Return potential suggestions based on substrings of words. Also modifies the
 * returned text to show which prefix matched the search term.
 */
public class DefaultSuggestOracle extends SuggestOracle {
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
   * Should all suggestions be returned if there is no valid subset for a given
   * search term?
   */
  private boolean showAllOnEmpty;

  /**
   * Constructor for <code>DefaultOracle</code>.
   */
  public DefaultSuggestOracle() {
  }

  /**
   * Constructor for <code>DefaultSuggestOracle</code> which takes in a set of
   * masks that filter its input.
   * <p>
   * Example: If <code>&lt;b&gt;,&lt;/b&gt; </code> are passed in as masks,
   * then the string <code>&lt;b&gt;Dog&lt;/b&gt</code> would <strong>not</strong>
   * match the search string "b".
   * 
   * @param masks the HTML tags and other strings to ignore for the purpose of
   *          matching
   */

  public DefaultSuggestOracle(Collection masks) {
    this.masks = new String[masks.size()];
    Iterator maskIter = masks.iterator();
    for (int i = 0; i < this.masks.length; i++) {
      this.masks[i] = (String) maskIter.next();
    }
  }

  /**
   * Constructor for <code>DefaultSuggestOracle</code> which takes in a set of
   * masks that filter its input.
   * <p>
   * Example: If <code>&lt;b&gt;,&lt;/b&gt; </code> are passed in as masks,
   * then the string <code>&lt;b&gt;Dog&lt;/b&gt</code> would <strong>not</strong>
   * match the search string "b".
   * 
   * @param masks the HTML tags and other strings to ignore for the purpose of
   *          matching
   */
  public DefaultSuggestOracle(String[] masks) {
    setMasks(masks);
  }

  /**
   * Adds all items specified in the iterator.
   * 
   * @param iterator the iterator
   */
  public void add(Iterator iterator) {
    while (iterator.hasNext()) {
      add((String) iterator.next());
    }
  }

  /**
   * Adds a suggestion to the oracle.
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
   * Compute the suggestions that are matches for a given search string.
   * 
   * @param search search string
   * @return matching suggestions
   */
  public List computeItemsFor(String search, int limit) {
    search = normalizeSearch(search);
    List candidates = createCandidatesFromSearch(search, limit);

    if (candidates.size() == 0) {
      // All candidates eliminated.
      List suggestions = candidates;
      if (isShowAllOnEmpty()) {
        suggestions.addAll(toFormattedSuggestions.values());
        Collections.sort(suggestions);
      }

      return suggestions;
    } else {
      // Convert candidates to suggestions.
      return convertToFormattedSuggestions(search, candidates);
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

  public void requestSuggestions(SuggestOracleRequest request,
      SuggestOracleCallback callback) {

    final List suggestions = computeItemsFor(request.getQuery(),
        request.getLimit());
    SuggestOracleResponse response = new SuggestOracleResponse() {

      public Iterator iterator() {
        return suggestions.iterator();
      }

    };
    callback.onSuggestionsRecieved(request, response);
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

        accum.append(part1).append("<strong>").append(part2).append("</strong>");
      }
      String end = formattedSuggestion.substring(start);
      accum.append(end);
      suggestions.add(accum.toString());
    }
    return suggestions;
  }

  /**
   * Creates a set of potential candidates that match on a single search string.
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
   * Normalize the search key by making it lower case, removing multiple spaces,
   * apply masks, and make it lower case.
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