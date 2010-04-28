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
package com.google.gwt.sample.expenses.gwt.ui.report;

import com.google.gwt.app.place.AbstractActivity;
import com.google.gwt.app.util.IsWidget;
import com.google.gwt.sample.expenses.gwt.request.ExpensesRequestFactory;
import com.google.gwt.sample.expenses.gwt.request.ReportRecord;
import com.google.gwt.user.client.ui.TakesValue;
import com.google.gwt.user.client.ui.TakesValueList;
import com.google.gwt.valuestore.shared.Value;

import java.util.List;

/**
 * An {@link com.google.gwt.app.place.Activity Activity} that requests and
 * displays detailed information on a given report.
 */
public class ReportDetailsActivity extends AbstractActivity {
  class RequestCallBack implements TakesValueList<ReportRecord> {
    public void setValueList(List<ReportRecord> listOfOne) {
      view.setValue(listOfOne.get(0));
      callback.showActivityWidget(view);
    }
  }

  interface View extends TakesValue<ReportRecord>, IsWidget {
  }

  private static ReportDetailsView defaultView;

  private static ReportDetailsView getDefaultView() {
    if (defaultView == null) {
      defaultView = new ReportDetailsView();
    }
    return defaultView;
  }

  private final ExpensesRequestFactory requests;
  private final View view;
  private String id;
  private Display callback;

  /**
   * Creates an activity that uses the default singleton view instance.
   */
  public ReportDetailsActivity(String id, ExpensesRequestFactory requests) {
    this(id, requests, getDefaultView());
  }

  /**
   * Creates an activity that uses its own view instance.
   */
  public ReportDetailsActivity(String id, ExpensesRequestFactory requests, View view) {
    this.id = id;
    this.requests = requests;
    this.view = view;
  }

  public void start(Display callback) {
    this.callback = callback;
    requests.reportRequest().findReport(Value.of(id)).to(new RequestCallBack()).fire();
  }
}
