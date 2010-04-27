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
package com.google.gwt.sample.expenses.gwt.customized;

import com.google.gwt.bikeshed.cells.client.DateCell;
import com.google.gwt.bikeshed.cells.client.TextCell;
import com.google.gwt.bikeshed.list.client.CellTable;
import com.google.gwt.bikeshed.list.client.Column;
import com.google.gwt.bikeshed.list.client.Header;
import com.google.gwt.bikeshed.list.shared.ListViewAdapter;
import com.google.gwt.bikeshed.list.shared.SingleSelectionModel;
import com.google.gwt.bikeshed.list.shared.SelectionModel.SelectionChangeEvent;
import com.google.gwt.bikeshed.list.shared.SelectionModel.SelectionChangeHandler;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.sample.expenses.gwt.request.ReportRecord;
import com.google.gwt.sample.expenses.gwt.request.ReportRecordChanged;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TakesValueList;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The list of expense reports on the left side of the app.
 */
public class ExpenseList extends Composite implements
    TakesValueList<ReportRecord>, ReportRecordChanged.Handler {

  private static final String TEXTBOX_DISABLED_COLOR = "#aaaaaa";
  private static final String TEXTBOX_DEFAULT_TEXT = "search";

  private static ExpenseListUiBinder uiBinder = GWT.create(ExpenseListUiBinder.class);

  /**
   * Custom listener for this widget.
   */
  interface Listener {

    /**
     * Called when the user enters a search value.
     * 
     * @param startWith the search string
     */
    void onSearch(String startWith);

    /**
     * Called whent he user selects a report.
     * 
     * @param report the selected report
     */
    void onReportSelected(ReportRecord report);
  }

  interface ExpenseListUiBinder extends UiBinder<Widget, ExpenseList> {
  }

  @UiField
  CellTable<ReportRecord> table;
  @UiField
  TextBox nameSearchBox;

  private Listener listener;

  /**
   * The adapter that provides reports.
   */
  private ListViewAdapter<ReportRecord> reports = new ListViewAdapter<ReportRecord>();

  /**
   * The timer used to delay searches until the user stops typing.
   */
  private Timer searchTimer = new Timer() {
    @Override
    public void run() {
      search();
    }
  };

  public ExpenseList() {
    initWidget(uiBinder.createAndBindUi(this));

    // Add the view to the adapter.
    reports.addView(table);

    // Setup the search box.
    nameSearchBox.setText(TEXTBOX_DEFAULT_TEXT);
    nameSearchBox.getElement().getStyle().setColor(TEXTBOX_DISABLED_COLOR);
    nameSearchBox.addKeyUpHandler(new KeyUpHandler() {
      public void onKeyUp(KeyUpEvent event) {
        searchTimer.schedule(500);
      }
    });
    nameSearchBox.addFocusHandler(new FocusHandler() {
      public void onFocus(FocusEvent event) {
        nameSearchBox.getElement().getStyle().clearColor();
        if (TEXTBOX_DEFAULT_TEXT.equals(nameSearchBox.getText())) {
          nameSearchBox.setText("");
        }
      }
    });
    nameSearchBox.addBlurHandler(new BlurHandler() {
      public void onBlur(BlurEvent event) {
        if ("".equals(nameSearchBox.getText())) {
          nameSearchBox.setText(TEXTBOX_DEFAULT_TEXT);
          nameSearchBox.getElement().getStyle().setColor(TEXTBOX_DISABLED_COLOR);
        }
      }
    });
  }

  public void onReportChanged(ReportRecordChanged event) {
    reports.refresh();
  }

  public void setListener(Listener listener) {
    this.listener = listener;
    search();
  }

  public void setValueList(List<ReportRecord> newValues) {
    // TODO(jlabanca): Handle search on the server.
    // Search through the values.
    String startsWith = nameSearchBox.getText().toLowerCase();
    if (TEXTBOX_DEFAULT_TEXT.equals(startsWith)) {
      startsWith = "";
    }
    List<ReportRecord> matched = new ArrayList<ReportRecord>();
    if (startsWith != null && startsWith.length() > 0) {
      for (ReportRecord record : newValues) {
        if (record.getPurpose().toLowerCase().startsWith(startsWith)) {
          matched.add(record);
        }
      }
    } else {
      matched.addAll(newValues);
    }

    reports.setList(matched);
  }

  @UiFactory
  CellTable<ReportRecord> createC() {
    CellTable<ReportRecord> view = new CellTable<ReportRecord>(25);

    // Add a selection model.
    final SingleSelectionModel<ReportRecord> selectionModel = new SingleSelectionModel<ReportRecord>();
    view.setSelectionModel(selectionModel);
    view.setSelectionEnabled(true);
    selectionModel.addSelectionChangeHandler(new SelectionChangeHandler() {
      public void onSelectionChange(SelectionChangeEvent event) {
        Object selected = selectionModel.getSelectedObject();
        if (selected != null && listener != null) {
          listener.onReportSelected((ReportRecord) selected);
        }
      }
    });

    // Purpose column.
    view.addColumn(new Column<ReportRecord, String>(TextCell.getInstance()) {
      @Override
      public String getValue(ReportRecord object) {
        return object.getPurpose();
      }
    }, new Header<String>(TextCell.getInstance()) {
      @Override
      public String getValue() {
        return "Name";
      }
    });

    // Date column.
    view.addColumn(new Column<ReportRecord, Date>(new DateCell()) {
      @Override
      public Date getValue(ReportRecord object) {
        return object.getCreated();
      }
    }, new Header<String>(TextCell.getInstance()) {
      @Override
      public String getValue() {
        return "Date";
      }
    });

    return view;
  }

  /**
   * Search based on the text.
   */
  private void search() {
    if (listener != null) {
      String startsWith = nameSearchBox.getText();
      if (TEXTBOX_DEFAULT_TEXT.equals(startsWith)) {
        startsWith = "";
      }
      listener.onSearch(startsWith);
    }
  }
}
