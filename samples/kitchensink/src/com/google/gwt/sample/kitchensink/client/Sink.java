/*
 * Copyright 2007 Google Inc.
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
package com.google.gwt.sample.kitchensink.client;

import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ImageBundle;
import com.google.gwt.user.client.ui.TreeImages;

/**
 * A 'sink' is a single panel of the kitchen sink. They are meant to be lazily
 * instantiated so that the application doesn't pay for all of them on startup.
 */
public abstract class Sink extends Composite {

  /**
   * An image provider to make available images to Sinks.
   */
  public interface Images extends ImageBundle, TreeImages {
    AbstractImagePrototype gwtLogo();
  }

  /**
   * Encapsulated information about a sink. Each sink is expected to have a
   * static <code>init()</code> method that will be called by the kitchen sink
   * on startup.
   */
  public abstract static class SinkInfo {
    private Sink instance;
    private String name, description;

    public SinkInfo(String name, String desc) {
      this.name = name;
      description = desc;
    }

    public abstract Sink createInstance();

    public String getColor() {
      return "#2a8ebf";
    }

    public String getDescription() {
      return description;
    }

    public final Sink getInstance() {
      if (instance != null) {
        return instance;
      }
      return (instance = createInstance());
    }

    public String getName() {
      return name;
    }
  }

  /**
   * Called just before this sink is hidden.
   */
  public void onHide() {
  }

  /**
   * Called just after this sink is shown.
   */
  public void onShow() {
  }
}
