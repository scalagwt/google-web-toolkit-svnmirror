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
package com.google.gwt.requestfactory.server;

import com.google.gwt.requestfactory.shared.SimpleBarProxy;
import com.google.gwt.requestfactory.shared.SimpleFooProxy;

import junit.framework.TestCase;

import java.util.List;
import java.util.Set;

/**
 * Tests for
 * {@link com.google.gwt.requestfactory.server.ReflectionBasedOperationRegistry}
 * .
 */
public class ReflectionBasedOperationRegistryTest extends TestCase {

  private ReflectionBasedOperationRegistry registry;

  @Override
  public void setUp() {
    registry = new ReflectionBasedOperationRegistry(
        new DefaultSecurityProvider());
  }

  public void testGetOperationListNoArgs() {
    RequestDefinition request = registry.getOperation("com.google.gwt.requestfactory.shared.SimpleFooRequest::findAll");
    assert request != null;
    assertEquals("com.google.gwt.requestfactory.server.SimpleFoo",
        request.getDomainClassName());
    assertEquals("findAll", request.getDomainMethod().getName());
    assertEquals(SimpleFooProxy.class, request.getReturnType());
    assertEquals(0, request.getParameterTypes().length);
    assertTrue(List.class.isAssignableFrom(request.getDomainMethod().getReturnType()));
  }

  public void testGetOperationScalarNoArgs() {
    RequestDefinition request = registry.getOperation("com.google.gwt.requestfactory.shared.SimpleFooRequest::countSimpleFoo");
    assert request != null;
    assertEquals("com.google.gwt.requestfactory.server.SimpleFoo",
        request.getDomainClassName());
    assertEquals("countSimpleFoo", request.getDomainMethod().getName());
    assertEquals(Long.class, request.getReturnType());
    assertEquals(0, request.getParameterTypes().length);
    assertFalse(List.class.isAssignableFrom(request.getDomainMethod().getReturnType()));
  }

  public void testGetOpertionScalarWithArgs() {
    {
      RequestDefinition request = registry.getOperation("com.google.gwt.requestfactory.shared.SimpleFooRequest::findSimpleFooById");
      assertNotNull(request);
      assertEquals("com.google.gwt.requestfactory.server.SimpleFoo",
          request.getDomainClassName());
      assertEquals("findSimpleFooById", request.getDomainMethod().getName());
      assertEquals(SimpleFooProxy.class, request.getReturnType());
      assertEquals(1, request.getParameterTypes().length);
      assertEquals(Long.class, request.getParameterTypes()[0]);
      assertFalse(List.class.isAssignableFrom(request.getDomainMethod().getReturnType()));
    }
    {
      RequestDefinition request = registry.getOperation("com.google.gwt.requestfactory.shared.SimpleBarRequest::findSimpleBarById");
      assertNotNull(request);
      assertEquals("com.google.gwt.requestfactory.server.SimpleBar",
          request.getDomainClassName());
      assertEquals("findSimpleBarById", request.getDomainMethod().getName());
      assertEquals(SimpleBarProxy.class, request.getReturnType());
      assertEquals(1, request.getParameterTypes().length);
      assertEquals(String.class, request.getParameterTypes()[0]);
      assertFalse(List.class.isAssignableFrom(request.getDomainMethod().getReturnType()));
    }
  }

  public void testGetOperationSetNoArgs() {
    RequestDefinition request = registry.getOperation("com.google.gwt.requestfactory.shared.SimpleBarRequest::findAsSet");
    assert request != null;
    assertEquals("com.google.gwt.requestfactory.server.SimpleBar",
        request.getDomainClassName());
    assertEquals("findAsSet", request.getDomainMethod().getName());
    assertEquals(SimpleBarProxy.class, request.getReturnType());
    assertEquals(0, request.getParameterTypes().length);
    assertTrue(Set.class.isAssignableFrom(request.getDomainMethod().getReturnType()));
  }

  public void testInsecureOperations() {
    try {
      // bogus class
      registry.getOperation("com.foo.Foo::bar");
      fail("Access to non-existent class.");
    } catch (SecurityException se) {
      // expected
    }
    try {
      // no @Service
      registry.getOperation("java.lang.System::currentTimeMillis");
      fail("Access allowed to class without @Service annotation");
    } catch (SecurityException se) {
      // expected
    }
  }

  public void testPrivateMethodFails() {
    RequestDefinition request = registry.getOperation("com.google.gwt.requestfactory.shared.SimpleFooRequest::privateMethod");
    assert request == null;
  }
}
