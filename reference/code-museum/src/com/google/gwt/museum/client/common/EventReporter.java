/*
 * Copyright 2008 Google Inc.
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

package com.google.gwt.museum.client.common;

import com.google.gwt.event.dom.client.AllKeyHandlers;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FocusListener;
import com.google.gwt.user.client.ui.HasHTML;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MouseListener;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SuggestionEvent;
import com.google.gwt.user.client.ui.SuggestionHandler;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

/**
 * Helper class to create visual tests.
 * 
 * @param <V> value type
 * @param <T> target type
 */
@SuppressWarnings("deprecation")
public class EventReporter<V, T> extends AllKeyHandlers implements
    ChangeListener, FocusListener, ValueChangeHandler<V>,
    SelectionHandler<Suggestion>, SuggestionHandler, KeyboardListener,
    ChangeHandler, BlurHandler, FocusHandler, ClickHandler, ClickListener,
    CloseHandler<T>, MouseListener {

  /**
   * Add/remove handlers via check box.
   * 
   */
  public abstract class CheckBoxEvent extends CheckBox implements
      ValueChangeHandler<Boolean> {
    String name;

    public CheckBoxEvent(String name, Panel p) {
      this.name = name;
      this.setText(name);
      p.add(this);
      this.addValueChangeHandler(this);
      this.setValue(true, true);
    }

    public abstract void addHandler();

    public void onValueChange(ValueChangeEvent<Boolean> event) {
      if (event.getValue().booleanValue()) {
        report("add " + name);
        addHandler();
      } else {
        report("remove " + name);
        removeHandler();
      }
    }

    public abstract void removeHandler();
  }

  private VerticalPanel panel = new VerticalPanel();

  public EventReporter(Panel parent) {
    parent.add(this.panel);
  }

  public String getInfo(Object sender) {
    if (sender instanceof HasText) {
      return ((HasText) sender).getText();
    } else if (sender instanceof UIObject
        && ((UIObject) sender).getTitle() != null) {
      return ((UIObject) sender).getTitle();
    } else if (sender instanceof HasHTML) {
      return ((HasHTML) sender).getHTML();
    } else {
      return sender.toString();
    }
  }

  public void onBlur(BlurEvent event) {
    report(event);
  }

  public void onChange(ChangeEvent event) {
    report(event);
  }

  @SuppressWarnings("deprecation")
  public void onChange(Widget sender) {
    report("change on " + getInfo(sender));
  }

  public void onClick(ClickEvent event) {
    report(event);
  }

  @SuppressWarnings("deprecation")
  public void onClick(Widget sender) {
    report("click: " + getInfo(sender));
  }

  public void onClose(CloseEvent<T> event) {
    report("close " + getInfo(event.getTarget()));
  }

  public void onFocus(FocusEvent event) {
    report(event);
  }

  public void onFocus(Widget sender) {
    report("focus: " + getInfo(sender));
  }

  public void onKeyDown(KeyDownEvent event) {
    report(event);
  }

  public void onKeyDown(Widget sender, char keyCode, int modifiers) {
    report(getInfo(sender) + "key down code: " + keyCode + " modifiers: "
        + modifiers);
  }

  public void onKeyPress(KeyPressEvent event) {
    report(event);
  }

  public void onKeyPress(Widget sender, char keyCode, int modifiers) {
    report(getInfo(sender) + "key press code: " + keyCode + " modifiers: "
        + modifiers);
  }

  public void onKeyUp(KeyUpEvent event) {
    report(event);
  }

  public void onKeyUp(Widget sender, char keyCode, int modifiers) {
    report(getInfo(sender) + "key  up code: " + keyCode + " modifiers: "
        + modifiers);
  }

  public void onLostFocus(Widget sender) {
    report("blur: " + sender.getClass());
  }

  public void onMouseDown(Widget sender, int x, int y) {
    report(getInfo(sender) + "mouse down");
  }

  public void onMouseEnter(Widget sender) {
    report(getInfo(sender) + "mouse enter");
  }

  public void onMouseLeave(Widget sender) {
    report(getInfo(sender) + "mouse leave");
  }

  public void onMouseMove(Widget sender, int x, int y) {
    report(getInfo(sender) + "mouse move");
  }

  public void onMouseUp(Widget sender, int x, int y) {
    report(getInfo(sender) + "mouse up");
  }

  public void onSelection(SelectionEvent<Suggestion> event) {
    report(event);
  }

  public void onSuggestionSelected(SuggestionEvent event) {
    report("suggestion: " + event.getSelectedSuggestion());
  }

  public void onValueChange(ValueChangeEvent<V> event) {
    report(event);
  }

  // will be replaced by logging
  public void report(String s) {
    panel.insert(new Label(s), 0);
    if (panel.getWidgetCount() == 10) {
      panel.remove(9);
    }
  }

  private void report(GwtEvent<?> event) {
    report(getInfo(event.getSource()) + " fired " + event.toDebugString());
  }

}
