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
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.VerticalPanel;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Standard GWT date picker.
 * 
 * <h3>CSS Style Rules</h3>
 * 
 * <ul class="css">
 * 
 * <li>.gwt-DatePicker { }</li>
 * 
 * <li>.datePickerMonthSelector { the month selector widget }</li>
 * 
 * <li>.datePickerMonth { the month in the month selector widget } <li>
 * 
 * <li>.datePickerPreviousButton { the previous month button } <li>
 * 
 * <li>.datePickerNextButton { the next month button } <li>
 * 
 * <li>.datePickerDays { the portion of the picker that shows the days }</li>
 * 
 * <li>.datePickerWeekdayLabel { the label over weekdays }</li>
 * 
 * <li>.datePickerWeekendLabel { the label over weekends }</li>
 * 
 * <li>.datePickerDay { a single day }</li>
 * 
 * <li>.datePickerDayIsToday { today's date }</li>
 * 
 * <li>.datePickerDayIsWeekend { a weekend day }</li>
 * 
 * <li>.datePickerDayIsFiller { a day in another month }</li>
 * 
 * <li>.datePickerDayIsValue { the selected day }</li>
 * 
 * <li>.datePickerDayIsDisabled { a disabled day }</li>
 * 
 * <li>.datePickerDayIsHighlighted { the currently highlighted day }</li>
 * 
 * <li>.datePickerDayIsValueAndHighlighted { the highlighted day if it is also
 * selected }</li>
 * 
 * </ul>
 * 
 * <p>
 * <h3>Example</h3>
 * {@example com.google.gwt.examples.DatePickerExample}
 * </p>
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

    @SuppressWarnings("deprecation")
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
   * Creates a new date picker.
   * 
   * @param monthSelector the month selector
   * @param view the view
   * @param model the model
   */

  protected DatePicker(MonthSelector monthSelector, CalendarView view,
      CalendarModel model) {

    this.model = model;
    this.monthSelector = monthSelector;
    monthSelector.setDatePicker(this);
    this.view = view;
    view.setDatePicker(this);

    view.setup();
    monthSelector.setup();
    this.setup();

    setCurrentMonth(new Date());
    addGlobalStyleToDate(css().dayIsToday(), new Date());
  }

  /**
   * Globally adds a style name to a date. i.e. the style name is associated
   * with the date each time it is rendered.
   * 
   * @param styleName style name
   * @param date date
   */
  public void addGlobalStyleToDate(String styleName, Date date) {
    styler.setStyleName(date, styleName, true);
    if (isDateVisible(date)) {
      getView().addStyleToDate(styleName, date);
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
    ShowRangeEvent<Date> event = new ShowRangeEvent<Date>(
        getView().getFirstDate(), getView().getLastDate()) {
    };
    handler.onShowRange(event);
    return addShowRangeHandler(handler);
  }

  /**
   * Shows the given style name on the specified date. This is only set until
   * the next time the DatePicker is refreshed.
   * 
   * @param styleName style name
   * @param date visible date
   * @param moreDates optional visible dates
   */
  public final void addStyleToVisibleDates(String styleName, Date date,
      Date... moreDates) {
    assert (assertVisible(date, moreDates));
    getView().addStyleToDate(styleName, date);
    if (moreDates != null) {
      for (Date d : moreDates) {
        getView().addStyleToDate(styleName, d);
      }
    }
  }

  /**
   * Adds a style name on a set of currently visible dates. This is only set
   * until the next time the DatePicker is refreshed.
   * 
   * @param styleName style name to remove
   * @param visibleDates dates that will have the supplied style removed
   */
  public final void addStyleToVisibleDates(String styleName,
      Iterable<Date> visibleDates) {
    for (Date date : visibleDates) {
      getView().addStyleToDate(styleName, date);
    }
  }

  public HandlerRegistration addValueChangeHandler(
      ValueChangeHandler<Date> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  /**
   * Gets the current month the date picker is showing.
   * 
   * <p>
   * A datepicker <b> may </b> show days not in the current month. It
   * <b>must</b> show all days in the current month.
   * </p>
   * 
   */
  public Date getCurrentMonth() {
    return getModel().getCurrentMonth();
  }

  /**
   * Returns the first shown date.
   * 
   * @return the first date.
   */
  // Final because the view should always control the value of the first date.
  public final Date getFirstDate() {
    return view.getFirstDate();
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
   * Gets the highlighted date (the one the mouse is hovering over), if any.
   * 
   * @return the highlighted date
   */
  public final Date getHighlightedDate() {
    return highlighted;
  }

  /**
   * Returns the last shown date.
   * 
   * @return the last date.
   */
  // Final because the view should always control the value of the last date.
  public final Date getLastDate() {
    return view.getLastDate();
  }

  /**
   * Returns the selected date, or null if none is selected.
   * 
   * @return the selected date, or null
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
    CalendarView r = getView();
    Date first = r.getFirstDate();
    Date last = r.getLastDate();
    return (date != null && (first.equals(date) || last.equals(date) || (first.before(date) && last.after(date))));
  }

  /**
   * Is the visible date enabled?
   * 
   * @param date the date
   * @return is the date enabled?
   */
  public boolean isVisibleDateEnabled(Date date) {
    assert isDateVisible(date) : date + " is not visible";
    return getView().isDateEnabled(date);
  }

  /**
   * Globally removes a style from a date.
   * 
   * @param styleName style name
   * @param date date
   */
  public void removeGlobalStyleFromDate(String styleName, Date date) {
    styler.setStyleName(date, styleName, false);
    if (isDateVisible(date)) {
      getView().removeStyleFromDate(styleName, date);
    }
  }

  /**
   * Removes a style name from multiple visible dates.
   * 
   * @param styleName style name to remove
   * @param date a visible date
   * @param moreDates optional additional visible dates
   */
  public final void removeStyleFromVisibleDates(String styleName, Date date,
      Date... moreDates) {
    assert (isDateVisible(date)) : date + " should be visible";
    getView().removeStyleFromDate(styleName, date);
    for (Date d : moreDates) {
      getView().removeStyleFromDate(styleName, d);
    }
  }

  /**
   * Removes a style name from multiple visible dates.
   * 
   * @param styleName style name to remove
   * @param dates dates that will have the supplied style removed
   */
  public final void removeStyleFromVisibleDates(String styleName,
      Iterator<Date> dates) {
    while (dates.hasNext()) {
      Date date = dates.next();
      assert (isDateVisible(date)) : date + " should be visible";
      getView().removeStyleFromDate(styleName, date);
    }
  }

  /**
   * Sets the date picker to show the given month, use {@link #getFirstDate()}
   * and {@link #getLastDate()} to access the exact date range the date picker
   * chose to display.
   * <p>
   * A datepicker <b> may </b> show days not in the current month. It
   * <b>must</b> show all days in the current month.
   * </p>
   * 
   * @param month the month to show
   */
  public void setCurrentMonth(Date month) {
    getModel().setCurrentMonth(month);
    refreshAll();
  }

  /**
   * Sets a visible date to be enabled or disabled. This is only set until the
   * next time the DatePicker is refreshed.
   * 
   * @param enabled is enabled
   * @param date the date
   * @param moreDates optional dates
   */
  public final void setEnabledOnVisibleDates(boolean enabled, Date date,
      Date... moreDates) {
    assert assertVisible(date, moreDates);
    getView().setEnabledOnDate(enabled, date);
    if (moreDates != null) {
      for (Date d : moreDates) {
        getView().setEnabledOnDate(enabled, d);
      }
    }
  }

  /**
   * Sets a group of visible dates to be enabled or disabled. This is only set
   * until the next time the DatePicker is refreshed.
   * 
   * @param enabled is enabled
   * @param dates the dates
   */
  public final void setEnabledOnVisibleDates(boolean enabled,
      Iterable<Date> dates) {
    CalendarView r = getView();
    for (Date date : dates) {
      assert isDateVisible(date) : date
          + " cannot be enabled or disabled as it is not visible";
      r.setEnabledOnDate(enabled, date);
    }
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
   * @param newValue the new value
   */
  public final void setValue(Date newValue) {
    setValue(newValue, false);
  }

  /**
   * Sets the {@link DatePicker}'s value.
   * 
   * @param newValue the new value for this date picker
   * @param fireEvents should events be fired.
   */
  public final void setValue(Date newValue, boolean fireEvents) {
    Date oldValue = value;

    if (oldValue != null) {
      removeGlobalStyleFromDate(css().dayIsValue(), oldValue);
    }

    value = CalendarUtil.copyDate(newValue);
    if (value != null) {
      addGlobalStyleToDate(css().dayIsValue(), value);
    }
    if (fireEvents) {
      ValueChangeEvent.fireIfNotEqual(this, oldValue, newValue);
    }
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
  protected final MonthSelector getMonthSelector() {
    return monthSelector;
  }

  /**
   * Gets the {@link CalendarView} associated with this date picker.
   * 
   * @return the view
   */
  protected final CalendarView getView() {
    return view;
  }

  /**
   * Refreshes all components of this date picker.
   */
  protected final void refreshAll() {
    highlighted = null;
    getModel().refresh();
    getView().refresh();
    getMonthSelector().refresh();
    ShowRangeEvent.fire(this, getFirstDate(), getLastDate());
  }

  /**
   * Sets up the date picker.
   */
  protected void setup() {
    /*
     * Use a table (VerticalPanel) to get shrink-to-fit behavior. Divs expand to
     * fill the available width, so we'd need to give it a size.
     */ 
    VerticalPanel panel = new VerticalPanel();
    initWidget(panel);
    setStyleName(panel.getElement(), css.datePicker());
    setStyleName(css().datePicker());
    panel.add(this.getMonthSelector());
    panel.add(this.getView());
  }

  /**
   * Gets the css associated with this date picker for use by extended month and
   * cell grids.
   * 
   * @return the css.
   */
  final StandardCss css() {
    return css;
  }

  /**
   * Sets the highlighted date.
   * 
   * @param highlighted highlighted date
   */
  void setHighlightedDate(Date highlighted) {
    this.highlighted = highlighted;
    HighlightEvent.fire(this, highlighted);
  }

  private boolean assertVisible(Date date, Date... moreDates) {
    assert isDateVisible(date) : date + " must be visible";
    if (moreDates != null) {
      for (Date d : moreDates) {
        assert isDateVisible(d) : d + " must be visible";
      }
    }
    return true;
  }

}
