package com.google.gwt.sample.expenses.gwt.customized;

import com.google.gwt.bikeshed.cells.client.CheckboxCell;
import com.google.gwt.bikeshed.cells.client.CurrencyCell;
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
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import java.util.Date;
import java.util.List;

/**
 * Details about the current expense report on the right side of the app,
 * including the list of expenses.
 */
public class ExpenseDetails extends Composite {

  private static ExpenseDetailsUiBinder uiBinder = GWT.create(ExpenseDetailsUiBinder.class);

  interface ExpenseDetailsUiBinder extends UiBinder<Widget, ExpenseDetails> {
  }

  /**
   * An expense in an expense report.
   * 
   * TODO(jlabanca): Replace this with a generated class.
   */
  private static class ExpenseItem {
    public String name;
    public Date date;
    public String category;
    public int price;
    public boolean approved;

    public ExpenseItem(String name, Date date, String category, int price,
        boolean approved) {
      this.name = name;
      this.date = date;
      this.category = category;
      this.price = price;
      this.approved = approved;
    }
  }

  /**
   * The adapter that provides expense items.
   */
  private ListViewAdapter<ExpenseItem> items = new ListViewAdapter<ExpenseItem>();

  @UiField
  PagingTableListView<ExpenseItem> listView;

  @UiField
  TextBox notesBox;

  @UiField
  Label totalLabel;

  public ExpenseDetails() {
    initWidget(uiBinder.createAndBindUi(this));

    // Initialize the widgets.
    notesBox.setText("Trip to Jaimaica for 'research'");
    totalLabel.setText("$42");

    // Initialize the list of expenses.
    List<ExpenseItem> itemList = items.getList();
    itemList.add(new ExpenseItem("Beer", new Date(), "Food", 4215, true));
    itemList.add(new ExpenseItem("Jerk Chicken", new Date(), "Food", 1500,
        false));

    // Add the view to the adapter.
    items.addView(listView);
  }

  @UiFactory
  PagingTableListView<ExpenseItem> createListView() {
    PagingTableListView<ExpenseItem> table = new PagingTableListView<ExpenseItem>(
        15);

    // Name column.
    table.addColumn(new Column<ExpenseItem, String>(TextCell.getInstance()) {
      @Override
      public String getValue(ExpenseItem object) {
        return object.name;
      }
    }, new Header<String>(TextCell.getInstance()) {
      @Override
      public String getValue() {
        return "Name";
      }
    });

    // Date column.
    table.addColumn(new Column<ExpenseItem, Date>(new DateCell()) {
      @Override
      public Date getValue(ExpenseItem object) {
        return object.date;
      }
    }, new Header<String>(TextCell.getInstance()) {
      @Override
      public String getValue() {
        return "Date";
      }
    });

    // Category column.
    table.addColumn(new Column<ExpenseItem, String>(TextCell.getInstance()) {
      @Override
      public String getValue(ExpenseItem object) {
        return object.category;
      }
    }, new Header<String>(TextCell.getInstance()) {
      @Override
      public String getValue() {
        return "Category";
      }
    });

    // Price column.
    table.addColumn(new Column<ExpenseItem, Integer>(new CurrencyCell()) {
      @Override
      public Integer getValue(ExpenseItem object) {
        return object.price;
      }
    }, new Header<String>(TextCell.getInstance()) {
      @Override
      public String getValue() {
        return "Price";
      }
    });

    // Approved column.
    table.addColumn(new Column<ExpenseItem, Boolean>(new CheckboxCell()) {
      @Override
      public Boolean getValue(ExpenseItem object) {
        return object.approved;
      }
    }, new Header<String>(TextCell.getInstance()) {
      @Override
      public String getValue() {
        return "Approved";
      }
    });

    return table;
  }
}
