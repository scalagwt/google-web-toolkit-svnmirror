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
package com.google.gwt.user.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyCodeEvent;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.PopupPanel.AnimationType;

import java.util.ArrayList;
import java.util.List;

/**
 * A standard menu bar widget. A menu bar can contain any number of menu items,
 * each of which can either fire a {@link com.google.gwt.user.client.Command} or
 * open a cascaded menu bar.
 * 
 * <p>
 * <img class='gallery' src='MenuBar.png'/>
 * </p>
 * 
 * <h3>CSS Style Rules</h3> <ul class='css'> <li>.gwt-MenuBar { the menu bar
 * itself }</li> <li>.gwt-MenuBar-horizontal { dependent style applied to
 * horizontal menu bars }</li> <li>.gwt-MenuBar-vertical { dependent style
 * applied to vertical menu bars }</li> <li>.gwt-MenuBar .gwt-MenuItem { menu
 * items }</li> <li>.gwt-MenuBar .gwt-MenuItem-selected { selected menu items }</li>
 * <li>.gwt-MenuBar .gwt-MenuItemSeparator { section breaks between menu items }
 * </li> <li>.gwt-MenuBar .gwt-MenuItemSeparator .menuSeparatorInner { inner
 * component of section separators }</li> <li>.gwt-MenuBarPopup
 * .menuPopupTopLeft { the top left cell }</li> <li>.gwt-MenuBarPopup
 * .menuPopupTopLeftInner { the inner element of the cell }</li> <li>
 * .gwt-MenuBarPopup .menuPopupTopCenter { the top center cell }</li> <li>
 * .gwt-MenuBarPopup .menuPopupTopCenterInner { the inner element of the cell }</li>
 * <li>.gwt-MenuBarPopup .menuPopupTopRight { the top right cell }</li> <li>
 * .gwt-MenuBarPopup .menuPopupTopRightInner { the inner element of the cell }</li>
 * <li>.gwt-MenuBarPopup .menuPopupMiddleLeft { the middle left cell }</li> <li>
 * .gwt-MenuBarPopup .menuPopupMiddleLeftInner { the inner element of the cell }
 * </li> <li>.gwt-MenuBarPopup .menuPopupMiddleCenter { the middle center cell }
 * </li> <li>.gwt-MenuBarPopup .menuPopupMiddleCenterInner { the inner element
 * of the cell }</li> <li>.gwt-MenuBarPopup .menuPopupMiddleRight { the middle
 * right cell }</li> <li>.gwt-MenuBarPopup .menuPopupMiddleRightInner { the
 * inner element of the cell }</li> <li>.gwt-MenuBarPopup .menuPopupBottomLeft {
 * the bottom left cell }</li> <li>.gwt-MenuBarPopup .menuPopupBottomLeftInner {
 * the inner element of the cell }</li> <li>.gwt-MenuBarPopup
 * .menuPopupBottomCenter { the bottom center cell }</li> <li>.gwt-MenuBarPopup
 * .menuPopupBottomCenterInner { the inner element of the cell }</li> <li>
 * .gwt-MenuBarPopup .menuPopupBottomRight { the bottom right cell }</li> <li>
 * .gwt-MenuBarPopup .menuPopupBottomRightInner { the inner element of the cell
 * }</li> </ul>
 * 
 * <p>
 * <h3>Example</h3>
 * {@example com.google.gwt.examples.MenuBarExample}
 * </p>
 */
public class MenuBar extends Widget implements PopupListener, HasAnimation {
  private static final String STYLENAME_DEFAULT = "gwt-MenuBar";

  /**
   * An {@link ImageBundle} that provides images for {@link MenuBar}.
   */
  public interface MenuBarImages extends ImageBundle {
    /**
     * An image indicating a {@link MenuItem} has an associated submenu.
     * 
     * @return a prototype of this image
     */
    AbstractImagePrototype menuBarSubMenuIcon();
  }

  /**
   * A bundle containing the RTL versions of the images for MenuBar.
   * 
   * Notice that this interface is package protected. This interface need not be
   * publicly exposed, as it is only used by the MenuBar class to provide RTL
   * versions of the images in the case the the user does not pass in their own
   * bundle. However, we cannot make this class private, because the generated
   * class needs to be able to extend this class.
   */
  interface MenuBarImagesRTL extends MenuBarImages {
    /**
     * An image indicating a {@link MenuItem} has an associated submenu for a
     * RTL context.
     * 
     * @return a prototype of this image
     */
    @Resource("menuBarSubMenuIcon_rtl.gif")
    AbstractImagePrototype menuBarSubMenuIcon();
  }

  /**
   * List of all {@link MenuItem}s and {@link MenuItemSeparator}s.
   */
  private ArrayList<UIObject> allItems = new ArrayList<UIObject>();

  /**
   * List of {@link MenuItem}s, not including {@link MenuItemSeparator}s.
   */
  private ArrayList<MenuItem> items = new ArrayList<MenuItem>();

  private Element body;

  private MenuBarImages images = null;
  private boolean isAnimationEnabled = false;
  private MenuBar parentMenu;
  private PopupPanel popup;
  private MenuItem selectedItem;
  private MenuBar shownChildMenu;
  private boolean vertical, autoOpen;

  /**
   * Creates an empty horizontal menu bar.
   */
  public MenuBar() {
    this(false);
  }

  /**
   * Creates an empty menu bar.
   * 
   * @param vertical <code>true</code> to orient the menu bar vertically
   */
  public MenuBar(boolean vertical) {
    super();
    if (LocaleInfo.getCurrentLocale().isRTL()) {
      init(vertical, GWT.<MenuBarImagesRTL> create(MenuBarImagesRTL.class));
    } else {
      init(vertical, GWT.<MenuBarImages> create(MenuBarImages.class));
    }
  }

  /**
   * Creates an empty menu bar that uses the specified image bundle for menu
   * images.
   * 
   * @param vertical <code>true</code> to orient the menu bar vertically
   * @param images a bundle that provides images for this menu
   */
  public MenuBar(boolean vertical, MenuBarImages images) {
    init(vertical, images);
  }

  /**
   * Creates an empty horizontal menu bar that uses the specified image bundle
   * for menu images.
   * 
   * @param images a bundle that provides images for this menu
   */
  public MenuBar(MenuBarImages images) {
    this(false, images);
  }

  /**
   * Adds a menu item to the bar.
   * 
   * @param item the item to be added
   * @return the {@link MenuItem} object
   */
  public MenuItem addItem(MenuItem item) {
    return insertItem(item, allItems.size());
  }

  /**
   * Adds a menu item to the bar, that will fire the given command when it is
   * selected.
   * 
   * @param text the item's text
   * @param asHTML <code>true</code> to treat the specified text as html
   * @param cmd the command to be fired
   * @return the {@link MenuItem} object created
   */
  public MenuItem addItem(String text, boolean asHTML, Command cmd) {
    return addItem(new MenuItem(text, asHTML, cmd));
  }

  /**
   * Adds a menu item to the bar, that will open the specified menu when it is
   * selected.
   * 
   * @param text the item's text
   * @param asHTML <code>true</code> to treat the specified text as html
   * @param popup the menu to be cascaded from it
   * @return the {@link MenuItem} object created
   */
  public MenuItem addItem(String text, boolean asHTML, MenuBar popup) {
    return addItem(new MenuItem(text, asHTML, popup));
  }

  /**
   * Adds a menu item to the bar, that will fire the given command when it is
   * selected.
   * 
   * @param text the item's text
   * @param cmd the command to be fired
   * @return the {@link MenuItem} object created
   */
  public MenuItem addItem(String text, Command cmd) {
    return addItem(new MenuItem(text, cmd));
  }

  /**
   * Adds a menu item to the bar, that will open the specified menu when it is
   * selected.
   * 
   * @param text the item's text
   * @param popup the menu to be cascaded from it
   * @return the {@link MenuItem} object created
   */
  public MenuItem addItem(String text, MenuBar popup) {
    return addItem(new MenuItem(text, popup));
  }

  /**
   * Adds a thin line to the {@link MenuBar} to separate sections of
   * {@link MenuItem}s.
   * 
   * @return the {@link MenuItemSeparator} object created
   */
  public MenuItemSeparator addSeparator() {
    return addSeparator(new MenuItemSeparator());
  }

  /**
   * Adds a thin line to the {@link MenuBar} to separate sections of
   * {@link MenuItem}s.
   * 
   * @param separator the {@link MenuItemSeparator} to be added
   * @return the {@link MenuItemSeparator} object
   */
  public MenuItemSeparator addSeparator(MenuItemSeparator separator) {
    return insertSeparator(separator, allItems.size());
  }

  /**
   * Removes all menu items from this menu bar.
   */
  public void clearItems() {
    // Deselect the current item
    selectItem(null);

    Element container = getItemContainerElement();
    while (DOM.getChildCount(container) > 0) {
      DOM.removeChild(container, DOM.getChild(container, 0));
    }

    // Set the parent of all items to null
    for (UIObject item : allItems) {
      setItemColSpan(item, 1);
      if (item instanceof MenuItemSeparator) {
        ((MenuItemSeparator) item).setParentMenu(null);
      } else {
        ((MenuItem) item).setParentMenu(null);
      }
    }

    // Clear out all of the items and separators
    items.clear();
    allItems.clear();
  }

  /**
   * Gets whether this menu bar's child menus will open when the mouse is moved
   * over it.
   * 
   * @return <code>true</code> if child menus will auto-open
   */
  public boolean getAutoOpen() {
    return autoOpen;
  }

  /**
   * Get the index of a {@link MenuItem}.
   * 
   * @return the index of the item, or -1 if it is not contained by this MenuBar
   */
  public int getItemIndex(MenuItem item) {
    return allItems.indexOf(item);
  }

  /**
   * Get the index of a {@link MenuItemSerpator}.
   * 
   * @return the index of the separator, or -1 if it is not contained by this
   *         MenuBar
   */
  public int getSeparatorIndex(MenuItemSeparator item) {
    return allItems.indexOf(item);
  }

  /**
   * Adds a menu item to the bar at a specific index.
   * 
   * @param item the item to be inserted
   * @param beforeIndex the index where the item should be inserted
   * @return the {@link MenuItem} object
   * @throws IndexOutOfBoundsException if <code>beforeIndex</code> is out of
   *           range
   */
  public MenuItem insertItem(MenuItem item, int beforeIndex)
      throws IndexOutOfBoundsException {
    // Check the bounds
    if (beforeIndex < 0 || beforeIndex > allItems.size()) {
      throw new IndexOutOfBoundsException();
    }

    // Add to the list of items
    allItems.add(beforeIndex, item);
    int itemsIndex = 0;
    for (int i = 0; i < beforeIndex; i++) {
      if (allItems.get(i) instanceof MenuItem) {
        itemsIndex++;
      }
    }
    items.add(itemsIndex, item);

    // Setup the menu item
    addItemElement(beforeIndex, item.getElement());
    item.setParentMenu(this);
    item.setSelectionStyle(false);
    updateSubmenuIcon(item);
    return item;
  }

  /**
   * Adds a thin line to the {@link MenuBar} to separate sections of
   * {@link MenuItem}s at the specified index.
   * 
   * @param beforeIndex the index where the seperator should be inserted
   * @return the {@link MenuItemSeparator} object
   * @throws IndexOutOfBoundsException if <code>beforeIndex</code> is out of
   *           range
   */
  public MenuItemSeparator insertSeparator(int beforeIndex) {
    return insertSeparator(new MenuItemSeparator(), beforeIndex);
  }

  /**
   * Adds a thin line to the {@link MenuBar} to separate sections of
   * {@link MenuItem}s at the specified index.
   * 
   * @param separator the {@link MenuItemSeparator} to be inserted
   * @param beforeIndex the index where the seperator should be inserted
   * @return the {@link MenuItemSeparator} object
   * @throws IndexOutOfBoundsException if <code>beforeIndex</code> is out of
   *           range
   */
  public MenuItemSeparator insertSeparator(MenuItemSeparator separator,
      int beforeIndex) throws IndexOutOfBoundsException {
    // Check the bounds
    if (beforeIndex < 0 || beforeIndex > allItems.size()) {
      throw new IndexOutOfBoundsException();
    }

    if (vertical) {
      setItemColSpan(separator, 2);
    }
    addItemElement(beforeIndex, separator.getElement());
    separator.setParentMenu(this);
    allItems.add(beforeIndex, separator);
    return separator;
  }

  public boolean isAnimationEnabled() {
    return isAnimationEnabled;
  }

  @Override
  public void onBrowserEvent(Event event) {
    super.onBrowserEvent(event);

    MenuItem item = findItem(DOM.eventGetTarget(event));
    switch (DOM.eventGetType(event)) {
      case Event.ONCLICK: {
        FocusPanel.impl.focus(getElement());
        // Fire an item's command when the user clicks on it.
        if (item != null) {
          doItemAction(item, true);
        }
        break;
      }

      case Event.ONMOUSEOVER: {
        if (item != null) {
          itemOver(item, true);
        }
        break;
      }

      case Event.ONMOUSEOUT: {
        if (item != null) {
          itemOver(null, true);
        }
        break;
      }

      case Event.ONFOCUS: {
        selectFirstItemIfNoneSelected();
        break;
      }

      case Event.ONKEYDOWN: {
        int keyCode = DOM.eventGetKeyCode(event);
        switch (keyCode) {
          case KeyCodeEvent.KEY_LEFT:
            if (LocaleInfo.getCurrentLocale().isRTL()) {
              moveToNextItem();
            } else {
              moveToPrevItem();
            }
            eatEvent(event);
            break;
          case KeyCodeEvent.KEY_RIGHT:
            if (LocaleInfo.getCurrentLocale().isRTL()) {
              moveToPrevItem();
            } else {
              moveToNextItem();
            }
            eatEvent(event);
            break;
          case KeyCodeEvent.KEY_UP:
            moveUp();
            eatEvent(event);
            break;
          case KeyCodeEvent.KEY_DOWN:
            moveDown();
            eatEvent(event);
            break;
          case KeyCodeEvent.KEY_ESCAPE:
            closeAllParents();
            eatEvent(event);
            break;
          case KeyCodeEvent.KEY_ENTER:
            if (!selectFirstItemIfNoneSelected()) {
              doItemAction(selectedItem, true);
              eatEvent(event);
            }
            break;
        } // end switch(keyCode)

        break;
      } // end case Event.ONKEYDOWN
    } // end switch (DOM.eventGetType(event))
  }

  public void onPopupClosed(PopupPanel sender, boolean autoClosed) {
    // If the menu popup was auto-closed, close all of its parents as well.
    if (autoClosed) {
      closeAllParents();
    }

    // When the menu popup closes, remember that no item is
    // currently showing a popup menu.
    onHide();
    shownChildMenu = null;
    popup = null;
  }

  /**
   * Removes the specified menu item from the bar.
   * 
   * @param item the item to be removed
   */
  public void removeItem(MenuItem item) {
    // Unselect if the item is currently selected
    if (selectedItem == item) {
      selectItem(null);
    }

    if (removeItemElement(item)) {
      setItemColSpan(item, 1);
      items.remove(item);
      item.setParentMenu(null);
    }
  }

  /**
   * Removes the specified {@link MenuItemSeparator} from the bar.
   * 
   * @param separator the separator to be removed
   */
  public void removeSeparator(MenuItemSeparator separator) {
    if (removeItemElement(separator)) {
      separator.setParentMenu(null);
    }
  }

  public void setAnimationEnabled(boolean enable) {
    isAnimationEnabled = enable;
  }

  /**
   * Sets whether this menu bar's child menus will open when the mouse is moved
   * over it.
   * 
   * @param autoOpen <code>true</code> to cause child menus to auto-open
   */
  public void setAutoOpen(boolean autoOpen) {
    this.autoOpen = autoOpen;
  }

  /**
   * Returns a list containing the <code>MenuItem</code> objects in the menu
   * bar. If there are no items in the menu bar, then an empty <code>List</code>
   * object will be returned.
   * 
   * @return a list containing the <code>MenuItem</code> objects in the menu bar
   */
  protected List<MenuItem> getItems() {
    return this.items;
  }

  /**
   * Returns the <code>MenuItem</code> that is currently selected (highlighted)
   * by the user. If none of the items in the menu are currently selected, then
   * <code>null</code> will be returned.
   * 
   * @return the <code>MenuItem</code> that is currently selected, or
   *         <code>null</code> if no items are currently selected
   */
  protected MenuItem getSelectedItem() {
    return this.selectedItem;
  }

  @Override
  protected void onDetach() {
    // When the menu is detached, make sure to close all of its children.
    if (popup != null) {
      popup.hide();
    }

    super.onDetach();
  }

  /**
   * <b>Affected Elements:</b>
   * <ul>
   * <li>-item# = the {@link MenuItem} at the specified index.</li>
   * </ul>
   * 
   * @see UIObject#onEnsureDebugId(String)
   */
  @Override
  protected void onEnsureDebugId(String baseID) {
    super.onEnsureDebugId(baseID);
    setMenuItemDebugIds(baseID);
  }

  /*
   * Closes all parent menu popups.
   */
  void closeAllParents() {
    MenuBar curMenu = this;
    while (curMenu.parentMenu != null) {
      curMenu.close();
      curMenu = curMenu.parentMenu;
    }
  }

  /*
   * Performs the action associated with the given menu item. If the item has a
   * popup associated with it, the popup will be shown. If it has a command
   * associated with it, and 'fireCommand' is true, then the command will be
   * fired. Popups associated with other items will be hidden.
   * 
   * @param item the item whose popup is to be shown. @param fireCommand
   * <code>true</code> if the item's command should be fired, <code>false</code>
   * otherwise.
   */
  void doItemAction(final MenuItem item, boolean fireCommand) {
    // Ensure that the item is selected.
    selectItem(item);

    if (item != null) {
      // if the command should be fired and the item has one, fire it
      if (fireCommand && item.getCommand() != null) {
        // Close this menu and all of its parents.
        closeAllParents();

        // Fire the item's command.
        Command cmd = item.getCommand();
        DeferredCommand.addCommand(cmd);

        // hide any open submenus of this item
        if (shownChildMenu != null) {
          shownChildMenu.onHide();
          popup.hide();
          shownChildMenu = null;
          selectItem(null);
        }
      } else if (item.getSubMenu() != null) {
        if (shownChildMenu == null) {
          // open this submenu
          openPopup(item);
        } else if (item.getSubMenu() != shownChildMenu) {
          // close the other submenu and open this one
          shownChildMenu.onHide();
          popup.hide();
          openPopup(item);
        } else if (fireCommand && !autoOpen) {
          // close this submenu
          shownChildMenu.onHide();
          popup.hide();
          shownChildMenu = null;
          selectItem(item);
        }
      } else if (autoOpen && shownChildMenu != null) {
        // close submenu
        shownChildMenu.onHide();
        popup.hide();
        shownChildMenu = null;
      }
    }
  }

  void itemOver(MenuItem item, boolean focus) {
    if (item == null) {
      // Don't clear selection if the currently selected item's menu is showing.
      if ((selectedItem != null)
          && (shownChildMenu == selectedItem.getSubMenu())) {
        return;
      }
    }

    // Style the item selected when the mouse enters.
    selectItem(item);
    if (focus) {
      focus();
    }

    // If child menus are being shown, or this menu is itself
    // a child menu, automatically show an item's child menu
    // when the mouse enters.
    if (item != null) {
      if ((shownChildMenu != null) || (parentMenu != null) || autoOpen) {
        doItemAction(item, false);
      }
    }
  }

  void selectItem(MenuItem item) {
    if (item == selectedItem) {
      return;
    }

    if (selectedItem != null) {
      selectedItem.setSelectionStyle(false);
      // Set the style of the submenu indicator
      if (vertical) {
        Element tr = DOM.getParent(selectedItem.getElement());
        if (DOM.getChildCount(tr) == 2) {
          Element td = DOM.getChild(tr, 1);
          setStyleName(td, "subMenuIcon-selected", false);
        }
      }
    }

    if (item != null) {
      item.setSelectionStyle(true);

      // Set the style of the submenu indicator
      if (vertical) {
        Element tr = DOM.getParent(item.getElement());
        if (DOM.getChildCount(tr) == 2) {
          Element td = DOM.getChild(tr, 1);
          setStyleName(td, "subMenuIcon-selected", true);
        }
      }

      Accessibility.setState(getElement(),
          Accessibility.STATE_ACTIVEDESCENDANT, DOM.getElementAttribute(
              item.getElement(), "id"));
    }

    selectedItem = item;
  }

  /**
   * Set the IDs of the menu items.
   * 
   * @param baseID the base ID
   */
  void setMenuItemDebugIds(String baseID) {
    int itemCount = 0;
    for (MenuItem item : items) {
      item.ensureDebugId(baseID + "-item" + itemCount);
      itemCount++;
    }
  }

  /**
   * Show or hide the icon used for items with a submenu.
   * 
   * @param item the item with or without a submenu
   */
  void updateSubmenuIcon(MenuItem item) {
    // The submenu icon only applies to vertical menus
    if (!vertical) {
      return;
    }

    // Get the index of the MenuItem
    int idx = allItems.indexOf(item);
    if (idx == -1) {
      return;
    }

    Element container = getItemContainerElement();
    Element tr = DOM.getChild(container, idx);
    int tdCount = DOM.getChildCount(tr);
    MenuBar submenu = item.getSubMenu();
    if (submenu == null) {
      // Remove the submenu indicator
      if (tdCount == 2) {
        DOM.removeChild(tr, DOM.getChild(tr, 1));
      }
      setItemColSpan(item, 2);
    } else if (tdCount == 1) {
      // Show the submenu indicator
      setItemColSpan(item, 1);
      Element td = DOM.createTD();
      DOM.setElementProperty(td, "vAlign", "middle");
      DOM.setInnerHTML(td, images.menuBarSubMenuIcon().getHTML());
      setStyleName(td, "subMenuIcon");
      DOM.appendChild(tr, td);
    }
  }

  /**
   * Physically add the td element of a {@link MenuItem} or
   * {@link MenuItemSeparator} to this {@link MenuBar}.
   * 
   * @param beforeIndex the index where the seperator should be inserted
   * @param tdElem the td element to be added
   */
  private void addItemElement(int beforeIndex, Element tdElem) {
    if (vertical) {
      Element tr = DOM.createTR();
      DOM.insertChild(body, tr, beforeIndex);
      DOM.appendChild(tr, tdElem);
    } else {
      Element tr = DOM.getChild(body, 0);
      DOM.insertChild(tr, tdElem, beforeIndex);
    }
  }

  /**
   * Closes this menu (if it is a popup).
   */
  private void close() {
    if (parentMenu != null) {
      parentMenu.popup.hide();
    }
  }

  private void eatEvent(Event event) {
    DOM.eventCancelBubble(event, true);
    DOM.eventPreventDefault(event);
  }

  private MenuItem findItem(Element hItem) {
    for (MenuItem item : items) {
      if (DOM.isOrHasChild(item.getElement(), hItem)) {
        return item;
      }
    }
    return null;
  }

  private void focus() {
    FocusPanel.impl.focus(getElement());
  }

  private Element getItemContainerElement() {
    if (vertical) {
      return body;
    } else {
      return DOM.getChild(body, 0);
    }
  }

  private void init(boolean vertical, MenuBarImages images) {
    this.images = images;

    Element table = DOM.createTable();
    body = DOM.createTBody();
    DOM.appendChild(table, body);

    if (!vertical) {
      Element tr = DOM.createTR();
      DOM.appendChild(body, tr);
    }

    this.vertical = vertical;

    Element outer = FocusPanel.impl.createFocusable();
    DOM.appendChild(outer, table);
    setElement(outer);

    Accessibility.setRole(getElement(), Accessibility.ROLE_MENUBAR);

    sinkEvents(Event.ONCLICK | Event.ONMOUSEOVER | Event.ONMOUSEOUT
        | Event.ONFOCUS | Event.ONKEYDOWN);

    setStyleName(STYLENAME_DEFAULT);
    if (vertical) {
      addStyleDependentName("vertical");
    } else {
      addStyleDependentName("horizontal");
    }

    // Hide focus outline in Mozilla/Webkit/Opera
    DOM.setStyleAttribute(getElement(), "outline", "0px");

    // Hide focus outline in IE 6/7
    DOM.setElementAttribute(getElement(), "hideFocus", "true");
  }

  private void moveDown() {
    if (selectFirstItemIfNoneSelected()) {
      return;
    }

    if (vertical) {
      selectNextItem();
    } else {
      if (selectedItem.getSubMenu() != null
          && !selectedItem.getSubMenu().getItems().isEmpty()
          && (shownChildMenu == null || shownChildMenu.getSelectedItem() == null)) {
        if (shownChildMenu == null) {
          doItemAction(selectedItem, false);
        }
        selectedItem.getSubMenu().focus();
      } else if (parentMenu != null) {
        if (parentMenu.vertical) {
          parentMenu.selectNextItem();
        } else {
          parentMenu.moveDown();
        }
      }
    }
  }

  private void moveToNextItem() {
    if (selectFirstItemIfNoneSelected()) {
      return;
    }

    if (!vertical) {
      selectNextItem();
    } else {
      if (selectedItem.getSubMenu() != null
          && !selectedItem.getSubMenu().getItems().isEmpty()
          && (shownChildMenu == null || shownChildMenu.getSelectedItem() == null)) {
        if (shownChildMenu == null) {
          doItemAction(selectedItem, false);
        }
        selectedItem.getSubMenu().focus();
      } else if (parentMenu != null) {
        if (!parentMenu.vertical) {
          parentMenu.selectNextItem();
        } else {
          parentMenu.moveToNextItem();
        }
      }
    }
  }

  private void moveToPrevItem() {
    if (selectFirstItemIfNoneSelected()) {
      return;
    }

    if (!vertical) {
      selectPrevItem();
    } else {
      if ((parentMenu != null) && (!parentMenu.vertical)) {
        parentMenu.selectPrevItem();
      } else {
        close();
      }
    }
  }

  private void moveUp() {
    if (selectFirstItemIfNoneSelected()) {
      return;
    }

    if ((shownChildMenu == null) && vertical) {
      selectPrevItem();
    } else if ((parentMenu != null) && parentMenu.vertical) {
      parentMenu.selectPrevItem();
    } else {
      close();
    }
  }

  /*
   * This method is called when a menu bar is hidden, so that it can hide any
   * child popups that are currently being shown.
   */
  private void onHide() {
    if (shownChildMenu != null) {
      shownChildMenu.onHide();
      popup.hide();
      focus();
    }
  }

  /*
   * This method is called when a menu bar is shown.
   */
  private void onShow() {
    // clear the selection; a keyboard user can cursor down to the first item
    selectItem(null);
  }

  private void openPopup(final MenuItem item) {
    // Create a new popup for this item, and position it next to
    // the item (below if this is a horizontal menu bar, to the
    // right if it's a vertical bar).
    popup = new DecoratedPopupPanel(true, false, "menuPopup") {
      {
        setWidget(item.getSubMenu());
        item.getSubMenu().onShow();
      }

      @Override
      public boolean onEventPreview(Event event) {
        // Hook the popup panel's event preview. We use this to keep it from
        // auto-hiding when the parent menu is clicked.
        switch (DOM.eventGetType(event)) {
          case Event.ONMOUSEDOWN:
            // If the event target is part of the parent menu, suppress the
            // event altogether.
            Element target = DOM.eventGetTarget(event);
            Element parentMenuElement = item.getParentMenu().getElement();
            if (DOM.isOrHasChild(parentMenuElement, target)) {
              return false;
            }
            boolean cancel = super.onEventPreview(event);
            if (cancel) {
              selectItem(null);
            }
            return cancel;
        }
        return super.onEventPreview(event);
      }
    };
    popup.setAnimationType(AnimationType.ONE_WAY_CORNER);
    popup.setAnimationEnabled(isAnimationEnabled);
    popup.setStyleName(STYLENAME_DEFAULT + "Popup");
    String primaryStyleName = getStylePrimaryName();
    if (!STYLENAME_DEFAULT.equals(primaryStyleName)) {
      popup.addStyleName(primaryStyleName + "Popup");
    }
    popup.addPopupListener(this);

    shownChildMenu = item.getSubMenu();
    item.getSubMenu().parentMenu = this;

    // Show the popup, ensuring that the menubar's event preview remains on top
    // of the popup's.
    popup.setPopupPositionAndShow(new PopupPanel.PositionCallback() {

      public void setPosition(int offsetWidth, int offsetHeight) {

        // depending on the bidi direction position a menu on the left or right
        // of its base item
        if (LocaleInfo.getCurrentLocale().isRTL()) {
          if (vertical) {
            popup.setPopupPosition(MenuBar.this.getAbsoluteLeft() - offsetWidth
                + 1, item.getAbsoluteTop());
          } else {
            popup.setPopupPosition(item.getAbsoluteLeft()
                + item.getOffsetWidth() - offsetWidth,
                MenuBar.this.getAbsoluteTop() + MenuBar.this.getOffsetHeight()
                    - 1);
          }
        } else {
          if (vertical) {
            popup.setPopupPosition(MenuBar.this.getAbsoluteLeft()
                + MenuBar.this.getOffsetWidth() - 1, item.getAbsoluteTop());
          } else {
            popup.setPopupPosition(item.getAbsoluteLeft(),
                MenuBar.this.getAbsoluteTop() + MenuBar.this.getOffsetHeight()
                    - 1);
          }
        }
      }
    });
  }

  /**
   * Removes the specified item from the {@link MenuBar} and the physical DOM
   * structure.
   * 
   * @param item the item to be removed
   * @return true if the item was removed
   */
  private boolean removeItemElement(UIObject item) {
    int idx = allItems.indexOf(item);
    if (idx == -1) {
      return false;
    }

    Element container = getItemContainerElement();
    DOM.removeChild(container, DOM.getChild(container, idx));
    allItems.remove(idx);
    return true;
  }

  /**
   * Selects the first item in the menu if no items are currently selected. This
   * method assumes that the menu has at least 1 item.
   * 
   * @return true if no item was previously selected and the first item in the
   *         list was selected, false otherwise
   */
  private boolean selectFirstItemIfNoneSelected() {
    if (selectedItem == null) {
      MenuItem nextItem = items.get(0);
      selectItem(nextItem);
      return true;
    }

    return false;
  }

  private void selectNextItem() {
    if (selectedItem == null) {
      return;
    }

    int index = items.indexOf(selectedItem);
    // We know that selectedItem is set to an item that is contained in the
    // items collection.
    // Therefore, we know that index can never be -1.
    assert (index != -1);

    MenuItem itemToBeSelected;

    if (index < items.size() - 1) {
      itemToBeSelected = items.get(index + 1);
    } else { // we're at the end, loop around to the start
      itemToBeSelected = items.get(0);
    }

    selectItem(itemToBeSelected);
    if (shownChildMenu != null) {
      doItemAction(itemToBeSelected, false);
    }
  }

  private void selectPrevItem() {
    if (selectedItem == null) {
      return;
    }

    int index = items.indexOf(selectedItem);
    // We know that selectedItem is set to an item that is contained in the
    // items collection.
    // Therefore, we know that index can never be -1.
    assert (index != -1);

    MenuItem itemToBeSelected;
    if (index > 0) {
      itemToBeSelected = items.get(index - 1);

    } else { // we're at the start, loop around to the end
      itemToBeSelected = items.get(items.size() - 1);
    }

    selectItem(itemToBeSelected);
    if (shownChildMenu != null) {
      doItemAction(itemToBeSelected, false);
    }
  }

  /**
   * Set the colspan of a {@link MenuItem} or {@link MenuItemSeparator}.
   * 
   * @param item the {@link MenuItem} or {@link MenuItemSeparator}
   * @param colspan the colspan
   */
  private void setItemColSpan(UIObject item, int colspan) {
    DOM.setElementPropertyInt(item.getElement(), "colSpan", colspan);
  }
}
