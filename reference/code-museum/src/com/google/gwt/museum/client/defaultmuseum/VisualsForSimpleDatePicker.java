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

package com.google.gwt.museum.client.defaultmuseum;

import com.google.gwt.museum.client.common.AbstractIssue;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DatePicker;

import java.util.Date;

/**
 * Date picker demo.
 */
public class VisualsForSimpleDatePicker extends AbstractIssue {

  @Override
  public Widget createIssue() {
    DatePicker picker = new DatePicker();
    picker.setValue(new Date());
    return picker;
  };

  @Override
  public String getInstructions() {
    return "Go back one month, go forward one month, check that highlighting is working, and try selecting a date.";
  }

  @Override
  public String getSummary() {
    return "Visual test for date picker";
  }

  @Override
  public boolean hasCSS() {
    return true;
  }

}
