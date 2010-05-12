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

import com.google.gwt.bikeshed.list.client.CellList;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.requestfactory.shared.Receiver;
import com.google.gwt.sample.expenses.gwt.request.ExpensesRequestFactory;
import com.google.gwt.sample.expenses.gwt.request.ReportRecord;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.valuestore.shared.Property;
import com.google.gwt.view.client.ListViewAdapter;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.gwt.view.client.SelectionModel.SelectionChangeEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * TODO
 */
public class MobileReportList extends Composite implements
    MobilePage, Receiver<List<ReportRecord>> {

  /**
   * TODO
   */
  public interface Listener {
    void onReportSelected(ReportRecord report);
  }

  private final CellList<ReportRecord> reportList;
  private final ListViewAdapter<ReportRecord> reportAdapter;
  private final SingleSelectionModel<ReportRecord> reportSelection;
  private final ExpensesRequestFactory requestFactory;

  public MobileReportList(final Listener listener,
      final ExpensesRequestFactory requestFactory) {
    this.requestFactory = requestFactory;
    reportAdapter = new ListViewAdapter<ReportRecord>();
    reportAdapter.setKeyProvider(Expenses.REPORT_RECORD_KEY_PROVIDER);

    reportList = new CellList<ReportRecord>(
        new AbstractCell<ReportRecord>() {
          @Override
          public void render(ReportRecord value, Object viewData,
              StringBuilder sb) {
            sb.append("<div onclick='' class='item'>" + value.getPurpose() + "</div>");
          }
        });

    reportSelection = new SingleSelectionModel<ReportRecord>();
    reportSelection.setKeyProvider(Expenses.REPORT_RECORD_KEY_PROVIDER);
    reportSelection.addSelectionChangeHandler(new SelectionModel.SelectionChangeHandler() {
      public void onSelectionChange(SelectionChangeEvent event) {
        listener.onReportSelected(reportSelection.getSelectedObject());
      }
    });

    reportList.setSelectionModel(reportSelection);
    reportAdapter.addView(reportList);

    initWidget(reportList);
    onRefresh(false);
  }

  public Widget asWidget() {
    return this;
  }

  public String getPageTitle() {
    return "Expense Reports";
  }

  public boolean needsAddButton() {
    return true;
  }

  public String needsCustomButton() {
    return null;
  }

  public boolean needsRefreshButton() {
    return true;
  }

  public void onAdd() {
    // TODO: add a new report
  }

  public void onCustom() {
  }

  public void onRefresh(boolean clear) {
    if (clear) {
      reportAdapter.getList().clear();
    }

    requestFactory.reportRequest().findReportEntriesBySearch(new Long(-1), "",
        "", ReportRecord.created.getName(), 0, 25).forProperties(
        getReportColumns()).to(this).fire();
  }

  public void onSuccess(List<ReportRecord> newValues) {
    reportAdapter.setList(newValues);
  }

  private Collection<Property<?>> getReportColumns() {
    List<Property<?>> columns = new ArrayList<Property<?>>();
    columns.add(ReportRecord.created);
    columns.add(ReportRecord.purpose);
    return columns;
  }
}
