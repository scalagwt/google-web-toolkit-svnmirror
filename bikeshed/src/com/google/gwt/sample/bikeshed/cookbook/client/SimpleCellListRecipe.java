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
package com.google.gwt.sample.bikeshed.cookbook.client;

import com.google.gwt.bikeshed.cells.client.TextCell;
import com.google.gwt.bikeshed.list.client.SimpleCellList;
import com.google.gwt.bikeshed.list.shared.ListViewAdapter;
import com.google.gwt.bikeshed.list.shared.SelectionModel;
import com.google.gwt.bikeshed.list.shared.SingleSelectionModel;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import java.util.List;

/**
 * SimpleCellList Recipe.
 */
public class SimpleCellListRecipe extends Recipe {

  public SimpleCellListRecipe() {
    super("Simple Cell List");
  }

  @Override
  protected Widget createWidget() {
    ListViewAdapter<String> adapter = new ListViewAdapter<String>();
    final List<String> list = adapter.getList();
    for (int i = 0; i < 40; i++) {
      list.add("" + ((i + 10) * 1000));
    }

    final SimpleCellList<String> simpleCellList = new SimpleCellList<String>(
        TextCell.getInstance());
    simpleCellList.setPageSize(10);
    final SelectionModel<String> selectionModel = new SingleSelectionModel<String>();
    simpleCellList.setSelectionModel(selectionModel);
    adapter.addView(simpleCellList);

    new Timer() {
      int index = 0;

      @Override
      public void run() {
        if (simpleCellList.isAttached()) {
          incrementValue(index);
          incrementValue(index + 15);
          index = (index + 1) % 10;
        }
        schedule(100);
      }

      private void incrementValue(int i) {
        // Set the value at index.
        String oldValue = list.get(i);
        int number = Integer.parseInt(oldValue);
        String newValue = "" + (number + 1);
        if (selectionModel.isSelected(oldValue)) {
          // Move the selection with the value.
          // TODO(jlabanca): Use a DTO with a unique ID instead.
          selectionModel.setSelected(newValue, true);
        }
        list.set(i, newValue);
      }
    }.schedule(100);

    // Add a Pager to control the table.
    SimplePager<String> pager = new SimplePager<String>(simpleCellList);

    FlowPanel fp = new FlowPanel();
    fp.add(simpleCellList);
    fp.add(pager);
    return fp;
  }
}
