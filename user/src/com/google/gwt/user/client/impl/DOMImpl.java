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
package com.google.gwt.user.client.impl;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;

/**
 * Native implementation associated with {@link com.google.gwt.user.client.DOM}.
 */
public abstract class DOMImpl {

  public native void appendChild(Element parent, Element child) /*-{
    parent.appendChild(child);
  }-*/;

  public abstract boolean compare(Element elem1, Element elem2);

  public native Element createElement(String tag) /*-{
    return $doc.createElement(tag);
  }-*/;

  public native Element createInputElement(String type) /*-{
    var e = $doc.createElement("INPUT");
    e.type = type;
    return e;
  }-*/;

  public abstract Element createInputRadioElement(String name);

  public Element createSelectElement(boolean multiple) {
    Element select = createElement("select");
    if (multiple) {
      setElementPropertyBoolean(select, "multiple", true);
    }
    return select;
  }

  public native void eventCancelBubble(Event evt, boolean cancel) /*-{
    evt.cancelBubble = cancel;
  }-*/;

  public native boolean eventGetAltKey(Event evt) /*-{
    return !!evt.altKey;
  }-*/;

  public native int eventGetButton(Event evt) /*-{
    return evt.button || -1;
  }-*/;

  public native int eventGetClientX(Event evt) /*-{
    return evt.clientX || -1;
  }-*/;

  public native int eventGetClientY(Event evt) /*-{
    return evt.clientY || -1;
  }-*/;

  public native boolean eventGetCtrlKey(Event evt) /*-{
    return !!evt.ctrlKey;
  }-*/;

  public native Element eventGetCurrentTarget(Event evt) /*-{
    return evt.currentTarget;
  }-*/;

  public abstract Element eventGetFromElement(Event evt);

  public native int eventGetKeyCode(Event evt) /*-{
    // 'which' gives the right key value, except when it doesn't -- in which
    // case, keyCode gives the right value on all browsers.
    // If all else fails, return an error code
    return evt.which || evt.keyCode || -1;
  }-*/;

  public native boolean eventGetMetaKey(Event evt) /*-{
    return !!evt.metaKey;
  }-*/;

  public abstract int eventGetMouseWheelVelocityY(Event evt);

  public native boolean eventGetRepeat(Event evt) /*-{
    return !!evt.repeat;
  }-*/;

  public native int eventGetScreenX(Event evt) /*-{
    return evt.screenX || -1;
  }-*/;

  public native int eventGetScreenY(Event evt) /*-{
    return evt.screenY || -1;
  }-*/;

  public native boolean eventGetShiftKey(Event evt) /*-{
    return !!evt.shiftKey;
  }-*/;

  public abstract Element eventGetTarget(Event evt);

  public abstract Element eventGetToElement(Event evt);

  public native String eventGetType(Event evt) /*-{
    return evt.type;
  }-*/;

  public native int eventGetTypeInt(Event evt) /*-{
    switch (evt.type) {
      case "blur": return 0x01000;
      case "change": return 0x00400;
      case "click": return 0x00001;
      case "dblclick": return 0x00002;
      case "focus": return 0x00800;
      case "keydown": return 0x00080;
      case "keypress": return 0x00100;
      case "keyup": return 0x00200;
      case "load": return 0x08000;
      case "losecapture": return 0x02000;
      case "mousedown": return 0x00004;
      case "mousemove": return 0x00040;
      case "mouseout": return 0x00020;
      case "mouseover": return 0x00010;
      case "mouseup": return 0x00008;
      case "scroll": return 0x04000;
      case "error": return 0x10000;
      case "mousewheel": return 0x20000;
      case "DOMMouseScroll": return 0x20000;
    }
  }-*/;

  public abstract void eventPreventDefault(Event evt);

  public native void eventSetKeyCode(Event evt, char key) /*-{
    evt.keyCode = key;
  }-*/;

  public abstract String eventToString(Event evt);

  public native int getAbsoluteLeft(Element elem) /*-{
    var left = 0;
    var curr = elem;
    // This intentionally excludes body which has a null offsetParent.    
    while (curr.offsetParent) {
      left -= curr.scrollLeft;
      curr = curr.parentNode;
    }
    while (elem) {
      left += elem.offsetLeft;
      elem = elem.offsetParent;
    }
    return left;
  }-*/;

  public native int getAbsoluteTop(Element elem) /*-{
    var top = 0;
    var curr = elem;
    // This intentionally excludes body which has a null offsetParent.    
    while (curr.offsetParent) {
      top -= curr.scrollTop;
      curr = curr.parentNode;
    }
    while (elem) {
      top += elem.offsetTop;
      elem = elem.offsetParent;
    }
    return top;
  }-*/;

  public abstract Element getChild(Element elem, int index);

  public abstract int getChildCount(Element elem);

  public abstract int getChildIndex(Element parent, Element child);

  public native String getElementAttribute(Element elem, String attr) /*-{
    var ret = elem.getAttribute(attr);
    return (ret == null) ? null : ret;
  }-*/;

  public native Element getElementById(String id) /*-{
    var elem = $doc.getElementById(id);
    return elem || null;
  }-*/;

  public native String getElementProperty(Element elem, String prop) /*-{
    var ret = elem[prop];
    return (ret == null) ? null : String(ret);
  }-*/;

  public native boolean getElementPropertyBoolean(Element elem, String prop) /*-{
    return !!elem[prop];
  }-*/;

  public native int getElementPropertyInt(Element elem, String prop) /*-{
    var i = parseInt(elem[prop]);
    if (!i) {
      return 0;
    }
    return i;
  }-*/;

  public native int getEventsSunk(Element elem) /*-{
    return elem.__eventBits || 0;
  }-*/;

  public abstract Element getFirstChild(Element elem);

  public native String getImgSrc(Element img) /*-{
    return img.src;
  }-*/;

  public native String getInnerHTML(Element elem) /*-{
    var ret = elem.innerHTML;
    return (ret == null) ? null : ret;
  }-*/;

  public native String getInnerText(Element node) /*-{
    // To mimic IE's 'innerText' property in the W3C DOM, we need to recursively
    // concatenate all child text nodes (depth first).
    var text = '', child = node.firstChild;
    while (child) {
      // 1 == Element node
      if (child.nodeType == 1) {
        text += this.@com.google.gwt.user.client.impl.DOMImpl::getInnerText(Lcom/google/gwt/user/client/Element;)(child);
      } else if (child.nodeValue) {
        text += child.nodeValue;
      }
      child = child.nextSibling;
    }
    return text;
  }-*/;

  public native int getIntStyleAttribute(Element elem, String attr) /*-{
    var i = parseInt(elem.style[attr]);
    if (!i) {
      return 0;
    }
    return i;
  }-*/;

  public abstract Element getNextSibling(Element elem);

  public abstract Element getParent(Element elem);

  public native String getStyleAttribute(Element elem, String attr) /*-{
    var ret = elem.style[attr];
    return (ret == null) ? null : ret;
  }-*/;

  public abstract void init();

  public native void insertBefore(Element parent, Element child, Element before) /*-{
    parent.insertBefore(child, before);
  }-*/;

  public abstract void insertChild(Element parent, Element child,
      int index);

  /**
   * @see com.google.gwt.user.client.DOM#insertListItem(Element, String, String, int)
   */
  public native void insertListItem(Element select, String item, String value,
      int index)/*-{
    var option = new Option(item, value);
    if (index == -1 || index > select.options.length - 1) {
      select.add(option, null);
    } else {
      select.add(option, select.options[index]);      
    }
  }-*/;

  public abstract boolean isOrHasChild(Element parent, Element child);

  public abstract void releaseCapture(Element elem);

  public native void removeChild(Element parent, Element child) /*-{
    parent.removeChild(child);
  }-*/;

  public native void removeElementAttribute(Element elem, String attr) /*-{
    elem.removeAttribute(attr);
  }-*/;

  public native void scrollIntoView(Element elem) /*-{
    var left = elem.offsetLeft, top = elem.offsetTop;
    var width = elem.offsetWidth, height = elem.offsetHeight;

    if (elem.parentNode != elem.offsetParent) {
      left -= elem.parentNode.offsetLeft;
      top -= elem.parentNode.offsetTop;
    }

    var cur = elem.parentNode;
    while (cur && (cur.nodeType == 1)) {
      // body tags are implicitly scrollable
      if ((cur.style.overflow == 'auto') || (cur.style.overflow == 'scroll') ||
          (cur.tagName == 'BODY')) {
      
        if (left < cur.scrollLeft) {
          cur.scrollLeft = left;
        }
        if (left + width > cur.scrollLeft + cur.clientWidth) {
          cur.scrollLeft = (left + width) - cur.clientWidth;
        }
        if (top < cur.scrollTop) {
          cur.scrollTop = top;
        }
        if (top + height > cur.scrollTop + cur.clientHeight) {
          cur.scrollTop = (top + height) - cur.clientHeight;
        }
      }

      var offsetLeft = cur.offsetLeft, offsetTop = cur.offsetTop;
      if (cur.parentNode != cur.offsetParent) {
        offsetLeft -= cur.parentNode.offsetLeft;
        offsetTop -= cur.parentNode.offsetTop;
      }

      left += offsetLeft - cur.scrollLeft;
      top += offsetTop - cur.scrollTop;
      cur = cur.parentNode;
    }
  }-*/;

  public abstract void setCapture(Element elem);

  public native void setElementAttribute(Element elem, String attr, String value) /*-{
    elem.setAttribute(attr, value);
  }-*/;

  public native void setElementProperty(Element elem, String prop, String value) /*-{
    elem[prop] = value;
  }-*/;

  public native void setElementPropertyBoolean(Element elem, String prop,
      boolean value) /*-{
    elem[prop] = value;
  }-*/;

  public native void setElementPropertyInt(Element elem, String prop, int value) /*-{
    elem[prop] = value;
  }-*/;

  public native void setEventListener(Element elem,
      EventListener listener) /*-{
    elem.__listener = listener;
  }-*/;

  public native void setImgSrc(Element img, String src) /*-{
    img.src = src;
  }-*/;

  public native void setInnerHTML(Element elem, String html) /*-{
    if (!html) {
      html = '';
    }
    elem.innerHTML = html;
  }-*/;

  public native void setInnerText(Element elem, String text) /*-{
    // Remove all children first.
    while (elem.firstChild) {
      elem.removeChild(elem.firstChild);
    }
    // Add a new text node.
    if (text != null) {
      elem.appendChild($doc.createTextNode(text));
    }
  }-*/;

  public native void setIntStyleAttribute(Element elem, String attr, int value) /*-{
    elem.style[attr] = value;
  }-*/;

  public native void setOptionText(Element select, String text, int index) /*-{
    // IE doesn't properly update the screen when you use
    // setAttribute("option", text), so we instead directly assign to the
    // 'option' property, which works correctly on all browsers.
    var option = select.options[index];
    option.text = text;
  }-*/;

  public native void setStyleAttribute(Element elem, String attr,
      String value) /*-{
    elem.style[attr] = value;
  }-*/;

  public abstract void sinkEvents(Element elem, int eventBits);

  public native String toString(Element elem) /*-{
    return elem.outerHTML;
  }-*/;

  /**
   * Gets the height of the browser window's client area excluding the
   * scroll bar.
   * 
   * @return the window's client height
   */
  public native int windowGetClientHeight() /*-{
    return $doc.body.clientHeight; 
  }-*/;

  /**
   * Gets the width of the browser window's client area excluding the
   * vertical scroll bar.
   * 
   * @return the window's client width
   */
  public native int windowGetClientWidth() /*-{
    return $doc.body.clientWidth; 
  }-*/;
}
