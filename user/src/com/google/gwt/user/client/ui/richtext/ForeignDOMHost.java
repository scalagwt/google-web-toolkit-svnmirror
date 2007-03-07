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

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;

/**
 * Interface for classes to implement to handle raw events from a foreign DOM.
 */
interface ForeignDOMHost {
  /**
   * An raw event method similar to
   * {@link com.google.gwt.user.client.ui.Widget#onBrowserEvent(Event)}. The
   * events may be preventDefault'ed, which will prevent the firing of the
   * listeners, although this must be done with
   * {@link DOMUtil#eventPreventDefault(Event)} to correctly mark the event.
   * 
   * @param sender the element sending the event
   * @param event the DOM event being sent
   */
  void onForeignDOMEvent(Element sender, Event event);
}
