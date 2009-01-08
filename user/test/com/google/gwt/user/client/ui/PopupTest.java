/*
 * Copyright 2009 Google Inc.
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

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;

/**
 * Tests for {@link PopupPanel}.
 */
public class PopupTest extends GWTTestCase {

  /**
   * Expose otherwise private or protected methods.
   */
  private class TestablePopupPanel extends PopupPanel {
    private int onLoadCount;

    @Override
    public Element getContainerElement() {
      return super.getContainerElement();
    }

    public void assertOnLoadCount(int expected) {
      assertEquals(expected, onLoadCount);
    }

    @Override
    public void onLoad() {
      super.onLoad();
      onLoadCount++;
    }
  }

  @Override
  public String getModuleName() {
    return "com.google.gwt.user.User";
  }

  /**
   * Test the basic accessors.
   */
  public void testAccessors() {
    PopupPanel popup = createPopupPanel();

    // Animation enabled
    assertFalse(popup.isAnimationEnabled());
    popup.setAnimationEnabled(true);
    assertTrue(popup.isAnimationEnabled());

    // Modal
    popup.setModal(true);
    assertTrue(popup.isModal());
    popup.setModal(false);
    assertFalse(popup.isModal());

    // AutoHide enabled
    popup.setAutoHideEnabled(true);
    assertTrue(popup.isAutoHideEnabled());
    popup.setAutoHideEnabled(false);
    assertFalse(popup.isAutoHideEnabled());

    // PreviewAllNativeEvents enabled
    popup.setPreviewingAllNativeEvents(true);
    assertTrue(popup.isPreviewingAllNativeEvents());
    popup.setPreviewingAllNativeEvents(false);
    assertFalse(popup.isPreviewingAllNativeEvents());

    // setVisible
    assertTrue(popup.isVisible());
    popup.setVisible(false);
    assertFalse(popup.isVisible());
    popup.setVisible(true);
    assertTrue(popup.isVisible());

    // isShowing
    assertFalse(popup.isShowing());
    popup.show();
    assertTrue(popup.isShowing());
    popup.hide();
    assertFalse(popup.isShowing());
  }

  public void testAutoHidePartner() {
    final PopupPanel popup = new PopupPanel();

    // Add a partner
    DivElement partner0 = Document.get().createDivElement();
    popup.addAutoHidePartner(partner0);
    popup.addAutoHidePartner(Document.get().createDivElement());

    // Remove a partner
    popup.removeAutoHidePartner(partner0);
  }

  /**
   * Issue 2463: If a {@link PopupPanel} contains a dependent {@link PopupPanel}
   * that is hidden or shown in the onDetach or onAttach method, we could run
   * into conflicts with the animations. The {@link MenuBar} exhibits this
   * behavior because, when we detach a {@link MenuBar} from the page, it closes
   * all of its sub menus, each located in a different {@link PopupPanel}.
   */
  public void testDependantPopupPanel() {
    // Create the dependent popup
    final PopupPanel dependantPopup = createPopupPanel();
    dependantPopup.setAnimationEnabled(true);

    // Create the primary popup
    final PopupPanel primaryPopup = new PopupPanel(false, false) {
      @Override
      protected void onAttach() {
        dependantPopup.show();
        super.onAttach();
      }

      @Override
      protected void onDetach() {
        dependantPopup.hide();
        super.onDetach();
      }
    };
    primaryPopup.setAnimationEnabled(true);

    testDependantPopupPanel(primaryPopup);
  }

  /**
   * Test that the onLoad method is only called once when showing the popup.
   */
  public void testOnLoad() {
    TestablePopupPanel popup = new TestablePopupPanel();

    // show() without animation
    {
      popup.setAnimationEnabled(false);
      popup.show();
      popup.assertOnLoadCount(1);
      popup.hide();
    }

    // show() with animation
    {
      popup.setAnimationEnabled(true);
      popup.show();
      popup.assertOnLoadCount(2);
      popup.hide();
    }

    // center() without animation
    {
      popup.setAnimationEnabled(false);
      popup.center();
      popup.assertOnLoadCount(3);
      popup.hide();
    }

    // center() with animation
    {
      popup.setAnimationEnabled(true);
      popup.center();
      popup.assertOnLoadCount(4);
      popup.hide();
    }
  }

  public void testPopup() {
    // Get rid of window margins so we can test absolute position.
    Window.setMargin("0px");

    PopupPanel popup = createPopupPanel();
    popup.setAnimationEnabled(false);
    Label lbl = new Label("foo");

    // Make sure that setting the popup's size & position works _before_
    // setting its widget.
    popup.setSize("384px", "128px");
    popup.setPopupPosition(128, 64);
    popup.setWidget(lbl);
    popup.show();

    // DecoratorPanel adds width and height because it wraps the content in a
    // 3x3 table.
    assertTrue(popup.getOffsetWidth() >= 384);
    assertTrue(popup.getOffsetHeight() >= 128);
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

    // DecoratorPanel adds width and height because it wraps the content in a
    // 3x3 table.
    assertTrue(popup.getOffsetWidth() >= lbl.getOffsetWidth());
    assertTrue(popup.getOffsetWidth() >= lbl.getOffsetHeight());
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
    assertFalse(p1.getContainerElement() == p2.getContainerElement());
  }

  /**
   * Issue 2481: Try to set the contents of the popup while the popup is
   * attached. When we hide the popup, this should not leave the popup in an
   * invalid attach state.
   */
  public void testSetWidgetWhileAttached() {
    PopupPanel popup = createPopupPanel();
    popup.show();
    popup.setWidget(new Label("test"));
    popup.hide();
  }

  /**
   * Create a new PopupPanel.
   */
  protected PopupPanel createPopupPanel() {
    return new PopupPanel();
  }

  /**
   * @see #testDependantPopupPanel()
   */
  protected void testDependantPopupPanel(final PopupPanel primaryPopup) {
    // Show the popup
    primaryPopup.show();

    // Hide the popup
    new Timer() {
      @Override
      public void run() {
        primaryPopup.hide();
      }
    }.schedule(1000);

    // Give time for any errors to occur
    new Timer() {
      @Override
      public void run() {
        finishTest();
      }
    }.schedule(2000);

    delayTestFinish(5000);
  }
}
