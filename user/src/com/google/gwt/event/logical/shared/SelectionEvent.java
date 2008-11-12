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

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;

/**
 * Represents a selection event.
 * 
 * @param <I> the type being selected
 */
public class SelectionEvent<I> extends GwtEvent<SelectionHandler<I>> {

  /**
   * Handler type.
   */
  private static Type<SelectionHandler<?>> TYPE;

  /**
   * Fires a selection event on all registered handlers in the handler manager.
   * 
   * @param <I> the selected item type
   * @param <S> The event source
   * @param source the source of the handlers. Must have selection handlers and
   *          a handler manager.
   * @param selectedItem the selected item
   */
  public static <I, S extends HasSelectionHandlers<I> & HasHandlers> void fire(
      S source, I selectedItem) {
    if (TYPE != null) {
      HandlerManager handlers = source.getHandlers();
      if (handlers != null) {
        SelectionEvent<I> event = new SelectionEvent<I>();
        event.setSelectedItem(selectedItem);
        handlers.fireEvent(event);
      }
    }
  }

  /**
   * Gets the abstract type associated with this event.
   * 
   * @return returns the handler type
   */
  public static Type<SelectionHandler<?>> getType() {
    if (TYPE == null) {
      TYPE = new Type<SelectionHandler<?>>();
    }
    return TYPE;
  }

  private I selectedItem;

  /**
   * Constructor. Should only be used by subclasses, almost always for testing.
   */
  protected SelectionEvent() {
  }

  /**
   * Gets the selected item.
   * 
   * @return the selected item
   */
  public I getSelectedItem() {
    return selectedItem;
  }

  @Override
  protected void dispatch(SelectionHandler<I> handler) {
    handler.onSelection(this);
  }

  // Because of type erasure, our static type is
  // wild carded, yet the "real" type should use our I param.
  @SuppressWarnings("unchecked")
  @Override
  protected Type<SelectionHandler<I>> getAssociatedType() {
    return (Type) TYPE;
  }

  /**
   * Sets the selected item.
   * 
   * @param selectedItem the selected item
   */
  protected final void setSelectedItem(I selectedItem) {
    this.selectedItem = selectedItem;
  }
}
