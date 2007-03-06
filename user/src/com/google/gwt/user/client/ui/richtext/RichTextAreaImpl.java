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
 * Fall back implementation of rich-text editing for browsers that don't support
 * it. It simply creates a text area and reports that it doesn't support rich
 * editing.
 */
class RichTextAreaImpl {

  Iterator addHighlights(Element elem, RichTextArea rich, List words,
      HighlightCategory category) {
    // return an empty Iterator so usage is consistent
    return new ArrayList().iterator();
  }

  Element createElement() {
    return DOM.createTextArea();
  }

  /**
   * Creates a highlight.
   * 
   * @param element the element of the rich text, containing the body
   * @param rich the rich text to hold the highlight
   * @param toBeHighlighted the text to be highlighted
   * @param category the category to use for the highlight
   * @param id the id to give the highlight
   * @return the new highlight
   */
  Highlight createHighlight(Element element, RichTextArea rich,
      Object toBeHighlighted, HighlightCategory category, String id) {
    return null;
  }

  void createLink(Element elem, String url) {
  }

  /**
   * Disowns and removes a {@link Highlight} from this
   * {@link RichTextEditorImpl}'s element.
   * 
   * @param highlight the {@link Highlight} to be disowned
   * @return the id of the {@link Highlight} that was disowned
   */
  String disownHighlight(Highlight highlight) {
    return null;
  }

  void doRichTextClick(RichTextArea rich, Element elem, Event event) {
    // We do not fire the listeners as they are stored in the superclass.

    // In the case that we have real rich text editing, the subclass will fire
    // highlight events here.
  }

  void doRichTextKeyPress(RichTextArea rich, Element elem, Event event) {
    // We do not fire the listeners as they are stored in the superclass.

    // In the case that we have real rich text editing, the subclass will fire
    // highlight events here.
  }

  void doRichTextMouseEvent(RichTextArea rich, Element elem, Event event) {
    // Fires the events as they are not stored in the superclass.
    rich.fireMouseListeners(event);
    // In the case that we have real rich text editing, the subclass will fire
    // highlight events here.
  }

  void formatBlock(Element elem, String format) {
  }

  int getAbsoluteLeft(Highlight highlight, Element elem) {
    // No rich text means no iframe and hence no separate document offset.
    return DOM.getAbsoluteLeft(highlight.getElement());
  }

  int getAbsoluteTop(Highlight highlight, Element elem) {
    // No rich text means no iframe and hence no separate document offset.
    return DOM.getAbsoluteTop(highlight.getElement());
  }

  String getBackColor(Element element) {
    return null;
  }

  Element getBody(Element elem) {
    return elem;
  }

  String getForeColor(Element element) {
    return null;
  }

  /**
   * returns the highlight ID of this highlight, or null if the supplied element
   * is not a highlight.
   * 
   * @param possibleHighlight the element that might be a highlight
   * @return the id or null
   */
  String getHighlightId(Element possibleHighlight) {
    return null;
  }

  String getHTML(Element element) {
    return DOM.getAttribute(element, "value");
  }

  Element getNodeAtCursor(Element body) {
    return null;
  }

  String getSelectedHTML(Element elem) {
    return null;
  }

  String getText(Element element) {
    return DOM.getAttribute(element, "value");
  }

  void hookEvents(Element elem, ForeignDOMHost host, String cssURL) {
  }

  void insertHorizontalRule(Element elem) {
  }

  void insertOrderedList(Element elem) {
  }

  void insertUnorderedList(Element elem) {
  }

  void installStyleSheet(Element elem, String url) {
  }

  boolean isBold(Element element) {
    return false;
  }

  boolean isItalic(Element element) {
    return false;
  }

  boolean isJustifiedCenter(Element element) {
    return false;
  }

  boolean isJustifiedLeft(Element element) {
    return false;
  }

  boolean isJustifiedRight(Element element) {
    return false;
  }

  boolean isLoaded(Element element) {
    return true;
  }

  boolean isRichEditingSupported() {
    return false;
  }

  boolean isUnderlined(Element element) {
    return false;
  }

  void justifyCenter(Element elem) {
  }

  void justifyLeft(Element elem) {
  }

  void justifyRight(Element elem) {
  }

  void leftIndent(Element elem) {
  }

  void rightIndent(Element elem) {
  }

  void setBackColor(Element elem, String color) {
  }

  void setFocus(Element elem, boolean focused) {
  }

  void setFontName(Element elem, String name) {
  }

  void setFontSize(Element elem, String size) {
  }

  void setForeColor(Element elem, String color) {
  }

  void setHTML(Element element, String html) {
  }

  native void setText(Element elem, String text) /*-{
    elem.innerText = text;
  }-*/;

  void toggleBold(Element elem) {
  }

  void toggleItalic(Element elem) {
  }

  void toggleUnderline(Element elem) {
  }

  void unhookEvents(Element elem, ForeignDOMHost host) {
  }

  public void selectAll(Element element) {    
  }
}
