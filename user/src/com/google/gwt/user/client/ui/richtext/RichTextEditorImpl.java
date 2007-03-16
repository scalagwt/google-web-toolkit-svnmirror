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
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FocusListener;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.MouseListener;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.richtext.RichTextArea.BlockFormat;
import com.google.gwt.user.client.ui.richtext.RichTextArea.FontSize;
import com.google.gwt.user.client.ui.richtext.RichTextArea.Justification;
import com.google.gwt.user.client.ui.richtext.RichTextEditor.ButtonCustomizer;
import com.google.gwt.user.client.ui.richtext.RichTextEditor.LabelProvider;

import java.util.Iterator;
import java.util.List;

/**
 * This is the implementation class for RichTextEditor, to be replaced via
 * dynamic binding. This functionality depends on browser support for rich text,
 * so unsupported browsers will have not have this functionality, and these
 * blank methods will be used.
 */
class RichTextEditorImpl {
  class WrappedRichText extends RichTextArea {
    RichTextArea area;

    public WrappedRichText(RichTextArea richTextArea) {
      area = richTextArea;
    }

    public void addChangeListener(ChangeListener listener) {
      area.addChangeListener(listener);
    }

    public void addClickListener(ClickListener listener) {
      area.addClickListener(listener);
    }

    public void addFocusListener(FocusListener listener) {
      area.addFocusListener(listener);
    }

    public Iterator addHighlights(List items, HighlightCategory category) {
      return area.addHighlights(items, category);
    }

    public void addKeyboardListener(KeyboardListener listener) {
      area.addKeyboardListener(listener);
    }

    public void addMouseListener(MouseListener listener) {
      area.addMouseListener(listener);
    }

    public void addStyleName(String style) {
      area.addStyleName(style);
    }

    public Highlight createHighlight(Object toBeHighlighted,
        HighlightCategory category) {
      return area.createHighlight(toBeHighlighted, category);
    }

    public void createLink(String url) {
      area.createLink(url);
    }

    public boolean equals(Object obj) {
      return area.equals(obj);
    }

    public void formatBlock(BlockFormat format) {
      area.formatBlock(format);
    }

    public int getAbsoluteLeft() {
      return area.getAbsoluteLeft();
    }

    public int getAbsoluteLeft(Element childElement) {
      return area.getAbsoluteLeft(childElement);
    }

    public int getAbsoluteTop() {
      return area.getAbsoluteTop();
    }

    public int getAbsoluteTop(Element childElement) {
      return area.getAbsoluteTop(childElement);
    }

    public String getBackColor() {
      return area.getBackColor();
    }

    public Element getElement() {
      return area.getElement();
    }

    public String getForeColor() {
      return area.getForeColor();
    }

    public String getHTML() {
      return area.getHTML();
    }

    public Justification getJustification() {
      return area.getJustification();
    }

    public int getOffsetHeight() {
      return area.getOffsetHeight();
    }

    public int getOffsetWidth() {
      return area.getOffsetWidth();
    }

    public Widget getParent() {
      return area.getParent();
    }

    public String getSelectedHTML() {
      return area.getSelectedHTML();
    }

    public String getStyleName() {
      return area.getStyleName();
    }

    public int getTabIndex() {
      return area.getTabIndex();
    }

    public String getText() {
      return area.getText();
    }

    public String getTitle() {
      return area.getTitle();
    }

    public int hashCode() {
      return area.hashCode();
    }

    public void insertHorizontalRule() {
      area.insertHorizontalRule();
    }

    public void insertOrderedList() {
      area.insertOrderedList();
    }

    public void insertUnorderedList() {
      area.insertUnorderedList();
    }

    public boolean isAttached() {
      return area.isAttached();
    }

    public boolean isBold() {
      return area.isBold();
    }

    public boolean isEnabled() {
      return area.isEnabled();
    }

    public boolean isItalic() {
      return area.isItalic();
    }

    public boolean isLoaded() {
      return area.isLoaded();
    }

    public boolean isUnderlined() {
      return area.isUnderlined();
    }

    public boolean isVisible() {
      return area.isVisible();
    }

    public void leftIndent() {
      report("leftIndent");
      area.leftIndent();
    }

    public void onBrowserEvent(Event event) {
      area.onBrowserEvent(event);
    }

    public void onForeignDOMEvent(Element sender, Event event) {
      area.onForeignDOMEvent(sender, event);
    }

    public void removeChangeListener(ChangeListener listener) {
      area.removeChangeListener(listener);
    }

    public void removeClickListener(ClickListener listener) {
      area.removeClickListener(listener);
    }

    public void removeFocusListener(FocusListener listener) {
      area.removeFocusListener(listener);
    }

    public void removeFromParent() {
      area.removeFromParent();
    }

    public void removeKeyboardListener(KeyboardListener listener) {
      area.removeKeyboardListener(listener);
    }

    public void removeMouseListener(MouseListener listener) {
      area.removeMouseListener(listener);
    }

    public void removeStyleName(String style) {
      area.removeStyleName(style);
    }

    public void rightIndent() {
      report("right indent");
      area.rightIndent();
    }

    public void setAccessKey(char key) {
      area.setAccessKey(key);
    }

    public void setBackColor(String color) {
      area.setBackColor(color);
    }

    public void setEnabled(boolean enabled) {
      area.setEnabled(enabled);
    }

    public void setFocus(boolean focused) {
      area.setFocus(focused);
    }

    public void setFontName(String name) {
      area.setFontName(name);
    }

    public void setFontSize(FontSize fontSize) {
      area.setFontSize(fontSize);
    }

    public void setForeColor(String color) {
      area.setForeColor(color);
    }

    public void setHeight(String height) {
      area.setHeight(height);
    }

    public void setHTML(String html) {
      area.setHTML(html);
    }

    public void setJustification(Justification justification) {
      area.setJustification(justification);
    }

    public void setPixelSize(int width, int height) {
      area.setPixelSize(width, height);
    }

    public void setSize(String width, String height) {
      area.setSize(width, height);
    }

    public void setTabIndex(int index) {
      area.setTabIndex(index);
    }

    public void setText(String text) {
      area.setText(text);
    }

    public void setTitle(String title) {
      area.setTitle(title);
    }

    public void setVisible(boolean visible) {
      area.setVisible(visible);
    }

    public void setWidth(String width) {
      area.setWidth(width);
    }

    public void sinkEvents(int eventBitsToAdd) {
    }

    public void toggleBold() {
      area.toggleBold();
    }

    public void toggleItalic() {
      area.toggleItalic();
    }

    public void toggleUnderline() {
      area.toggleUnderline();
    }

    public String toString() {
      return area.toString();
    }

    public void unsinkEvents(int eventBitsToRemove) {
      if (area != null) {
        area.unsinkEvents(eventBitsToRemove);
      }
    }

    private void report(String string) {
      System.err.println(string);
    }
  }

  /**
   * The rich text editor that this is implementing the functionality of.
   */
  RichTextArea richTextArea;

  /**
   * Adds the appropriate listeners to the editor so the button states get
   * updated in a timely manner.
   */
  void addListenersToRichText() {
  }

  /**
   * Creates the tool bar to expose functionality supported by the underlying
   * {@link RichTextArea} implementation. This will vary by browser.
   * 
   * @param editor the editor being used to edit rich text
   * @param panel the panel within the editor that will contain the tool bar
   */
  void createToolBar(RichTextEditor editor, Panel panel) {
  }

  LabelProvider getLabels() {
    return null;
  }

  void init(RichTextEditor editor, Panel panel) {
    richTextArea = new WrappedRichText(editor.getRichTextArea());
    createToolBar(editor, panel);
    addListenersToRichText();
  }

  /**
   * Sets the button provider.
   * 
   * @param provider the new button provider
   */
  void setButtonProvider(ButtonCustomizer provider) {
  }

  /**
   * Sets the label provider.
   * 
   * @param provider the new label provider
   */
  void setLabels(LabelProvider provider) {
  }

  void setSpellCheckModel(SpellCheckOracle spellCheckModel) {
  }

  /**
   * Sets the current spell check state.
   * 
   * @param state the spell check state
   */
  void setSpellCheckState(SpellCheck.State state) {
    return;
  }

  /**
   * Makes the editor update all of the button display states depending on the
   * editor's current state.
   */
  void updateImages() {
  }

  /**
   * Make the editor use the default (ImageBundle backed) button provider.
   */
  void useDefaultButtonProvider() {
  }

  /**
   * Make the editor use the default (internationalized property file based)
   * text provider.
   */
  void useDefaultTextProvider() {
  }
}
