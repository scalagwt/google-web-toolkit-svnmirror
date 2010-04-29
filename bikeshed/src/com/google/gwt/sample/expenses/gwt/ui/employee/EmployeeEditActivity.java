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

import com.google.gwt.app.place.AbstractActivity;
import com.google.gwt.app.place.PlaceController;
import com.google.gwt.requestfactory.shared.Receiver;
import com.google.gwt.sample.expenses.gwt.client.place.ListScaffoldPlace;
import com.google.gwt.sample.expenses.gwt.client.place.ScaffoldPlace;
import com.google.gwt.sample.expenses.gwt.request.EmployeeRecord;
import com.google.gwt.sample.expenses.gwt.request.ExpensesRequestFactory;
import com.google.gwt.user.client.Window;
import com.google.gwt.valuestore.shared.DeltaValueStore;
import com.google.gwt.valuestore.shared.Value;
import com.google.gwt.valuestore.ui.RecordEditView;

/**
 * An activity that requests all info on an employee, allows the user to  edit it,
 * and persists the results. 
 */
public class EmployeeEditActivity extends AbstractActivity implements
    RecordEditView.Delegate {
  private static RecordEditView<EmployeeRecord> defaultView;

  private static RecordEditView<EmployeeRecord> getDefaultView() {
    if (defaultView == null) {
      defaultView = new EmployeeEditView();
    }
    return defaultView;
  }

  private final ExpensesRequestFactory requests;
  private final RecordEditView<EmployeeRecord> view;
  private final String id;
  private final PlaceController<ScaffoldPlace> placeController;

  private DeltaValueStore deltas;

  /**
   * Creates an activity that uses the default singleton view instance.
   */
  public EmployeeEditActivity(String id, ExpensesRequestFactory requests,
      PlaceController<ScaffoldPlace> placeController) {
    this(id, getDefaultView(), requests, placeController);
  }

  /**
   * Creates an activity that uses its own view instance.
   */
  public EmployeeEditActivity(String id, RecordEditView<EmployeeRecord> view,
      ExpensesRequestFactory requests,
      PlaceController<ScaffoldPlace> placeController) {
    this.requests = requests;
    this.id = id;
    this.view = view;
    this.deltas = requests.getValueStore().spawnDeltaView();
    this.placeController = placeController;
    view.setDelegate(this);
    view.setDeltaValueStore(deltas);
  }

  public void saveClicked() {
    if (deltas.isChanged()) {
      view.setEnabled(false);
      DeltaValueStore toCommit = deltas;
      deltas = null;
      requests.syncRequest(toCommit).fire(); // TODO Need callback, idiot
      placeController.goTo(new ListScaffoldPlace(EmployeeRecord.class));
    }
  }

  public void start(final Display display) {
    Receiver<EmployeeRecord> callback = new Receiver<EmployeeRecord>() {
      public void onSuccess(EmployeeRecord record) {
        view.setEnabled(true);
        view.setValue(record);
        display.showActivityWidget(view);
      }
    };
    requests.employeeRequest().findEmployee(Value.of(id)).to(callback).fire();
  }

  @Override
  public boolean willStop() {
    return deltas == null || !deltas.isChanged()
        || Window.confirm("Dude! Really drop your edits?");
  }
}
