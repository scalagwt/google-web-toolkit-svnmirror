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
package com.google.gwt.user.client.ui.richtext;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;

/**
 * This class has DOM utility methods which should either be added to DOM, or
 * the DOM methods that are present should be modified to fix bugs. Many of them
 * are here because the DOM implementations assume the document in question will
 * be $doc rather than a sub-document.
 */
class DOMUtil {

  static native Element createElementInSameDocument(String type, Element e) /*-{
    return e.ownerDocument.createElement(type);
  }-*/;

  static void eventPreventDefault(Event e) {
    DOM.eventPreventDefault(e);
    markEventPrevented(e);
  }

  static native boolean eventWasPrevented(Event event) /*-{
    return event.eventWasPrevented != null;
  }-*/;

  static native String getAttribute(Element e, String tag) /*-{
    if (e.nodeType != 1) {
      return null;
    }
    var attr = e.getAttribute(tag);
    return attr==null?null:attr.toString();
  }-*/;

  static native Element getFirstChildRaw(Element elem) /*-{
    var child = elem.firstChild;
    return child ? child : null;
  }-*/;

  static native Element getNextSibling(Element elem) /*-{
    var sib = elem.nextSibling;
    return sib ? sib : null;
  }-*/;

  static native int getNodeType(Element elem) /*-{
    return elem.nodeType;
  }-*/;

  static native Element getParent(Element elem) /*-{
    var out = elem.parentNode;
    return out?out:null;
  }-*/;

  static native void insertChildBefore(Element parent, Element childToInsert,
      Element nextSibling) /*-{
    if (childToInsert == null) {
      return;     
    }
    if (nextSibling == null) {
      parent.appendChild(childToInsert);
    } else {
      parent.insertBefore(childToInsert, nextSibling);
    }
  }-*/;

  /**
   * Marks an event as prevented so we do not fire handlers on it. 
   * 
   * @param event the event to be prevented
   */
  static native void markEventPrevented(Event event) /*-{
   // Tested on IE hosted mode, web mode, and Firefox web mode.
    event.eventWasPrevented = true;
  }-*/;

  static native void setAttribute(Element element, String attr, String value) /*-{
    element.setAttribute(attr, value);
  }-*/;

  static void unwrapNode(Element element) {
    Element parent = DOMUtil.getParent(element);
    if (parent != null) {
      Element childToMove;
      while ((childToMove = getFirstChildRaw(element)) != null) {
        insertChildBefore(parent, childToMove, element);
      }
      DOM.removeChild(parent, element);
    }
  }

}
