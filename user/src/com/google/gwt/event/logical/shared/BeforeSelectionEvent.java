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

import com.google.gwt.event.shared.AbstractEvent;

/**
 * Fired before an event source has selected a new value.
 * 
 * @param <Value> the type of value the widget has selected
 */
public class BeforeSelectionEvent<Value> extends AbstractEvent {

  /**
   * The event type.
   */
  public static final Type<BeforeSelectionEvent, BeforeSelectionHandler> TYPE = new Type<BeforeSelectionEvent, BeforeSelectionHandler>() {
    @Override
    protected void fire(BeforeSelectionHandler handler,
        BeforeSelectionEvent event) {
      handler.onBeforeSelection(event);
    }
  };

  private Value oldValue;
  private Value newValue;
  private boolean canceled = false;

  /**
   * Constructor.
   * 
   * @param oldValue the old value
   * @param newValue the new value
   */

  public BeforeSelectionEvent(Value oldValue, Value newValue) {
    this.oldValue = oldValue;
    this.newValue = newValue;
  }

  /**
   * Cancel the selection. Firing this will prevent a subsequent
   * {@link SelectionEvent} from being fired.
   */
  public void cancel() {
    this.canceled = true;
  }

  /**
   * Returns the new value.
   * 
   * @return the new value
   */
  public Value getNewValue() {
    assertLive();
    return newValue;
  }

  /**
   * Returns the old value.
   * 
   * @return the old value
   */
  public Value getOldValue() {
    assertLive();
    return oldValue;
  }

  /**
   * Check to see if this event has been canceled. If canceled, the subsequent
   * {@link SelectionEvent} will not fire.
   * 
   * @return true if the event has been canceled.
   */
  public boolean isCancelled() {
    return canceled;
  }

  @Override
  public String toDebugString() {
    assertLive();
    return super.toDebugString() + " old = " + oldValue + " new =" + newValue;
  }

  @Override
  protected Type getType() {
    return TYPE;
  }
}
