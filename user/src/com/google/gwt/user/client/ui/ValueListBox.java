/*
 * Copyright 2010 Google Inc.
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

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.text.shared.Renderer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link HasConstrainedValue} based on a
 * {@link com.google.gwt.dom.client.SelectElement}.
 * <p>
 * A {@link Renderer<T>} is used to get user presentable strings to display in
 * the select element. It is an error for two values to render to the same
 * string.
 * 
 * @param <T> the value type
 */
public class ValueListBox<T> extends Composite implements
    HasConstrainedValue<T> {

  private final List<T> values = new ArrayList<T>();
  private final Map<T, Integer> valueToIndex = new HashMap<T, Integer>();
  private final Renderer<T> renderer;

  private T value;

  public ValueListBox(Renderer<T> renderer) {
    this.renderer = renderer;
    initWidget(new ListBox());

    getListBox().addChangeHandler(new ChangeHandler() {
      public void onChange(ChangeEvent event) {
        int selectedIndex = getListBox().getSelectedIndex();

        if (selectedIndex < 0) {
          return; // Not sure why this happens during addValue
        }
        T newValue = values.get(selectedIndex);
        setValue(newValue, true);
      }
    });
  }

  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<T> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  public T getValue() {
    return value;
  }

  public void setAcceptableValues(Collection<T> newValues) {
    values.clear();
    valueToIndex.clear();
    ListBox listBox = getListBox();
    listBox.clear();

    for (T nextNewValue : newValues) {
      addValue(nextNewValue);
    }

    updateListBox();
  }

  /**
   * Set the value and display it in the select element. Add the value to the
   * acceptable set if it is not already there.
   */
  public void setValue(T value) {
    setValue(value, false);
  }

  public void setValue(T value, boolean fireEvents) {
    if (value == this.value || (this.value != null && this.value.equals(value))) {
      return;
    }

    T before = this.value;
    this.value = value;
    updateListBox();

    if (fireEvents) {
      ValueChangeEvent.fireIfNotEqual(this, before, value);
    }
  }

  private void addValue(T value) {
    if (valueToIndex.containsKey(value)) {
      throw new IllegalArgumentException("Duplicate value: " + value);
    }

    valueToIndex.put(value, values.size());
    values.add(value);
    getListBox().addItem(renderer.render(value));
    assert values.size() == getListBox().getItemCount();
  }

  private ListBox getListBox() {
    return (ListBox) getWidget();
  }

  private void updateListBox() {
    Integer index = valueToIndex.get(value);
    if (index == null) {
      addValue(value);
    }

    index = valueToIndex.get(value);
    getListBox().setSelectedIndex(index);
  }
}
