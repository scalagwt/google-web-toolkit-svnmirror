/*
 * Copyright 2010 Google Inc.
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
package com.google.gwt.view.client;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;

import java.util.List;

/**
 * A mock {@link HasData} used for testing.
 *
 * @param <T> the data type
 */
public class MockHasData<T> implements HasData<T> {

  private static final int DEFAULT_PAGE_SIZE = 10;

  /**
   * A mock {@link RowCountChangeEvent.Handler} used for testing.
   */
  public static class MockRowCountChangeHandler
      implements RowCountChangeEvent.Handler {

    private int lastRowCount = -1;
    private boolean lastRowCountExact;

    public int getLastRowCount() {
      return lastRowCount;
    }

    public boolean isLastRowCountExact() {
      return lastRowCountExact;
    }

    public void onRowCountChange(RowCountChangeEvent event) {
      this.lastRowCount = event.getNewRowCount();
      this.lastRowCountExact = event.isNewRowCountExact();
    }

    public void reset() {
      lastRowCount = -1;
      lastRowCountExact = false;
    }
  }

  /**
   * A mock {@link RangeChangeEvent.Handler} used for testing.
   */
  public static class MockRangeChangeHandler
      implements RangeChangeEvent.Handler {

    private Range lastRange;

    public Range getLastRange() {
      return lastRange;
    }

    public void onRangeChange(RangeChangeEvent event) {
      this.lastRange = event.getNewRange();
    }

    public void reset() {
      lastRange = null;
    }
  }

  private final HandlerManager handlerManager = new HandlerManager(this);
  private Range lastRange;
  private List<T> lastRowData;

  private int pageStart;
  private int pageSize = DEFAULT_PAGE_SIZE;
  private int rowCount;
  private boolean rowCountExact;
  private HandlerRegistration selectionHandler;
  private SelectionModel<? super T> selectionModel;

  public HandlerRegistration addRangeChangeHandler(
      RangeChangeEvent.Handler handler) {
    return handlerManager.addHandler(RangeChangeEvent.getType(), handler);
  }

  public HandlerRegistration addRowCountChangeHandler(
      RowCountChangeEvent.Handler handler) {
    return handlerManager.addHandler(RowCountChangeEvent.getType(), handler);
  }

  /**
   * Clear the last data set by {@link #setRowData(int, List)}.
   */
  public void clearLastRowDataAndRange() {
    lastRowData = null;
    lastRange = null;
  }

  public void fireEvent(GwtEvent<?> event) {
    handlerManager.fireEvent(event);
  }

  /**
   * Get the last data set in {@link #setRowData(int, List)}.
   *
   * @return the last data set
   */
  public List<T> getLastRowData() {
    return lastRowData;
  }

  /**
   * Get the last data range set in {@link #setRowData(int, List)}.
   *
   * @return the last data range
   */
  public Range getLastRowDataRange() {
    return lastRange;
  }

  public int getRowCount() {
    return rowCount;
  }

  public SelectionModel<? super T> getSelectionModel() {
    return selectionModel;
  }

  public Range getVisibleRange() {
    return new Range(pageStart, pageSize);
  }

  public boolean isRowCountExact() {
    return rowCountExact;
  }

  public void setRowData(int start, List<T> values) {
    lastRange = new Range(start, values.size());
    lastRowData = values;
  }

  public final void setRowCount(int count) {
    setRowCount(count, true);
  }

  public void setRowCount(int count, boolean isExact) {
    if (this.rowCount == count && this.rowCountExact == isExact) {
      return;
    }
    this.rowCount = count;
    this.rowCountExact = isExact;
    RowCountChangeEvent.fire(this, count, isExact);
  }

  public final void setVisibleRange(int start, int length) {
    setVisibleRange(new Range(start, length));
  }

  public void setVisibleRange(Range range) {
    setVisibleRange(range, false, false);
  }

  public void setVisibleRangeAndClearData(
      Range range, boolean forceRangeChangeEvent) {
    setVisibleRange(range, true, forceRangeChangeEvent);
  }

  public void setSelectionModel(SelectionModel<? super T> selectionModel) {
    // Remove the old selection handler.
    if (selectionHandler != null) {
      selectionHandler.removeHandler();
      selectionHandler = null;
    }

    // Add the new selection model.
    this.selectionModel = selectionModel;
    if (selectionModel != null) {
      selectionHandler = selectionModel.addSelectionChangeHandler(
          new SelectionChangeEvent.Handler() {
            public void onSelectionChange(SelectionChangeEvent event) {
            }
          });
    }
  }

  private void setVisibleRange(
      Range range, boolean clearData, boolean forceRangeChangeEvent) {
    int start = range.getStart();
    int length = range.getLength();
    if (clearData) {
      lastRowData = null;
    }
    if (!forceRangeChangeEvent && this.pageStart == start
        && this.pageSize == length) {
      return;
    }
    this.pageStart = start;
    this.pageSize = length;
    RangeChangeEvent.fire(this, getVisibleRange());
  }
}