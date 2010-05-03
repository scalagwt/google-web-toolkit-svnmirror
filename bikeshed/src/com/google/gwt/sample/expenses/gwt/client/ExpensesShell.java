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

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * UI shell for expenses sample app.
 */
public class ExpensesShell extends Composite {

  interface ShellUiBinder extends UiBinder<Widget, ExpensesShell> {
  }

  private static ShellUiBinder uiBinder = GWT.create(ShellUiBinder.class);

  @UiField
  ExpenseDetails expenseDetails;
  @UiField
  ExpenseList expenseList;
  @UiField
  ExpenseTree expenseTree;
  @UiField
  LayoutPanel layoutPanel;
  @UiField
  SplitLayoutPanel splitLayout;

  public ExpensesShell() {
    initWidget(uiBinder.createAndBindUi(this));
    splitLayout.setWidgetMinSize(expenseTree, 150);

    // Handle breadcrumb events from Expense Details.
    expenseDetails.getReportsLink().addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        showExpenseDetails(false);
      }
    });
  }

  public ExpenseDetails getExpenseDetails() {
    return expenseDetails;
  }

  public ExpenseList getExpenseList() {
    return expenseList;
  }

  public ExpenseTree getExpenseTree() {
    return expenseTree;
  }

  /**
   * Show or hide the expense details. When showing, the expense list is hidden.
   * 
   * @param isShowing true to show details, false to show reports list
   */
  public void showExpenseDetails(boolean isShowing) {
    if (isShowing) {
      layoutPanel.setWidgetLeftWidth(expenseList, 0.0, Unit.PX, 0.0, Unit.PX);
      layoutPanel.setWidgetLeftRight(expenseDetails, 0.0, Unit.PX, 0.0, Unit.PX);
    } else {
      layoutPanel.setWidgetLeftRight(expenseList, 0.0, Unit.PX, 0.0, Unit.PX);
      layoutPanel.setWidgetRightWidth(expenseDetails, 0.0, Unit.PX, 0.0,
          Unit.PX);
    }
    layoutPanel.animate(300);
  }
}
