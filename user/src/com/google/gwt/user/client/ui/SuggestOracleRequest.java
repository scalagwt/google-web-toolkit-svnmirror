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

public class SuggestOracleRequest implements IsSerializable {
  private int limit = 20;
  private String query;

  public SuggestOracleRequest() {
  }

  public SuggestOracleRequest(String query) {
    setQuery(query);
  }

  public SuggestOracleRequest(String query, int limit) {
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
   * Gets the query string used for this request.
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
   * @param query string
   */
  public void setQuery(String query) {
    this.query = query;
  }
}