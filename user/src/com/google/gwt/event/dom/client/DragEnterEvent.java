/*
 * Copyright 2011 Google Inc.
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
package com.google.gwt.event.dom.client;

/**
 * Represents a native drag enter event.
 */
public class DragEnterEvent extends DragDropEventBase<DragEnterHandler> {

  /**
   * Event type for drag enter events. Represents the meta-data associated
   * with this event.
   */
  private static final Type<DragEnterHandler> TYPE = new Type<DragEnterHandler>(
      "dragenter", new DragEnterEvent());

  /**
   * Gets the event type associated with drag enter events.
   * 
   * @return the handler type
   */
  public static Type<DragEnterHandler> getType() {
    return TYPE;
  }

  /**
   * Protected constructor, use
   * {@link DomEvent#fireNativeEvent(com.google.gwt.dom.client.NativeEvent, com.google.gwt.event.shared.HasHandlers)}
   * or
   * {@link DomEvent#fireNativeEvent(com.google.gwt.dom.client.NativeEvent, com.google.gwt.event.shared.HasHandlers, com.google.gwt.dom.client.Element)}
   * to fire drag enter events.
   */
  protected DragEnterEvent() {
  }

  @Override
  public final Type<DragEnterHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(DragEnterHandler handler) {
    handler.onDragEnter(this);
  }

}
