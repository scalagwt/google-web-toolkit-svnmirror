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
 * @param <ItemType> the type being selected
 */
public class BeforeSelectionEvent<ItemType> extends AbstractEvent {

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

  private ItemType item;
  private boolean canceled = false;

  /**
   * Construct a new {@link BeforeSelectionEvent}.
   * 
   * @param item the item being selected
   */

  public BeforeSelectionEvent(ItemType item) {
    this.item = item;
  }

  /**
   * Cancel the selection. Firing this will prevent a subsequent
   * {@link SelectionEvent} from being fired.
   */
  public void cancel() {
    this.canceled = true;
  }

  /**
   * Gets the item that is being selected.
   * 
   * @return the item being selected
   */
  public ItemType getItem() {
    return item;
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
    return "selecting: " + item;
  }

  @Override
  protected Type getType() {
    return TYPE;
  }
}
