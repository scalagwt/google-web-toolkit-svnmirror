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

import java.util.ArrayList;
import java.util.List;

/**
 * Tests the color picker class. As ColorPicker is primarily a widget, the
 * majority of the tests will have to be in the new UI test suite.
 * 
 */
public class ColorPickerTest extends ItemPickerTest {

  public void testColorFast() {
    ColorPicker picker = new ColorPicker();
    List colors = ColorPicker.DEFAULT_COLORS;

    assertExpectedValues(picker, colors);

    // Now we want only one color in the color picker.
    List single = new ArrayList();
    single.add("#000000");
    picker.setItems(single.iterator());
    assertExpectedValues(picker, single);

    // Should be able to use arrays
    String[] stringColors = {"#000000"};
    ColorPicker picker2 = new ColorPicker(stringColors, 1);
    assertExpectedValues(picker2, single);
    Object[] asArray = ColorPicker.DEFAULT_COLORS.toArray();
    picker2.setItems(asArray);
    assertExpectedValues(picker2, ColorPicker.DEFAULT_COLORS);

  }

  public void testForIllegalInputs() {

    // Should throw illegal state exception if we try to give it no colors
    List noColors = new ArrayList();
    ColorPicker picker = new ColorPicker();
    try {
      picker.setItems(noColors.iterator());
      fail("Should have thrown an IllegatStateException");
    } catch (IllegalStateException s) {
      // Expected
    }
    try {
      picker.setColumnsPerRow(0);
      fail("Should have thrown an IllegatStateException");
    } catch (IllegalStateException s) {
      // Expected
    }

  }
}
