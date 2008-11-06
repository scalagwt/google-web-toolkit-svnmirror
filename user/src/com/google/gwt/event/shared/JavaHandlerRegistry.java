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

import java.util.ArrayList;
import java.util.HashMap;

/**
 * The default Handler manager's handler registry.
 */
class JavaHandlerRegistry extends HashMap<GwtEvent.Type<?>, ArrayList<?>> {

  public <H extends EventHandler> void addHandler(Type<H> type, H handler) {
    ArrayList<H> l = get(type);
    if (l == null) {
      l = new ArrayList<H>();
      super.put(type, l);
    }
    l.add(handler);
  }

  public void clearHandlers(Type<?> type) {
    super.remove(type);
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
    int handlerCount = getHandlerCount(eventKey);
    assert (index < handlerCount);
    ArrayList<H> l = get(eventKey);
    return  l.get(index);
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
      l.remove(handler);
    }
  }

  @SuppressWarnings("unchecked")
  private <H> ArrayList<H> get(GwtEvent.Type<H> type) {
    // This cast is safe because we control the puts.
    return (ArrayList<H>) super.get(type);
  }
}
