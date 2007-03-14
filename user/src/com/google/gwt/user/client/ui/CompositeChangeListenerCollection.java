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

package com.google.gwt.user.client.ui;

/**
 * {@link ClickListenerCollection} used to correctly hook up event listeners to
 * the composite's wrapped widget.
 * 
 */
class CompositeChangeListenerCollection extends ChangeListenerCollection
    implements ChangeListener {

  private Composite composite;

  public CompositeChangeListenerCollection(Composite composite) {
    this.composite = composite;
    SourcesChangeEvents wrappedWidget = (SourcesChangeEvents) composite.getWidget();
    wrappedWidget.addChangeListener(this);
  }

  public void onChange(Widget sender) {
    super.fireChange(composite);
  }
}
