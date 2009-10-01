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
package com.google.gwt.resources.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.junit.client.GWTTestCase;

/**
 * Tests for TextResource assembly and use.
 */
public class TextResourceTest extends GWTTestCase {

  static interface Resources extends ClientBundleWithLookup {
    @Source("com/google/gwt/resources/client/hello.txt")
    TextResource helloWorldAbsolute();

    @Source("hello.txt")
    ExternalTextResource helloWorldExternal();

    @Source("com/google/gwt/resources/server/outside_resource_oracle.txt")
    TextResource helloWorldOutsideResourceOracle();

    @Source("hello.txt")
    TextResource helloWorldRelative();
  }

  private static final String HELLO = "Hello World!";

  @Override
  public String getModuleName() {
    return "com.google.gwt.resources.Resources";
  }

  public void testExternal() throws ResourceException {
    final Resources r = GWT.create(Resources.class);

    delayTestFinish(2000);

    ResourceCallback<TextResource> c = new ResourceCallback<TextResource>() {

      public void onError(ResourceException e) {
        e.printStackTrace();
        fail("Unable to fetch " + e.getResource().getName());
      }

      public void onSuccess(TextResource resource) {
        assertEquals(r.helloWorldExternal().getName(), resource.getName());
        assertEquals(HELLO, resource.getText());
        finishTest();
      }
    };

    r.helloWorldExternal().getText(c);
  }

  public void testInline() {
    Resources r = GWT.create(Resources.class);
    assertEquals(HELLO, r.helloWorldRelative().getText());
    assertEquals(HELLO, r.helloWorldAbsolute().getText());
  }

  public void testMeta() {
    Resources r = GWT.create(Resources.class);
    assertEquals("helloWorldAbsolute", r.helloWorldAbsolute().getName());
    assertEquals("helloWorldRelative", r.helloWorldRelative().getName());
    assertEquals("helloWorldExternal", r.helloWorldExternal().getName());

    ResourcePrototype[] resources = r.getResources();
    assertEquals(4, resources.length);
  }

  public void testOutsideResourceOracle() {
    Resources r = GWT.create(Resources.class);
    assertTrue(r.helloWorldOutsideResourceOracle().getText().startsWith(HELLO));
  }
}
