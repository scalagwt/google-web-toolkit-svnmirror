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
import com.google.gwt.bikeshed.cells.client.ClickableTextCell;
import com.google.gwt.bikeshed.cells.client.CurrencyCell;
import com.google.gwt.bikeshed.cells.client.DateCell;
import com.google.gwt.bikeshed.cells.client.TextCell;
import com.google.gwt.bikeshed.cells.client.ValueUpdater;
import com.google.gwt.bikeshed.list.client.CellTable;
import com.google.gwt.bikeshed.list.client.Column;
import com.google.gwt.bikeshed.list.client.Header;
import com.google.gwt.bikeshed.list.shared.ListViewAdapter;
import com.google.gwt.core.client.GWT;
import com.google.gwt.requestfactory.shared.Receiver;
import com.google.gwt.sample.expenses.gwt.request.ExpenseRecord;
import com.google.gwt.sample.expenses.gwt.request.ExpenseRecordChanged;
import com.google.gwt.sample.expenses.gwt.request.ReportRecord;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Details about the current expense report on the right side of the app,
 * including the list of expenses.
 */
public class ExpenseDetails extends Composite implements
    Receiver<List<ExpenseRecord>>, ExpenseRecordChanged.Handler {

  interface ExpenseDetailsUiBinder extends UiBinder<Widget, ExpenseDetails> {
  }

  static interface GetValue<T, C> {
    C getValue(T object);
  }

  private static ExpenseDetailsUiBinder uiBinder = GWT.create(ExpenseDetailsUiBinder.class);

  @UiField
  TextBox notesBox;

  @UiField
  Label reportName;

  @UiField
  CellTable<ExpenseRecord> table;

  @UiField
  Label totalLabel;

  private final Comparator<ExpenseRecord> amountComparator = new Comparator<ExpenseRecord>() {
    public int compare(ExpenseRecord o1, ExpenseRecord o2) {
      double cmp = o1.getAmount() - o2.getAmount();
      if (cmp < 0) {
        return -1;
      } else if (cmp > 0) {
        return 1;
      } else {
        return 0;
      }
    }
  };

  private final Comparator<ExpenseRecord> dateComparator = new Comparator<ExpenseRecord>() {
    public int compare(ExpenseRecord o1, ExpenseRecord o2) {
      long cmp = o1.getDate().getTime() - o2.getDate().getTime();
      if (cmp < 0) {
        return -1;
      } else if (cmp > 0) {
        return 1;
      } else {
        return 0;
      }
    }
  };

  /**
   * The adapter that provides expense items.
   */
  private ListViewAdapter<ExpenseRecord> items = new ListViewAdapter<ExpenseRecord>();

  private List<ExpenseRecord> itemList = items.getList();

  private Comparator<ExpenseRecord> lastComparator = dateComparator;

  private boolean lastSortUp = true;

  public ExpenseDetails() {
    initWidget(uiBinder.createAndBindUi(this));

    // Add the view to the adapter.
    items.addView(table);
  }

  public void onReportDetailChanged(ExpenseRecordChanged event) {
    items.refresh();
    itemList = items.getList();
    sortExpenses(lastComparator, lastSortUp);
  }

  public void onSuccess(List<ExpenseRecord> newValues) {
    items.setList(newValues);
    itemList = items.getList();
    sortExpenses(lastComparator, lastSortUp);
  }

  public void setReportRecord(ReportRecord report) {
    reportName.setText(report.getPurpose());
    notesBox.setText(report.getNotes());
  }

  @UiFactory
  CellTable<ExpenseRecord> createTable() {
    CellTable<ExpenseRecord> view = new CellTable<ExpenseRecord>(15);

    // Date column
    addColumn(view, "Date", new DateCell(),
        new GetValue<ExpenseRecord, Date>() {
          public Date getValue(ExpenseRecord object) {
            return object.getDate();
          }
        }, dateComparator);

    // Description column.
    addColumn(view, "Description", new GetValue<ExpenseRecord, String>() {
      public String getValue(ExpenseRecord object) {
        return object.getDescription();
      }
    });

    // Category column.
    addColumn(view, "Category", new GetValue<ExpenseRecord, String>() {
      public String getValue(ExpenseRecord object) {
        return object.getCategory();
      }
    });

    // Amount column.
    addColumn(view, "Amount", new CurrencyCell(),
        new GetValue<ExpenseRecord, Integer>() {
          public Integer getValue(ExpenseRecord object) {
            return (int) (object.getAmount().doubleValue() * 100);
          }
        }, amountComparator);

    // Approval column.
    addColumn(view, "Approval Status", new GetValue<ExpenseRecord, String>() {
      public String getValue(ExpenseRecord object) {
        return object.getApproval();
      }
    });

    return view;
  }

  private <C extends Comparable<C>> Column<ExpenseRecord, C> addColumn(
      CellTable<ExpenseRecord> table, final String text, final Cell<C> cell,
      final GetValue<ExpenseRecord, C> getter,
      final Comparator<ExpenseRecord> comparator) {
    Column<ExpenseRecord, C> column = new Column<ExpenseRecord, C>(cell) {
      @Override
      public C getValue(ExpenseRecord object) {
        return getter.getValue(object);
      }
    };
    Header<String> header = new Header<String>(ClickableTextCell.getInstance()) {
      @Override
      public String getValue() {
        return text;
      }
    };
    header.setUpdater(new ValueUpdater<String>() {
      boolean sortUp = true;

      public void update(String value) {
        if (comparator == null) {
          sortExpenses(new Comparator<ExpenseRecord>() {
            public int compare(ExpenseRecord o1, ExpenseRecord o2) {
              return getter.getValue(o1).compareTo(getter.getValue(o2));
            }
          }, sortUp);
        } else {
          sortExpenses(comparator, sortUp);
        }
        sortUp = !sortUp;
      }
    });
    table.addColumn(column, header);
    return column;
  }

  private Column<ExpenseRecord, String> addColumn(
      CellTable<ExpenseRecord> table, final String text,
      final GetValue<ExpenseRecord, String> getter) {
    return addColumn(table, text, TextCell.getInstance(), getter, null);
  }

  private void sortExpenses(final Comparator<ExpenseRecord> comparator,
      boolean sortUp) {
    lastComparator = comparator;
    lastSortUp = sortUp;
    if (sortUp) {
      Collections.sort(itemList, comparator);
    } else {
      Collections.sort(itemList, new Comparator<ExpenseRecord>() {
        public int compare(ExpenseRecord o1, ExpenseRecord o2) {
          return -comparator.compare(o1, o2);
        }
      });
    }
  }
}
