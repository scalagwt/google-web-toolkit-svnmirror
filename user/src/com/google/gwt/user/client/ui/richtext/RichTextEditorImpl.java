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

import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.richtext.RichTextEditor.ButtonFaces;
import com.google.gwt.user.client.ui.richtext.RichTextEditor.LabelProvider;
import com.google.gwt.user.client.ui.richtext.SpellCheck.Oracle;

/**
 * This is the implementation class for RichTextEditor, to be replaced via
 * dynamic binding. This functionality depends on browser support for rich text,
 * so unsupported browsers will have not have this functionality, and these
 * blank methods will be used.
 */
class RichTextEditorImpl {

  /**
   * The rich text editor that this is implementing the functionality of.
   */
  RichTextArea richTextArea;

  public Oracle getSpellCheckOracle() {
    return null;
  }

  public void setNumberOfButtonsPerRow(int numberOfButtons) {
  }

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
    richTextArea = editor.getRichTextArea();
    createToolBar(editor, panel);
    addListenersToRichText();
  }

  /**
   * Sets the button provider.
   * 
   * @param provider the new button provider
   */
  void setButtonProvider(ButtonFaces provider) {
  }

  /**
   * Sets the label provider.
   * 
   * @param provider the new label provider
   */
  void setLabels(LabelProvider provider) {
  }

  void setSpellCheckModel(Oracle spellCheckModel) {
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
