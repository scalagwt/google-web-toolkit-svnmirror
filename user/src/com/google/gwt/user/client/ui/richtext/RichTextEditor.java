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
import com.google.gwt.i18n.client.Constants;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.CustomButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.richtext.SpellCheck.Oracle;

/**
 * A simple rich text editor.
 */
public class RichTextEditor extends Composite {

  /**
   * Provides buttons for use in the tool bar.
   */
  public interface ButtonCustomizer {

    /**
     * Customize button for background color. *
     * 
     * @param button the button for background color.
     */
    public void backgroundColor(CustomButton button);

    /**
     * Customize button for block style.
     * 
     * @param button the button for block style.
     */
    public void blockStyle(CustomButton button);

    /**
     * Customize button for bold.
     * 
     * @param button the button for bold.
     */
    public void bold(CustomButton button);

    /**
     * Customize button for bulleted list.
     * 
     * @param button the button for bulleted list.
     */
    public void bulletList(CustomButton button);

    /**
     * Customize button for font color.
     * 
     * @param button the button for font color.
     */
    public void fontColor(CustomButton button);

    /**
     * Customize button for font family. *
     * 
     * @param button the button for font family.
     */
    public void fontFamily(CustomButton button);

    /**
     * Customize button for font size.
     * 
     * @param button the button for font size.
     */
    public void fontSize(CustomButton button);

    /**
     * Customize button for insert horizontal rule.
     * 
     * @param button the button for insert horizontal rule.
     */
    public void insertHRule(CustomButton button);

    /**
     * Customize button for italics.
     * 
     * @param button the button for italics.
     */
    public void italics(CustomButton button);

    /**
     * Customize button for justify center.
     * 
     * @param button the button for justify center.
     */
    public void justifyCenter(CustomButton button);

    /**
     * Customize button for justify left.
     * 
     * @param button the button for justify left.
     */
    public void justifyLeft(CustomButton button);

    /**
     * Customize button for justify right.
     * 
     * @param button the button for justify right.
     */
    public void justifyRight(CustomButton button);

    /**
     * Customize button for left indent.
     * 
     * @param button the button for left indent.
     */
    public void leftIndent(CustomButton button);

    /**
     * Customize button for link.
     * 
     * @param button the button for link.
     */
    public void link(CustomButton button);

    /**
     * Customize button for ordered list.
     * 
     * @param button the button for ordered list.
     */
    public void orderedList(CustomButton button);

    /**
     * Customize button for right indent.
     * 
     * @param button the button for right indent.
     */
    public void rightIndent(CustomButton button);

    /**
     * Customize button for underline.
     * 
     * @param button the button for underline.
     */
    public void underline(CustomButton button);
  }

  /**
   * Supplies text constants for the rich +text editor.
   */
  public interface LabelProvider extends Constants, SpellCheck.LabelProvider {

    public String addressFormatStyle();

    public String alignCenterIconText();

    public String alignLeftIconText();

    public String alignRightIconText();

    public String blockStyleIconText();

    public String boldIconText();

    public String bulletedListIconText();

    public String checkingStatus();

    public String doneAction();

    public String fontArial();

    public String fontColorIconText();

    public String fontCourierNew();

    public String fontCursive();

    public String fontIconText();

    public String fontSizeHuge();

    public String fontSizeIconText();

    public String fontSizeLarge();

    public String fontSizeNormal();

    public String fontSizeSmall();

    public String fontTimesNewRoman();

    public String h1FormatStyle();

    public String h2FormatStyle();

    public String h3FormatStyle();

    public String h4FormatStyle();

    public String h5FormatStyle();

    public String h6FormatStyle();

    public String highlightColorIconText();

    public String htmlLinkIconText();

    public String indentIconText();

    public String insertHRuleIconText();

    public String italicsIconText();

    public String linkEnterText();

    public String linkNeedSelection();

    public String noMisspellingsFoundStatus();

    public String noSuggestions();

    public String orderedListIconText();

    public String outdentIconText();

    public String paragraphFormatStyle();

    public String preFormatStyle();

    public String reheckAction();

    public String spellingAction();

    public String underlineIconText();
  }

  private final VerticalPanel panel = new VerticalPanel();
  private final RichTextArea richTextArea;
  private final RichTextEditorImpl impl = (RichTextEditorImpl) GWT.create(RichTextEditorImpl.class);

  /**
   * Constructor for {@link RichTextEditor}.
   */
  public RichTextEditor() {
    this((String) null);
  }

  /**
   * 
   * Constructor for {@link RichTextEditor}.
   * 
   * @param buttonProvider custom button provider
   */
  public RichTextEditor(ButtonCustomizer buttonProvider) {
    this((String) null, buttonProvider);
  }

  /**
   * 
   * Constructor for {@link RichTextEditor}.
   * 
   * @param buttonProvider custom button provider
   * @param labelProvider custom label provider
   */
  public RichTextEditor(ButtonCustomizer buttonProvider,
      LabelProvider labelProvider) {
    this((String) null, buttonProvider, labelProvider);
  }

  /**
   * Constructor for {@link RichTextEditor}.
   * 
   * @param labelProvider custom label provider
   */
  public RichTextEditor(LabelProvider labelProvider) {
    this((String) null, labelProvider);
  }

  /**
   * Constructor for {@link RichTextEditor}.
   * 
   * @param cssURL the css file to use for styling the edited highlighted item
   */
  public RichTextEditor(String cssURL) {
    richTextArea = new RichTextArea(cssURL);
    impl.useDefaultButtonProvider();
    impl.useDefaultTextProvider();
    setup();
  }

  /**
   * 
   * Constructor for {@link RichTextEditor}.
   * 
   * @param cssURL the css file to use for styling the edited highlighted item
   * @param buttonProvider custom button provider
   */
  public RichTextEditor(String cssURL, ButtonCustomizer buttonProvider) {
    richTextArea = new RichTextArea(cssURL);
    impl.setButtonProvider(buttonProvider);
    impl.useDefaultTextProvider();
    setup();
  }

  /**
   * 
   * Constructor for {@link RichTextEditor}.
   * 
   * @param cssURL the css file to use for styling the edited highlighted item
   * @param buttonProvider custom button provider
   * @param labelProvider custom label provider
   */
  public RichTextEditor(String cssURL, ButtonCustomizer buttonProvider,
      LabelProvider labelProvider) {
    richTextArea = new RichTextArea(cssURL);
    impl.setButtonProvider(buttonProvider);
    impl.setLabels(labelProvider);
    setup();
  }

  /**
   * Constructor for {@link RichTextEditor}.
   * 
   * @param cssURL the css file to use for styling the edited highlighted item
   * @param labelProvider custom label provider
   */
  public RichTextEditor(String cssURL, LabelProvider labelProvider) {
    richTextArea = new RichTextArea(cssURL);
    impl.setLabels(labelProvider);
    impl.useDefaultButtonProvider();
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

  /**
   * Sets the spell check model.
   * 
   * @param spellCheckModel spell check model to set
   */
  public void setSpellCheckModel(Oracle spellCheckModel) {
    impl.setSpellCheckModel(spellCheckModel);
  }

  protected void onAttach() {
    super.onAttach();
    impl.updateImages();
  }

  /**
   * Returns the current label provider.
   * 
   * @return the current label provider.
   */
  LabelProvider getTextProvider() {
    return impl.getLabels();
  }

  /**
   * Shared setup between all constructors.
   */
  private void setup() {
    initWidget(panel);
    this.setStyleName("gwt-RichTextEditor");
    impl.init(this, panel);
    panel.add(richTextArea);
    richTextArea.setWidth("100%");
  }
}
