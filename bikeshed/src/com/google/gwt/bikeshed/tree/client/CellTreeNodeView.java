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

import com.google.gwt.bikeshed.cells.client.Cell;
import com.google.gwt.bikeshed.list.client.PagingListView;
import com.google.gwt.bikeshed.list.client.impl.CellListImpl;
import com.google.gwt.bikeshed.list.shared.ProvidesKey;
import com.google.gwt.bikeshed.list.shared.Range;
import com.google.gwt.bikeshed.list.shared.SelectionModel;
import com.google.gwt.bikeshed.tree.client.CellTreeViewModel.NodeInfo;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.user.client.ui.UIObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A view of a tree node.
 * 
 * @param <T> the type that this view contains
 */
class CellTreeNodeView<T> extends UIObject {

  /**
   * The default number of children to show under a tree node.
   */
  private static final int DEFAULT_LIST_SIZE = 100;

  /**
   * The element used in place of an image when a node has no children.
   */
  private static final String LEAF_IMAGE = "<div style='position:absolute;display:none;'></div>";

  /**
   * Returns the element that parents the cell contents of the node.
   * 
   * @param nodeElem the element that represents the node
   * @return the cell parent within the node
   */
  private static Element getCellParent(Element nodeElem) {
    return nodeElem.getChild(1).cast();
  }

  /**
   * Show or hide an element.
   * 
   * @param element the element to show or hide
   * @param show true to show, false to hide
   */
  private static void showOrHide(Element element, boolean show) {
    if (show) {
      element.getStyle().clearDisplay();
    } else {
      element.getStyle().setDisplay(Display.NONE);
    }
  }

  /**
   * The {@link com.google.gwt.bikeshed.list.client.ListView ListView} used to
   * show children.
   * 
   * @param <C> the child item type
   */
  private static class NodeListView<C> implements PagingListView<C> {

    private final CellListImpl<C> impl;
    private CellTreeNodeView<?> nodeView;
    private Map<Object, CellTreeNodeView<?>> savedViews;

    public NodeListView(final NodeInfo<C> nodeInfo,
        final CellTreeNodeView<?> nodeView) {
      this.nodeView = nodeView;

      final Cell<C> cell = nodeInfo.getCell();
      impl = new CellListImpl<C>(this, DEFAULT_LIST_SIZE,
          nodeView.ensureChildContainer()) {

        @Override
        public void setData(List<C> values, int start) {
          showOrHide(nodeView.loadingMessageElem, false);

          // Ensure that we have a children array.
          if (nodeView.children == null) {
            nodeView.children = new ArrayList<CellTreeNodeView<?>>();
          }

          // Construct a map of former child views based on their value keys.
          int len = values.size();
          int end = start + len;
          int childCount = nodeView.getChildCount();
          Map<Object, CellTreeNodeView<?>> openNodes = new HashMap<Object, CellTreeNodeView<?>>();
          for (int i = start; i < end && i < childCount; i++) {
            CellTreeNodeView<?> child = nodeView.getChildNode(i);
            // Ignore child nodes that are closed.
            if (child.isOpen()) {
              openNodes.put(child.getValueKey(), child);
            }
          }

          // Hide the child container so we can animate it.
          if (nodeView.tree.isAnimationEnabled()) {
            nodeView.ensureAnimationFrame().getStyle().setDisplay(Display.NONE);
          }

          // Trim the saved views down to the children that still exists.
          ProvidesKey<C> providesKey = nodeInfo.getProvidesKey();
          savedViews = new HashMap<Object, CellTreeNodeView<?>>();
          for (C childValue : values) {
            // Remove any child elements that correspond to prior children
            // so the call to setInnerHtml will not destroy them
            Object key = providesKey.getKey(childValue);
            CellTreeNodeView<?> savedView = openNodes.remove(key);
            if (savedView != null) {
              savedView.ensureAnimationFrame().removeFromParent();
              savedViews.put(key, savedView);
            }
          }

          // Create the new cells.
          super.setData(values, start);

          // Create the child TreeNodeViews from the new elements.
          Element childElem = nodeView.ensureChildContainer().getFirstChildElement();
          for (int i = start; i < end; i++) {
            C childValue = values.get(i - start);
            CellTreeNodeView<C> child = nodeView.createTreeNodeView(nodeInfo,
                childElem, childValue, null);
            CellTreeNodeView<?> savedChild = savedViews.remove(providesKey.getKey(childValue));
            // Copy the saved child's state into the new child
            if (savedChild != null) {
              child.animationFrame = savedChild.animationFrame;
              child.contentContainer = savedChild.contentContainer;
              child.childContainer = savedChild.childContainer;
              child.children = savedChild.children;
              child.emptyMessageElem = savedChild.emptyMessageElem;
              child.nodeInfo = savedChild.nodeInfo;
              child.nodeInfoLoaded = savedChild.nodeInfoLoaded;
              child.open = savedChild.open;
              child.showFewerElem = savedChild.showFewerElem;
              child.showMoreElem = savedChild.showMoreElem;

              // Swap the node view in the child. We reuse the same NodeListView
              // so that we don't have to unset and register a new view with the
              // NodeInfo.
              savedChild.listView.setNodeView(child);

              // Copy the child container element to the new child
              child.getElement().appendChild(savedChild.ensureAnimationFrame());
            }

            if (childCount > i) {
              if (savedChild == null) {
                // Cleanup the child node if we aren't going to reuse it.
                nodeView.children.get(i).cleanup();
              }
              nodeView.children.set(i, child);
            } else {
              nodeView.children.add(child);
            }
            childElem = childElem.getNextSiblingElement();
          }

          // Clear temporary state.
          savedViews = null;

          // Animate the child container open.
          if (nodeView.tree.isAnimationEnabled()) {
            nodeView.tree.maybeAnimateTreeNode(nodeView);
          }
        }

        @Override
        protected boolean dependsOnSelection() {
          return cell.dependsOnSelection();
        }

        @Override
        protected void emitHtml(StringBuilder sb, List<C> values, int start,
            SelectionModel<? super C> selectionModel) {
          String selectedStyle = nodeView.tree.getStyle().selectedItem();
          String itemStyle = nodeView.tree.getStyle().item();
          String openStyle = nodeView.tree.getStyle().openItem();

          ProvidesKey<C> providesKey = nodeInfo.getProvidesKey();
          CellTreeViewModel model = nodeView.tree.getTreeViewModel();
          int imageWidth = nodeView.tree.getImageWidth();
          for (C value : values) {
            Object key = providesKey.getKey(value);
            boolean isOpen = savedViews.containsKey(key);
            sb.append("<div style='position:relative;padding-left:");
            sb.append(imageWidth).append("px'>");
            if (isOpen) {
              sb.append(nodeView.tree.getOpenImageHtml());
            } else if (model.isLeaf(value)) {
              sb.append(LEAF_IMAGE);
            } else {
              sb.append(nodeView.tree.getClosedImageHtml());
            }
            sb.append("<div class='").append(itemStyle);
            if (isOpen) {
              sb.append(" ").append(openStyle);
            }
            if (selectionModel != null && selectionModel.isSelected(value)) {
              sb.append(" ").append(selectedStyle);
            }
            sb.append("'>");
            cell.render(value, null, sb);
            sb.append("</div></div>");
          }
        }

        @Override
        protected void removeLastItem() {
          CellTreeNodeView<?> child = nodeView.children.remove(nodeView.children.size() - 1);
          child.cleanup();
          super.removeLastItem();
        }

        @Override
        protected void setSelected(Element elem, boolean selected) {
          setStyleName(getCellParent(elem),
              nodeView.tree.getStyle().selectedItem(), selected);
        }
      };

      // Use a pager to update buttons.
      impl.setPager(new Pager<C>() {
        public void onRangeOrSizeChanged(PagingListView<C> listView) {
          // Assumes a page start of 0.
          showOrHide(nodeView.showMoreElem,
              impl.getDataSize() > impl.getPageSize());
          showOrHide(nodeView.showFewerElem,
              impl.getPageSize() > DEFAULT_LIST_SIZE);
          showOrHide(nodeView.emptyMessageElem, impl.getDataSize() == 0);
        }
      });
    }

    /**
     * Cleanup this node view.
     */
    public void cleanup() {
      impl.setSelectionModel(null, false);
    }

    public int getDataSize() {
      return impl.getDataSize();
    }

    public int getPageSize() {
      return impl.getPageSize();
    }

    public int getPageStart() {
      return impl.getPageStart();
    }

    public Range getRange() {
      return impl.getRange();
    }

    public void setData(int start, int length, List<C> values) {
      impl.setData(values, start);
    }

    public void setDataSize(int size, boolean isExact) {
      impl.setDataSize(size);
    }

    public void setDelegate(Delegate<C> delegate) {
      impl.setDelegate(delegate);
    }

    public void setPager(Pager<C> pager) {
      impl.setPager(pager);
    }

    public void setPageSize(int pageSize) {
      impl.setPageSize(pageSize);
    }

    public void setPageStart(int pageStart) {
      impl.setPageStart(pageStart);
    }

    public void setSelectionModel(final SelectionModel<? super C> selectionModel) {
      impl.setSelectionModel(selectionModel, true);
    }

    /**
     * Assign this {@link PagingListView} to a new {@link CellTreeNodeView}.
     * 
     * @param nodeView the new node view
     */
    private void setNodeView(CellTreeNodeView<?> nodeView) {
      this.nodeView.listView = null;
      this.nodeView = nodeView;
      nodeView.listView = this;
    }
  }

  /**
   * True during the time a node should be animated.
   */
  private boolean animate;

  /**
   * A reference to the element that is used to animate nodes. Parent of the
   * contentContainer.
   */
  private Element animationFrame;

  /**
   * A reference to the element that contains the children. Parent to the actual
   * child nodes.
   */
  private Element childContainer;

  /**
   * A list of child views.
   */
  private List<CellTreeNodeView<?>> children;

  /**
   * A reference to the element that contains all content. Parent of the
   * childContainer and the show/hide elements.
   */
  private Element contentContainer;

  /**
   * The element used when there are no children to display.
   */
  private Element emptyMessageElem;

  /**
   * The list view used to display the nodes.
   */
  private NodeListView<?> listView;

  /**
   * The element used when the children are loading.
   */
  private Element loadingMessageElem;

  /**
   * The info about children of this node.
   */
  private NodeInfo<?> nodeInfo;

  /**
   * Indicates whether or not we've loaded the node info.
   */
  private boolean nodeInfoLoaded;

  /**
   * Indicates whether or not this node is open.
   */
  private boolean open;

  /**
   * The parent {@link CellTreeNodeView}.
   */
  private final CellTreeNodeView<?> parentNode;

  /**
   * The {@link NodeInfo} of the parent node.
   */
  private final NodeInfo<T> parentNodeInfo;

  /**
   * The element used to display less children.
   */
  private Element showFewerElem;

  /**
   * The element used to display more children.
   */
  private Element showMoreElem;

  /**
   * The {@link CellTree} that this node belongs to.
   */
  private final CellTree tree;

  /**
   * This node's value.
   */
  private T value;

  /**
   * Construct a {@link CellTreeNodeView}.
   * 
   * @param tree the parent {@link CellTreeNodeView}
   * @param parent the parent {@link CellTreeNodeView}
   * @param parentNodeInfo the {@link NodeInfo} of the parent
   * @param elem the outer element of this {@link CellTreeNodeView}
   * @param value the value of this node
   */
  CellTreeNodeView(final CellTree tree, final CellTreeNodeView<?> parent,
      NodeInfo<T> parentNodeInfo, Element elem, T value) {
    this.tree = tree;
    this.parentNode = parent;
    this.parentNodeInfo = parentNodeInfo;
    this.value = value;
    setElement(elem);
  }

  public int getChildCount() {
    return children == null ? 0 : children.size();
  }

  public CellTreeNodeView<?> getChildNode(int childIndex) {
    return children.get(childIndex);
  }

  /**
   * Check whether or not this node is open.
   * 
   * @return true if open, false if closed
   */
  public boolean isOpen() {
    return open;
  }

  /**
   * Select this node.
   */
  public void select() {
    SelectionModel<? super T> selectionModel = parentNodeInfo.getSelectionModel();
    if (selectionModel != null) {
      selectionModel.setSelected(value, true);
    }
  }

  /**
   * Sets whether this item's children are displayed.
   * 
   * @param open whether the item is open
   */
  public void setOpen(boolean open) {
    // Early out.
    if (this.open == open) {
      return;
    }

    this.animate = true;
    this.open = open;
    if (open) {
      if (!nodeInfoLoaded) {
        nodeInfoLoaded = true;
        nodeInfo = tree.getTreeViewModel().getNodeInfo(value);
      }

      // If we don't have any nodeInfo, we must be a leaf node.
      if (nodeInfo != null) {
        // Add a loading message.
        ensureChildContainer();
        showOrHide(loadingMessageElem, true);
        showOrHide(showFewerElem, false);
        showOrHide(showMoreElem, false);
        showOrHide(emptyMessageElem, false);
        ensureAnimationFrame().getStyle().setProperty("display", "");
        onOpen(nodeInfo);
      }
    } else {
      cleanup();
      tree.maybeAnimateTreeNode(this);
    }

    // Update the image.
    updateImage();
  }

  /**
   * Unregister the list handler and destroy all child nodes.
   */
  protected void cleanup() {
    // Unregister the list handler.
    if (listView != null) {
      listView.cleanup();
      nodeInfo.unsetView();
      listView = null;
    }

    // Recursively kill children.
    if (children != null) {
      for (CellTreeNodeView<?> child : children) {
        child.cleanup();
      }
      children = null;
    }
  }

  protected boolean consumeAnimate() {
    boolean hasAnimate = animate;
    animate = false;
    return hasAnimate;
  }

  /**
   * Returns an instance of TreeNodeView of the same subclass as the calling
   * object.
   * 
   * @param <C> the data type of the node's children
   * @param nodeInfo a NodeInfo object describing the child nodes
   * @param childElem the DOM element used to parent the new TreeNodeView
   * @param childValue the child's value
   * @param viewData view data associated with the node
   * @return a TreeNodeView of suitable type
   */
  protected <C> CellTreeNodeView<C> createTreeNodeView(NodeInfo<C> nodeInfo,
      Element childElem, C childValue, Object viewData) {
    return new CellTreeNodeView<C>(tree, this, nodeInfo, childElem, childValue);
  }

  /**
   * Fire an event to the {@link com.google.gwt.bikeshed.cells.client.Cell}.
   * 
   * @param event the native event
   * @return true if the cell consumes the event, false if not
   */
  protected boolean fireEventToCell(NativeEvent event) {
    if (parentNodeInfo != null) {
      Element cellParent = getCellParent();
      Cell<T> parentCell = parentNodeInfo.getCell();
      parentCell.onBrowserEvent(cellParent, value, null, event,
          parentNodeInfo.getValueUpdater());
      return parentCell.consumesEvents();
    }
    return false;
  }

  /**
   * Returns the element that parents the cell contents of this node.
   */
  protected Element getCellParent() {
    return getCellParent(getElement());
  }

  /**
   * Returns the element corresponding to the open/close image.
   * 
   * @return the open/close image element
   */
  protected Element getImageElement() {
    return getElement().getFirstChildElement();
  }

  /**
   * Returns the key for the value of this node using the parent's
   * implementation of NodeInfo.getKey().
   */
  protected Object getValueKey() {
    return parentNodeInfo.getProvidesKey().getKey(value);
  }

  /**
   * Set up the node when it is opened.
   * 
   * @param nodeInfo the {@link NodeInfo} that provides information about the
   *          child values
   * @param <C> the child data type of the node
   */
  protected <C> void onOpen(final NodeInfo<C> nodeInfo) {
    NodeListView<C> view = new NodeListView<C>(nodeInfo, this);
    listView = view;
    view.setSelectionModel(nodeInfo.getSelectionModel());
    nodeInfo.setView(view);
  }

  /**
   * Update the image based on the current state.
   */
  protected void updateImage() {
    // Early out if this is a root node.
    if (parentNode == null) {
      return;
    }

    // Replace the image element with a new one.
    String html = open ? tree.getOpenImageHtml() : tree.getClosedImageHtml();
    if (nodeInfoLoaded && nodeInfo == null) {
      html = LEAF_IMAGE;
    }
    Element tmp = Document.get().createDivElement();
    tmp.setInnerHTML(html);
    Element imageElem = tmp.getFirstChildElement();

    Element oldImg = getImageElement();
    oldImg.getParentElement().replaceChild(imageElem, oldImg);
  }

  /**
   * Ensure that the animation frame exists and return it.
   * 
   * @return the animation frame
   */
  Element ensureAnimationFrame() {
    if (animationFrame == null) {
      animationFrame = Document.get().createDivElement();
      animationFrame.getStyle().setPosition(Position.RELATIVE);
      animationFrame.getStyle().setOverflow(Overflow.HIDDEN);
      animationFrame.setId("animFrame");
      getElement().appendChild(animationFrame);
    }
    return animationFrame;
  }

  /**
   * Ensure that the child container exists and return it.
   * 
   * @return the child container
   */
  Element ensureChildContainer() {
    if (childContainer == null) {
      childContainer = Document.get().createDivElement();
      ensureContentContainer().insertFirst(childContainer);
    }
    return childContainer;
  }

  /**
   * Ensure that the content container exists and return it.
   * 
   * @return the content container
   */
  Element ensureContentContainer() {
    if (contentContainer == null) {
      contentContainer = Document.get().createDivElement();
      ensureAnimationFrame().appendChild(contentContainer);

      loadingMessageElem = Document.get().createDivElement();
      loadingMessageElem.setInnerHTML(tree.getLoadingHtml());
      showOrHide(loadingMessageElem, false);
      contentContainer.appendChild(loadingMessageElem);

      // TODO(jlabanca): I18N no data string.
      emptyMessageElem = Document.get().createDivElement();
      emptyMessageElem.setInnerHTML("<i>no data</i>");
      showOrHide(emptyMessageElem, false);
      contentContainer.appendChild(emptyMessageElem);

      showMoreElem = Document.get().createPushButtonElement();
      showMoreElem.setInnerText("Show more");
      showOrHide(showMoreElem, false);
      contentContainer.appendChild(showMoreElem);

      showFewerElem = Document.get().createPushButtonElement();
      showFewerElem.setInnerText("Show fewer");
      showOrHide(showFewerElem, false);
      contentContainer.appendChild(showFewerElem);
    }
    return contentContainer;
  }

  Element getShowFewerElement() {
    return showFewerElem;
  }

  Element getShowMoreElement() {
    return showMoreElem;
  }

  void showFewer() {
    int maxSize = Math.max(DEFAULT_LIST_SIZE, listView.impl.getPageSize()
        - DEFAULT_LIST_SIZE);
    listView.impl.setPageSize(maxSize);
  }

  void showMore() {
    listView.impl.setPageSize(listView.impl.getPageSize() + DEFAULT_LIST_SIZE);
  }
}
