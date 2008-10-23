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
 * Represents a selection event.
 * 
 * @param <SelectedItemType> the type being selected
 */
public class SelectionEvent<SelectedItemType> extends AbstractEvent {

  /**
   * Event type.
   */
  public static final Type<SelectionEvent, SelectionHandler> TYPE = new Type<SelectionEvent, SelectionHandler>() {
    @Override
    protected void fire(SelectionHandler handler, SelectionEvent event) {
      handler.onSelection(event);
    }
  };
  private SelectedItemType item;

  /**
   * Constructs a SelectionEvent event.
   * 
   * @param item the selected item
   */
  public SelectionEvent(SelectedItemType item) {
    this.item = item;
  }

  /**
   * Gets the selected item.
   * 
   * @return the selected item
   */
  public SelectedItemType getSelectedItem() {
    return item;
  }

  @Override
  public String toDebugString() {
    return "selected: " + item;
  }

  @Override
  protected Type getType() {
    return TYPE;
  }
}
