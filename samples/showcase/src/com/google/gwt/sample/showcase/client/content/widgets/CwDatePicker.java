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
package com.google.gwt.sample.showcase.client.content.widgets;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.Constants;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.sample.showcase.client.ContentWidget;
import com.google.gwt.sample.showcase.client.ShowcaseAnnotations.ShowcaseData;
import com.google.gwt.sample.showcase.client.ShowcaseAnnotations.ShowcaseSource;
import com.google.gwt.sample.showcase.client.ShowcaseAnnotations.ShowcaseStyle;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.user.datepicker.client.DatePicker;

import java.util.Date;

/**
 * Example file.
 */
@ShowcaseStyle({
    ".gwt-DatePicker", ".datePicker", "td.datePickerMonth", ".gwt-DateBox",
    ".dateBox"})
public class CwDatePicker extends ContentWidget {
  /**
   * The constants used in this Content Widget.
   */
  @ShowcaseSource
  public static interface CwConstants extends Constants,
      ContentWidget.CwConstants {
    String cwDatePickerBoxLabel();

    String cwDatePickerDescription();

    String cwDatePickerLabel();

    String cwDatePickerName();
  }

  /**
   * An instance of the constants.
   */
  @ShowcaseData
  private CwConstants constants;

  /**
   * Constructor.
   * 
   * @param constants the constants
   */
  public CwDatePicker(CwConstants constants) {
    super(constants);
    this.constants = constants;
  }

  @Override
  public String getDescription() {
    return constants.cwDatePickerDescription();
  }

  @Override
  public String getName() {
    return constants.cwDatePickerName();
  }

  /**
   * Initialize this example.
   */
  @ShowcaseSource
  @Override
  public Widget onInitialize() {
    // Create a basic date picker
    DatePicker datePicker = new DatePicker();
    datePicker.setValue(new Date());
    final TextBox textBox = new TextBox();
    textBox.setEnabled(false);

    // Set the value in the text box when the user selects a date
    datePicker.addValueChangeHandler(new ValueChangeHandler<Date>() {
      public void onValueChange(ValueChangeEvent<Date> event) {
        Date date = event.getValue();
        String dateString = DateTimeFormat.getMediumDateFormat().format(date);
        textBox.setValue(dateString);
      }
    });

    // Create a DateBox
    DateBox dateBox = new DateBox();

    // Combine the widgets into a panel and return them
    VerticalPanel vPanel = new VerticalPanel();
    vPanel.add(new HTML(constants.cwDatePickerLabel()));
    vPanel.add(textBox);
    vPanel.add(datePicker);
    vPanel.add(new HTML(constants.cwDatePickerBoxLabel()));
    vPanel.add(dateBox);
    return vPanel;
  }
}
