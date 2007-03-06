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
import com.google.gwt.user.client.ui.ColorPickerPopup;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.CustomButton;
import com.google.gwt.user.client.ui.SelectablePopupButton;
import com.google.gwt.user.client.ui.SuggestionsPopup;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * A simple rich text editor.
 */
public class RichTextEditor extends Composite {

  /**
   * Supplies push buttons for use in the tool bar.
   */
  public interface ButtonProvider {

    /**
     * Button for background color. *
     * 
     * @param popup popup to use
     * @return the button for background color.
     */
    public SelectablePopupButton backgroundColor(ColorPickerPopup popup);

    /**
     * Button for block style.
     * 
     * @param popup popup to use
     * @return the button for block style.
     */
    public SelectablePopupButton blockStyle(SuggestionsPopup popup);

    /**
     * Button for bold.
     * 
     * @return the button for bold.
     */
    public CustomButton bold();

    /**
     * Button for bulleted list.
     * 
     * @return the button for bulleted list.
     */
    public CustomButton bulletList();

    /**
     * Button for font color.
     * 
     * @param popup popup to use
     * @return the button for font color.
     */
    public SelectablePopupButton fontColor(ColorPickerPopup popup);

    /**
     * Button for font family.
     * 
     * @param popup popup to use
     * @return the button for font family.
     */
    public SelectablePopupButton fontFamily(SuggestionsPopup popup);

    /**
     * Button for font size.
     * 
     * @param popup popup to use
     * @return the button for font size.
     */
    public SelectablePopupButton fontSize(SuggestionsPopup popup);

    /**
     * Button for insert horizontal rule.
     * 
     * @return the button for insert horizontal rule.
     */
    public CustomButton insertHRule();

    /**
     * Button for italics.
     * 
     * @return the button for italics.
     */
    public CustomButton italics();

    /**
     * Button for justify center.
     * 
     * @return the button for justify center.
     */
    public CustomButton justifyCenter();

    /**
     * Button for justify left.
     * 
     * @return the button for justify left.
     */
    public CustomButton justifyLeft();

    /**
     * Button for justify right.
     * 
     * @return the button for justify right.
     */
    public CustomButton justifyRight();

    /**
     * Button for left indent.
     * 
     * @return the button for left indent.
     */
    public CustomButton leftIndent();

    /**
     * Button for link.
     * 
     * @return the button for link.
     */
    public CustomButton link();

    /**
     * Button for ordered list.
     * 
     * @return the button for ordered list.
     */
    public CustomButton orderedList();

    /**
     * Button for right indent.
     * 
     * @return the button for right indent.
     */
    public CustomButton rightIndent();

    /**
     * Button for underline.
     * 
     * @return the button for underline.
     */
    public CustomButton underline();
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
  public RichTextEditor(ButtonProvider buttonProvider) {
    this((String) null, buttonProvider);
  }

  /**
   * 
   * Constructor for {@link RichTextEditor}.
   * 
   * @param buttonProvider custom button provider
   * @param labelProvider custom label provider
   */
  public RichTextEditor(ButtonProvider buttonProvider,
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
  public RichTextEditor(String cssURL, ButtonProvider buttonProvider) {
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
  public RichTextEditor(String cssURL, ButtonProvider buttonProvider,
      LabelProvider labelProvider) {
    richTextArea = new RichTextArea(cssURL);
    impl.setButtonProvider(buttonProvider);
    impl.setTextProvider(labelProvider);
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
    impl.setTextProvider(labelProvider);
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
   * Returns the current label provider.
   * 
   * @return the current label provider.
   */
  public LabelProvider getTextProvider() {
    return impl.getTextProvider();
  }

  /**
   * Sets the spell check model.
   * 
   * @param spellCheckModel spell check model to set
   */
  public void setSpellCheckModel(SpellCheck.Model spellCheckModel) {
    impl.setSpellCheckModel(spellCheckModel);
  }

  protected void onAttach() {
    super.onAttach();
    impl.updateImages();
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
