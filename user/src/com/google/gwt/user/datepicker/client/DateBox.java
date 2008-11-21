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

package com.google.gwt.user.datepicker.client;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.HasKeyDownHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasAnimation;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;

import java.util.Date;

/**
 * A simple date box.
 */
public class DateBox extends Composite implements HasKeyDownHandlers,
    HasAnimation {

  /**
   * Default style name.
   */
  public static final String DEFAULT_STYLENAME = "gwt-DateBox";
  private static final DateTimeFormat DEFAULT_FORMATTER = DateTimeFormat.getMediumDateFormat();
  private boolean dirtyText = false;
  private PopupPanel popup = new PopupPanel();
  private TextBox box = new TextBox();
  private DatePicker picker;
  private DateTimeFormat dateFormatter;
  private boolean allowDPShow = true;

  /**
   * Create a new date box.
   */
  public DateBox() {
    this(new DatePicker());
  }

  /**
   * Create a new date box.
   * 
   * @param picker the picker to drop down from the date box
   */
  public DateBox(final DatePicker picker) {
    this(picker, DEFAULT_FORMATTER);
  }

  /**
   * Constructor.
   * 
   * @param picker the picker to drop down
   * @param formatter date time formatter to use for parsing the dates in this
   *          date box
   */
  public DateBox(final DatePicker picker, DateTimeFormat formatter) {
    FlowPanel p = new FlowPanel();
    this.dateFormatter = formatter;
    p.add(box);
    this.picker = picker;
    popup.setWidget(picker);
    initWidget(p);
    setStyleName(DEFAULT_STYLENAME);

    class DateBoxHandler implements ValueChangeHandler<Date>, FocusHandler,
        BlurHandler, ClickHandler, KeyDownHandler {

      public void onBlur(BlurEvent event) {
        if (dirtyText) {
          updateDateFromTextBox();
        }
      }

      public void onClick(ClickEvent event) {
        showCurrentDate();
      }

      public void onFocus(FocusEvent event) {
        if (allowDPShow) {
          showCurrentDate();
        }
      }

      public void onKeyDown(KeyDownEvent event) {
        switch (event.getNativeKeyCode()) {
          case KeyCodes.KEY_ENTER:
          case KeyCodes.KEY_TAB:
          case KeyCodes.KEY_ESCAPE:
          case KeyCodes.KEY_UP:
            updateDateFromTextBox();
            popup.hide();
            break;
          case KeyCodes.KEY_DOWN:
            showCurrentDate();
            break;
          default:
            dirtyText = true;
        }
      }

      public void onValueChange(ValueChangeEvent<Date> event) {
        setText(event.getValue());
        hideDatePicker();
        preventDatePickerPopup();
        box.setFocus(true);
      }
    }
    DateBoxHandler handler = new DateBoxHandler();
    picker.addValueChangeHandler(handler);
    box.addFocusHandler(handler);
    box.addBlurHandler(handler);
    box.addClickHandler(handler);
    box.addKeyDownHandler(handler);
  }

  public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
    return addHandler(handler, KeyDownEvent.getType());
  }

  /**
   * Clears the current selection.
   */
  public void clear() {
    picker.setValue(null, false);
    box.setText("");
  }

  /**
   * Gets the current cursor position in the date box.
   * 
   * @return the cursor position
   * 
   */
  public int getCursorPos() {
    return box.getCursorPos();
  }

  /**
   * Gets the date picker.
   * 
   * @return the date picker
   */
  public DatePicker getDatePicker() {
    return picker;
  }

  /**
   * Gets the date box's position in the tab index.
   * 
   * @return the date box's tab index
   */
  public int getTabIndex() {
    return box.getTabIndex();
  }

  /**
   * Get current text in text box.
   * 
   * @return the text in the date box
   */
  public String getText() {
    return box.getText();
  }

  /**
   * Get text box.
   * 
   * @return the text box used to enter the formatted date
   */
  public TextBox getTextBox() {
    return box;
  }

  /**
   * Hide the date picker.
   */
  public void hideDatePicker() {
    popup.hide();
  }

  public boolean isAnimationEnabled() {
    return popup.isAnimationEnabled();
  }

  /**
   * @return true if date picker is currently visible, false if not
   */
  public boolean isDatePickerVisible() {
    return popup.isVisible();
  }

  /**
   * Sets the date box's 'access key'. This key is used (in conjunction with a
   * browser-specific modifier key) to automatically focus the widget.
   * 
   * @param key the date box's access key
   */
  public void setAccessKey(char key) {
    box.setAccessKey(key);
  }

  public void setAnimationEnabled(boolean enable) {
    popup.setAnimationEnabled(enable);
  }

  /**
   * Sets the date format to the given format. If date box is not empty,
   * contents of date box will be replaced with current date in new format.
   * 
   * @param format format.
   */
  public void setDateFormat(DateTimeFormat format) {
    if (format != dateFormatter) {
      dateFormatter = format;
      String cur = box.getText();
      if (cur != null && cur.length() != 0) {
        try {
          box.setText(dateFormatter.format(picker.getValue()));
        } catch (IllegalArgumentException e) {
          box.setText("");
        }
      }
    }
  }

  /**
   * Sets whether the date box is enabled.
   * 
   * @param enabled is the box enabled
   */
  public void setEnabled(boolean enabled) {
    box.setEnabled(enabled);
  }

  /**
   * Explicitly focus/unfocus this widget. Only one widget can have focus at a
   * time, and the widget that does will receive all keyboard events.
   * 
   * @param focused whether this widget should take focus or release it
   */
  public void setFocus(boolean focused) {
    box.setFocus(focused);
  }

  /**
   * Sets the date box's position in the tab index. If more than one widget has
   * the same tab index, each such widget will receive focus in an arbitrary
   * order. Setting the tab index to <code>-1</code> will cause this widget to
   * be removed from the tab order.
   * 
   * @param index the date box's tab index
   */
  public void setTabIndex(int index) {
    box.setTabIndex(index);
  }

  /**
   * Parses the current date box's value and shows that date.
   */
  public void showCurrentDate() {
    Date current = null;

    String value = box.getText().trim();
    if (!value.equals("")) {
      try {
        current = dateFormatter.parse(value);
      } catch (IllegalArgumentException e) {
        // Does not trigger error reporting because user has not left the text
        // box yet.
      }
    }

    if (current == null) {
      current = new Date();
    }
    picker.setCurrentMonth(current);
    popup.showRelativeTo(this);
  }

  /**
   * Show the given date.
   * 
   * @param date picker
   */
  public void showDate(Date date) {
    picker.setValue(date, false);
    picker.setCurrentMonth(date);
    setText(date);
    dirtyText = false;
  }

  private void preventDatePickerPopup() {
    allowDPShow = false;
    DeferredCommand.addCommand(new Command() {
      public void execute() {
        allowDPShow = true;
      }
    });
  }

  private void setText(Date value) {
    box.setText(dateFormatter.format(value));
    dirtyText = false;
  }

  private void updateDateFromTextBox() {
    String text = box.getText().trim();
    if (text.equals("")) {
      return;
    }
    try {
      Date d = dateFormatter.parse(text);
      showDate(d);
    } catch (IllegalArgumentException exception) {
      // TODO(ECC) use new reporter interface here.
    }
    dirtyText = false;
  }

}
