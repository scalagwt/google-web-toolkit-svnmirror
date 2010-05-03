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
import com.google.gwt.app.place.PlaceController;
import com.google.gwt.requestfactory.shared.Receiver;
import com.google.gwt.requestfactory.shared.SyncResult;
import com.google.gwt.sample.expenses.gwt.client.place.ListScaffoldPlace;
import com.google.gwt.sample.expenses.gwt.client.place.ScaffoldPlace;
import com.google.gwt.sample.expenses.gwt.request.ExpensesRequestFactory;
import com.google.gwt.sample.expenses.gwt.request.ReportRecord;
import com.google.gwt.user.client.Window;
import com.google.gwt.valuestore.shared.DeltaValueStore;
import com.google.gwt.valuestore.shared.Value;
import com.google.gwt.valuestore.ui.RecordEditView;

import java.util.Set;

/**
 * An activity that requests all info on a report, allows the user to edit it,
 * and persists the results.
 */
public class ReportEditActivity extends AbstractActivity implements
    RecordEditView.Delegate {
  private static RecordEditView<ReportRecord> defaultView;

  private static RecordEditView<ReportRecord> getDefaultView() {
    if (defaultView == null) {
      defaultView = new ReportEditView();
    }
    return defaultView;
  }

  private final ExpensesRequestFactory requests;
  private final RecordEditView<ReportRecord> view;
  private final String id;
  private final PlaceController<ScaffoldPlace> placeController;

  private DeltaValueStore deltas;

  /**
   * Creates an activity that uses the default singleton view instance.
   */
  public ReportEditActivity(String id, ExpensesRequestFactory requests,
      PlaceController<ScaffoldPlace> placeController) {
    this(id, getDefaultView(), requests, placeController);
  }

  /**
   * Creates an activity that uses its own view instance.
   */
  public ReportEditActivity(String id, RecordEditView<ReportRecord> view,
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
      final DeltaValueStore toCommit = deltas;
      deltas = null;
      Receiver<Set<SyncResult>> receiver = new Receiver<Set<SyncResult>>() {
        public void onSuccess(Set<SyncResult> response) {
          boolean hasViolations = false;
          for (SyncResult syncResult : response) {
            if (syncResult.getRecord().getId().equals(id)) {
              if (syncResult.hasViolations()) {
                hasViolations = true;
                view.showErrors(syncResult.getViolations());
              }
            }
          }
          if (!hasViolations) {
            placeController.goTo(new ListScaffoldPlace(ReportRecord.class));
          } else {
            view.setEnabled(true);
            deltas = toCommit;
            deltas.clearUsed();
          }
        }
      };
      requests.syncRequest(toCommit).to(receiver).fire();
    }
  }

  public void start(final Display display) {
    Receiver<ReportRecord> callback = new Receiver<ReportRecord>() {
      public void onSuccess(ReportRecord record) {
        view.setEnabled(true);
        view.setValue(record);
        view.showErrors(null);
        display.showActivityWidget(view);
      }
    };
    requests.reportRequest().findReport(Value.of(id)).to(callback).fire();
  }

  @Override
  public boolean willStop() {
    return deltas == null || !deltas.isChanged()
        || Window.confirm("Dude! Really drop your edits?");
  }

}
