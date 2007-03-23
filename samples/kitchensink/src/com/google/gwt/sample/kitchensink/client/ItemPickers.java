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

package com.google.gwt.sample.kitchensink.client;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.ColorPicker;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ItemPicker;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PopupListener;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Suggest;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestPicker;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Demonstrates {@link com.google.gwt.user.client.ui.MenuBar} and
 * {@link com.google.gwt.user.client.ui.MenuItem}.
 */
public class ItemPickers extends Sink {

  public static SinkInfo init() {
    return new SinkInfo(
        "Pickers",
        "GWT's <code>ItemPicker</code> and related classes make it easy to create rich user selection widgets.") {
      public Sink createInstance() {
        return new ItemPickers();
      }
    };
  }

  ItemPickers() {
    // Format the display.
    Grid grid = new Grid(2, 2);
    grid.setWidth("100%");
    grid.getColumnFormatter().setWidth(0, "50%");
    grid.getColumnFormatter().setWidth(1, "50%");
    grid.setCellSpacing(30);

    // Add demos.
    VerticalPanel demo1 = new VerticalPanel();
    demo1.setSpacing(8);
    grid.setWidget(0, 0, demo1);
    sampleSuggestPicker(demo1);

    VerticalPanel demo2 = new VerticalPanel();
    demo2.setSpacing(8);
    grid.setWidget(0, 1, demo2);
    sampleColorPicker(demo2);

    VerticalPanel demo3 = new VerticalPanel();
    demo3.setSpacing(8);
    grid.setWidget(1, 0, demo3);
    sampleUSStates(demo3);

    VerticalPanel demo4 = new VerticalPanel();
    demo4.setSpacing(8);
    grid.setWidget(1, 1, demo4);
    sampleColorPickerButton(demo4);
    initWidget(grid);
  }

  /**
   * Glues a {@link ToggleButton}, {@link PopupPanel}, and {@link ItemPicker}
   * together in order to create a virtual ItemPickerButton.
   */
  private ToggleButton createItemPickerButton(final ItemPicker picker) {
    final ToggleButton showBelow = new ToggleButton() {
      final PopupPanel itemPickerPopup = new PopupPanel(true);
      {
        // Adds the item picker.
        itemPickerPopup.setWidget((Widget) picker);

        // Toggles menu based on button click.
        addClickListener(new ClickListener() {
          public void onClick(Widget sender) {
            show();
          }
        });

        // Hide the popup after the user selects an item.
        picker.addChangeListener(new ChangeListener() {
          public void onChange(Widget sender) {
            itemPickerPopup.hide();
          }
        });

        // Button should be up if popup is closed.
        itemPickerPopup.addPopupListener(new PopupListener() {
          public void onPopupClosed(PopupPanel sender, boolean autoClosed) {
            setDown(false);
          }
        });

        // Hook up keyboard events.
        this.addKeyboardListener(new KeyboardListenerAdapter() {
          public void onKeyDown(Widget sender, char keyCode, int modifiers) {
            if (itemPickerPopup.isAttached() == false) {
              show();
            } else if (keyCode == KEY_ESCAPE) {
              itemPickerPopup.hide();
            } else {
              picker.delegateKeyDown(keyCode);
            }
          }
        });
      }

      public void show() {
        // Note, production code should always check for off-of-screen errors,
        // so this code should never be used in a production environment.

        // Calculate left.
        int left = this.getAbsoluteLeft();

        // Calculate top.
        int top = this.getAbsoluteTop() + this.getOffsetHeight();

        // Set the popup position.
        itemPickerPopup.setPopupPosition(left, top);
        itemPickerPopup.show();
      }

    };
    return showBelow;
  }

  /**
   * Wraps an {@link ItemPicker} in a {@link FocusPanel}
   */
  private FocusPanel createItemPickerPanel(final ItemPicker picker) {
    // Creates the panel.
    FocusPanel focus = new FocusPanel();

    // Adds the item picker.
    focus.setWidget((Widget) picker);
    DOM.setStyleAttribute(focus.getWidget().getElement(), "border",
        "2px solid navy");

    // Hook up keyboard events.
    focus.addKeyboardListener(new KeyboardListenerAdapter() {
      public void onKeyDown(Widget sender, char keyCode, int modifiers) {
        picker.delegateKeyDown(keyCode);
      }
    });
    return focus;

  }

  private void sampleColorPicker(Panel panel) {
    final HTML colorPickerText = new HTML("Color Picker");
    panel.add(colorPickerText);
    final ColorPicker picker = new ColorPicker();

    // Add a change listener to color the "Color" string.
    picker.addChangeListener(new ChangeListener() {
      public void onChange(Widget sender) {
        colorPickerText.setHTML("<span style='color:"
            + picker.getSelectedValue() + "'>Color</span> Picker");
      }
    });
    panel.add(createItemPickerPanel(picker));

  }

  private void sampleColorPickerButton(Panel panel) {
    panel.add(new HTML("Color Picker Button"));
    final ColorPicker picker = new ColorPicker();
    final ToggleButton itemPickerButton = createItemPickerButton(picker);
    itemPickerButton.getUpFace().setHTML("Pick a <strong>color</strong>");
    itemPickerButton.getDownFace().setHTML("Picking...");

    // Add a change listener to color the "color" string.
    picker.addChangeListener(new ChangeListener() {
      public void onChange(Widget sender) {
        itemPickerButton.getUpFace().setHTML(
            "Pick a <strong style='color:" + picker.getSelectedValue()
                + "'>color</strong>");
      }
    });
    panel.add(itemPickerButton);
  }

  private void sampleSuggestPicker(Panel panel) {
    final HTML label = new HTML("Font Picker");
    panel.add(label);
    final SuggestPicker picker = new SuggestPicker();

    // Create the items to go in the suggest picker.
    ArrayList items = new ArrayList();
    final String template = "<div style ='font-family:X'>X</div>";
    items.add(template.replaceAll("X", "Times"));
    items.add(template.replaceAll("X", "Courier new"));
    items.add(template.replaceAll("X", "Arial"));
    items.add(template.replaceAll("X", "Cursive"));
    picker.setItems(items);

    // Add a change listener to style the label using the selected font.
    picker.addChangeListener(new ChangeListener() {
      public void onChange(Widget sender) {
        DOM.setStyleAttribute(label.getElement(), "fontFamily",
            picker.getSelectedValue().toString());
      }
    });

    panel.add(createItemPickerPanel(picker));
  }

  private void sampleUSStates(Panel panel) {
    panel.add(new HTML("U.S. States"));

    // Create an oracle which treats "(" and ")" as word separators.
    Suggest.DefaultOracle oracle = new Suggest.DefaultOracle(
        Arrays.asList(new String[] {"(", ")"}));

    // Create a list of states and add them to the oracle.
    List stateList = Arrays.asList(new String[] {
        "Alabama(AL)", "Alaska(AK)", "Arizona(AZ)", "Arkansas(AR)",
        "California(CA)", "Colorado(CO)", "Connecticut(CT)", "Delaware(DE)",
        "Florida(FL)", "Hawaii(HI)", "Idaho (ID)", "Illinois(IL)",
        "Indiana(IN)", "Iowa(IA)", "Kansas(KS)", "Kentucky(KY)",
        "Louisiana(LA)", "Maine(ME)", "Maryland(MD)", "Massachusetts(MA)",
        "Michigan(MI)", "Minnesota(MN)", "Mississippi(MS)", "Missouri(MO)",
        "Montana(MT)", "Nebraska(NE)", "Nevada(NV)", "New Hampshire(NH)",
        "New Jersey(NJ)", "New Mexico(NM)", "New York(NY)",
        "North Carolina(NC)", "North Dakota(ND)", "Ohio(OH)", "Oklahoma(OK)",
        "Oregon(OR)", "Pennsylvania(PA)", "Rhode Island(RI)",
        "South Carolina(SC)", "South Dakota(SD)", "Tennessee(TN)", "Texas(TX)",
        "Utah(UT)", "Vermont(VT)", "Virginia(VA)", "Washington(WA)",
        "West Virginia(WV)", "Wisconsin(WI)", "Wyoming(WY)"});
    oracle.addAll(stateList);

    // Create a new SuggestBox for the oracle.
    SuggestBox states = new SuggestBox(oracle);
    states.setTitle("There is one state missing, can you guess which one?");
    panel.add(states);
  }
}