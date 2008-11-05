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
package com.google.gwt.user.client.ui;

import com.google.gwt.benchmarks.client.Benchmark;
import com.google.gwt.benchmarks.client.IntRange;
import com.google.gwt.benchmarks.client.Operator;
import com.google.gwt.benchmarks.client.RangeField;
import com.google.gwt.benchmarks.client.Setup;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;

import java.util.ArrayList;
import java.util.List;

/**
 * Measures the speed with which event handlers can be added and removed to a
 * few simple UI classes. This is here to allow us to compare the performance of
 * the old event handlersOrRegistrations and the new event handlers. This first
 * version, of course, can only look at handlersOrRegistrations, as handlers
 * aren't here yet.
 * <p>
 * This class is redundant with DomEventBenchmark (thus V2), and was made this
 * way to simplify branch merging. TODO(rjrjr) This should be resolved when the
 * 1_6_event branch is merged in to releases/1.6
 */
@SuppressWarnings("deprecation")
public class DomHandlerBenchmark extends Benchmark {

  protected final IntRange listenerRange = new IntRange(1, 10, Operator.ADD, 9);
  protected final IntRange widgetRange = new IntRange(100, 400, Operator.ADD,
      100);
  private List<CheckBox> widgets;

  private List<ClickHandler> handlers;

  private List<HandlerRegistration> registrations;

  @Override
  public String getModuleName() {
    return "com.google.gwt.user.User";
  }

  // Required for JUnit
  public void testCheckBoxAddForClicks() {
  }

  @Setup("reset")
  public void testCheckBoxAddForClicks(
      @RangeField("widgetRange") Integer numWidgets,
      @RangeField("listenerRange") Integer numListeners) {

    // The RegistrationStyle blank is here to be filled in when handlers arrive.
    // Until then, just run the tests twice.
    int numHandlers = numListeners;
    for (CheckBox cb : widgets) {
      for (int i = 0; i < numHandlers; i++) {
        cb.addClickHandler(handlers.get(i));
      }
    }
  }

  // Required for junit
  public void testMultiListenerAttachAndDetach() {
  }

  @Setup("resetWithHandlers")
  public void testMultiListenerAttachAndDetach(
      @RangeField("widgetRange") Integer numWidgets,
      @RangeField("listenerRange") Integer numListeners) {

    for (CheckBox cb : widgets) {
      RootPanel.get().add(cb);
    }
    RootPanel.get().clear();
  }

  // Required for JUnit
  public void testSimpleAttachAndDetach() {
  }

  @Setup("reset")
  public void testSimpleAttachAndDetach(
      @RangeField("widgetRange") Integer numWidgets,
      @RangeField("listenerRange") Integer numListeners) {

    // The RegistrationStyle blank is here to be filled in when handlers arrive.
    // Until then, just run the tests twice.

    for (CheckBox cb : widgets) {
      RootPanel.get().add(cb);
    }
    RootPanel.get().clear();
  }

  // Required for JUnit
  public void testCheckBoxAddAndRemoveForClicks() {
  }

  @Setup("reset")
  public void testCheckBoxAddAndRemoveForClicks(
      @RangeField("widgetRange") Integer numWidgets,
      @RangeField("listenerRange") Integer numListeners) {

    // The RegistrationStyle blank is here to be filled in when handlers arrive.
    // Until then, just run the tests twice.

    for (CheckBox cb : widgets) {
      for (int i = 0; i < numListeners; i++) {
        HandlerRegistration r = cb.addClickHandler(handlers.get(i));
        registrations.add(r);
      }
    }

    for (@SuppressWarnings("unused") CheckBox cb : widgets) {
      for (int i = 0; i < numListeners; i++) {
        registrations.get(i).removeHandler();
      }
    }
  }

  // Required for JUnit
  public void testCheckBoxCreate() {
  }

  public void testCheckBoxCreate(@RangeField("widgetRange") Integer numWidgets) {
    int max = numWidgets;
    RootPanel root = RootPanel.get();
    for (int i = 0; i < max; i++) {
      CheckBox cb = new CheckBox();
      root.add(cb);
    }
  }

  void resetWithHandlers(Integer numWidgets, Integer numListeners) {
    widgets = new ArrayList<CheckBox>();

    int max = numWidgets;
    for (int i = 0; i < max; i++) {
      CheckBox cb = new CheckBox();
      widgets.add(cb);
      for (int j = 0; j < numListeners; j++) {
        cb.addClickHandler(new ClickHandler() {
          public void onClick(ClickEvent event) {
          }

        });
      }
    }
  }

  void reset(Integer numWidgets, Integer numListeners) {
    widgets = new ArrayList<CheckBox>();
    handlers = new ArrayList<ClickHandler>();
    registrations = new ArrayList<HandlerRegistration>();
    int max = numWidgets;
    for (int i = 0; i < max; i++) {
      CheckBox cb = new CheckBox();
      widgets.add(cb);
    }

    for (int i = 0; i < numListeners; i++) {
      handlers.add(new ClickHandler() {
        public void onClick(ClickEvent event) {
        }
      });
    }
  }

  // /**
  // * Cannot do this until we fix our inability to synthesize events,
  // * pending...
  // */
  // public void testDispatch() {
  //     
  // }
}
