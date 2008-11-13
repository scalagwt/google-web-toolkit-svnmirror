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
package com.google.gwt.event.shared;

import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.gwt.user.client.Command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Manager responsible for adding handlers to event sources and firing those
 * handlers on passed in events.
 */
public class HandlerManager {
  /**
   * The default Handler manager's handler registry for running in Java code.
   */
  private static class JavaHandlerRegistry extends
      HashMap<GwtEvent.Type<?>, ArrayList<?>> {

    public <H extends EventHandler> void addHandler(Type<H> type, H handler) {
      ArrayList<H> l = get(type);
      if (l == null) {
        l = new ArrayList<H>();
        super.put(type, l);
      }
      l.add(handler);
    }

    public <H extends EventHandler> void fireEvent(GwtEvent<H> event) {
      Type<H> type = event.getAssociatedType();
      int count = getHandlerCount(type);
      for (int i = 0; i < count; i++) {
        H handler = getHandler(type, i);
        event.dispatch(handler);
      }
    }

    public <H extends EventHandler> H getHandler(GwtEvent.Type<H> eventKey,
        int index) {
      ArrayList<H> l = get(eventKey);
      return l.get(index);
    }

    public int getHandlerCount(GwtEvent.Type<?> eventKey) {
      ArrayList<?> l = super.get(eventKey);
      if (l == null) {
        return 0;
      } else {
        return l.size();
      }
    }

    public <H> void removeHandler(GwtEvent.Type<H> eventKey, H handler) {
      ArrayList<H> l = get(eventKey);
      if (l != null) {
        boolean result = l.remove(handler);
        assert result : "Tried to remove unknown handler";
      }
    }

    @SuppressWarnings("unchecked")
    private <H> ArrayList<H> get(GwtEvent.Type<H> type) {
      // This cast is safe because we control the puts.
      return (ArrayList<H>) super.get(type);
    }
  }

  // Used to optimize the JavaScript handler container structure.
  private static int EXPECTED_HANDLERS = 5;

  private static final boolean useJs = false;

  private static int index;

  // Used to assign hash codes to gwt event types so they are easy to store in a
  // js structure.
  static int createKeyIndex() {
    // Need to leave space for the size and the unflattened list if we end up
    // needing it.
    index += EXPECTED_HANDLERS + 2;
    return index;
  }

  private int firingDepth = 0;
  // Only JavaHandlerRegistry is supported for now as we need to decide if we
  // need the more complicated js structure or not.
  private JavaHandlerRegistry javaRegistry;
  private JavaHandlerRegistry javaScriptRegistry;

  // source of the event.
  private final Object source;
  
  // Add and remove operations received during dispatch.
  private List<Command> deferredDeltas;
  
  /**
   * Creates a handler manager with the given source.
   * 
   * @param source the event source
   */
  public HandlerManager(Object source) {
    if (useJs) {
      javaScriptRegistry = JavaScriptObject.createObject().cast();
    } else {
      javaRegistry = new JavaHandlerRegistry();
    }
    this.source = source;
  }

  /**
   * Adds a handle.
   * 
   * @param <H> The type of handler
   * @param type the event type associated with this handler
   * @param handler the handler
   * @return the handler registration, can be stored in order to remove the
   * handler later
   */
  public <H extends EventHandler> HandlerRegistration addHandler(
      GwtEvent.Type<H> type, final H handler) {
    if (firingDepth > 0) {
      enqueueAdd(type, handler);
    } else {
      doAdd(type, handler);
    }
    return new DefaultHandlerRegistration(this, type, handler);
  }

  /**
   * Fires the given event to the handlers listening to the event's type.
   * 
   * @param event the event
   */
  // Final so we can manage buffering adds and removes without a subclass
  // throwing all our calculations off.
  public final void fireEvent(GwtEvent<?> event) {
    // If it not live we should revive it.
    if (event.isLive() == false) {
      event.revive();
    }
    Object oldSource = event.getSource();
    event.setSource(source);
    try {
      firingDepth++;
      if (useJs) {
        javaScriptRegistry.fireEvent(event);
      } else {
        javaRegistry.fireEvent(event);
      }
    } finally {
      firingDepth--;
      if (firingDepth == 0) {
        handleQueuedAddsAndRemoves();
      }
    }
    if (oldSource == null) {
      // This was my event, so I should kill it now that I'm done.
      event.onRelease();
    } else {
      // Restoring the source for the next handler to use.
      event.setSource(oldSource);
    }
  }

  /**
   * Gets the handler at the given index.
   * 
   * @param <H> the event handler type
   * @param index the index
   * @param type the handler's event type
   * @return the given handler
   */
  public <H extends EventHandler> H getHandler(GwtEvent.Type<H> type, int index) {
    assert index < getHandlerCount(type) : "handlers for " + type.getClass()
        + " have size: " + getHandlerCount(type)
        + " so do not have a handler at index: " + index;
    if (useJs) {
      return javaScriptRegistry.getHandler(type, index);
    } else {
      return javaRegistry.getHandler(type, index);
    }
  }

  /**
   * Gets the number of handlers listening to the event type.
   * 
   * @param type the event type
   * @return the number of registered handlers
   */
  public int getHandlerCount(Type<?> type) {
    if (useJs) {
      return javaScriptRegistry.getHandlerCount(type);
    } else {
      return javaRegistry.getHandlerCount(type);
    }
  }

  /**
   * Are there handlers in this manager listening to the given event type?
   * 
   * @param type the event type
   * @return are handlers listening on the given event type
   */
  public boolean isEventHandled(Type<?> type) {
    return getHandlerCount(type) > 0;
  }

  /**
   * Removes the given handler from the specified event type. Normally,
   * applications should call {@link HandlerRegistration#removeHandler()}
   * instead.
   * 
   * @param <H> handler type
   * 
   * @param type the event type
   * @param handler the handler
   * @deprecated We currently believe this method will not be needed after
   * listeners are removed in GWT 2.0. If you have a use case for it after that
   * time, please add your comments to issue
   * http://code.google.com/p/google-web-toolkit/issues/detail?id=3102
   */
  public <H extends EventHandler> void removeHandler(GwtEvent.Type<H> type,
      final H handler) {
    if (firingDepth > 0) {
      enqueueRemove(type, handler);
    } else {
      doRemove(type, handler);
    }
  }

  private void defer(Command command) {
    if (deferredDeltas == null) {
      deferredDeltas = new ArrayList<Command>();
    }
    deferredDeltas.add(command);
  }

  private <H extends EventHandler> void doAdd(GwtEvent.Type<H> type,
      final H handler) {
    if (useJs) {
      javaScriptRegistry.addHandler(type, handler);
    } else {
      javaRegistry.addHandler(type, handler);
    }
  }

  private <H extends EventHandler> void doRemove(GwtEvent.Type<H> type,
      final H handler) {
    if (useJs) {
      javaScriptRegistry.removeHandler(type, handler);
    } else {
      javaRegistry.removeHandler(type, handler);
    }
  }

  private <H extends EventHandler> void enqueueAdd(final GwtEvent.Type<H> type,
      final H handler) {
    defer(new Command() {
      public void execute() {
        doAdd(type, handler);
      }
    });
  }

  private <H extends EventHandler> void enqueueRemove(
      final GwtEvent.Type<H> type, final H handler) {
    defer(new Command() {
      public void execute() {
        doRemove(type, handler);
      }
    });
  }

  @SuppressWarnings("unchecked")
  private void handleQueuedAddsAndRemoves() {
    if (deferredDeltas != null) {
      try {
        for (Command c : deferredDeltas) {
         c.execute(); 
        }
      } finally {
        deferredDeltas = null;
      }
    }
  }
}
