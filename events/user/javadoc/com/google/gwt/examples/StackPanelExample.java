/*
 * Copyright 2006 Google Inc.
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
package com.google.gwt.examples;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.StackPanel;

public class StackPanelExample implements EntryPoint {

  public void onModuleLoad() {
    // Create a stack panel containing three labels.
    StackPanel panel = new StackPanel();
    panel.add(new Label("Foo"), "foo");
    panel.add(new Label("Bar"), "bar");
    panel.add(new Label("Baz"), "baz");

    // Add it to the root panel.
    RootPanel.get().add(panel);
  }
}
