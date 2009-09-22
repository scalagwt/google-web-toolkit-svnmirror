/*
 * Copyright 2009 Google Inc.
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
package com.google.gwt.uibinder.client;

import com.google.gwt.dom.client.StyleInjector;
import com.google.gwt.resources.client.CssResource;

import java.util.HashSet;
import java.util.Set;

/**
 * Extended by code generated by calls to GWT.create(Class<? extends UiBinder>).
 *
 * @param <U> The type of the root object of the generated UI, typically a
 *          subclass of {@link com.google.gwt.dom.client.Element} or
 *          {@link com.google.gwt.user.client.ui.UIObject}
 * @param <O> The type of the object that will own the generated UI
 */
public abstract class AbstractUiBinder<U, O> implements UiBinder<U, O> {
  private static final Set<Class<? extends CssResource>> injected =
    new HashSet<Class<? extends CssResource>>();

  /**
   * Invokes {@link StyleInjector#injectStylesheet} on the given css's
   * {@link CssResource#getText()}. Injection is performed only once for each
   * CssResource subclass received (that is, we key on
   * <code>css.getClass()</code>);
   *
   * @param css the resource to inject
   */
  protected static void ensureCssInjected(CssResource css) {
    if (!injected.contains(css.getClass())) {
      StyleInjector.injectStylesheet(css.getText());
      injected.add(css.getClass());
    }
  }
}
