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

    public String dayIsToday() {
      return day("Today");
    }

    public String dayIsValue() {
      return day("Value");
    }

    public String dayIsValueAndHighlighted() {
      return dayIsValue() + "AndHighlighted";
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

  private final DateStyler styler = new DateStyler();
  private final MonthSelector monthSelector;
  private final CalendarView view;
  private final CalendarModel model;
  private Date value;
  private Date highlighted;
  private StandardCss css = StandardCss.DEFAULT;

  /**
   * Create a new date picker.
   */
  public DatePicker() {
    this(new DefaultMonthSelector(), new DefaultCalendarView(),
        new CalendarModel());
  }

  /**
   * Constructor for use by subType()s.
   * 
   * @param monthSelector the month selector
   * @param calendarView the view view
   * @param model the view model
   */

  protected DatePicker(MonthSelector monthSelector, CalendarView calendarView,
      CalendarModel model) {

    this.model = model;
    this.monthSelector = monthSelector;
    monthSelector.setDatePicker(this);
    this.view = calendarView;
    view.setDatePicker(this);

    view.setup();
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
      view.addStyleToDate(date, styleName);
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
   * current view.
   * 
   * @param handler the handler
   * @return the handler registration
   */
  public HandlerRegistration addShowRangeHandlerAndFire(
      ShowRangeHandler<Date> handler) {
    ShowRangeEvent event = new ShowRangeEvent(view.getFirstDate(),
        view.getLastDate()) {
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
    view.addStyleToDate(visibleDate, styleName);
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
    getView().addStyleToDates(visibleDates, styleName);
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
    return highlighted;
  }

  /**
   * Gets the {@link DatePicker}'s value.
   * 
   * @return the value
   */
  public final Date getValue() {
    return value;
  }

  /**
   * Is the date currently shown in the date picker?
   * 
   * @param date
   * @return is the date currently shown
   */
  public boolean isDateVisible(Date date) {
    return view.isDateVisible(date);
  }

  /**
   * Is the visible date enabled?
   * 
   * @param date the date
   * @return is the date enabled?
   */
  public boolean isVisibleDateEnabled(Date date) {
    assert isDateVisible(date) : date + " is not visible";
    return view.isDateEnabled(date);
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
      view.removeStyleFromDate(date, styleName);
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
      view.removeStyleFromDate(date, styleName);
    }
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
    getView().setDateEnabled(date, enabled);
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
    getView().setDatesEnabled(dates, enabled);
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
   * Sets the {@link DatePicker}'s value.
   * 
   * @param date the new value
   */
  public final void setValue(Date date) {
    setValue(date, true);
  }

  /**
   * Sets the {@link DatePicker}'s value.
   * 
   * @param newValue the new value for this date picker
   * @param fireEvents should events be fired.
   */
  public final void setValue(Date newValue, boolean fireEvents) {
    Date oldSelected = value;

    if (oldSelected != null) {
      removeGlobalStyleFromDate(oldSelected, css().dayIsValue());
    }

    value = CalendarUtil.copyDate(newValue);
    if (value != null) {
      addGlobalStyleToDate(value, css().dayIsValue());
    }
    ValueChangeEvent.fire(this, newValue);
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
   * Gets the {@link CalendarView} associated with this date picker.
   * 
   * @return view view
   */
  protected final CalendarView getView() {
    return view;
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
    panel.add(this.getView());
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
    highlighted = null;
    view.refresh();
    monthSelector.refresh();
    ShowRangeEvent.fire(this, getView().getFirstDate(),
        getView().getLastDate());
  }

  /**
   * Sets the highlighted date.
   * 
   * @param highlighted highlighted date
   */
  void setHighlightedDate(Date highlightedDate) {
    this.highlighted = highlightedDate;
    HighlightEvent.fire(this, highlightedDate);
  }

}
