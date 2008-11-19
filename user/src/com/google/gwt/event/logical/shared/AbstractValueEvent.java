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
package com.google.gwt.event.logical.shared;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Represents an event class which target a single value.
 * 
 * @param <V> the value type
 * @param <H> the handler type
 */
public abstract class AbstractValueEvent<V, H extends EventHandler> extends
    GwtEvent<H> {
  private final V value;

  /**
   * Creates a new abstract value event.
   * 
   * @param value the value for this event
   */
  protected AbstractValueEvent(V value) {
    this.value = value;
  }

  /**
   * Gets the the value for this event.
   * 
   * @return the value for this event
   */
  public V getValue() {
    return value;
  }
}
