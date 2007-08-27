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

/**
 * Opera implementation of {@link com.google.gwt.user.client.impl.DOMImpl}.
 */
public class DOMImplOpera extends DOMImplStandard {

  public native int eventGetButton(Event evt) /*-{
    // Opera and IE disagree on what the button codes for left button should be.
    // Translating to match IE standard.
    var button = evt.button;
    if(button == 0){
      return 1;
    } else {
      return button || -1;
    }
  }-*/;

  public native int eventGetMouseWheelVelocityY(Event evt) /*-{
    return evt.detail * 4 || -1;
  }-*/;

  public native int getAbsoluteLeft(Element elem) /*-{
    var left = 0;
    var curr = elem.parentNode;
    // This intentionally excludes body
    while (curr != $doc.body) {

      // see https://bugs.opera.com/show_bug.cgi?id=249965
      // The net effect is that TR and TBODY elemnts report the scroll offsets
      // of the BODY and HTML elements instead of 0.
      if (curr.tagName != 'TR' && curr.tagName != 'TBODY') {
        left -= curr.scrollLeft;
      }
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

    // This intentionally excludes body
    var curr = elem.parentNode;
    while (curr != $doc.body) {
      // see getAbsoluteLeft()
      if (curr.tagName != 'TR' && curr.tagName != 'TBODY') {
        top -= curr.scrollTop;
      }
      curr = curr.parentNode;
    }

    while (elem) {
      top += elem.offsetTop;
      elem = elem.offsetParent;
    }
    return top;
  }-*/;
}
