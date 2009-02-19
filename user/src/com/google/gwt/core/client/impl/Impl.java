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

/**
 * Private implementation class for GWT core. This API is should not be
 * considered public or stable.
 */
public final class Impl {

  private static int sNextHashId = 0;

  /**
   * Gets an identity-based hash code on the passed-in Object by adding an
   * expando. This method should not be used with <code>null</code> or any
   * String. The former will crash and the later will produce unstable results
   * when called repeatedly with a String primitive.
   * <p>
   * The sequence of hashcodes generated by this method are a
   * monotonically-increasing sequence.
   */
  public static native int getHashCode(Object o) /*-{
    return o.$H || (o.$H = @com.google.gwt.core.client.impl.Impl::getNextHashId()());
  }-*/;

  public static native String getHostPageBaseURL() /*-{
    var s = $doc.location.href;

    // Pull off any hash.
    var i = s.indexOf('#');
    if (i != -1)
      s = s.substring(0, i);

    // Pull off any query string.
    i = s.indexOf('?');
    if (i != -1)
      s = s.substring(0, i);

    // Rip off everything after the last slash.
    i = s.lastIndexOf('/');
    if (i != -1)
      s = s.substring(0, i);

    // Ensure a final slash if non-empty.
    return s.length > 0 ? s + "/" : "";
  }-*/;

  public static native String getModuleBaseURL() /*-{
    return $moduleBase;
  }-*/;

  public static native String getModuleName() /*-{
    return $moduleName;
  }-*/;

  /**
   * Called from JSNI. Do not change this implementation without updating:
   * <ul>
   * <li>{@link com.google.gwt.user.client.rpc.impl.SerializerBase}</li>
   * </ul>
   */
  @SuppressWarnings("unused")
  private static int getNextHashId() {
    return ++sNextHashId;
  }
}
