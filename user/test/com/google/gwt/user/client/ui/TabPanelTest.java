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

import java.util.Iterator;

/**
 * Tests the TabPanel.
 */
public class TabPanelTest extends GWTTestCase {

  static class Adder implements HasWidgetsTester.WidgetAdder {
    public void addChild(HasWidgets container, Widget child) {
      ((TabPanel) container).add(child, "foo");
    }
  }

  public String getModuleName() {
    return "com.google.gwt.user.User";
  }

  public void testAttachDetachOrder() {
    HasWidgetsTester.testAll(new TabPanel(), new Adder());
  }
  
  public void testInsertMultipleTimes() {
    TabPanel p = new TabPanel();

    TextBox tb = new TextBox();
    p.add(tb, "Title");
    p.add(tb, "Title");
    p.add(tb, "Title3");
    
    assertEquals(1, p.getWidgetCount());
    assertEquals(0, p.getWidgetIndex(tb));
    Iterator i = p.iterator();
    assertTrue(i.hasNext());
    assertTrue(tb.equals(i.next()));
    assertFalse(i.hasNext());
    
    Label l = new Label();
    p.add(l, "Title");
    p.add(l, "Title");
    p.add(l, "Title3");
    assertEquals(2, p.getWidgetCount());
    assertEquals(0, p.getWidgetIndex(tb));
    assertEquals(1, p.getWidgetIndex(l));
    
    p.insert(l, "Title", 0);
    assertEquals(2, p.getWidgetCount());
    assertEquals(0, p.getWidgetIndex(l));
    assertEquals(1, p.getWidgetIndex(tb));
    
    p.insert(l, "Title", 1);
    assertEquals(2, p.getWidgetCount());
    assertEquals(0, p.getWidgetIndex(l));
    assertEquals(1, p.getWidgetIndex(tb));

    p.insert(l, "Title", 2);
    assertEquals(2, p.getWidgetCount());
    assertEquals(0, p.getWidgetIndex(tb));
    assertEquals(1, p.getWidgetIndex(l));
  }

  public void testInsertWithHTML() {
    TabPanel p = new TabPanel();
    Label l = new Label();
    p.add(l, "three");
    p.insert(new HTML("<b>hello</b>"), "two", true, 0);
    p.insert(new HTML("goodbye"), "one", false, 0);
    assertEquals(3, p.getWidgetCount());
  }

  /**
   * Tests to ensure that arbitrary widgets can be added/inserted effectively.
   */
  public void testInsertWithWidgets() {
    TabPanel p = new TabPanel();

    TextBox wa = new TextBox();
    CheckBox wb = new CheckBox();
    VerticalPanel wc = new VerticalPanel();
    wc.add(new Label("First"));
    wc.add(new Label("Second"));

    p.add(new Label("Content C"), wc);
    p.insert(new Label("Content B"), wb, 0);
    p.insert(new Label("Content A"), wa, 0);

    // Call these to ensure we don't throw an exception.
    assertTrue(p.getTabBar().getTabHTML(0).length() > 0);
    assertTrue(p.getTabBar().getTabHTML(1).length() > 0);
    assertTrue(p.getTabBar().getTabHTML(2).length() > 0);
    assertEquals(3, p.getWidgetCount());
  }

  public void testIterator() {
    TabPanel p = new TabPanel();
    HTML foo = new HTML("foo");
    HTML bar = new HTML("bar");
    HTML baz = new HTML("baz");
    p.add(foo, "foo");
    p.add(bar, "bar");
    p.add(baz, "baz");

    // Iterate over the entire set and make sure it stops correctly.
    Iterator it = p.iterator();
    assertTrue(it.hasNext());
    assertTrue(it.next() == foo);
    assertTrue(it.hasNext());
    assertTrue(it.next() == bar);
    assertTrue(it.hasNext());
    assertTrue(it.next() == baz);
    assertFalse(it.hasNext());

    // Test removing using the iterator.
    it = p.iterator();
    it.next();
    it.remove();
    assertTrue(it.next() == bar);
    assertTrue(p.getWidgetCount() == 2);
    assertTrue(p.getWidget(0) == bar);
    assertTrue(p.getWidget(1) == baz);
  }

  public void testSelectionEvents() {
    TabPanel p = new TabPanel();
    RootPanel.get().add(p);

    p.add(new Button("foo"), "foo");
    p.add(new Button("bar"), "bar");

    // Make sure selecting a tab fires both events in the right order.
    p.addTabListener(new TabListener() {
      private boolean onBeforeFired;

      public boolean onBeforeTabSelected(SourcesTabEvents sender, int tabIndex) {
        onBeforeFired = true;
        return true;
      }

      public void onTabSelected(SourcesTabEvents sender, int tabIndex) {
        assertTrue(onBeforeFired);
        finishTest();
      }
    });

    this.delayTestFinish(1000);
    p.selectTab(1);
  }

  public void testUnmodifiableDeckPanelSubclasses() {
    TabPanel p = new TabPanel();
    DeckPanel d = p.getDeckPanel();
    
    try {
      d.add(new Label("No"));
      fail("Internal DeckPanel should not allow add() method");
    } catch (UnsupportedOperationException e) {
      // Expected behavior
    }
    
    try {
      d.insert(new Label("No"), 0);
      fail("Internal DeckPanel should not allow insert() method");
    } catch (UnsupportedOperationException e) {
      // Expected behavior
    }

    try {
      d.clear();
      fail("Internal DeckPanel should not allow clear() method");
    } catch (UnsupportedOperationException e) {
      // Expected behavior
    }
  }

  public void testUnmodifiableTabBarSubclasses() {
    TabPanel p = new TabPanel();
    TabBar b = p.getTabBar();
    
    try {
      b.addTab("no");
      fail("Internal TabBar should not allow addTab() method");
    } catch (UnsupportedOperationException e) {
      // Expected behavior
    }

    try {
      b.addTab("no", true);
      fail("Internal TabBar should not allow addTab() method");
    } catch (UnsupportedOperationException e) {
      // Expected behavior
    }
    
    try {
      b.addTab(new Label("no"));
      fail("Internal TabBar should not allow addTab() method");
    } catch (UnsupportedOperationException e) {
      // Expected behavior
    }

    try {
      b.insertTab("no", 0);
      fail("Internal TabBar should not allow insertTab() method");
    } catch (UnsupportedOperationException e) {
      // Expected behavior
    }
    
    try {
      b.insertTab("no", true, 0);
      fail("Internal TabBar should not allow insertTab() method");
    } catch (UnsupportedOperationException e) {
      // Expected behavior
    }
    
    try {
      b.insertTab(new Label("no"), 0);
      fail("Internal TabBar should not allow insertTab() method");
    } catch (UnsupportedOperationException e) {
      // Expected behavior
    }
                                
    try {
      b.removeTab(0);
      fail("Internal TabBar should not allow removeTab() method");
    } catch (UnsupportedOperationException e) {
      // Expected behavior
    }
  }
}
