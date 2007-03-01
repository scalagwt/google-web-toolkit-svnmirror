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

import com.google.gwt.user.client.Event;

/**
 * The event used to denote a click on a {@link Highlight}.
 */
public class HighlightClickEvent extends HighlightEvent {

  /**
   * Creates a new {@link HighlightClickEvent}.
   * 
   * @param highlight the {@link Highlight} that received the event
   * @param domEvent the underlying DOM {@link Event} in the richTextArea DOM
   */
  HighlightClickEvent(Highlight highlight, Event domEvent) {
    super(highlight, domEvent);
  }
}