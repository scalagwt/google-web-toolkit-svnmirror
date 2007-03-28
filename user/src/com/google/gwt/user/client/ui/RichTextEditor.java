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
package com.google.gwt.user.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.impl.RichTextEditorImpl;
import com.google.gwt.user.client.ui.richtext.RichTextArea;

/**
 * A simple rich text editor.
 */
public class RichTextEditor extends Composite {

  private static final String STYLENAME_DEFAULT = "gwt-RichTextEditor";

  /**
   * Customizes the button faces for use in the tool bar.
   */
  public interface ButtonFaces {

    /**
     * Customize button for background color.
     * 
     * @param button the button for background color.
     */
    public void backgroundColor(CustomButton button);

    /**
     * Customize button for block style.
     * 
     * @param button the button for block style
     */
    public void blockStyle(CustomButton button);

    /**
     * Customize button for bold.
     * 
     * @param button the button for bold
     */
    public void bold(CustomButton button);

    /**
     * Customize button for bulleted list.
     * 
     * @param button the button for bulleted list
     */
    public void bulletList(CustomButton button);

    /**
     * Customize button for font color.
     * 
     * @param button the button for font color
     */
    public void fontColor(CustomButton button);

    /**
     * Customize button for font family.
     * 
     * @param button the button for font family
     */
    public void fontFamily(CustomButton button);

    /**
     * Customize button for font size.
     * 
     * @param button the button for font size
     */
    public void fontSize(CustomButton button);

    /**
     * Customize button for insert horizontal rule.
     * 
     * @param button the button for insert horizontal rule
     */
    public void insertHRule(CustomButton button);

    /**
     * Customize button for italics.
     * 
     * @param button the button for italics
     */
    public void italics(CustomButton button);

    /**
     * Customize button for justify center.
     * 
     * @param button the button for justify center
     */
    public void justifyCenter(CustomButton button);

    /**
     * Customize button for justify left.
     * 
     * @param button the button for justify left
     */
    public void justifyLeft(CustomButton button);

    /**
     * Customize button for justify right.
     * 
     * @param button the button for justify right
     */
    public void justifyRight(CustomButton button);

    /**
     * Customize button for left indent.
     * 
     * @param button the button for left indent
     */
    public void leftIndent(CustomButton button);

    /**
     * Customize button for link.
     * 
     * @param button the button for link
     */
    public void link(CustomButton button);

    /**
     * Customize button for ordered list.
     * 
     * @param button the button for ordered list
     */
    public void orderedList(CustomButton button);

    /**
     * Customize button for right indent.
     * 
     * @param button the button for right indent
     */
    public void rightIndent(CustomButton button);

    /**
     * Customize button for underline.
     * 
     * @param button the button for underline
     */
    public void underline(CustomButton button);
  }

  /**
   * Supplies labels for the {@link RichTextEditor}.
   */
  public interface LabelProvider extends com.google.gwt.i18n.client.Constants {

    /**
     * Translated "Address".
     * 
     * @return translated "Address"
     * @gwt.key addressFormatStyle
     */
    String addressFormatStyle();

    /**
     * Translated "Align Center".
     * 
     * @return translated "Align Center"
     * @gwt.key alignCenterIconText
     */
    String alignCenterIconText();

    /**
     * Translated "Align Left".
     * 
     * @return translated "Align Left"
     * @gwt.key alignLeftIconText
     */
    String alignLeftIconText();

    /**
     * Translated "Align Right".
     * 
     * @return translated "Align Right"
     * @gwt.key alignRightIconText
     */
    String alignRightIconText();

    /**
     * Translated "Block Style".
     * 
     * @return translated "Block Style"
     * @gwt.key blockStyleIconText
     */
    String blockStyleIconText();

    /**
     * Translated "Bold".
     * 
     * @return translated "Bold"
     * @gwt.key boldIconText
     */
    String boldIconText();

    /**
     * Translated "Bulleted List".
     * 
     * @return translated "Bulleted List"
     * @gwt.key bulletedListIconText
     */
    String bulletedListIconText();

    /**
     * Translated "Checking...".
     * 
     * @return translated "Checking..."
     * @gwt.key checkingStatus
     */
    String checkingStatus();

    /**
     * Translated "Done".
     * 
     * @return translated "Done"
     * @gwt.key doneAction
     */
    String doneAction();

    /**
     * Translated "Arial".
     * 
     * @return translated "Arial"
     * @gwt.key fontArial
     */
    String fontArial();

    /**
     * Translated "Font Color".
     * 
     * @return translated "Font Color"
     * @gwt.key fontColorIconText
     */
    String fontColorIconText();

    /**
     * Translated "Courier New".
     * 
     * @return translated "Courier New"
     * @gwt.key fontCourierNew
     */
    String fontCourierNew();

    /**
     * Translated "Cursive".
     * 
     * @return translated "Cursive"
     * @gwt.key fontCursive
     */
    String fontCursive();

    /**
     * Translated "Font".
     * 
     * @return translated "Font"
     * @gwt.key fontIconText
     */
    String fontIconText();

    /**
     * Translated "Huge".
     * 
     * @return translated "Huge"
     * @gwt.key fontSizeHuge
     */
    String fontSizeHuge();

    /**
     * Translated "Font Size".
     * 
     * @return translated "Font Size"
     * @gwt.key fontSizeIconText
     */
    String fontSizeIconText();

    /**
     * Translated "Large".
     * 
     * @return translated "Large"
     * @gwt.key fontSizeLarge
     */
    String fontSizeLarge();

    /**
     * Translated "Normal".
     * 
     * @return translated "Normal"
     * @gwt.key fontSizeNormal
     */
    String fontSizeNormal();

    /**
     * Translated "Small".
     * 
     * @return translated "Small"
     * @gwt.key fontSizeSmall
     */
    String fontSizeSmall();

    /**
     * Translated "Times New Roman".
     * 
     * @return translated "Times New Roman"
     * @gwt.key fontTimesNewRoman
     */
    String fontTimesNewRoman();

    /**
     * Translated "Headline 1".
     * 
     * @return translated "Headline 1"
     * @gwt.key h1FormatStyle
     */
    String h1FormatStyle();

    /**
     * Translated "Headline 2".
     * 
     * @return translated "Headline 2"
     * @gwt.key h2FormatStyle
     */
    String h2FormatStyle();

    /**
     * Translated "Headline 3".
     * 
     * @return translated "Headline 3"
     * @gwt.key h3FormatStyle
     */
    String h3FormatStyle();

    /**
     * Translated "Headline 4".
     * 
     * @return translated "Headline 4"
     * @gwt.key h4FormatStyle
     */
    String h4FormatStyle();

    /**
     * Translated "Headline 5".
     * 
     * @return translated "Headline 5"
     * @gwt.key h5FormatStyle
     */
    String h5FormatStyle();

    /**
     * Translated "Headline 6".
     * 
     * @return translated "Headline 6"
     * @gwt.key h6FormatStyle
     */
    String h6FormatStyle();

    /**
     * Translated "Highlight Color".
     * 
     * @return translated "Highlight Color"
     * @gwt.key highlightColorIconText
     */
    String highlightColorIconText();

    /**
     * Translated "HTML Link".
     * 
     * @return translated "HTML Link"
     * @gwt.key htmlLinkIconText
     */
    String htmlLinkIconText();

    /**
     * Translated "Indent".
     * 
     * @return translated "Indent"
     * @gwt.key indentIconText
     */
    String indentIconText();

    /**
     * Translated "Insert Horizontal Rule".
     * 
     * @return translated "Insert Horizontal Rule"
     * @gwt.key insertHRuleIconText
     */
    String insertHRuleIconText();

    /**
     * Translated "Italic".
     * 
     * @return translated "Italic"
     * @gwt.key italicsIconText
     */
    String italicsIconText();

    /**
     * Translated "Enter an URL".
     * 
     * @return translated "Enter an URL"
     * @gwt.key linkEnterText
     */
    String linkEnterText();

    /**
     * Translated "First select the text that you want to make into a link".
     * 
     * @return translated "First select the text that you want to make into a
     *         link"
     * @gwt.key linkNeedSelection
     */
    String linkNeedSelection();

    /**
     * Translated "No misspellings found".
     * 
     * @return translated "No misspellings found"
     * @gwt.key noMisspellingsFoundStatus
     */
    String noMisspellingsFoundStatus();

    /**
     * Translated "No Suggestions".
     * 
     * @return translated "No Suggestions"
     * @gwt.key noSuggestions
     */
    String noSuggestions();

    /**
     * Translated "Ordered List".
     * 
     * @return translated "Ordered List"
     * @gwt.key orderedListIconText
     */
    String orderedListIconText();

    /**
     * Translated "Outdent".
     * 
     * @return translated "Outdent"
     * @gwt.key outdentIconText
     */
    String outdentIconText();

    /**
     * Translated "Paragraph".
     * 
     * @return translated "Paragraph"
     * @gwt.key paragraphFormatStyle
     */
    String paragraphFormatStyle();

    /**
     * Translated "Preformatted".
     * 
     * @return translated "Preformatted"
     * @gwt.key preFormatStyle
     */
    String preFormatStyle();

    /**
     * Translated "Recheck".
     * 
     * @return translated "Recheck"
     * @gwt.key reheckAction
     */
    String reheckAction();

    /**
     * Translated "Spelling".
     * 
     * @return translated "Spelling"
     * @gwt.key spellingAction
     */
    String spellingAction();

    /**
     * Translated "Underline".
     * 
     * @return translated "Underline"
     * @gwt.key underlineIconText
     */
    String underlineIconText();
  }

  private final VerticalPanel panel = new VerticalPanel();
  private final RichTextArea richTextArea;
  private final RichTextEditorImpl impl = (RichTextEditorImpl) GWT.create(RichTextEditorImpl.class);

  /**
   * Constructor for {@link RichTextEditor}.
   */
  public RichTextEditor() {
    richTextArea = new RichTextArea();
    impl.useDefaultButtonFaces();
    impl.useDefaultLabels();
    setup();
  }

  /**
   * 
   * Constructor for {@link RichTextEditor}.
   * 
   * @param buttonFaces custom button faces
   */
  public RichTextEditor(ButtonFaces buttonFaces) {
    richTextArea = new RichTextArea();
    impl.setButtonFaces(buttonFaces);
    impl.useDefaultLabels();
    setup();
  }

  /**
   * 
   * Constructor for {@link RichTextEditor}.
   * 
   * @param buttonFaces custom button faces
   * @param numberOfButtonsPerRow number of buttons per row
   */
  public RichTextEditor(ButtonFaces buttonFaces, int numberOfButtonsPerRow) {
    richTextArea = new RichTextArea();
    impl.setNumberOfButtonsPerRow(numberOfButtonsPerRow);
    impl.setButtonFaces(buttonFaces);
    impl.useDefaultLabels();
    setup();
  }

  /**
   * 
   * Constructor for {@link RichTextEditor}.
   * 
   * @param buttonFaces custom button faces
   * @param labelProvider custom label provider
   */
  public RichTextEditor(ButtonFaces buttonFaces, LabelProvider labelProvider) {
    richTextArea = new RichTextArea();
    impl.setButtonFaces(buttonFaces);
    impl.setLabels(labelProvider);
    setup();
  }

  /**
   * 
   * Constructor for {@link RichTextEditor}.
   * 
   * @param buttonFaces custom button faces
   * @param labelProvider custom label provider
   * @param numberOfButtonsPerRow number of buttons per row
   */
  public RichTextEditor(ButtonFaces buttonFaces, LabelProvider labelProvider,
      int numberOfButtonsPerRow) {
    richTextArea = new RichTextArea();
    impl.setNumberOfButtonsPerRow(numberOfButtonsPerRow);
    impl.setButtonFaces(buttonFaces);
    impl.setLabels(labelProvider);
    setup();
  }

  /**
   * Constructor for {@link RichTextEditor}.
   * 
   * @param labelProvider custom label provider
   */
  public RichTextEditor(LabelProvider labelProvider) {
    richTextArea = new RichTextArea();
    impl.setLabels(labelProvider);
    impl.useDefaultButtonFaces();
    setup();
  }

  /**
   * Constructor for {@link RichTextEditor}.
   * 
   * @param labelProvider custom label provider *
   * @param numberOfButtonsPerRow number of buttons per row
   */
  public RichTextEditor(LabelProvider labelProvider, int numberOfButtonsPerRow) {
    richTextArea = new RichTextArea();
    impl.setLabels(labelProvider);
    impl.setNumberOfButtonsPerRow(numberOfButtonsPerRow);
    impl.useDefaultButtonFaces();
    setup();
  }

  /**
   * Get the rich text widget associated with this editor.
   * 
   * @return rich text widget
   */
  public RichTextArea getRichTextArea() {
    return richTextArea;
  }

  protected void onAttach() {
    super.onAttach();
    impl.updateListeningButtons();
  }

  /**
   * Shared setup between all constructors.
   */
  private void setup() {
    initWidget(panel);
    richTextArea.setWidth("100%");
    this.setStyleName(STYLENAME_DEFAULT);
    impl.init(this, panel);
    panel.add(richTextArea);
  }
}
