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

import com.google.gwt.app.client.EditorSupport;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.sample.expenses.gwt.request.ReportRecord;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.valuestore.shared.DeltaValueStore;
import com.google.gwt.valuestore.shared.Property;
import com.google.gwt.valuestore.ui.RecordEditView;

import java.util.Map;
import java.util.Set;

/**
 * Edit view for employee records.
 */
public class ReportEditView extends Composite implements
    RecordEditView<ReportRecord> {
  interface Binder extends UiBinder<HTMLPanel, ReportEditView> {
  }

  interface DataBinder extends EditorSupport<ReportRecord, ReportEditView> {
  }

  private static final Binder BINDER = GWT.create(Binder.class);
  private static final DataBinder DATA_BINDER = GWT.create(DataBinder.class);
  @UiField
  TextBox notes;
  @UiField
  TextBox purpose;
  @UiField
  TextBox reporterKey;
  @UiField
  TextBox approvedSupervisorKey;
  @UiField
  InlineLabel created; // TODO: use a DatePicker
  @UiField
  Button save;
  @UiField
  InlineLabel id;
  @UiField
  InlineLabel version;
  @UiField
  DivElement errors;

  private Delegate delegate;
  private DeltaValueStore deltas;

  private ReportRecord record;

  public ReportEditView() {
    initWidget(BINDER.createAndBindUi(this));
    DATA_BINDER.init(this);
  }

  public ReportEditView asWidget() {
    return this;
  }

  public DeltaValueStore getDeltaValueStore() {
    return deltas;
  }

  public Set<Property<?>> getProperties() {
    return DATA_BINDER.getProperties();
  }

  public ReportRecord getValue() {
    return record;
  }

  public void setDelegate(Delegate delegate) {
    this.delegate = delegate;
  }

  public void setDeltaValueStore(DeltaValueStore deltas) {
    this.deltas = deltas;
  }

  public void setEnabled(boolean enabled) {
    DATA_BINDER.setEnabled(this, enabled);
    save.setEnabled(enabled);
  }

  public void setValue(ReportRecord value) {
    this.record = value;
    DATA_BINDER.setValue(this, value);
  }

  @UiHandler("save")
  void onSave(@SuppressWarnings("unused") ClickEvent event) {
    delegate.saveClicked();
  }

  public void showErrors(Map<String, String> errorMap) {
    // TODO Make EditorSupport do this

    errors.setInnerText("");

    if (errorMap == null || errorMap.isEmpty()) {
      return;
    }

    Document doc = Document.get();
    for (Map.Entry<String, String> entry : errorMap.entrySet()) {
      /*
       * Note that we are careful not to use setInnerHtml, to ensure we don't
       * render user created markup: xsite attack protection
       */

      DivElement div = doc.createDivElement();
      div.setInnerText(" " + entry.getValue());

      SpanElement name = doc.createSpanElement();
      name.getStyle().setFontWeight(FontWeight.BOLD);
      name.setInnerText(entry.getKey());

      div.insertFirst(name);

      errors.appendChild(div);
    }
  }
}
