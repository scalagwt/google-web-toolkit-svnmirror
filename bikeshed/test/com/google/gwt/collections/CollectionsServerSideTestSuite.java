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
package com.google.gwt.collections;


import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * This suite collects all server-side Lightweight collections tests.
 */
public class CollectionsServerSideTestSuite extends TestSuite {
  public static Test suite() {
    TestSuite suite = 
      new TestSuite("All Gwt Lightweight collections unit tests");

    suite.addTestSuite(MutableArrayTest.class);
    suite.addTestSuite(MutableArrayInternalTest.class);
    suite.addTestSuite(ImmutableArrayTest.class);
    suite.addTestSuite(ImmutableArrayInternalTest.class);
    suite.addTestSuite(MutableSetStringTest.class);
    suite.addTestSuite(MutableSetCustomAdapterTest.class);
    suite.addTestSuite(ImmutableSetStringTest.class);
    suite.addTestSuite(ImmutableSetAdapterTest.class);
    suite.addTestSuite(MutableStringMapTest.class);
    suite.addTestSuite(ImmutableStringMapTest.class);
    suite.addTestSuite(MutableStringMapInternalTest.class);
    suite.addTestSuite(MutableMapAdapterTest.class);

    return suite;
  }
}
