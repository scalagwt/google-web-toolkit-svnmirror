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
package com.google.gwt.dom.client;


/**
 * Mozilla implementation of StandardBrowser.
 */
class DOMImplMozilla extends DOMImplStandard {

  @Override
  public native void buttonClick(ButtonElement button) /*-{
    var doc = button.ownerDocument;
    if (doc != null) {
      var evt = doc.createEvent('MouseEvents');
      evt.initMouseEvent('click', true, true, null, 0, 0,
        0, 0, 0, false, false, false, false, 0, null);
      button.dispatchEvent(evt);
    }
  }-*/;

  @Override
  public native int eventGetMouseWheelVelocityY(NativeEvent evt) /*-{
    return evt.detail || 0;
  }-*/;

  @Override
  public int getAbsoluteLeft(Element elem) {
    return getAbsoluteLeftImpl(elem.getOwnerDocument().getViewportElement(),
        elem);
  }

  @Override
  public int getAbsoluteTop(Element elem) {
    return getAbsoluteTopImpl(elem.getOwnerDocument().getViewportElement(),
        elem);
  }

  @Override
  public native int getBodyOffsetLeft(Document doc) /*-{
    var style = $wnd.getComputedStyle(doc.documentElement, '');
    return parseInt(style.marginLeft) + parseInt(style.borderLeftWidth);
  }-*/;

  @Override
  public native int getBodyOffsetTop(Document doc) /*-{
    var style = $wnd.getComputedStyle(doc.documentElement, '');
    return parseInt(style.marginTop) + parseInt(style.borderTopWidth);
  }-*/;

  @Override
  public native String getInnerText(Element elem) /*-{
    return elem.textContent;
  }-*/;

  @Override
  public int getScrollLeft(Element elem) {
    if (!isGecko19() && isRTL(elem)) {
      return super.getScrollLeft(elem)
          - (elem.getScrollWidth() - elem.getClientWidth());
    }
    return super.getScrollLeft(elem);
  }

  @Override
  public native boolean isOrHasChild(Element parent, Element child) /*-{
    // For more information about compareDocumentPosition, see:
    // http://www.quirksmode.org/blog/archives/2006/01/contains_for_mo.html
    return (parent === child) || !!(parent.compareDocumentPosition(child) & 16);
  }-*/;

  @Override
  public native void setInnerText(Element elem, String text) /*-{
    elem.textContent = text || '';
  }-*/;

  @Override
  public void setScrollLeft(Element elem, int left) {
    if (!isGecko19() && isRTL(elem)) {
      left += elem.getScrollWidth() - elem.getClientWidth();
    }
    super.setScrollLeft(elem, left);
  }

  @Override
  public native String toString(Element elem) /*-{
    // Basic idea is to use the innerHTML property by copying the node into a
    // div and getting the innerHTML
    var doc = elem.ownerDocument;
    var temp = elem.cloneNode(true);
    var tempDiv = doc.createElement("DIV");
    tempDiv.appendChild(temp);
    outer = tempDiv.innerHTML;
    temp.innerHTML = "";
    return outer;
  }-*/;

  private native int getAbsoluteLeftImpl(Element viewport, Element elem) /*-{
    // Firefox 3 is actively throwing errors when getBoxObjectFor() is called,
    // so we use getBoundingClientRect() whenever possible (but it's not
    // supported on older versions). If changing this code, make sure to check
    // the museum entry for issue 1932.
    // (x) | 0 is used to coerce the value to an integer
    if (Element.prototype.getBoundingClientRect) {
      return (elem.getBoundingClientRect().left + viewport.scrollLeft) | 0;
    } else {
      // We cannot use DOMImpl here because offsetLeft/Top return erroneous
      // values when overflow is not visible.  We have to difference screenX
      // here due to a change in getBoxObjectFor which causes inconsistencies
      // on whether the calculations are inside or outside of the element's
      // border.
      // If the element is in a scrollable div, getBoxObjectFor(elem) can return
      // a value that varies by 1 pixel.
      var doc = elem.ownerDocument;
      return doc.getBoxObjectFor(elem).screenX -
        doc.getBoxObjectFor(doc.documentElement).screenX;
    }
  }-*/;

  private native int getAbsoluteTopImpl(Element viewport, Element elem) /*-{
    // Firefox 3 is actively throwing errors when getBoxObjectFor() is called,
    // so we use getBoundingClientRect() whenever possible (but it's not
    // supported on older versions). If changing this code, make sure to check
    // the museum entry for issue 1932.
    // (x) | 0 is used to coerce the value to an integer
    if (Element.prototype.getBoundingClientRect) {
      return (elem.getBoundingClientRect().top + viewport.scrollTop) | 0;
    } else {
      // We cannot use DOMImpl here because offsetLeft/Top return erroneous
      // values when overflow is not visible.  We have to difference screenX
      // here due to a change in getBoxObjectFor which causes inconsistencies
      // on whether the calculations are inside or outside of the element's
      // border.
      var doc = elem.ownerDocument;
      return doc.getBoxObjectFor(elem).screenY -
        doc.getBoxObjectFor(doc.documentElement).screenY;
    }
  }-*/;

  private native boolean isGecko19() /*-{
    var result = /rv:([0-9]+)\.([0-9]+)/.exec(navigator.userAgent.toLowerCase());
    if (result && result.length == 3) {
      var version = (parseInt(result[1]) * 1000) + parseInt(result[2]);
      if (version >= 1009) {
        return true;
      }
    }
    return false;
  }-*/;

  private native boolean isRTL(Element elem) /*-{
    var style = elem.ownerDocument.defaultView.getComputedStyle(elem, null);
    return style.direction == 'rtl';
  }-*/;
}
