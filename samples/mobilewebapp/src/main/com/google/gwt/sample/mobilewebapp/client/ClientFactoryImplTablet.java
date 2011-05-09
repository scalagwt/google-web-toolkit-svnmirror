/*
 * Copyright 2011 Google Inc.
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
package com.google.gwt.sample.mobilewebapp.client;

import com.google.gwt.sample.mobilewebapp.client.activity.TaskEditView;
import com.google.gwt.sample.mobilewebapp.client.activity.TaskListView;
import com.google.gwt.sample.mobilewebapp.client.mobile.MobileTaskListView;
import com.google.gwt.sample.mobilewebapp.client.tablet.MobileWebAppShellTablet;
import com.google.gwt.sample.mobilewebapp.client.tablet.TabletTaskEditView;
import com.google.gwt.sample.mobilewebapp.client.ui.OrientationHelper;
import com.google.gwt.sample.mobilewebapp.client.ui.WindowBasedOrientationHelper;

/**
 * Tablet version of {@link ClientFactory}.
 */
public class ClientFactoryImplTablet extends ClientFactoryImpl {
  private final OrientationHelper orientationHelper = new WindowBasedOrientationHelper();

  @Override
  protected MobileWebAppShell createShell() {
    return new MobileWebAppShellTablet(getEventBus(), orientationHelper, getPlaceController(),
        getTaskListActivityProvider(), getTaskListView());
  }

  @Override
  protected TaskEditView createTaskEditView() {
    return new TabletTaskEditView();
  }

  @Override
  protected TaskListView createTaskListView() {
    // Use the mobile list view on tablets.
    return new MobileTaskListView();
  }

  @Override
  protected Provider<Boolean> getIsTaskListIncludedProvider() {
    /*
     * TODO(rjrjr) This is awkward. Seems like we should be wrapping the tablet
     * version of the ActivityMapper or something.
     */
    return new Provider<Boolean>() {
      @Override
      public Boolean get() {
        return !orientationHelper.isPortrait();
      }
    };
  }
}
