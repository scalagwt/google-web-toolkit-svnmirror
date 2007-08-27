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
package com.google.gwt.user.client.ui.impl;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.Element;

/**
 * Safari-specific implementation of {@link FocusImpl} that creates a completely
 * transparent hidden element, since Safari will not keyboard focus on an input
 * element that has zero width and height.
 */
public class FocusImplSafari extends FocusImplOld {
  
  public native void blur(Element elem) /*-{
    // Attempts to blur elements from within an event callback will generally
    // be unsuccessful, so we invoke blur() from outside of the callback.
    $wnd.setTimeout(function() {
      elem.firstChild.blur();
    }, 0);
  }-*/;

  public native void focus(Element elem) /*-{
    // Attempts to focus elements from within an event callback will generally
    // be unsuccessful, so we invoke focus() from outside of the callback.
    $wnd.setTimeout(function() {
      elem.firstChild.focus();
    }, 0);
  }-*/;

  protected native Element createHiddenInput() /*-{
    var input = $doc.createElement('input');
    input.type = 'text';
    input.style.opacity = 0;
    input.style.zIndex = -1;
    input.style.height = '1px';
    input.style.width = '1px';
    input.style.overflow = 'hidden';
    input.style.position = 'absolute';
    return input;
  }-*/;

  protected native JavaScriptObject createMouseHandler() /*-{
    return function() {
      // This function is called directly as an event handler, so 'this' is
      // set up by the browser to be the div on which the event is fired.
      var firstChild = this.firstChild;

      // Attempts to focus elements from within an event callback will generally
      // be unsuccessful, so we invoke focus() from outside of the callback.
      $wnd.setTimeout(function() {
        firstChild.focus();
      }, 0);
    }
  }-*/;

}
