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

import com.google.gwt.bikeshed.cells.client.DateCell;
import com.google.gwt.bikeshed.cells.client.EditTextCell;
import com.google.gwt.bikeshed.cells.client.FieldUpdater;
import com.google.gwt.bikeshed.cells.client.TextCell;
import com.google.gwt.bikeshed.list.client.CellTable;
import com.google.gwt.bikeshed.list.client.Column;
import com.google.gwt.bikeshed.list.shared.ListViewAdapter;
import com.google.gwt.core.client.GWT;
import com.google.gwt.sample.expenses.gwt.request.ReportRecord;
import com.google.gwt.sample.expenses.gwt.request.ReportRecordChanged;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.TakesValueList;
import com.google.gwt.user.client.ui.Widget;

import java.util.Date;
import java.util.List;

/**
 * UI shell for expenses sample app. A horrible clump of stuff that should be
 * refactored into proper MVP pieces.
 */
public class ExpensesShell extends Composite implements
    TakesValueList<ReportRecord>, ReportRecordChanged.Handler {
  interface Listener {
    void setPurpose(ReportRecord report, String purpose);
  }

  interface ShellUiBinder extends UiBinder<Widget, ExpensesShell> {
  }

  private static ShellUiBinder uiBinder = GWT.create(ShellUiBinder.class);

  @UiField
  ExpenseDetails expenseDetails;
  @UiField
  ExpenseList expenseList;
  @UiField
  DockLayoutPanel northPanel;
  @UiField
  SplitLayoutPanel splitLayout;

  // TODO(jlabanca): Remove this when the app is done.
  CellTable<ReportRecord> table;

  // TODO(jlabanca): Remove this when the app is done.
  ListBox users = new ListBox();

  private Column<ReportRecord, Date> createdCol = new Column<ReportRecord, Date>(
      new DateCell()) {
    @Override
    public Date getValue(ReportRecord object) {
      return object.getCreated();
    }
  };
  private Listener listener;
  private final ListViewAdapter<ReportRecord> adapter;

  private Column<ReportRecord, String> purposeCol = new Column<ReportRecord, String>(
      new EditTextCell()) {
    @Override
    public String getValue(ReportRecord object) {
      return object.getPurpose();
    }
  };

  private Column<ReportRecord, String> statusCol = new Column<ReportRecord, String>(
      TextCell.getInstance()) {
    @Override
    public String getValue(ReportRecord object) {
      return "...";
    }
  };

  private List<ReportRecord> values;

  public ExpensesShell() {
    adapter = new ListViewAdapter<ReportRecord>();
    table = createTable();
    initWidget(uiBinder.createAndBindUi(this));
    splitLayout.setWidgetMinSize(northPanel, 150);

    table.addColumn(createdCol, "Created");
    table.addColumn(statusCol, "Status (tbd)");
    table.addColumn(purposeCol, "Purpose");

    purposeCol.setFieldUpdater(new FieldUpdater<ReportRecord, String>() {
      public void update(int index, ReportRecord object, String value) {
        adapter.getList().set(index, object);
        listener.setPurpose(object, value);
      }
    });
  }

  public ExpenseDetails getExpenseDetails() {
    return expenseDetails;
  }

  public ExpenseList getExpenseList() {
    return expenseList;
  }

  public List<ReportRecord> getValues() {
    return values;
  }

  public void onReportChanged(ReportRecordChanged event) {
    refresh();
  }

  public void setListener(Listener listener) {
    this.listener = listener;
  }

  public void setValueList(List<ReportRecord> newValues) {
    this.values = newValues;
    refresh();
  }

  @UiFactory
  CellTable<ReportRecord> createTable() {
    CellTable<ReportRecord> view = new CellTable<ReportRecord>(10);
    adapter.addView(view);
    return view;
  }

  private void refresh() {
    adapter.setList(values);
  }
}
