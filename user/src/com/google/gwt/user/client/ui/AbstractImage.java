/*
 * Copyright 2006 Google Inc.
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

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;

import java.util.HashMap;

/**
 * Abstract superclass of various image types.
 */
public class AbstractImage extends Widget implements SourcesClickEvents,
    SourcesMouseEvents {

  /**
   * This map is used to store prefetched images. If a reference is not kept to
   * the prefetched image objects, they can get garbage collected, which
   * sometimes keeps them from getting fully fetched.
   */
  private static HashMap prefetchImages = new HashMap();

  /**
   * Causes the browser to pre-fetch the image at a given URL.
   * 
   * @param url the URL of the image to be prefetched
   */
  public static void prefetch(String url) {
    Element img = DOM.createImg();
    DOM.setAttribute(img, "src", url);
    prefetchImages.put(url, img);
  }

  private ClickListenerCollection clickListeners;
  private MouseListenerCollection mouseListeners;

  /**
   * Creates an empty image.
   */
  public AbstractImage() {
    this(DOM.createImg());
  }

  /**
   * Allows subclass constructors to specify alternate root elements.
   */
  protected AbstractImage(Element elem) {
    setElement(elem);
    sinkEvents(Event.ONCLICK | Event.MOUSEEVENTS | Event.ONERROR);
  }

  public void addClickListener(ClickListener listener) {
    if (clickListeners == null) {
      clickListeners = new ClickListenerCollection();
    }
    clickListeners.add(listener);
  }

  public void addMouseListener(MouseListener listener) {
    if (mouseListeners == null) {
      mouseListeners = new MouseListenerCollection();
    }
    mouseListeners.add(listener);
  }

  public void onBrowserEvent(Event event) {
    switch (DOM.eventGetType(event)) {
      case Event.ONCLICK: {
        if (clickListeners != null) {
          clickListeners.fireClick(this);
        }
        break;
      }
      case Event.ONMOUSEDOWN:
      case Event.ONMOUSEUP:
      case Event.ONMOUSEMOVE:
      case Event.ONMOUSEOVER:
      case Event.ONMOUSEOUT: {
        if (mouseListeners != null) {
          mouseListeners.fireMouseEvent(this, event);
        }
        break;
      }
    }
  }

  public void removeClickListener(ClickListener listener) {
    if (clickListeners != null) {
      clickListeners.remove(listener);
    }
  }

  public void removeMouseListener(MouseListener listener) {
    if (mouseListeners != null) {
      mouseListeners.remove(listener);
    }
  }
}
