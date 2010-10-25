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
package com.google.gwt.requestfactory;

import com.google.gwt.requestfactory.client.impl.SimpleEntityProxyIdTest;
import com.google.gwt.requestfactory.rebind.model.RequestFactoryModelTest;
import com.google.gwt.requestfactory.server.JsonRequestProcessorTest;
import com.google.gwt.requestfactory.server.ReflectionBasedOperationRegistryTest;
import com.google.gwt.requestfactory.server.RequestFactoryInterfaceValidatorTest;
import com.google.gwt.requestfactory.server.RequestPropertyTest;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Suite of RequestFactory tests that require the JRE.
 */
public class RequestFactoryJreSuite {
  public static Test suite() {
    TestSuite suite = new TestSuite(
        "requestfactory package tests that require the JRE");
    suite.addTestSuite(SimpleEntityProxyIdTest.class);
    suite.addTestSuite(JsonRequestProcessorTest.class);
    suite.addTestSuite(ReflectionBasedOperationRegistryTest.class);
    suite.addTestSuite(RequestFactoryInterfaceValidatorTest.class);
    suite.addTestSuite(RequestFactoryModelTest.class);
    suite.addTestSuite(RequestPropertyTest.class);
    return suite;
  }
}
