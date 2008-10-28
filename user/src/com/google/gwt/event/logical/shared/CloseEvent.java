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
 * Represents a close event.
 * 
 * @param <T> the type being closed
 */
public class CloseEvent<T> extends AbstractEvent<CloseHandler<T>> {

  /**
   * Handler type.
   */
  private static Type<CloseHandler<?>> TYPE;

  /**
   * Fires a close event on all registered handlers in the handler manager.
   * 
   * @param <T> the target type
   * @param <S> The event source.
   * @param source the source of the handlers. Must have close handlers and a
   *          handler manager.
   * @param target the target
   * @param autoClosed was the target closed automatically
   */
  public static <T, S extends HasCloseHandlers<T> & HasHandlers> void fire(
      S source, T target, boolean autoClosed) {
    if (TYPE != null) {
      HandlerManager handlers = source.getHandlers();
      if (handlers != null) {
        CloseEvent<T> event = new CloseEvent<T>();
        event.autoClosed = autoClosed;
        event.setTarget(target);
        handlers.fireEvent(event);
      }
    }
  }

  /**
   * Fires a close event on all registered handlers in the handler manager.
   * 
   * @param <T> the target type
   * @param <S> The event source.
   * @param source the source of the handlers. Must have close handlers and a
   *          handler manager.
   * @param target the target
   */
  public static <T, S extends HasCloseHandlers<T> & HasHandlers> void fire(
      S source, T target) {
    fire(source, target, false);
  }

  /**
   * Gets the abstract type associated with this event.
   * 
   * @return returns the handler type
   */
  public static Type<CloseHandler<?>> getType() {
    if (TYPE == null) {
      TYPE = new Type<CloseHandler<?>>();
    }
    return TYPE;
  }

  private T target;

  private boolean autoClosed;

  /**
   * Constructor. Should only be used by subclasses, almost always for testing.
   */
  protected CloseEvent() {
  }

  /**
   * Gets the target.
   * 
   * @return the target
   */
  public T getTarget() {
    return target;
  }

  /**
   * Was the target automatically closed?
   * 
   * @return auto closed
   */
  public boolean isAutoClosed() {
    return autoClosed;
  }

  // @param autoClosed was the close event triggered automatically
  @Override
  protected void dispatch(CloseHandler<T> handler) {
    handler.onClose(this);
  }

  // Because of type erasure, our static type is
  // wild carded, yet the "real" type should use our I param.
  @SuppressWarnings("unchecked")
  @Override
  protected final Type<CloseHandler<T>> getAssociatedType() {
    return (Type) TYPE;
  }

  /**
   * Was the target automatically closed? 
   * 
   * @param autoClosed autoClosed
   */
  protected final void setAutoClosed(boolean autoClosed) {
    this.autoClosed = autoClosed;
  }

  /**
   * Sets the target.
   * 
   * @param target the target
   */
  protected final void setTarget(T target) {
    this.target = target;
  }
}
