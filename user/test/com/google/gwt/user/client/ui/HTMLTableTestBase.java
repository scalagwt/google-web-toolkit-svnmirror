// Copyright 2006 Google Inc. All Rights Reserved.
package com.google.gwt.user.client.ui;

import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
import com.google.gwt.user.client.ui.HTMLTable.RowFormatter;

import java.util.List;

public abstract class HTMLTableTestBase extends GWTTestCase {

  public static void assertEquals(Object[] x, Object[] y) {
    assertEquals(x.length, y.length);
    for (int i = 0; i < y.length; i++) {
      assertEquals(x[i], y[i]);
    }

  }

  /** Easy way to test what should be in a list */
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
}
