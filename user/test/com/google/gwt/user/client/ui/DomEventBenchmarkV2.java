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
import com.google.gwt.benchmarks.client.RangeEnum;
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
 * the old event handlersOrRegistrations and the new event handlers. This first version, of
 * course, can only look at handlersOrRegistrations, as handlers aren't here yet.
 * <p>
 * This class is redundant with DomEventBenchmark (thus V2), and was made this
 * way to simplify branch merging. TODO(rjrjr) This should be resolved when the
 * 1_6_event branch is merged in to releases/1.6
 */
@SuppressWarnings("deprecation")
public class DomEventBenchmarkV2 extends Benchmark {

  /**
   * Whether to use old handlersOrRegistrations or new handlers.
   */
  protected enum RegistrationStyle {
    OLD_LISTENERS, NEW_HANDLERS
  }

  private static final int NUM_WIDGETS = 250;

  protected final IntRange listenerRange =
      new IntRange(4, 400, Operator.MULTIPLY, 10);

  private List<SimpleCheckBox> widgets;
  private List<ClickListener> listeners;
  private List<ClickHandler> handlers;
  private List<HandlerRegistration> registrations;

  @Override
  public String getModuleName() {
    return "com.google.gwt.user.User";
  }

  // Required for JUnit
  public void testSimpleCheckBoxAddAndRemoveForClicks() {
  }

  @Setup("reset") 
  public void testSimpleCheckBoxAddAndRemoveForClicks(
      @RangeEnum(RegistrationStyle.class) RegistrationStyle style, 
      @RangeField("listenerRange") Integer numListeners) {

    // The RegistrationStyle blank is here to be filled in when handlers arrive.
    // Until then, just run the tests twice.

    if (RegistrationStyle.OLD_LISTENERS == style) {
      for (SimpleCheckBox cb : widgets) {
        for (int i = 0; i < numListeners; i++) {
          cb.addClickListener(listeners.get(i));
        }
      }
    } else {
      for (SimpleCheckBox cb : widgets) {
        for (int i = 0; i < numListeners; i++) {
          HandlerRegistration r = cb.addClickHandler(handlers.get(i));
          registrations.add(r);
        }
      }
    }
    if (RegistrationStyle.OLD_LISTENERS == style) {
      for (SimpleCheckBox cb : widgets) {
        for (int i = 0; i < numListeners; i++) {
          cb.removeClickListener(listeners.get(i));
        }
      }
    } else {
      for (SimpleCheckBox cb : widgets) {
        for (int i = 0; i < numListeners; i++) {
          registrations.get(i).removeHandler();
        }
      }
    }
  }

  // Required for JUnit
  public void testSimpleCheckBoxAddForClicks() {
  }

  @Setup("reset") 
  public void testSimpleCheckBoxAddForClicks(
      @RangeEnum(RegistrationStyle.class) RegistrationStyle style, 
      @RangeField("listenerRange") Integer numListeners) {

    // The RegistrationStyle blank is here to be filled in when handlers arrive.
    // Until then, just run the tests twice.

    if (RegistrationStyle.OLD_LISTENERS == style) {
      for (SimpleCheckBox cb : widgets) {
        for (int i = 0; i < numListeners; i++) {
          cb.addClickListener(listeners.get(i));
        }
      }
    } else {
      for (SimpleCheckBox cb : widgets) {
        for (int i = 0; i < numListeners; i++) {
          HandlerRegistration r = cb.addClickHandler(handlers.get(i));
          registrations.add(r);
        }
      }
    }
  }

  void reset(RegistrationStyle style, Integer numListeners) {
    RootPanel root = RootPanel.get();
    root.clear();
    widgets = new ArrayList<SimpleCheckBox>();
    listeners = new ArrayList<ClickListener>();
    handlers = new ArrayList<ClickHandler>();
    registrations = new ArrayList<HandlerRegistration>();

    for (int i = 0; i < NUM_WIDGETS; i++) {
      SimpleCheckBox cb = new SimpleCheckBox();
      widgets.add(cb);
      root.add(cb);
    }

    for (int i = 0; i < numListeners; i++) {
      if (RegistrationStyle.OLD_LISTENERS == style) {
        listeners.add(new ClickListener() {
          public void onClick(Widget sender) { }
          public String toString() { return "listener"; }
        });
      } else {
        handlers.add(new ClickHandler() {
          public void onClick(ClickEvent event) { }
          public String toString() { return "handler"; }
        });
      }
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
