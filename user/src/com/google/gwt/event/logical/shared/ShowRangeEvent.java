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
package com.google.gwt.event.logical.shared;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Fired after an event source shows a range of values.
 * 
 * @param <V> the type of value shown in the range
 */
public class ShowRangeEvent<V> extends GwtEvent<ShowRangeHandler<V>> {

  /**
   * Event type for {@link ShowRangeEvent}.
   */
  public static final Type<ShowRangeHandler<?>> TYPE = new Type<ShowRangeHandler<?>>();

  private V start;
  private V end;

  /**
   * Constructs a ShowRangeEvent event.
   * 
   * @param start start of range
   * @param end end of range
   */
  public ShowRangeEvent(V start, V end) {
    this.start = start;
    this.end = end;
  }

  /**
   * Gets the end of the range.
   * 
   * @return range end
   */
  public V getEnd() {
    assertLive();
    return end;
  }

  /**
   * Gets the start of the range.
   * 
   * @return range start
   */
  public V getStart() {
    assertLive();
    return start;
  }

  @Override
  protected void dispatch(ShowRangeHandler<V> handler) {
    handler.onShowRange(this);
  }

  @Override
  protected final Type getAssociatedType() {
    return TYPE;
  }
}
