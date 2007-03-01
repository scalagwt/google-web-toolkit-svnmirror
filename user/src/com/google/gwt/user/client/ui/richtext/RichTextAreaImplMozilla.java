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

import com.google.gwt.user.client.Element;

/**
 * Mozilla-specific implementation of rich-text editing.
 */
class RichTextAreaImplMozilla extends RichTextAreaImplStandard {

  native void initElement(Element elem) /*-{
    // Mozilla doesn't seem to like setting designMode until slightly _after_
    // the iframe becomes attached to the DOM. Any non-zero timeout will do
    // just fine.
    window.setTimeout(function() {
      elem.contentWindow.document.designMode = 'On';
    }, 1);
  }-*/;

  void setBackColor(Element elem, String color) {
    execCommand(elem, "HiliteColor", color);
  }
}
