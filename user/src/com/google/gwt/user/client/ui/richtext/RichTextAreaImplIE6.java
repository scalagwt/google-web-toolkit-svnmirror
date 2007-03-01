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
 * IE6-specific implementation of rich-text editing.
 */
class RichTextAreaImplIE6 extends RichTextAreaImplStandard {

  native int getAbsoluteLeft(Element elem) /*-{
    // Adapted from DOMImplIE6.
    var doc = elem.ownerDocument;
    // Standard mode used documentElement.scrollLeft. Quirks mode uses 
    // document.body.scrollLeft. So we take the max of the two.  
    var scrollLeft = doc.documentElement.scrollLeft;
    if(scrollLeft == 0){
      scrollLeft = doc.body.scrollLeft
    }
    
    // Offset needed as IE starts the window's upper left at
    // 2,2 rather than 0,0.
    return (elem.getBoundingClientRect().left + scrollLeft) - 2;
  }-*/;

  native int getAbsoluteTop(Element elem) /*-{
    // Adapted from DOMImplIE6.
    var doc = elem.ownerDocument;
    // Standard mode used documentElement.scrollTop. Quirks mode uses 
    // document.body.scrollTop. So we take the max of the two.
    var scrollTop = doc.documentElement.scrollTop;
    if(scrollTop == 0){
      scrollTop = doc.body.scrollTop
    } 
    
    // Offset needed as IE starts the window's upper left as 2,2 
    // rather than 0,0.
    return (elem.getBoundingClientRect().top +  scrollTop) - 2;
   }-*/;

  native Element getNodeAtCursor(Element elem) /*-{
    return elem.contentWindow.document.selection.createRange().parentElement();
  }-*/;
  
  native String getSelectedHTML(Element elem) /*-{
    return elem.contentWindow.document.selection.createRange().htmlText;
  }-*/;

  native String getText(Element element) /*-{
    return element.contentWindow.document.body.innerText;
  }-*/;

  native void hookEvents(Element elem, ForeignDOMHost host, String cssURL) /*-{
    var w = elem.contentWindow;
    w.document.onreadystatechange =
        function() { 
      if (w.document.body == null) {
        return;
      }
      if(w.document.readyState == "complete") {
        var doc = w.document;
        doc.designMode = "On";
        doc.open();
        doc.write('<html><head>');
        if(cssURL){
            // must be done after the document has been opened
            doc.write('<link type="text/css" rel="stylesheet" href=' + cssURL + '/>');
        }
        doc.write('</head><body>');
        doc.write('</body></html>');
        doc.close();
        w.setTimeout(function() {
          var callOnRichTextEvent = function() {
            if(w.document.readyState != "complete") {
              return;
            }
            host.@com.google.gwt.user.client.ui.richtext.ForeignDOMHost::onForeignDOMEvent(Lcom/google/gwt/user/client/Element;Lcom/google/gwt/user/client/Event;)(elem, w.event);
          };

          // This is needed to ensure that IE does not throw away the selection each time
          // it loses focus.
          w.document.body.onbeforeactivate = function(){
            if (!elem.savedSelection) {
              return;
            }
            try {
            var r = w.document.selection.createRange();
            r.moveToBookmark(elem.savedSelection);
            r.select();
            } catch (e) {}
          };
          w.document.body.onbeforedeactivate = function(){
            elem.savedSelection=w.document.selection.createRange().getBookmark();
          };
          w.document.body.contentEditable = true;
          w.document.body.onblur        = 
          w.document.body.onclick       =
          w.document.body.onmousedown   =
          w.document.body.onmouseup     =
          w.document.body.onmousemove   =
          w.document.body.onkeydown     =
          w.document.body.onkeypress    =
          w.document.body.onkeyup       =
          w.document.body.onfocus       =
          w.document.body.ondblclick    = callOnRichTextEvent;
          w.editable = true;
        }, 1);
      } else {
        return;
      }
    }
  }-*/;

  native void unhookEvents(Element elem, ForeignDOMHost host) /*-{
  return;
    var body = elem.contentWindow.document.body;
    body.onclick       =
    body.onmousedown   =
    body.onmouseup     =
    body.onmousemove   =
    body.onkeydown     =
    body.onkeypress    =
    body.onkeyup       =
    body.onfocus       =
    body.onblur        =
    body.ondblclick    = null;
  }-*/;
  
}
