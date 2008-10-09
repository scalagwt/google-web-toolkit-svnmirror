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

import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;

/**
 * Tests for {@link Composite}.
 */
public class CompositeTest extends GWTTestCase {

  public String getModuleName() {
    return "com.google.gwt.user.User";
  }

  private static class EventTestComposite extends Composite {
    TextBox tb = new TextBox();
    boolean widgetFocusFired;
    boolean widgetLostFocusFired;
    boolean domFocusFired;
    boolean domBlurFired;

    public EventTestComposite() {
      initWidget(tb);
      sinkEvents(Event.FOCUSEVENTS);

      tb.addFocusListener(new FocusListener() {
        public void onLostFocus(Widget sender) {
          widgetLostFocusFired = true;
        }

        public void onFocus(Widget sender) {
          widgetFocusFired = true;
        }
      });
    }

    @Override
    public void onBrowserEvent(Event event) {
      switch (DOM.eventGetType(event)) {
        case Event.ONFOCUS:
          domFocusFired = true;
          // Eat the focus event.
          return;

        case Event.ONBLUR:
          domBlurFired = true;
          // *Don't* eat the blur event.
          break;
      }

      super.onBrowserEvent(event);
    }
  }

  public void testBrowserEvents() {
// TODO: re-enable this test when we figure out why the focus events aren't
// firing on some browsers.
//
//    final EventTestComposite c = new EventTestComposite();
//    RootPanel.get().add(c);
//
//    this.delayTestFinish(1000);
//
//    // Focus, then blur, the composite's text box. This has to be done in
//    // deferred commands, because focus events usually require the event loop
//    // to be pumped in order to fire.
//    DeferredCommand.addCommand(new Command() {
//      public void execute() {
//        DeferredCommand.addCommand(new Command() {
//          public void execute() {
//            // Ensure all events fired as expected.
//            assertTrue(c.domFocusFired);
//            assertTrue(c.domBlurFired);
//            assertTrue(c.widgetLostFocusFired);
//
//            // Ensure that the widget's focus event was eaten by the
//            // composite's implementation of onBrowserEvent().
//            assertFalse(c.widgetFocusFired);
//            finishTest();
//          }
//        });
//
//        c.tb.setFocus(false);
//      }
//    });
//
//    c.tb.setFocus(true);
  }
}
