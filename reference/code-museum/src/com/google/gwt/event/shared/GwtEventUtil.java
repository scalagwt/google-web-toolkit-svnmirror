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

package com.google.gwt.event.shared;

import com.google.gwt.event.shared.GwtEvent.Type;

/**
 * Utility class to help with managing events.
 */
public class GwtEventUtil {

  /**
   * Fire the event on the given handler.
   * 
   * @param event the event to dispatch
   * @param handler the handler to dispatch it to
   * @param <H> the event's handler type
   * 
   */
  public static <H extends EventHandler> void dispatch(GwtEvent<H> event,
      H handler) {
    event.dispatch(handler);
  }

  /**
   * Gets the event's type.
   * 
   * @param <H> handler type
   * 
   * @param event the event
   * @return the associated type
   */
  public static <H extends EventHandler> Type<H> getType(GwtEvent<H> event) {
    return event.getAssociatedType();
  }

  private GwtEventUtil() {
    // Utility class, should not have instances.
  }
}
