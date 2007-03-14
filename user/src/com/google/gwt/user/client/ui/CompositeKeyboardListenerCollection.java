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
 * {@link KeyboardListenerCollection} used to correctly hook up event listeners
 * to the composite's wrapped widget.
 * 
 */
class CompositeKeyboardListenerCollection extends KeyboardListenerCollection
    implements KeyboardListener {

  private Composite composite;

  public CompositeKeyboardListenerCollection(Composite composite) {
    this.composite = composite;
    SourcesKeyboardEvents wrappedWidget = (SourcesKeyboardEvents) composite.getWidget();
    wrappedWidget.addKeyboardListener(this);
  }

  public void onKeyDown(Widget sender, char keyCode, int modifiers) {
    fireKeyDown(composite, keyCode, modifiers);
  }

  public void onKeyPress(Widget sender, char keyCode, int modifiers) {
    fireKeyPress(composite, keyCode, modifiers);
  }

  public void onKeyUp(Widget sender, char keyCode, int modifiers) {
    fireKeyPress(composite, keyCode, modifiers);
  }
}
