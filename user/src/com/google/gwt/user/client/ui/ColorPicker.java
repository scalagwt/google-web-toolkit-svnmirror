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

import com.google.gwt.user.client.DOM;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Color picker popup. Items are represented as CSS colors.
 */
public class ColorPicker extends AbstractItemPicker {
  private class ColorItem extends Item {

    public ColorItem(int index, String color) {
      super(index);
      DOM.setStyleAttribute(getElement(), "background", color);
    }

    public Object getValue() {
      return DOM.getStyleAttribute(getElement(), "background");
    }
  }

  /**
   * Default colors supplied to the color picker popup. The default colors are a
   * selection of 60 web-safe CSS color styles.
   */
  public static final String[] DEFAULT_COLORS = {
      "#ffffcc", "#ffff66", "#ffcc66", "#F2984C", "#E1771E", "#B47B10",
      "#A9501B", "#6F3C1B", "#804000", "#CC0000", "#940F04", "#660000",
      "#C3D9FF", "#99C9FF", "#66B5FF", "#3D81EE", "#0066CC", "#6C82B5",
      "#32527A", "#2D6E89", "#006699", "#215670", "#003366", "#000033",
      "#CAF99B", "#80FF00", "#00FF80", "#78B749", "#2BA94F", "#38B63C",
      "#0D8F63", "#2D8930", "#1B703A", "#11593C", "#063E3F", "#002E3F",
      "#FFBBE8", "#E895CC", "#FF6FCF", "#C94093", "#9D1961", "#800040",
      "#800080", "#72179D", "#6728B2", "#6131BD", "#341473", "#400058",
      "#ffffff", "#e6e6e6", "#cccccc", "#b3b3b3", "#999999", "#808080",
      "#7f7f7f", "#666666", "#4c4c4c", "#333333", "#191919", "#000000"};

  private int numColumns = -1;

  /**
   * Constructor for {@link ColorPicker}.
   */
  public ColorPicker() {
    this(12);
  }

  /**
   * Constructor for {@link ColorPicker}F
   * 
   * @param numColumns number of columns to be displayed
   */
  public ColorPicker(int numColumns) {
    this(Arrays.asList(DEFAULT_COLORS), numColumns);
  }

  /**
   * 
   * Constructor for {@link ColorPicker}
   * 
   * @param colors colors list of colors
   * @param numColumns number of columns to be displayed
   */
  public ColorPicker(List colors, int numColumns) {
    this.numColumns = numColumns;
    setStyleName("gwt-ColorPickerPopup");
    setItems(colors.iterator());
  }

  public boolean navigate(char keyCode) {
    if (isAttached()) {
      switch (keyCode) {
        case KeyboardListener.KEY_DOWN:
          shiftSelection(numColumns);
          break;
        case KeyboardListener.KEY_UP:
          shiftSelection(-numColumns);
          break;
        case KeyboardListener.KEY_LEFT:
          shiftSelection(-1);
          break;
        case KeyboardListener.KEY_RIGHT:
          shiftSelection(1);
          break;
        default:
          // Avoid shared post processing.
          return false;
      }
      return true;
    } else {
      return false;
    }
  }

  /**
   * Sets the items in the <code> ColorPickerPopup</code>. Each item should
   * represent a CSS color.
   * 
   * @param colors list of colors.
   */
  public void setItems(Iterator colors) {

    int row = 0;
    int i = 0;

    while (true) {
      for (int column = 0; column < numColumns; column++) {
        if (!colors.hasNext()) {
          // All items have been placed.
          return;
        }
        String color = (String) colors.next();
        ColorItem item = new ColorItem(i, color);
        getLayout().setWidget(row, column, item);
        ++i;
      }
      ++row;
    }
  }

}
