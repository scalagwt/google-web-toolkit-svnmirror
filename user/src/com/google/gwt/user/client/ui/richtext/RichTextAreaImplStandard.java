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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Standard implementation of rich-text editing.
 */
class RichTextAreaImplStandard extends RichTextAreaImpl {

  /**
   * The constant tag used to mark highlighted areas.
   */
  static final String HIGHLIGHT_ID = "highlightid";

  private static final int NODE_TYPE_TEXT = 3;

  /**
   * Prevent default actions intended for that event from happening.
   * 
   * @param event the event to be prevented.
   */
  static void eventPreventDefault(Event event) {
    DOM.eventPreventDefault(event);
    DOMUtil.markEventPrevented(event);
  }

  public Highlight createHighlight(Element elem, RichTextArea rich,
      Object toBeHighlighted, HighlightCategory category, String id) {
    Element highlightElement = DOMUtil.createElementInSameDocument("span",
        getBody(elem));
    DOMUtil.setAttribute(highlightElement,
        RichTextAreaImplStandard.HIGHLIGHT_ID, id);
    DOM.setInnerHTML(highlightElement, toBeHighlighted.toString());
    Highlight highlight = new Highlight(rich, toBeHighlighted, category,
        highlightElement);
    return highlight;
  }

  public String disownHighlight(Highlight highlight) {
    Element elem = highlight.getElement();
    String highlightId = getHighlightId(elem);
    if (highlightId == null) {
      return highlightId;
    }
    if (DOMUtil.getParent(elem) != null) {
      // Highlight does not know how it was wrapped.
      DOMUtil.unwrapNode(elem);
    }
    return highlightId;
  }

  public String getHighlightId(Element possibleHighlight) {
    return DOMUtil.getAttribute(possibleHighlight, HIGHLIGHT_ID);
  }

  Iterator addHighlights(Element elem, RichTextArea rich, List words,
      HighlightCategory category) {
    return addHighlights(rich, elem, words, category, null, getBody(elem));
  }

  Iterator addHighlights(RichTextArea rich, Element elem, List words,
      HighlightCategory category, Element nextSibling, Element toBeProcessed) {
    List out = new ArrayList();
    if (words == null || words.size() == 0) {
      return out.iterator();
    }
    int nodeType = DOMUtil.getNodeType(toBeProcessed);
    if (nodeType == NODE_TYPE_TEXT) {
      // Divide the text node up into highlights and more text.
      Element parent = DOMUtil.getParent(toBeProcessed);
      String text = getInnerHTMLOfTextNode(elem, toBeProcessed);
      List toBeAdded = spanify(rich, words, text, category);
      for (Iterator iter = toBeAdded.iterator(); iter.hasNext();) {
        Object toAdd = iter.next();
        if (toAdd instanceof Highlight) {
          // We highlighted this item in spanify.
          Highlight highlight = (Highlight) toAdd;
          out.add(toAdd);
          DOMUtil.insertChildBefore(parent, highlight.getElement(), nextSibling);
        } else if (toAdd instanceof String) {
          // We did not highlight this item in spanify.
          String newText = (String) toAdd;
          Element childToInsert = createTextNode(getBody(elem), newText);
          DOMUtil.insertChildBefore(parent, childToInsert, nextSibling);
        }
      }
    } else {
      // Walk the DOM tree looking for text nodes to spanify.
      Element child = DOMUtil.getFirstChildRaw(toBeProcessed);
      while (child != null) {
        Element newNextSibling = DOMUtil.getNextSibling(child);
        Iterator iter = addHighlights(rich, elem, words, category,
            newNextSibling, child);
        while (iter.hasNext()) {
          out.add(iter.next());
        }
        child = newNextSibling;
      }
    }
    return out.iterator();
  }

  native Element createElement() /*-{
    return $doc.createElement('iframe');
  }-*/;

  void createLink(Element elem, String url) {
    execCommand(elem, "CreateLink", url);
  }

  /**
   * Fires {@link HighlightClickEvent} on all relevant handlers. This ensures
   * the highlight first gets the event, then the category get the event.
   */
  void doRichTextClick(RichTextArea rich, Element elem, Event domEvent) {
    Element clicked = DOM.eventGetTarget(domEvent);
    String highlightId = rich.getHighlightId(clicked);
    if (highlightId != null) {
      Highlight highlight = rich.getHighlight(highlightId);
      HighlightClickEvent event = new HighlightClickEvent(highlight, domEvent);
      highlight.fireClickEvent(event);
    }
    // Listeners are handled in Focus widget.
  }

  /**
   * Fires {@link HighlightKeyboardEvent} on all relevant handlers. This ensures
   * the highlight first gets the event, then the category get the event.
   */
  void doRichTextKeyPress(RichTextArea rich, Element elem, Event domEvent) {
    Element typedInto = getNodeAtCursor(elem);
    String highlightId = rich.getHighlightId(typedInto);
    if (highlightId != null) {
      Highlight highlight = rich.getHighlight(highlightId);
      HighlightKeyboardEvent event = new HighlightKeyboardEvent(highlight,
          domEvent);
      highlight.fireKeyboardEvent(event);
    }
    // Listeners are handled in Focus widget.
  }

  /**
   * Fires {@link HighlightMouseEvent} on all relevant handlers. This ensures
   * the highlight first gets the event, then the category get the event.
   */
  void doRichTextMouseEvent(RichTextArea rich, Element elem, Event domEvent) {
    Element moused = DOM.eventGetTarget(domEvent);
    String highlightId = rich.getHighlightId(moused);
    if (highlightId != null) {
      Highlight highlight = rich.getHighlight(highlightId);
      HighlightMouseEvent event = new HighlightMouseEvent(highlight, domEvent);
      highlight.fireMouseEvent(event);
    }
    // Listeners are not handled in Focus widget.
    if (!DOMUtil.eventWasPrevented(domEvent)) {
      rich.fireMouseListeners(domEvent);
    }
  }

  void execCommand(Element elem, String cmd, String param) {
    if (isRichEditingActive(elem)) {
      // When executing a command, focus the iframe first, since some commands
      // don't take properly when it's not focused.
      setFocus(elem, true);
      execCommandAssumingFocus(elem, cmd, param);
    }
  }

  void formatBlock(Element elem, String format) {
    execCommand(elem, "FormatBlock", format);
  }

  native int getAbsoluteLeft(Element elem) /*-{
    var doc = elem.ownerDocument;
    var left = doc.getBoxObjectFor(elem).x;
    var parent = elem;

    while (parent) {
      // Sometimes get NAN.
      if (parent.scrollLeft > 0) {
        left = left -  parent.scrollLeft;
      }
      parent = parent.parentNode;
    }
 
  // Must cover both Standard and Quirks mode. 
    return left + doc.body.scrollLeft + doc.documentElement.scrollLeft;
  }-*/;

  int getAbsoluteLeft(Highlight highlight, Element elem) {
    return getAbsoluteLeft(highlight.getElement()) + DOM.getAbsoluteLeft(elem);
  }

  native int getAbsoluteTop(Element elem) /*-{
    var doc = elem.ownerDocument;
    var top = doc.getBoxObjectFor(elem).y;
    var parent = elem;
    while (parent) {
      // Sometimes get NAN.
      if (parent.scrollTop > 0) {
        top -= parent.scrollTop;
      }
      parent = parent.parentNode;
    }
   
    // Must cover both Standard and Quirks mode.
    return top + doc.body.scrollTop + doc.documentElement.scrollTop;
  }-*/;

  int getAbsoluteTop(Highlight highlight, Element elem) {
    return getAbsoluteTop(highlight.getElement()) + DOM.getAbsoluteTop(elem);
  }

  String getBackColor(Element elem) {
    return queryCommandValue(elem, "BackColor");
  }

  native Element getBody(Element elem) /*-{
    return elem.contentWindow.document.body;
  }-*/;

  String getForeColor(Element elem) {
    return queryCommandValue(elem, "ForeColor");
  }

  native String getHTML(Element element) /*-{
    return element.contentWindow.document.body.innerHTML;
  }-*/;

  native Element getNodeAtCursor(Element elem) /*-{
    var node = elem.contentWindow.getSelection().focusNode;
    if (node.nodeType != 1) {
      node = node.parentNode;
    }
    return node;
  }-*/;

  native String getSelectedHTML(Element elem) /*-{
    var selection = elem.getSelection();
    var range = elem.createRange();
    return range.htmlText;
  }-*/;

  native String getText(Element element) /*-{
    return element.contentWindow.document.body.textContent;
  }-*/;

  native void hookEvents(Element elem, ForeignDOMHost host, String cssURL) /*-{
    elem.onload = function() {
      var wnd = elem.contentWindow;

      if (wnd.__richTextEventHandler)
        return;
      var handler = wnd.__richTextEventHandler = function(event) {
        host.@com.google.gwt.user.client.ui.richtext.ForeignDOMHost::onForeignDOMEvent(Lcom/google/gwt/user/client/Element;Lcom/google/gwt/user/client/Event;)(elem, event);
      };
  
      var doc = wnd.document; 
      doc.designMode = 'On';
      doc.open();
      doc.write('<html><head>');
      if(cssURL){
          // must be done after the document has been opened
          doc.write('<link type="text/css" rel="stylesheet" href='+cssURL+'/>');
      }
      doc.write('</head><body>');
      doc.write('</body></html>');
      doc.close();
      wnd.addEventListener('click', handler, true);
      wnd.addEventListener('dblclick', handler, true);
      wnd.addEventListener('focus', handler, true);
      wnd.addEventListener('blur', handler, true);
      wnd.addEventListener('mousedown', handler, true);
      wnd.addEventListener('mouseup', handler, true);
      wnd.addEventListener('mousemove', handler, true);
      wnd.addEventListener('keydown', handler, true);
      wnd.addEventListener('keyup', handler, true);
      wnd.addEventListener('keypress', handler, true);
    };
  }-*/;

  native void initElement(Element elem) /*-{
    // Mozilla doesn't seem to like setting designMode until slightly _after_
    // the iframe becomes attached to the DOM. Any non-zero timeout will do
    // just fine.
    window.setTimeout(function() {
      elem.contentWindow.document.designMode = 'On';
    }, 1);
  }-*/;

  void insertHorizontalRule(Element elem) {
    execCommand(elem, "InsertHorizontalRule", null);
  }

  void insertOrderedList(Element elem) {
    execCommand(elem, "InsertOrderedList", null);
  }

  void insertUnorderedList(Element elem) {
    execCommand(elem, "InsertUnorderedList", null);
  }

  native void installStyleSheet(Element elem, String url) /*-{
    var doc = elem.contentWindow.document;
    var head = doc.head;
    if (!doc.head) {
      head = doc.createElement("head");
    }
    var linkCSS = doc.createElement("link");
    linkCSS.rel = "stylesheet";
    linkCSS.href = url;
    head.appendChild(linkCSS);
  }-*/;

  boolean isBold(Element elem) {
    return queryCommandState(elem, "Bold");
  }

  boolean isItalic(Element elem) {
    return queryCommandState(elem, "Italic");
  }

  boolean isJustifiedCenter(Element elem) {
    return queryCommandState(elem, "JustifyCenter");
  }

  boolean isJustifiedLeft(Element elem) {
    return queryCommandState(elem, "JustifyLeft");
  }

  boolean isJustifiedRight(Element elem) {
    return queryCommandState(elem, "JustifyRight");
  }

  native boolean isLoaded(Element element) /*-{
    return element.contentWindow.document.designMode == "On";
  }-*/;

  native boolean isRichEditingActive(Element e) /*-{
    return ((e.contentWindow.document.designMode).toUpperCase()) == 'ON';
  }-*/;

  boolean isRichEditingSupported() {
    return true;
  }

  boolean isUnderlined(Element elem) {
    return queryCommandState(elem, "Underline");
  }

  void justifyCenter(Element elem) {
    execCommand(elem, "JustifyCenter", null);
  }

  void justifyLeft(Element elem) {
    execCommand(elem, "JustifyLeft", null);
  }

  void justifyRight(Element elem) {
    execCommand(elem, "JustifyRight", null);
  }

  void leftIndent(Element elem) {
    execCommand(elem, "Outdent", null);
  }

  boolean queryCommandState(Element elem, String cmd) {
    if (isRichEditingActive(elem)) {
      // When executing a command, focus the iframe first, since some commands
      // don't take properly when it's not focused.
      setFocus(elem, true);
      return queryCommandStateAssumingFocus(elem, cmd);
    } else {
      return false;
    }
  }

  String queryCommandValue(Element elem, String cmd) {
    // When executing a command, focus the iframe first, since some commands
    // don't take properly when it's not focused.
    setFocus(elem, true);
    return queryCommandValueAssumingFocus(elem, cmd);
  }

  void rightIndent(Element elem) {
    execCommand(elem, "Indent", null);
  }

  void setBackColor(Element elem, String color) {
    execCommand(elem, "BackColor", color);
  }

  native void setFocus(Element elem, boolean focused) /*-{
    if (focused)
      elem.contentWindow.focus();
    else
      elem.contentWindow.blur();
  }-*/;

  void setFontName(Element elem, String name) {
    execCommand(elem, "FontName", name);
  }

  void setFontSize(Element elem, String size) {
    execCommand(elem, "FontSize", size);
  }

  void setForeColor(Element elem, String color) {
    execCommand(elem, "ForeColor", color);
  }

  native void setHTML(Element elem, String html) /*-{
    elem.contentWindow.document.body.innerHTML = html;
  }-*/;

  native void setText(Element elem, String text) /*-{
    elem.contentWindow.document.body.textContent = text;
  }-*/;

  void toggleBold(Element elem) {
    execCommand(elem, "Bold", "false");
  }

  void toggleItalic(Element elem) {
    execCommand(elem, "Italic", "false");
  }

  void toggleUnderline(Element elem) {
    execCommand(elem, "Underline", "False");
  }

  native void unhookEvents(Element elem, ForeignDOMHost host) /*-{
    var wnd = elem.contentWindow;
    var handler = wnd.__richTextEventHandler;

    wnd.removeEventListener('click', handler, true);
    wnd.removeEventListener('dblclick', handler, true);
    wnd.removeEventListener('mousedown', handler, true);
    wnd.removeEventListener('mouseup', handler, true);
    wnd.removeEventListener('mousemove', handler, true);
    wnd.removeEventListener('keydown', handler, true);
    wnd.removeEventListener('keyup', handler, true);
    wnd.removeEventListener('keypress', handler, true);
  }-*/;

  private Element createTextNode(Element body, String text) {
    // prevent spaces from being lost in the shuffle.
    if (text.startsWith(" ")) {
      text = "&nbsp;" + text.substring(1);
    }
    Element div = DOMUtil.createElementInSameDocument("div", body);
    DOM.setInnerHTML(div, text);
    return DOMUtil.getFirstChildRaw(div);
  }

  private native void execCommandAssumingFocus(Element elem, String cmd, String param) /*-{
    elem.contentWindow.document.execCommand(cmd, false, param);
  }-*/;

  /**
   * Gets the inner HTML of this text node.
   * 
   * @param elem the element pointing to the document to use to create the
   *          scratch node
   * @param toBeProcessed the text node to get the escaped html of
   * @return
   */
  private String getInnerHTMLOfTextNode(Element elem, Element toBeProcessed) {
    Element scratchParent = DOMUtil.createElementInSameDocument("div",
        getBody(elem));
    DOM.appendChild(scratchParent, toBeProcessed);
    String text = DOM.getInnerHTML(scratchParent);
    return text;
  }

  private native boolean queryCommandStateAssumingFocus(Element elem, String cmd) /*-{
      return !!elem.contentWindow.document.queryCommandState(cmd); 
  }-*/;

  private native String queryCommandValueAssumingFocus(Element elem, String cmd) /*-{
    return elem.contentWindow.document.queryCommandState(cmd);
  }-*/;

  private List spanify(RichTextArea rich, List words, String original,
      HighlightCategory category) {
    List accum = new ArrayList();
    accum.add(original);
    for (Iterator iter = words.iterator(); iter.hasNext();) {
      String word = (String) iter.next();
      accum = spanify(rich, word, category, accum);
    }
    return accum;
  }

  private List spanify(RichTextArea rich, String word, HighlightCategory cls,
      List accum) {
    List out = new ArrayList();
    for (Iterator iter = accum.iterator(); iter.hasNext();) {
      Object item = iter.next();
      if (item instanceof String) {
        out.addAll(spanify(rich, word, (String) item, cls));
      } else { // already highlighted
        out.add(item);
      }
    }
    return out;
  }

  private List spanify(RichTextArea rich, String word, String item,
      HighlightCategory cls) {
    int indexOf;
    int start = 0;
    int lastCut = 0;
    List out = new ArrayList();
    indexOf = 0;
    while (indexOf != -1) {
      indexOf = item.indexOf(word, start);
      if (indexOf == -1) {
        break;
      }
      boolean wordStart = (indexOf == 0)
          || (!Character.isLetterOrDigit(item.charAt(indexOf - 1)));
      int endOfMatch = indexOf + word.length();
      boolean wordEnd = (endOfMatch == item.length() || (!Character.isLetterOrDigit(item.charAt(endOfMatch))));
      if (wordStart && wordEnd) {
        out.add(item.substring(lastCut, indexOf));
        String highlightMe = item.substring(indexOf, endOfMatch);
        lastCut = endOfMatch;
        out.add(rich.createHighlight(highlightMe, cls));
      }
      start = endOfMatch;
    }
    out.add(item.substring(lastCut));
    return out;
  }
}
