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

  private TargetType target;

  /**
   * Constructs a CloseEvent event.
   */
  public CloseEvent(TargetType target) {
    this.target = target;
  }

  /**
   * Gets the given target.
   */
  public TargetType getTarget() {
    return target;
  }

  @Override
  protected Type getType() {
    return TYPE;
  }
}
