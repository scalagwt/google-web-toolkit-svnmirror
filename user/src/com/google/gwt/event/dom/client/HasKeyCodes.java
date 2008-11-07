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

package com.google.gwt.event.dom.client;

/**
 * Adapting from {@link com.google.gwt.user.client.ui.KeyboardListener}. Without
 * this interface the migration headache from keyboard listeners to key handlers
 * becomes a pain, as almost all current listeners use the short form
 * <codE>KEY_ALT</code> when referring to key codes.
 */

// Need to have a team discussion to decide if we want this pattern in general.
// Until then, just turning checkstyle off.
// CHECKSTYLE_OFF
@SuppressWarnings("all")
public interface HasKeyCodes {
  /**
   * Alt key code.
   */
  int KEY_ALT = 18;

  /**
   * Backspace key code.
   */
  int KEY_BACKSPACE = 8;

  /**
   * Control key code.
   */
  int KEY_CTRL = 17;

  /**
   * Delete key code.
   */
  int KEY_DELETE = 46;

  /**
   * Down arrow code.
   */
  int KEY_DOWN = 40;

  /**
   * End key code.
   */
  int KEY_END = 35;
  /**
   * Enter key code.
   */
  int KEY_ENTER = 13;
  /**
   * Escape key code.
   */
  int KEY_ESCAPE = 27;
  /**
   * Home key code.
   */
  int KEY_HOME = 36;
  /**
   * Left key code.
   */
  int KEY_LEFT = 37;
  /**
   * Page down key code.
   */
  int KEY_PAGEDOWN = 34;
  /**
   * Page up key code.
   */
  int KEY_PAGEUP = 33;
  /**
   * Right arrow key code.
   */
  int KEY_RIGHT = 39;

  /**
   * Shift key code.
   */
  int KEY_SHIFT = 16;
  /**
   * Tab key code.
   */
  int KEY_TAB = 9;

  /**
   * Up Arrow key code.
   */
  int KEY_UP = 38;

}
