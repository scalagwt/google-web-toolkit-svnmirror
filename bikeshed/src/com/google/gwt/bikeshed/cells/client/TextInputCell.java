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
package com.google.gwt.bikeshed.cells.client;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.NativeEvent;

/**
 * A {@link Cell} used to render a text input.
 */
public class TextInputCell extends Cell<String> {

  @Override
  public boolean consumesEvents() {
    return true;
  }

  @Override
  public Object onBrowserEvent(Element parent, String value, Object viewData,
      NativeEvent event, ValueUpdater<String> valueUpdater) {
    if (valueUpdater != null && "change".equals(event.getType())) {
      InputElement input = parent.getFirstChild().cast();
      valueUpdater.update(input.getValue());
    }

    return viewData;
  }

  @Override
  public void render(String data, Object viewData, StringBuilder sb) {
    sb.append("<input type='text'");
    if (data != null) {
      sb.append(" value='" + data + "'");
    }
    sb.append("></input>");
  }
}
