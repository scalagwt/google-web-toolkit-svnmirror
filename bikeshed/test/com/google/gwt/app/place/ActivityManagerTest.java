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
  private static class AsyncActivity extends SyncActivity {
    AsyncActivity(MyView view) {
      super(view);
    }

    @Override
    public void start(Display display) {
      this.display = display;
    }

    void finish() {
      display.showActivityWidget(view);
    }
  }

  private static class MyDisplay implements Activity.Display {
    IsWidget widget = null;

    public void showActivityWidget(IsWidget widget) {
      this.widget = widget;
    }
  }

  private static class MyPlace extends Place {
  };

  private static class MyView implements IsWidget {
    public Widget asWidget() {
      return null;
    }
  }

  private static class SyncActivity implements Activity {
    boolean canceled = false;
    boolean stopped = false;
    Display display = null;
    boolean willStop = true;
    MyView view;

    SyncActivity(MyView view) {
      this.view = view;
    }

    public void onCancel() {
      canceled = true;
    }

    public void onStop() {
      stopped = true;
    }

    public void start(Display display) {
      this.display = display;
      display.showActivityWidget(view);
    }

    public boolean willStop() {
      return willStop;
    }
  }

  private final MyPlace place1 = new MyPlace();
  private final MyPlace place2 = new MyPlace();

  private final SyncActivity activity1 = new SyncActivity(new MyView());
  private final SyncActivity activity2 = new SyncActivity(new MyView());

  private final MyDisplay realDisplay = new MyDisplay();

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

  private HandlerManager eventBus = new HandlerManager(null);
  private ActivityManager<MyPlace> manager = new ActivityManager<MyPlace>(
      myMap, eventBus);

  public void testCancel() {
    final AsyncActivity activity1 = new AsyncActivity(new MyView());
    final AsyncActivity activity2 = new AsyncActivity(new MyView());

    ActivityMapper<MyPlace> map = new ActivityMapper<MyPlace>() {
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

    manager = new ActivityManager<MyPlace>(map, eventBus);
    manager.setDisplay(realDisplay);

    PlaceChangeRequestedEvent<MyPlace> event = new PlaceChangeRequestedEvent<MyPlace>(
        place1);
    eventBus.fireEvent(event);
    assertFalse(event.isRejected());
    assertNull(realDisplay.widget);
    assertFalse(activity1.stopped);
    assertFalse(activity1.canceled);
    assertNull(activity1.display);

    eventBus.fireEvent(new PlaceChangeEvent<Place>(place1));
    assertNull(realDisplay.widget);
    assertFalse(activity1.stopped);
    assertFalse(activity1.canceled);
    assertNotNull(activity1.display);

    event = new PlaceChangeRequestedEvent<MyPlace>(place2);
    eventBus.fireEvent(event);
    assertFalse(event.isRejected());
    assertNull(realDisplay.widget);
    assertFalse(activity1.stopped);
    assertFalse(activity1.canceled);

    eventBus.fireEvent(new PlaceChangeEvent<Place>(place2));
    assertNull(realDisplay.widget);
    assertTrue(activity1.canceled);
    assertFalse(activity1.stopped);
    assertFalse(activity2.stopped);
    assertFalse(activity2.canceled);
    assertNotNull(activity2.display);

    activity2.finish();
    assertEquals(activity2.view, realDisplay.widget);
    
    activity1.finish();
    assertEquals(activity2.view, realDisplay.widget);
  }

  public void testAsyncDispatch() {
    final AsyncActivity activity1 = new AsyncActivity(new MyView());
    final AsyncActivity activity2 = new AsyncActivity(new MyView());

    ActivityMapper<MyPlace> map = new ActivityMapper<MyPlace>() {
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

    manager = new ActivityManager<MyPlace>(map, eventBus);
    manager.setDisplay(realDisplay);

    PlaceChangeRequestedEvent<MyPlace> event = new PlaceChangeRequestedEvent<MyPlace>(
        place1);
    eventBus.fireEvent(event);
    assertFalse(event.isRejected());
    assertNull(realDisplay.widget);
    assertFalse(activity1.stopped);
    assertFalse(activity1.canceled);
    assertNull(activity1.display);

    eventBus.fireEvent(new PlaceChangeEvent<Place>(place1));
    assertNull(realDisplay.widget);
    assertFalse(activity1.stopped);
    assertFalse(activity1.canceled);
    assertNotNull(activity1.display);

    activity1.finish();
    assertEquals(activity1.view, realDisplay.widget);
    assertFalse(activity1.stopped);
    assertFalse(activity1.canceled);

    event = new PlaceChangeRequestedEvent<MyPlace>(place2);
    eventBus.fireEvent(event);
    assertFalse(event.isRejected());
    assertEquals(activity1.view, realDisplay.widget);
    assertFalse(activity1.stopped);
    assertFalse(activity1.canceled);
    assertFalse(activity2.stopped);
    assertFalse(activity2.canceled);
    assertNull(activity2.display);

    eventBus.fireEvent(new PlaceChangeEvent<Place>(place2));
    assertNull(realDisplay.widget);
    assertFalse(activity1.canceled);
    assertTrue(activity1.stopped);
    assertFalse(activity2.stopped);
    assertFalse(activity2.canceled);
    assertNotNull(activity2.display);

    activity2.finish();
    assertEquals(activity2.view, realDisplay.widget);
  }

  public void testEventSetupAndTeardown() {
    assertEquals(0, eventBus.getHandlerCount(PlaceChangeEvent.TYPE));
    assertEquals(0, eventBus.getHandlerCount(PlaceChangeRequestedEvent.TYPE));

    manager.setDisplay(realDisplay);

    assertEquals(1, eventBus.getHandlerCount(PlaceChangeEvent.TYPE));
    assertEquals(1, eventBus.getHandlerCount(PlaceChangeRequestedEvent.TYPE));

    manager.setDisplay(null);

    assertEquals(0, eventBus.getHandlerCount(PlaceChangeEvent.TYPE));
    assertEquals(0, eventBus.getHandlerCount(PlaceChangeRequestedEvent.TYPE));
  }

  public void testRejected() {
    manager.setDisplay(realDisplay);

    activity1.willStop = false;

    PlaceChangeRequestedEvent<MyPlace> event = new PlaceChangeRequestedEvent<MyPlace>(
        place1);
    eventBus.fireEvent(event);
    assertFalse(event.isRejected());
    assertNull(realDisplay.widget);

    eventBus.fireEvent(new PlaceChangeEvent<Place>(place1));
    assertEquals(activity1.view, realDisplay.widget);

    event = new PlaceChangeRequestedEvent<MyPlace>(place2);
    eventBus.fireEvent(event);
    assertTrue(event.isRejected());
    assertEquals(activity1.view, realDisplay.widget);
    assertFalse(activity1.stopped);
    assertFalse(activity1.canceled);
  }

  public void testSyncDispatch() {
    manager.setDisplay(realDisplay);

    PlaceChangeRequestedEvent<MyPlace> event = new PlaceChangeRequestedEvent<MyPlace>(
        place1);
    eventBus.fireEvent(event);
    assertFalse(event.isRejected());
    assertNull(realDisplay.widget);
    assertFalse(activity1.stopped);
    assertFalse(activity1.canceled);

    eventBus.fireEvent(new PlaceChangeEvent<Place>(place1));
    assertEquals(activity1.view, realDisplay.widget);
    assertFalse(activity1.stopped);
    assertFalse(activity1.canceled);

    event = new PlaceChangeRequestedEvent<MyPlace>(place2);
    eventBus.fireEvent(event);
    assertFalse(event.isRejected());
    assertEquals(activity1.view, realDisplay.widget);
    assertFalse(activity1.stopped);
    assertFalse(activity1.canceled);

    eventBus.fireEvent(new PlaceChangeEvent<Place>(place2));
    assertEquals(activity2.view, realDisplay.widget);
    assertTrue(activity1.stopped);
    assertFalse(activity1.canceled);
  }
}
