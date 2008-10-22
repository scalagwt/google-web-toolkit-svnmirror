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
 * Represents a close event.
 * 
 * @param <TargetType> the target closed
 */
public class CloseEvent<TargetType> extends AbstractEvent {
  /**
   * Event type for Close.
   */
  public static final Type<CloseEvent, CloseHandler> TYPE = new Type<CloseEvent, CloseHandler>() {
    @Override
    protected void fire(CloseHandler handler, CloseEvent event) {
      handler.onClose(event);
    }
  };

  private boolean autoClosed = false;

  private TargetType target;

  /**
   * Constructs a {@link CloseEvent} event.
   * 
   * @param target the target being closed
   */
  public CloseEvent(TargetType target) {
    this.target = target;
  }

  /**
   * Constructs a {@link CloseEvent} event.
   * 
   * @param target the target being closed
   * @param autoClosed was the target auto closed
   */
  public CloseEvent(TargetType target, boolean autoClosed) {
    this.target = target;
    this.autoClosed = autoClosed;
  }

  /**
   * Gets the given target.
   * 
   * @return gets the target type
   */
  public TargetType getTarget() {
    return target;
  }

  /**
   * Many widgets with close events have the option of closing them
   * automatically. Auto closed should return true if that happened.
   * 
   * @return was the target automatically closed
   */
  public boolean isAutoClosed() {
    return autoClosed;
  }

  @Override
  protected Type getType() {
    return TYPE;
  }
}
