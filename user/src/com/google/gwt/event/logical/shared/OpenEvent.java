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
 * Represents a open event.
 * 
 * @param <TargetType> the type of the object being opened
 */
public class OpenEvent<TargetType> extends AbstractEvent {

  /**
   * Event type.
   */
  public static final Type<OpenEvent, OpenHandler> TYPE = new Type<OpenEvent, OpenHandler>() {
    @Override
    protected void fire(OpenHandler handler, OpenEvent event) {
      handler.onOpen(event);
    }
  };
  private TargetType target;

  /**
   * Constructs a OpenEvent event.
   * 
   * @param target the opened target
   */
  public OpenEvent(TargetType target) {
    this.target = target;
  }

  /**
   * Gets the target being opened
   * 
   * @return the opened target
   */
  public TargetType getTarget() {
    return target;
  }

  @Override
  protected Type getType() {
    return TYPE;
  }
}
