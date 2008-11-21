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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.museum.client.common.AbstractIssue;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;

import java.util.Date;

/**
 * Visuals for date box.
 */
public class VisualsForDateBox extends AbstractIssue {
  
  @Override
  public Widget createIssue() {
    return dateRange();
  }

  @Override
  public String getInstructions() {
    return "Click on first date box, see that date picker is displayed, use arrow keys to navigate to second date box, select a date";
  }

  @Override
  public String getSummary() {
   return "date box visual test";
  }

  @Override
  public boolean hasCSS() {
    return false;
  }

  private Widget dateRange() {
    VerticalPanel v = new VerticalPanel();
    HorizontalPanel p = new HorizontalPanel();
    v.add(p);
    final DateBox start = new DateBox();
    start.setWidth("15em");
    final DateBox end = new DateBox();
    end.setWidth("15em");
    start.setAnimationEnabled(true);

    end.setAnimationEnabled(true);

    start.addKeyDownHandler(new KeyDownHandler() {
      public void onKeyDown(KeyDownEvent e) {
        if (e.getNativeKeyCode() == KeyCodes.KEY_RIGHT
            && start.getCursorPos() == start.getText().length()) {
          start.hideDatePicker();
          end.setFocus(true);
        }
      }
    });

    end.addKeyDownHandler(new KeyDownHandler() {
      public void onKeyDown(KeyDownEvent e) {
        if ((e.getNativeKeyCode() == KeyCodes.KEY_LEFT)
            && end.getCursorPos() == 0) {
          start.setFocus(true);
          end.hideDatePicker();
        }
      }
    });

    end.getDatePicker().addValueChangeHandler(new ValueChangeHandler<Date>() {
      public void onValueChange(ValueChangeEvent<Date> event) {
        start.removeStyleName("user-modified");
      }
    });

    start.showDate(new Date());

    p.add(start);
    Label l = new Label(" - ");
    l.setStyleName("filler");
    p.add(l);
    p.add(end);
    HorizontalPanel h2 = new HorizontalPanel();
    v.add(h2);
    h2.add(new Button("Short format", new ClickHandler() {
      public void onClick(ClickEvent event) {
        start.setDateFormat(DateTimeFormat.getShortDateFormat());
        end.setDateFormat(DateTimeFormat.getShortDateFormat());
      }
    }));
    h2.add(new Button("Long format", new ClickHandler() {

      public void onClick(ClickEvent event) {
        start.setDateFormat(DateTimeFormat.getLongDateFormat());
        end.setDateFormat(DateTimeFormat.getLongDateFormat());
      }
    }));

    h2.add(new Button("clear", new ClickHandler() {
      public void onClick(ClickEvent sender) {
        start.clear();
        end.clear();
      }
    }));
    return v;
  }
}
