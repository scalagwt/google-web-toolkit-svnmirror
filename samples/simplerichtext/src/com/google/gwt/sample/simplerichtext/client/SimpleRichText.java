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
package com.google.gwt.sample.simplerichtext.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.richtext.Highlight;
import com.google.gwt.user.client.ui.richtext.HighlightCategory;
import com.google.gwt.user.client.ui.richtext.HighlightClickEvent;
import com.google.gwt.user.client.ui.richtext.HighlightClickHandler;
import com.google.gwt.user.client.ui.richtext.HighlightMouseEvent;
import com.google.gwt.user.client.ui.richtext.HighlightMouseHandler;
import com.google.gwt.user.client.ui.richtext.Misspelling;
import com.google.gwt.user.client.ui.richtext.RichTextEditor;
import com.google.gwt.user.client.ui.richtext.SpellCheckCallback;
import com.google.gwt.user.client.ui.richtext.SpellCheckOracle;
import com.google.gwt.user.client.ui.richtext.SpellCheckRequest;
import com.google.gwt.user.client.ui.richtext.SpellCheckResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Simple rich text editor demonstration.
 */
public class SimpleRichText implements EntryPoint {

  private static Map dictionary = new HashMap();

  {
    String[] choices = {"cat", "can't", "can", "cap", "cane", "catastrophe"};
    dictionary.put("caat", choices);

    String[] dogChoices = {"dog", "done", "doom"};
    dictionary.put("doog", dogChoices);
  }

  public void onModuleLoad() {
    demoRichText();
  }

  private void demoRichText() {

    // Each RichTextEditor can get its own style sheet.
    final RichTextEditor b = new RichTextEditor("SimpleRichText.css");

    // Each RichTextEditor can also customize where it gets it spell check
    // information from.

    b.setSpellCheckModel(new SpellCheckOracle() {

      public void spellCheck(final SpellCheckRequest request,
          SpellCheckCallback callback) {

        SpellCheckResponse response = new SpellCheckResponse() {
          public Misspelling[] getMisspellings() {
            return demoSpellCheck(request);
          }
        };
        callback.onSpellCheckResponseRecieved(request, response);
      }
    });

    Timer t = new Timer() {
      public void run() {
        b.getRichTextArea().setHTML(
            "A doog does not like a caat unless it has a bone or a biscuit.");
        HighlightCategory countedCategory = new HighlightCategory(
            "inanimateObject");
        Iterator i = b.getRichTextArea().addHighlights(
            Arrays.asList(new String[] {"bone", "biscuit"}), countedCategory);
        while (i.hasNext()) {
          ((Highlight) i.next()).addClickHandler(new HighlightClickHandler() {
            int count = 0;

            public void onClick(HighlightClickEvent event) {
              String num = (count == 0) ? "no" : ("" + count);
              String s = "";
              if (count != 1) {
                s = "s";
              }
              count++;
              Window.alert("I have " + num + " "
                  + event.getHighlight().getHighlighted() + s + "!");
            }
          });
        }
        HighlightCategory category = new HighlightCategory("verb");
        b.getRichTextArea().addHighlights(
            Arrays.asList(new String[] {"does", "like", "has"}), category);
        category.addMouseHandler(new HighlightMouseHandler() {

          public void onMouseDown(HighlightMouseEvent event) {
          }

          public void onMouseMove(HighlightMouseEvent event) {
            event.getHighlight().unhighlight();
          }

          public void onMouseUp(HighlightMouseEvent event) {
          }

        });
        RootPanel.get().add(new Label(b.getRichTextArea().getHTML()));
      }
    };
    t.schedule(5);
    b.setWidth("100%");
    b.setHeight("90%");
    RootPanel.get().add(b);
    TextArea ta = new TextArea();
    ta.setWidth("100%");
    ta.setHeight("10%");
    RootPanel.get().add(ta);
  }

  /**
   * A client side demo spell checker that has a very tiny dictionary.
   */
  private Misspelling[] demoSpellCheck(SpellCheckRequest request) {
    ArrayList accum = new ArrayList();
    String[] words = request.getText().split(" ");
    for (int i = 0; i < words.length; i++) {
      String[] choices = (String[]) dictionary.get(words[i]);
      if (choices != null) {
        Misspelling mispelling = new Misspelling();
        mispelling.setSuggestions(choices);
        mispelling.setWord(words[i]);
        accum.add(mispelling);
      }
    }
    if (accum.size() == 0) {
      return null;
    } else {
      Misspelling[] mispellings = new Misspelling[accum.size()];
      int count = 0;
      for (Iterator i = accum.iterator(); i.hasNext();) {
        mispellings[count++] = (Misspelling) i.next();
      }
      return mispellings;
    }
  }
}
