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

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.shared.GwtEvent.Type;

/**
 * Default JavaScript handler registry. This is in the shared package so we
 * don't have to make it public, should never be called outside of a GWT runtime
 * environment.
 * 
 * The JsHandlerRegistry makes use of the fact that in the large majority of
 * cases, only one or two handlers are added for each event type. Therefore,
 * rather than storing handlers in a list of lists, we store then in a single
 * flattened array with an escape clause to handle the rare case where we have
 * more handlers then expected.
 */
class JsHandlerRegistry extends JavaScriptObject {

  public static JsHandlerRegistry create() {
    return (JsHandlerRegistry) JavaScriptObject.createObject();
  }

  /**
   * Required constructor.
   */
  protected JsHandlerRegistry() {
  }

  public final <H extends EventHandler> void addHandler(HandlerManager manager,
      Type<H> type, H myHandler) {

    // The base is the equivalent to a c pointer into the flattened handler
    // data structure.
    int base = type.hashCode();
    int count = getCount(base);
    boolean flattened = isFlattened(base);
    H handler = myHandler;
    // If we already have the maximum number of handlers we can store in the
    // flattened data structure, store the handlers in an external list
    // instead.
    if ((count == HandlerManager.EXPECTED_HANDLERS) & flattened) {
      // As long as we are only adding to the end of a handler list, should
      // not need to queue.
      if (manager.firingDepth > 0) {
        manager.enqueueAdd(type, myHandler);
        return;
      }
      unflatten(base);
      flattened = false;
    }
    if (flattened) {
      setFlatHandler(base, count, handler);
    } else {
      setHandler(base, count, handler);
    }
    setCount(base, count + 1);
  }

  public final <T> void clearHandlers(Type<T> type) {
    int base = type.hashCode();
    // Clearing handlers is relatively unusual, so the cost of unflattening the
    // handler list is justified by the smaller code.
    unflatten(base);

    // Replace the list of handlers.
    setHandlerList(base + 1, JavaScriptObject.createArray());
    setCount(base, 0);
  }

  // Adding the extra field to js getHandler() broke the inlining so using the
  // unsafe cast instead.
  @SuppressWarnings("unchecked")
  public final <H extends EventHandler> void fireEvent(GwtEvent<H> event) {
    Type<H> type = event.getAssociatedType();
    int base = type.hashCode();
    int count = getCount(base);
    boolean isFlattened = isFlattened(base);
    if (isFlattened) {
      for (int i = 0; i < count; i++) {
        // Gets the given handler to fire.
        H handler = (H) getFlatHandler(base, i);
        // Fires the handler.
        event.dispatch(handler);
      }
    } else {
      JavaScriptObject handlers = getHandlers(base);
      for (int i = 0; i < count; i++) {
        // Gets the given handler to fire.
        H handler = (H) getHandler(handlers, i);

        // Fires the handler.
        event.dispatch(handler);
      }
    }
  }

  // Adding the extra field to js getHandler() broke the inlining so using the
  // unsafe cast instead.
  @SuppressWarnings("unchecked")
  public final <H extends EventHandler> H getHandler(
      GwtEvent.Type<H> type, int index) {
    int base = type.hashCode();
    int count = getCount(base);
    if (index >= count) {
      throw new IndexOutOfBoundsException("index: " + index);
    }
    return (H) getHandler(base, index, isFlattened(base));
  }

  public final int getHandlerCount(GwtEvent.Type<?> eventKey) {
    return getCount(eventKey.hashCode());
  }

  public final <H> void removeHandler(GwtEvent.Type<H> eventKey,
      EventHandler handler) {
    int base = eventKey.hashCode();

    // Removing a handler is unusual, so smaller code is preferable then
    // handling both flat and dangling list of pointers.
    if (isFlattened(base)) {
      unflatten(base);
    }
    boolean result = removeHelper(base, handler);
    // Hiding this behind an assertion as we'd rather not force the compiler to
    // have to include all handler.toString() instances.
    assert result : handler + " did not exist";
  }

  final native int getCount(int index) /*-{
    var count = this[index];
    return count == null? 0:count;
  }-*/;

  final native void unflatten(int base) /*-{
    var handlerList = {};
    var count = this[base];
    var start = base + 2;
     for(var i = 0; i < count;i++){
       handlerList[i] = this[start + i];
       this[start + i] = null;
      }
     this[base + 1] = handlerList;
  }-*/;

  private native EventHandler getFlatHandler(int base, int index) /*-{
    return this[base + 2 + index];
  }-*/;

  private native EventHandler getHandler(int base, int index, boolean flattened) /*-{
    return flattened? this[base + 2 + index]: this[base + 1][index];
  }-*/;

  private native EventHandler getHandler(JavaScriptObject handlers, int index) /*-{
    return handlers[index];
  }-*/;

  private native JavaScriptObject getHandlers(int base) /*-{
    return  this[base + 1];
  }-*/;

  private native boolean isFlattened(int base) /*-{
    return this[base + 1] == null;
  }-*/;

  private native boolean removeHelper(int base, EventHandler handler) /*-{
    // Find the handler.
    var count = this[base];
    var handlerList = this[base + 1];
    var handlerIndex = -1;
    for(var index = 0;  index < count; index++){
      if(handlerList[index] == handler){
        handlerIndex = index;
        break;
      }
    }
    if(handlerIndex == -1) {
      return false;
    }

    // Remove the handler.
    var last = count -1;
    for(; handlerIndex < last; handlerIndex++){
      handlerList[handlerIndex] = handlerList[handlerIndex+1]
    }
    handlerList[last] = null;
    this[base] = this[base]-1;
    return true;
  }-*/;

  private native void setCount(int index, int count) /*-{
    this[index] = count;
  }-*/;

  private native void setFlatHandler(int base, int index, EventHandler handler) /*-{
    this[base + 2 + index] = handler;
  }-*/;

  private native void setHandler(int base, int index, EventHandler handler) /*-{
    this[base + 1][index] = handler;
  }-*/;

  private native void setHandlerList(int base, JavaScriptObject handlerList) /*-{
    this[base + 1] = handlerList;
  }-*/;
}
