/*
 * Copyright 2006 Google Inc.
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
import java.util.Arrays;
import java.util.List;

public class SuggestPickerTest extends ItemPickerTest {
  public void testValues() {
    SuggestPicker picker = new SuggestPicker();
    String[] test1 = {"a", "b", "c"};
    String[] test2 = {"d"};
    String[] test3 = {"x", "y", "z", "q"};
    picker.setItems(Arrays.asList(test1));
    assertExpectedValues(picker, test1);
    picker.setItems(Arrays.asList(test2));
    assertExpectedValues(picker, test2);
    picker.setItems(Arrays.asList(test3));
    assertExpectedValues(picker, test3);
  }

  public void testForIllegalInputs() {

    // Should throw illegal state exception if we try to give it no colors
    List noSuggestions = new ArrayList();
    SuggestPicker picker = new SuggestPicker();
    try {
      picker.setItems(noSuggestions);
      fail("Should have thrown an IllegatStateException");
    } catch (IllegalStateException s) {
      // Expected
    }

  }
}
