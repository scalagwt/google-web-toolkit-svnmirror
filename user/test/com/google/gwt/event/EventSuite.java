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
package com.google.gwt.event;

import com.google.gwt.event.dom.client.EventsTest;
import com.google.gwt.event.dom.client.GWTEventsTest;
import com.google.gwt.event.shared.HandlerManagerTest;
import com.google.gwt.event.shared.JSHanderRegistryTest;
import com.google.gwt.junit.tools.GWTTestSuite;

import junit.framework.Test;

/**
 * Test for suite for the com.google.gwt.event module.
 */
public class EventSuite {
  public static Test suite() {
    GWTTestSuite suite = new GWTTestSuite(
        "Test for suite for the com.google.gwt.event module.");
    
    // ...dom.client
    suite.addTestSuite(EventsTest.class);
    suite.addTestSuite(GWTEventsTest.class);
    
    // ...dom.shared
    suite.addTestSuite(HandlerManagerTest.class);
    suite.addTestSuite(JSHanderRegistryTest.class);

    return suite;
  }
}
