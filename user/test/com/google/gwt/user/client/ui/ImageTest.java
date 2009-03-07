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
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.junit.client.GWTTestCase;

/**
 * Tests for the Image widget. Images in both clipped mode and unclipped mode
 * are tested, along with the transitions between the two modes.
 */
@SuppressWarnings("deprecation")
public class ImageTest extends GWTTestCase {
  private static class TestErrorHandler implements ErrorHandler {
    private Image image;

    public TestErrorHandler(Image image) {
      this.image = image;
    }

    public void onError(ErrorEvent event) {
      fail("The image " + image.getUrl() + " failed to load.");
    }
  }

  @Deprecated
  private abstract static class TestLoadListener implements LoadListener {
    private boolean finished = false;
    private Image image;

    public TestLoadListener(Image image) {
      this.image = image;
    }

    /**
     * Mark the test as finished.
     */
    public void finish() {
      finished = true;
    }

    /**
     * @return true if the test has finished
     */
    public boolean isFinished() {
      return finished;
    }

    public void onError(Widget sender) {
      fail("The image " + image.getUrl() + " failed to load.");
    }
  }

  /**
   * Helper method that allows us to 'peek' at the private <code>state</code>
   * field in the Image object, and call the <code>state.getStateName()</code>
   * method.
   * 
   * @param image The image instance
   * @return "unclipped" if image is in the unclipped state, or "clipped" if the
   *         image is in the clipped state
   */
  public static native String getCurrentImageStateName(Image image) /*-{
      var imgState = image.@com.google.gwt.user.client.ui.Image::state;
      return imgState.@com.google.gwt.user.client.ui.Image.State::getStateName() ();
    }-*/;

  private int firedError;

  /**
   * Tests the transition from the unclipped state to the clipped state
   */
  /*
   * This test is commented out because of issue #863 public void
   * testChangeImageToClipped() { final Image image = new
   * Image("counting-forwards.png"); assertEquals("unclipped",
   * getCurrentImageStateName(image));
   * 
   * image.addLoadListener(new LoadListener() { private int onLoadEventCount =
   * 0;
   * 
   * public void onError(Widget sender) { fail("The image " + ((Image)
   * sender).getUrl() + " failed to load."); }
   * 
   * public void onLoad(Widget sender) { if
   * (getCurrentImageStateName(image).equals("unclipped")) {
   * image.setVisibleRect(12, 13, 8, 8); }
   * 
   * if (++onLoadEventCount == 2) { assertEquals(12, image.getOriginLeft());
   * assertEquals(13, image.getOriginTop()); assertEquals(8, image.getWidth());
   * assertEquals(8, image.getHeight()); assertEquals("clipped",
   * getCurrentImageStateName(image)); finishTest(); } } });
   * 
   * RootPanel.get().add(image);
   * 
   * delayTestFinish(5000); }
   */

  /**
   * Tests the transition from the clipped state to the unclipped state.
   */
  /*
   * This test is commented out because of issue #863 & #864 It fails
   * intermittently in linux hosted mode tests. public void
   * testChangeClippedImageToUnclipped() { final Image image = new
   * Image("counting-forwards.png", 12, 13, 8, 8); assertEquals("clipped",
   * getCurrentImageStateName(image));
   * 
   * image.addLoadListener(new LoadListener() { private int onLoadEventCount =
   * 0;
   * 
   * public void onError(Widget sender) { fail("The image " + ((Image)
   * sender).getUrl() + " failed to load."); }
   * 
   * public void onLoad(Widget sender) { ++onLoadEventCount; if
   * (onLoadEventCount == 1) { // Set the url after the first image loads
   * image.setUrl("counting-forwards.png"); } else if (onLoadEventCount == 2) {
   * assertEquals(0, image.getOriginLeft()); assertEquals(0,
   * image.getOriginTop()); assertEquals(32, image.getWidth()); assertEquals(32,
   * image.getHeight()); assertEquals("unclipped",
   * getCurrentImageStateName(image)); finishTest(); } } });
   * 
   * RootPanel.get().add(image);
   * 
   * delayTestFinish(5000); }
   */

  /**
   * Tests the creation of an image in unclipped mode
   */
  /*
   * This test is commented out because of issue #864 and issue #863 public void
   * testCreateImage() { final Image image = new Image("counting-forwards.png");
   * 
   * image.addLoadListener(new LoadListener() { private int onLoadEventCount =
   * 0;
   * 
   * public void onError(Widget sender) { fail("The image " + ((Image)
   * sender).getUrl() + " failed to load."); }
   * 
   * public void onLoad(Widget sender) { if (++onLoadEventCount == 1) {
   * assertEquals(32, image.getWidth()); assertEquals(32, image.getHeight());
   * finishTest(); } } });
   * 
   * RootPanel.get().add(image); assertEquals(0, image.getOriginLeft());
   * assertEquals(0, image.getOriginTop()); assertEquals("unclipped",
   * getCurrentImageStateName(image));
   * 
   * delayTestFinish(5000); }
   */

  private int firedLoad;

  /**
   * Tests the firing of onload events when
   * {@link com.google.gwt.user.client.ui.Image#setUrl(String)} is called on an
   * unclipped image.
   * 
   * Disabled because of issue #863
   */
  public void disabledTestSetUrlAndLoadEventsOnUnclippedImage() {
    final Image image = new Image();

    image.addLoadListener(new LoadListener() {
      private int onLoadEventCount = 0;

      public void onError(Widget sender) {
        fail("The image " + ((Image) sender).getUrl() + " failed to load.");
      }

      public void onLoad(Widget sender) {
        if (++onLoadEventCount == 2) {
          finishTest();
        } else {
          image.setUrl("counting-forwards.png");
        }
      }
    });

    RootPanel.get().add(image);
    image.setUrl("counting-backwards.png");
    delayTestFinish(5000);
  }

  /**
   * Tests the behavior of
   * <code>setUrlAndVisibleRect(String, int, int, int, int)</code> method on
   * an unclipped image, which causes a state transition to the clipped state.
   * 
   * Disabled because of issue #863.
   */
  public void disabledTestSetUrlAndVisibleRectOnUnclippedImage() {
    final Image image = new Image("counting-backwards.png");

    image.addLoadListener(new LoadListener() {
      private int onLoadEventCount = 0;

      public void onError(Widget sender) {
        fail("The image " + ((Image) sender).getUrl() + " failed to load.");
      }

      public void onLoad(Widget sender) {
        if (getCurrentImageStateName(image).equals("unclipped")) {
          image.setUrlAndVisibleRect("counting-forwards.png", 0, 16, 16, 16);
        }

        if (++onLoadEventCount == 2) {
          assertEquals(0, image.getOriginLeft());
          assertEquals(16, image.getOriginTop());
          assertEquals(16, image.getWidth());
          assertEquals(16, image.getHeight());
          assertEquals("clipped", getCurrentImageStateName(image));
          finishTest();
        }
      }
    });

    RootPanel.get().add(image);
    assertEquals("unclipped", getCurrentImageStateName(image));
    delayTestFinish(5000);
  }

  @Override
  public String getModuleName() {
    return "com.google.gwt.user.UserTest";
  }

  /**
   * Tests the creation of an image in clipped mode.
   */
  public void testCreateClippedImage() {
    final Image image = new Image("counting-forwards.png", 16, 16, 16, 16);

    final TestLoadListener listener = new TestLoadListener(image) {
      private int onLoadEventCount = 0;

      public void onLoad(Widget sender) {
        if (++onLoadEventCount == 1) {
          assertEquals(16, image.getWidth());
          assertEquals(16, image.getHeight());
          finish();
        }
      }
    };
    image.addLoadListener(listener);

    image.addLoadHandler(new LoadHandler() {
      private int onLoadEventCount = 0;

      public void onLoad(LoadEvent event) {
        if (++onLoadEventCount == 1) {
          assertEquals(16, image.getWidth());
          assertEquals(16, image.getHeight());
          if (listener.isFinished()) {
            finishTest();
          } else {
            fail("Listener did not fire first");
          }
        }
      }
    });
    image.addErrorHandler(new TestErrorHandler(image));

    RootPanel.get().add(image);
    assertEquals(16, image.getOriginLeft());
    assertEquals(16, image.getOriginTop());
    assertEquals("clipped", getCurrentImageStateName(image));
    delayTestFinish(5000);
  }

  @SuppressWarnings("deprecation")
  public void testLoadListenerWiring() {
    Image im = new Image();

    im.addLoadListener(new LoadListener() {

      public void onError(Widget sender) {
        ++firedError;
      }

      public void onLoad(Widget sender) {
        ++firedLoad;
      }
    });
    im.fireEvent(new LoadEvent() {
      // Replaced by Joel's event firing when possible.
    });
    assertEquals(1, firedLoad);
    assertEquals(0, firedError);
    im.fireEvent(new ErrorEvent() {
      // Replaced by Joel's event firing when possible.
    });
    assertEquals(1, firedLoad);
    assertEquals(1, firedError);
  }

  /**
   * Tests the behavior of
   * {@link com.google.gwt.user.client.ui.Image#setUrlAndVisibleRect(String,int,int,int,int)}
   * on a clipped image.
   */
  public void testSetUrlAndVisibleRectOnClippedImage() {
    final Image image = new Image("counting-backwards.png", 12, 12, 12, 12);

    final TestLoadListener listener = new TestLoadListener(image) {
      private int onLoadEventCount = 0;

      public void onLoad(Widget sender) {
        if (++onLoadEventCount == 2) {
          assertEquals(0, image.getOriginLeft());
          assertEquals(16, image.getOriginTop());
          assertEquals(16, image.getWidth());
          assertEquals(16, image.getHeight());
          assertEquals("clipped", getCurrentImageStateName(image));
          finish();
        }
      }
    };
    image.addLoadListener(listener);

    image.addLoadHandler(new LoadHandler() {
      private int onLoadEventCount = 0;

      public void onLoad(LoadEvent event) {
        if (++onLoadEventCount == 2) {
          assertEquals(0, image.getOriginLeft());
          assertEquals(16, image.getOriginTop());
          assertEquals(16, image.getWidth());
          assertEquals(16, image.getHeight());
          assertEquals("clipped", getCurrentImageStateName(image));
          if (listener.isFinished()) {
            finishTest();
          } else {
            fail("Listener did not fire first");
          }
        }
      }
    });
    image.addErrorHandler(new TestErrorHandler(image));

    RootPanel.get().add(image);
    assertEquals("clipped", getCurrentImageStateName(image));
    image.setUrlAndVisibleRect("counting-forwards.png", 0, 16, 16, 16);
    delayTestFinish(5000);
  }

  /**
   * Tests the firing of onload events when calling
   * {@link com.google.gwt.user.client.ui.Image#setVisibleRect(int,int,int,int)}
   * on a clipped image.
   */
  public void testSetVisibleRectAndLoadEventsOnClippedImage() {
    final Image image = new Image("counting-backwards.png", 16, 16, 16, 16);

    final TestLoadListener listener = new TestLoadListener(image) {
      private int onLoadEventCount = 0;

      public void onLoad(Widget sender) {
        if (++onLoadEventCount == 4) {
          finish();
        }
      }
    };
    image.addLoadListener(listener);

    image.addLoadHandler(new LoadHandler() {
      private int onLoadEventCount = 0;

      public void onLoad(LoadEvent event) {
        if (++onLoadEventCount == 4) {
          if (listener.isFinished()) {
            finishTest();
          } else {
            fail("Listener did not fire first");
          }
        }
      }
    });
    image.addErrorHandler(new TestErrorHandler(image));

    RootPanel.get().add(image);
    image.setVisibleRect(0, 0, 16, 16);
    image.setVisibleRect(0, 0, 16, 16);
    image.setVisibleRect(16, 0, 16, 16);
    image.setVisibleRect(16, 8, 8, 8);
    delayTestFinish(5000);
  }

  /**
   * Tests that wrapping an existing DOM element works if you call
   * setUrlAndVisibleRect() on it.
   */
  public void testWrapThenSetUrlAndVisibleRect() {
    String uid = Document.get().createUniqueId();
    HTML html = new HTML("<img id='" + uid
        + "' src='counting-backwards.png' width='16' height='16'>");
    RootPanel.get().add(html);
    final Image image = Image.wrap(Document.get().getElementById(uid));

    assertEquals(0, image.getOriginLeft());
    assertEquals(0, image.getOriginTop());
    assertEquals(16, image.getWidth());
    assertEquals(16, image.getHeight());
    assertEquals("unclipped", getCurrentImageStateName(image));

    image.setUrlAndVisibleRect("counting-forwards.png", 0, 16, 16, 16);
    assertEquals(0, image.getOriginLeft());
    assertEquals(16, image.getOriginTop());
    assertEquals(16, image.getWidth());
    assertEquals(16, image.getHeight());
    assertEquals("clipped", getCurrentImageStateName(image));
  }

}
