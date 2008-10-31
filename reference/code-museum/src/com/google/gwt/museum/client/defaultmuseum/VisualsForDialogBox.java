/*
 * Copyright 2008 Google Inc.
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
package com.google.gwt.museum.client.defaultmuseum;

import com.google.gwt.dom.client.Element;
import com.google.gwt.museum.client.common.AbstractIssue;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

import java.util.HashMap;
import java.util.Map;

/**
 * Verify that events fire in all browsers.
 */
public class VisualsForDialogBox extends AbstractIssue {

  /**
   * The main grid used for layout.
   */
  private FlexTable layout = null;

  enum Event {
    mouseDown, mouseEnter, mouseLeave, mouseMove, mouseUp
  }

  final Map<Event, Element> eventToElement = new HashMap<Event, Element>();

  @Override
  public Widget createIssue() {
    layout = new FlexTable();
    layout.setCellPadding(3);
    layout.setBorderWidth(2);
    layout.setHTML(0, 0, "<b>Event</b>");
    layout.setHTML(0, 1, "<b>Status</b>");

    for (Event e : Event.values()) {
      eventToElement.put(e, addResultRow(e.name()));
    }

    final DialogBox dialog = new DialogBox() {

      @Override
      public void onMouseDown(Widget sender, int x, int y) {
        pass(Event.mouseDown);
        super.onMouseDown(sender, x, y);
      }

      @Override
      public void onMouseEnter(Widget sender) {
        pass(Event.mouseEnter);
        super.onMouseEnter(sender);
      }

      @Override
      public void onMouseLeave(Widget sender) {
        pass(Event.mouseLeave);
        super.onMouseLeave(sender);
      }

      @Override
      public void onMouseMove(Widget sender, int x, int y) {
        pass(Event.mouseMove);
        super.onMouseMove(sender, x, y);
      }

      @Override
      public void onMouseUp(Widget sender, int x, int y) {
        pass(Event.mouseUp);
        super.onMouseUp(sender, x, y);
      }

    };
    dialog.setModal(false);
    dialog.setText("Click here");
    dialog.add(layout);
    
    dialog.center();

    return new HTML() {
      @Override
      protected void onDetach() {
        dialog.hide();
      }
    };
  }

  @Override
  public String getInstructions() {
    return "Confirm that the events listed happen when you click on the "
        + "dialog's caption.";
  }

  @Override
  public String getSummary() {
    return "Legacy mouse event callbacks fire";
  }

  @Override
  public boolean hasCSS() {
    return false;
  }
  
  private Element addResultRow(String eventName) {
    int row = layout.getRowCount();
    layout.setHTML(row, 0, eventName);
    layout.setHTML(row, 1, "<span style='color:red'>?</span>");
    Element cell = layout.getCellFormatter().getElement(row, 1);
    return cell;
  }

  private void pass(Event event) {
    eventToElement.get(event).setInnerHTML(
        "<span style='color:green'>pass</span>");
  }
}
