/*
 * Copyright 2007 Google Inc.
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
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;

/**
 * TODO: document me.
 */
public class PopupTest extends GWTTestCase {

  /**
   * Expose otherwise private or protected methods.
   */
  private class TestablePopupPanel extends PopupPanel {
    public Element getContainerElement() {
      return super.getContainerElement();
    }
  }

  public String getModuleName() {
    return "com.google.gwt.user.User";
  }

  public void testPopup() {
    // Get rid of window margins so we can test absolute position.
    Window.setMargin("0px");

    PopupPanel popup = new PopupPanel();
    Label lbl = new Label("foo");

    // Make sure that setting the popup's size & position works _before_
    // setting its widget.
    popup.setSize("384px", "128px");
    popup.setPopupPosition(128, 64);
    popup.setWidget(lbl);
    popup.show();

    assertEquals(384, popup.getOffsetWidth());
    assertEquals(128, popup.getOffsetHeight());
    assertEquals(128, popup.getPopupLeft());
    assertEquals(64, popup.getPopupTop());

    // Make sure that the popup returns to the correct position
    // after hiding and showing it.
    popup.hide();
    popup.show();
    assertEquals(128, popup.getPopupLeft());
    assertEquals(64, popup.getPopupTop());

    // Make sure that setting the popup's size & position works _after_
    // setting its widget (and that clearing its size properly resizes it to
    // its widget's size).
    popup.setSize("", "");
    popup.setPopupPosition(16, 16);

    assertEquals(lbl.getOffsetWidth(), popup.getOffsetWidth());
    assertEquals(lbl.getOffsetHeight(), popup.getOffsetHeight());
    assertEquals(16, popup.getAbsoluteLeft());
    assertEquals(16, popup.getAbsoluteTop());

    // Ensure that hiding the popup fires the appropriate events.
    delayTestFinish(1000);
    popup.addPopupListener(new PopupListener() {
      public void onPopupClosed(PopupPanel sender, boolean autoClosed) {
        finishTest();
      }
    });
    popup.hide();
  }
  
  public void testSeparateContainers() {
    TestablePopupPanel p1 = new TestablePopupPanel();
    TestablePopupPanel p2 = new TestablePopupPanel();
    assertTrue(p1.getContainerElement() != null);
    assertTrue(p2.getContainerElement() != null);
    assertFalse(
        DOM.compare(p1.getContainerElement(), p2.getContainerElement()));
  }
}
