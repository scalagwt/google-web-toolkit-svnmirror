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
package com.google.gwt.requestfactory.shared;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * An event posted whenever an RPC request is sent or its response is received.
 */
public class RequestEvent extends GwtEvent<RequestEvent.Handler> {
  /**
   * Implemented by handlers of this type of event.
   */
  public interface Handler extends EventHandler {
    void onRequestEvent(RequestEvent requestEvent);
  }
  
  public enum State {
    SENT, RECEIVED
  }
  
  private final State state;

  public static final Type<Handler> TYPE = new Type<Handler>();

  public RequestEvent(State state) {
    this.state = state;
  }
  
  @Override
  public GwtEvent.Type<Handler> getAssociatedType() {
    return TYPE;
  }

  public State getState() {
    return state;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onRequestEvent(this);
  }
}
