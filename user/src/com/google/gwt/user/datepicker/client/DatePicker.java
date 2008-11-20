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

import com.google.gwt.event.logical.shared.HasHighlightHandlers;
import com.google.gwt.event.logical.shared.HasShowRangeHandlers;
import com.google.gwt.event.logical.shared.HighlightEvent;
import com.google.gwt.event.logical.shared.HighlightHandler;
import com.google.gwt.event.logical.shared.ShowRangeEvent;
import com.google.gwt.event.logical.shared.ShowRangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Standard GWT date picker.
 */
public class DatePicker extends Composite implements
    HasHighlightHandlers<Date>, HasShowRangeHandlers<Date>, HasValue<Date> {

  /**
   * Convenience class to group css style names.
   */
  static class StandardCss {

    static StandardCss DEFAULT = new StandardCss("gwt-DatePicker", "datePicker");

    private String baseName;
    private String widgetName;

    public StandardCss(String widgetName, String baseName) {
      this.widgetName = widgetName;
      this.baseName = baseName;
    }

    public String datePicker() {
      return getWidgetStyleName();
    }

    public String day() {
      return wrap("Day");
    }

    public String day(String dayModifier) {
      return day() + "Is" + dayModifier;
    }

    public String dayIsDisabled() {
      return day("Disabled");
    }

    public String dayIsFiller() {
      return day("Filler");
    }

    public String dayIsHighlighted() {
      return day("Highlighted");
    }

    public String dayIsSelected() {
      return day("Selected");
    }

    public String dayIsSelectedAndHighlighted() {
      return dayIsSelected() + "AndHighlighted";
    }

    public String dayIsToday() {
      return day("Today");
    }

    public String dayIsWeekend() {
      return day("Weekend");
    }

    public String days() {
      return wrap("Days");
    }

    public String daysLabel() {
      return wrap("DaysLabel");
    }

    public String getBaseStyleName() {
      return baseName;
    }

    public String getWidgetStyleName() {
      return widgetName;
    }

    public String month() {
      return wrap("Month");
    }

    public String monthSelector() {
      return wrap("MonthSelector");
    }

    public String nextButton() {
      return wrap("NextButton");
    }

    public String previousButton() {
      return wrap("PreviousButton");
    }

    public String weekdayLabel() {
      return wrap("WeekdayLabel");
    }

    public String weekendLabel() {
      return wrap("WeekendLabel");
    }

    /**
     * Prepends the base name to the given style.
     * 
     * @param style style name
     * @return style name
     */
    protected String wrap(String style) {
      return baseName + style;
    }
  }

  private class DateStyler {
    private Map<String, String> info = new HashMap<String, String>();

    public String getStyleName(Date d) {
      return info.get(genKey(d));
    }

    public void setStyleName(Date d, String styleName, boolean add) {
      // Code is easier to maintain if surrounded by " ", and on all browsers
      // this is a no-op.
      styleName = " " + styleName + " ";
      String key = genKey(d);
      String current = info.get(key);

      if (add) {
        if (current == null) {
          info.put(key, styleName);
        } else if (current.indexOf(styleName) == -1) {
          info.put(key, current + styleName);
        }
      } else {
        assert current != null : "Removing style " + styleName + " from date "
            + d + " but the style name wasn't there";

        String newValue = current.replaceAll(styleName, "");
        if (newValue.trim().length() == 0) {
          info.remove(key);
        } else {
          info.put(key, newValue);
        }
      }
    }

    private String genKey(Date d) {
      return d.getYear() + "/" + d.getMonth() + "/" + d.getDate();
    }
  }

  private DateStyler styler = new DateStyler();
  private Date highlightedDate;
  private MonthSelector monthSelector;
  private CalendarView calendar;
  private CalendarModel model;
  private Date selectedDate;
  private StandardCss css = StandardCss.DEFAULT;

  /**
   * Constructor.
   */
  public DatePicker() {
    this(new DefaultMonthSelector(), new DefaultCalendarView(),
        new CalendarModel());
  }

  /**
   * Constructor for use by subgetType()s.
   * 
   * @param monthSelector the month selector
   * @param calendarView the calendar view
   * @param model the calendar model
   */

  protected DatePicker(MonthSelector monthSelector, CalendarView calendarView,
      CalendarModel model) {
    this.setModel(model);
    this.monthSelector = monthSelector;
    monthSelector.setDatePicker(this);
    this.calendar = calendarView;
    calendar.setDatePicker(this);
    calendar.setup();
    monthSelector.setup();
    this.setup();
    showDate(new Date());
    addGlobalStyleToDate(new Date(), css().dayIsToday());
  }

  /**
   * Globally adds a style name to a date. i.e. the style name is associated
   * with the date each time it is rendered.
   * 
   * @param date date
   * @param styleName style name
   */
  public void addGlobalStyleToDate(Date date, String styleName) {
    styler.setStyleName(date, styleName, true);
    if (isDateVisible(date)) {
      calendar.addStyleToDate(date, styleName);
    }
  }

  public HandlerRegistration addHighlightHandler(HighlightHandler<Date> handler) {
    return addHandler(handler, HighlightEvent.getType());
  }

  public HandlerRegistration addShowRangeHandler(ShowRangeHandler<Date> handler) {
    return addHandler(handler, ShowRangeEvent.getType());
  }

  /**
   * Adds a show range handler and immediately activate the handler on the
   * current calendar view.
   * 
   * @param handler the handler
   * @return the handler registration
   */
  public HandlerRegistration addShowRangeHandlerAndFire(
      ShowRangeHandler<Date> handler) {
    ShowRangeEvent event = new ShowRangeEvent(calendar.getFirstDate(),
        calendar.getLastDate()) {
    };
    handler.onShowRange(event);
    return addShowRangeHandler(handler);
  }

  /**
   * Shows the given style name on the specified date. This is only set until
   * the next time the DatePicker is refreshed.
   * 
   * @param visibleDate current visible date
   * @param styleName style name
   */
  public final void addStyleToVisibleDate(Date visibleDate, String styleName) {
    calendar.addStyleToDate(visibleDate, styleName);
  }

  /**
   * Adds a style name on a set of currently visible dates. This is only set
   * until the next time the DatePicker is refreshed.
   * 
   * @param visibleDates dates that will have the supplied style removed
   * @param styleName style name to remove
   */
  public final void addStyleToVisibleDates(Iterable<Date> visibleDates,
      String styleName) {
    getCalendarView().addStyleToDates(visibleDates, styleName);
  }

  public HandlerRegistration addValueChangeHandler(
      ValueChangeHandler<Date> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  /**
   * Gets the currently shown date.
   * 
   * @return the current date shown
   */
  public Date getDateShown() {
    return getModel().getCurrentMonth();
  }

  /**
   * Gets the global style name associated with a date.
   * 
   * @param date the date
   * @return the styles associated with this date
   */
  public String getGlobalStyleOfDate(Date date) {
    return styler.getStyleName(date);
  }

  /**
   * Gets the highlighted date, if any.
   * 
   * @return the highlighted date
   */
  public final Date getHighlightedDate() {
    return highlightedDate;
  }

  /**
   * Gets the selected date, if any.
   * 
   * @return the selected date
   */
  public final Date getValue() {
    return selectedDate;
  }

  /**
   * Is the date currently shown in the date picker?
   * 
   * @param date
   * @return is the date currently shown
   */
  public boolean isDateVisible(Date date) {
    return calendar.isDateVisible(date);
  }

  /**
   * Is the visible date enabled?
   * 
   * @param date the date
   * @return is the date enabled?
   */
  public boolean isVisibleDateEnabled(Date date) {
    assert isDateVisible(date) : date + " is not visible";
    return calendar.isDateEnabled(date);
  }

  /**
   * Globally removes a style from a date.
   * 
   * @param date date
   * @param styleName style name
   */
  public void removeGlobalStyleFromDate(Date date, String styleName) {
    styler.setStyleName(date, styleName, false);
    if (isDateVisible(date)) {
      calendar.removeStyleFromDate(date, styleName);
    }
  }

  /**
   * Removes a style name from multiple visible dates.
   * 
   * @param dates dates that will have the supplied style removed
   * @param styleName style name to remove
   */
  public final void removeStyleFromVisibleDates(Iterator<Date> dates,
      String styleName) {
    while (dates.hasNext()) {
      Date date = dates.next();
      assert (isDateVisible(date)) : date + " should be visible";
      calendar.removeStyleFromDate(date, styleName);
    }
  }

  /**
   * Selects the current highlighted date.
   */
  public final void selectHighlightedDate() {
    setValue(getHighlightedDate());
  }

  /**
   * Sets a visible date to be enabled or disabled. This is only set until the
   * next time the DatePicker is refreshed.
   * 
   * @param date the date
   * @param enabled is enabled
   */
  public final void setEnabledOnVisibleDate(Date date, boolean enabled) {
    assert isDateVisible(date) : date
        + " cannot be enabled or disabled as it is not visible";
    getCalendarView().setDateEnabled(date, enabled);
  }

  /**
   * Sets a group of visible dates to be enabled or disabled. This is only set
   * until the next time the DatePicker is refreshed.
   * 
   * @param dates the dates
   * @param enabled is enabled
   */
  public final void setEnabledOnVisibleDates(Iterable<Date> dates,
      boolean enabled) {
    getCalendarView().setDatesEnabled(dates, enabled);
  }

  /**
   * Sets the date picker style name.
   */
  @Override
  public void setStyleName(String styleName) {
    css = new StandardCss(styleName, "datePicker");
    super.setStyleName(styleName);
  }

  /**
   * Sets the selected date.
   * 
   * @param date the new selected date
   */
  public final void setValue(Date date) {
    setValue(date, true);
  }

  /**
   * Sets the selected date.
   * 
   * @param newSelected the new selected date
   * @param fireEvents should events be fired.
   */
  public final void setValue(Date newSelected, boolean fireEvents) {
    Date oldSelected = selectedDate;

    if (oldSelected != null) {
      removeGlobalStyleFromDate(oldSelected, css().dayIsSelected());
    }

    selectedDate = CalendarUtil.copyDate(newSelected);
    if (selectedDate != null) {
      addGlobalStyleToDate(selectedDate, css().dayIsSelected());
    }
    ValueChangeEvent.fire(this, newSelected);
  }

  /**
   * Shows the given date.
   * 
   * @param date the date to show
   */
  public final void showDate(Date date) {
    getModel().setCurrentMonth(date);
    refreshAll();
  }

  /**
   * Gets the {@link CalendarView} associated with this date picker.
   * 
   * @return calendar view
   */
  protected final CalendarView getCalendarView() {
    return calendar;
  }

  /**
   * Gets the {@link CalendarModel} associated with this date picker.
   * 
   * @return the model
   */
  protected final CalendarModel getModel() {
    return model;
  }

  /**
   * Gets the {@link MonthSelector} associated with this date picker.
   * 
   * @return the month selector
   */
  protected final Widget getMonthSelector() {
    return monthSelector;
  }

  /**
   * Sets up the date picker.
   */
  protected void setup() {
    FlowPanel panel = new FlowPanel();
    initWidget(panel);
    setStyleName(panel.getElement(), css.datePicker());
    setStyleName(css().datePicker());
    panel.add(this.getMonthSelector());
    panel.add(this.getCalendarView());
  }

  /**
   * Gets the {@link DatePicker.Css} associated with this date picker for use by
   * extended month and cell grids.
   * 
   * @return the css.
   */
  final StandardCss css() {
    return css;
  }

  /**
   * Refreshes all components of this date picker.
   */
  final void refreshAll() {
    highlightedDate = null;
    calendar.refresh();
    monthSelector.refresh();
    ShowRangeEvent.fire(this, getCalendarView().getFirstDate(),
        getCalendarView().getLastDate());
  }

  /**
   * Sets the highlighted date.
   * 
   * @param highlightedDate highlighted date
   */
  void setHighlightedDate(Date highlightedDate) {
    this.highlightedDate = highlightedDate;
    HighlightEvent.fire(this, highlightedDate);
  }

  private void setModel(CalendarModel model) {
    this.model = model;
  }
}
