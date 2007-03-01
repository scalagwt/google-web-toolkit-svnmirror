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

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Helper class for widgets that accept
 * {@link com.google.gwt.user.client.ui.richtext.HighlightMouseHandler HighlightMouseHandlers}.
 * This {@link java.util.Collection} assumes that all items added to it will be of type
 * {@link com.google.gwt.user.client.ui.richtext.HighlightMouseHandler}.
 */
class HighlightMouseHandlerCollection extends ArrayList {

  /**
   * Fires a {@link HighlightMouseHandler#onMouseDown(HighlightMouseEvent)} on
   * all handlers in the collection.
   * 
   * @param event the {@link HighlightMouseEvent} to send
   */
  void fireMouseDown(HighlightMouseEvent event) {
    
    for (Iterator it = iterator(); it.hasNext();) {
      if (event.isCanceled()) {
        break;
      }
      HighlightMouseHandler handler = (HighlightMouseHandler) it.next();
      handler.onMouseDown(event);
    }
  }

  /**
   * Automatically fires the appropriate mouse event to all listeners on this
   * highlight. If the given event is not a mouse event, no action will be
   * performed.
   * 
   * @param event the event being fired
   */
  void fireMouseEvent(HighlightMouseEvent event) {

    switch (DOM.eventGetType(event.getEvent())) {
      case Event.ONMOUSEDOWN:
        fireMouseDown(event);
        break;

      case Event.ONMOUSEUP:
        fireMouseUp(event);
        break;

      case Event.ONMOUSEMOVE:
        fireMouseMove(event);
        break;
    }
  }

  /**
   * Fires a {@link HighlightMouseHandler#onMouseMove(HighlightMouseEvent)} on
   * all handlers in the collection.
   * 
   * 
   * @param event the event being fired
   */
  void fireMouseMove(HighlightMouseEvent event) {

    for (Iterator it = iterator(); it.hasNext();) {
      if (event.isCanceled()) {
        break;
      }
      HighlightMouseHandler handler = (HighlightMouseHandler) it.next();
      handler.onMouseMove(event);
    }
  }

  /**
   * Fires a {@link HighlightMouseHandler#onMouseUp(HighlightMouseEvent)} on all
   * handlers in the collection.
   * 
   * 
   * @param event the event being fired
   */
  void fireMouseUp(HighlightMouseEvent event) {
    for (Iterator it = iterator(); it.hasNext();) {
      if (event.isCanceled()) {
        break;
      }
      HighlightMouseHandler handler = (HighlightMouseHandler) it.next();
      handler.onMouseUp(event);
    }
  }
}