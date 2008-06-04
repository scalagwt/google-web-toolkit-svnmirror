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
package com.google.gwt.user.client.ui.impl;

import com.google.gwt.user.client.Element;

/**
 * Internet Explorer 6 implementation of
 * {@link com.google.gwt.user.client.ui.impl.PopupImpl}.
 */
public class PopupImplIE6 extends PopupImpl {

  @Override
  public native void onHide(Element popup) /*-{
    var frame = popup.__frame;
    frame.parentElement.removeChild(frame);
    popup.__frame = null;
    frame.__popup = null;
  }-*/;

  @Override
  public native void onShow(Element popup) /*-{
    // TODO: make this more Java and less JSNI?
    var frame = $doc.createElement('iframe');

    // Setting a src prevents mixed-content warnings.
    // http://weblogs.asp.net/bleroy/archive/2005/08/09/how-to-put-a-div-over-a-select-in-ie.aspx
    frame.src = "javascript:''";

    frame.scrolling = 'no';
    frame.frameBorder = 0;

    popup.__frame = frame;
    frame.__popup = popup;

    // Make the frame shadow the popup
    var style = frame.style;
    style.position = 'absolute';

    // Don't get in the way of transparency effects
    style.filter = 'alpha(opacity=0)';

    // Visibility of frame should match visiblity of popup element.
    style.visibility = popup.style.visibility;
  
    // Issue 2443: remove styles that affect the size of the iframe
    style.border = 0;
    style.padding = 0;
    style.margin = 0;

    // takes effect immediately
    style.left = popup.offsetLeft;
    style.top = popup.offsetTop;
    style.width = popup.offsetWidth;
    style.height = popup.offsetHeight;
    style.zIndex = popup.style.zIndex;

    // updates position and dimensions as popup is moved & resized
    style.setExpression('left', 'this.__popup.offsetLeft');
    style.setExpression('top', 'this.__popup.offsetTop');
    style.setExpression('width', 'this.__popup.offsetWidth');
    style.setExpression('height', 'this.__popup.offsetHeight');
    style.setExpression('zIndex', 'this.__popup.style.zIndex');
    popup.parentElement.insertBefore(frame, popup);
  }-*/;
  
  @Override
  public native void setVisible(Element popup, boolean visible) /*-{
    if (popup.__frame) {
      popup.__frame.style.visibility = visible ? 'visible' : 'hidden';
    }
  }-*/;
}
