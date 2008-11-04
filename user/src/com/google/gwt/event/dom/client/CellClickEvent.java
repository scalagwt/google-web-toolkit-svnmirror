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
package com.google.gwt.event.dom.client;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Event;

/**
 * Represents a native cell click event.
 */
public final class CellClickEvent extends DomEvent<CellClickHandler> {

  /**
   * Event type for cell click events. Represents the meta-data associated with
   * this event.
   */
  private static Type<CellClickHandler> HOOK;

  /**
   * Fires the cell click event.
   * 
   * @param handlers to fire the click event to
   * @param nativeEvent native event
   * @param rowIndex row index
   * @param cellIndex cell index
   */
  public static void fire(HandlerManager handlers, Event nativeEvent,
      int rowIndex, int cellIndex) {
    final CellClickEvent event = new CellClickEvent();
    event.setNativeEvent(nativeEvent);
    event.rowIndex = rowIndex;
    event.cellIndex = cellIndex;
    handlers.fireEvent(event);
  }

  /**
   * Ensures the existence of the handler hook, then returns it.
   * 
   * @return the handler hook
   */
  public static Type<CellClickHandler> getType() {
    if (HOOK == null) {
      HOOK = new Type<CellClickHandler>(Event.ONCLICK);
    }
    return HOOK;
  }

  private int rowIndex;

  private int cellIndex;

  /**
   * Gets the cell index.
   * 
   * @return the cell index
   */
  public int getCellIndex() {
    return cellIndex;
  }

  /**
   * Gets the row index.
   * 
   * @return the row index
   */
  public int getRowIndex() {
    return rowIndex;
  }

  @Override
  protected void dispatch(CellClickHandler handler) {
    handler.onCellClick(this);
  }

  @Override
  protected Type<CellClickHandler> getAssociatedType() {
    return HOOK;
  }

}
