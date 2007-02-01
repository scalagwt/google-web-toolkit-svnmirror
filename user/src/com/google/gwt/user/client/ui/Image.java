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
import com.google.gwt.user.client.Event;

/**
 * A widget that displays the image at a given URL.
 * 
 * <h3>CSS Style Rules</h3>
 * <ul class="css">
 * <li>.gwt-Image { }</li>
 * </ul>
 * 
 * <p>
 * <h3>Example</h3>
 * {@example com.google.gwt.examples.ImageExample}
 * </p>
 */
public class Image extends AbstractImage implements SourcesLoadEvents {

  private LoadListenerCollection loadListeners;

  /**
   * Creates an empty image.
   */
  public Image() {
    super();
    // Add load-related events.
    sinkEvents(Event.ONLOAD | Event.ONERROR);
    setStyleName("gwt-Image");
  }

  /**
   * Creates an image with a specified URL.
   * 
   * @param url the URL of the image to be displayed
   */
  public Image(String url) {
    this();
    setUrl(url);
  }

  public void addLoadListener(LoadListener listener) {
    if (loadListeners == null) {
      loadListeners = new LoadListenerCollection();
    }
    loadListeners.add(listener);
  }

  /**
   * Gets the URL of the image.
   * 
   * @return the image URL
   */
  public String getUrl() {
    return DOM.getAttribute(getElement(), "src");
  }

  public void onBrowserEvent(Event event) {
    switch (DOM.eventGetType(event)) {
      case Event.ONLOAD: {
        if (loadListeners != null) {
          loadListeners.fireLoad(this);
        }
        break;
      }
      case Event.ONERROR: {
        if (loadListeners != null) {
          loadListeners.fireError(this);
        }
        break;
      }
      default:
        // Delegate to superclass.
        super.onBrowserEvent(event);
        break;
    }
  }

  public void removeLoadListener(LoadListener listener) {
    if (loadListeners != null) {
      loadListeners.remove(listener);
    }
  }

  /**
   * Sets the URL of the image to be displayed.
   * 
   * @param url the image URL
   */
  public void setUrl(String url) {
    DOM.setAttribute(getElement(), "src", url);
  }
}
