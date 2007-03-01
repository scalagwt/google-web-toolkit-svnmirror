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

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Helper class for widgets that accept {@link HighlightClickHandlers}. This
 * {@link java.util.Collection} assumes that all items added to it will be of
 * type {@link HighlightClickHandler}.
 */
class HighlightClickHandlerCollection extends ArrayList {

  /**
   * Fires a {@link HighlightClickHandler#onClick(HighlightClickEvent)} on all
   * handlers in the collection.
   * 
   * @param event the {@link HighlightClickEvent} being fired
   */
  void fireClickEvent(HighlightClickEvent event) {
    for (Iterator it = iterator(); it.hasNext();) {
      if (event.isCanceled()) {
        break;
      }
      HighlightClickHandler handler = (HighlightClickHandler) it.next();
      handler.onClick(event);
    }
  }
}