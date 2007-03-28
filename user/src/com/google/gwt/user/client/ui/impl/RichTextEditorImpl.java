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

package com.google.gwt.user.client.ui.impl;

import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RichTextEditor;
import com.google.gwt.user.client.ui.RichTextEditor.ButtonFaces;
import com.google.gwt.user.client.ui.RichTextEditor.LabelProvider;
import com.google.gwt.user.client.ui.richtext.RichTextArea;

/**
 * This is the implementation class for RichTextEditor, to be replaced via
 * dynamic binding. This functionality depends on browser support for rich text,
 * so unsupported browsers will have not have this functionality, and these
 * blank methods will be used.
 */
public class RichTextEditorImpl {

  /**
   * The rich text editor.
   */
  RichTextArea richTextArea;

  /**
   * Initialize the {@link RichTextEditorImpl}
   * @param editor editor
   * @param panel root panel
   */
  public void init(RichTextEditor editor, Panel panel) {
    richTextArea = editor.getRichTextArea();
    createToolBar(editor, panel);
    addListenersToRichText();
  }

  /**
   * Sets the button faces.
   * 
   * @param faces the new button faces
   */
  public void setButtonFaces(ButtonFaces faces) {
  }

  /**
   * Sets the label provider.
   * 
   * @param provider the new label provider
   */
  public void setLabels(LabelProvider provider) {
  }

  /**
   * Sets the number of rich text buttons per row.
   */
  public void setNumberOfButtonsPerRow(int numberOfButtons) {
  }

  /**
   * Makes the editor update all of the button display states depending on the
   * editor's current state.
   */
  public void updateListeningButtons() {
  }

  /**
   * Make the editor use the default (ImageBundle backed) button provider.
   */
  public void useDefaultButtonFaces() {
  }

  /**
   * Make the editor use the default (internationalized property file based)
   * label provider.
   */
  public void useDefaultLabels() {
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
}
