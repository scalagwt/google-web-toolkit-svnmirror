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
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.sample.expenses.gwt.request.ExpenseRecord;
import com.google.gwt.sample.expenses.gwt.request.ExpensesRequestFactory;
import com.google.gwt.sample.expenses.gwt.request.ReportRecord;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

/**
 * TODO
 */
public class ExpensesMobileShell extends Composite implements Controller {

  interface ShellUiBinder extends UiBinder<Widget, ExpensesMobileShell> { }
  private static ShellUiBinder BINDER = GWT.create(ShellUiBinder.class);

  @UiField DeckPanel deck;
  @UiField HTML backButton, addButton, refreshButton;
  @UiField Element titleSpan;

  @UiField MobileReportList reportList;
  @UiField MobileExpenseList expenseList;
  @UiField MobileExpenseEntry expenseEntry;

  private final ExpensesRequestFactory requestFactory;
  private int curPage;
  private Page[] pages;

  public ExpensesMobileShell(ExpensesRequestFactory requestFactory) {
    this.requestFactory = requestFactory;
    initWidget(BINDER.createAndBindUi(this));

    pages = new Page[] {reportList, expenseList, expenseEntry};
    showPage(0);
  }

  public void showButtons(boolean back, boolean refresh, boolean add) {
    setVisible(backButton, back);
    setVisible(refreshButton, refresh);
    setVisible(addButton, add);
  }

  @UiFactory
  MobileExpenseList createExpenseList() {
    return new MobileExpenseList(new MobileExpenseList.Listener() {
      public void onExpenseSelected(ExpenseRecord expense) {
        expenseEntry.show(expense);
        showPage(2);
      }
    }, requestFactory);
  }

  @UiFactory
  MobileReportList createReportList() {
    return new MobileReportList(new MobileReportList.Listener() {
      public void onReportSelected(ReportRecord report) {
        expenseList.show(report);
        showPage(1);
      }
    }, requestFactory);
  }

  @UiHandler("addButton")
  void onAdd(ClickEvent evt) {
    pages[curPage].onAdd();
  }

  @UiHandler("backButton")
  void onBack(ClickEvent evt) {
    if (curPage > 0) {
      showPage(curPage - 1);
    }
  }

  @UiHandler("refreshButton")
  void onRefresh(ClickEvent evt) {
    pages[curPage].onRefresh();
  }

  private void setVisible(Widget widget, boolean visible) {
    widget.getElement().getStyle().setVisibility(
        visible ? Visibility.VISIBLE : Visibility.HIDDEN);
  }

  private void showPage(int idx) {
    if (curPage < 0 || curPage >= pages.length) {
      return;
    }

    curPage = idx;
    pages[idx].onShow(this);
    deck.showWidget(idx);
    titleSpan.setInnerText(pages[idx].getPageTitle());
  }
}
