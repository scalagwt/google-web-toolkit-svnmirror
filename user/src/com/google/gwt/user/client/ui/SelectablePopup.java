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

import com.google.gwt.user.client.Window;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class to represent a popup with selectable items and an optional
 * item controller.
 */
public abstract class SelectablePopup extends PopupPanel implements
    SourcesChangeEvents {

  /**
   * Controls the items that display in the selectable popup.
   */
  public abstract static class ItemController {
    /*
     * Implementation note: To allow for an eventual async implementation of a
     * controller, the controller itself must display the actual list of
     * suggestions.
     * 
     */

    private int limit = 20;

    /**
     * Gets the limit on the number of items displayed for a specific input.
     * 
     * @return the limit
     */
    public int getLimit() {
      return limit;
    }

    /**
     * Sets the limit on the number of items displayed for a specific input.
     * 
     * @param limit the limit
     */
    public void setLimit(int limit) {
      this.limit = limit;
    }

    /**
     * Configures and displays a popup.
     * 
     * @param popup popup to show
     * @param input represents the input to the controller
     * @param showBelow <code>UIObject</code> to show the popup below
     */
    public void showBelow(SelectablePopup popup, String input,
        UIObject showBelow) {
      List filteredItems = computeItemsFor(input);
      if (filteredItems.size() > 0) {
        popup.setItems(filteredItems);
        popup.showBelow(showBelow);
      } else {
        popup.hide();
      }
    }

    /**
     * Returns a list of items associated with the given input.
     * 
     * @param input the given input
     * @return list of items
     */
    protected abstract List computeItemsFor(String input);
  }

  /**
   * Selectable item.
   */
  class Item extends HTML {
    /*
     * Implementation note: Item is package protected in order to allow us to
     * change its implementation in the future if a faster implementation
     * becomes available.
     */
    private int index;

    /**
     * 
     * Constructor for <code>Item</code>.
     * 
     * @param index index associated with item
     */
    public Item(int index) {
      this.index = index;
      this.setStyleName(itemStyle);
      this.addMouseListener(defaultMouseListener);
      items.add(index, this);
    }

    /**
     * Gets the index of the item.
     * 
     * @return the item's index
     */
    public int getIndex() {
      return index;
    }

    /**
     * Gets the value of the item.
     * 
     * @return value of the item.
     */
    public Object getValue() {
      if (values != null) {
        return values.get(index);
      } else {
        return getText();
      }
    }

    public String toString() {
      return "value: " + this.getValue() + " index: " + this.getIndex();
    }
  }

  private Item selectedItem;

  private String selectedStyle;

  private String itemStyle;

  private final FlexTable layout = new FlexTable();
  private final MouseListener defaultMouseListener = new MouseListenerAdapter() {

    public void onMouseDown(Widget sender, int x, int y) {
      fireChange();
    }

    public void onMouseEnter(Widget sender) {
      Item item = (Item) sender;
      setSelection(item);
    }
  };
  private ChangeListenerCollection changeListeners = new ChangeListenerCollection();
  private List values;
  private List items = new ArrayList();

  /**
   * Constructor for <code>SelectablePopup</code>. Provides "item" as the
   * default item style, and "selected" as the default selected item style.
   */
  public SelectablePopup() {
    this("item", "selected");
  }

  /**
   * 
   * Constructor for <code>SelectablePopup</code>.
   * 
   * @param itemStyle CSS class name for default items
   * @param selectedItemStyle CSS class name for the currently selected item
   */
  public SelectablePopup(String itemStyle, String selectedItemStyle) {
    super(true);
    this.selectedStyle = selectedItemStyle;
    this.itemStyle = itemStyle;

    // CSS does not effect padding and spacing correctly. So setting to 0
    // here.
    layout.setCellPadding(0);
    layout.setCellSpacing(0);
    this.setWidget(layout);
  }

  public void addChangeListener(ChangeListener listener) {
    if (changeListeners == null) {
      changeListeners = new ChangeListenerCollection();
    }
    changeListeners.add(listener);
  }

  /**
   * Fires all registered change listeners.
   */
  public void fireChange() {
    if (selectedItem == null) {
      throw new RuntimeException("No element is selected");
    }
    changeListeners.fireChange(this);
    this.hide();
  }

  /**
   * Gets the number of items in this selectable popup.
   * 
   * @return number of items
   */
  public int getItemCount() {

    return items.size();
  }

  /**
   * Gets the currently selected index.
   * 
   * @return selected index
   */
  public final int getSelectedIndex() {
    return getSelectedItem().getIndex();
  }

  /**
   * Gets the value associated with the currently selected index.
   * 
   * @return current value
   */
  public final Object getSelectedValue() {
    return getSelectedItem().getValue();
  }

  public void removeChangeListener(ChangeListener listener) {
    this.changeListeners.remove(listener);
  }

  /**
   * Sets the items to be displayed.
   * 
   * @param items items to be displayed
   */
  public abstract void setItems(List items);

  /**
   * Sets the values associated with each item.
   * 
   * @param values values associated with each item
   */
  public void setValues(List values) {
    this.values = values;
  }

  /**
   * Shows the popup, automatically selecting the first item.
   */
  public void show() {
    Item item = getItem(0);
    setSelection(item);
    super.show();
  }

  /**
   * Shows the popup below the given UI object.
   * <p>
   * Note, if the popup would not be visible on the browser, than the popup's
   * position may be adjusted.
   * 
   * @param showBelow the <code>UIObject</code> beneath which the popup should
   *          be shown.
   */
  public void showBelow(UIObject showBelow) {
    // show must be called first, as otherwise getOffsetWidth is not correct.
    show();
    final int left = showBelow.getAbsoluteLeft();
    int overshootLeft = Math.max(0, (left + getOffsetWidth())
        - Window.getClientWidth());

    final int top = showBelow.getAbsoluteTop() + showBelow.getOffsetHeight();

    final int overshootTop = Math.max(0, (top + getOffsetHeight())
        - Window.getClientHeight());

    setPopupPosition(left - overshootLeft, top - overshootTop);
  }

  /**
   * Returns the table that is used to actually render the item layout.
   * 
   * @return layout table
   */
  protected FlexTable getLayout() {
    return layout;
  }

  Item getItem(int index) {
    return (Item) items.get(index);
  }

  /**
   * Gets the currently selected item.
   * 
   * @return selected item.
   */
  Item getSelectedItem() {
    return selectedItem;
  }

  /**
   * Navigate through the popup based upon a key code.
   * 
   * @param keyCode navigation key code
   */
  abstract boolean navigate(char keyCode);

  void setSelection(Item item) {
    if (selectedItem == item) {
      return;
    }
    // Remove old selected item.
    if (selectedItem != null) {
      selectedItem.setStyleName(itemStyle);
    }

    // Add new selected item.
    selectedItem = item;
    if (selectedItem != null) {
      selectedItem.setStyleName(selectedStyle);
    }
  }

  /**
   * Shifts the current selection by the given amount, unless that would put the
   * user past the beginning or end of the list.
   * 
   * @param shift the amount to shift the current selection by.
   */
  void shiftSelection(int offset) {
    int newIndex = getSelectedIndex() + offset;
    if (newIndex < 0 || newIndex >= getItemCount()) {
      return;
    } else {
      Item item = getItem(newIndex);
      setSelection(item);
    }
  }

}
