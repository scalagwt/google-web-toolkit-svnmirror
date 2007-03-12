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
import java.util.Iterator;
import java.util.List;

/**
 * Helpful base implementation of the {@link ItemPicker} interface.
 * 
 */
public abstract class AbstractItemPicker extends Composite implements
    ItemPicker {

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
      this.setStyleName(itemStyleName);
      this.addMouseListener(itemMouseListener);
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

    public AbstractItemPicker getOwner() {
      return AbstractItemPicker.this;
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

  /**
   * Each item is given this mouse listener in order to allow the mouse to
   * highlight each item in turn.
   */
  private static final MouseListener itemMouseListener = new MouseListenerAdapter() {
    public void onMouseDown(Widget sender, int x, int y) {
      // Unlike buttons, a item picker is selected as soon as the mouse down is
      // fired.

      Item item = (Item) sender;
      item.getOwner().click();
    }

    public void onMouseEnter(Widget sender) {
      Item item = (Item) sender;
      item.getOwner().setSelection(item);
    }
  };
  private static final ClickListener itemClickListener = new ClickListener() {
    public void onClick(Widget sender) {
      Item item = (Item) sender;
      item.getOwner().clickListeners.fireClick(item);
    }
  };

  private static final String STYLE_SELECTED_ITEM = "selected";

  private static final String STYLE_ITEM = "item";

  private ChangeListenerCollection changeListeners = new ChangeListenerCollection();
  private ClickListenerCollection clickListeners = new ClickListenerCollection();
  private Item selectedItem;
  private final String selectedStyleName;
  private final String itemStyleName;

  private List values;
  private List items = new ArrayList();

  {
    initWidget(new FlexTable());
  }

  /**
   * Constructor for <code>ItemPicker</code>. Provides "item" as the default
   * item style, and "selected" as the default selected item style.
   */
  public AbstractItemPicker() {
    this(STYLE_ITEM, STYLE_SELECTED_ITEM);
  }

  /**
   * 
   * Constructor for <code>ItemPicker</code>.
   * 
   * @param itemStyleName CSS class name for default items
   * @param selectedItemStyleName CSS class name for the currently selected item
   */
  public AbstractItemPicker(String itemStyleName, String selectedItemStyleName) {
    this.selectedStyleName = selectedItemStyleName;
    this.itemStyleName = itemStyleName;

    // CSS does not effect padding and spacing correctly. So setting to 0
    // here.
    getLayout().setCellPadding(0);
    getLayout().setCellSpacing(0);
  }

  public void addChangeListener(ChangeListener listener) {
    if (changeListeners == null) {
      changeListeners = new ChangeListenerCollection();
    }
    changeListeners.add(listener);
  }

  public void addClickListener(ClickListener listener) {
    if (clickListeners == null) {
      clickListeners = new ClickListenerCollection();
    }
    clickListeners.add(listener);
  }

  public void click() {
    if (selectedItem == null) {
      throw new RuntimeException("No element is selected");
    }
    changeListeners.fireChange(this);
  }

  public int getItemCount() {
    return items.size();
  }

  public final int getSelectedIndex() {
    if (getSelectedItem() == null) {
      return -1;
    }
    return getSelectedItem().getIndex();
  }

  public final Object getSelectedValue() {
    if (getSelectedItem() == null) {
      return null;
    }
    return getSelectedItem().getValue();
  }

  public final Object getValue(int index) {
    return getItem(index).getValue();
  }

  public abstract boolean navigate(char keyCode);

  public void removeChangeListener(ChangeListener listener) {
    this.changeListeners.remove(listener);
  }

  public void removeClickListener(ClickListener listener) {
    if (clickListeners != null) {
      clickListeners.remove(listener);
    }
  }

  public abstract void setItems(Iterator items);

  public void setSelectedIndex(int index) {
    Item item = getItem(index);
    setSelection(item);
  }

  public void shiftSelection(int offset) {
    int newIndex = getSelectedIndex() + offset;
    if (newIndex < 0 || newIndex >= getItemCount()) {
      return;
    } else {
      Item item = getItem(newIndex);
      setSelection(item);
    }
  }

  protected FlexTable getLayout() {
    return (FlexTable) getWidget();
  }

  /**
   * Gets the ith item.
   * 
   * @param index index of item
   * @return the ith item
   */
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
   * Sets the current selection.
   * 
   * @param item item to set
   */
  void setSelection(Item item) {
    if (selectedItem == item) {
      return;
    }

    // Remove old selected item.
    if (selectedItem != null) {
      selectedItem.setStyleName(itemStyleName);
    }

    // Add new selected item.
    selectedItem = item;
    if (selectedItem != null) {
      selectedItem.setStyleName(selectedStyleName);
    }
  }
}
