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
import com.google.gwt.user.client.ImageBundle;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.ClippedImage;
import com.google.gwt.user.client.ui.ColorPicker;
import com.google.gwt.user.client.ui.CustomButton;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ItemPicker;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.MouseListenerAdapter;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.Suggest;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.impl.ItemPickerButtonImpl;
import com.google.gwt.user.client.ui.impl.SuggestItemPickerButtonImpl;
import com.google.gwt.user.client.ui.richtext.RichTextArea.BlockFormat;
import com.google.gwt.user.client.ui.richtext.RichTextArea.FontSize;
import com.google.gwt.user.client.ui.richtext.RichTextArea.Justification;
import com.google.gwt.user.client.ui.richtext.RichTextEditor.ButtonFaces;
import com.google.gwt.user.client.ui.richtext.RichTextEditor.LabelProvider;
import com.google.gwt.user.client.ui.richtext.SpellCheck.Oracle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * Standard Editor for rich text.
 */
class RichTextEditorImplStandard extends RichTextEditorImpl {

  /**
   * Default button provider.
   * 
   */
  static class ButtonCustomizerImpl implements ButtonFaces {

    /**
     * Images for the default button case.
     * 
     */
    interface Images extends ImageBundle {

      /**
       * @gwt.resource com/google/gwt/user/client/ui/richtext/style-button-depressed-small.gif
       */
      public ClippedImage blockStyleDown();

      /**
       * @gwt.resource com/google/gwt/user/client/ui/richtext/style-button-small.gif
       */
      public ClippedImage blockStyleUp();

      /**
       * @gwt.resource com/google/gwt/user/client/ui/richtext/bold-button-depressed-small.gif
       */
      public ClippedImage boldDown();

      /**
       * @gwt.resource com/google/gwt/user/client/ui/richtext/bold-button-small.gif
       */
      public ClippedImage boldUp();

      /**
       * @gwt.resource com/google/gwt/user/client/ui/richtext/color-button-depressed-small.gif
       */
      public abstract ClippedImage colorDown();

      /**
       * @gwt.resource com/google/gwt/user/client/ui/richtext/color-button-small.gif
       */
      public abstract ClippedImage colorUp();

      /**
       * @gwt.resource com/google/gwt/user/client/ui/richtext/font-button-depressed-small.gif
       */
      public abstract ClippedImage fontDown();

      /**
       * @gwt.resource com/google/gwt/user/client/ui/richtext/font-button-small.gif
       */
      public abstract ClippedImage fontUp();

      /**
       * @gwt.resource com/google/gwt/user/client/ui/richtext/left-indent-button-depressed-small.gif
       */
      public abstract ClippedImage indentLeftDown();

      /**
       * @gwt.resource com/google/gwt/user/client/ui/richtext/left-indent-button-small.gif
       */
      public abstract ClippedImage indentLeftUp();

      /**
       * @gwt.resource com/google/gwt/user/client/ui/richtext/right-indent-button-depressed-small.gif
       */
      public abstract ClippedImage indentRightDown();

      /**
       * @gwt.resource com/google/gwt/user/client/ui/richtext/right-indent-button-small.gif
       */
      public abstract ClippedImage indentRightUp();

      /**
       * @gwt.resource com/google/gwt/user/client/ui/richtext/hrule-button-depressed-small.gif
       */
      public ClippedImage insertHRuleDown();

      /**
       * @gwt.resource com/google/gwt/user/client/ui/richtext/hrule-button-small.gif
       */
      public ClippedImage insertHRuleUp();

      /**
       * @gwt.resource com/google/gwt/user/client/ui/richtext/italic-button-depressed-small.gif
       */
      public abstract ClippedImage italicDown();

      /**
       * @gwt.resource com/google/gwt/user/client/ui/richtext/italic-button-small.gif
       */
      public abstract ClippedImage italicUp();

      /**
       * @gwt.resource com/google/gwt/user/client/ui/richtext/justify-center-button-depressed-small.gif
       */
      public abstract ClippedImage justifyCenterDown();

      /**
       * @gwt.resource com/google/gwt/user/client/ui/richtext/justify-center-button-small.gif
       */
      public abstract ClippedImage justifyCenterUp();

      /**
       * @gwt.resource com/google/gwt/user/client/ui/richtext/justify-left-button-depressed-small.gif
       */
      public abstract ClippedImage justifyLeftDown();

      /**
       * @gwt.resource com/google/gwt/user/client/ui/richtext/justify-left-button-small.gif
       */
      public abstract ClippedImage justifyLeftUp();

      /**
       * @gwt.resource com/google/gwt/user/client/ui/richtext/justify-right-button-depressed-small.gif
       */
      public abstract ClippedImage justifyRightDown();

      /**
       * @gwt.resource com/google/gwt/user/client/ui/richtext/justify-right-button-small.gif
       */
      public abstract ClippedImage justifyRightUp();

      /**
       * @gwt.resource com/google/gwt/user/client/ui/richtext/link-button-depressed-small.gif
       */
      public abstract ClippedImage linkDown();

      /**
       * @gwt.resource com/google/gwt/user/client/ui/richtext/link-button-small.gif
       */
      public ClippedImage linkUp();

      /**
       * @gwt.resource com/google/gwt/user/client/ui/richtext/list-button-depressed-small.gif
       */
      public abstract ClippedImage listDown();

      /**
       * @gwt.resource com/google/gwt/user/client/ui/richtext/list-button-small.gif
       */
      public abstract ClippedImage listUp();

      /**
       * @gwt.resource com/google/gwt/user/client/ui/richtext/ordered-list-button-depressed-small.gif
       */
      public ClippedImage orderedListDown();

      /**
       * @gwt.resource com/google/gwt/user/client/ui/richtext/ordered-list-button-small.gif
       */
      public ClippedImage orderedListUp();

      /**
       * @gwt.resource com/google/gwt/user/client/ui/richtext/size-button-depressed-small.gif
       */
      public abstract ClippedImage sizeDown();

      /**
       * @gwt.resource com/google/gwt/user/client/ui/richtext/size-button-small.gif
       */
      public abstract ClippedImage sizeUp();

      /**
       * @gwt.resource com/google/gwt/user/client/ui/richtext/underline-button-depressed-small.gif
       */
      public abstract ClippedImage underlineDown();

      /**
       * @gwt.resource com/google/gwt/user/client/ui/richtext/underline-button-small.gif
       */
      public abstract ClippedImage underlineUp();
    }

    private Images images = (Images) GWT.create(Images.class);

    public void backgroundColor(CustomButton button) {
      button.getUpFace().setImage(images.colorUp());
      button.getDownFace().setImage(images.colorDown());
    }

    public void blockStyle(CustomButton button) {
      button.getUpFace().setImage(images.blockStyleUp());
      button.getDownFace().setImage(images.blockStyleDown());
    }

    public void bold(CustomButton button) {
      button.getUpFace().setImage(images.boldUp());
      button.getDownFace().setImage(images.boldDown());
    }

    public void bulletList(CustomButton button) {
      button.getUpFace().setImage(images.listUp());
      button.getDownFace().setImage(images.listDown());
    }

    public void fontColor(CustomButton button) {
      button.getUpFace().setImage(images.colorUp());
      button.getDownFace().setImage(images.colorDown());
    }

    public void fontFamily(CustomButton button) {
      button.getUpFace().setImage(images.fontUp());
      button.getDownFace().setImage(images.fontDown());
    }

    public void fontSize(CustomButton button) {
      button.getUpFace().setImage(images.sizeUp());
      button.getDownFace().setImage(images.sizeDown());
    }

    public void insertHRule(CustomButton button) {
      button.getUpFace().setImage(images.insertHRuleUp());
      button.getDownFace().setImage(images.insertHRuleDown());
    }

    public void italics(CustomButton button) {
      button.getUpFace().setImage(images.italicUp());
      button.getDownFace().setImage(images.italicDown());
    }

    public void justifyCenter(CustomButton button) {
      button.getUpFace().setImage(images.justifyCenterUp());
      button.getDownFace().setImage(images.justifyCenterDown());
    }

    public void justifyLeft(CustomButton button) {
      button.getUpFace().setImage(images.justifyLeftUp());
      button.getDownFace().setImage(images.justifyLeftDown());
    }

    public void justifyRight(CustomButton button) {
      button.getUpFace().setImage(images.justifyRightUp());
      button.getDownFace().setImage(images.justifyRightDown());
    }

    public void leftIndent(CustomButton button) {
      button.getUpFace().setImage(images.indentLeftUp());
      button.getDownFace().setImage(images.indentLeftDown());
    }

    public void link(CustomButton button) {
      button.getUpFace().setImage(images.linkUp());
      button.getDownFace().setImage(images.linkDown());
    }

    public void orderedList(CustomButton button) {
      button.getUpFace().setImage(images.orderedListUp());
      button.getDownFace().setImage(images.orderedListDown());
    }

    public void rightIndent(CustomButton button) {
      button.getUpFace().setImage(images.indentRightUp());
      button.getDownFace().setImage(images.indentRightDown());
    }

    public void underline(CustomButton button) {
      button.getUpFace().setImage(images.underlineUp());
      button.getDownFace().setImage(images.underlineDown());
    }
  }
  /**
   * Hook up a push button to be a drop down push button.
   * 
   */
  private abstract class HookupItemPickerButton {

    public HookupItemPickerButton(ItemPickerButtonImpl button, String string) {
      initButton(button, string);
      final ItemPicker picker = button.getItemPicker();

      picker.addChangeListener(new ChangeListener() {
        public void onChange(Widget sender) {
          change(picker.getSelectedValue());
        }
      });
    }

    /**
     * Wires a tooltip and a popup to a button.
     * 
     * @param button the button to use
     * @param toolTip the tooltip to give the button
     * @param values
     * @param popup the popup to use to get the desired choice
     */
    HookupItemPickerButton(final ItemPickerButtonImpl dropDown, String toolTip,
        final HashMap valuesMap) {

      initButton(dropDown, toolTip);
      final ItemPicker picker = dropDown.getItemPicker();

      picker.addChangeListener(new ChangeListener() {
        public void onChange(Widget sender) {
          Object realValue = valuesMap.get(picker.getSelectedValue());
          if (realValue == null) {
            throw new RuntimeException(picker.getSelectedValue()
                + " was not a known values, known values are "
                + valuesMap.keySet());
          }
          change(realValue);
        }
      });
    }

    /**
     * A callback to use when the user selects a value.
     * 
     * @param object the selection made
     */
    abstract void change(Object object);
  }

  /**
   * Hook up a push button with a rich text state.
   * 
   */
  private abstract class HookupListeningButton {
    ToggleButton button;

    /**
     * Wires a tooltip to a button, and makes it adjust to the underlying state.
     * 
     * @param button the button to use
     * @param toolTip the tooltip to give the button
     */
    HookupListeningButton(final ToggleButton button, String toolTip) {
      this.button = button;
      // Click Listener
      button.addClickListener(new ClickListener() {
        public void onClick(Widget sender) {
          HookupListeningButton.this.onClick();
        }
      });

      initButton(button, toolTip);
      listeningButtons.add(this);
    }

    /**
     * The callback to perform when the button is toggled.
     */
    abstract void click();

    void onClick() {
      click();
      updateButton();
    }

    /**
     * The callback responsible for updating the button to match the underlying
     * state.
     */
    abstract void updateButton();
  }

  /**
   * Hook up a push button.
   */
  private abstract class HookupPushButton {
    HookupPushButton(final PushButton button, String toolTip) {
      initButton(button, toolTip);
      button.addClickListener(new ClickListener() {
        public void onClick(Widget sender) {
          click();
        }
      });
    }

    /**
     * The callback to perform when the button is toggled.
     */
    abstract void click();
  }

  private abstract class HookupSuggestItemPickerButton {

    /**
     * Wires a tooltip and a popup to a button.
     * 
     * @param button the button to use
     * @param toolTip the tooltip to give the button
     * @param values
     * @param popup the popup to use to get the desired choice
     */
    HookupSuggestItemPickerButton(String toolTip, ArrayList labels,
        ArrayList values) {

      final HashMap labelsToValues = new HashMap();
      HashSet masks = new HashSet();
      ArrayList formattedLabels = new ArrayList();

      for (int i = 0; i < labels.size(); i++) {
        labelsToValues.put(labels.get(i), values.get(i));
        String beginHTML = getBeginHTML(i);
        String endHTML = getEndHTML(i);
        masks.add(beginHTML);
        masks.add(endHTML);
        String fullHTML = beginHTML + labels.get(i) + endHTML;
        formattedLabels.add(fullHTML);
      }
      Suggest.DefaultOracle oracle = new Suggest.DefaultOracle(masks.iterator());
      oracle.addAll(formattedLabels.iterator());
      SuggestItemPickerButtonImpl button = new SuggestItemPickerButtonImpl(
          oracle);

      button.setDefaultSuggestions(formattedLabels);
      customizeButton(button);
      initButton(button, toolTip);
      final ItemPicker picker = button.getItemPicker();
      picker.addChangeListener(new ChangeListener() {
        public void onChange(Widget sender) {
          Object realValue = labelsToValues.get(picker.getSelectedValue());
          if (realValue == null) {
            throw new RuntimeException(picker.getSelectedValue()
                + " was not a known values, known values are "
                + labelsToValues.keySet());
          }
          change(realValue);
        }
      });
    }

    /**
     * A callback to use when the user selects a value.
     * 
     * @param object the selection made
     */
    abstract void change(Object object);

    abstract void customizeButton(SuggestItemPickerButtonImpl button);

    abstract String getBeginHTML(int i);

    abstract String getEndHTML(int i);
  }

  private class SpellCheckControl {
    private SpellCheck spellCheck;
    private HTML spell;
    private HTML noMisspellingsFound;
    private Panel recheckSpelling;
    private HTML checking;

    {
      setup();
    }

    Iterator addHighlights(Iterator words, HighlightCategory category) {
      return richTextArea.addHighlights(words, category);
    }

    SpellCheck getSpellCheck() {
      return spellCheck;
    }

    String getText() {
      return richTextArea.getText();
    }

    /**
     * The widget to be used to start a spellcheck.
     * 
     * @param widget the widget to be used to start a spellcheck
     */
    void setSpellCheckWidget(Widget widget) {
      layout.setWidget(0, 1, widget);
    }

    private void setup() {
      // Spelling widget.
      spell = new HTML(labelProvider.spellingAction());
      ClickListener requestSpellCheck = new ClickListener() {
        public void onClick(Widget sender) {
          spellCheck.requestSpellCheck();
        }
      };

      spell.addClickListener(requestSpellCheck);
      spell.setStyleName("spellCheck");

      // Checking
      checking = new HTML(labelProvider.checkingStatus());
      checking.setStyleName("spellCheck");

      // Misspelling widget.
      noMisspellingsFound = new HTML(labelProvider.noMisspellingsFoundStatus());
      noMisspellingsFound.setStyleName("noMisspellings");

      // Recheck widget.
      recheckSpelling = new HorizontalPanel();
      recheckSpelling.addStyleName("spellCheck");

      HTML recheck = new HTML(labelProvider.reheckAction());
      recheck.setStyleName("misspelledWord");
      recheck.addClickListener(requestSpellCheck);
      recheckSpelling.add(recheck);

      HTML spellCheckDone = new HTML(labelProvider.doneAction());
      spellCheckDone.setStyleName("spellCheck");
      spellCheckDone.addClickListener(new ClickListener() {
        public void onClick(Widget sender) {
          spellCheck.finishSpellCheck();
        }
      });
      recheckSpelling.add(spellCheckDone);
      setSpellCheckWidget(spell);

      setupSpellCheck();
    }

    private void setupSpellCheck() {
      SpellCheck.LabelProvider spellCheckLabels = new SpellCheck.LabelProvider() {
        public String noSuggestions() {
          return labelProvider.noSuggestions();
        }
      };

      SpellCheck.StateListener listener = new SpellCheck.StateListener() {
        public void onChange(SpellCheck.State state) {
          if (state == SpellCheck.State.NO_MISSPELLING) {
            setSpellCheckWidget(noMisspellingsFound);
          } else if (state == SpellCheck.State.RECHECK) {
            setSpellCheckWidget(recheckSpelling);
          } else if (state == SpellCheck.State.SPELLCHECK) {
            setSpellCheckWidget(spell);
          } else if (state == SpellCheck.State.CHECKING) {
            setSpellCheckWidget(checking);
          } else {
            throw new RuntimeException("Unknown state: " + state);
          }
        }
      };
      spellCheck = new SpellCheck(richTextArea, spellCheckLabels, listener);
    }
  }

  private int numButtonsPerRow = 20;

  private SpellCheckControl spellCheckControl;

  private ButtonFaces buttonFaces;

  private LabelProvider labelProvider;

  private HorizontalPanel buttons;

  private List listeningButtons = new ArrayList();

  // Using FlexTable for layout as HorizontalPanel had a weird alignment bug,
  // spelling would not align correctly.
  private FlexTable layout;

  public Oracle getSpellCheckOracle() {
    return this.spellCheckControl.spellCheck.getSpellCheckOracle();
  }

  public void setNumberOfButtonsPerRow(int numberOfButtons) {
    this.numButtonsPerRow = numberOfButtons;
  }

  void addListenersToRichText() {
    richTextArea.addKeyboardListener(new KeyboardListenerAdapter() {

      public void onKeyUp(Widget sender, char keyCode, int modifiers) {
        updateImages();
      }
    });

    richTextArea.addMouseListener(new MouseListenerAdapter() {
      public void onMouseUp(Widget sender, int x, int y) {
        updateImages();
      }
    });
  }

  /**
   * Adds the buttons to the toolbar.
   */
  void createFormattingButtons() {
    addBold();
    addItalics();
    addUnderline();
    addIndentButtons();
    addFontFamily();
    addFontSize();
    addFontColor();
    addBackColor();
    addLink();
    addHRuleButton();
    addAlignments();
    addLists();
    addFormatBlock();
  }

  void createToolBar(RichTextEditor editor, Panel root) {
    layout = new FlexTable();
    layout.setCellPadding(0);
    layout.setCellSpacing(0);
    layout.setStyleName("toolBar");
    root.add(layout);
    createFormattingButtons();

    spellCheckControl = new SpellCheckControl();
    spellCheckControl.setup();

    layout.getCellFormatter().setHorizontalAlignment(0, 1,
        HasHorizontalAlignment.ALIGN_RIGHT);
  }

  LabelProvider getLabels() {
    return labelProvider;
  }

  SpellCheck getSpellCheck() {
    return spellCheckControl.getSpellCheck();
  }

  void setButtonProvider(ButtonFaces buttonProvider) {
    this.buttonFaces = buttonProvider;
  }

  void setLabels(LabelProvider labelProvider) {
    this.labelProvider = labelProvider;
  }

  void setSpellCheckModel(Oracle spellCheckModel) {
    spellCheckControl.getSpellCheck().setOracle(spellCheckModel);
  }

  void updateImages() {
    for (int i = 0; i < listeningButtons.size(); i++) {
      HookupListeningButton factory = (HookupListeningButton) listeningButtons.get(i);
      factory.updateButton();
    }
  }

  void useDefaultButtonProvider() {
    buttonFaces = new ButtonCustomizerImpl();
  }

  void useDefaultTextProvider() {
    this.labelProvider = (LabelProvider) GWT.create(LabelProvider.class);
  }

  /**
   * Adds left, right and center text alignment buttons.
   */
  private void addAlignments() {

    final ToggleButton justifyLeft = new ToggleButton();
    buttonFaces.justifyLeft(justifyLeft);
    final ArrayList justifyButtons = new ArrayList();

    class HookupAlignButton extends HookupListeningButton {
      RichTextArea.Justification justify;

      HookupAlignButton(ToggleButton button,
          RichTextArea.Justification justify, String tooltip) {
        super(button, tooltip);
        this.justify = justify;
        justifyButtons.add(this);
      }

      void click() {
        richTextArea.setJustification(justify);
      }

      void onClick() {
        click();
        updateAllButtons();
      }

      void updateAllButtons() {
        for (int i = 0; i < justifyButtons.size(); i++) {
          ((HookupAlignButton) justifyButtons.get(i)).updateButton();
        }
      }

      void updateButton() {
        button.setDown(richTextArea.getJustification() == justify);
      }
    }
    new HookupAlignButton(justifyLeft, Justification.LEFT,
        labelProvider.alignLeftIconText());

    ToggleButton justifyCenter = new ToggleButton();
    buttonFaces.justifyCenter(justifyCenter);

    new HookupAlignButton(justifyCenter, Justification.CENTER,
        labelProvider.alignCenterIconText());

    ToggleButton justifyRight = new ToggleButton();
    buttonFaces.justifyRight(justifyRight);

    new HookupAlignButton(justifyRight, Justification.RIGHT,
        labelProvider.alignRightIconText());
  }

  /**
   * Adds button to change the color of the background color.
   */
  private void addBackColor() {
    ColorPicker colorPopup = new ColorPicker();
    ItemPickerButtonImpl button = new ItemPickerButtonImpl(colorPopup);
    buttonFaces.backgroundColor(button);

    new HookupItemPickerButton(button, labelProvider.fontColorIconText()) {
      void change(Object selected) {
        String color = (String) selected;
        richTextArea.setBackColor(color);
      }
    };
  }

  /**
   * Adds a button to toggle bold.
   */
  private void addBold() {
    ToggleButton button = new ToggleButton();
    buttonFaces.bold(button);
    new HookupListeningButton(button, labelProvider.boldIconText()) {
      void click() {
        richTextArea.toggleBold();
      }

      void updateButton() {
        button.setDown(richTextArea.isBold());
      }
    };
  }

  private void addFontColor() {

    ColorPicker colorPopup = new ColorPicker();
    ItemPickerButtonImpl button = new ItemPickerButtonImpl(colorPopup);
    buttonFaces.fontColor(button);

    new HookupItemPickerButton(button, labelProvider.fontColorIconText()) {
      void change(Object selected) {
        String color = (String) selected;
        richTextArea.setForeColor(color);
      }
    };
  }

  private void addFontFamily() {
    // Font family.

    ArrayList labels = new ArrayList();
    final ArrayList values = new ArrayList();

    labels.add(labelProvider.fontTimesNewRoman());
    values.add("times,serif");

    labels.add(labelProvider.fontArial());
    values.add("arial, sans-serif");

    labels.add(labelProvider.fontCourierNew());
    values.add("Courier new, monospace");

    labels.add(labelProvider.fontCursive());
    values.add("cursive");

    new HookupSuggestItemPickerButton(labelProvider.fontIconText(), labels,
        values) {
      void change(Object selected) {
        String value = (String) selected;
        richTextArea.setFontName(value);
      }

      void customizeButton(SuggestItemPickerButtonImpl button) {
        buttonFaces.fontFamily(button);
      }

      String getBeginHTML(int i) {
        return "<div style = 'font-family: " + values.get(i) + "'>";
      }

      String getEndHTML(int i) {
        return "</div>";
      }
    };
  }

  private void addFontSize() {
    final ArrayList htmlFormat = new ArrayList();
    ArrayList labels = new ArrayList();
    ArrayList values = new ArrayList();

    labels.add(labelProvider.fontSizeSmall());
    htmlFormat.add("50");
    values.add(RichTextArea.FontSize.SIZE_1);

    labels.add(labelProvider.fontSizeNormal());
    htmlFormat.add("100");
    values.add(RichTextArea.FontSize.SIZE_3);

    labels.add(labelProvider.fontSizeLarge());
    htmlFormat.add("170");
    values.add(RichTextArea.FontSize.SIZE_5);

    labels.add(labelProvider.fontSizeHuge());
    htmlFormat.add("250");
    values.add(RichTextArea.FontSize.SIZE_7);

    new HookupSuggestItemPickerButton(labelProvider.fontSizeIconText(), labels,
        values) {
      void change(Object selected) {
        FontSize font = (FontSize) selected;
        richTextArea.setFontSize(font);
      }

      void customizeButton(SuggestItemPickerButtonImpl button) {
        buttonFaces.fontSize(button);
      }

      String getBeginHTML(int index) {
        return "<div style='font-size: " + htmlFormat.get(index) + "%'>";
      }

      String getEndHTML(int i) {
        return "</div>";
      }

    };
  }

  private void addFormatBlock() {
    ArrayList labels = new ArrayList();
    final ArrayList values = new ArrayList();

    labels.add(labelProvider.h1FormatStyle());
    values.add(RichTextArea.BlockFormat.H1);

    labels.add(labelProvider.h2FormatStyle());
    values.add(RichTextArea.BlockFormat.H2);

    labels.add(labelProvider.h3FormatStyle());
    values.add(RichTextArea.BlockFormat.H3);

    labels.add(labelProvider.h4FormatStyle());
    values.add(RichTextArea.BlockFormat.H4);

    labels.add(labelProvider.h5FormatStyle());
    values.add(RichTextArea.BlockFormat.H5);

    labels.add(labelProvider.h6FormatStyle());
    values.add(RichTextArea.BlockFormat.H6);

    labels.add(labelProvider.preFormatStyle());
    values.add(RichTextArea.BlockFormat.PRE);

    labels.add(labelProvider.paragraphFormatStyle());
    values.add(RichTextArea.BlockFormat.PARAGRAPH);

    labels.add(labelProvider.addressFormatStyle());
    values.add(RichTextArea.BlockFormat.ADDRESS);

    new HookupSuggestItemPickerButton(labelProvider.blockStyleIconText(),
        labels, values) {
      void change(Object selected) {
        BlockFormat blockFormat = (BlockFormat) selected;
        richTextArea.formatBlock(blockFormat);
      }

      void customizeButton(SuggestItemPickerButtonImpl button) {
        buttonFaces.blockStyle(button);
      }

      String getBeginHTML(int i) {
        return "<" + values.get(i) + ">";
      }

      String getEndHTML(int i) {
        return "</" + values.get(i) + ">";
      }
    };
  }

  private void addHRuleButton() {
    PushButton button = new PushButton();
    buttonFaces.insertHRule(button);
    new HookupPushButton(button, labelProvider.insertHRuleIconText()) {
      void click() {
        richTextArea.insertHorizontalRule();
      }

    };
  }

  private void addIndentButtons() {
    PushButton rightIndentButton = new PushButton();
    buttonFaces.rightIndent(rightIndentButton);
    new HookupPushButton(rightIndentButton, labelProvider.indentIconText()) {
      void click() {
        richTextArea.rightIndent();
      }
    };

    PushButton leftIndentButton = new PushButton();
    buttonFaces.leftIndent(leftIndentButton);
    new HookupPushButton(leftIndentButton, labelProvider.outdentIconText()) {
      void click() {
        richTextArea.leftIndent();
      }
    };
  }

  private void addItalics() {
    ToggleButton button = new ToggleButton();
    buttonFaces.italics(button);
    new HookupListeningButton(button, labelProvider.italicsIconText()) {
      void click() {
        richTextArea.toggleItalic();
        updateButton();
      }

      void updateButton() {
        button.setDown(richTextArea.isItalic());
      }
    };
  }

  private void addLink() {
    PushButton button = new PushButton();
    buttonFaces.link(button);
    new HookupPushButton(button, labelProvider.htmlLinkIconText()) {
      void click() {
        String selected = richTextArea.getSelectedHTML();
        if (selected == null || selected.length() == 0) {
          Window.alert(labelProvider.linkNeedSelection());
          return;
        }

        // Most people do not add multiple links to their docs, so it should be
        // cheaper to create then throw away then caching.
        final PopupPanel p = new PopupPanel(true);
        FlexTable table = new FlexTable();
        p.setWidget(table);
        table.setText(0, 0, labelProvider.linkEnterText());
        final TextBox url = new TextBox();
        url.addKeyboardListener(new KeyboardListenerAdapter() {

          public void onKeyPress(Widget sender, char keyCode, int modifiers) {
            if (keyCode == KEY_ENTER) {
              richTextArea.createLink(url.getText());
              p.hide();
            }
          }

        });
        url.setText("http://");
        table.setWidget(0, 1, url);

        p.setPopupPosition(richTextArea.getAbsoluteLeft(),
            richTextArea.getAbsoluteTop());
        p.setStyleName("gwt-RichTextEditor-LinkPopup");

        p.show();
      }
    };
  }

  private void addLists() {
    PushButton button = new PushButton();
    buttonFaces.bulletList(button);
    new HookupPushButton(button, labelProvider.bulletedListIconText()) {
      void click() {
        richTextArea.insertUnorderedList();
      }
    };

    PushButton orderedList = new PushButton();
    buttonFaces.orderedList(orderedList);
    new HookupPushButton(orderedList, labelProvider.orderedListIconText()) {
      void click() {
        richTextArea.insertOrderedList();
      }
    };
  }

  private void addUnderline() {
    ToggleButton button = new ToggleButton();
    buttonFaces.underline(button);
    new HookupListeningButton(button, labelProvider.underlineIconText()) {
      void click() {
        richTextArea.toggleUnderline();
      }

      void updateButton() {
        button.setDown(richTextArea.isUnderlined());
      }
    };
  }

  private void initButton(CustomButton button, String tooltip) {
    button.setTitle(tooltip);
    if (buttons == null || buttons.getWidgetCount() == numButtonsPerRow) {
      buttons = new HorizontalPanel();
      buttons.setWidth("100%");
      layout.setWidget(layout.getRowCount(), 0, buttons);
    }
    buttons.add(button);
  }
}
