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

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.sample.expenses.gwt.request.ReportRecord;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Details view for employee records.
 */
public class ReportDetailsView extends Composite implements ReportDetailsActivity.View {
  interface Binder extends UiBinder<HTMLPanel, ReportDetailsView> {
  }

  private static final Binder BINDER = GWT.create(Binder.class);

  @UiField
  SpanElement idSpan;
  @UiField
  SpanElement versionSpan;
  @UiField
  SpanElement purpose;
  @UiField
  SpanElement created;
  @UiField
  SpanElement reporterKey;
  @UiField
  SpanElement approvedSupervisorKey;

  public ReportDetailsView() {
    initWidget(BINDER.createAndBindUi(this));
  }

  public Widget asWidget() {
    return this;
  }

  public void setValue(ReportRecord record) {
    purpose.setInnerText(record.getPurpose());
    created.setInnerText(DateTimeFormat.getShortDateFormat().format(record.getCreated()));
    idSpan.setInnerText(record.getId());
    versionSpan.setInnerText(record.getVersion().toString());
    reporterKey.setInnerText(record.getReporterKey());
    approvedSupervisorKey.setInnerText(record.getApprovedSupervisorKey());
  }

}
