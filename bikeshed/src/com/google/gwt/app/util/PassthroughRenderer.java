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
package com.google.gwt.app.util;

/**
 * A no-op renderer.
 */
public class PassthroughRenderer implements Renderer<String> {

  private static PassthroughRenderer INSTANCE;
  
  /**
   * @return the instance of the no-op renderer
   */
  public static Renderer<String> instance() {
    if (INSTANCE == null) {
      INSTANCE = new PassthroughRenderer();
    }
    return INSTANCE;
  }
  
  protected PassthroughRenderer() {
  }

  public String render(String object) {
    return object;
  }

}
