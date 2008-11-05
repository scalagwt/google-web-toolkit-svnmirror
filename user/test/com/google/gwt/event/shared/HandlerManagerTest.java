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

package com.google.gwt.event.shared;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.user.client.Event;

/**
 * Handler manager test.
 * 
 */
public class HandlerManagerTest extends HandlerTestBase {

  public void testAddHandlers() {

    HandlerManager manager = new HandlerManager("bogus source");
    addHandlers(manager);
  }

  private void addHandlers(HandlerManager manager) {
    manager.addHandler(MouseDownEvent.getType(), mouse1);
    manager.addHandler(MouseDownEvent.getType(), mouse2);
    manager.addHandler(MouseDownEvent.getType(), adaptor1);
    DomEvent.unsafeFireNativeEvent(Event.ONMOUSEDOWN, manager);
    assertEquals(3, manager.getHandlerCount(MouseDownEvent.getType()));
    assertFired(mouse1, mouse2, adaptor1);
    manager.addHandler(MouseDownEvent.getType(), mouse3);
    assertEquals(4, manager.getHandlerCount(MouseDownEvent.getType()));

    manager.addHandler(MouseDownEvent.getType(), mouse1);
    manager.addHandler(MouseDownEvent.getType(), mouse2);
    manager.addHandler(MouseDownEvent.getType(), adaptor1);

    // You can indeed add handlers twice, they will only be removed one at a
    // time though.
    assertEquals(7, manager.getHandlerCount(MouseDownEvent.getType()));
    manager.addHandler(ClickEvent.getType(), adaptor1);
    manager.addHandler(ClickEvent.getType(), click1);
    manager.addHandler(ClickEvent.getType(), click2);

    assertEquals(7, manager.getHandlerCount(MouseDownEvent.getType()));
    assertEquals(3, manager.getHandlerCount(ClickEvent.getType()));

    reset();
    DomEvent.unsafeFireNativeEvent(Event.ONMOUSEDOWN, manager);
    assertFired(mouse1, mouse2, mouse3, adaptor1);
    assertNotFired(click1, click2);
  }

  public void testRemoveHandlers() {
    HandlerManager manager = new HandlerManager("bogus source");
    addHandlers(manager);
    // Gets rid of first instance.
    manager.removeHandler(MouseDownEvent.getType(), adaptor1);
    DomEvent.unsafeFireNativeEvent(Event.ONMOUSEDOWN, manager);
    assertFired(mouse1, mouse2, mouse3, adaptor1);
    assertNotFired(click1, click2);

    // Gets rid of second instance.
    manager.removeHandler(MouseDownEvent.getType(), adaptor1);
    reset();
    DomEvent.unsafeFireNativeEvent(Event.ONMOUSEDOWN, manager);
    assertFired(mouse1, mouse2, mouse3);
    assertNotFired(adaptor1, click1, click2);

    // Checks to see if click events are still working.
    reset();
    DomEvent.unsafeFireNativeEvent(Event.ONCLICK, manager);

    assertNotFired(mouse1, mouse2, mouse3);
    assertFired(click1, click2, adaptor1);
  }

  public void testConcurrentAdd() {
    final HandlerManager manager = new HandlerManager("bogus source");
    final MouseDownHandler two = new MouseDownHandler() {
      public void onMouseDown(MouseDownEvent event) {
        add(this);
      }
    };
    MouseDownHandler one = new MouseDownHandler() {
      public void onMouseDown(MouseDownEvent event) {
        manager.addHandler(MouseDownEvent.getType(), two);
        add(this);
      }
    };
    manager.addHandler(MouseDownEvent.getType(), one);
    manager.addHandler(MouseDownEvent.getType(), mouse1);
    manager.addHandler(MouseDownEvent.getType(), mouse2);
    manager.addHandler(MouseDownEvent.getType(), mouse3);
    manager.fireEvent(new MouseDownEvent() {
    });
    assertFired(one, mouse1, mouse2, mouse3);
    assertNotFired(two);

    reset();
    manager.fireEvent(new MouseDownEvent() {
    });
    assertFired(one, two, mouse1, mouse2, mouse3);
  }

  class ShyHandler implements MouseDownHandler {
    HandlerRegistration r;

    public void onMouseDown(MouseDownEvent event) {
      add(this);
      r.removeHandler();
    }
  }

  public void testConcurrentRemove() {
    final HandlerManager manager = new HandlerManager("bogus source");

    ShyHandler h = new ShyHandler();

    manager.addHandler(MouseDownEvent.getType(), mouse1);
    h.r = manager.addHandler(MouseDownEvent.getType(), h);
    manager.addHandler(MouseDownEvent.getType(), mouse2);
    manager.addHandler(MouseDownEvent.getType(), mouse3);

    manager.fireEvent(new MouseDownEvent() {
    });
    assertFired(h, mouse1, mouse2, mouse3);
    reset();
    manager.fireEvent(new MouseDownEvent() {
    });
    assertFired(mouse1, mouse2, mouse3);
    assertNotFired(h);
  }

  public void testConcurrentAddAndRemoveByNastyUsersTryingToHurtUs() {
    final HandlerManager manager = new HandlerManager("bogus source");
    final MouseDownHandler two = new MouseDownHandler() {
      public void onMouseDown(MouseDownEvent event) {
        add(this);
      }

      @Override
      public String toString() {
        return "two";
      }
    };
    MouseDownHandler one = new MouseDownHandler() {
      public void onMouseDown(MouseDownEvent event) {
        manager.addHandler(MouseDownEvent.getType(), two).removeHandler();
        add(this);
      }

      @Override
      public String toString() {
        return "one";
      }
    };
    manager.addHandler(MouseDownEvent.getType(), one);
    manager.addHandler(MouseDownEvent.getType(), mouse1);
    manager.addHandler(MouseDownEvent.getType(), mouse2);
    manager.addHandler(MouseDownEvent.getType(), mouse3);
    manager.fireEvent(new MouseDownEvent() {
    });
    assertFired(one, mouse1, mouse2, mouse3);
    assertNotFired(two);

    reset();
    manager.fireEvent(new MouseDownEvent() {
    });
    assertFired(one, mouse1, mouse2, mouse3);
    assertNotFired(two);
  }

  public void testMultiFiring() {

    HandlerManager manager = new HandlerManager("source1");

    final HandlerManager manager2 = new HandlerManager("source2");

    manager.addHandler(MouseDownEvent.getType(), mouse1);

    manager.addHandler(MouseDownEvent.getType(), new MouseDownHandler() {

      public void onMouseDown(MouseDownEvent event) {
        manager2.fireEvent(event);
      }

    });
    manager.addHandler(MouseDownEvent.getType(), mouse3);
    manager2.addHandler(MouseDownEvent.getType(), adaptor1);
    manager2.addHandler(MouseDownEvent.getType(), new MouseDownHandler() {

      public void onMouseDown(MouseDownEvent event) {
        assertEquals("source2", event.getSource());
      }

    });
    manager.addHandler(MouseDownEvent.getType(), new MouseDownHandler() {

      public void onMouseDown(MouseDownEvent event) {
        assertEquals("source1", event.getSource());
      }

    });

    reset();
    DomEvent.unsafeFireNativeEvent(Event.ONMOUSEDOWN, manager);
    assertFired(mouse1, adaptor1, mouse3);
  }
}
