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

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ChangeListenerCollection;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.HasHTML;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.KeyboardListenerCollection;
import com.google.gwt.user.client.ui.MouseListener;
import com.google.gwt.user.client.ui.MouseListenerCollection;
import com.google.gwt.user.client.ui.SourcesChangeEvents;
import com.google.gwt.user.client.ui.SourcesKeyboardEvents;
import com.google.gwt.user.client.ui.SourcesMouseEvents;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A rich text control that can be edited by the user. Supports highlighting and
 * DOM manipulation.
 */
public class RichTextArea extends FocusWidget implements HasHTML,
    ForeignDOMHost, SourcesMouseEvents, SourcesChangeEvents,
    SourcesKeyboardEvents, HasHighlights {

  /*
   * Implementation note: RichTextArea objects are currently iframes, if rich
   * text editing is supported. Preview events are forwarded out to the
   * containing DOM, so popups function.
   */

  /**
   * Block format enumeration. For example <code>H1</code>,<code>H2</code>,
   * and <code>PRE</code>.
   */
  public static class BlockFormat {

    /**
     * Represents an address.
     */
    public static final BlockFormat ADDRESS = new BlockFormat("ADDRESS");

    /**
     * Represents a primary headline.
     */
    public static final BlockFormat H1 = new BlockFormat("H1");

    /**
     * Represents a secondary headline.
     */
    public static final BlockFormat H2 = new BlockFormat("H2");

    /**
     * Represents a tertiary headline.
     */
    public static final BlockFormat H3 = new BlockFormat("H3");

    /**
     * Represents a quaternary headline.
     */
    public static final BlockFormat H4 = new BlockFormat("H4");

    /**
     * Represents a quinary headline.
     */
    public static final BlockFormat H5 = new BlockFormat("H5");

    /**
     * Represents a senary headline.
     */
    public static final BlockFormat H6 = new BlockFormat("H6");

    /**
     * Represents a paragraph.
     */
    public static final BlockFormat PARAGRAPH = new BlockFormat("P");

    /**
     * Represents a preformatted block.
     */
    public static final BlockFormat PRE = new BlockFormat("PRE");

    private String tag;

    private BlockFormat(String tag) {
      this.tag = tag;
    }

    public String toString() {
      return tag;
    }
  }

  /**
   * Font size enumeration. Represents the numbers one through seven with one
   * being small, and seven being huge.
   */
  public static class FontSize {

    /**
     * Represents a tiny font.
     */
    public static final FontSize SIZE_1 = new FontSize("1");

    /**
     * Represents a small font.
     */
    public static final FontSize SIZE_2 = new FontSize("2");

    /**
     * Represents a normal font.
     */
    public static final FontSize SIZE_3 = new FontSize("3");

    /**
     * Represents a medium-large font.
     */
    public static final FontSize SIZE_4 = new FontSize("4");

    /**
     * Represents a large font.
     */
    public static final FontSize SIZE_5 = new FontSize("5");

    /**
     * Represents a very large font.
     */
    public static final FontSize SIZE_6 = new FontSize("6");

    /**
     * Represents a huge font.
     */
    public static final FontSize SIZE_7 = new FontSize("7");

    private String tag;

    private FontSize(String tag) {
      this.tag = tag;
    }

    public String toString() {
      return "FontSize " + tag;
    }
  }

  /**
   * Justification enumeration. The three values are <code>left</code>,
   * <code>right</code>, <code>center</code>.
   */
  public static class Justification {

    /**
     * Center justification.
     */
    public static final Justification CENTER = new Justification("Center");

    /**
     * Left justification.
     */
    public static final Justification LEFT = new Justification("Left");

    /**
     * Right justification.
     */
    public static final Justification RIGHT = new Justification("Right");

    private String tag;

    private Justification(String tag) {
      this.tag = tag;
    }

    public String toString() {
      return "Justify " + tag;
    }
  }

  private static RichTextAreaImpl impl = (RichTextAreaImpl) GWT.create(RichTextAreaImpl.class);

  /**
   * Is rich text editing supported?
   * 
   * @return true if rich text editing supported
   */
  public static boolean isRichEditingSupported() {
    return impl.isRichEditingSupported();
  }

  private KeyboardListenerCollection keyboardListeners;

  private MouseListenerCollection mouseListeners;

  private ChangeListenerCollection changeListeners;

  /**
   * Map to associate highlightIds with highlight objects. If highlighted
   * regions are copied, this table will not be automatically updated, and the
   * clone is not given a new {@link Highlight} object.
   */
  private Map highlights = new HashMap();

  private String cssURL = null;

  /**
   * The next next serial number to use for highlights. The id of the highlight
   * created is this serial number converted to a string.
   */
  private int highlightSerialNumber;

  /**
   * Creates a new, blank {@link RichTextArea} object with no stylesheet.
   */
  public RichTextArea() {
    super(impl.createElement());
    setStyleName("gwt-RichTextArea");
  }

  /**
   * Creates a new, blank {@link RichTextArea} object with the supplied
   * stylesheet URL.
   * 
   * @param cssURL the url of the stylesheet
   */
  public RichTextArea(String cssURL) {
    this();
    this.cssURL = cssURL;
  }

  public void addChangeListener(ChangeListener listener) {
    if (changeListeners == null) {
      changeListeners = new ChangeListenerCollection();
    }
    changeListeners.add(listener);
  }

  public Iterator addHighlights(List items, HighlightCategory category) {
    return impl.addHighlights(getElement(), this, items, category);
  }

  public void addKeyboardListener(KeyboardListener listener) {
    if (keyboardListeners == null) {
      keyboardListeners = new KeyboardListenerCollection();
    }
    keyboardListeners.add(listener);
  }

  public void addMouseListener(MouseListener listener) {
    if (mouseListeners == null) {
      mouseListeners = new MouseListenerCollection();
    }
    mouseListeners.add(listener);
  }

  public Highlight createHighlight(Object toBeHighlighted,
      HighlightCategory category) {
    String id = "" + highlightSerialNumber++;
    Highlight highlight = impl.createHighlight(getElement(), this,
        toBeHighlighted, category, id);
    if (highlight != null) {
      highlights.put(id, highlight);
    }
    return highlight;
  }

  /**
   * Creates a link to the supplied URL.
   * 
   * @param url the URL to be linked to
   */
  public void createLink(String url) {
    impl.createLink(getElement(), url);
  }

  public void disownHighlight(Highlight highlight) {
    String idToRemove = impl.disownHighlight(highlight);
    highlights.remove(idToRemove);
  }

  /**
   * Uses the specified format.
   * 
   * @param format the format to use
   */
  public void formatBlock(BlockFormat format) {
    impl.formatBlock(getElement(), format.toString());
  }

  public int getAbsoluteLeft(Element childElement) {
    return impl.getAbsoluteLeft(childElement, getElement());
  }

  public int getAbsoluteTop(Element childElement) {
    return impl.getAbsoluteTop(childElement, getElement());
  }

  /**
   * Gets the background color.
   * 
   * @return the background color
   */
  public String getBackColor() {
    return impl.getBackColor(getElement());
  }

  /**
   * Gets the foreground color.
   * 
   * @return the foreground color
   */
  public String getForeColor() {
    return impl.getForeColor(getElement());
  }

  public String getHTML() {
    return impl.getHTML(getElement());
  }

  /**
   * Gets the justification.
   * 
   * @return the justification
   */
  public Justification getJustification() {
    Element e = getElement();
    if (impl.isJustifiedLeft(e)) {
      return Justification.LEFT;
    } else if (impl.isJustifiedRight(e)) {
      return Justification.RIGHT;
    } else if (impl.isJustifiedCenter(e)) {
      return Justification.CENTER;
    } else {
      return null;
    }
  }

  /**
   * Gets the currently selected HTML.
   * 
   * @return the currently selected HTML
   */
  public String getSelectedHTML() {
    return impl.getSelectedHTML(getElement());
  }

  public String getText() {
    return impl.getText(getElement());
  }

  /**
   * Inserts a horizontal rule.
   */
  public void insertHorizontalRule() {
    impl.insertHorizontalRule(getElement());
  }

  /**
   * Starts an numbered list. Indentation will create nested items.
   */
  public void insertOrderedList() {
    impl.insertOrderedList(getElement());
  }

  /**
   * Starts an bulleted list. Indentation will create nested items.
   */
  public void insertUnorderedList() {
    impl.insertUnorderedList(getElement());
  }

  /**
   * Is the current region bold?
   * 
   * @return true if the current region is bold
   */
  public boolean isBold() {
    return impl.isBold(getElement());
  }

  /**
   * Is the current region italic?
   * 
   * @return true if the current region is italic
   */
  public boolean isItalic() {
    return impl.isItalic(getElement());
  }

  /**
   * Is the inner iframe finished loading?
   * 
   * @return true if the inner iframe finished loading
   */
  public boolean isLoaded() {
    return impl.isLoaded(getElement());
  }

  /**
   * Is the current region underlined?
   * 
   * @return true if the current region is underlined
   */
  public boolean isUnderlined() {
    return impl.isUnderlined(getElement());
  }

  /**
   * Left indent.
   */
  public void leftIndent() {
    impl.leftIndent(getElement());
  }

  public void onForeignDOMEvent(Element sender, Event event) {
    // The doXXEvent methods possibly fire highlight handlers based on the
    // ability to do real rich text editing.
    previewEvent(event);
    switch (DOM.eventGetType(event)) {
      case Event.ONCHANGE:
        fireChangeListeners();
        break;
      case Event.ONKEYDOWN:
      case Event.ONKEYUP:
      case Event.ONKEYPRESS:
        impl.doRichTextKeyPress(this, getElement(), event);
        break;
      case Event.ONCLICK:
        impl.doRichTextClick(this, getElement(), event);
        break;
      case Event.ONMOUSEDOWN:
      case Event.ONMOUSEMOVE:
      case Event.ONMOUSEOUT:
      case Event.ONMOUSEOVER:
      case Event.ONMOUSEUP:
        impl.doRichTextMouseEvent(this, getElement(), event);
    }

    // FocusWiget handles keyboard events and click events and nothing else.
    if (!DOMUtil.eventWasPrevented(event)) {
      super.onBrowserEvent(event);
    }
  }

  public void removeChangeListener(ChangeListener listener) {
    changeListeners.remove(listener);
  }

  public void removeKeyboardListener(KeyboardListener listener) {
    if (keyboardListeners != null) {
      keyboardListeners.remove(listener);
    }
  }

  public void removeMouseListener(MouseListener listener) {
    if (mouseListeners != null) {
      mouseListeners.remove(listener);
    }
  }

  /**
   * Indent left.
   */
  public void rightIndent() {
    impl.rightIndent(getElement());
  }

  /**
   * Sets the background color.
   * 
   * @param color the new background color
   */
  public void setBackColor(String color) {
    impl.setBackColor(getElement(), color);
  }

  public void setFocus(boolean focused) {
    impl.setFocus(getElement(), focused);
  }

  /**
   * Sets the font name.
   * 
   * @param name the new font name
   */
  public void setFontName(String name) {
    impl.setFontName(getElement(), name);
  }

  /**
   * Sets the font size.
   * 
   * @param fontSize the new font size
   */
  public void setFontSize(FontSize fontSize) {
    impl.setFontSize(getElement(), fontSize.tag);
  }

  /**
   * Sets the foreground color.
   * 
   * @param color the new foreground color
   */
  public void setForeColor(String color) {
    impl.setForeColor(getElement(), color);
  }

  public void setHTML(String html) {
    impl.setHTML(getElement(), html);
  }

  /**
   * Sets the justification.
   * 
   * @param justification the new justification
   */
  public void setJustification(Justification justification) {
    if (justification == Justification.LEFT) {
      impl.justifyLeft(getElement());
    } else if (justification == Justification.RIGHT) {
      impl.justifyRight(getElement());
    } else if (justification == Justification.CENTER) {
      impl.justifyCenter(getElement());
    }
  }

  public void setText(String text) {
    impl.setText(getElement(), text);
  }

  /**
   * Toggles bold.
   */
  public void toggleBold() {
    impl.toggleBold(getElement());
  }

  /**
   * Toggles italic.
   */
  public void toggleItalic() {
    impl.toggleItalic(getElement());
  }

  /**
   * Toggles underline.
   */
  public void toggleUnderline() {
    impl.toggleUnderline(getElement());
  }

  protected void onAttach() {
    super.onAttach();

    // The element must be initialized only once it is attached to the DOM.
    impl.hookEvents(getElement(), this, this.cssURL);
  }

  protected void onDetach() {
    // Unhook the iframe's onLoad when detached.
    impl.unhookEvents(getElement(), this);
    super.onDetach();
  }

  void fireChangeListeners() {
    if (changeListeners != null) {
      changeListeners.fireChange(this);
    }
  }

  void fireMouseListeners(Event event) {
    if (mouseListeners != null) {
      mouseListeners.fireMouseEvent(this, event);
    }
  }

  /**
   * Get the {@link Highlight} associated with this id.
   * 
   * @param highlightId the id possibly associated with a highlight
   * @return the {@link Highlight} associated with this id
   */
  Highlight getHighlight(String highlightId) {
    return (Highlight) (highlights.get(highlightId));
  }

  /**
   * Returns the highlight id of the supplied element if it exists.
   * 
   * @param possibleHighlight the element that may have a highlight id
   * @return the highlight id or null if none is present
   */
  String getHighlightId(Element possibleHighlight) {
    return impl.getHighlightId(possibleHighlight);
  }

  /**
   * Selects all the text. Not public as we have not settled on a selection API.
   * Used in testing.
   */
  void selectAll() {
    impl.selectAll(getElement());
  }

  private native void previewEvent(Event evt) /*-{
   @com.google.gwt.user.client.DOM::previewEvent(Lcom/google/gwt/user/client/Event;)(evt);
   }-*/;
}
