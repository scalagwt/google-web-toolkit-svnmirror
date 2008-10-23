/*
 * Copyright 2008 Google Inc.
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

package com.google.gwt.user.client;

import com.google.gwt.event.shared.AbstractEvent;

/**
 * Fired when the user clicks the browser's 'back' or 'forward' buttons.
 */
public class HistoryChangeEvent extends AbstractEvent {
  /**
   * The event type.
   */
  public static final Type<HistoryChangeEvent, HistoryChangeHandler> TYPE = new Type<HistoryChangeEvent, HistoryChangeHandler>() {
    @Override
    protected void fire(HistoryChangeHandler handler, HistoryChangeEvent event) {
      handler.onHistoryChanged(event);
    }
  };

  /**
   * The token representing the current history state.
   */
  private String historyToken = null;

  /**
   * Construct a new {@link HistoryChangeEvent}.
   * 
   * @param historyToken the token representing the current history state.
   */
  public HistoryChangeEvent(String historyToken) {
    this.historyToken = historyToken;
  }

  /**
   * Get the token representing the current history state.
   * 
   * @return the history token.
   */
  public String getHistoryToken() {
    return historyToken;
  }

  @Override
  protected Type getType() {
    return TYPE;
  }
}
