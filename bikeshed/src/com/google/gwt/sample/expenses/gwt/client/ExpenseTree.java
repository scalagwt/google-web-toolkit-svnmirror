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
import com.google.gwt.bikeshed.cells.client.IconCellDecorator;
import com.google.gwt.bikeshed.cells.client.TextCell;
import com.google.gwt.bikeshed.list.shared.ListViewAdapter;
import com.google.gwt.bikeshed.list.shared.ProvidesKey;
import com.google.gwt.bikeshed.list.shared.SingleSelectionModel;
import com.google.gwt.bikeshed.list.shared.SelectionModel.SelectionChangeEvent;
import com.google.gwt.bikeshed.list.shared.SelectionModel.SelectionChangeHandler;
import com.google.gwt.bikeshed.tree.client.CellTree;
import com.google.gwt.bikeshed.tree.client.CellTreeViewModel;
import com.google.gwt.requestfactory.shared.Receiver;
import com.google.gwt.sample.bikeshed.style.client.Styles;
import com.google.gwt.sample.expenses.gwt.request.EmployeeRecord;
import com.google.gwt.sample.expenses.gwt.request.EmployeeRecordChanged;
import com.google.gwt.user.client.ui.Composite;

import java.util.List;

/**
 * The employee tree located on the left of the app.
 */
public class ExpenseTree extends Composite implements
    Receiver<List<EmployeeRecord>>, EmployeeRecordChanged.Handler {

  /**
   * Custom listener for this widget.
   */
  public interface Listener {

    /**
     * Called when the user selects a tree item.
     * 
     * @param category the selected category name
     * @param employee the selected employee
     */
    void onSelection(String category, EmployeeRecord employee);
  }

  /**
   * A {@link Cell} that represents an {@link EmployeeRecord}.
   */
  private class EmployeeCell extends IconCellDecorator<EmployeeRecord> {

    public EmployeeCell() {
      super(Styles.resources().userIcon(), new Cell<EmployeeRecord>() {
        @Override
        public void render(EmployeeRecord value, Object viewData,
            StringBuilder sb) {
          if (value != null) {
            sb.append(value.getDisplayName()).append("<br>");
            sb.append("<i>").append(value.getUserName()).append("</i>");
          }
        }
      });
    }
  }

  /**
   * The {@link CellTreeViewModel} used to browse expense reports.
   */
  private class ExpensesTreeViewModel implements CellTreeViewModel {

    /**
     * The category cell singleton.
     */
    private final Cell<String> categoryCell = new IconCellDecorator<String>(
        Styles.resources().groupIcon(), TextCell.getInstance());

    /**
     * The {@link EmployeeCell} singleton.
     */
    private final EmployeeCell employeeCell = new EmployeeCell();

    public <T> NodeInfo<?> getNodeInfo(T value) {
      if (value == null) {
        // Top level.
        return new DefaultNodeInfo<String>(categories, categoryCell,
            selectionModel, null);
      } else if (value instanceof String) {
        // Second level.
        return new DefaultNodeInfo<EmployeeRecord>(employees, employeeCell,
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

  /**
   * The adapter that provides categories.
   */
  private ListViewAdapter<String> categories = new ListViewAdapter<String>();

  /**
   * The adapter that provides employees.
   */
  private ListViewAdapter<EmployeeRecord> employees = new ListViewAdapter<EmployeeRecord>();

  /**
   * The last selected category.
   */
  private String lastCategory;

  /**
   * The last selected category.
   */
  private EmployeeRecord lastEmployee;

  /**
   * The listener of this widget.
   */
  private Listener listener;

  /**
   * The shared {@link SingleSelectionModel}.
   */
  private final SingleSelectionModel<Object> selectionModel = new SingleSelectionModel<Object>();

  /**
   * The main widget.
   */
  private CellTree tree;

  public ExpenseTree() {
    createTree();
    initWidget(tree);

    // Initialize the categories.
    List<String> categoriesList = categories.getList();
    categoriesList.add("All");
    categoriesList.add("Sales");
    categoriesList.add("Marketing");
    categoriesList.add("Engineering");
  }

  public void onEmployeeChanged(EmployeeRecordChanged event) {
    employees.refresh();
  }

  public void onSuccess(List<EmployeeRecord> response) {
    employees.setList(response);
  }

  public void setListener(Listener listener) {
    this.listener = listener;
  }

  /**
   * Create the {@link CellTree}.
   */
  private void createTree() {
    // Listen for selection. We need to add this handler before the CellBrowser
    // adds its own handler.
    selectionModel.addSelectionChangeHandler(new SelectionChangeHandler() {
      public void onSelectionChange(SelectionChangeEvent event) {
        Object selected = selectionModel.getSelectedObject();
        if (selected == null) {
          lastEmployee = null;
          lastCategory = null;
        } else if (selected instanceof EmployeeRecord) {
          lastEmployee = (EmployeeRecord) selected;
        } else if (selected instanceof String) {
          lastEmployee = null;
          lastCategory = (String) selected;
        }

        if (listener != null) {
          listener.onSelection(lastCategory, lastEmployee);
        }
      }
    });
    selectionModel.setKeyProvider(new ProvidesKey<Object>() {
      public Object getKey(Object item) {
        if (item instanceof EmployeeRecord) {
          return Expenses.EMPLOYEE_RECORD_KEY_PROVIDER.getKey((EmployeeRecord) item);
        }
        return item;
      }
    });

    // Create a CellBrowser.
    tree = new CellTree(new ExpensesTreeViewModel(), null);
    tree.setAnimationEnabled(true);
  }
}
