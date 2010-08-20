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

import com.google.gwt.valuestore.shared.SimpleFooRecord;

import junit.framework.TestCase;

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
    RequestDefinition request = registry.getOperation("com.google.gwt.valuestore.shared.SimpleFooRequest::findAll");
    assert request != null;
    assertEquals("com.google.gwt.valuestore.server.SimpleFoo",
        request.getDomainClassName());
    assertEquals("findAll", request.getDomainMethodName());
    assertEquals(SimpleFooRecord.class, request.getReturnType());
    assertEquals(0, request.getParameterTypes().length);
    assertEquals(true, request.isReturnTypeList());
  }

  public void testGetOperationScalarNoArgs() {
    RequestDefinition request = registry.getOperation("com.google.gwt.valuestore.shared.SimpleFooRequest::countSimpleFoo");
    assert request != null;
    assertEquals("com.google.gwt.valuestore.server.SimpleFoo",
        request.getDomainClassName());
    assertEquals("countSimpleFoo", request.getDomainMethodName());
    assertEquals(Long.class, request.getReturnType());
    assertEquals(0, request.getParameterTypes().length);
    assertEquals(false, request.isReturnTypeList());
  }

  public void testGetOpertionScalarWithArgs() {
    RequestDefinition request = registry.getOperation("com.google.gwt.valuestore.shared.SimpleFooRequest::findSimpleFooById");
    assert request != null;
    assertEquals("com.google.gwt.valuestore.server.SimpleFoo",
        request.getDomainClassName());
    assertEquals("findSimpleFooById", request.getDomainMethodName());
    assertEquals(SimpleFooRecord.class, request.getReturnType());
    assert request.getParameterTypes().length == 1
        && request.getParameterTypes()[0] == Long.class;
    assertEquals(false, request.isReturnTypeList());
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
    RequestDefinition request = registry.getOperation("com.google.gwt.valuestore.shared.SimpleFooRequest::privateMethod");
    assert request == null;
  }
}
