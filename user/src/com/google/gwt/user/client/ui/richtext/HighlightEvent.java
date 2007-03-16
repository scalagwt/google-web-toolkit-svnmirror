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

import java.util.EventObject;

/**
 * The superclass of all events that are generated from {@link Highlight}.
 * Notice that the DOM events and elements used here are those inside the
 * iframe, and not the ones outside.
 */
class HighlightEvent extends EventObject {
  /*
   * Implementation note: Most of this functionality should belong in a
   * superclass BrowserEvent that is for all DOM based events.
   */
  /**
   * The underlying DOM {@link Event}.
   */
  protected final Event event;

  /**
   * Creates a new {@link HighlightEvent}.
   * 
   * @param highlight the {@link Highlight} that received the event
   * @param domEvent the underlying {@link DOM} {@link Event} in the
   *          richTextArea DOM
   */
  HighlightEvent(Highlight highlight, Event domEvent) {
    super(highlight);
    this.event = domEvent;
  }

  /**
   * Cancels the event, so that no other handlers or listeners will processes
   * it, and it will be preventDefault'ed.
   * 
   */
  public void cancel() {
    DOM.eventPreventDefault(getEvent());
    DOMUtil.markEventPrevented(getEvent());
  }

  /**
   * Gets the underlying Javascript event.
   * 
   * @return the underlying Javascript event
   */
  public Event getEvent() {
    return event;
  }

  /**
   * Gets the {@link Highlight} object, otherwise known as the source of the
   * event.
   * 
   * @return the {@link Highlight} object
   */
  public Highlight getHighlight() {
    return (Highlight) getSource();
  }

  /**
   * Has the event been cancelled?
   * 
   * @return whether the event has been cancelled
   */
  public boolean isCanceled() {
    return DOMUtil.eventWasPrevented(getEvent());
  }
}