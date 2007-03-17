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

package com.google.gwt.user.client.ui;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.impl.UtilImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * Asynchronous set of classes used to allow users to provide suggestions for
 * certain widgets such as {@link SuggestBox}.
 * 
 */
public class Suggest {

  /**
   * Callback for {@link com.google.gwt.user.client.ui.Suggest.Oracle}. Every
   * {@link Request} should be associated with a callback that should be called
   * after a {@link  Response} is generated.
   * 
   */
  public abstract interface Callback {
    /**
     * Consume the suggestions created by a
     * {@link com.google.gwt.user.client.ui.Suggest.Oracle} in response to a
     * {@link Request}.
     * 
     * @param request the request
     * @param response the response
     * 
     */
    public abstract void onSuggestionsReceived(Request request,
        Response response);
  }

  /**
   * The default {@link com.google.gwt.user.client.ui.Suggest.Oracle}. Returns
   * potential suggestions based on breaking the query into separate words and
   * looking for matches. Modifies the returned text to show which prefix
   * matched the query term. The matching is case insensitive. All suggestions
   * are sorted before being passed into a response.
   * 
   * 
   * <p>
   * Example Table
   * </p>
   * <p>
   * <table width = "100%" border = "1">
   * <tr>
   * <td><b> All Suggestions </b> </td>
   * <td><b>Query string</b> </td>
   * </tr>
   * <td><b>Matching Suggestions</b></td>
   * <tr>
   * <td> John Smith, Joe Brown, Jane Doe, Jane Smith, Bob Jones</td>
   * <td> Jo</td>
   * <td> John Smith, Joe Brown, Bob Jones</td>
   * </tr>
   * <tr>
   * <td> John Smith, Joe Brown, Jane Doe, Jane Smith, Bob Jones</td>
   * <td> Smith</td>
   * <td> John Smith, Jane Smith</td>
   * </tr>
   * <tr>
   * <td> Georgia, New York, California</td>
   * <td> g</td>
   * <td> Georgia</td>
   * </tr>
   * </table>
   * </p>
   */
  public static final class DefaultOracle extends Oracle {
    /*
     * Implementation note: The DefaultOracle works by breaking suggestions down
     * into their component words using whitespace, finding all suggestions that
     * are potential matches, and then using substring matching to confirm the
     * match.
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
       * @return start
       */
      public int getStart() {
        return start;
      }
    }

    private static final String WORD_SPLITTER = " ";

    /**
     * Regular expression used to collapse all whitespace in a query string.
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
     * The List of masks used to prevent matching and replacing of the given
     * substrings.
     */
    private String[] masks;

    /**
     * Constructor for <code>DefaultOracle</code>.
     */
    public DefaultOracle() {
    }

    /**
     * Constructor for <code>DefaultOracle</code>. which takes in a set of
     * masks that filter its input.
     * 
     * 
     * 
     * @param masks the HTML tags and other strings to ignore for the purpose of
     *          matching.
     *          <p>
     *          For example: If <code>&lt;b&gt;</code> and
     *          <code>&lt;/b&gt; </code> are passed in as masks, then the
     *          string <code>&lt;b&gt;Dog&lt;/b&gt;</code> would <strong>not</strong>
     *          match the query "b".
     *          </p>
     */

    public DefaultOracle(Iterator masks) {
      // Move to UtilImpl if we need to do this more than once.
      List l = new ArrayList();
      while (masks.hasNext()) {
        l.add(masks.next());
      }
      this.masks = new String[l.size()];
      for (int i = 0; i < this.masks.length; i++) {
        this.masks[i] = (String) l.get(i);
      }
    }

    /**
     * Constructor for <code>DefaultOracle</code> which takes in a set of
     * masks that filter its input.
     * <p>
     * Example: If <code>&lt;b&gt;,&lt;/b&gt; </code> are passed in as masks,
     * then the string <code>&lt;b&gt;Dog&lt;/b&gt;</code> would <strong>not</strong>
     * match the query "b".
     * </p>
     * 
     * @param masks the HTML tags and other strings to ignore for the purpose of
     *          matching
     */
    public DefaultOracle(String[] masks) {
      setMasks(masks);
    }

    /**
     * Adds a suggestion to the oracle.
     * 
     * @param suggestion the suggestion
     */
    public void add(String suggestion) {
      String candidate = normalizeSuggestion(suggestion);
      toFormattedSuggestions.put(candidate, suggestion);
      String[] words = candidate.split(WORD_SPLITTER);

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
     * Adds all suggestions specified in the iterator.
     * 
     * @param iterator the iterator
     */
    public void addAll(Iterator iterator) {
      while (iterator.hasNext()) {
        add((String) iterator.next());
      }
    }

    /**
     * Adds all suggestions specified in the String[].
     * 
     * @param suggestions the array
     */
    public void addAll(String[] suggestions) {
      addAll(UtilImpl.asIterator(suggestions));
    }

    public void requestSuggestions(Request request, Callback callback) {

      final List suggestions = computeItemsFor(request.getQuery(),
          request.getLimit());
      Response response = new Response(suggestions);
      callback.onSuggestionsReceived(request, response);
    }

    /**
     * Compute the suggestions that are matches for a given query.
     * 
     * @param query search string
     * @param limit limit
     * @return matching suggestions
     */
    private List computeItemsFor(String query, int limit) {
      query = normalizeSearch(query);
      List candidates = createCandidatesFromSearch(query, limit);

      // Convert candidates to suggestions.
      return convertToFormattedSuggestions(query, candidates);
    }

    /**
     * Returns formatted suggestions with the given query in <code>strong</code>
     * html font.
     * 
     * @param query query string
     * @param candidates candidates
     * @return formatted suggestions
     */
    private List convertToFormattedSuggestions(String query, List candidates) {
      List suggestions = new ArrayList();
      for (int i = 0; i < candidates.size(); i++) {
        String candidate = (String) candidates.get(i);
        List offsets = findSearchOffsets(query, candidate);

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
     * Creates a set of potential candidates that match the given query.
     * 
     * @param limit number of candidates to return
     * @param query query string
     * @return possible candidates
     */
    private HashSet createCandidatesFromSearch(int limit, String query) {
      HashSet candidateSet = new HashSet();
      List words = tree.getSuggestions(query, limit);
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
     * Find the sorted list of candidates that are matches for the given query.
     */
    private List createCandidatesFromSearch(String query, int limit) {
      List candidates = new ArrayList();

      if (query.length() == 0) {
        return candidates;
      }
      // Find all words to search for.
      String[] searchWords = query.split(NORMALIZE_TO_SPACE);
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
        String newSearch = search.replaceAll(NORMALIZE_TO_SPACE, WORD_SPLITTER);
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
     * then the string <code>&lt;b&gt;Dog&lt;/b&gt;</code> would <strong>not</strong>
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
   * An object that implements this interface provides a
   * {@link com.google.gwt.user.client.ui.Suggest.Oracle} that it uses to
   * display suggestions. As the provided
   * {@link com.google.gwt.user.client.ui.Suggest.Oracle} may be immutable or
   * computed, no setter is required to conform to this interface.
   * 
   */
  public interface HasOracle {
    /**
     * Gets the provided {@link com.google.gwt.user.client.ui.Suggest.Oracle}.
     * 
     * @return the oracle
     */
    Oracle getSuggestOracle();
  }

  /**
   * 
   * A {@link com.google.gwt.user.client.ui.Suggest.Oracle} can be used to
   * create suggestions associated with a specific query string.
   * 
   */
  public abstract static class Oracle {
    private int limit = 20;

    /** 
     * Constructor for {@Oracle}.
     */
    public Oracle() {
    }

    /**
     * Generate a {@link Response} based on a specific {@link Request}.
     * 
     * @param request the request
     * @param callback the callback to use for the response
     */
    public abstract void requestSuggestions(Request request, Callback callback);
  }
  /**
   * A {@link com.google.gwt.user.client.ui.Suggest.Oracle} request.
   */
  public static class Request implements IsSerializable {
    private int limit = 20;
    private String query;

    /**
     * Constructor for {@link Request}.
     */
    public Request() {
    }

    /**
     * Constructor for {@link Request}.
     * 
     * @param query the query string
     */
    public Request(String query) {
      setQuery(query);
    }

    /**
     * 
     * Constructor for {@link Request}.
     * 
     * @param query the query string
     * @param limit limit on the number of suggestions that should be created
     *          for this query
     */
    public Request(String query, int limit) {
      setQuery(query);
      setLimit(limit);
    }

    /**
     * Gets the limit on the number of suggestions that should be created.
     * 
     * @return the limit
     */
    public int getLimit() {
      return limit;
    }

    /**
     * Gets the query string.
     * 
     * @return the query string
     */
    public String getQuery() {
      return query;
    }

    /**
     * Sets the limit on the number of suggestions that should be created.
     * 
     * @param limit the limit
     */
    public void setLimit(int limit) {
      this.limit = limit;
    }

    /**
     * Sets the query string used for this request.
     * 
     * @param query the query string
     */
    public void setQuery(String query) {
      this.query = query;
    }
  }

  /**
   * {@link com.google.gwt.user.client.ui.Suggest.Oracle} response. In order to
   * send a {@link Response} object over RPC, responses must currently be of
   * type {@link String}. Subclass the {@link Response} object in order to send
   * other types over RPC.
   */
  public static class Response implements IsSerializable {

    /**
     * @gwt.typeArgs <java.lang.String>
     */
    private Collection suggestions;

    /**
     * Constructor for {@link Response}.
     */
    public Response() {
    }

    /**
     * 
     * Constructor for {@link Response}.
     * 
     * @param suggestions each element of suggestions must have a human readable
     *          toString() method. If the {@link Response} is being passed over
     *          RPC, each element must be a {@link String} object.
     */
    public Response(Collection suggestions) {
      setSuggestions(suggestions);
    }

    /**
     * Gets the collection of suggestions.
     * 
     * @return the collection of suggestions. Each element of suggestions must
     *         have a human readable toString() method.
     */
    public Collection getSuggestions() {
      return this.suggestions;
    }

    /**
     * 
     * Sets the suggestions for this response.
     * 
     * @param suggestions each element of suggestions must have a human readable
     *          toString() method. If the {@link Response} is being passed over
     *          RPC, each element must be a {@link String} object.
     */
    public void setSuggestions(Collection suggestions) {
      this.suggestions = suggestions;
    }

    public String toString() {
      return "Suggest.Response: " + getSuggestions();
    }
  }

  /**
   * This class in not instantiatable.
   */
  private Suggest() {
  }

}
