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

import com.google.gwt.event.dom.client.AllKeyHandlers;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.AbstractEvent;
import com.google.gwt.museum.client.common.AbstractIssue;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.FocusListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

import java.util.Arrays;
import java.util.List;

/**
 * A simple test for suggest box events.
 */
public class VisualsForSuggestBoxEvents extends AbstractIssue {
  static int xPos = 0;
  VerticalPanel report = new VerticalPanel();

  @Override
  public Widget createIssue() {
    VerticalPanel p = new VerticalPanel();

    p.add(createSuggestBox("suggest 1"));
    p.add(createSuggestBox("suggest 2"));
    p.add(report);
    report("reporting");
    return p;
  }

  @Override
  public String getInstructions() {
    return "Select suggestions from suggest box, check report for events being fired";
  }

  @Override
  public String getSummary() {
    return "suggest box event visual test";
  }

  @Override
  public boolean hasCSS() {
    return false;
  }

  SuggestBox createSuggestBox(final String suggestBoxName) {

    List<String> femaleNames = Arrays.asList(new String[] {
        "Jamie", "Jill", "Jackie", "Susan", "Helen", "Emily", "Karen",
        "Abigail", "Kaitlyn", "Laura", "Joanna", "Tasha"});
    MultiWordSuggestOracle oracle = new MultiWordSuggestOracle();
    oracle.addAll(femaleNames);
    SuggestBox b = new SuggestBox(oracle) {
      @Override
      public String toString() {
        return suggestBoxName;
      }
    };

    class MyHandler extends AllKeyHandlers implements ChangeListener,
        FocusListener, ValueChangeHandler<String>, SelectionHandler<Suggestion> {

      public void onChange(Widget sender) {
        report("change: " + sender.getClass());
      }

      public void onFocus(Widget sender) {
        report("focus: " + sender.getClass());
      }

      public void onKeyDown(KeyDownEvent event) {
        report(event);
      }

      public void onKeyPress(KeyPressEvent event) {
        report(event);
      }

      public void onKeyUp(KeyUpEvent event) {
        report(event);
      }

      public void onLostFocus(Widget sender) {
        report("blur: " + sender.getClass());
      }

      public void onSelection(SelectionEvent<Suggestion> event) {
        report(event);
      }

      public void onValueChange(ValueChangeEvent<String> event) {
        report(event);
      }
    }
    MyHandler handler = new MyHandler();
    b.addKeyDownHandler(handler);
    b.addKeyUpHandler(handler);
    b.addKeyPressHandler(handler);
    b.addChangeListener(handler);
    b.addFocusListener(handler);
    b.addSelectionHandler(handler);
    b.addValueChangeHandler(handler);
    return b;
  }

  private void report(AbstractEvent event) {
    report(event.toDebugString());
  }

  // will be replaced by logging
  private void report(String s) {
    System.err.println("fobar");
    report.insert(new Label(s), 0);
    if (report.getWidgetCount() == 10) {
      report.remove(9);
    }
  }
}