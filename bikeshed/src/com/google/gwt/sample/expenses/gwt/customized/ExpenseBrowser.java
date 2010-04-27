package com.google.gwt.sample.expenses.gwt.customized;

import com.google.gwt.bikeshed.cells.client.TextCell;
import com.google.gwt.bikeshed.list.shared.ListViewAdapter;
import com.google.gwt.bikeshed.list.shared.SelectionModel;
import com.google.gwt.bikeshed.list.shared.SingleSelectionModel;
import com.google.gwt.bikeshed.tree.client.SideBySideTreeView;
import com.google.gwt.bikeshed.tree.client.TreeViewModel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import java.util.List;

/**
 * The browser at the top of the app used to browse expense reports.
 */
public class ExpenseBrowser extends Composite {

  private static ExpenseBrowserUiBinder uiBinder = GWT.create(ExpenseBrowserUiBinder.class);

  interface ExpenseBrowserUiBinder extends UiBinder<Widget, ExpenseBrowser> {
  }

  /**
   * The {@link TreeViewModel} used to browse expense reports.
   * 
   * TODO(jlabanca): Implement this with real data.
   */
  private class ExpensesTreeViewModel implements TreeViewModel {

    /**
     * The shared {@link SelectionModel}.
     */
    private final SelectionModel<Object> selectionModel = new SingleSelectionModel<Object>();

    public <T> NodeInfo<?> getNodeInfo(T value) {
      if (value == null) {
        // Top level.
        return new DefaultNodeInfo<String>(categories, TextCell.getInstance(),
            selectionModel, null);
      } else if (isCategory(value)) {
        // Second level.
        return new DefaultNodeInfo<String>(usernames, TextCell.getInstance(),
            selectionModel, null);
      }

      return null;
    }

    public boolean isLeaf(Object value) {
      return !isCategory(value);
    }

    private boolean isCategory(Object value) {
      return categories.getList().contains(value.toString());
    }
  }

  @UiField
  SideBySideTreeView cellBrowser;

  /**
   * The adapter that provides categories.
   */
  private ListViewAdapter<String> categories = new ListViewAdapter<String>();

  /**
   * The adapter that provides usernames.
   */
  private ListViewAdapter<String> usernames = new ListViewAdapter<String>();

  public ExpenseBrowser() {
    initWidget(uiBinder.createAndBindUi(this));

    // Initialize the categories.
    List<String> categoriesList = categories.getList();
    categoriesList.add("All");
    categoriesList.add("Sales");
    categoriesList.add("Marketing");
    categoriesList.add("Engineering");

    // Initialize the usernames.
    List<String> usernamesList = usernames.getList();
    usernamesList.add("John Q Smith");
    usernamesList.add("Joel Webber");
    usernamesList.add("Dan Rice");
    usernamesList.add("Jaime Yap");
    usernamesList.add("Alfed E Newmann");
    usernamesList.add("John LaBanca");
  }

  @UiFactory
  SideBySideTreeView createTreeBrowser() {
    SideBySideTreeView treeView = new SideBySideTreeView(
        new ExpensesTreeViewModel(), null);
    treeView.setAnimationEnabled(true);
    return treeView;
  }
}
