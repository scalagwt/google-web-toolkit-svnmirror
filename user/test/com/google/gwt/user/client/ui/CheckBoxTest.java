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
package com.google.gwt.user.client.ui;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;

/**
 * Tests the CheckBox Widget.
 */
public class CheckBoxTest extends GWTTestCase {

  @SuppressWarnings("deprecation")
  static class ListenerTester implements ClickListener {
    static int fired = 0;
    static HandlerManager manager;

    public static void fire() {
      fired = 0;
      manager.fireEvent(new ClickEvent() {
      });
    }

    public void onClick(Widget sender) {
      ++fired;
    }
  }

  private static class Handler implements ValueChangeHandler<Boolean> {
    Boolean received = null;

    public void onValueChange(ValueChangeEvent<Boolean> event) {
      received = event.getValue();
    }
  }

  private CheckBox cb;

  @Override
  public String getModuleName() {
    return "com.google.gwt.user.DebugTest";
  }

  /**
   * Test accessors.
   */
  @SuppressWarnings("deprecation")
  public void testAccessors() {
    cb.setHTML("test HTML");
    assertEquals(cb.getHTML(), "test HTML");
    cb.setText("test Text");
    assertEquals(cb.getText(), "test Text");

    cb.setChecked(true);
    assertTrue(cb.isChecked());
    cb.setChecked(false);
    assertFalse(cb.isChecked());

    cb.setValue(true);
    assertTrue(cb.getValue());
    cb.setValue(false);
    assertFalse(cb.getValue());

    cb.setEnabled(false);
    assertFalse(cb.isEnabled());
    cb.setEnabled(true);
    assertTrue(cb.isEnabled());

    cb.setTabIndex(2);
    assertEquals(cb.getTabIndex(), 2);

    cb.setName("my name");
    assertEquals(cb.getName(), "my name");

    cb.setFormValue("valuable");
    assertEquals("valuable", cb.getFormValue());
  }

  public void testDebugId() {
    CheckBox check = new CheckBox("myLabel");

    // We need to replace the input element so we can keep a handle to it
    Element newInput = DOM.createInputCheck();
    check.replaceInputElement(newInput);

    check.ensureDebugId("myCheck");
    RootPanel.get().add(check);

    UIObjectTest.assertDebugId("myCheck", check.getElement());
    UIObjectTest.assertDebugId("myCheck-input", newInput);
    UIObjectTest.assertDebugIdContents("myCheck-label", "myLabel");
  }

  public void testConstructorInputElement() {
    InputElement elm = DOM.createInputCheck().cast();
    CheckBox box = new CheckBox(elm.<Element> cast());
    assertFalse(box.getValue());
    elm.setDefaultChecked(true);
    assertTrue(box.getValue());
  }

  public void testReplaceInputElement() {
    cb.setValue(true);
    cb.setTabIndex(1234);
    cb.setEnabled(false);
    cb.setAccessKey('k');
    cb.setFormValue("valuable");

    InputElement elm = Document.get().createCheckInputElement();
    assertFalse(elm.isChecked());

    Element asOldElement = elm.cast();
    cb.replaceInputElement(asOldElement);

    // The values should be preserved
    assertTrue(cb.getValue());
    assertEquals(1234, cb.getTabIndex());
    assertFalse(cb.isEnabled());
    assertEquals("k", elm.getAccessKey());
    assertEquals("valuable", cb.getFormValue());

    assertTrue(elm.isChecked());
    cb.setValue(false);
    assertFalse(elm.isChecked());

    elm.setChecked(true);
    assertTrue(cb.getValue());

    // TODO: When event creation is in, test that click on the new element works
  }

  public void testFormValue() {
    InputElement elm = Document.get().createCheckInputElement();
    Element asOldElement = elm.cast();
    cb.replaceInputElement(asOldElement);

    // assertEquals("", elm.getValue());
    cb.setFormValue("valuable");
    assertEquals("valuable", elm.getValue());

    elm.setValue("invaluable");
    assertEquals("invaluable", cb.getFormValue());
  }

  @SuppressWarnings("deprecation")
  public void testListenerRemoval() {
    ClickListener r1 = new ListenerTester();
    ClickListener r2 = new ListenerTester();
    ListenerTester.manager = cb.ensureHandlers();
    cb.addClickListener(r1);
    cb.addClickListener(r2);

    ListenerTester.fire();
    assertEquals(ListenerTester.fired, 2);

    cb.removeClickListener(r1);
    ListenerTester.fire();
    assertEquals(ListenerTester.fired, 1);

    cb.removeClickListener(r2);
    ListenerTester.fire();
    assertEquals(ListenerTester.fired, 0);
  }

  @SuppressWarnings("deprecation")
  public void testValueChangeEvent() {
    Handler h = new Handler();
    cb.addValueChangeHandler(h);
    cb.setChecked(false);
    assertNull(h.received);
    cb.setChecked(true);
    assertNull(h.received);

    cb.setValue(false);
    assertNull(h.received);
    cb.setValue(true);
    assertNull(h.received);

    cb.setValue(true, true);
    assertNull(h.received);

    cb.setValue(false, true);
    assertFalse(h.received);

    cb.setValue(true, true);
    assertTrue(h.received);

    try {
      cb.setValue(null);
      fail("Should throw IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      /* pass */
    }
  }

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
    RootPanel.get().clear();
    cb = new CheckBox();
    RootPanel.get().add(cb);
  }

  @Override
  protected void gwtTearDown() throws Exception {
    RootPanel.get().clear();
    super.gwtTearDown();
  }
}
