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
package com.google.gwt.app.place;

import com.google.gwt.app.util.IsWidget;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.Widget;

import junit.framework.TestCase;

/**
 * Eponymous unit test.
 */
public class ActivityManagerTest extends TestCase {
  private static class MyPlace extends Place {
  }

  private static class MyActivity implements Activity {
    Boolean canceled = null;
    Boolean stopped = null;
    Display started = null;
    boolean willStop = true;
    MyView view;
    
    MyActivity(MyView view) {
      this.view = view;
    }

    public void onCancel() {
      canceled = true;
    }

    public void onStop() {
      stopped = true;
    }

    public void start(Display panel) {
      started = panel;
      panel.showActivityWidget(view);
    }

    public boolean willStop() {
      return willStop;
    }

  };

  private static class MyView implements IsWidget {
    boolean asWidgetCalled = false;

    public Widget asWidget() {
      asWidgetCalled = true;
      return null;
    }
  }
  
  private static class MyDisplay implements Activity.Display {
    IsWidget widget = null;
    
    public void showActivityWidget(IsWidget widget) {
      this.widget = widget;
    }
  }

  private final MyPlace place1 = new MyPlace();
  private final MyPlace place2 = new MyPlace();

  private final MyActivity activity1 = new MyActivity(new MyView());
  private final MyActivity activity2 = new MyActivity(new MyView());
  
  private final MyDisplay display = new MyDisplay();

  private final ActivityMapper<MyPlace> myMap = new ActivityMapper<MyPlace>() {
    public Activity getActivity(MyPlace place) {
      if (place.equals(place1)) {
        return activity1;
      }
      if (place.equals(place2)) {
        return activity2;
      }
      
      return null;
    }
  };

  HandlerManager eventBus = new HandlerManager(null);
  ActivityManager<MyPlace> manager = new ActivityManager<MyPlace>(myMap, eventBus);

  public void testEventSetupAndTeardown() {
    assertEquals(0, eventBus.getHandlerCount(PlaceChangeEvent.TYPE));
    assertEquals(0, eventBus.getHandlerCount(PlaceChangeRequestedEvent.TYPE));
    
    manager.setDisplay(display);
    
    assertEquals(1, eventBus.getHandlerCount(PlaceChangeEvent.TYPE));
    assertEquals(1, eventBus.getHandlerCount(PlaceChangeRequestedEvent.TYPE));
    
    manager.setDisplay(null);
    
    assertEquals(0, eventBus.getHandlerCount(PlaceChangeEvent.TYPE));
    assertEquals(0, eventBus.getHandlerCount(PlaceChangeRequestedEvent.TYPE));
    
    manager.setDisplay(null);
  }

  public void testSimpleDispatch() {
    manager.setDisplay(display);
    
    PlaceChangeRequestedEvent<MyPlace> event = new PlaceChangeRequestedEvent<MyPlace>(place1);
    eventBus.fireEvent(event);
    assertFalse(event.isRejected());
    assertNull(display.widget);
    
    eventBus.fireEvent(new PlaceChangeEvent<Place>(place1));
    assertEquals(activity1.view, display.widget);
    
    event = new PlaceChangeRequestedEvent<MyPlace>(place2);
    eventBus.fireEvent(event);
    assertFalse(event.isRejected());
    assertEquals(activity1.view, display.widget);

    eventBus.fireEvent(new PlaceChangeEvent<Place>(place2));
    assertEquals(activity2.view, display.widget);
  }
  
  public void testRejected() {
    manager.setDisplay(display);
    
    activity1.willStop = false;
    
    PlaceChangeRequestedEvent<MyPlace> event = new PlaceChangeRequestedEvent<MyPlace>(place1);
    eventBus.fireEvent(event);
    assertFalse(event.isRejected());
    assertNull(display.widget);
    
    eventBus.fireEvent(new PlaceChangeEvent<Place>(place1));
    assertEquals(activity1.view, display.widget);
    
    event = new PlaceChangeRequestedEvent<MyPlace>(place2);
    eventBus.fireEvent(event);
    assertTrue(event.isRejected());
    assertEquals(activity1.view, display.widget);
  }
}
