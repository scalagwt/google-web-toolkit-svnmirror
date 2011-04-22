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
package com.google.gwt.requestfactory.shared.impl;

import com.google.gwt.requestfactory.shared.EntityProxy;
import com.google.gwt.requestfactory.shared.EntityProxyId;

/**
 * Extends SimpleProxyId with the correct parameterization to implement
 * EntityProxyId.
 *
 * <p><span style='color:red'>RequestFactory has moved to
 * <code>com.google.web.bindery.requestfactory</code>.  This package will be
 * removed in a future version of GWT.</span></p>
 *
 * @param <P> the type of EntityProxy object the id describes
 */
@Deprecated
public class SimpleEntityProxyId<P extends EntityProxy> extends
    SimpleProxyId<P> implements EntityProxyId<P> {

  /**
   * Construct an ephemeral id. May be called only from
   * {@link IdFactory#getId()}.
   */
  SimpleEntityProxyId(Class<P> proxyClass, int clientId) {
    super(proxyClass, clientId);
  }

  /**
   * Construct a stable id. May only be called from {@link IdFactory#getId()}
   */
  SimpleEntityProxyId(Class<P> proxyClass, String encodedAddress) {
    super(proxyClass, encodedAddress);
  }
}
