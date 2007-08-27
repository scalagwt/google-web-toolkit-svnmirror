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
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
import com.google.gwt.user.client.ui.HTMLTable.RowFormatter;

import java.util.Iterator;
import java.util.List;

/**
 * Base test for HTMLTable derived classes.
 */
public abstract class HTMLTableTestBase extends GWTTestCase {

  static class Adder implements HasWidgetsTester.WidgetAdder {
    public void addChild(HasWidgets container, Widget child) {
      ((HTMLTable) container).setWidget(0, 0, child);
    }
  }

  public static void assertEquals(Object[] x, Object[] y) {
    assertEquals(x.length, y.length);
    for (int i = 0; i < y.length; i++) {
      assertEquals(x[i], y[i]);
    }
  }

  /**
   * Easy way to test what should be in a list.
   */
  protected static void assertEquals(Object[] array, List target) {
    if (target.size() != array.length) {
      fail(target + " should be the same length as" + array);
    }
    for (int i = 0; i < array.length; i++) {
      assertEquals(target.get(i), array[i]);
    }
  }

  public String getModuleName() {
    return "com.google.gwt.user.User";
  }

  public abstract HTMLTable getTable(int row, int column);

  public void testAttachDetachOrder() {
    HasWidgetsTester.testAll(getTable(1, 1), new Adder());
  }

  public void testBoundsOnEmptyTable() {
    HTMLTable t = getTable(0, 0);
    try {
      t.getCellFormatter().getElement(4, 5);
    } catch (IndexOutOfBoundsException e) {
      return;
    }
    fail("should have throw an index out of bounds");
  }

  public void testDoubleSet() {
    HTMLTable t = getTable(4, 4);
    t.setWidget(0, 0, new Label());
    Widget s = new Label();
    t.setWidget(0, 0, s);
    assertEquals(s, t.getWidget(0, 0));
  }

  public void testIterator() {
    // Check remove.
    HTMLTable t = getTable(5, 5);
    Label l = new Label("hello");
    t.setWidget(0, 0, l);
    Iterator iter = t.iterator();
    assertEquals(l, iter.next());
    iter.remove();
    Iterator iter2 = t.iterator();
    assertFalse(iter2.hasNext());

    // Check put after remove.
    Widget w = new Label("bo");
    t.setWidget(0, 0, w);
    Iterator iter3 = t.iterator();
    assertEquals(w, iter3.next());
    assertFalse(iter3.hasNext());

    // Check swapping widgets.
    Widget w2 = new Label("ba");
    t.setWidget(0, 0, w2);
    Iterator iter4 = t.iterator();
    assertEquals(w2, iter4.next());
    assertFalse(iter4.hasNext());

    // Check put after put.
    Widget w3 = new Label("be");
    t.setWidget(1, 1, w3);
    Iterator iter5 = t.iterator();
    assertEquals(w2, iter5.next());
    assertEquals(w3, iter5.next());
    assertFalse(iter5.hasNext());
  }

  public void testSettingCellAttributes() {
    // These tests simple test for errors while setting these fields. The
    // Patient sample under the survey project has the visual part of the test.
    HTMLTable t = getTable(4, 4);

    CellFormatter formatter = t.getCellFormatter();
    formatter.setHeight(0, 0, "100%");
    formatter.setVerticalAlignment(0, 1, HasVerticalAlignment.ALIGN_BOTTOM);
    formatter.setHorizontalAlignment(1, 1, HasHorizontalAlignment.ALIGN_RIGHT);
    formatter.setWidth(0, 2, "100%");
  }

  public void testStyles() {
    HTMLTable t = getTable(4, 4);
    t.getCellFormatter().setStyleName(2, 2, "hello");
    assertEquals("hello", t.getCellFormatter().getStyleName(2, 2));
    t.getCellFormatter().setStyleName(2, 2, "goodbye");
    t.getCellFormatter().addStyleName(2, 2, "hello");

    // Visable Styles.
    t.getCellFormatter().setVisible(0, 0, false);
    assertTrue(t.getCellFormatter().isVisible(2, 2));
    assertFalse(t.getCellFormatter().isVisible(0, 0));
    RowFormatter formatter = t.getRowFormatter();
    formatter.setVisible(3, false);
    assertFalse(formatter.isVisible(3));
    assertTrue(formatter.isVisible(2));
    assertTrue(t.getCellFormatter().isVisible(2, 0));

    // Style name.
    assertEquals("goodbye hello", t.getCellFormatter().getStyleName(2, 2));
    t.getRowFormatter().setStyleName(3, "newStyle");
    assertEquals("newStyle", t.getRowFormatter().getStyleName(3));
  }
}
