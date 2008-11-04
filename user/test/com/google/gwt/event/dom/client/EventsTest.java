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
package com.google.gwt.event.dom.client;

import com.google.gwt.event.shared.AbstractEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;

import junit.framework.TestCase;

/**
 * Tests of events.
 */
public class EventsTest extends TestCase {

  private HandlerManager manager;

  public void setUp() {
    manager = new HandlerManager(this);
  }

  static class Flag {

    public boolean flag = false;
  }

  public void testKeyEvents() {
    final Flag flag = new Flag();
    HandlerRegistration downRegistration = manager.addHandler(
        KeyDownEvent.getType(), new KeyDownHandler() {
          public void onKeyDown(KeyDownEvent event) {
            flag.flag = true;
          }
        });
    HandlerRegistration upRegistration = manager.addHandler(KeyUpEvent.getType(),
        new KeyUpHandler() {
          public void onKeyUp(KeyUpEvent event) {
            flag.flag = true;
          }
        });

    HandlerRegistration pressRegistration = manager.addHandler(
        KeyPressEvent.getType(), new KeyPressHandler() {
          public void onKeyPress(KeyPressEvent event) {
            flag.flag = true;
          }
        });

    checkFire(new KeyDownEvent(), downRegistration, flag, "onKeyDown");
    checkFire(new KeyUpEvent(), upRegistration, flag, "onKeyUp");
    checkFire(new KeyPressEvent(), pressRegistration, flag, "onKeyPressed");
  }

  public void testMouseEvents() {

    final Flag flag = new Flag();
    HandlerRegistration downRegistration = manager.addHandler(
        MouseDownEvent.getType(), new MouseDownHandler() {
          public void onMouseDown(MouseDownEvent event) {
            flag.flag = true;
          }
        });
    HandlerRegistration upRegistration = manager.addHandler(
        MouseUpEvent.getType(), new MouseUpHandler() {
          public void onMouseUp(MouseUpEvent event) {
            flag.flag = true;
          }
        });

    HandlerRegistration clickRegistration = manager.addHandler(
        ClickEvent.getType(), new ClickHandler() {
          public void onClick(ClickEvent event) {
            flag.flag = true;
          }
        });

    HandlerRegistration dblclickRegistration = manager.addHandler(
        DoubleClickEvent.getType(), new DoubleClickHandler() {
          public void onDoubleClick(DoubleClickEvent event) {
            flag.flag = true;
          }
        });

    HandlerRegistration outRegistration = manager.addHandler(
        MouseOutEvent.getType(), new MouseOutHandler() {
          public void onMouseOut(MouseOutEvent event) {
            flag.flag = true;
          }
        });
    HandlerRegistration overRegistration = manager.addHandler(
        MouseOverEvent.getType(), new MouseOverHandler() {
          public void onMouseOver(MouseOverEvent event) {
            flag.flag = true;
          }
        });

    HandlerRegistration moveRegistration = manager.addHandler(
        MouseMoveEvent.getType(), new MouseMoveHandler() {
          public void onMouseMove(MouseMoveEvent event) {
            flag.flag = true;
          }
        });

    HandlerRegistration wheelRegistration = manager.addHandler(
        MouseWheelEvent.getType(), new MouseWheelHandler() {
          public void onMouseWheel(MouseWheelEvent event) {
            flag.flag = true;
          }
        });

    checkFire(new MouseDownEvent(), downRegistration, flag, "onMouseDown");
    checkFire(new MouseUpEvent(), upRegistration, flag, "onMouseUp");
    checkFire(new MouseOutEvent(), outRegistration, flag, "onMouseOut");
    checkFire(new MouseOverEvent(), overRegistration, flag, "onMouseOver");
    checkFire(new MouseMoveEvent(), moveRegistration, flag, "onMouseMove");
    checkFire(new MouseWheelEvent(), wheelRegistration, flag, "onMouseWheel");
    checkFire(new ClickEvent(), clickRegistration, flag, "onClick");
    checkFire(new DoubleClickEvent(), dblclickRegistration, flag,
        "onDoubleClick");
  }

  private void checkFire(AbstractEvent event, HandlerRegistration registration,
      Flag flag, String eventName) {

    flag.flag = false;
    manager.fireEvent(event);
    assertTrue(eventName + " didn't fire.", flag.flag);

    flag.flag = false;
    registration.removeHandler();
    manager.fireEvent(event);
    assertTrue(eventName + " fired when it shouldn't have.", !flag.flag);
  }
}
