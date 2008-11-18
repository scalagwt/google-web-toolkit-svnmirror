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

package com.google.gwt.user.datepicker.client;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;

/**
 * A simple {@link MonthSelector} used for the default date picker. Not part of
 * the public API as we wish to evolve it freely over time. Please copy though,
 * as we like it getting used.
 */

public final class DefaultMonthSelector extends MonthSelector {

  private PushButton backwards;
  private PushButton forwards;
  private Grid grid;

  /**
   * Constructor.
   */
  public DefaultMonthSelector() {
  }
 
  @Override
  protected void refresh() {
    String formattedMonth = getModel().formatCurrentMonth();
    grid.setText(0, 1, formattedMonth);
  }

  @Override
  protected void setup() {
    // Set up backwards.
    backwards = new PushButton();
    backwards.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        addMonths(-1);
      }
    });

    backwards.getUpFace().setHTML("&laquo;");
    backwards.setStyleName(css().previousButton());

    forwards = new PushButton();
    forwards.getUpFace().setHTML("&raquo;");
    forwards.setStyleName(css().nextButton());
    forwards.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        addMonths(+1);
      }
    });

    // Set up grid.
    grid = new Grid(1, 3);
    grid.setWidget(0, 0, backwards);
    grid.setWidget(0, 2, forwards);

    CellFormatter formatter = grid.getCellFormatter();
    formatter.setStyleName(0, 1, css().month());
    formatter.setWidth(0, 0, "20%");
    formatter.setWidth(0, 2, "20%");
    grid.setStyleName(css().days());
    initWidget(grid);
  }

}
