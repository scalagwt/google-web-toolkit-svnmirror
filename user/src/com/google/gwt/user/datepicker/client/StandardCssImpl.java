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

package com.google.gwt.user.datepicker.client;

/**
 * An impl class that provides basic functionally to support the standard
 * handling of css interface classes.
 * 
 * Applications that care about Css size and performance should use css
 * resources.
 * 
 */
class StandardCssImpl {
  private String baseName;
  private String widgetName;

  /**
   * Constructor.
   */
  public StandardCssImpl(String widgetName, String baseName) {
    this.widgetName = widgetName;
    this.baseName = baseName;
  }

  /**
   * Gets the style name.
   * 
   * @return the style name.
   */
  public String getBaseStyleName() {
    return baseName;
  }
 
  public String getWidgetStyleName() {
    return widgetName;
  }

  /**
   * Prepends the base name to the given style.
   * 
   * @param style style name
   * @return style name
   */
  protected String wrap(String style) {
    return baseName + style;
  }
}