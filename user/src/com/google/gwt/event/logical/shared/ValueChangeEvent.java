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
import com.google.gwt.event.shared.HandlerManager;

/**
 * Represents a value change event.
 * 
 * @param <I> the value about to be changed
 */
public class ValueChangeEvent<I> extends AbstractEvent<ValueChangeHandler<I>> {

  /**
   * Handler type.
   */
  private static Type<ValueChangeHandler<?>> TYPE;

  /**
   * Fires a value change event on all registered handlers in the handler
   * manager.
   * 
   * @param <I> the old value type
   * @param <S> The event source.
   * @param source the source of the handlers. Must have value change handlers
   *          and a handler manager.
   * @param oldValue the old value
   * @param newValue the new value
   */
  public static <I, S extends HasValueChangeHandlers<I> & HasHandlers> void fire(
      S source, I oldValue, I newValue) {
    if (TYPE != null) {
      HandlerManager handlers = source.getHandlers();
      if (handlers != null) {
        ValueChangeEvent<I> event = new ValueChangeEvent<I>();
        event.setOldValue(oldValue);
        event.setNewValue(newValue);
        handlers.fireEvent(event);
      }
    }
  }

  /**
   * Gets the abstract type associated with this event.
   * 
   * @return returns the handler type
   */
  public static Type<ValueChangeHandler<?>> getType() {
    if (TYPE == null) {
      TYPE = new Type<ValueChangeHandler<?>>();
    }
    return TYPE;
  }

  private I oldValue;

  private I newValue;

  /**
   * Constructor. Should only be used by subclasses, almost always for testing.
   */
  protected ValueChangeEvent() {
  }

  /**
   * Gets the new value.
   * 
   * @return the new value
   */
  public I getNewValue() {
    return newValue;
  }

  /**
   * Gets the old value.
   * 
   * @return the old value
   */
  public I getOldValue() {
    return oldValue;
  }

  /**
   * Sets the new value.
   * 
   * @param newValue the new value
   */
  public void setNewValue(I newValue) {
    this.newValue = newValue;
  }

  @Override
  protected void dispatch(ValueChangeHandler<I> handler) {
    handler.onValueChange(this);
  }

  // Because of type erasure, our static type is
  // wild carded, yet the "real" type should use our I param.
  @SuppressWarnings("unchecked")
  @Override
  protected Type<ValueChangeHandler<I>> getAssociatedType() {
    return (Type) TYPE;
  }

  /**
   * Sets the old value.
   * 
   * @param oldValue the old value
   */
  protected final void setOldValue(I oldValue) {
    this.oldValue = oldValue;
  }
}
