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
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.ClippedImage;
import com.google.gwt.user.client.ui.ColorPickerPopup;
import com.google.gwt.user.client.ui.CustomButton;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.MouseListenerAdapter;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SelectablePopup;
import com.google.gwt.user.client.ui.SelectablePopupButton;
import com.google.gwt.user.client.ui.SuggestionsPopup;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.richtext.RichTextArea.BlockFormat;
import com.google.gwt.user.client.ui.richtext.RichTextArea.FontSize;
import com.google.gwt.user.client.ui.richtext.RichTextEditor.ButtonProvider;
import com.google.gwt.user.client.ui.richtext.RichTextEditor.LabelProvider;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Standard Editor for rich text.
 */
public class RichTextEditorImplStandard extends RichTextEditorImpl {
  /**
   * Default button provider.
   * 
   */
  public static class ButtonProviderImpl implements ButtonProvider {

    /**
     * Images for the default button case.
     * 
     */
    public interface Images extends ImageBundle {

      /**
       * @gwt.resource com/google/gwt/user/client/ui/richtext/style-button-depressed-small.gif
       */
      public ClippedImage blockStyleDown();

      /**
       * @gwt.resource com/google/gwt/user/client/ui/richtext/style-button-small.gif
       */
      public ClippedImage blockStyleUp();

      /**
       * @gwt.resource com/google/gwt/user/client/ui/richtext/bold-button-small.gif
       */
      public ClippedImage boldDown();

      /**
       * @gwt.resource com/google/gwt/user/client/ui/richtext/bold-button-depressed-small.gif
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

    public SelectablePopupButton backgroundColor(ColorPickerPopup popup) {
      return new SelectablePopupButton(images.colorUp(), images.colorDown(),
          popup);
    }

    public SelectablePopupButton blockStyle(SuggestionsPopup popup) {
      return new SelectablePopupButton(images.blockStyleUp(),
          images.blockStyleDown(), popup);
    };

    public CustomButton bold() {
      return new CustomButton(images.boldUp(), images.boldDown());
    }

    public CustomButton bulletList() {
      return new CustomButton(images.listUp(), images.listDown());
    }

    public SelectablePopupButton fontColor(ColorPickerPopup popup) {
      return new SelectablePopupButton(images.colorUp(), images.colorDown(),
          popup);
    };

    public SelectablePopupButton fontFamily(SuggestionsPopup popup) {
      return new SelectablePopupButton(images.fontUp(), images.fontDown(), popup);
    }

    public SelectablePopupButton fontSize(SuggestionsPopup popup) {
      return new SelectablePopupButton(images.sizeUp(), images.sizeDown(),
          popup);
    }

    public CustomButton insertHRule() {
      return new CustomButton(images.insertHRuleUp(), images.insertHRuleDown());
    }

    public CustomButton italics() {
      return new CustomButton(images.italicUp(), images.italicDown());
    }

    public CustomButton justifyCenter() {
      return new CustomButton(images.justifyCenterUp(),
          images.justifyCenterDown());
    }

    public CustomButton justifyLeft() {
      return new CustomButton(images.justifyLeftUp(), images.justifyLeftDown());
    }

    public CustomButton justifyRight() {
      return new CustomButton(images.justifyRightUp(),
          images.justifyRightDown());
    }

    public CustomButton leftIndent() {
      return new CustomButton(images.indentLeftUp(), images.indentLeftDown());
    }

    public CustomButton link() {
      return new CustomButton(images.linkUp(), images.linkDown());
    }

    public CustomButton orderedList() {
      return new CustomButton(images.orderedListUp(), images.orderedListDown());
    }

    public CustomButton rightIndent() {
      return new CustomButton(images.indentRightUp(), images.indentRightDown());
    }

    public CustomButton underline() {
      return new CustomButton(images.underlineUp(), images.underlineDown());
    }
  }

  /**
   * Hook up a push button to be a drop down push button.
   * 
   */
  private abstract class HookupDropDownButton {
    /**
     * Wires a tooltip and a popup to a button.
     * 
     * @param button the button to use
     * @param toolTip the tooltip to give the button
     * @param popup the popup to use to get the desired choice
     */
    public HookupDropDownButton(final CustomButton button, String toolTip,
        final SelectablePopup popup) {
      final SelectablePopupButton dropDown = (SelectablePopupButton) button;
      button.setTitle(toolTip);
      popup.addChangeListener(new ChangeListener() {
        public void onChange(Widget sender) {
          change(popup.getSelectedValue());
        }
      });
      buttons.add(dropDown);
    }

    /**
     * A callback to use when the user selects a value.
     * 
     * @param object the selection made
     */
    protected abstract void change(Object object);
  }

  /**
   * Hook up a push button with a rich text state.
   * 
   */
  private abstract class HookupListeningButton {
    CustomButton button;

    /**
     * Wires a tooltip to a button, and makes it adjust to the underlying state.
     * 
     * @param button the button to use
     * @param toolTip the tooltip to give the button
     */
    public HookupListeningButton(final CustomButton button, String toolTip) {
      this.button = button;
      // Click Listener
      button.addClickListener(new ClickListener() {
        public void onClick(Widget sender) {
          toggle();
          updateButton();
        }
      });
      buttons.add(button);
      listeningButtons.add(this);
      button.setTitle(toolTip);
    }

    /**
     * The callback to perform when the button is toggled.
     */
    public abstract void toggle();

    /**
     * The callback responsible for updating the button to match the underlying
     * state.
     */
    public abstract void updateButton();
  }

  /**
   * Hook up a push button.
   */
  private abstract class HookupPressButton {

    HookupPressButton(final CustomButton button, String toolTip) {
      button.setToggleBehavior(false);
      buttons.add(button);
      button.setTitle(toolTip);
      button.addClickListener(new ClickListener() {
        public void onClick(Widget sender) {
          toggle();
        }
      });
    }

    /**
     * The callback to perform when the button is toggled.
     */
    public abstract void toggle();
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

    public Iterator addHighlights(List words, HighlightCategory category) {
      return richTextArea.addHighlights(words, category);
    }

    public SuggestionsPopup getNoSuggestionsPopup() {
      SuggestionsPopup noSuggestions = new SuggestionsPopup();
      List noSuggestionsList = new ArrayList();
      noSuggestionsList.add(labelProvider.noSuggestions());
      noSuggestions.setItems(noSuggestionsList);
      noSuggestions.setStyleName("gwt-RichTextEditor-NoSuggestions");
      return noSuggestions;
    }

    public SpellCheck getSpellCheck() {
      return spellCheck;
    }

    public String getText() {
      return richTextArea.getText();
    }

    /**
     * The widget to be used to start a spellcheck.
     * 
     * @param widget the widget to be used to start a spellcheck
     */
    protected void setSpellCheckWidget(Widget widget) {
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
      spellCheck = new SpellCheck(richTextArea, labelProvider,
          new SpellCheck.StateListener() {

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
          });
    }
  }

  SpellCheckControl spellCheckWidget;

  private ButtonProvider buttonProvider;

  private LabelProvider labelProvider;

  private HorizontalPanel buttons;
  private List listeningButtons = new ArrayList();

  // Using FlexTable for layout as HorizontalPanel had a weird alignment bug,
  // spelling would not align correctly.
  private FlexTable layout;

  public void addListenersToRichText() {
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

  public void createToolBar(RichTextEditor editor, Panel root) {
    layout = new FlexTable();
    layout.setCellPadding(0);
    layout.setCellSpacing(0);
    layout.setStyleName("toolBar");
    root.add(layout);

    createFormattingButtons();

    layout.setWidget(0, 0, buttons);

    spellCheckWidget = new SpellCheckControl();
    spellCheckWidget.setup();

    layout.getCellFormatter().setHorizontalAlignment(0, 1,
        HasHorizontalAlignment.ALIGN_RIGHT);
  }

  // Used only to
  public SpellCheck getSpellCheck() {
    return spellCheckWidget.getSpellCheck();
  }

  public LabelProvider getTextProvider() {
    return labelProvider;
  }

  public void setButtonProvider(ButtonProvider buttonProvider) {
    this.buttonProvider = buttonProvider;
  }

  public void setTextProvider(LabelProvider labelProvider) {
    this.labelProvider = labelProvider;
  }

  public void updateImages() {
    for (int i = 0; i < listeningButtons.size(); i++) {
      HookupListeningButton factory = (HookupListeningButton) listeningButtons.get(i);
      factory.updateButton();
    }
  }

  public void useDefaultButtonProvider() {
    buttonProvider = new ButtonProviderImpl();
  }

  public void useDefaultTextProvider() {
    this.labelProvider = (LabelProvider) GWT.create(LabelProvider.class);
  }

  /**
   * Adds the buttons to the toolbar.
   */
  protected void createFormattingButtons() {
    buttons = new HorizontalPanel();
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

  void setSpellCheckModel(SpellCheck.Model spellCheckModel) {
    spellCheckWidget.getSpellCheck().setModel(spellCheckModel);
  }

  /**
   * Adds left, right and center text alignment buttons.
   */
  private void addAlignments() {
    new HookupPressButton(buttonProvider.justifyLeft(),
        labelProvider.alignLeftIconText()) {
      public void toggle() {
        richTextArea.setJustification(RichTextArea.Justification.LEFT);
      }
    };

    new HookupPressButton(buttonProvider.justifyCenter(),
        labelProvider.alignCenterIconText()) {
      public void toggle() {
        richTextArea.setJustification(RichTextArea.Justification.CENTER);
      }
    };

    new HookupPressButton(buttonProvider.justifyRight(),
        labelProvider.alignRightIconText()) {
      public void toggle() {
        richTextArea.setJustification(RichTextArea.Justification.RIGHT);
      }
    };
  }

  /**
   * Adds button to change the color of the background color.
   */
  private void addBackColor() {
    ColorPickerPopup colorPopup = new ColorPickerPopup();
    new HookupDropDownButton(buttonProvider.backgroundColor(colorPopup),
        labelProvider.fontColorIconText(), colorPopup) {
      public void change(Object selected) {
        String color = (String) selected;
        richTextArea.setBackColor(color);
      }
    };
  }

  /**
   * Adds a button to toggle bold.
   */
  private void addBold() {
    new HookupListeningButton(buttonProvider.bold(),
        labelProvider.boldIconText()) {
      public void toggle() {
        richTextArea.toggleBold();
      }

      public void updateButton() {
        button.setPressed(richTextArea.isBold());
      }
    };
  }

  private void addFontColor() {

    ColorPickerPopup colorPopup = new ColorPickerPopup();

    new HookupDropDownButton(buttonProvider.fontColor(colorPopup),
        labelProvider.fontColorIconText(), colorPopup) {
      public void change(Object selected) {
        String color = (String) selected;
        richTextArea.setForeColor(color);
      }
    };
  }

  private void addFontFamily() {
    // Font family.
    ArrayList labels = new ArrayList();
    ArrayList values = new ArrayList();

    labels.add("<div style='font-family:times, serif '>"
        + labelProvider.fontTimesNewRoman() + "</div>");
    values.add("times,serif");

    labels.add("<div style='font-family:arial, sans-serif'>"
        + labelProvider.fontArial() + "</div>");
    values.add("arial, sans-serif");

    labels.add("<div style='font-family:Courier new, monospace'>"
        + labelProvider.fontCourierNew() + "</div>");
    values.add("Courier new, monospace");

    labels.add("<div style='font-family:cursive'>"
        + labelProvider.fontCursive() + "</div>");
    values.add("cursive");

    SuggestionsPopup popup = new SuggestionsPopup();
    popup.setItems(labels);
    popup.setValues(values);
    new HookupDropDownButton(buttonProvider.fontFamily(popup),
        labelProvider.fontIconText(), popup) {
      public void change(Object selected) {
        String value = (String) selected;
        richTextArea.setFontName(value);
      }
    };
  }

  private void addFontSize() {
    ArrayList labels = new ArrayList();
    ArrayList values = new ArrayList();

    labels.add("<div style='font-size:50%'>" + labelProvider.fontSizeSmall()
        + "</div>");
    values.add(RichTextArea.FontSize.SIZE_1);

    labels.add("<div style='font-size:100%'>" + labelProvider.fontSizeNormal()
        + "</div>");
    values.add(RichTextArea.FontSize.SIZE_3);

    labels.add("<div style='font-size:170%'>" + labelProvider.fontSizeLarge()
        + "</div>");
    values.add(RichTextArea.FontSize.SIZE_5);
    labels.add("<div style='font-size:250%'>" + labelProvider.fontSizeHuge()
        + "</div>");
    values.add(RichTextArea.FontSize.SIZE_7);

    SuggestionsPopup popup = new SuggestionsPopup();
    popup.setItems(labels);
    popup.setValues(values);
    new HookupDropDownButton(buttonProvider.fontSize(popup),
        labelProvider.fontSizeIconText(), popup) {
      public void change(Object selected) {
        FontSize font = (FontSize) selected;
        richTextArea.setFontSize(font);
      }
    };
  }

  private void addFormatBlock() {
    ArrayList labels = new ArrayList();
    ArrayList values = new ArrayList();

    labels.add("<h1>" + labelProvider.h1FormatStyle() + "</h1>");
    values.add(RichTextArea.BlockFormat.H1);
    labels.add("<h2>" + labelProvider.h2FormatStyle() + "</h2>");
    values.add(RichTextArea.BlockFormat.H2);
    labels.add("<h3>" + labelProvider.h3FormatStyle() + "</h3>");
    values.add(RichTextArea.BlockFormat.H3);
    labels.add("<h4>" + labelProvider.h4FormatStyle() + "</h4>");
    values.add(RichTextArea.BlockFormat.H4);
    labels.add("<h5>" + labelProvider.h5FormatStyle() + "</h5>");
    values.add(RichTextArea.BlockFormat.H5);
    labels.add("<h6>" + labelProvider.h6FormatStyle() + "</h6>");
    values.add(RichTextArea.BlockFormat.H6);
    labels.add("<pre>" + labelProvider.preFormatStyle() + "</pre>");
    values.add(RichTextArea.BlockFormat.PRE);
    labels.add("<p>" + labelProvider.paragraphFormatStyle() + "</p>");
    values.add(RichTextArea.BlockFormat.PARAGRAPH);
    labels.add("<address>" + labelProvider.addressFormatStyle() + "</address>");
    values.add(RichTextArea.BlockFormat.ADDRESS);
    SuggestionsPopup popup = new SuggestionsPopup();
    popup.setItems(labels);
    popup.setValues(values);
    new HookupDropDownButton(buttonProvider.blockStyle(popup),
        labelProvider.blockStyleIconText(), popup) {
      public void change(Object selected) {
        BlockFormat blockFormat = (BlockFormat) selected;
        richTextArea.formatBlock(blockFormat);
      }
    };
  }

  private void addHRuleButton() {
    new HookupListeningButton(buttonProvider.insertHRule(),
        labelProvider.insertHRuleIconText()) {
      public void toggle() {
        richTextArea.insertHorizontalRule();
      }

      public void updateButton() {
      }
    };
  }

  private void addIndentButtons() {
    new HookupPressButton(buttonProvider.rightIndent(),
        labelProvider.indentIconText()) {
      public void toggle() {
        richTextArea.rightIndent();
      }
    };
    new HookupPressButton(buttonProvider.leftIndent(),
        labelProvider.outdentIconText()) {
      public void toggle() {
        richTextArea.leftIndent();
      }
    };
  }

  private void addItalics() {
    new HookupListeningButton(buttonProvider.italics(),
        labelProvider.italicsIconText()) {
      public void toggle() {
        richTextArea.toggleItalic();
        updateButton();
      }

      public void updateButton() {
        button.setPressed(richTextArea.isItalic());
      }
    };
  }

  private void addLink() {
    new HookupPressButton(buttonProvider.link(),
        labelProvider.htmlLinkIconText()) {
      public void toggle() {
        richTextArea.createLink(richTextArea.getSelectedHTML());
      }
    };
  }

  private void addLists() {
    new HookupPressButton(buttonProvider.bulletList(),
        labelProvider.bulletedListIconText()) {
      public void toggle() {
        richTextArea.insertUnorderedList();
      }
    };
    new HookupPressButton(buttonProvider.orderedList(),
        labelProvider.orderedListIconText()) {
      public void toggle() {
        richTextArea.insertOrderedList();
      }
    };
  }

  private void addUnderline() {
    new HookupListeningButton(buttonProvider.underline(),
        labelProvider.underlineIconText()) {
      public void toggle() {
        richTextArea.toggleUnderline();
      }

      public void updateButton() {
        button.setPressed(richTextArea.isUnderlined());
      }
    };
  }
}
