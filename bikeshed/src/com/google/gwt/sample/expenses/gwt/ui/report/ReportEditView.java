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

import com.google.gwt.app.util.DateTimeFormatRenderer;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.sample.expenses.gwt.request.ReportRecord;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.valuestore.shared.DeltaValueStore;
import com.google.gwt.valuestore.shared.Property;
import com.google.gwt.valuestore.ui.RecordEditView;

import java.util.HashSet;
import java.util.Set;

/**
 * Edit view for employee records.
 */
public class ReportEditView extends Composite implements
    RecordEditView<ReportRecord> {
  interface Binder extends UiBinder<HTMLPanel, ReportEditView> {
  }

  private static final Binder BINDER = GWT.create(Binder.class);

  @UiField
  TextBox purpose;
  @UiField
  TextBox reporterKey;
  @UiField
  TextBox approvedSupervisorKey;
  @UiField
  SpanElement created; //TODO: use a DatePicker
  @UiField
  Button save;
  @UiField
  SpanElement idSpan;
  @UiField
  SpanElement version;

  private Delegate delegate;
  private DeltaValueStore deltas;
  
  private ReportRecord record;

  public ReportEditView() {
    initWidget(BINDER.createAndBindUi(this));
  }

  public ReportEditView asWidget() {
    return this;
  }

  public Set<Property<?>> getProperties() {
    Set<Property<?>> rtn = new HashSet<Property<?>>();
    rtn.add(ReportRecord.purpose);
    rtn.add(ReportRecord.created);
    rtn.add(ReportRecord.reporterKey);
    rtn.add(ReportRecord.approvedSupervisorKey);
    return rtn;
  }

  public void setDelegate(Delegate delegate) {
    this.delegate = delegate;
  }

  public void setDeltaValueStore(DeltaValueStore deltas) {
    this.deltas = deltas;
  }

  public void setEnabled(boolean enabled) {
    purpose.setEnabled(enabled);
    save.setEnabled(enabled);
  }

  public void setValue(ReportRecord value) {
    this.record = value;
    purpose.setValue(record.getPurpose());
    reporterKey.setValue(record.getReporterKey());
    approvedSupervisorKey.setValue(record.getApprovedSupervisorKey());
    created.setInnerText(new DateTimeFormatRenderer(
        DateTimeFormat.getShortDateFormat()).render(record.getCreated()));
    idSpan.setInnerText(record.getId());
    version.setInnerText(record.getVersion().toString());
  }
  
  @UiHandler("approvedSupervisorKey")
  void onApprovedSupervisorKeyChange(ValueChangeEvent<String> event) {
    deltas.set(ReportRecord.approvedSupervisorKey, record, event.getValue());
  }
  @UiHandler("purpose")
  void onPurposeChange(ValueChangeEvent<String> event) {
    deltas.set(ReportRecord.purpose, record, event.getValue());
  }
  @UiHandler("reporterKey")
  void onReporterKeyChange(ValueChangeEvent<String> event) {
    deltas.set(ReportRecord.reporterKey, record, event.getValue());
  }

  @UiHandler("save")
  void onSave(@SuppressWarnings("unused") ClickEvent event) {
    delegate.saveClicked();
  }

}
