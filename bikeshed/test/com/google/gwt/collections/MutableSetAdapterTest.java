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

import com.google.gwt.junit.client.GWTTestCase;

/**
 * Tests MutableSet when used providing a {@link Relation} with
 * custom {@link MutableSet#setAdapter(Relation)} adapters.
 */
public class MutableSetAdapterTest extends GWTTestCase {
  
  private boolean assertionsEnabled;
  
  private Relation<Object, String> adapter1 = new Relation<Object, String>() {
    public String applyTo(Object element) {
      if (!(element instanceof Integer) || (Integer) element >= 0) {
        return null;
      }
      return "__" + ((Integer) element).toString();
    }
  };

  private Relation<Object, String> adapter2 = new Relation<Object, String>() {
    public String applyTo(Object element) {
      if (!(element instanceof Integer) || (Integer) element >= 0) {
        return null;
      }
      return "_$_" + ((Integer) element).toString();
    }
  };
  
  public void gwtSetUp() {
    assertionsEnabled = this.getClass().desiredAssertionStatus();
  }

  public void testAdd() {
    MutableSet<Integer> ms = CollectionFactory.createMutableSet(adapter1);

    ms.add(-1);
    assertTrue(ms.contains(-1));
    
    // Do not test undefined behavior without assertions
    if (!assertionsEnabled) {
      return;
    }
    try {
      ms.add(null);
      fail("Should have triggered an assertion");
    } catch (AssertionError e) {
      // Good
      assertEquals(Assertions.ACCESS_UNSUPPORTED_VALUE, e.getMessage());
    }
    try {
      ms.add(1);
      fail("Should have triggered an assertion");
    } catch (AssertionError e) {
      // Good
      assertEquals(Assertions.ACCESS_UNSUPPORTED_VALUE, e.getMessage());
    }
  }

  public void testAddAll() {
    MutableSet<Integer> msA = CollectionFactory.createMutableSet(adapter1);
    MutableSet<Integer> msB = CollectionFactory.createMutableSet(adapter1);

    msA.add(-1);
    msA.add(-2);
    msA.add(-3);

    msB.addAll(msA);

    assertTrue(msB.isEqual(msA));
    try {
      msA.addAll(null);
      fail();
    } catch (NullPointerException e) {
      // Expected behavior
    }
    
    // Test different adapters
    MutableSet<Integer> msC = CollectionFactory.createMutableSet(adapter2);
    msC.addAll(msA);

    assertTrue(msC.isEqual(msA));
  }

  public void testContains() {
    MutableSet<Integer> ms = CollectionFactory.createMutableSet(adapter1);
    ms.add(-1);

    assertTrue(ms.contains(-1));
    assertFalse(ms.contains(-2));
    
    assertFalse(ms.contains(null));
  }

  public void testContainsAll() {
    MutableSet<Integer> msA = CollectionFactory.createMutableSet(adapter1);
    MutableSet<Integer> msB = CollectionFactory.createMutableSet(adapter1);
    MutableSet<Integer> msEmpty = CollectionFactory.createMutableSet(adapter1);

    msA.add(-1);
    msA.add(-2);
    msA.add(-3);

    msB.add(-1);
    msB.add(-2);

    assertTrue(msA.containsAll(msB));
    assertFalse(msB.containsAll(msA));
    assertTrue(msA.containsAll(msEmpty));
    try {
      msA.containsAll(null);
      fail();
    } catch (NullPointerException e) {
      // Expected behavior
    }
    
    // Test different adapters
    MutableSet<Integer> msC = CollectionFactory.createMutableSet(adapter2);

    msC.add(-1);
    msC.add(-2);
    
    assertTrue(msA.containsAll(msC));
    assertFalse(msC.containsAll(msA));
  }

  public void testContainsSome() {
    MutableSet<Integer> msA = CollectionFactory.createMutableSet(adapter1);
    MutableSet<Integer> msB = CollectionFactory.createMutableSet(adapter1);
    MutableSet<Integer> msEmpty = CollectionFactory.createMutableSet(adapter1);

    msA.add(-1);
    msA.add(-2);
    msA.add(-3);

    msB.add(-4);
    msB.add(-5);
    msB.add(-1);

    assertTrue(msA.containsSome(msB));
    msB.remove(-1);
    assertFalse(msB.containsSome(msA));
    assertFalse(msA.containsSome(msEmpty));
    try {
      msA.containsSome(null);
      fail();
    } catch (NullPointerException e) {
      // Expected behavior
    }
    
    // Test different adapters
    MutableSet<Integer> msC = CollectionFactory.createMutableSet(adapter2);
    msC.add(-4);
    msC.add(-5);
    msC.add(-1);

    assertTrue(msA.containsSome(msC));
    msC.remove(-1);
    assertFalse(msC.containsSome(msA));
  }

  public void testIsEmpty() {
    MutableSet<Integer> ms = CollectionFactory.createMutableSet(adapter1);

    assertTrue(ms.isEmpty());
    ms.add(-1);
    assertFalse(ms.isEmpty());
  }

  public void testIsEqual() {
    MutableSet<Integer> msA = CollectionFactory.createMutableSet(adapter1);
    MutableSet<Integer> msB = CollectionFactory.createMutableSet(adapter1);

    assertTrue(msA.isEqual(msB));
    msA.add(-1);
    assertFalse(msA.isEqual(msB));
    msB.add(-1);
    assertTrue(msA.isEqual(msB));
    msB.add(-2);
    assertFalse(msA.isEqual(msB));
    assertFalse(msA.isEqual(null));
    
    // Test different adapters
    MutableSet<Integer> msC = CollectionFactory.createMutableSet(adapter1);
    MutableSet<Integer> msD = CollectionFactory.createMutableSet(adapter2);

    assertTrue(msC.isEqual(msD));
    msC.add(-1);
    assertFalse(msC.isEqual(msD));
    msD.add(-1);
    assertTrue(msC.isEqual(msD));
    msD.add(-2);
    assertFalse(msC.isEqual(msD));
  }

  public void testKeepAll() {
    MutableSet<Integer> msA = CollectionFactory.createMutableSet(adapter1);
    MutableSet<Integer> msB = CollectionFactory.createMutableSet(adapter1);
    MutableSet<Integer> msC = CollectionFactory.createMutableSet(adapter1);

    msA.add(-1);
    msA.add(-2);
    msA.add(-3);

    msB.add(-1);
    msB.add(-2);
    msB.add(-4);

    msC.add(-1);
    msC.add(-2);

    msA.keepAll(msB);
    assertTrue(msC.isEqual(msA));
    try {
      msA.keepAll(null);
      fail();
    } catch (NullPointerException e) {
      // Expected behavior
    }
    
    // Test different adapters
    MutableSet<Integer> msD = CollectionFactory.createMutableSet(adapter2);

    msD.add(-1);
    msD.add(-2);
    msD.add(-3);

    msD.keepAll(msB);
    
    assertTrue(msC.isEqual(msD));
  }

  public void testRemove() {
    MutableSet<Integer> msA = CollectionFactory.createMutableSet(adapter1);
    MutableSet<Integer> msB = CollectionFactory.createMutableSet(adapter1);

    msA.add(-1);
    msA.add(-2);
    msA.remove(-1);
    msB.add(-2);
    assertTrue(msA.isEqual(msB));
    msA.remove(-3);
    assertTrue(msA.isEqual(msB));
    msA.remove(null);
    assertTrue(msA.isEqual(msB));
    msA.remove(-2);
    assertTrue(msA.isEmpty());
  }

  public void testRemoveAll() {
    MutableSet<Integer> msA = CollectionFactory.createMutableSet(adapter1);
    MutableSet<Integer> msB = CollectionFactory.createMutableSet(adapter1);
    MutableSet<Integer> msC = CollectionFactory.createMutableSet(adapter1);

    msA.add(-1);
    msA.add(-2);
    msA.add(-3);

    msB.add(-1);
    msB.add(-3);
    msB.add(-4);

    msC.add(-2);

    msA.removeAll(msB);
    assertTrue(msC.isEqual(msA));
    msA.removeAll(msC);
    assertTrue(msA.isEmpty());
    try {
      msA.removeAll(null);
      fail();
    } catch (NullPointerException e) {
      // Expected behavior
    }
    
    // Test different adapters
    MutableSet<Integer> msD = CollectionFactory.createMutableSet(adapter2);

    msD.add(-1);
    msD.add(-2);
    msD.add(-3);
    
    msD.removeAll(msB);
    assertTrue(msC.isEqual(msD));
    msD.removeAll(msC);
    assertTrue(msD.isEmpty());
}

  @Override
  public String getModuleName() {
    return null;
  }

}
