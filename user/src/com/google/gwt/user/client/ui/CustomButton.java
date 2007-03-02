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

package com.google.gwt.user.client.ui;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
 
/**
 * Style Button with built in support for pressed, disabled, and hovering
 * attributes. By default, a <code>StyleButton</code> acts as a toggle button.
 * Once a button is pressed it will not be depressed until the button is clicked
 * again or <code>setPressed(true)</code> is called.
 * <p>
 * The three attributes are displayed using the display states defined below.
 * 
 * <table border=4>
 * <tr>
 * <td><b>attributes</b></td>
 * <td><b>state</b></td>
 * </tr>
 * 
 * <tr>
 * <td> none</td>
 * <td>up</td>
 * </tr>
 * 
 * <tr>
 * <td>pressed</td>
 * <td>down</td>
 * </tr>
 * 
 * <tr>
 * <td>hovering</td>
 * <td>upHover</td>
 * </tr>
 * 
 * 
 * <tr>
 * <td>disabled</td>
 * <td>upDisabled</td>
 * </tr>
 * 
 * <tr>
 * <td>pressed, hovering</td>
 * <td>downHover</td>
 * </tr>
 * 
 * <tr>
 * <td>pressed, disabled</td>
 * <td>downDisabled</td>
 * </tr>
 * </table>
 * <p>
 * 
 * 
 * 
 * Each state has it's own style. For example, downHover is assigned, by
 * default, the css class <code> gwt-StyleButton-downHover </code>. Optionally,
 * each state can be assigned is own image or html face.
 * 
 * 
 * ButtonStyle.setPressed(true); ButtonStyle.setHover(false);
 * ButtonStyle.setFace(boolean isPressed, boolean
 * 
 */
public class CustomButton extends FocusWidget implements SourcesKeyboardEvents {
  /**
   * Represents the button's current state.
   */
  public abstract static class State {
    private static final String STYLE_HTML_FACE = "html-face";
    private final State delegateTo;
    private Element face;

    /**
     * Constructor for <code>State</code>. Creates a new state that delegates
     * to the supplied state.
     * 
     * @param delegateTo default display face provider
     */
    private State(State delegateTo) {
      this.delegateTo = delegateTo;
    }

    /**
     * Set the state's face display as an image.
     * 
     * @param image image to set as state display
     */
    public final void setFace(AbstractImage image) {
      // Cloning face in order to suppress extra click events.
      face = image.getElement();
      DOM.sinkEvents(face, 0);
    }

    /**
     * Sets the state's display face as text.
     * 
     * @param text text to set as state's display text
     */
    public final void setFace(String text) {
      setFace(text, false);
    }

    /**
     * Set the state's display face as text.
     * 
     * @param text text to set as state's display text
     * @param asHTML <code>true</code> to treat the specified text as html
     */
    public void setFace(String text, boolean asHTML) {
      face = DOM.createDiv();
      if (asHTML) {
        DOM.setInnerHTML(face, text);
      } else {
        DOM.setInnerText(face, text);
      }
      UIObject.setStyleName(face, STYLE_HTML_FACE, true);
    }

    /**
     * Sets the state's display face.
     */

    public final String toString() {
      return this.getName();
    }

    /**
     * Get the name of the state. This property is also used as a modifier on
     * the <code>StyleButton</code> style.
     * <p>
     * For instance, if the <code>StyleButton</code> style is "gwt-PushButton"
     * and the state name is "up", then the CSS class name will be
     * "gwt-StyleButton-up".
     * 
     * @return the state's name
     */
    abstract String getName();

    /**
     * Gets the ID associated with this state. This will be a bitwise and of all
     * of the attributes that comprise this state.
     */
    abstract int getStateID();

    /**
     * Gets the display face associated with this state.
     */
    private Element getFace() {
      if (face == null) {
        if (delegateTo == null) {
          // provide a default face as none was supplied.
          face = DOM.createDiv();
          return face;
        } else {
          return delegateTo.getFace();
        }
      } else {
        return face;
      }
    }
  }

  /**
   * Pressed Attribute bit.
   */
  private static final int PRESSED_ATTRIBUTE = 1;

  /**
   * Hovering Attribute bit.
   */
  private static final int HOVERING_ATTRIBUTE = 2;

  /**
   * Disabled Attribute bit.
   */
  private static final int DISABLED_ATTRIBUTE = 4;

  /**
   * ID for up state.
   */
  private static final int UP = 0;

  /**
   * ID for down state.
   */
  private static final int DOWN = PRESSED_ATTRIBUTE;

  /**
   * ID for upHover state.
   */
  private static final int UP_HOVER = HOVERING_ATTRIBUTE;

  /**
   * ID for downHover state.
   */
  private static final int DOWN_HOVER = PRESSED_ATTRIBUTE | HOVERING_ATTRIBUTE;

  /**
   * ID for upDisabled state.
   */
  private static final int UP_DISABLED = DISABLED_ATTRIBUTE;

  /**
   * ID for downDisabled state.
   */
  private static final int DOWN_DISABLED = DOWN | DISABLED_ATTRIBUTE;

  /**
   * Should the button act as a toggle button?
   */
  private boolean toggling = false;

  /**
   * Base style name. By default gwt-StyleButton.
   */
  private String baseStyleName;

  /**
   * The button's current face.
   */
  private Element curFace;

  /**
   * The button's current state.
   */
  private State curState;

  /**
   * State for up.
   */
  private State up;

  /**
   * State for down.
   */
  private State down;

  /**
   * State for downHover.
   */
  private State downHover;

  /**
   * State for upHover.
   */
  private State upHover;

  /**
   * State for upDisabled.
   */
  private State upDisabled;

  /**
   * State for downDisabled.
   */
  private State downDisabled;

  /**
   * 
   * Constructor for <code>CustomButton</code>.
   */
  public CustomButton() {
    super(FocusWidget.getFocusImpl().createFocusable());
    sinkEvents(Event.ONCLICK | Event.MOUSEEVENTS);
    setUp(createState(null, "up", UP));
  }

  /**
   * 
   * Constructor for <code>StyleButton</code>. The supplied image is used as
   * the default face for all states.
   * 
   * @param upImage image for the default face of the button
   */
  public CustomButton(AbstractImage upImage) {
    this();
    getUp().setFace(upImage);
  }

  /**
   * 
   * Constructor for <code>StyleButton</code>.
   * 
   * @param upImage image for the default face of the button
   * @param downImage image for the down state of the button
   */
  public CustomButton(AbstractImage upImage, AbstractImage downImage) {
    this(upImage);
    getDown().setFace(downImage);
  }

  /**
   * Constructor for <code>StyleButton</code>.
   * 
   * @param upImage image for the default face of the button
   * @param downImage image for the down state of the button
   * @param listener clickListener
   */
  public CustomButton(AbstractImage upImage, AbstractImage downImage,
      ClickListener listener) {
    this(upImage, listener);
    getDown().setFace(downImage);
  }

  /**
   * 
   * Constructor for <code>StyleButton</code>. The supplied image is used as
   * the default face for all states.
   * 
   * @param upImage the default face of the button.
   * @param listener the click listener
   */
  public CustomButton(AbstractImage upImage, ClickListener listener) {
    this(upImage);
    addClickListener(listener);
  }

  /**
   * 
   * Constructor for <code>StyleButton</code>. The supplied html is used as
   * the default face for all states.
   * 
   * @param htmlFace the default face of the button.
   */
  public CustomButton(String htmlFace) {
    this();
    getUp().setFace(htmlFace);
  }

  /**
   * Constructor for <code>StyleButton</code>. The supplied html is used as
   * the default face for all states.
   * 
   * @param htmlFace the default face of the button
   * @param listener the click listener
   */
  public CustomButton(String htmlFace, ClickListener listener) {
    this(htmlFace);
    addClickListener(listener);
  }

  /**
   * 
   * Constructor for <code>CustomButton</code>.
   * 
   * @param string
   * @param string2
   */
  public CustomButton(String upText, String downText) {
    this(upText);
  }

  /**
   * Gets the down state.
   * 
   * @return the down state
   */
  public final State getDown() {
    if (down == null) {
      setDown(createState(getUp(), "down", DOWN));
    }
    return down;
  }

  /**
   * Gets the downDisabled state.
   * 
   * @return the downDisabled state
   */
  public final State getDownDisabled() {
    if (downDisabled == null) {
      setDownDisabled(createState(getDown(), "down-disabled", DOWN_DISABLED));
    }
    return downDisabled;
  }

  /**
   * Gets the downHover state.
   * 
   * @return the downHover state
   */
  public final State getDownHover() {
    if (downHover == null) {
      setDownHover(createState(getDown(), "down-hover", DOWN_HOVER));
    }
    return downHover;
  }

  public final String getStyleName() {
    return baseStyleName;
  }

  /**
   * Gets the up state.
   * 
   * @return the up state
   */
  public final State getUp() {
    return up;
  }

  /**
   * Gets the upDisabled state.
   * 
   * @return the upDisabled state
   */
  public final State getUpDisabled() {
    if (upDisabled == null) {
      setUpDisabled(createState(getUp(), "up-disabled", UP_DISABLED));
    }
    return upDisabled;
  }

  /**
   * Gets the upHover state.
   * 
   * @return the upHover state
   */
  public final State getUpHover() {
    if (upHover == null) {
      setUpHover(createState(getUp(), "up-hover", UP_HOVER));
    }
    return upHover;
  }

  /**
   * Is this button disabled?
   * 
   * @return <code>true</code> if the button is disabled
   */
  public final boolean isDisabled() {
    return (DISABLED_ATTRIBUTE & curState.getStateID()) > 0;
  }

  /**
   * Is this button pressed?
   * 
   * @return <code>true</code> if the button is pressed
   */
  public final boolean isPressed() {
    return (PRESSED_ATTRIBUTE & curState.getStateID()) > 0;
  }

  /**
   * Is this button a toggle button?
   * 
   * @return whether this button is a toggle button. Defaults to true
   */
  public boolean isToggleButton() {
    return toggling;
  }

  public void onBrowserEvent(Event event) {
    // Should not act on button if disabled.
    if (isDisabled()) {
      return;
    }

    int type = DOM.eventGetType(event);
    switch (type) {
      case Event.ONMOUSEOUT:
        setHovering(false);
        break;
      case Event.ONMOUSEOVER:
        setHovering(true);
        break;
      case Event.ONMOUSEDOWN:
      case Event.ONMOUSEUP:

        if (!toggling) {
          setPressed(type == Event.ONMOUSEDOWN);
        }
        break;
      case Event.ONCLICK:
        if (toggling) {
          togglePress();
        }
        break;
    }
    super.onBrowserEvent(event);
  }

  /**
   * Sets whether this button is disabled.
   * 
   * @param disabled <code>true</code> to disabled the button,
   *          <code>false</code> to disable it
   */
  public final void setDisabled(boolean disabled) {
    if (isDisabled() != disabled) {
      toggleDisabled();
    }
  }

  /**
   * Sets whether this button is pressed.
   * 
   * @param pressed <code>true</code> to press the button, <code>false</code>
   *          otherwise
   */
  public final void setPressed(boolean pressed) {
    if (pressed != isPressed()) {
      togglePress();
    }
  }

  public final void setStyleName(String styleName) {
    if (styleName == null) {
      throw new IllegalStateException("Illegal State");
    }

    baseStyleName = styleName;
    // force state refresh
    State temp = curState;
    curState = null;
    setCurrentState(temp);
  }

  /**
   * Sets whether this button behaves as a toggle button.
   * 
   * @param togglingBehavior is this button a toggle button
   */
  public void setToggleBehavior(boolean togglingBehavior) {
    this.toggling = togglingBehavior;
  }

  /**
   * Overridden on attach to ensure that a button state has been chosen before
   * the button is displayed.
   */
  protected void onAttach() {
    if (curState == null) {
      finishSetup();
    }
    super.onAttach();
  }

  /**
   * Gets the current state. Used for debugging
   * 
   * @return the current state
   */
  State getCurrentState() {
    return curState;
  }

  /**
   * Is the mouse hovering over this button?
   * 
   * @return <code>true</code> if the mouse is hovering
   */
  final boolean isHovering() {
    return (HOVERING_ATTRIBUTE & curState.getStateID()) > 0;
  }

  /**
   * Sets whether this button is hovering.
   * 
   * @param hovering is this button hovering?
   */
  final void setHovering(boolean hovering) {
    if (hovering != isHovering()) {
      toggleHovering();
    }
  }

  private State createState(State delegateTo, final String name,
      final int stateID) {
    return new State(delegateTo) {

      public String getName() {
        return name;
      }

      int getStateID() {
        return stateID;
      }
    };
  }

  /**
   * Common setup between constructors.
   */
  private void finishSetup() {
    setCurrentState(getUp());
    setStyleName("gwt-StyleButton");
  }

  private State getStateFromID(int id) {
    switch (id) {
      case DOWN:
        return getDown();
      case UP:
        return getUp();
      case DOWN_HOVER:
        return getDownHover();
      case UP_HOVER:
        return getUpHover();
      case UP_DISABLED:
        return getUpDisabled();
      case DOWN_DISABLED:
        return getDownDisabled();
      default:
        throw new IllegalStateException(id + " is not a known state id.");
    }
  }

  /**
   * Sets the current state based on the stateID.
   * 
   * @param stateID sets the new state of the button
   */
  private void setCurrentState(int stateID) {
    State newState = getStateFromID(stateID);
    setCurrentState(newState);
  }

  private void setCurrentState(State newState) {
    if (curState != newState) {
      curState = newState;
      Element newFace = newState.getFace();
      if (curFace != newFace) {
        if (curFace != null) {
          DOM.removeChild(getElement(), curFace);
        }
        curFace = newFace;
        DOM.appendChild(getElement(), curFace);
      }

      super.setStyleName(baseStyleName + "-" + curState.getName());
    }
  }

  /**
   * Sets the down state.
   * 
   * @param down the down state
   */
  private void setDown(State down) {
    this.down = down;
  }

  /**
   * Sets the downDisabled state.
   * 
   * @param downDisabled downDisabled state
   */
  private void setDownDisabled(State downDisabled) {
    this.downDisabled = downDisabled;
  }

  /**
   * Sets the downHover state.
   * 
   * @param hoverDown hoverDown state
   */
  private void setDownHover(State hoverDown) {
    this.downHover = hoverDown;
  }

  /**
   * Sets the up state.
   * 
   * @param up up state
   */
  private void setUp(State up) {
    this.up = up;
  }

  /**
   * Sets the upDisabled state.
   * 
   * @param upDisabled upDisabled state
   */
  private void setUpDisabled(State upDisabled) {
    this.upDisabled = upDisabled;
  }

  /**
   * Sets the upHover state.
   * 
   * @param upHover upHover state
   */
  private void setUpHover(State upHover) {
    this.upHover = upHover;
  }

  /**
   * Toggle the disabled attribute.
   */
  private void toggleDisabled() {
    // Add disabled.
    int newStateID = curState.getStateID() ^ DISABLED_ATTRIBUTE;

    // Remove hovering.
    newStateID &= ~HOVERING_ATTRIBUTE;

    // Sets the current state.
    setCurrentState(newStateID);
  }

  /**
   * Toggle the hovering attribute.
   */
  private void toggleHovering() {
    // Add hovering.
    int newStateID = curState.getStateID() ^ HOVERING_ATTRIBUTE;

    // Remove disabled.
    newStateID &= ~DISABLED_ATTRIBUTE;
    setCurrentState(newStateID);
  }

  /**
   * Toggle the pressed attribute.
   * 
   */
  private void togglePress() {
    int newStateID = curState.getStateID() ^ PRESSED_ATTRIBUTE;
    setCurrentState(newStateID);
  }
}
