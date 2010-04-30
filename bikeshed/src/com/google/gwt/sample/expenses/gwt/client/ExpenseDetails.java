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
import com.google.gwt.bikeshed.cells.client.CurrencyCell;
import com.google.gwt.bikeshed.cells.client.DateCell;
import com.google.gwt.bikeshed.cells.client.FieldUpdater;
import com.google.gwt.bikeshed.cells.client.SelectionCell;
import com.google.gwt.bikeshed.cells.client.TextCell;
import com.google.gwt.bikeshed.cells.client.ValueUpdater;
import com.google.gwt.bikeshed.list.client.CellTable;
import com.google.gwt.bikeshed.list.client.Column;
import com.google.gwt.bikeshed.list.shared.ListViewAdapter;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.requestfactory.shared.Receiver;
import com.google.gwt.sample.expenses.gwt.request.ExpenseRecord;
import com.google.gwt.sample.expenses.gwt.request.ExpenseRecordChanged;
import com.google.gwt.sample.expenses.gwt.request.ExpensesRequestFactory;
import com.google.gwt.sample.expenses.gwt.request.ReportRecord;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.valuestore.shared.DeltaValueStore;

import java.util.ArrayList;
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

  class DenialPopup extends DialogBox {
    private Button cancelButton = new Button("Cancel", new ClickHandler() {
      public void onClick(ClickEvent event) {
        reasonDenied = "";
        hide();
      }
    });
    private Button confirmButton = new Button("Confirm", new ClickHandler() {
      public void onClick(ClickEvent event) {
        reasonDenied = reasonBox.getText();
        hide();
      }
    });

    private ExpenseRecord expenseRecord;
    private FlexTable layout = new FlexTable();
    private TextBox reasonBox = new TextBox();
    private String reasonDenied;

    public DenialPopup() {
      super(false, true);
      setGlassEnabled(true);
      setWidget(layout);

      layout.setHTML(0, 0, "Reason for denial:");
      layout.setWidget(1, 0, reasonBox);
      HorizontalPanel p = new HorizontalPanel();
      p.add(confirmButton);
      p.add(cancelButton);
      layout.setWidget(2, 0, p);
    }

    public ExpenseRecord getExpenseRecord() {
      return expenseRecord;
    }

    public String getReasonDenied() {
      return reasonDenied;
    }

    public void popup() {
      center();
      reasonBox.setFocus(true);
    }

    public void setExpenseRecord(ExpenseRecord expenseRecord) {
      this.expenseRecord = expenseRecord;
    }

    public void setReasonDenied(String reasonDenied) {
      this.reasonDenied = reasonDenied;
      reasonBox.setText(reasonDenied);
    }
  }

  interface ExpenseDetailsUiBinder extends UiBinder<Widget, ExpenseDetails> {
  }

  private static final GetValue<ExpenseRecord, Date> dateGetter = new GetValue<ExpenseRecord, Date>() {
    public Date getValue(ExpenseRecord object) {
      return object.getDate();
    }
  };

  private static ExpenseDetailsUiBinder uiBinder = GWT.create(ExpenseDetailsUiBinder.class);

  @UiField
  Element approvedLabel;

  @UiField
  Element costLabel;

  @UiField
  Element defaultText;

  ExpensesRequestFactory expensesRequestFactory;

  @UiField
  Element mainLayout;

  @UiField
  TextBox notesBox;

  @UiField
  Element reportName;

  @UiField
  CellTable<ExpenseRecord> table;

  private List<SortableHeader> allHeaders = new ArrayList<SortableHeader>();

  private SortableColumn<ExpenseRecord, Date> dateColumn;

  /**
   * The adapter that provides expense items.
   */
  private ListViewAdapter<ExpenseRecord> items = new ListViewAdapter<ExpenseRecord>();

  private Comparator<ExpenseRecord> lastComparator;

  public ExpenseDetails() {
    initWidget(uiBinder.createAndBindUi(this));
    setReportRecord(null);

    // Add the view to the adapter.
    items.addView(table);
  }

  public void onExpenseRecordChanged(ExpenseRecordChanged event) {
    ExpenseRecord newRecord = event.getRecord();
    String id = newRecord.getId();

    int index = 0;
    List<ExpenseRecord> list = items.getList();
    for (ExpenseRecord r : list) {
      if (r.getId().equals(id)) {
        list.set(index, newRecord);
      }
      index++;
    }

    if (lastComparator != null) {
      sortExpenses(lastComparator);
    }
  }

  public void onSuccess(List<ExpenseRecord> newValues) {
    items.setList(newValues);
    sortExpenses(dateColumn.getComparator(false));
  }

  public void setExpensesRequestFactory(
      ExpensesRequestFactory expensesRequestFactory) {
    this.expensesRequestFactory = expensesRequestFactory;
  }

  public void setReportRecord(ReportRecord report) {
    if (report == null) {
      setVisible(defaultText, true);
      setVisible(mainLayout, false);
      return;
    }

    // Show the main layout when a report becomes available.
    setVisible(defaultText, false);
    setVisible(mainLayout, true);

    reportName.setInnerText(report.getPurpose());
    notesBox.setText(report.getNotes());

    // Reset sorting state of table
    lastComparator = null;
    for (SortableHeader header : allHeaders) {
      header.setSorted(false);
      header.setReverseSort(true);
    }
    allHeaders.get(0).setSorted(true);
    allHeaders.get(0).setReverseSort(false);
    table.refreshHeaders();
  }

  @UiFactory
  CellTable<ExpenseRecord> createTable() {
    CellTable<ExpenseRecord> view = new CellTable<ExpenseRecord>(15);

    dateColumn = addColumn(view, "Date", new DateCell(), dateGetter);
    lastComparator = dateColumn.getComparator(false);

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
        });

    // Dialog box to obtain a reason for a denial
    final DenialPopup denialPopup = new DenialPopup();
    denialPopup.addCloseHandler(new CloseHandler<PopupPanel>() {
      public void onClose(CloseEvent<PopupPanel> event) {
        String reasonDenied = denialPopup.getReasonDenied();
        ExpenseRecord record = denialPopup.getExpenseRecord();
        if (reasonDenied == null || reasonDenied.length() == 0) {
          updateExpenseRecord(record, "", "");
        } else {
          updateExpenseRecord(record, "Denied", reasonDenied);
        }
      }
    });

    // Approval column.
    List<String> options = new ArrayList<String>();
    // TODO(rice): I18N
    options.add("");
    options.add("Approved");
    options.add("Denied");
    SortableColumn<ExpenseRecord, String> approvalColumn = addColumn(view,
        "Approval Status", new SelectionCell(options),
        new GetValue<ExpenseRecord, String>() {
          public String getValue(ExpenseRecord object) {
            return object.getApproval();
          }
        });
    approvalColumn.setFieldUpdater(new FieldUpdater<ExpenseRecord, String>() {
      public void update(int index, final ExpenseRecord object, String value) {
        if ("Denied".equals(value)) {
          denialPopup.setExpenseRecord(object);
          denialPopup.setReasonDenied(object.getReasonDenied());
          denialPopup.popup();
        } else {
          updateExpenseRecord(object, value, "");
        }
      }
    });

    return view;
  }

  private <C extends Comparable<C>> SortableColumn<ExpenseRecord, C> addColumn(
      final CellTable<ExpenseRecord> table, final String text,
      final Cell<C> cell, final GetValue<ExpenseRecord, C> getter) {
    final SortableColumn<ExpenseRecord, C> column = new SortableColumn<ExpenseRecord, C>(
        cell) {
      @Override
      public C getValue(ExpenseRecord object) {
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
        sortExpenses(column.getComparator(header.getReverseSort()));
        table.refreshHeaders();
      }
    });
    table.addColumn(column, header);
    return column;
  }

  private Column<ExpenseRecord, String> addColumn(
      CellTable<ExpenseRecord> table, final String text,
      final GetValue<ExpenseRecord, String> getter) {
    return addColumn(table, text, TextCell.getInstance(), getter);
  }

  private void sortExpenses(final Comparator<ExpenseRecord> comparator) {
    lastComparator = comparator;
    Collections.sort(items.getList(), comparator);
  }

  private void updateExpenseRecord(ExpenseRecord record, String approval,
      String reasonDenied) {
    DeltaValueStore deltas = expensesRequestFactory.getValueStore().spawnDeltaView();
    deltas.set(ExpenseRecord.approval, record, approval);
    deltas.set(ExpenseRecord.reasonDenied, record, reasonDenied);
    expensesRequestFactory.syncRequest(deltas).fire();
  }
}
