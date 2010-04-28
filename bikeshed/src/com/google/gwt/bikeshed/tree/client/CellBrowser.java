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
package com.google.gwt.bikeshed.tree.client;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.bikeshed.cells.client.Cell;
import com.google.gwt.bikeshed.cells.client.ValueUpdater;
import com.google.gwt.bikeshed.list.client.ListView;
import com.google.gwt.bikeshed.list.client.PageSizePager;
import com.google.gwt.bikeshed.list.client.PagingListView;
import com.google.gwt.bikeshed.list.client.CellList;
import com.google.gwt.bikeshed.list.client.PagingListView.Pager;
import com.google.gwt.bikeshed.list.shared.ProvidesKey;
import com.google.gwt.bikeshed.list.shared.Range;
import com.google.gwt.bikeshed.list.shared.SelectionModel;
import com.google.gwt.bikeshed.tree.client.CellTreeViewModel.NodeInfo;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasAnimation;
import com.google.gwt.user.client.ui.ProvidesResize;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

import java.util.ArrayList;
import java.util.List;

/**
 * A "browsable" view of a tree in which only a single node per level may be
 * open at one time.
 */
public class CellBrowser extends CellTreeView implements ProvidesResize,
    RequiresResize, HasAnimation {

  /**
   * The element used in place of an image when a node has no children.
   */
  private static final String LEAF_IMAGE = "<div style='position:absolute;display:none;'></div>";

  /**
   * The style name assigned to each column.
   */
  private static final String STYLENAME_COLUMN = "gwt-cellBrowser-column";

  /**
   * The style name assigned to the first column.
   */
  private static final String STYLENAME_FIRST_COLUMN = "gwt-cellBrowser-firstColumn";

  /**
   * The style name assigned to each column.
   */
  private static final String STYLENAME_OPEN = "gwt-cellBrowser-openItem";

  /**
   * The prefix of the ID assigned to open cells.
   */
  private static final String ID_PREFIX_OPEN = "__gwt-cellBrowser-open-";

  /**
   * The animation used to scroll to the newly added list view.
   */
  private class ScrollAnimation extends Animation {

    /**
     * The starting scroll position.
     */
    private int startScrollLeft;

    /**
     * The ending scroll position.
     */
    private int targetScrollLeft;

    @Override
    protected void onComplete() {
      getElement().setScrollLeft(targetScrollLeft);
    }

    @Override
    protected void onUpdate(double progress) {
      int diff = targetScrollLeft - startScrollLeft;
      getElement().setScrollLeft(startScrollLeft + (int) (diff * progress));
    }

    void scrollToEnd() {
      Element elem = getElement();
      targetScrollLeft = elem.getScrollWidth() - elem.getClientWidth();

      if (isAnimationEnabled()) {
        // Animate the scrolling.
        startScrollLeft = elem.getScrollLeft();
        run(250);
      } else {
        // Scroll instantly.
        onComplete();
      }
    }
  }

  /**
   * A wrapper around a cell that adds an open button.
   * 
   * @param <C> the data type of the cell
   */
  private class CellDecorator<C> extends Cell<C> {

    /**
     * The cell used to render the inner contents.
     */
    private final Cell<C> cell;

    /**
     * The level of this list view.
     */
    private final int level;

    /**
     * The key of the currently open item.
     */
    private Object openKey;

    /**
     * The key provider for the node.
     */
    private final ProvidesKey<C> providesKey;

    /**
     * Construct a new {@link CellDecorator}.
     * 
     * @param nodeInfo the {@link NodeInfo} associated with the cell
     * @param level the level of items rendered by this decorator
     */
    public CellDecorator(NodeInfo<C> nodeInfo, int level) {
      this.cell = nodeInfo.getCell();
      this.level = level;
      this.providesKey = nodeInfo.getProvidesKey();
    }

    @Override
    public boolean consumesEvents() {
      return cell.consumesEvents();
    }

    @Override
    public boolean dependsOnSelection() {
      return cell.dependsOnSelection();
    }

    @Override
    public Object onBrowserEvent(Element parent, C value, Object viewData,
        NativeEvent event, ValueUpdater<C> valueUpdater) {

      // Fire the event to the inner cell.
      viewData = cell.onBrowserEvent(getCellParent(parent), value, viewData,
          event, valueUpdater);

      // Open child nodes.
      if (Event.getTypeInt(event.getType()) == Event.ONMOUSEDOWN) {
        trimToLevel(level);

        // Remove style from currently open item.
        updateOpenElement(false);

        // Save the key of the new open item and update the Element.
        if (!getTreeViewModel().isLeaf(value)) {
          NodeInfo<?> nodeInfo = getTreeViewModel().getNodeInfo(value);
          if (nodeInfo != null) {
            openKey = providesKey.getKey(value);
            replaceImageElement(parent, true);
            appendTreeNode(nodeInfo);
          }
        }
      }

      return viewData;
    }

    @Override
    public void render(C value, Object viewData, StringBuilder sb) {
      boolean isOpen = (openKey == null) ? false
          : openKey.equals(providesKey.getKey(value));
      int imageWidth = getImageWidth();
      sb.append("<div style='position:relative;padding-right:");
      sb.append(imageWidth);
      sb.append("px;'");
      if (isOpen) {
        sb.append(" id='").append(getOpenId()).append("'");
      }
      sb.append(">");
      if (isOpen) {
        sb.append(getOpenImageHtml());
      } else if (getTreeViewModel().isLeaf(value)) {
        sb.append(LEAF_IMAGE);
      } else {
        sb.append(getClosedImageHtml());
      }
      sb.append("<div>");
      cell.render(value, viewData, sb);
      sb.append("</div></div>");
    }

    @Override
    public void setValue(Element parent, C value, Object viewData) {
      cell.setValue(getCellParent(parent), value, viewData);
    }

    /**
     * Find the currently open element and update its style.
     * 
     * @param isOpen if the open element is still open
     */
    public void updateOpenElement(boolean isOpen) {
      Element curOpenItem = Document.get().getElementById(getOpenId());
      if (curOpenItem != null) {
        replaceImageElement(curOpenItem.getParentElement(), isOpen);
      }
      if (!isOpen) {
        openKey = null;
      }
    }

    /**
     * Get the parent element of the decorated cell.
     * 
     * @param parent the parent of this cell
     * @return the decorated cell's parent
     */
    private Element getCellParent(Element parent) {
      return parent.getFirstChildElement().getChild(1).cast();
    }

    /**
     * Get the image element of the decorated cell.
     * 
     * @param parent the parent of this cell
     * @return the image element
     */
    private Element getImageElement(Element parent) {
      return parent.getFirstChildElement().getFirstChildElement();
    }

    /**
     * Get the ID of the open element.
     * 
     * @return the ID
     */
    private String getOpenId() {
      return ID_PREFIX_OPEN + level + "-" + uniqueId;
    }

    /**
     * Replace the image element of a cell.
     * 
     * @param parent the parent element of the cell
     * @param open true if open, false if closed
     */
    private void replaceImageElement(Element parent, boolean open) {
      // Update the style name and ID.
      Element wrapper = parent.getFirstChildElement();
      if (open) {
        parent.addClassName(STYLENAME_OPEN);
        wrapper.setId(getOpenId());
      } else {
        parent.removeClassName(STYLENAME_OPEN);
        wrapper.setId("");
      }

      // Replace the image element.
      String html = open ? getOpenImageHtml() : getClosedImageHtml();
      Element tmp = Document.get().createDivElement();
      tmp.setInnerHTML(html);
      Element imageElem = tmp.getFirstChildElement();
      Element oldImg = getImageElement(parent);
      wrapper.replaceChild(imageElem, oldImg);
    }
  }

  /**
   * A node in the tree.
   * 
   * @param <C> the data type of the children of the node
   */
  private class TreeNode<C> {
    private CellDecorator<C> cell;
    private ListView<C> listView;
    private NodeInfo<C> nodeInfo;
    private Widget widget;

    /**
     * Construct a new {@link TreeNode}.
     * 
     * @param nodeInfo the nodeInfo for the children nodes
     * @param listView the list view assocated with the node
     * @param widget the widget that represents the list view
     */
    public TreeNode(NodeInfo<C> nodeInfo, ListView<C> listView,
        CellDecorator<C> cell, Widget widget) {
      this.cell = cell;
      this.listView = listView;
      this.nodeInfo = nodeInfo;
      this.widget = widget;
    }

    /**
     * Get the {@link CellDecorator} used to render the node.
     * 
     * @return the cell decorator
     */
    public CellDecorator<C> getCell() {
      return cell;
    }

    /**
     * Unregister the list view and remove it from the widget.
     */
    void cleanup() {
      listView.setSelectionModel(null);
      nodeInfo.unsetView();
      getSplitLayoutPanel().remove(widget);
    }
  }

  /**
   * The counter used to assigned unique IDs.
   */
  private static int NEXT_ID = 0;

  /**
   * The animation used for scrolling.
   */
  private final ScrollAnimation animation = new ScrollAnimation();

  /**
   * The default width of new columns.
   */
  private int defaultWidth = 200;

  /**
   * The HTML used to generate the closed image.
   */
  private String closedImageHtml;

  /**
   * The unique ID assigned to this tree view.
   */
  private final int uniqueId = NEXT_ID++;

  /**
   * A boolean indicating whether or not animations are enabled.
   */
  private boolean isAnimationEnabled;

  /**
   * The minimum width of new columns.
   */
  private int minWidth = getImageWidth() + 20;

  /**
   * The HTML used to generate the open image.
   */
  private String openImageHtml;

  /**
   * The element used to maintain the scrollbar when columns are removed.
   */
  private Element scrollLock;

  /**
   * The visible {@link TreeNode}.
   */
  private List<TreeNode<?>> treeNodes = new ArrayList<TreeNode<?>>();

  /**
   * Construct a new {@link CellTreeView}.
   * 
   * @param <T> the type of data in the root node
   * @param viewModel the {@link CellTreeViewModel} that backs the tree
   * @param rootValue the hidden root value of the tree
   */
  public <T> CellBrowser(CellTreeViewModel viewModel, T rootValue) {
    super(viewModel, new SplitLayoutPanel());
    getElement().getStyle().setOverflow(Overflow.AUTO);
    setStyleName("gwt-SideBySideTreeView");

    // Add a placeholder to maintain the scroll width.
    scrollLock = Document.get().createDivElement();
    scrollLock.getStyle().setPosition(Position.ABSOLUTE);
    scrollLock.getStyle().setVisibility(Visibility.HIDDEN);
    scrollLock.getStyle().setZIndex(-32767);
    scrollLock.getStyle().setBackgroundColor("red");
    scrollLock.getStyle().setTop(0, Unit.PX);
    scrollLock.getStyle().setLeft(0, Unit.PX);
    scrollLock.getStyle().setHeight(1, Unit.PX);
    scrollLock.getStyle().setWidth(1, Unit.PX);
    getElement().appendChild(scrollLock);

    // Associate the first ListView with the rootValue.
    appendTreeNode(viewModel.getNodeInfo(rootValue));

    // Catch scroll events.
    sinkEvents(Event.ONSCROLL);
  }

  /**
   * Get the default width of new columns.
   * 
   * @return the default width in pixels
   */
  public int getDefaultColumnWidth() {
    return defaultWidth;
  }

  /**
   * Get the minimum width of columns.
   * 
   * @return the minimum width in pixels
   */
  public int getMinimumColumnWidth() {
    return minWidth;
  }

  public boolean isAnimationEnabled() {
    return isAnimationEnabled;
  }

  @Override
  public void onBrowserEvent(Event event) {
    switch (DOM.eventGetType(event)) {
      case Event.ONSCROLL:
        // Shorten the scroll bar is possible.
        adjustScrollLock();
        break;
    }
    super.onBrowserEvent(event);
  }

  public void onResize() {
    getSplitLayoutPanel().onResize();
  }

  public void setAnimationEnabled(boolean enable) {
    this.isAnimationEnabled = enable;
  }

  /**
   * Set the default width of new columns.
   * 
   * @param width the default width in pixels
   */
  public void setDefaultColumnWidth(int width) {
    this.defaultWidth = width;
  }

  /**
   * Set the minimum width of columns.
   * 
   * @param minWidth the minimum width in pixels
   */
  public void setMinimumColumnWidth(int minWidth) {
    this.minWidth = minWidth;
  }

  /**
   * Create a Pager to control the list view. The {@link ListView} must extend
   * {@link Widget}.
   * 
   * @param <C> the item type in the list view
   * @param listView the list view to add paging too
   * @return the {@link Pager}
   */
  protected <C> Pager<C> createPager(PagingListView<C> listView) {
    return new PageSizePager<C>(listView, listView.getPageSize());
  }

  /**
   * Create a {@link PagingListView} that will display items. The
   * {@link PagingListView} must extend {@link Widget}.
   * 
   * @param <C> the item type in the list view
   * @param nodeInfo the node info with child data
   * @param cell the cell to use in the list view
   * @return the {@link ListView}
   */
  protected <C> PagingListView<C> createPagingListView(NodeInfo<C> nodeInfo,
      Cell<C> cell) {
    CellList<C> pagingListView = new CellList<C>(cell);
    pagingListView.setValueUpdater(nodeInfo.getValueUpdater());
    return pagingListView;
  }

  /**
   * Adjust the size of the scroll lock element based on the new position of the
   * scroll bar.
   */
  private void adjustScrollLock() {
    int scrollLeft = getElement().getScrollLeft();
    if (scrollLeft > 0) {
      int clientWidth = getElement().getClientWidth();
      scrollLock.getStyle().setWidth(scrollLeft + clientWidth, Unit.PX);
    } else {
      scrollLock.getStyle().setWidth(1.0, Unit.PX);
    }
  }

  /**
   * Create a new {@link TreeNode} and append it to the end of the LayoutPanel.
   * 
   * @param <C> the data type of the children
   * @param nodeInfo the info about the node
   */
  private <C> void appendTreeNode(final NodeInfo<C> nodeInfo) {
    // Create the list view.
    final int level = treeNodes.size();
    final CellDecorator<C> cell = new CellDecorator<C>(nodeInfo, level);
    final PagingListView<C> listView = createPagingListView(nodeInfo, cell);
    assert (listView instanceof Widget) : "createPagingListView() must return a widget";

    // Create a pager and wrap the components in a scrollable container.
    ScrollPanel scrollable = new ScrollPanel();
    final Pager<C> pager = createPager(listView);
    if (pager != null) {
      assert (pager instanceof Widget) : "createPager() must return a widget";
      FlowPanel flowPanel = new FlowPanel();
      flowPanel.add((Widget) listView);
      flowPanel.add((Widget) pager);
      scrollable.setWidget(flowPanel);
    } else {
      scrollable.setWidget((Widget) listView);
    }
    scrollable.setStyleName(STYLENAME_COLUMN);
    if (level == 0) {
      scrollable.addStyleName(STYLENAME_FIRST_COLUMN);
    }

    // Create a delegate list view so we can trap data changes.
    ListView<C> listViewDelegate = new ListView<C>() {
      public Range getRange() {
        return listView.getRange();
      }

      public void setData(int start, int length, List<C> values) {
        // Trim to the current level if the open node no longer exists.
        TreeNode<?> node = treeNodes.get(level);
        Object openKey = node.getCell().openKey;
        if (openKey != null) {
          boolean stillExists = false;
          ProvidesKey<C> keyProvider = nodeInfo.getProvidesKey();
          for (C value : values) {
            if (openKey.equals(keyProvider.getKey(value))) {
              stillExists = true;
              break;
            }
          }
          if (!stillExists) {
            trimToLevel(level);
          }
        }

        // Refresh the list.
        listView.setData(start, length, values);

        // Update the style of the open item. We do this after the rows have
        // been rendered because we want the style to live on the cell's parent
        // element, which is controlled by the PagingListView.
        cell.updateOpenElement(true);
      }

      public void setDataSize(int size, boolean isExact) {
        listView.setDataSize(size, isExact);
      }

      public void setDelegate(Delegate<C> delegate) {
        listView.setDelegate(delegate);
      }

      public void setSelectionModel(SelectionModel<? super C> selectionModel) {
        listView.setSelectionModel(selectionModel);
      }
    };

    // Create a TreeNode.
    TreeNode<C> treeNode = new TreeNode<C>(nodeInfo, listViewDelegate, cell,
        scrollable);
    treeNodes.add(treeNode);

    // Attach the view to the selection model and node info.
    listView.setSelectionModel(nodeInfo.getSelectionModel());
    nodeInfo.setView(listViewDelegate);

    // Add the ListView to the LayoutPanel.
    SplitLayoutPanel splitPanel = getSplitLayoutPanel();
    splitPanel.insertWest(scrollable, defaultWidth, null);
    splitPanel.setWidgetMinSize(scrollable, minWidth);
    splitPanel.forceLayout();

    // Scroll to the right.
    animation.scrollToEnd();
  }

  /**
   * @return the HTML to render the closed image.
   */
  private String getClosedImageHtml() {
    if (closedImageHtml == null) {
      AbstractImagePrototype proto = AbstractImagePrototype.create(getResources().treeClosed());
      closedImageHtml = proto.getHTML().replace("style='",
          "style='position:absolute;right:0px;top:0px;");
    }
    return closedImageHtml;
  }

  /**
   * @return the HTML to render the open image.
   */
  private String getOpenImageHtml() {
    if (openImageHtml == null) {
      AbstractImagePrototype proto = AbstractImagePrototype.create(getResources().treeOpen());
      openImageHtml = proto.getHTML().replace("style='",
          "style='position:absolute;right:0px;top:0px;");
    }
    return openImageHtml;
  }

  /**
   * Get the {@link SplitLayoutPanel} used to lay out the views.
   * 
   * @return the {@link SplitLayoutPanel}
   */
  private SplitLayoutPanel getSplitLayoutPanel() {
    return (SplitLayoutPanel) getWidget();
  }

  /**
   * Reduce the number of {@link ListView}s down to the specified level.
   * 
   * @param level the level to trim to
   */
  private void trimToLevel(int level) {
    // Add a placeholder to maintain the same scroll width.
    adjustScrollLock();

    // Remove the listViews that are no longer needed.
    int curLevel = treeNodes.size() - 1;
    while (curLevel > level) {
      TreeNode<?> removed = treeNodes.remove(curLevel);
      removed.cleanup();
      curLevel--;
    }
  }
}
