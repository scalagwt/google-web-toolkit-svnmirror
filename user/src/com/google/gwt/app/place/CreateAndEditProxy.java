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
package com.google.gwt.app.place;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.requestfactory.shared.EntityProxy;
import com.google.gwt.requestfactory.shared.RequestContext;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

/**
 * Extends {@link AbstractProxyEditActivity} to first create an instance to edit
 * 
 * @param <P> the type of proxy to create and edit
 */
public abstract class CreateAndEditProxy<P extends EntityProxy> extends AbstractProxyEditActivity<P> {

  private AcceptsOneWidget display;
  private final P proxy;

  public CreateAndEditProxy(Class<P> proxyClass, RequestContext request,
      ProxyEditView<P, ?> view, PlaceController placeController) {
    super(view, placeController);
    this.proxy = request.create(proxyClass);
  }

  @Override
  public void start(AcceptsOneWidget display, EventBus eventBus) {
    this.display = display;
    super.start(display, eventBus);
  }

  /**
   * Called when the user cancels or has successfully saved. Refines the default
   * implementation to clear the display given at {@link #start} on cancel.
   * 
   * @param saved true if changes were comitted, false if user canceled
   */
  @Override
  protected void exit(boolean saved) {
    if (!saved) {
      display.setWidget(null);
    }

    super.exit(saved);
  }

  @Override
  protected P getProxy() {
    return proxy;
  }  
}
