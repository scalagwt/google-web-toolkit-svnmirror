/*
 * Copyright 2010 Google Inc.
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
package com.google.gwt.user.cellview.client;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;

/**
 * A table column header or footer.
 *
 * @param <H> the {#link Cell} type
 */
public abstract class Header<H> {

  private final Cell<H> cell;

  private ValueUpdater<H> updater;

  public Header(Cell<H> cell) {
    this.cell = cell;
  }

  public boolean dependsOnSelection() {
    return false;
  }

  public abstract H getValue();

  public void onBrowserEvent(Element elem, NativeEvent event) {
    cell.onBrowserEvent(elem, getValue(), null, event, updater);
  }

  public void render(StringBuilder sb) {
    cell.render(getValue(), null, sb);
  }

  public void setUpdater(ValueUpdater<H> updater) {
    this.updater = updater;
  }
}
