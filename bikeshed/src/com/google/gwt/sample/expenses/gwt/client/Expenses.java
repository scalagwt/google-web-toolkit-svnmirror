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

import com.google.gwt.bikeshed.list.shared.ProvidesKey;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.sample.expenses.gwt.request.EmployeeRecord;
import com.google.gwt.sample.expenses.gwt.request.ExpenseRecord;
import com.google.gwt.sample.expenses.gwt.request.ExpenseRecordChanged;
import com.google.gwt.sample.expenses.gwt.request.ExpensesRequestFactory;
import com.google.gwt.sample.expenses.gwt.request.ReportRecord;
import com.google.gwt.sample.expenses.gwt.request.ReportRecordChanged;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.valuestore.shared.DeltaValueStore;
import com.google.gwt.valuestore.shared.Property;
import com.google.gwt.valuestore.shared.Record;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Entry point for the Expenses app.
 */
public class Expenses implements EntryPoint {

  /**
   * The key provider for {@link EmployeeRecord}s.
   */
  public static final ProvidesKey<EmployeeRecord> EMPLOYEE_RECORD_KEY_PROVIDER = new ProvidesKey<EmployeeRecord>() {
    public Object getKey(EmployeeRecord item) {
      return item == null ? null : item.getId();
    }
  };

  private ExpensesRequestFactory requestFactory;
  private String searchCategory;
  private EmployeeRecord searchEmployee;
  private String searchStartsWith;
  private ExpensesShell shell;

  public void onModuleLoad() {
    final HandlerManager eventBus = new HandlerManager(null);
    requestFactory = GWT.create(ExpensesRequestFactory.class);
    requestFactory.init(eventBus);

    RootLayoutPanel root = RootLayoutPanel.get();

    shell = new ExpensesShell();
    final ExpenseBrowser expenseBrowser = shell.getExpenseBrowser();
    final ExpenseList expenseList = shell.getExpenseList();
    final ExpenseDetails expenseDetails = shell.getExpenseDetails();

    root.add(shell);

    // Listen for requests from ExpenseBrowser.
    expenseBrowser.setListener(new ExpenseBrowser.Listener() {
      public void onBrowse(String category, EmployeeRecord employee) {
        if (category != null && !category.equals(searchCategory)) {
          // TODO(jlabanca): Limit employees using category.
          requestFactory.employeeRequest().findAllEmployees().forProperties(
              getEmployeeMenuProperties()).to(expenseBrowser).fire();
        }
        searchEmployee = employee;
        searchCategory = category;
        searchForReports();
      }
    });

    // Listen for requests from the ExpenseList.
    expenseList.setListener(new ExpenseList.Listener() {
      public void onReportSelected(ReportRecord report) {
        expenseDetails.setExpensesRequestFactory(requestFactory);
        expenseDetails.setReportRecord(report);

        requestFactory.expenseRequest().findExpensesByReport(
            report.getRef(Record.id)).forProperties(getExpenseColumns()).to(
            expenseDetails).fire();
      }

      public void onSearch(String startsWith) {
        searchStartsWith = startsWith;
        searchForReports();
      }
    });
    eventBus.addHandler(ReportRecordChanged.TYPE, expenseList);

    shell.setListener(new ExpensesShell.Listener() {
      public void setPurpose(ReportRecord report, String purpose) {
        DeltaValueStore deltaValueStore = requestFactory.getValueStore().spawnDeltaView();
        deltaValueStore.set(ReportRecord.purpose, report, purpose);
        requestFactory.syncRequest(deltaValueStore).fire();
      }
    });

    eventBus.addHandler(ExpenseRecordChanged.TYPE, expenseDetails);
    eventBus.addHandler(ReportRecordChanged.TYPE, shell);
  }

  private Collection<Property<?>> getEmployeeMenuProperties() {
    List<Property<?>> columns = new ArrayList<Property<?>>();
    columns.add(EmployeeRecord.displayName);
    columns.add(EmployeeRecord.userName);
    return columns;
  }

  private Collection<Property<?>> getExpenseColumns() {
    List<Property<?>> columns = new ArrayList<Property<?>>();
    columns.add(ExpenseRecord.amount);
    columns.add(ExpenseRecord.approval);
    columns.add(ExpenseRecord.category);
    columns.add(ExpenseRecord.date);
    columns.add(ExpenseRecord.description);
    columns.add(ExpenseRecord.reasonDenied);
    return columns;
  }

  private Collection<Property<?>> getReportColumns() {
    List<Property<?>> columns = new ArrayList<Property<?>>();
    columns.add(ReportRecord.created);
    columns.add(ReportRecord.purpose);
    return columns;
  }

  /**
   * Search for reports based on the search criteria in the browser and search
   * box.
   */
  private void searchForReports() {
    // TODO(jlabanca): Limit search using search terms.
    requestFactory.reportRequest().findAllReports().forProperties(
        getReportColumns()).to(shell.getExpenseList()).fire();
  }
}
