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
package com.google.gwt.user.client.ui;

/**
 * A widget that implements this interface fires the events defined by the
 * {@link SuggestionHandler} interface.
 */
@Deprecated
public interface FiresSuggestionEvents {

  /**
   * Adds a handler interface to receive suggestion events.
   * 
   * @param handler the handler to add
   */
  @Deprecated
  void addEventHandler(SuggestionHandler handler);

  /**
   * Removes a previously added handler interface.
   * 
   * @param handler the handler to remove.
   */
  @Deprecated
  void removeEventHandler(SuggestionHandler handler);
}
