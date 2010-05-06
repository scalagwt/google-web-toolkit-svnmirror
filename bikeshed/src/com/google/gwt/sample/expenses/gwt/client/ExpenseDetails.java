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

import com.google.gwt.bikeshed.list.client.CellTable;
import com.google.gwt.bikeshed.list.client.Column;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.CurrencyCell;
import com.google.gwt.cell.client.DateCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.SelectionCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.SelectElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.requestfactory.shared.Receiver;
import com.google.gwt.requestfactory.shared.SyncResult;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.sample.bikeshed.style.client.Styles;
import com.google.gwt.sample.expenses.gwt.request.EmployeeRecord;
import com.google.gwt.sample.expenses.gwt.request.ExpenseRecord;
import com.google.gwt.sample.expenses.gwt.request.ExpenseRecordChanged;
import com.google.gwt.sample.expenses.gwt.request.ExpensesRequestFactory;
import com.google.gwt.sample.expenses.gwt.request.ReportRecord;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.valuestore.shared.DeltaValueStore;
import com.google.gwt.valuestore.shared.Property;
import com.google.gwt.valuestore.shared.Record;
import com.google.gwt.view.client.ListViewAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Details about the current expense report on the right side of the app,
 * including the list of expenses.
 */
public class ExpenseDetails extends Composite implements
    Receiver<List<ExpenseRecord>>, ExpenseRecordChanged.Handler {

  /**
   * String indicating approval.
   */
  private static final String APPROVED = "Approved";

  /**
   * String indicating denial.
   */
  private static final String DENIED = "Denied";

  /**
   * The maximum amount that can be approved for a given report.
   */
  private static final int MAX_COST = 500;

  /**
   * The ViewData associated with the {@link ApprovalCell}.
   */
  private static class ApprovalViewData {
    private final String pendingApproval;
    private String rejectionText;

    public ApprovalViewData(String approval) {
      this.pendingApproval = approval;
    }

    public String getPendingApproval() {
      return pendingApproval;
    }

    public String getRejectionText() {
      return rejectionText;
    }

    public boolean isRejected() {
      return rejectionText != null;
    }

    public void reject(String text) {
      this.rejectionText = text;
    }
  }

  /**
   * The cell used for approval status.
   */
  private class ApprovalCell extends SelectionCell {

    private final String approvedClass;
    private final String blankClass;
    private final String deniedClass;
    private final String errorIconHtml;
    private final String pendingIconHtml;

    public ApprovalCell(List<String> options) {
      super(options);
      approvedClass = " class='" + Styles.common().approvedOption() + "'";
      blankClass = " class='" + Styles.common().blankOption() + "'";
      deniedClass = " class='" + Styles.common().deniedOption() + "'";

      // Cache the html string for the error icon.
      ImageResource errorIcon = Styles.resources().errorIcon();
      AbstractImagePrototype errorImg = AbstractImagePrototype.create(errorIcon);
      errorIconHtml = errorImg.getHTML();

      // Cache the html string for the pending icon.
      ImageResource pendingIcon = Styles.resources().pendingCommit();
      AbstractImagePrototype pendingImg = AbstractImagePrototype.create(pendingIcon);
      pendingIconHtml = pendingImg.getHTML();
    }

    @Override
    public Object onBrowserEvent(Element parent, String value, Object viewData,
        NativeEvent event, ValueUpdater<String> valueUpdater) {
      String type = event.getType();
      if ("change".equals(type)) {
        // Disable the select box.
        SelectElement select = parent.getFirstChild().cast();
        select.setClassName(Styles.common().blankOption());
        select.setDisabled(true);

        // Add the pending icon if it isn't already visible.
        if (viewData == null) {
          Element tmpElem = Document.get().createDivElement();
          tmpElem.setInnerHTML(pendingIconHtml);
          parent.appendChild(tmpElem.getFirstChildElement());
        }

        // Remember which value is now selected.
        int index = select.getSelectedIndex();
        String pendingValue = select.getOptions().getItem(index).getValue();
        viewData = new ApprovalViewData(pendingValue);
      } else if ("click".equals(type) && viewData != null
          && parent.getChildCount() >= 3) {
        // Alert the user of the error
        Element img = parent.getChild(1).cast();
        Element anchor = img.getNextSiblingElement();
        if (anchor.isOrHasChild(Element.as(event.getEventTarget().cast()))) {
          // Alert the user of the error.
          ApprovalViewData avd = (ApprovalViewData) viewData;
          errorPopupMessage.setText(avd.getRejectionText());
          errorPopup.center();

          // Clear the view data now that we've viewed the message.
          viewData = null;
          parent.removeChild(anchor);
          parent.removeChild(img);
        }
      }
      super.onBrowserEvent(parent, value, viewData, event, valueUpdater);
      return viewData;
    }

    @Override
    public void render(String value, Object viewData, StringBuilder sb) {
      // Get the view data.
      boolean isRejected = false;
      String pendingValue = null;
      String renderValue = value;
      if (viewData != null) {
        ApprovalViewData avd = (ApprovalViewData) viewData;
        if (!avd.getPendingApproval().equals(value)) {
          isRejected = avd.isRejected();
          pendingValue = avd.getPendingApproval();
          if (!isRejected) {
            renderValue = pendingValue;
          }
        }
      }
      boolean isApproved = APPROVED.equals(renderValue);
      boolean isDenied = DENIED.equals(renderValue);

      // Create the select element.
      sb.append("<select style='background-color:white;");
      sb.append("border:1px solid #707172;width:10em;margin-right:10px;'");
      if (pendingValue != null && !isRejected) {
        // No icon on pending values.
        sb.append(blankClass);
      } else if (isApproved) {
        sb.append(approvedClass);
      } else if (isDenied) {
        sb.append(deniedClass);
      } else {
        sb.append(blankClass);
      }
      sb.append(">");
      sb.append("<option></option>");

      // Approved Option.
      sb.append("<option");
      sb.append(approvedClass);
      if (isApproved) {
        sb.append(" selected='selected'");
      }
      sb.append(">").append(APPROVED).append("</option>");

      // Denied Option.
      sb.append("<option");
      sb.append(deniedClass);
      if (isDenied) {
        sb.append(" selected='selected'");
      }
      sb.append(">").append(DENIED).append("</option>");

      sb.append("</select>");

      // Add an icon indicating the commit state.
      if (isRejected) {
        // Add error icon if viewData does not match.
        sb.append(errorIconHtml);
        sb.append("<a style='padding-left:3px;color:red;' href='javascript:;'>Error!</a>");
      } else if (pendingValue != null) {
        // Add refresh icon if pending.
        sb.append(pendingIconHtml);
      }
    }
  }

  /**
   * The popup used to enter the rejection reason.
   */
  private class DenialPopup extends DialogBox {
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

  ExpensesRequestFactory expensesRequestFactory;

  @UiField
  TextBox notesBox;

  @UiField
  Element reportName;

  @UiField
  Anchor reportsLink;

  @UiField
  CellTable<ExpenseRecord> table;

  private List<SortableHeader> allHeaders = new ArrayList<SortableHeader>();

  private SortableColumn<ExpenseRecord, String> approvalColumn;
  private SortableColumn<ExpenseRecord, Date> dateColumn;

  /**
   * The popup used to display errors to the user.
   */
  private final PopupPanel errorPopup = new PopupPanel(false, true);

  /**
   * The label inside the error popup.
   */
  private final Label errorPopupMessage = new Label();

  /**
   * The adapter that provides expense items.
   */
  private final ListViewAdapter<ExpenseRecord> items;

  private Comparator<ExpenseRecord> lastComparator;

  /**
   * The current report being displayed.
   */
  private ReportRecord report;

  /**
   * The total amount that has been approved.
   */
  private double totalApproved;

  public ExpenseDetails() {
    createErrorPopup();
    initWidget(uiBinder.createAndBindUi(this));
    items = new ListViewAdapter<ExpenseRecord>();
    items.setKeyProvider(Expenses.EXPENSE_RECORD_KEY_PROVIDER);
    table.setKeyProvider(items);
    items.addView(table);
  }

  public Anchor getReportsLink() {
    return reportsLink;
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

    refreshCost();
    if (lastComparator != null) {
      sortExpenses(list, lastComparator);
    }
  }

  public void onSuccess(List<ExpenseRecord> newValues) {
    List<ExpenseRecord> list = new ArrayList<ExpenseRecord>(newValues);
    sortExpenses(list, lastComparator);
    items.setList(list);
    refreshCost();
  }

  public void setExpensesRequestFactory(
      ExpensesRequestFactory expensesRequestFactory) {
    this.expensesRequestFactory = expensesRequestFactory;
  }

  /**
   * Set the {@link ReportRecord} to show.
   * 
   * @param report the {@link ReportRecord}
   * @param department the selected department
   * @param employee the selected employee
   */
  public void setReportRecord(ReportRecord report, String department,
      EmployeeRecord employee) {
    this.report = report;
    reportName.setInnerText(report.getPurpose());
    notesBox.setText(report.getNotes());
    costLabel.setInnerText("");
    approvedLabel.setInnerText("");
    totalApproved = 0;

    // Update the breadcrumb.
    reportsLink.setText(ExpenseList.getBreadcrumb(department, employee));

    // Reset sorting state of table
    lastComparator = dateColumn.getComparator(false);
    for (SortableHeader header : allHeaders) {
      header.setSorted(false);
      header.setReverseSort(true);
    }
    allHeaders.get(0).setSorted(true);
    allHeaders.get(0).setReverseSort(false);
    table.refreshHeaders();

    // Request the expenses.
    requestExpenses();
  }

  @UiFactory
  CellTable<ExpenseRecord> createTable() {
    CellTable.Resources resources = GWT.create(CellTable.CleanResources.class);
    CellTable<ExpenseRecord> view = new CellTable<ExpenseRecord>(15, resources);
    Styles.Common common = Styles.common();
    view.addColumnStyleName(0, common.expenseDetailsDateColumn());
    view.addColumnStyleName(2, common.expenseDetailsCategoryColumn());
    view.addColumnStyleName(3, common.expenseDetailsAmountColumn());
    view.addColumnStyleName(4, common.expenseDetailsApprovalColumn());

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
          // We need to redraw the table to reset the select box.
          syncCommit(record, null);
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
    approvalColumn = addColumn(view, "Approval Status", new ApprovalCell(
        options), new GetValue<ExpenseRecord, String>() {
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
        sortExpenses(items.getList(),
            column.getComparator(header.getReverseSort()));
        table.refreshHeaders();
      }
    });
    table.addColumn(column, header);
    return column;
  }

  private Column<ExpenseRecord, String> addColumn(
      CellTable<ExpenseRecord> table, final String text,
      final GetValue<ExpenseRecord, String> getter) {
    return addColumn(table, text, new TextCell(), getter);
  }

  /**
   * Create the error message popup.
   */
  private void createErrorPopup() {
    errorPopup.setGlassEnabled(true);
    errorPopupMessage.addStyleName(Styles.common().expenseDetailsErrorPopupMessage());

    Button closeButton = new Button("Dismiss", new ClickHandler() {
      public void onClick(ClickEvent event) {
        errorPopup.hide();
      }
    });

    // Organize the widgets in the popup.
    VerticalPanel layout = new VerticalPanel();
    layout.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    layout.add(errorPopupMessage);
    layout.add(closeButton);
    errorPopup.setWidget(layout);
  }

  /**
   * Return a formatted currency string.
   * 
   * @param amount the amount in dollars
   * @return a formatted string
   */
  private String formatCurrency(double amount) {
    boolean negative = amount < 0;
    if (negative) {
      amount = -amount;
    }
    int dollars = (int) amount;
    int cents = (int) ((amount * 100) % 100);

    StringBuilder sb = new StringBuilder();
    if (negative) {
      sb.append("-");
    }
    sb.append("$");
    sb.append(dollars);
    sb.append('.');
    if (cents < 10) {
      sb.append('0');
    }
    sb.append(cents);
    return sb.toString();
  }

  /**
   * Get the columns displayed in the expense table.
   */
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

  /**
   * Refresh the total cost and approved amount.
   */
  private void refreshCost() {
    double totalCost = 0;
    totalApproved = 0;
    List<ExpenseRecord> records = items.getList();
    for (ExpenseRecord record : records) {
      double cost = record.getAmount();
      totalCost += cost;
      if (APPROVED.equals(record.getApproval())) {
        totalApproved += cost;
      }
    }
    costLabel.setInnerText(formatCurrency(totalCost));
    approvedLabel.setInnerText(formatCurrency(totalApproved));
  }

  /**
   * Request the expenses.
   */
  private void requestExpenses() {
    expensesRequestFactory.expenseRequest().findExpensesByReport(
        report.getRef(Record.id)).forProperties(getExpenseColumns()).to(this).fire();
  }

  private void sortExpenses(List<ExpenseRecord> list,
      final Comparator<ExpenseRecord> comparator) {
    lastComparator = comparator;
    Collections.sort(list, comparator);
  }

  /**
   * Update the state of a pending approval change.
   * 
   * @param record the {@link ExpenseRecord} to sync
   * @param message the error message if rejected, or null if accepted
   */
  private void syncCommit(ExpenseRecord record, String message) {
    final Object key = items.getKey(record);
    if (message != null) {
      final ApprovalViewData avd = (ApprovalViewData) approvalColumn.getViewData(key);
      if (avd != null) {
        avd.reject(message);
      }

      // Redraw the table so the changes are applied.
      table.redraw();
    } else {
      approvalColumn.setViewData(key, null);
    }
  }

  private void updateExpenseRecord(final ExpenseRecord record, String approval,
      String reasonDenied) {
    // Verify that the total is under the cap.
    if (APPROVED.equals(approval) && !APPROVED.equals(record.getApproval())) {
      double amount = record.getAmount();
      if (amount + totalApproved > MAX_COST) {
        syncCommit(record,
            "The total approved amount for an Expense Report cannot exceed $"
                + MAX_COST);
        return;
      }
    }

    // Create a delta and sync with the value store.
    DeltaValueStore deltas = expensesRequestFactory.getValueStore().spawnDeltaView();
    deltas.set(ExpenseRecord.approval, record, approval);
    deltas.set(ExpenseRecord.reasonDenied, record, reasonDenied);
    expensesRequestFactory.syncRequest(deltas).to(
        new Receiver<Set<SyncResult>>() {
          public void onSuccess(Set<SyncResult> response) {
            // Check for commit errors.
            String errorMessage = "";
            for (SyncResult result : response) {
              if (result.hasViolations()) {
                // TODO(jlabanca): Get the error messages from the violations.
                errorMessage = "Could not commit change";
              }
            }

            // Sync the view data.
            if (errorMessage.length() > 0) {
              syncCommit(record, errorMessage.length() > 0 ? errorMessage
                  : null);
            }

            // Request the updated expenses.
            requestExpenses();
          }
        }).fire();
  }
}
