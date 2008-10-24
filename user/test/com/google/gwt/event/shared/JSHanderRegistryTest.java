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
import com.google.gwt.event.dom.client.MouseDownEvent;

/**
 * Basic tests for Handler registry's. Not much to it as most testing should be
 * in GWTEventsTest.
 */
public class JSHanderRegistryTest extends HandlerTestBase {

  public void testAccesors() {
    JsHandlerRegistry registry = JsHandlerRegistry.create();
    registry.addHandler(MouseDownEvent.TYPE, mouse1);
    assertEquals(mouse1, registry.getHandler(MouseDownEvent.TYPE, 0));

    registry.addHandler(ClickEvent.TYPE, click1);
    registry.addHandler(MouseDownEvent.TYPE, mouse2);
    assertEquals(2, registry.getHandlerCount(MouseDownEvent.TYPE));
    registry.removeHandler(MouseDownEvent.TYPE, mouse1);

    assertEquals(1, registry.getHandlerCount(MouseDownEvent.TYPE));
    assertEquals(1, registry.getHandlerCount(ClickEvent.TYPE));
    assertEquals(mouse2, registry.getHandler(MouseDownEvent.TYPE, 0));
    assertEquals(click1, registry.getHandler(ClickEvent.TYPE, 0));
  }

  public void testAdd() {
    JsHandlerRegistry registry = JsHandlerRegistry.create();
    registry.addHandler(MouseDownEvent.TYPE, mouse1);
    assertEquals(1, registry.getHandlerCount(MouseDownEvent.TYPE));
  }

  public void testRemove() {
    JsHandlerRegistry registry = JsHandlerRegistry.create();
    registry.addHandler(MouseDownEvent.TYPE, mouse1);
    registry.addHandler(MouseDownEvent.TYPE, mouse2);

    assertEquals(2, registry.getHandlerCount(MouseDownEvent.TYPE));
    registry.removeHandler(MouseDownEvent.TYPE, mouse2);
    assertEquals(1, registry.getHandlerCount(MouseDownEvent.TYPE));

    // Check for correct firing.
    reset();
    registry.fireEvent(new MouseDownEvent());
    assertFired(mouse1);
    assertNotFired(mouse2);

    registry.removeHandler(MouseDownEvent.TYPE, mouse1);

    // Nothing should happen now.
    reset();
    registry.fireEvent(new MouseDownEvent());
    assertNotFired(mouse1);
    assertNotFired(mouse2);

    registry.addHandler(MouseDownEvent.TYPE, mouse2);
    assertEquals(1, registry.getHandlerCount(MouseDownEvent.TYPE));

    // Two should fire
    reset();
    registry.fireEvent(new MouseDownEvent());
    assertNotFired(mouse1);
    assertFired(mouse2);
  }
}
