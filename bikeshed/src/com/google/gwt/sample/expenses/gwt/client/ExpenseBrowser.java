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
import com.google.gwt.bikeshed.tree.client.CellBrowser;
import com.google.gwt.bikeshed.tree.client.CellTreeViewModel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.requestfactory.shared.Receiver;
import com.google.gwt.sample.bikeshed.style.client.Styles;
import com.google.gwt.sample.expenses.gwt.request.EmployeeRecord;
import com.google.gwt.sample.expenses.gwt.request.EmployeeRecordChanged;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import java.util.List;

/**
 * The browser at the top of the app used to browse expense reports.
 */
public class ExpenseBrowser extends Composite implements
    Receiver<List<EmployeeRecord>>, EmployeeRecordChanged.Handler {

  private static ExpenseBrowserUiBinder uiBinder = GWT.create(ExpenseBrowserUiBinder.class);

  /**
   * Custom listener for this widget.
   */
  public interface Listener {

    /**
     * Called when the user browses for something.
     * 
     * @param category the selected category name
     * @param employee the selected employee
     */
    void onBrowse(String category, EmployeeRecord employee);
  }

  interface ExpenseBrowserUiBinder extends UiBinder<Widget, ExpenseBrowser> {
  }

  /**
   * A {@link Cell} that represents an {@link EmployeeRecord}.
   */
  private class CategoryCell extends IconCellDecorator<String> {

    public CategoryCell() {
      super(Styles.resources().groupIcon(), TextCell.getInstance());
    }

    @Override
    public boolean dependsOnSelection() {
      return true;
    }

    @Override
    protected boolean isIconUsed(String value) {
      return value == null ? false : value.equals(lastCategory);
    }
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

    @Override
    public boolean dependsOnSelection() {
      return true;
    }

    @Override
    protected boolean isIconUsed(EmployeeRecord value) {
      return lastEmployee == null || value == null ? false
          : lastEmployee.getId().equals(value.getId());
    }
  }

  /**
   * The {@link CellTreeViewModel} used to browse expense reports.
   */
  private class ExpensesTreeViewModel implements CellTreeViewModel {

    /**
     * The {@link CategoryCell} singleton.
     */
    private final CategoryCell categoryCell = new CategoryCell();

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

  @UiField
  CellBrowser browser;

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

  private Listener listener;

  /**
   * The shared {@link SingleSelectionModel}.
   */
  private final SingleSelectionModel<Object> selectionModel = new SingleSelectionModel<Object>();

  public ExpenseBrowser() {
    initWidget(uiBinder.createAndBindUi(this));

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

  @UiFactory
  CellBrowser createBrowser() {
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
        browse();
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
    CellBrowser view = new CellBrowser(new ExpensesTreeViewModel(), null);
    view.setAnimationEnabled(true);
    return view;
  }

  /**
   * Browse based on the current selection.
   */
  private void browse() {
    if (listener != null) {
      listener.onBrowse(lastCategory, lastEmployee);
    }
  }
}
