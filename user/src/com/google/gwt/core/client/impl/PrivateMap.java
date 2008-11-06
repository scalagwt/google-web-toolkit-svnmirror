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

package com.google.gwt.core.client.impl;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;

import java.util.HashMap;

/**
 * A raw js map implementation. public so we can avoid creating multiple
 * versions for our internal code, the API is completely unsafe with no fewer
 * then three versions of put and get, so do not use!
 * 
 * @param <V> value type
 */
public class PrivateMap<V> {

  private static class JsMap<V> extends JavaScriptObject {

    public static PrivateMap.JsMap<?> create() {
      return JavaScriptObject.createObject().cast();
    }

    protected JsMap() {
    }

    public final native void put(int key, V value) /*-{
      this[key] = value;
    }-*/;

    public final native void put(String key, V value) /*-{
      this[key] = value;
    }-*/;

    public final native V unsafeGet(int key) /*-{
      return this[key];
    }-*/;

    public final native V unsafeGet(String key) /*-{
      return this[key];
    }-*/;
  }

  private PrivateMap.JsMap<V> map;
  private HashMap<String, V> javaMap;

  public PrivateMap() {
    if (GWT.isScript()) {
      map = JsMap.create().cast();
    } else {
      javaMap = new HashMap<String, V>();
    }
  }

  // Raw put, only use with int get.
  public final void put(int key, V value) {
    if (GWT.isScript()) {
      map.put(key, value);
    } else {
      javaMap.put(key + "", value);
    }
  }

  // ONLY use this for values put with safePut.
  public final V safeGet(String key) {
    return unsafeGet(":" + key);
  }

  // ONLY use this for values that will be accessed with saveGet.
  public final void safePut(String key, V value) {
    unsafePut(":" + key, value);
  }

  // int unsafeGet only use with int get.
  public final V unsafeGet(int key) {
    if (GWT.isScript()) {
      return map.unsafeGet(key);
    } else {
      return javaMap.get(key + "");
    }
  }

  // Raw get, only use for values that are known not to conflict with the
  // browser's reserved keywords.
  public final V unsafeGet(String key) {
    if (GWT.isScript()) {
      return map.unsafeGet(key);
    } else {
      return javaMap.get(key);
    }
  }

  // Raw put, only use for values that are known not to conflict with the
  // browser's reserved keywords.
  public final void unsafePut(String key, V value) {
    if (GWT.isScript()) {
      map.put(key, value);
    } else {
      javaMap.put(key, value);
    }
  }
}