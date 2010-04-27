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
package com.google.gwt.bikeshed.list.client;

import com.google.gwt.bikeshed.list.client.impl.CellListImpl;
import com.google.gwt.bikeshed.list.shared.ProvidesKey;
import com.google.gwt.bikeshed.list.shared.Range;
import com.google.gwt.bikeshed.list.shared.SelectionModel;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.dom.client.TableElement;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.dom.client.TableSectionElement;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Widget;

import java.util.ArrayList;
import java.util.List;

/**
 * A list view that supports paging and columns.
 *
 * @param <T> the data type of each row
 */
public class CellTable<T> extends Widget implements PagingListView<T> {

  /**
   * Style name applied to even rows.
   */
  private static final String STYLENAME_EVEN = "gwt-cellTable-evenRow";

  /**
   * Style name applied to odd rows.
   */
  private static final String STYLENAME_ODD = "gwt-cellTable-oddRow";

  /**
   * The style name applied to selected rows.
   */
  private static final String STYLENAME_SELECTED = "gwt-cellTable-selectedRow";

  private static final int DEFAULT_SIZE = 10;

  private List<Column<T, ?>> columns = new ArrayList<Column<T, ?>>();
  private List<Header<?>> footers = new ArrayList<Header<?>>();
  private List<Header<?>> headers = new ArrayList<Header<?>>();
  private TableRowElement hoveringRow;
  private final CellListImpl<T> impl;

  /**
   * If null, each T will be used as its own key.
   */
  private ProvidesKey<T> providesKey;

  /**
   * If true, enable selection via the mouse.
   */
  private boolean isSelectionEnabled;

  private TableElement table;
  private TableSectionElement tbody;
  private TableSectionElement tfoot;

  private TableSectionElement thead;

  /**
   * Constructs a table with a default page size of 10.
   */
  public CellTable() {
    this(DEFAULT_SIZE);
  }

  /**
   * Constructs a table with the given page size.
   *
   * @param pageSize the page size
   */
  public CellTable(final int pageSize) {
    setElement(table = Document.get().createTableElement());
    table.setCellSpacing(0);
    thead = table.createTHead();
    table.appendChild(tbody = Document.get().createTBodyElement());
    tfoot = table.createTFoot();

    // Create the implementation.
    this.impl = new CellListImpl<T>(this, pageSize, tbody) {

      private final TableElement tmpElem = Document.get().createTableElement();

      @Override
      public void setData(List<T> values, int start) {
        createHeadersAndFooters();
        super.setData(values, start);
      }

      @Override
      protected Element convertToElements(String html) {
        tmpElem.setInnerHTML(html);
        return tmpElem.getTBodies().getItem(0);
      }

      @Override
      protected boolean dependsOnSelection() {
        for (Column<T, ?> column : columns) {
          if (column.dependsOnSelection()) {
            return true;
          }
        }
        return false;
      }

      @Override
      protected void emitHtml(StringBuilder sb, List<T> values, int start,
          SelectionModel<? super T> selectionModel) {
        int length = values.size();
        int end = start + length;
        for (int i = start; i < end; i++) {
          T value = values.get(i - start);
          boolean isSelected = (selectionModel == null || value == null) ? false
              : selectionModel.isSelected(value);
          sb.append("<tr __idx='").append(i).append("'");
          sb.append(" class='");
          sb.append(i % 2 == 0 ? STYLENAME_EVEN : STYLENAME_ODD);
          if (isSelected) {
            sb.append(" ").append(STYLENAME_SELECTED);
          }
          sb.append("'>");
          for (Column<T, ?> column : columns) {
            // TODO(jlabanca): How do we sink ONFOCUS and ONBLUR?
            sb.append("<td>");
            if (value != null) {
              column.render(value, sb);
            }
            sb.append("</td>");
          }
          sb.append("</tr>");
        }
      }

      @Override
      protected void setSelected(Element elem, boolean selected) {
        setStyleName(elem, STYLENAME_SELECTED, selected);
      }

      @Override
      protected void updateSelection() {
        // Refresh headers.
        for (Header<?> header : headers) {
          if (header != null && header.dependsOnSelection()) {
            createHeaders(headers, thead);
            break;
          }
        }

        // Refresh footers.
        for (Header<?> footer : footers) {
          if (footer != null && footer.dependsOnSelection()) {
            createHeaders(footers, tfoot);
            break;
          }
        }

        // Update data.
        super.updateSelection();
      }
    };

    setPageSize(pageSize);

    // TODO: Total hack. It would almost definitely be preferable to sink only
    // those events actually needed by cells.
    sinkEvents(Event.ONCLICK | Event.MOUSEEVENTS | Event.KEYEVENTS
        | Event.ONCHANGE | Event.FOCUSEVENTS);
  }

  /**
   * Adds a column to the table.
   */
  public void addColumn(Column<T, ?> col) {
    addColumn(col, null, null);
  }

  /**
   * Adds a column to the table with an associated header.
   */
  public void addColumn(Column<T, ?> col, Header<?> header) {
    addColumn(col, header, null);
  }

  /**
   * Adds a column to the table with an associated header and footer.
   */
  public void addColumn(Column<T, ?> col, Header<?> header, Header<?> footer) {
    headers.add(header);
    footers.add(footer);
    columns.add(col);
    refresh();
  }

  /**
   * Adds a column to the table with an associated String header.
   */
  public void addColumn(Column<T, ?> col, String headerString) {
    addColumn(col, new TextHeader(headerString), null);
  }

  // TODO: remove(Column)

  public int getBodyHeight() {
    int height = getClientHeight(tbody);
    return height;
  }

  public int getDataSize() {
    return impl.getDataSize();
  }

  public T getDisplayedItem(int indexOnPage) {
    if (indexOnPage < 0 || indexOnPage >= getNumDisplayedItems()) {
      throw new IndexOutOfBoundsException("indexOnPage = " + indexOnPage);
    }
    return impl.getData().get(indexOnPage);
  }

  public List<T> getDisplayedItems() {
    return new ArrayList<T>(impl.getData());
  }

  public int getHeaderHeight() {
    int height = getClientHeight(thead);
    return height;
  }

  public int getNumDisplayedItems() {
    return impl.getDisplayedItemCount();
  }

  public int getPageSize() {
    return impl.getPageSize();
  }

  public int getPageStart() {
    return impl.getPageStart();
  }

  public ProvidesKey<T> getProvidesKey() {
    return providesKey;
  }

  public Range getRange() {
    return impl.getRange();
  }

  public int getSize() {
    return impl.getDataSize();
  }

  /**
   * Check whether or not mouse selection is enabled.
   *
   * @return true if enabled, false if disabled
   */
  public boolean isSelectionEnabled() {
    return isSelectionEnabled;
  }

  @Override
  public void onBrowserEvent(Event event) {
    super.onBrowserEvent(event);

    // Find the cell where the event occurred.
    EventTarget eventTarget = event.getEventTarget();
    TableCellElement cell = null;
    if (eventTarget != null && Element.is(eventTarget)) {
      cell = findNearestParentCell(Element.as(eventTarget));
    }
    if (cell == null) {
      return;
    }

    // Forward the event to the associated header, footer, or column.
    TableRowElement tr = TableRowElement.as(cell.getParentElement());
    TableSectionElement section = TableSectionElement.as(tr.getParentElement());
    int col = cell.getCellIndex();
    if (section == thead) {
      Header<?> header = headers.get(col);
      if (header != null) {
        header.onBrowserEvent(cell, event);
      }
    } else if (section == tfoot) {
      Header<?> footer = footers.get(col);
      if (footer != null) {
        footer.onBrowserEvent(cell, event);
      }
    } else if (section == tbody) {
      int row = tr.getSectionRowIndex();

      if (event.getType().equals("mouseover")) {
        if (hoveringRow != null) {
          hoveringRow.removeClassName("hover");
        }
        hoveringRow = tr;
        tr.addClassName("hover");
      } else if (event.getType().equals("mouseout")) {
        hoveringRow = null;
        tr.removeClassName("hover");
      }

      T value = impl.getData().get(row);
      Column<T, ?> column = columns.get(col);
      column.onBrowserEvent(cell, impl.getPageStart() + row, value, event,
          providesKey);

      // Update selection.
      if (isSelectionEnabled && event.getTypeInt() == Event.ONMOUSEDOWN) {
        SelectionModel<? super T> selectionModel = impl.getSelectionModel();
        if (selectionModel != null) {
          selectionModel.setSelected(value, true);
        }
      }
    }
  }

  /**
   * Redraw the table, requesting data from the delegate.
   */
  public void refresh() {
    impl.refresh();
  }

  public void setData(int start, int length, List<T> values) {
    impl.setData(values, start);
  }

  public void setDataSize(int size, boolean isExact) {
    impl.setDataSize(size);
  }

  public void setDelegate(Delegate<T> delegate) {
    impl.setDelegate(delegate);
  }

  public void setPager(PagingListView.Pager<T> pager) {
    impl.setPager(pager);
  }

  /**
   * Set the number of rows per page and refresh the table.
   *
   * @param pageSize the page size
   *
   * @throw {@link IllegalArgumentException} if pageSize is negative or 0
   */
  public void setPageSize(int pageSize) {
    impl.setPageSize(pageSize);
  }

  /**
   * Set the starting index of the current visible page. The actual page start
   * will be clamped in the range [0, getSize() - 1].
   *
   * @param pageStart the index of the row that should appear at the start of
   *          the page
   */
  public void setPageStart(int pageStart) {
    impl.setPageStart(pageStart);
  }

  /**
   * Sets the {@link ProvidesKey} instance that will be used to generate keys
   * for each record object as needed.
   *
   * @param providesKey an instance of {@link ProvidesKey} used to generate keys
   *          for record objects.
   */
  // TODO - when is this valid? Do we rehash column view data if it changes?
  public void setProvidesKey(ProvidesKey<T> providesKey) {
    this.providesKey = providesKey;
  }

  /**
   * Enable mouse and keyboard selection.
   *
   * @param isSelectionEnabled true to enable, false to disable
   */
  public void setSelectionEnabled(boolean isSelectionEnabled) {
    this.isSelectionEnabled = isSelectionEnabled;
  }

  public void setSelectionModel(SelectionModel<? super T> selectionModel) {
    impl.setSelectionModel(selectionModel, true);
  }

  private void createHeaders(List<Header<?>> headers,
      TableSectionElement section) {
    StringBuilder sb = new StringBuilder();
    sb.append("<tr>");
    for (Header<?> header : headers) {
      sb.append("<th>");
      if (header != null) {
        header.render(sb);
      }
      sb.append("</th>");
    }
    sb.append("</tr>");

    section.setInnerHTML(sb.toString());
  }

  private void createHeadersAndFooters() {
    createHeaders(headers, thead);
    createHeaders(footers, tfoot);
  }

  private TableCellElement findNearestParentCell(Element elem) {
    while ((elem != null) && (elem != table)) {
      // TODO: We need is() implementations in all Element subclasses.
      // This would allow us to use TableCellElement.is() -- much cleaner.
      String tagName = elem.getTagName();
      if ("td".equalsIgnoreCase(tagName) || "th".equalsIgnoreCase(tagName)) {
        return elem.cast();
      }
      elem = elem.getParentElement();
    }
    return null;
  }

  private native int getClientHeight(Element element) /*-{
    return element.clientHeight;
  }-*/;
}
