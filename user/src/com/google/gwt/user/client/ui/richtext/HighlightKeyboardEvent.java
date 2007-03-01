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
package com.google.gwt.user.client.ui.richtext;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;

/**
 * The event used to denote a keypress on a {@link Highlight}.
 */
public class HighlightKeyboardEvent extends HighlightEvent {
  /**
   * Denotes the 'alt' key.
   */
  public static final int KEY_ALT = 18;
  /**
   * Denotes the 'backspace' key.
   */
  public static final int KEY_BACKSPACE = 8;
  /**
   * Denotes the 'ctrl' key.
   */
  public static final int KEY_CTRL = 17;
  /**
   * Denotes the 'delete' key.
   */
  public static final int KEY_DELETE = 46;
  /**
   * Denotes the 'down' key.
   */
  public static final int KEY_DOWN = 40;
  /**
   * Denotes the 'end' key.
   */
  public static final int KEY_END = 35;
  /**
   * Denotes the 'enter' key.
   */
  public static final int KEY_ENTER = 13;
  /**
   * Denotes the 'escape' key.
   */
  public static final int KEY_ESCAPE = 27;
  /**
   * Denotes the 'home' key.
   */
  public static final int KEY_HOME = 36;
  /**
   * Denotes the 'left' key.
   */
  public static final int KEY_LEFT = 37;
  /**
   * Denotes the 'page down' key.
   */
  public static final int KEY_PAGEDOWN = 34;
  /**
   * Denotes the 'page up' key.
   */
  public static final int KEY_PAGEUP = 33;
  /**
   * Denotes the 'right' key.
   */
  public static final int KEY_RIGHT = 39;
  /**
   * Denotes the 'shift' key.
   */
  public static final int KEY_SHIFT = 16;
  /**
   * Denotes the 'tab' key.
   */
  public static final int KEY_TAB = 9;
  /**
   * Denotes the 'up' key.
   */
  public static final int KEY_UP = 38;

  /**
   * Denotes the 'alt' modifier.
   */
  public static final int MODIFIER_ALT = 4;

  /**
   * Denotes the 'ctrl' modifier.
   */
  public static final int MODIFIER_CTRL = 2;

  /**
   * Denotes the 'shift' modifier.
   */
  public static final int MODIFIER_SHIFT = 1;

  /**
   * Creates a new {@link HighlightKeyboardEvent}.
   * 
   * @param highlight the highlight that received the event
   * @param domEvent the underlying {@link DOM} {@link Event} in the richTextArea
   *          DOM
   */
  HighlightKeyboardEvent(Highlight highlight, Event domEvent) {
    super(highlight, domEvent);
  }

  /**
   * Gets whether the Alt modifier key was down.
   * 
   * @return true if the Alt key was was down
   */
  public boolean getAltKey() {
    return DOM.eventGetAltKey(event);
  }

  /**
   * Gets whether the Control modifier key was down.
   * 
   * @return true if the Control key was was down
   */
  public boolean getControlKey() {
    return DOM.eventGetCtrlKey(event);
  }

  /**
   * Gets the key code of the event.
   * 
   * @return the key code involved
   */
  public int getKeyCode() {
    return DOM.eventGetKeyCode(getEvent());
  }

  /**
   * Gets whether the Shift modifier key was down.
   * 
   * @return true if the Shift key was was down
   */
  public boolean getShiftKey() {
    return DOM.eventGetShiftKey(event);
  }

}
