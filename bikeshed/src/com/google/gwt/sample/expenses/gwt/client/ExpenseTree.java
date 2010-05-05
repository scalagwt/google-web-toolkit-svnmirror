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

import com.google.gwt.bikeshed.list.client.ListView;
import com.google.gwt.bikeshed.list.shared.AsyncListViewAdapter;
import com.google.gwt.bikeshed.list.shared.ListViewAdapter;
import com.google.gwt.bikeshed.list.shared.ProvidesKey;
import com.google.gwt.bikeshed.list.shared.Range;
import com.google.gwt.bikeshed.list.shared.SingleSelectionModel;
import com.google.gwt.bikeshed.list.shared.SelectionModel.SelectionChangeEvent;
import com.google.gwt.bikeshed.list.shared.SelectionModel.SelectionChangeHandler;
import com.google.gwt.bikeshed.tree.client.CellTree;
import com.google.gwt.bikeshed.tree.client.CellTreeViewModel;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.IconCellDecorator;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.requestfactory.shared.Receiver;
import com.google.gwt.sample.bikeshed.style.client.Styles;
import com.google.gwt.sample.expenses.gwt.request.EmployeeRecord;
import com.google.gwt.sample.expenses.gwt.request.ExpensesRequestFactory;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.valuestore.shared.Property;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The employee tree located on the left of the app.
 */
public class ExpenseTree extends Composite {

  /**
   * Custom listener for this widget.
   */
  public interface Listener {

    /**
     * Called when the user selects a tree item.
     * 
     * @param department the selected department name
     * @param employee the selected employee
     */
    void onSelection(String department, EmployeeRecord employee);
  }

  /**
   * A {@link AbstractCell} that represents an {@link EmployeeRecord}.
   */
  private class EmployeeCell extends IconCellDecorator<EmployeeRecord> {

    public EmployeeCell() {
      super(Styles.resources().userIcon(), new AbstractCell<EmployeeRecord>() {
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
   * The {@link ListViewAdapter} used for Employee lists.
   */
  private class EmployeeListViewAdapter extends
      AsyncListViewAdapter<EmployeeRecord> implements
      Receiver<List<EmployeeRecord>> {

    private final String department;

    public EmployeeListViewAdapter(String department) {
      this.department = department;
    }

    @Override
    public void addView(ListView<EmployeeRecord> view) {
      super.addView(view);

      // Request the count anytime a view is added.
      requestFactory.employeeRequest().countEmployeesByDepartment(department).to(
          new Receiver<Long>() {
            public void onSuccess(Long response) {
              updateDataSize(response.intValue(), true);
            }
          }).fire();
    }

    public void onSuccess(List<EmployeeRecord> response) {
      updateViewData(0, response.size(), response);
    }

    @Override
    protected void onRangeChanged(ListView<EmployeeRecord> view) {
      Range range = view.getRange();
      requestFactory.employeeRequest().findEmployeeEntriesByDepartment(
          department, range.getStart(), range.getLength()).forProperties(
          getEmployeeMenuProperties()).to(this).fire();
    }
  }

  /**
   * The {@link CellTreeViewModel} used to browse expense reports.
   */
  private class ExpensesTreeViewModel implements CellTreeViewModel {

    /**
     * The department cell singleton.
     */
    private final Cell<String> departmentCell = new IconCellDecorator<String>(
        Styles.resources().groupIcon(), new TextCell());

    /**
     * The {@link EmployeeCell} singleton.
     */
    private final EmployeeCell employeeCell = new EmployeeCell();

    public <T> NodeInfo<?> getNodeInfo(T value) {
      if (value == null) {
        // Top level.
        return new DefaultNodeInfo<String>(departments, departmentCell,
            selectionModel, null);
      } else if (isAllDepartment(value)) {
        // Employees are not displayed under the 'All' Department.
        return null;
      } else if (value instanceof String) {
        // Second level.
        EmployeeListViewAdapter adapter = new EmployeeListViewAdapter(
            (String) value);
        return new DefaultNodeInfo<EmployeeRecord>(adapter, employeeCell,
            selectionModel, null);
      }

      return null;
    }

    public boolean isLeaf(Object value) {
      return !isDepartment(value) || isAllDepartment(value);
    }

    /**
     * @return true if the object is the All department
     */
    private boolean isAllDepartment(Object value) {
      return departments.getList().get(0).equals(value);
    }

    /**
     * @return true if the object is a department
     */
    private boolean isDepartment(Object value) {
      return departments.getList().contains(value.toString());
    }
  }

  /**
   * The adapter that provides departments.
   */
  private ListViewAdapter<String> departments = new ListViewAdapter<String>();

  /**
   * The last selected department.
   */
  private String lastDepartment;

  /**
   * The last selected employee.
   */
  private EmployeeRecord lastEmployee;

  /**
   * The listener of this widget.
   */
  private Listener listener;

  /**
   * The factory used to send requests.
   */
  private ExpensesRequestFactory requestFactory;

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
    getElement().getStyle().setOverflow(Overflow.AUTO);

    // Initialize the departments.
    List<String> departmentList = departments.getList();
    departmentList.add("All");
    departmentList.add("Engineering");
    // The Finance department is empty.
    departmentList.add("Finance");
    departmentList.add("Marketing");
    departmentList.add("Operations");
    departmentList.add("Sales");
  }

  public void setListener(Listener listener) {
    this.listener = listener;
  }

  public void setRequestFactory(ExpensesRequestFactory factory) {
    this.requestFactory = factory;
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
          lastDepartment = null;
        } else if (selected instanceof EmployeeRecord) {
          lastEmployee = (EmployeeRecord) selected;
        } else if (selected instanceof String) {
          lastEmployee = null;
          lastDepartment = (String) selected;
        }

        if (listener != null) {
          listener.onSelection(lastDepartment, lastEmployee);
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

  private Collection<Property<?>> getEmployeeMenuProperties() {
    List<Property<?>> columns = new ArrayList<Property<?>>();
    columns.add(EmployeeRecord.displayName);
    columns.add(EmployeeRecord.userName);
    return columns;
  }
}
