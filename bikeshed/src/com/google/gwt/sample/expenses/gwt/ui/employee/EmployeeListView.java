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
package com.google.gwt.sample.expenses.gwt.ui.employee;

import com.google.gwt.bikeshed.list.client.CellTable;
import com.google.gwt.core.client.GWT;
import com.google.gwt.sample.expenses.gwt.request.EmployeeRecord;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.valuestore.ui.AbstractRecordListView;
import com.google.gwt.valuestore.ui.PropertyColumn;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link AbstractRecordListView} specialized to {@link EmployeeKey} values.
 */
public class EmployeeListView extends AbstractRecordListView<EmployeeRecord> {
  interface Binder extends UiBinder<HTMLPanel, EmployeeListView> {
  }

  private static final Binder BINDER = GWT.create(Binder.class);

  @UiField CellTable<EmployeeRecord> table;
  @UiField Button newButton;

  public EmployeeListView() {
    init(BINDER.createAndBindUi(this), table, newButton, getColumns());
  }

  protected List<PropertyColumn<EmployeeRecord, ?>> getColumns() {
    // TODO These should be <g:col> elements in a <g:table> in the ui.xml file

    List<PropertyColumn<EmployeeRecord, ?>> columns = new ArrayList<PropertyColumn<EmployeeRecord, ?>>();

    columns.add(PropertyColumn.<EmployeeRecord> getStringPropertyColumn(EmployeeRecord.userName));
    columns.add(PropertyColumn.<EmployeeRecord> getStringPropertyColumn(EmployeeRecord.displayName));
    columns.add(PropertyColumn.<EmployeeRecord> getStringPropertyColumn(EmployeeRecord.password));
    columns.add(PropertyColumn.<EmployeeRecord> getStringPropertyColumn(EmployeeRecord.supervisorKey));

    return columns;
  }
}
