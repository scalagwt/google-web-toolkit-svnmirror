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
 * Tests MutableSet when used without providing a {@link Relation} with
 * {@link MutableSet#setAdapter(Relation)}.
 */
public class MutableSetTest extends GWTTestCase {
  
  private boolean assertionsEnabled;

  public void gwtSetUp() {
    assertionsEnabled = this.getClass().desiredAssertionStatus();
  }

  public void testAdd() {
    MutableSet<String> ms = CollectionFactory.createMutableSet();

    ms.add("peach");
    assertTrue(ms.contains("peach"));
    
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
  }

  public void testAddAll() {
    MutableSet<String> msA = CollectionFactory.createMutableSet();
    MutableSet<String> msB = CollectionFactory.createMutableSet();

    msA.add("peach");
    msA.add("apricot");
    msA.add("prune");

    msB.addAll(msA);

    assertTrue(msB.isEqual(msA));
    try {
      msA.addAll(null);
      fail();
    } catch (NullPointerException e) {
      // Expected behavior
    }
    assertTrue(msB.isEqual(msA));
  }

  public void testContains() {
    MutableSet<String> ms = CollectionFactory.createMutableSet();
    ms.add("peach");

    assertTrue(ms.contains("peach"));
    assertFalse(ms.contains("lemon"));
    
    assertFalse(ms.contains(null));
  }

  public void testContainsAll() {
    MutableSet<String> msA = CollectionFactory.createMutableSet();
    MutableSet<String> msB = CollectionFactory.createMutableSet();
    MutableSet<String> msEmpty = CollectionFactory.createMutableSet();

    msA.add("peach");
    msA.add("apricot");
    msA.add("prune");

    msB.add("peach");
    msB.add("prune");

    assertTrue(msA.containsAll(msB));
    assertFalse(msB.containsAll(msA));
    assertTrue(msA.containsAll(msEmpty));
    try {
      msA.containsAll(null);
      fail();
    } catch (NullPointerException e) {
      // Expected behavior
    }
  }

  public void testContainsSome() {
    MutableSet<String> msA = CollectionFactory.createMutableSet();
    MutableSet<String> msB = CollectionFactory.createMutableSet();
    MutableSet<String> msEmpty = CollectionFactory.createMutableSet();

    msA.add("peach");
    msA.add("apricot");
    msA.add("prune");

    msB.add("lemon");
    msB.add("orange");
    msB.add("peach");

    assertTrue(msA.containsSome(msB));
    msB.remove("peach");
    assertFalse(msB.containsSome(msA));
    assertFalse(msA.containsSome(msEmpty));
    try {
      msA.containsSome(null);
      fail();
    } catch (NullPointerException e) {
      // Expected behavior
    }
  }

  public void testIsEmpty() {
    MutableSet<String> ms = CollectionFactory.createMutableSet();

    assertTrue(ms.isEmpty());
    ms.add("peach");
    assertFalse(ms.isEmpty());
  }

  public void testIsEqual() {
    MutableSet<String> msA = CollectionFactory.createMutableSet();
    MutableSet<String> msB = CollectionFactory.createMutableSet();

    assertTrue(msA.isEqual(msB));
    msA.add("peach");
    assertFalse(msA.isEqual(msB));
    msB.add("peach");
    assertTrue(msA.isEqual(msB));
    msB.add("apricot");
    assertFalse(msA.isEqual(msB));
    assertFalse(msA.isEqual(null));
  }

  public void testKeepAll() {
    MutableSet<String> msA = CollectionFactory.createMutableSet();
    MutableSet<String> msB = CollectionFactory.createMutableSet();
    MutableSet<String> msC = CollectionFactory.createMutableSet();

    msA.add("peach");
    msA.add("apricot");
    msA.add("prune");

    msB.add("peach");
    msB.add("prune");
    msB.add("orange");

    msC.add("peach");
    msC.add("prune");

    msA.keepAll(msB);
    assertTrue(msC.isEqual(msA));
    try {
      msA.keepAll(null);
      fail();
    } catch (NullPointerException e) {
      // Expected behavior
    }
  }

  public void testRemove() {
    MutableSet<String> msA = CollectionFactory.createMutableSet();
    MutableSet<String> msB = CollectionFactory.createMutableSet();

    msA.add("peach");
    msA.add("apricot");
    msA.remove("peach");
    msB.add("apricot");
    assertTrue(msA.isEqual(msB));
    msA.remove("blue_berry");
    assertTrue(msA.isEqual(msB));
    msA.remove(null);
    assertTrue(msA.isEqual(msB));
    msA.remove("apricot");
    assertTrue(msA.isEmpty());
  }

  public void testRemoveAll() {
    MutableSet<String> msA = CollectionFactory.createMutableSet();
    MutableSet<String> msB = CollectionFactory.createMutableSet();
    MutableSet<String> msC = CollectionFactory.createMutableSet();

    msA.add("peach");
    msA.add("apricot");
    msA.add("prune");

    msB.add("peach");
    msB.add("prune");
    msB.add("orange");

    msC.add("apricot");

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
  }

  @Override
  public String getModuleName() {
    return null;
  }

}
