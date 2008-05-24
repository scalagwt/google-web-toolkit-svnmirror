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

/**
 * An object that implements this interface contains text, which can be set and
 * retrieved using these methods.
 */
public interface HasText {

  /**
   * Gets this object's text.
   * 
   * @return the object's text
   */
  String getText();

  /**
   * Sets this object's text.
   * 
   * @param text the object's new text
   */
  void setText(String text);
}
