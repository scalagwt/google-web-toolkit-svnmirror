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

import com.google.gwt.user.client.Event;

/**
 * Represents a native cell click event.
 */
public class CellClickEvent extends ClickEvent {

  /**
   * Event type for cell click events. Represents the meta-data associated with
   * this event.
   */
  public static final Type<CellClickEvent, CellClickHandler> TYPE = new Type<CellClickEvent, CellClickHandler>(
      Event.ONCLICK) {
    @Override
    public void fire(CellClickHandler handler, CellClickEvent event) {
      handler.onCellClick(event);
    }
  };

  private int rowIndex;
  private int cellIndex;

  /**
   * Constructor.
   * 
   * @param nativeEvent native event
   * @param rowIndex row index
   * @param cellIndex cell index
   */
  public CellClickEvent(Event nativeEvent, int rowIndex, int cellIndex) {
    setNativeEvent(nativeEvent);
    this.rowIndex = rowIndex;
    this.cellIndex = cellIndex;
  }

  public int getCellIndex() {
    return cellIndex;
  }

  public int getRowIndex() {
    return rowIndex;
  }

  @Override
  protected Type getType() {
    return TYPE;
  }

}
