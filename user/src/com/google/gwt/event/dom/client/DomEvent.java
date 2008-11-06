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

import com.google.gwt.core.client.impl.PrivateMap;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Event;

/**
 * {@link DomEvent} is a subclass of AbstractEvent that provides events that map
 * to DOM Level 2 Events. It provides an additional method to access the
 * underlying native browser event object as well as a subclass of
 * AbstractEvent.Key that understands GWT event bits used by sinkEvents().
 * 
 * @param <H> handler type
 * 
 */
public abstract class DomEvent<H extends EventHandler> extends GwtEvent<H> {
  /**
   * Type class used by dom event subclasses.
   * 
   * @param <HandlerType> handler type
   */
  public static class Type<HandlerType extends EventHandler> extends
      GwtEvent.Type<HandlerType> {
    private final int nativeEventTypeInt;
    private DomEvent<HandlerType> cached;

    /**
     * Constructor.
     * 
     * @param nativeEventTypeInt the native event type
     */
    public Type(int nativeEventTypeInt) {
      this.nativeEventTypeInt = nativeEventTypeInt;
    }

    /**
     * This is a highly dangerous method that allows dom event types to be
     * triggered by the {@link DomEvent#fireNativeEvent(Event, HandlerManager)}
     * method. It should only be used by implementors supporting new dom events.
     * <p>
     * Any such dom event type must act as a flyweight around a native event
     * object.
     * </p>
     * 
     * 
     * @param nativeEventTypeInt the integer value used by sink events to set up
     *          event handling for this dom type
     * @param eventName the raw js event name
     * @param cached the cached object instance that will be used as a flyweight
     *          to wrap a native event
     */
    protected Type(int nativeEventTypeInt, String eventName,
        DomEvent<HandlerType> cached) {
      this.cached = cached;
      // All clinit activity should take place here for DomEvent.
      this.nativeEventTypeInt = nativeEventTypeInt;
      if (registered == null) {
        init();
      }
      registered.unsafePut(eventName, this);
      reverseRegistered.unsafePut(nativeEventTypeInt + "", this);
    }

    Type(int nativeEventTypeInt, String[] eventNames,
        DomEvent<HandlerType> cached) {
      this(nativeEventTypeInt, eventNames[0], cached);
      for (int i = 1; i < eventNames.length; i++) {
        registered.unsafePut(eventNames[i], this);
      }
    }

    /**
     * Gets the native {@link Event} type integer corresponding to the native
     * event.
     * 
     * @return the native event type
     */
    public int getNativeEventTypeInt() {
      return nativeEventTypeInt;
    }
  }

  private static PrivateMap<Type<?>> registered;

  private static PrivateMap<Type<?>> reverseRegistered;

  /**
   * Fires the given native event on the manager.
   * 
   * @param nativeEvent the native event
   * @param manager the event manager
   */
  public static void fireNativeEvent(Event nativeEvent, HandlerManager manager) {
    final DomEvent.Type<?> typeKey = registered.unsafeGet(nativeEvent.getType());
    if (typeKey != null) {
      if (manager != null) {
        // Store and restore native event just in case we are in recursive
        // loop.
        Event currentNative = typeKey.cached.nativeEvent;
        typeKey.cached.setNativeEvent(nativeEvent);
        manager.fireEvent(typeKey.cached);
        typeKey.cached.setNativeEvent(currentNative);
      }
    }
  }

  /**
   * Fires the given native event on the manager with a null underlying native
   * event.
   * 
   * <p>
   * This method is used in the rare case that GWT widgets have to fire native
   * events but do not have access to the corresponding native event. It allows
   * the compiler to avoid instantiating event types that are never handlers.
   * </p>
   * 
   * @deprecated should go away after triggering of native events is introduced
   * @param eventType the GWT event type representing the type of the native
   *          event.
   * @param handlers the handler manager containing the handlers
   */
  public static void unsafeFireNativeEvent(int eventType,
      HandlerManager handlers) {
    if (registered != null) {
      final DomEvent.Type<?> typeKey = reverseRegistered.unsafeGet(eventType + "");
      if (typeKey != null) {
        if (handlers != null) {
          // Store and restore native event just in case we are in recursive
          // loop.
          Event currentNative = null;
          if (typeKey.cached.isLive()) {
            currentNative = typeKey.cached.nativeEvent;
          }
          typeKey.cached.setNativeEvent(null);
          handlers.fireEvent(typeKey.cached);
          if (currentNative != null) {
            typeKey.cached.setNativeEvent(currentNative);
          }
        }
      }
    }
  }

  // This method can go away once we have eager clinits.
  static void init() {
    registered = new PrivateMap<Type<?>>();
    reverseRegistered = new PrivateMap<Type<?>>();
    // At the cost of a bit extra constanct
  }

  private Event nativeEvent;

  /**
   * Gets the underlying native event for this {@link DomEvent}.
   * 
   * @return gets the native event
   */
  public final Event getNativeEvent() {
    assertLive();
    return nativeEvent;
  }

  /**
   * Prevents the wrapped native event's default action.
   */
  public void preventDefault() {
    assertLive();
    nativeEvent.preventDefault();
  }

  /**
   * Sets the native event associated with this dom event. In general, dom
   * events should be fired using the static firing methods.
   * 
   * @param nativeEvent the native event
   */
  public final void setNativeEvent(Event nativeEvent) {
    this.nativeEvent = nativeEvent;
  }

  /**
   * Stops the propagation of the underlying native event.
   */
  public void stopPropagation() {
    assertLive();
    nativeEvent.cancelBubble(true);
  }

  @Override
  protected abstract DomEvent.Type<H> getAssociatedType();
}
