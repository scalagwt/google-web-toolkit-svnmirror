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
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.museum.client.common.AbstractIssue;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import java.util.HashMap;
import java.util.Map;

/**
 * Verify that events fire in all browsers.
 */
public class VisualsForDialogBox extends AbstractIssue {

  enum Event {
    mouseDown, mouseEnter, mouseLeave, mouseMove, mouseUp
  }

  private final class VisibleDialogBox extends DialogBox {
    private FlexTable layout = null;

    private final Map<Event, Element> eventToElement =
        new HashMap<Event, Element>();

    private Boolean defeatDragging = false;

    public VisibleDialogBox() {
      this(false);
    }

    public VisibleDialogBox(boolean autoHide) {
      this(autoHide, true);
    }

    public VisibleDialogBox(boolean autoHide, boolean modal) {
      super(autoHide, modal);
      layout = new FlexTable();
      layout.setCellPadding(3);
      layout.setBorderWidth(2);
      layout.setHTML(0, 0, "<b>Event</b>");
      layout.setHTML(0, 1, "<b>Status</b>");

      for (Event e : Event.values()) {
        eventToElement.put(e, addResultRow(e.name()));
      }
      add(layout);
    }

    @Override
    public void onMouseDown(Widget sender, int x, int y) {
      pass(Event.mouseDown);
      if (!defeatDragging) {
        super.onMouseDown(sender, x, y);
      }
    }

    @Override
    public void onMouseEnter(Widget sender) {
      pass(Event.mouseEnter);
      if (!defeatDragging) {
        super.onMouseEnter(sender);
      }
    }

    @Override
    public void onMouseLeave(Widget sender) {
      pass(Event.mouseLeave);
      if (!defeatDragging) {
        super.onMouseLeave(sender);
      }
    }

    @Override
    public void onMouseMove(Widget sender, int x, int y) {
      pass(Event.mouseMove);
      if (!defeatDragging) {
        super.onMouseMove(sender, x, y);
      }
    }

    @Override
    public void onMouseUp(Widget sender, int x, int y) {
      pass(Event.mouseUp);
      super.onMouseUp(sender, x, y);
    }

    public void setDefeatDragging(Boolean defeatDragging) {
      this.defeatDragging = defeatDragging;
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

  private CheckBox defeatDragging;

  @Override
  public Widget createIssue() {
    final VisibleDialogBox dialog = showVisibleDialog();

    defeatDragging = new CheckBox("Defeat dragging");
    defeatDragging.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
      public void onValueChange(ValueChangeEvent<Boolean> event) {
        dialog.setDefeatDragging(event.getValue());
      }
    });
    
    SimplePanel panel = new SimplePanel() {
      @Override
      protected void onUnload() {
        dialog.hide();
      }
    };
    panel.add(defeatDragging);
    
    return panel;
  }

  @Override
  public String getInstructions() {
    return "Confirm that the events listed happen when you click on the "
        + "dialog's caption. Confirm that dragging is defeated when the "
        + "checkbox is selected.";
  }

  @Override
  public String getSummary() {
    return "Legacy mouse event callbacks fire";
  }

  @Override
  public boolean hasCSS() {
    return false;
  }

  private VisibleDialogBox showVisibleDialog() {
    final VisibleDialogBox dialog = new VisibleDialogBox();
    dialog.setModal(false);
    dialog.setText("I'm a god damn dialog");

    dialog.center();

    return dialog;
  }
}
