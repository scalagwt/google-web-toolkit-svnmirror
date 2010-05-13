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

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.requestfactory.shared.Receiver;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.sample.bikeshed.style.client.Styles;
import com.google.gwt.sample.expenses.gwt.request.ExpenseRecord;
import com.google.gwt.sample.expenses.gwt.request.ExpensesRequestFactory;
import com.google.gwt.sample.expenses.gwt.request.ReportRecord;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.valuestore.shared.Property;
import com.google.gwt.view.client.AsyncListViewAdapter;
import com.google.gwt.view.client.ListView;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.gwt.view.client.SelectionModel.SelectionChangeEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * TODO: doc.
 */
public class MobileExpenseList extends Composite implements MobilePage {

  /**
   * String indicating approval.
   */
  private static final String APPROVED = "Approved";

  /**
   * String indicating denial.
   */
  private static final String DENIED = "Denied";

  /**
   * The auto refresh interval in milliseconds.
   */
  private static final int REFRESH_INTERVAL = 5000;

  /**
   * TODO: doc.
   */
  public interface Listener {
    void onExpenseSelected(ExpenseRecord expense);

    void onCreateExpense(String reportId);
  }

  /**
   * The cell used to render {@link ExpenseRecord}s.
   */
  private class ExpenseCell extends AbstractCell<ExpenseRecord> {

    private final String approvedHtml;
    private final String blankHtml;
    private final String deniedHtml;

    public ExpenseCell() {
      Styles.Resources res = Styles.resources();
      approvedHtml = getImageHtml(res.approvedIcon());
      blankHtml = getImageHtml(res.blankIcon());
      deniedHtml = getImageHtml(res.deniedIcon());
    }

    @Override
    public void render(ExpenseRecord value, Object viewData, StringBuilder sb) {
      sb.append("<div onclick='' class='item'>");
      String approval = value.getApproval();
      if (APPROVED.equals(approval)) {
        sb.append(approvedHtml);
      } else if (DENIED.equals(approval)) {
        sb.append(deniedHtml);
      } else {
        sb.append(blankHtml);
      }
      sb.append(value.getDescription());
      sb.append(" (");
      sb.append(ExpensesMobile.formatCurrency(value.getAmount()));
      sb.append(")</div>");
    }

    private String getImageHtml(ImageResource res) {
      AbstractImagePrototype proto = AbstractImagePrototype.create(res);
      return proto.getHTML();
    }
  }

  private final ExpensesRequestFactory requestFactory;
  private final CellList<ExpenseRecord> expenseList;
  private final AsyncListViewAdapter<ExpenseRecord> expenseAdapter;
  private final SingleSelectionModel<ExpenseRecord> expenseSelection;

  /**
   * The set of Expense keys that we already know are denied. When a new key is
   * added, we compare it to the list of known keys to determine if it is new.
   */
  private Set<Object> knownDeniedKeys = null;

  /**
   * The receiver for the last request.
   */
  private Receiver<List<ExpenseRecord>> lastReceiver;

  private ReportRecord report;
  private final Listener listener;

  /**
   * The {@link Timer} used to periodically refresh the table.
   */
  private final Timer refreshTimer = new Timer() {
    @Override
    public void run() {
      requestExpenses();
    }
  };

  public MobileExpenseList(final Listener listener,
      final ExpensesRequestFactory requestFactory) {
    this.listener = listener;
    this.requestFactory = requestFactory;
    expenseAdapter = new AsyncListViewAdapter<ExpenseRecord>() {
      @Override
      protected void onRangeChanged(ListView<ExpenseRecord> view) {
        requestExpenses();
      }
    };
    expenseAdapter.setKeyProvider(Expenses.EXPENSE_RECORD_KEY_PROVIDER);

    expenseList = new CellList<ExpenseRecord>(new ExpenseCell());

    expenseSelection = new SingleSelectionModel<ExpenseRecord>();
    expenseList.setSelectionModel(expenseSelection);
    expenseSelection.addSelectionChangeHandler(new SelectionModel.SelectionChangeHandler() {
      public void onSelectionChange(SelectionChangeEvent event) {
        listener.onExpenseSelected(expenseSelection.getSelectedObject());
      }
    });

    expenseAdapter.addView(expenseList);
    initWidget(expenseList);
  }

  public Widget asWidget() {
    return this;
  }

  public String getPageTitle() {
    return report != null ? report.getPurpose() : "";
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
    listener.onCreateExpense(report.getId());
  }

  public void onCustom() {
  }

  public void onRefresh(boolean clear) {
    if (clear) {
      expenseAdapter.updateDataSize(0, true);
    }
    expenseList.refresh();
  }

  public void show(ReportRecord report) {
    this.report = report;
    knownDeniedKeys = null;

    onRefresh(true);
  }

  private Collection<Property<?>> getExpenseColumns() {
    List<Property<?>> columns = new ArrayList<Property<?>>();
    columns.add(ExpenseRecord.description);
    columns.add(ExpenseRecord.amount);
    return columns;
  }

  /**
   * Request the expenses.
   */
  private void requestExpenses() {
    refreshTimer.cancel();
    if (requestFactory == null || report == null) {
      return;
    }
    lastReceiver = new Receiver<List<ExpenseRecord>>() {
      public void onSuccess(List<ExpenseRecord> newValues) {
        if (this == lastReceiver) {
          int size = newValues.size();
          expenseAdapter.updateDataSize(size, true);
          expenseAdapter.updateViewData(0, size, newValues);

          // Add the new keys to the known keys.
          boolean isInitialData = knownDeniedKeys == null;
          if (knownDeniedKeys == null) {
            knownDeniedKeys = new HashSet<Object>();
          }
          for (ExpenseRecord value : newValues) {
            Object key = expenseAdapter.getKey(value);
            String approval = value.getApproval();
            if (DENIED.equals(approval)) {
              if (!isInitialData && !knownDeniedKeys.contains(key)) {
                (new PhaseAnimation.CellListPhaseAnimation<ExpenseRecord>(
                    expenseList, value, expenseAdapter)).run();
              }
              knownDeniedKeys.add(key);
            } else {
              knownDeniedKeys.remove(key);
            }
          }

          refreshTimer.schedule(REFRESH_INTERVAL);
        }
      }
    };
    requestFactory.expenseRequest().findExpensesByReport(
        report.getRef(ReportRecord.id)).forProperties(getExpenseColumns()).to(
        lastReceiver).fire();
  }
}
