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
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;
import com.google.gwt.sample.bikeshed.style.client.Styles;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasAnimation;
import com.google.gwt.user.client.ui.SimplePanel;

import java.util.ArrayList;

/**
 * A view of a tree.
 */
public class CellTree extends Composite implements HasAnimation {

  /**
   * Styles used by this widget.
   */
  public static interface Style extends CssResource {

    /**
     * Applied to tree items.
     */
    String item();

    /**
     * Applied to open/close icon.
     */
    String itemImage();

    /**
     * Applied to open tree items.
     */
    String openItem();

    /**
     * Applied to selected tree items.
     */
    String selectedItem();
  }

  /**
   * A ClientBundle that provides images for this widget.
   */
  public static interface Resources extends ClientBundle {

    /**
     * An image indicating a closed branch.
     */
    ImageResource cellTreeClosedItem();

    /**
     * An image indicating an open branch.
     */
    ImageResource cellTreeOpenItem();

    /**
     * The background used for selected items.
     */
    @Source("../../list/client/cellListSelectedBackground.png")
    @ImageOptions(repeatStyle = RepeatStyle.Horizontal)
    ImageResource cellTreeSelectedBackground();

    /**
     * The styles used in this widget.
     */
    @Source("CellTree.css")
    Style cellTreeStyle();
  }

  /**
   * A node animation.
   */
  public abstract static class NodeAnimation extends Animation {

    /**
     * The default animation delay in milliseconds.
     */
    private static final int DEFAULT_ANIMATION_DURATION = 450;

    /**
     * The duration of the animation.
     */
    private int duration = DEFAULT_ANIMATION_DURATION;

    NodeAnimation() {
    }

    /**
     * Animate a tree node into its new state.
     * 
     * @param node the node to animate
     * @param isAnimationEnabled true to animate
     */
    abstract void animate(CellTreeNodeView<?> node, boolean isAnimationEnabled);

    public int getDuration() {
      return duration;
    }

    public void setDuration(int duration) {
      this.duration = duration;
    }
  }

  /**
   * A {@link NodeAnimation} that reveals the contents of child nodes.
   */
  public static class RevealAnimation extends NodeAnimation {

    /**
     * Create a new {@link RevealAnimation}.
     * 
     * @return the new animation
     */
    public static RevealAnimation create() {
      return new RevealAnimation();
    }

    /**
     * The container that holds the content, includind the children.
     */
    Element contentContainer;

    /**
     * The target height when opening, the start height when closing.
     */
    int height;

    /**
     * True if the node is opening, false if closing.
     */
    boolean opening;

    /**
     * The container that holds the child container.
     */
    private Element animFrame;

    /**
     * The container that holds the children.
     */
    private Element childContainer;

    /**
     * Not instantiable.
     */
    private RevealAnimation() {
    }

    /**
     * Animate a {@link CellTreeNodeView} into its new state.
     * 
     * @param node the {@link CellTreeNodeView} to animate
     * @param isAnimationEnabled true to animate
     */
    @Override
    void animate(CellTreeNodeView<?> node, boolean isAnimationEnabled) {
      // Cancel any pending animations.
      cancel();

      // Initialize the fields.
      this.opening = node.isOpen();
      animFrame = node.ensureAnimationFrame();
      contentContainer = node.ensureContentContainer();
      childContainer = node.ensureChildContainer();

      if (isAnimationEnabled) {
        // Animated.
        int duration = getDuration();
        int childCount = childContainer.getChildCount();
        if (childCount < 4) {
          // Reduce the duration if there are less than four items or it will
          // look really slow.
          duration = (int) ((childCount / 4.0) * duration);
        }
        run(duration);
      } else {
        // Non animated.
        cleanup();
      }
    }

    @Override
    protected void onComplete() {
      cleanup();
    }

    @Override
    protected void onStart() {
      if (opening) {
        animFrame.getStyle().setHeight(1.0, Unit.PX);
        animFrame.getStyle().clearDisplay();
        height = contentContainer.getScrollHeight();
      } else {
        height = contentContainer.getOffsetHeight();
      }
    }

    @Override
    protected void onUpdate(double progress) {
      if (opening) {
        double curHeight = progress * height;
        animFrame.getStyle().setHeight(curHeight, Unit.PX);
      } else {
        double curHeight = (1.0 - progress) * height;
        animFrame.getStyle().setHeight(curHeight, Unit.PX);
      }
    }

    /**
     * Put the node back into a clean state and clear fields.
     */
    private void cleanup() {
      if (opening) {
        animFrame.getStyle().clearDisplay();
      } else {
        animFrame.getStyle().setDisplay(Display.NONE);
        childContainer.setInnerHTML("");
      }
      animFrame.getStyle().clearHeight();
      this.contentContainer = null;
      this.childContainer = null;
      this.animFrame = null;
    }
  }

  /**
   * A {@link NodeAnimation} that slides children into view.
   */
  public static class SlideAnimation extends RevealAnimation {
    /**
     * Create a new {@link RevealAnimation}.
     * 
     * @return the new animation
     */
    public static SlideAnimation create() {
      return new SlideAnimation();
    }

    /**
     * Not instantiable.
     */
    private SlideAnimation() {
    }

    @Override
    protected void onComplete() {
      contentContainer.getStyle().clearPosition();
      contentContainer.getStyle().clearTop();
      contentContainer.getStyle().clearWidth();
      super.onComplete();
    }

    @Override
    protected void onStart() {
      super.onStart();
      if (opening) {
        contentContainer.getStyle().setTop(-height, Unit.PX);
      } else {
        contentContainer.getStyle().setTop(0, Unit.PX);
      }
      contentContainer.getStyle().setPosition(Position.RELATIVE);
    }

    @Override
    protected void onUpdate(double progress) {
      super.onUpdate(progress);
      if (opening) {
        double curTop = (1.0 - progress) * -height;
        contentContainer.getStyle().setTop(curTop, Unit.PX);
      } else {
        double curTop = progress * -height;
        contentContainer.getStyle().setTop(curTop, Unit.PX);
      }
    }
  }

  /**
   * The animation.
   */
  private NodeAnimation animation;

  /**
   * The HTML used to generate the closed image.
   */
  private final String closedImageHtml;

  /**
   * The maximum width of the open and closed images.
   */
  private final int imageWidth;

  /**
   * Indicates whether or not animations are enabled.
   */
  private boolean isAnimationEnabled;

  /**
   * The message displayed while child nodes are loading.
   */
  // TODO(jlabanca): I18N loading HTML, or remove the text.
  private String loadingHtml = "Loading...";

  /**
   * The HTML used to generate the open image.
   */
  private final String openImageHtml;

  /**
   * The hidden root node in the tree.
   */
  private final CellTreeNodeView<?> rootNode;

  /**
   * The styles used by this widget.
   */
  private final Style style;

  /**
   * The {@link CellTreeViewModel} that backs the tree.
   */
  private final CellTreeViewModel viewModel;

  /**
   * Construct a new {@link CellTree}.
   * 
   * @param <T> the type of data in the root node
   * @param viewModel the {@link CellTreeViewModel} that backs the tree
   * @param rootValue the hidden root value of the tree
   */
  public <T> CellTree(CellTreeViewModel viewModel, T rootValue) {
    this(viewModel, rootValue, Styles.resources());
  }

  /**
   * Construct a new {@link CellTree}.
   * 
   * @param <T> the type of data in the root node
   * @param viewModel the {@link CellTreeViewModel} that backs the tree
   * @param rootValue the hidden root value of the tree
   * @param resources the resources used to render the tree
   */
  public <T> CellTree(CellTreeViewModel viewModel, T rootValue,
      Resources resources) {
    this.viewModel = viewModel;
    this.style = resources.cellTreeStyle();
    this.style.ensureInjected();
    initWidget(new SimplePanel());
    setStyleName("gwt-StandardTreeView");

    // Initialize the open and close images strings.
    ImageResource treeOpen = resources.cellTreeOpenItem();
    ImageResource treeClosed = resources.cellTreeClosedItem();
    openImageHtml = getImageHtml(treeOpen);
    closedImageHtml = getImageHtml(treeClosed);
    imageWidth = Math.max(treeOpen.getWidth(), treeClosed.getWidth());

    // We use one animation for the entire tree.
    setAnimation(SlideAnimation.create());

    // Add event handlers.
    sinkEvents(Event.ONCLICK | Event.ONCHANGE | Event.MOUSEEVENTS);

    // Associate a view with the item.
    CellTreeNodeView<T> root = new CellTreeNodeView<T>(this, null, null,
        getElement(), rootValue);
    rootNode = root;
    root.setOpen(true);
  }

  /**
   * Get the animation used to open and close nodes in this tree if animations
   * are enabled.
   * 
   * @return the animation
   * @see #isAnimationEnabled()
   */
  public NodeAnimation getAnimation() {
    return animation;
  }

  /**
   * Get the HTML string that is displayed while nodes wait for their children
   * to load.
   * 
   * @return the loading HTML string
   */
  public String getLoadingHtml() {
    return loadingHtml;
  }

  public CellTreeViewModel getTreeViewModel() {
    return viewModel;
  }

  public boolean isAnimationEnabled() {
    return isAnimationEnabled;
  }

  @Override
  public void onBrowserEvent(Event event) {
    super.onBrowserEvent(event);

    Element target = event.getEventTarget().cast();

    ArrayList<Element> chain = new ArrayList<Element>();
    collectElementChain(chain, getElement(), target);

    CellTreeNodeView<?> nodeView = findItemByChain(chain, 0, rootNode);
    if (nodeView != null && nodeView != rootNode) {
      if ("click".equals(event.getType())) {
        // Open the node when the open image is clicked.
        Element showFewerElem = nodeView.getShowFewerElement();
        Element showMoreElem = nodeView.getShowMoreElement();
        if (nodeView.getImageElement().isOrHasChild(target)) {
          nodeView.setOpen(!nodeView.isOpen());
          return;
        } else if (showFewerElem != null && showFewerElem.isOrHasChild(target)) {
          nodeView.showFewer();
          return;
        } else if (showMoreElem != null && showMoreElem.isOrHasChild(target)) {
          nodeView.showMore();
          return;
        }
      }

      // Forward the event to the cell.
      if (nodeView.getCellParent().isOrHasChild(target)) {
        boolean consumesEvent = nodeView.fireEventToCell(event);
        if (!consumesEvent && "click".equals(event.getType())) {
          nodeView.select();
        }
      }
    }
  }

  /**
   * Set the animation used to open and close nodes in this tree. You must call
   * {@link #setAnimationEnabled(boolean)} to enable or disable animation.
   * 
   * @param animation a {@link NodeAnimation}
   * @see #setAnimationEnabled(boolean)
   */
  public void setAnimation(NodeAnimation animation) {
    assert animation != null : "animation cannot be null";
    this.animation = animation;
  }

  public void setAnimationEnabled(boolean enable) {
    this.isAnimationEnabled = enable;
    if (!enable && animation != null) {
      animation.cancel();
    }
  }

  /**
   * Set the HTML string that will be displayed when a node is waiting for its
   * child nodes to load.
   * 
   * @param loadingHtml the HTML string
   */
  public void setLoadingHtml(String loadingHtml) {
    this.loadingHtml = loadingHtml;
  }

  /**
   * @return the HTML to render the closed image.
   */
  String getClosedImageHtml() {
    return closedImageHtml;
  }

  /**
   * Get the width required for the images.
   * 
   * @return the maximum width required for images.
   */
  int getImageWidth() {
    return imageWidth;
  }

  /**
   * @return the HTML to render the open image.
   */
  String getOpenImageHtml() {
    return openImageHtml;
  }

  /**
   * @return the Style used by the tree
   */
  Style getStyle() {
    return style;
  }

  /**
   * Animate the current state of a {@link CellTreeNodeView} in this tree.
   * 
   * @param node the node to animate
   */
  void maybeAnimateTreeNode(CellTreeNodeView<?> node) {
    if (animation != null) {
      animation.animate(node, node.consumeAnimate() && isAnimationEnabled());
    }
  }

  /**
   * Collects parents going up the element tree, terminated at the tree root.
   */
  private void collectElementChain(ArrayList<Element> chain, Element hRoot,
      Element hElem) {
    if ((hElem == null) || (hElem == hRoot)) {
      return;
    }

    collectElementChain(chain, hRoot, hElem.getParentElement());
    chain.add(hElem);
  }

  private CellTreeNodeView<?> findItemByChain(ArrayList<Element> chain,
      int idx, CellTreeNodeView<?> parent) {
    if (idx == chain.size()) {
      return parent;
    }

    Element hCurElem = chain.get(idx);
    for (int i = 0, n = parent.getChildCount(); i < n; ++i) {
      CellTreeNodeView<?> child = parent.getChildNode(i);
      if (child.getElement() == hCurElem) {
        CellTreeNodeView<?> retItem = findItemByChain(chain, idx + 1, child);
        if (retItem == null) {
          return child;
        }
        return retItem;
      }
    }

    return findItemByChain(chain, idx + 1, parent);
  }

  /**
   * Get the HTML representation of an image.
   * 
   * @param res the {@link ImageResource} to render as HTML
   * @return the rendered HTML
   */
  private String getImageHtml(ImageResource res) {
    StringBuilder sb = new StringBuilder();
    sb.append("<div class='").append(style.itemImage()).append("' ");

    // Add the position and dimensions.
    sb.append("style=\"position:absolute;left:0px;top:0px;");
    sb.append("height:").append(res.getHeight()).append("px;");
    sb.append("width:").append(res.getWidth()).append("px;");

    // Add the background, vertically centered.
    sb.append("background:url('").append(res.getURL()).append("') ");
    sb.append("no-repeat scroll center center transparent;");

    // Close the div and return.
    sb.append("\"></div>");
    return sb.toString();
  }
}
