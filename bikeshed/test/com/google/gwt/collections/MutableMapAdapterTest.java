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
package com.google.gwt.collections;

/**
 * Tests {@link MutableMap} behavior.
 */
public class MutableMapAdapterTest extends MutableMapTest<Integer> {
  
  private Relation<Object, String> adapter = new Relation<Object, String>() {
    public String applyTo(Object element) {
      if (element == null) {
        return "_null";
      }      
      if (!(element instanceof Integer) || (Integer) element >= 0) {
        return null;
      }
      return "__" + ((Integer) element).toString();
    }
  };
  
  public void testSetAdapterExceptions() {
    try {
      CollectionFactory.createMutableMap(null);
      fail("Should have triggered NullPointerException");
    } catch (NullPointerException e) {
      // Expected exception
    }
    
    // Do not test undefined behavior without assertions
    if (!assertionsEnabled) {
      return;
    }
    
    MutableMap<Integer, Integer> mm = getMap();
    
    try {
      mm.setAdapter(adapter);
      fail("Should have triggered Assertion");
    } catch (AssertionError e) {
      // Good
      assertEquals(Assertions.INIT_ADAPTER_TWICE, e.getMessage());
    }
  }
  
  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
    keyA = -1;
    keyB = null;
    keyUnsupported = 1;
  }

  @Override
  protected MutableMap<Integer, Integer> getMap() {
    return CollectionFactory.createMutableMap(adapter);
  }
  
}
