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
 * Fired when the widget is resized.
 */
public class ResizeEvent extends AbstractEvent {

  /**
   * The event type.
   */
  public static final Type<ResizeEvent, ResizeHandler> TYPE = new Type<ResizeEvent, ResizeHandler>() {

    @Override
    protected void fire(ResizeHandler handler, ResizeEvent event) {
      handler.onResize(event);
    }
  };

  private int width;
  private int height;

  /**
   * Construct a new {@link ResizeEvent}.
   * 
   * @param width the new width
   * @param height the new height
   */
  public ResizeEvent(int width, int height) {
    this.width = width;
    this.height = height;
  }

  /**
   * Returns the new height.
   * 
   * @return the new height
   */
  public int getHeight() {
    return height;
  }
  
  /**
   * Returns the new width.
   * 
   * @return the new width
   */
  public int getWidth() {
    return width;
  }

  @Override
  public String toDebugString() {
    assertLive();
    return super.toDebugString() + " width = " + width + " height =" + height;
  }

  @Override
  protected Type getType() {
    return TYPE;
  }
}
