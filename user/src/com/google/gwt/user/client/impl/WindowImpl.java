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
package com.google.gwt.user.client.impl;

/**
 * Native implementation associated with
 * {@link com.google.gwt.user.client.Window}.
 */
public class WindowImpl {
  public native void enableScrolling(boolean enable) /*-{
   @com.google.gwt.user.client.impl.DocumentRootImpl::documentRoot.style.overflow =
       enable ? "" : "hidden";
  }-*/;

  public native int getClientHeight() /*-{
   return @com.google.gwt.user.client.impl.DocumentRootImpl::documentRoot.clientHeight;
  }-*/;

  public native int getClientWidth() /*-{
   return @com.google.gwt.user.client.impl.DocumentRootImpl::documentRoot.clientWidth;
  }-*/;

  public native String getHash() /*-{
    return $wnd.location.hash;
  }-*/;

  public native String getQueryString() /*-{
    return $wnd.location.search;
  }-*/;
  
  public native int getScrollLeft() /*-{
   return @com.google.gwt.user.client.impl.DocumentRootImpl::documentRoot.scrollLeft;
  }-*/;

  public native int getScrollTop() /*-{
   return @com.google.gwt.user.client.impl.DocumentRootImpl::documentRoot.scrollTop;
  }-*/;

  public native void initWindowCloseHandler() /*-{
    var oldOnBeforeUnload = $wnd.onbeforeunload;
    var oldOnUnload = $wnd.onunload;
    
    $wnd.onbeforeunload = function(evt) {
      var ret, oldRet;
      try {
        ret = @com.google.gwt.user.client.Window::onClosing()();
      } finally {
        oldRet = oldOnBeforeUnload && oldOnBeforeUnload(evt);
      }
      // Avoid returning null as IE6 will coerce it into a string.
      // Ensure that "" gets returned properly.
      if (ret != null) {
        return ret;
      }
      if (oldRet != null) {
        return oldRet;
      }
      // returns undefined.
    };
    
    $wnd.onunload = function(evt) {
      try {
        @com.google.gwt.user.client.Window::onClosed()();
      } finally {
        oldOnUnload && oldOnUnload(evt);
        $wnd.onresize = null;
        $wnd.onscroll = null;
        $wnd.onbeforeunload = null;
        $wnd.onunload = null;
      }
    };
  }-*/;

  public native void initWindowResizeHandler() /*-{
    var oldOnResize = $wnd.onresize;
    $wnd.onresize = function(evt) {
      try {
        @com.google.gwt.user.client.Window::onResize()();
      } finally {
        oldOnResize && oldOnResize(evt);
      }
    };
  }-*/;

  public native void initWindowScrollHandler() /*-{
    var oldOnScroll = $wnd.onscroll;
    $wnd.onscroll = function(evt) {
      try {
        @com.google.gwt.user.client.Window::onScroll()();
      } finally {
        oldOnScroll && oldOnScroll(evt);
      }
    };
  }-*/;
}
