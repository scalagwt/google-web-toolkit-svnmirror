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

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventPreview;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.impl.PopupImpl;

/**
 * A panel that can "pop up" over other widgets. It overlays the browser's
 * client area (and any previously-created popups). <p/> The width and height of
 * the PopupPanel cannot be explicitly set; they are determined by the
 * PopupPanel's widget. Calls to {@link #setWidth(String)} and
 * {@link #setHeight(String)} will call these methods on the PopupPanel's
 * widget.
 * <p>
 * <img class='gallery' src='PopupPanel.png'/>
 * </p>
 * 
 * <p>
 * <h3>Example</h3>
 * {@example com.google.gwt.examples.PopupPanelExample}
 * </p>
 */
public class PopupPanel extends SimplePanel implements SourcesPopupEvents,
    EventPreview {

  /**
   * A callback that is used to set the position of a {@link PopupPanel} right
   * before it is shown.
   */
  public interface PositionCallback {

    /**
     * Provides the opportunity to set the position of the PopupPanel right
     * before the PopupPanel is shown. The offsetWidth and offsetHeight values
     * of the PopupPanel are made available to allow for positioning based on
     * its size.
     *
     * @param offsetWidth the offsetWidth of the PopupPanel
     * @param offsetHeight the offsetHeight of the PopupPanel
     * @see PopupPanel#setPopupPositionAndShow(PositionCallback)
     */
    public void setPosition(int offsetWidth, int offsetHeight);
  }

  private static final PopupImpl impl = (PopupImpl) GWT.create(PopupImpl.class);

  private boolean autoHide, modal, showing;

  // Used to track requested size across changing child widgets
  private String desiredHeight;

  private String desiredWidth;

  // the left style attribute in pixels
  private int leftPosition = -1;
  
  // The top style attribute in pixels
  private int topPosition = -1; 
  
  private PopupListenerCollection popupListeners;

  /**
   * Creates an empty popup panel. A child widget must be added to it before it
   * is shown.
   */
  public PopupPanel() {
    super(impl.createElement());

    // Default position of popup should be in the upper-left corner of the
    // window. By setting a default position, the popup will not appear in
    // an undefined location if it is shown before its position is set.
    setPopupPosition(0,0);
  }

  /**
   * Creates an empty popup panel, specifying its "auto-hide" property.
   * 
   * @param autoHide <code>true</code> if the popup should be automatically
   *          hidden when the user clicks outside of it
   */
  public PopupPanel(boolean autoHide) {
    this();
    this.autoHide = autoHide;
  }

  /**
   * Creates an empty popup panel, specifying its "auto-hide" property.
   * 
   * @param autoHide <code>true</code> if the popup should be automatically
   *          hidden when the user clicks outside of it
   * @param modal <code>true</code> if keyboard or mouse events that do not
   *          target the PopupPanel or its children should be ignored
   */
  public PopupPanel(boolean autoHide, boolean modal) {
    this(autoHide);
    this.modal = modal;
  }

  public void addPopupListener(PopupListener listener) {
    if (popupListeners == null) {
      popupListeners = new PopupListenerCollection();
    }
    popupListeners.add(listener);
  }

  /**
   * Centers the popup in the browser window and shows it. If the popup was
   * already showing, then the popup is centered. 
   */
  public void center() {
    boolean initiallyShowing = showing;

    if (!initiallyShowing) {
      setVisible(false);
      show();
    }

    int left = (Window.getClientWidth() - getOffsetWidth()) / 2;
    int top = (Window.getClientHeight() - getOffsetHeight()) / 2;
    setPopupPosition(Window.getScrollLeft() + left, Window.getScrollTop() + top);

    if (!initiallyShowing) {
      setVisible(true);
    }
  }

  /**
    * Gets the panel's offset height in pixels. Calls to {@link #setHeight(String)}
    * before the panel's child widget is set will not influence the offset height.
    *
    * @return the object's offset height
    */
   public int getOffsetHeight() {
     return super.getOffsetHeight();
   }

   /**
    * Gets the panel's offset width in pixels. Calls to {@link #setWidth(String)}
    * before the panel's child widget is set will not influence the offset width.
    *
    * @return the object's offset width
    */
   public int getOffsetWidth() {
     return super.getOffsetWidth();
   }

  /**
   * Gets the popup's left position relative to the browser's client area.
   * 
   * @return the popup's left position
   */
  public int getPopupLeft() {
    return DOM.getElementPropertyInt(getElement(), "offsetLeft");
  }

  /**
   * Gets the popup's top position relative to the browser's client area.
   * 
   * @return the popup's top position
   */
  public int getPopupTop() {
    return DOM.getElementPropertyInt(getElement(), "offsetTop");
  }

  public String getTitle() {
    return DOM.getElementProperty(getContainerElement(), "title");
  }

  /**
   * Hides the popup. This has no effect if it is not currently visible.
   */
  public void hide() {
    hide(false);
  }

  public boolean onEventPreview(Event event) {
    Element target = DOM.eventGetTarget(event);
    boolean eventTargetsPopup = DOM.isOrHasChild(getElement(), target);

    int type = DOM.eventGetType(event);
    switch (type) {
      case Event.ONKEYDOWN: {
        boolean allow = onKeyDownPreview((char) DOM.eventGetKeyCode(event),
            KeyboardListenerCollection.getKeyboardModifiers(event));
        return allow && (eventTargetsPopup || !modal);
      }
      case Event.ONKEYUP: {
        boolean allow =  onKeyUpPreview((char) DOM.eventGetKeyCode(event),
            KeyboardListenerCollection.getKeyboardModifiers(event));
        return allow && (eventTargetsPopup || !modal);
      }
      case Event.ONKEYPRESS: {
        boolean allow =  onKeyPressPreview((char) DOM.eventGetKeyCode(event),
            KeyboardListenerCollection.getKeyboardModifiers(event));
        return allow && (eventTargetsPopup || !modal);
      }

      case Event.ONMOUSEDOWN:
      case Event.ONMOUSEUP:
      case Event.ONMOUSEMOVE:
      case Event.ONCLICK:
      case Event.ONDBLCLICK: {
        // Don't eat events if event capture is enabled, as this can interfere
        // with dialog dragging, for example.
        if (DOM.getCaptureElement() != null) {
          return true;
        }

        // If it's an outside click and auto-hide is enabled:
        // hide the popup and _don't_ eat the event. ONMOUSEDOWN is used to
        // prevent problems with showing a popup in response to a mousedown.
        if (!eventTargetsPopup && autoHide && (type == Event.ONMOUSEDOWN)) {
          hide(true);
          return true;
        }

        break;
      }

      case Event.ONFOCUS: {
        if (modal && !eventTargetsPopup && (target != null)) {
          blur(target);
          return false;
        }
      }
    }

    return !modal || eventTargetsPopup;
  }

  /**
   * Popups get an opportunity to preview keyboard events before they are passed
   * to a widget contained by the Popup.
   * 
   * @param key the key code of the depressed key
   * @param modifiers keyboard modifiers, as specified in
   *          {@link KeyboardListener}.
   * @return <code>false</code> to suppress the event
   */
  public boolean onKeyDownPreview(char key, int modifiers) {
    return true;
  }

  /**
   * Popups get an opportunity to preview keyboard events before they are passed
   * to a widget contained by the Popup.
   * 
   * @param key the unicode character pressed
   * @param modifiers keyboard modifiers, as specified in
   *          {@link KeyboardListener}.
   * @return <code>false</code> to suppress the event
   */
  public boolean onKeyPressPreview(char key, int modifiers) {
    return true;
  }

  /**
   * Popups get an opportunity to preview keyboard events before they are passed
   * to a widget contained by the Popup.
   * 
   * @param key the key code of the released key
   * @param modifiers keyboard modifiers, as specified in
   *          {@link KeyboardListener}.
   * @return <code>false</code> to suppress the event
   */
  public boolean onKeyUpPreview(char key, int modifiers) {
    return true;
  }

  public void removePopupListener(PopupListener listener) {
    if (popupListeners != null) {
      popupListeners.remove(listener);
    }
  }

  /**
   * Sets the height of the panel's child widget. If the panel's child widget
   * has not been set, the height passed in will be cached and used to set
   * the height immediately after the child widget is set.
   *
   * @param height the object's new height, in CSS units (e.g. "10px", "1em")
   */
  public void setHeight(String height) {
    desiredHeight = height;
    maybeUpdateSize();
    // If the user cleared the size, revert to not trying to control children.
    if (height.length() == 0) {
      desiredHeight = null;
    }
  }

  /**
   * Sets the popup's position relative to the browser's client area. The
   * popup's position may be set before calling {@link #show()}.
   * 
   * @param left the left position, in pixels
   * @param top the top position, in pixels
   */
  public void setPopupPosition(int left, int top) {
    // Keep the popup within the browser's client area, so that they can't get
    // 'lost' and become impossible to interact with. Note that we don't attempt
    // to keep popups pegged to the bottom and right edges, as they will then
    // cause scrollbars to appear, so the user can't lose them.
    if (left < 0) {
      left = 0;
    }
    if (top < 0) {
      top = 0;
    }

    // Save the position of the popup
    leftPosition = left;
    topPosition = top;

    // Set the popup's position manually, allowing setPopupPosition() to be
    // called before show() is called (so a popup can be positioned without it
    // 'jumping' on the screen).
    Element elem = getElement();
    DOM.setStyleAttribute(elem, "left", left + "px");
    DOM.setStyleAttribute(elem, "top", top + "px");
  }

  /**
   * Sets the popup's position using a {@link PositionCallback}, and shows
   * the popup. The callback allows positioning to be performed based on
   * the offsetWidth and offsetHeight of the popup, which are normally
   * not available until the popup is showing. By positioning the popup
   * before it is shown, the the popup will not jump from its original
   * position to the new position.
   *
   * @param callback the callback to set the position of the popup
   * @see PositionCallback#setPosition(int offsetWidth, int offsetHeight)
   */
  public void setPopupPositionAndShow(PositionCallback callback) {
    setVisible(false);
    show();
    callback.setPosition(getOffsetWidth(), getOffsetHeight());
    setVisible(true);
  }

  public void setTitle(String title) {
    Element containerElement = getContainerElement();
    if (title == null || title.length() == 0) {
      DOM.removeElementAttribute(containerElement, "title");
    } else {
      DOM.setElementAttribute(containerElement, "title", title);
    }
  }

  /**
   * Sets whether this object is visible.
   * 
   * @param visible <code>true</code> to show the object, <code>false</code>
   *          to hide it
   */
  public void setVisible(boolean visible) {
    // We use visibility here instead of UIObject's default of display
    // Because the panel is absolutely positioned, this will not create
    // "holes" in displayed contents and it allows normal layout passes
    // to occur so the size of the PopupPanel can be reliably determined.
    DOM.setStyleAttribute(getElement(), "visibility", visible ? "visible"
        : "hidden");

    // If the PopupImpl creates an iframe shim, it's also necessary to hide it
    // as well.
    impl.setVisible(getElement(), visible);
  }

  public void setWidget(Widget w) {
    super.setWidget(w);
    maybeUpdateSize();
  }

  /**
   * Sets the width of the panel's child widget. If the panel's child widget
   * has not been set, the width passed in will be cached and used to set
   * the width immediately after the child widget is set.
   *
   * @param width the object's new width, in CSS units (e.g. "10px", "1em")
   */
  public void setWidth(String width) {
    desiredWidth = width;
    maybeUpdateSize();
    // If the user cleared the size, revert to not trying to control children.
    if (width.length() == 0) {
      desiredWidth = null;
    }
  }

  /**
   * Shows the popup. It must have a child widget before this method is called.
   */
  public void show() {
    if (showing) {
      return;
    }
    showing = true;
    DOM.addEventPreview(this);

    // Set the position attribute, and then attach to the DOM. Otherwise,
    // the PopupPanel will appear to 'jump' from its static/relative position
    // to its absolute position (issue #1231).
    DOM.setStyleAttribute(getElement(), "position", "absolute");
    if (topPosition != -1) {
      setPopupPosition(leftPosition, topPosition);
    }
    RootPanel.get().add(this);

    impl.onShow(getElement());
  }

  protected Element getContainerElement() {
    return impl.getContainerElement(getElement());
  }

  protected Element getStyleElement() {
    return impl.getContainerElement(getElement());
  }

  /**
   * This method is called when a widget is detached from the browser's
   * document. To receive notification before the PopupPanel is removed from the
   * document, override the {@link Widget#onUnload()} method instead.
   */
  protected void onDetach() {
    DOM.removeEventPreview(this);
    super.onDetach();
  }

  /**
   * Remove focus from an Element.
   * 
   * @param elt The Element on which <code>blur()</code> will be invoked
   */
  private native void blur(Element elt) /*-{
    if (elt.blur) {
      elt.blur();
    }
  }-*/;

  private void hide(boolean autoClosed) {
    if (!showing) {
      return;
    }
    showing = false;

    RootPanel.get().remove(this);
    impl.onHide(getElement());
    if (popupListeners != null) {
      popupListeners.firePopupClosed(this, autoClosed);
    }
  }

  /**
   * We control size by setting our child widget's size. However, if we don't
   * currently have a child, we record the size the user wanted so that when we
   * do get a child, we can set it correctly. Until size is explicitly cleared,
   * any child put into the popup will be given that size.
   */
  private void maybeUpdateSize() {
    Widget w = getWidget();
    if (w != null) {
      if (desiredHeight != null) {
        w.setHeight(desiredHeight);
      }
      if (desiredWidth != null) {
        w.setWidth(desiredWidth);
      }
    }
  }
}
