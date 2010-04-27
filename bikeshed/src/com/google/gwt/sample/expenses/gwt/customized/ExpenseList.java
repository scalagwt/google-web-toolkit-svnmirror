package com.google.gwt.sample.expenses.gwt.customized;

import com.google.gwt.bikeshed.cells.client.DateCell;
import com.google.gwt.bikeshed.cells.client.TextCell;
import com.google.gwt.bikeshed.list.client.Column;
import com.google.gwt.bikeshed.list.client.Header;
import com.google.gwt.bikeshed.list.client.PagingTableListView;
import com.google.gwt.bikeshed.list.shared.ListViewAdapter;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import java.util.Date;
import java.util.List;

/**
 * The list of expense reports on the left side of the app.
 */
public class ExpenseList extends Composite {

  private static ExpenseListUiBinder uiBinder = GWT.create(ExpenseListUiBinder.class);

  /**
   * An expense report.
   * 
   * TODO(jlabanca): Replace this with a generated class.
   */
  private static class ExpenseReport {
    public String name;
    public Date date;
    public String notes;

    public ExpenseReport(String name, Date date, String notes) {
      this.name = name;
      this.date = date;
      this.notes = notes;
    }
  }

  interface ExpenseListUiBinder extends UiBinder<Widget, ExpenseList> {
  }

  @UiField
  PagingTableListView<ExpenseReport> listView;

  /**
   * The adapter that provides reports.
   */
  private ListViewAdapter<ExpenseReport> reports = new ListViewAdapter<ExpenseReport>();

  public ExpenseList() {
    initWidget(uiBinder.createAndBindUi(this));

    // Initialize the reports.
    List<ExpenseReport> reportsList = reports.getList();
    reportsList.add(new ExpenseReport("Jamaica Trip", new Date(2000, 1, 1),
        "Trip to Jaimaica for 'research'"));
    reportsList.add(new ExpenseReport("SFO", new Date(2000, 5, 15), ""));
    reportsList.add(new ExpenseReport("NYY", new Date(2003, 6, 12), ""));
    reportsList.add(new ExpenseReport("ATL", new Date(2005, 3, 1), "Long trip"));

    // Add the view to the adapter.
    reports.addView(listView);
  }

  @UiFactory
  PagingTableListView<ExpenseReport> createListView() {
    PagingTableListView<ExpenseReport> table = new PagingTableListView<ExpenseReport>(
        25);

    // Name column.
    table.addColumn(new Column<ExpenseReport, String>(TextCell.getInstance()) {
      @Override
      public String getValue(ExpenseReport object) {
        return object.name;
      }
    }, new Header<String>(TextCell.getInstance()) {
      @Override
      public String getValue() {
        return "Name";
      }
    });

    // Date column.
    table.addColumn(new Column<ExpenseReport, Date>(new DateCell()) {
      @Override
      public Date getValue(ExpenseReport object) {
        return object.date;
      }
    }, new Header<String>(TextCell.getInstance()) {
      @Override
      public String getValue() {
        return "Date";
      }
    });

    // Notes column.
    table.addColumn(new Column<ExpenseReport, String>(TextCell.getInstance()) {
      @Override
      public String getValue(ExpenseReport object) {
        return object.notes;
      }
    }, new Header<String>(TextCell.getInstance()) {
      @Override
      public String getValue() {
        return "Notes";
      }
    });

    return table;
  }
}
