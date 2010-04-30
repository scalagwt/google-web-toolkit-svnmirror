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
package com.google.gwt.sample.expenses.gwt.client;

import com.google.gwt.bikeshed.cells.client.Cell;
import com.google.gwt.bikeshed.cells.client.DateCell;
import com.google.gwt.bikeshed.cells.client.TextCell;
import com.google.gwt.bikeshed.cells.client.ValueUpdater;
import com.google.gwt.bikeshed.list.client.CellTable;
import com.google.gwt.bikeshed.list.client.SimplePager;
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
import com.google.gwt.requestfactory.shared.Receiver;
import com.google.gwt.sample.expenses.gwt.request.ReportRecord;
import com.google.gwt.sample.expenses.gwt.request.ReportRecordChanged;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * The list of expense reports on the left side of the app.
 */
public class ExpenseList extends Composite implements
    Receiver<List<ReportRecord>>, ReportRecordChanged.Handler {

  interface ExpenseListUiBinder extends UiBinder<Widget, ExpenseList> {
  }
  /**
   * Custom listener for this widget.
   */
  interface Listener {

    /**
     * Called when the user selects a report.
     * 
     * @param report the selected report
     */
    void onReportSelected(ReportRecord report);

    /**
     * Called when the user enters a search value.
     * 
     * @param startsWith the search string
     */
    void onSearch(String startsWith);
  }

  private static final String TEXTBOX_DEFAULT_TEXT = "search";

  private static final String TEXTBOX_DISABLED_COLOR = "#aaaaaa";

  private static ExpenseListUiBinder uiBinder = GWT.create(ExpenseListUiBinder.class);

  @UiField
  SimplePager<ReportRecord> pager;
  @UiField
  TextBox searchBox;

  /**
   * The main table. We provide this in the constructor before calling
   * {@link UiBinder#createAndBindUi(Object)} because the pager depends on it.
   */
  @UiField(provided = true)
  CellTable<ReportRecord> table;

  private List<SortableHeader> allHeaders = new ArrayList<SortableHeader>();

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

  private SortableColumn<ReportRecord, String> purposeColumn;

  public ExpenseList() {
    createTable();
    initWidget(uiBinder.createAndBindUi(this));

    // Add the view to the adapter.
    reports.addView(table);

    // Setup the search box.
    searchBox.setText(TEXTBOX_DEFAULT_TEXT);
    searchBox.getElement().getStyle().setColor(TEXTBOX_DISABLED_COLOR);
    searchBox.addKeyUpHandler(new KeyUpHandler() {
      public void onKeyUp(KeyUpEvent event) {
        searchTimer.schedule(500);
      }
    });
    searchBox.addFocusHandler(new FocusHandler() {
      public void onFocus(FocusEvent event) {
        searchBox.getElement().getStyle().clearColor();
        if (TEXTBOX_DEFAULT_TEXT.equals(searchBox.getText())) {
          searchBox.setText("");
        }
      }
    });
    searchBox.addBlurHandler(new BlurHandler() {
      public void onBlur(BlurEvent event) {
        if ("".equals(searchBox.getText())) {
          searchBox.setText(TEXTBOX_DEFAULT_TEXT);
          searchBox.getElement().getStyle().setColor(TEXTBOX_DISABLED_COLOR);
        }
      }
    });
  }

  public void onReportChanged(ReportRecordChanged event) {
    reports.refresh();
  }

  public void onSuccess(List<ReportRecord> newValues) {
    // TODO(jlabanca): Handle search on the server.
    // Search through the values.
    String startsWith = searchBox.getText().toLowerCase();
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

    allHeaders.get(0).setSorted(true);
    allHeaders.get(0).setReverseSort(false);
    table.refreshHeaders();
    sortReports(purposeColumn.getComparator(false));
  }

  public void setListener(Listener listener) {
    this.listener = listener;
    search();
  }

  @UiFactory
  SimplePager<ReportRecord> createPager() {
    SimplePager<ReportRecord> p = new SimplePager<ReportRecord>(table);
    p.setRangeLimited(true);
    return p;
  }

  private <C extends Comparable<C>> SortableColumn<ReportRecord, C> addColumn(
      final CellTable<ReportRecord> table, final String text,
      final Cell<C> cell, final GetValue<ReportRecord, C> getter) {
    final SortableColumn<ReportRecord, C> column = new SortableColumn<ReportRecord, C>(
        cell) {
      @Override
      public C getValue(ReportRecord object) {
        return getter.getValue(object);
      }
    };
    final SortableHeader header = new SortableHeader(text);
    allHeaders.add(header);

    header.setUpdater(new ValueUpdater<String>() {
      public void update(String value) {
        header.setSorted(true);
        header.toggleReverseSort();

        for (SortableHeader otherHeader : allHeaders) {
          if (otherHeader != header) {
            otherHeader.setSorted(false);
            otherHeader.setReverseSort(true);
          }
        }
        sortReports(column.getComparator(header.getReverseSort()));
        table.refreshHeaders();
      }
    });
    table.addColumn(column, header);
    return column;
  }

  /**
   * Create the {@link CellTable}.
   */
  private void createTable() {
    table = new CellTable<ReportRecord>(8);

    // Add a selection model.
    final SingleSelectionModel<ReportRecord> selectionModel = new SingleSelectionModel<ReportRecord>();
    table.setSelectionModel(selectionModel);
    table.setSelectionEnabled(true);
    selectionModel.addSelectionChangeHandler(new SelectionChangeHandler() {
      public void onSelectionChange(SelectionChangeEvent event) {
        Object selected = selectionModel.getSelectedObject();
        if (selected != null && listener != null) {
          listener.onReportSelected((ReportRecord) selected);
        }
      }
    });

    // Purpose column.
    purposeColumn = addColumn(table, "Purpose", TextCell.getInstance(),
        new GetValue<ReportRecord, String>() {
          public String getValue(ReportRecord object) {
            return object.getPurpose();
          }
        });

    // Created column.
    addColumn(table, "Created", new DateCell(),
        new GetValue<ReportRecord, Date>() {
          public Date getValue(ReportRecord object) {
            return object.getCreated();
          }
        });
  }

  /**
   * Search based on the text.
   */
  private void search() {
    if (listener != null) {
      String startsWith = searchBox.getText();
      if (TEXTBOX_DEFAULT_TEXT.equals(startsWith)) {
        startsWith = "";
      }
      listener.onSearch(startsWith);
    }
  }

  private void sortReports(final Comparator<ReportRecord> comparator) {
    Collections.sort(reports.getList(), comparator);
  }
}
