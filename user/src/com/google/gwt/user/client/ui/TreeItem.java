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

import com.google.gwt.animation.client.Animation;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * An item that can be contained within a
 * {@link com.google.gwt.user.client.ui.Tree}.
 * 
 * Each tree item is assigned a unique DOM id in order to support ARIA. See
 * {@link com.google.gwt.user.client.ui.Accessibility} for more information.
 * 
 * <p>
 * <h3>Example</h3>
 * {@example com.google.gwt.examples.TreeExample}
 * </p>
 */
public class TreeItem extends UIObject implements HasHTML {
  /**
   * Implementation class for {@link TreeItem}.
   */
  public static class TreeItemImpl {
    public TreeItemImpl() {
      initializeClonableElements();
    }

    void convertToFullNode(TreeItem item) {
      if (item.imageHolder == null) {
        // Extract the Elements from the object
        Element itemTable = DOM.clone(BASE_INTERNAL_ELEM, true);
        DOM.appendChild(item.getElement(), itemTable);
        Element tr = DOM.getFirstChild(DOM.getFirstChild(itemTable));
        Element tdImg = DOM.getFirstChild(tr);
        Element tdContent = DOM.getNextSibling(tdImg);

        // Undoes padding from table element.
        DOM.setStyleAttribute(item.getElement(), "padding", "0px");
        DOM.appendChild(tdContent, item.contentElem);
        item.imageHolder = tdImg;
      }
    }

    /**
     * Setup clonable elements.
     */
    void initializeClonableElements() {
      if (GWT.isClient()) {
        // Create the base table element that will be cloned.
        BASE_INTERNAL_ELEM = DOM.createTable();
        Element contentElem = DOM.createSpan();
        Element tbody = DOM.createTBody(), tr = DOM.createTR();
        Element tdImg = DOM.createTD(), tdContent = DOM.createTD();
        DOM.appendChild(BASE_INTERNAL_ELEM, tbody);
        DOM.appendChild(tbody, tr);
        DOM.appendChild(tr, tdImg);
        DOM.appendChild(tr, tdContent);
        DOM.setStyleAttribute(tdImg, "verticalAlign", "middle");
        DOM.setStyleAttribute(tdContent, "verticalAlign", "middle");
        DOM.appendChild(tdContent, contentElem);
        DOM.setStyleAttribute(contentElem, "display", "inline");
        setStyleName(contentElem, "gwt-TreeItem");
        DOM.setStyleAttribute(BASE_INTERNAL_ELEM, "whiteSpace", "nowrap");

        // Create the base element that will be cloned
        BASE_BARE_ELEM = DOM.createDiv();

        // Simulates padding from table element.
        DOM.setStyleAttribute(BASE_BARE_ELEM, "padding", "3px");
        DOM.appendChild(BASE_BARE_ELEM, contentElem);
        Accessibility.setRole(contentElem, Accessibility.ROLE_TREEITEM);
      }
    }
  }

  /**
   * IE specific implementation class for {@link TreeItem}.
   */
  public static class TreeItemImplIE6 extends TreeItemImpl {
    @Override
    void convertToFullNode(TreeItem item) {
      super.convertToFullNode(item);
      DOM.setStyleAttribute(item.getElement(), "marginBottom", "0px");
    }

    @Override
    void initializeClonableElements() {
      super.initializeClonableElements();
      if (GWT.isClient()) {
        // Remove the padding from the cells and re-add it to the table element
        DOM.setElementPropertyInt(BASE_INTERNAL_ELEM, "cellPadding", 0);
        DOM.setElementPropertyInt(BASE_INTERNAL_ELEM, "cellSpacing", 0);
        BASE_INTERNAL_ELEM.getStyle().setPropertyPx("paddingBottom", 3);

        // We can't use a 3px padding all around because IE will wrap the
        // childSpan to the next line, so we need to add a 3px margin on the top
        // and bottom instead. However, margins overlap, so we need a 6px bottom
        // margin.
        DOM.setStyleAttribute(BASE_BARE_ELEM, "margin", "3px 0px 6px 0px");
        DOM.setStyleAttribute(BASE_BARE_ELEM, "padding", "0px 3px");
      }
    }
  }

  /**
   * An {@link Animation} used to open the child elements. If a {@link TreeItem}
   * is in the process of opening, it will immediately be opened and the new
   * {@link TreeItem} will use this animation.
   */
  private static class TreeItemAnimation extends Animation {

    /**
     * The {@link TreeItem} currently being affected.
     */
    private TreeItem curItem = null;

    /**
     * Whether the item is being opened or closed.
     */
    private boolean opening = true;

    /**
     * The target height of the child items.
     */
    private int scrollHeight = 0;

    /**
     * Open the specified {@link TreeItem}.
     * 
     * @param item the {@link TreeItem} to open
     * @param animate true to animate, false to open instantly
     */
    public void setItemState(TreeItem item, boolean animate) {
      // Immediately complete previous open
      cancel();

      // Open the new item
      if (animate) {
        curItem = item;
        opening = item.open;
        run(Math.min(ANIMATION_DURATION, ANIMATION_DURATION_PER_ITEM
            * curItem.getChildCount()));
      } else {
        UIObject.setVisible(item.childSpanElem, item.open);
      }
    }

    @Override
    protected void onComplete() {
      if (curItem != null) {
        if (opening) {
          UIObject.setVisible(curItem.childSpanElem, true);
          onUpdate(1.0);
          DOM.setStyleAttribute(curItem.childSpanElem, "height", "auto");
        } else {
          UIObject.setVisible(curItem.childSpanElem, false);
        }
        DOM.setStyleAttribute(curItem.childSpanElem, "overflow", "visible");
        DOM.setStyleAttribute(curItem.childSpanElem, "width", "auto");
        curItem = null;
      }
    }

    @Override
    protected void onStart() {
      scrollHeight = 0;

      // If the TreeItem is already open, we can get its scrollHeight
      // immediately.
      if (!opening) {
        scrollHeight = curItem.childSpanElem.getScrollHeight();
      }
      DOM.setStyleAttribute(curItem.childSpanElem, "overflow", "hidden");

      // If the TreeItem is already open, onStart will set its height to its
      // natural height. If the TreeItem is currently closed, onStart will set
      // its height to 1px (see onUpdate below), and then we make the TreeItem
      // visible so we can get its correct scrollHeight.
      super.onStart();

      // If the TreeItem is currently closed, we need to make it visible before
      // we can get its height.
      if (opening) {
        UIObject.setVisible(curItem.childSpanElem, true);
        scrollHeight = curItem.childSpanElem.getScrollHeight();
      }
    }

    @Override
    protected void onUpdate(double progress) {
      int height = (int) (progress * scrollHeight);
      if (!opening) {
        height = scrollHeight - height;
      }

      // Issue 2338: If the height is 0px, IE7 will display all of the children
      // instead of hiding them completely.
      height = Math.max(height, 1);

      DOM.setStyleAttribute(curItem.childSpanElem, "height", height + "px");

      // We need to set the width explicitly of the item might be cropped
      int scrollWidth = DOM.getElementPropertyInt(curItem.childSpanElem,
          "scrollWidth");
      DOM.setStyleAttribute(curItem.childSpanElem, "width", scrollWidth + "px");
    }
  }

  // By not overwriting the default tree padding and spacing, we traditionally
  // added 7 pixels between our image and content.
  // <2>|<1>image<1>|<2>|<1>content
  // So to preserve the current spacing we must add a 7 pixel pad when no image
  // is supplied.
  static final int IMAGE_PAD = 7;

  /**
   * The duration of the animation.
   */
  private static final int ANIMATION_DURATION = 200;

  /**
   * The duration of the animation per child {@link TreeItem}. If the per item
   * duration times the number of child items is less than the duration above,
   * the smaller duration will be used.
   */
  private static final int ANIMATION_DURATION_PER_ITEM = 75;

  /**
   * The static animation used to open {@link TreeItem TreeItems}.
   */
  private static TreeItemAnimation itemAnimation = new TreeItemAnimation();

  /**
   * The structured table to hold images.
   */

  private static Element BASE_INTERNAL_ELEM;
  /**
   * The base tree item element that will be cloned.
   */
  private static Element BASE_BARE_ELEM;

  private static TreeItemImpl impl = GWT.create(TreeItemImpl.class);

  private ArrayList<TreeItem> children;
  private Element contentElem, childSpanElem, imageHolder;
  private boolean open;
  private TreeItem parent;
  private boolean selected;

  private Object userObject;

  private Tree tree;

  private Widget widget;

  /**
   * Creates an empty tree item.
   */
  public TreeItem() {
    Element elem = DOM.clone(BASE_BARE_ELEM, true);
    setElement(elem);
    contentElem = DOM.getFirstChild(elem);
    DOM.setElementAttribute(contentElem, "id", DOM.createUniqueId());
  }

  /**
   * Constructs a tree item with the given HTML.
   * 
   * @param html the item's HTML
   */
  public TreeItem(String html) {
    this();
    setHTML(html);
  }

  /**
   * Constructs a tree item with the given <code>Widget</code>.
   * 
   * @param widget the item's widget
   */
  public TreeItem(Widget widget) {
    this();
    setWidget(widget);
  }

  /**
   * Adds a child tree item containing the specified text.
   * 
   * @param itemText the text to be added
   * @return the item that was added
   */
  public TreeItem addItem(String itemText) {
    TreeItem ret = new TreeItem(itemText);
    addItem(ret);
    return ret;
  }

  /**
   * Adds another item as a child to this one.
   * 
   * @param item the item to be added
   */
  public void addItem(TreeItem item) {
    // Detach item from existing parent.
    if ((item.getParentItem() != null) || (item.getTree() != null)) {
      item.remove();
    }

    if (children == null) {
      initChildren();
    }

    // Logical attach.
    item.setParentItem(this);
    children.add(item);

    // Physical attach.
    if (LocaleInfo.getCurrentLocale().isRTL()) {
      DOM.setStyleAttribute(item.getElement(), "marginRight", "16px");
    } else {
      DOM.setStyleAttribute(item.getElement(), "marginLeft", "16px");
    }

    DOM.appendChild(childSpanElem, item.getElement());

    // Adopt.
    item.setTree(tree);

    if (children.size() == 1) {
      updateState(false, false);
    }
  }

  /**
   * Adds a child tree item containing the specified widget.
   * 
   * @param widget the widget to be added
   * @return the item that was added
   */
  public TreeItem addItem(Widget widget) {
    TreeItem ret = new TreeItem(widget);
    addItem(ret);
    return ret;
  }

  /**
   * Gets the child at the specified index.
   * 
   * @param index the index to be retrieved
   * @return the item at that index
   */

  public TreeItem getChild(int index) {
    if ((index < 0) || (index >= getChildCount())) {
      return null;
    }

    return children.get(index);
  }

  /**
   * Gets the number of children contained in this item.
   * 
   * @return this item's child count.
   */

  public int getChildCount() {
    if (children == null) {
      return 0;
    }
    return children.size();
  }

  /**
   * Gets the index of the specified child item.
   * 
   * @param child the child item to be found
   * @return the child's index, or <code>-1</code> if none is found
   */

  public int getChildIndex(TreeItem child) {
    if (children == null) {
      return -1;
    }
    return children.indexOf(child);
  }

  public String getHTML() {
    return DOM.getInnerHTML(contentElem);
  }

  /**
   * Gets this item's parent.
   * 
   * @return the parent item
   */
  public TreeItem getParentItem() {
    return parent;
  }

  /**
   * Gets whether this item's children are displayed.
   * 
   * @return <code>true</code> if the item is open
   */
  public boolean getState() {
    return open;
  }

  public String getText() {
    return DOM.getInnerText(contentElem);
  }

  /**
   * Gets the tree that contains this item.
   * 
   * @return the containing tree
   */
  public final Tree getTree() {
    return tree;
  }

  /**
   * Gets the user-defined object associated with this item.
   * 
   * @return the item's user-defined object
   */
  public Object getUserObject() {
    return userObject;
  }

  /**
   * Gets the <code>Widget</code> associated with this tree item.
   * 
   * @return the widget
   */
  public Widget getWidget() {
    return widget;
  }

  /**
   * Determines whether this item is currently selected.
   * 
   * @return <code>true</code> if it is selected
   */
  public boolean isSelected() {
    return selected;
  }

  /**
   * Removes this item from its tree.
   */
  public void remove() {
    if (parent != null) {
      // If this item has a parent, remove self from it.
      parent.removeItem(this);
    } else if (tree != null) {
      // If the item has no parent, but is in the Tree, it must be a top-level
      // element.
      tree.removeItem(this);
    }
  }

  /**
   * Removes one of this item's children.
   * 
   * @param item the item to be removed
   */

  public void removeItem(TreeItem item) {
    // Validate.
    if (children == null || !children.contains(item)) {
      return;
    }

    // Orphan.
    item.setTree(null);

    // Physical detach.
    DOM.removeChild(childSpanElem, item.getElement());

    // Logical detach.
    item.setParentItem(null);
    children.remove(item);

    if (children.size() == 0) {
      updateState(false, false);
    }
  }

  /**
   * Removes all of this item's children.
   */
  public void removeItems() {
    while (getChildCount() > 0) {
      removeItem(getChild(0));
    }
  }

  public void setHTML(String html) {
    setWidget(null);
    DOM.setInnerHTML(contentElem, html);
  }

  /**
   * Selects or deselects this item.
   * 
   * @param selected <code>true</code> to select the item, <code>false</code> to
   *          deselect it
   */
  public void setSelected(boolean selected) {
    if (this.selected == selected) {
      return;
    }
    this.selected = selected;
    setStyleName(getContentElem(), "gwt-TreeItem-selected", selected);
  }

  /**
   * Sets whether this item's children are displayed.
   * 
   * @param open whether the item is open
   */
  public void setState(boolean open) {
    setState(open, true);
  }

  /**
   * Sets whether this item's children are displayed.
   * 
   * @param open whether the item is open
   * @param fireEvents <code>true</code> to allow open/close events to be
   */
  public void setState(boolean open, boolean fireEvents) {
    if (open && getChildCount() == 0) {
      return;
    }

    // Only do the physical update if it changes
    if (this.open != open) {
      this.open = open;
      updateState(true, true);

      if (fireEvents && tree != null) {
        tree.fireStateChanged(this, open);
      }
    }
  }

  public void setText(String text) {
    setWidget(null);
    DOM.setInnerText(contentElem, text);
  }

  /**
   * Sets the user-defined object associated with this item.
   * 
   * @param userObj the item's user-defined object
   */
  public void setUserObject(Object userObj) {
    userObject = userObj;
  }

  /**
   * Sets the current widget. Any existing child widget will be removed.
   * 
   * @param newWidget Widget to set
   */
  public void setWidget(Widget newWidget) {
    // Detach new child from old parent.
    if (newWidget != null) {
      newWidget.removeFromParent();
    }

    // Detach old child from tree.
    if (widget != null && tree != null) {
      tree.orphan(widget);
    }

    // Physical detach old from self.
    // Clear out any existing content before adding a widget.
    DOM.setInnerHTML(contentElem, "");

    // Logical detach old/attach new.
    widget = newWidget;

    if (newWidget != null) {
      // Physical attach new.
      DOM.appendChild(contentElem, newWidget.getElement());

      // Attach child to tree.
      if (tree != null) {
        tree.adopt(widget, this);
      }

      // Set tabIndex on the widget to -1, so that it doesn't mess up the tab
      // order of the entire tree

      if (Tree.shouldTreeDelegateFocusToElement(widget.getElement())) {
        DOM.setElementAttribute(widget.getElement(), "tabIndex", "-1");
      }
    }
  }

  /**
   * Returns the widget, if any, that should be focused on if this TreeItem is
   * selected.
   * 
   * @return widget to be focused.
   */
  protected HasFocus getFocusableWidget() {
    Widget w = getWidget();
    if (w instanceof HasFocus) {
      return (HasFocus) w;
    } else {
      return null;
    }
  }

  /**
   * <b>Affected Elements:</b>
   * <ul>
   * <li>-content = The text or {@link Widget} next to the image.</li>
   * <li>-child# = The child at the specified index.</li>
   * </ul>
   * 
   * @see UIObject#onEnsureDebugId(String)
   */
  @Override
  protected void onEnsureDebugId(String baseID) {
    super.onEnsureDebugId(baseID);
    ensureDebugId(contentElem, baseID, "content");
    if (imageHolder != null) {
      // The image itself may or may not exist.
      ensureDebugId(imageHolder, baseID, "image");
    }

    if (children != null) {
      int childCount = 0;
      for (TreeItem child : children) {
        child.ensureDebugId(baseID + "-child" + childCount);
        childCount++;
      }
    }
  }

  void addTreeItems(List<TreeItem> accum) {
    int size = getChildCount();
    for (int i = 0; i < size; i++) {
      TreeItem item = children.get(i);
      accum.add(item);
      item.addTreeItems(accum);
    }
  }

  ArrayList<TreeItem> getChildren() {
    return children;
  }

  Element getContentElem() {
    return contentElem;
  }

  Element getImageElement() {
    return DOM.getFirstChild(getImageHolderElement());
  }

  Element getImageHolderElement() {
    if (imageHolder == null) {
      convertToFullNode();
    }
    return imageHolder;
  }

  void initChildren() {
    convertToFullNode();
    childSpanElem = DOM.createDiv();
    DOM.appendChild(getElement(), childSpanElem);
    DOM.setStyleAttribute(childSpanElem, "whiteSpace", "nowrap");
    children = new ArrayList<TreeItem>();
  }

  void setParentItem(TreeItem parent) {
    this.parent = parent;
  }

  void setTree(Tree newTree) {
    // Early out.
    if (tree == newTree) {
      return;
    }

    // Remove this item from existing tree.
    if (tree != null) {
      if (tree.getSelectedItem() == this) {
        tree.setSelectedItem(null);
      }

      if (widget != null) {
        tree.orphan(widget);
      }
    }

    tree = newTree;
    for (int i = 0, n = getChildCount(); i < n; ++i) {
      children.get(i).setTree(newTree);
    }
    updateState(false, true);

    if (newTree != null) {
      if (widget != null) {
        // Add my widget to the new tree.
        newTree.adopt(widget, this);
      }
    }
  }

  void updateState(boolean animate, boolean updateTreeSelection) {
    // If the tree hasn't been set, there is no visual state to update.
    // If the tree is not attached, then update will be called on attach.
    if (tree == null || tree.isAttached() == false) {
      return;
    }

    if (getChildCount() == 0) {
      if (childSpanElem != null) {
        UIObject.setVisible(childSpanElem, false);
      }
      tree.showLeafImage(this);
      return;
    }

    // We must use 'display' rather than 'visibility' here,
    // or the children will always take up space.
    if (animate && (tree != null) && (tree.isAttached())) {
      itemAnimation.setItemState(this, tree.isAnimationEnabled());
    } else {
      itemAnimation.setItemState(this, false);
    }

    // Change the status image
    if (open) {
      tree.showOpenImage(this);
    } else {
      tree.showClosedImage(this);
    }

    // We may need to update the tree's selection in response to a tree state
    // change. For example, if the tree's currently selected item is a
    // descendant of an item whose branch was just collapsed, then the item
    // itself should become the newly-selected item.
    if (updateTreeSelection) {
      tree.maybeUpdateSelection(this, this.open);
    }
  }

  void updateStateRecursive() {
    updateStateRecursiveHelper();
    tree.maybeUpdateSelection(this, this.open);
  }

  private void convertToFullNode() {
    impl.convertToFullNode(this);
  }

  private void updateStateRecursiveHelper() {
    updateState(false, false);
    for (int i = 0, n = getChildCount(); i < n; ++i) {
      children.get(i).updateStateRecursiveHelper();
    }
  }
}
