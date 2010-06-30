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
 * 
 * @param <E> type of elements to test
 */
public abstract class MutableSetTest<E> extends GWTTestCase {
  
  protected E element1;
  
  protected E element2;

  protected E element3;

  protected E element4;

  protected E element5;
  
  protected E elementUnsupported;

  protected boolean assertionsEnabled;
  
  public void gwtSetUp() {
    assertionsEnabled = this.getClass().desiredAssertionStatus();
  }

  public void testAdd() {
    MutableSet<E> ms = getSet();

    ms.add(element1);
    assertTrue(ms.contains(element1));
    
    // Do not test undefined behavior without assertions
    if (!assertionsEnabled) {
      return;
    }
    try {
      ms.add(elementUnsupported);
      fail("Should have triggered an assertion");
    } catch (AssertionError e) {
      // Good
      assertEquals(Assertions.ACCESS_UNSUPPORTED_VALUE, e.getMessage());
    }
  }

  public void testAddAll() {
    MutableSet<E> msA = getSet();
    MutableSet<E> msB = getSet();

    msA.add(element1);
    msA.add(element2);
    msA.add(element3);

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
    MutableSet<E> ms = getSet();
    ms.add(element1);

    assertTrue(ms.contains(element1));
    assertFalse(ms.contains(element4));
    
    assertFalse(ms.contains(elementUnsupported));
  }

  public void testContainsAll() {
    MutableSet<E> msA = getSet();
    MutableSet<E> msB = getSet();
    MutableSet<E> msEmpty = getSet();

    msA.add(element1);
    msA.add(element2);
    msA.add(element3);

    msB.add(element1);
    msB.add(element3);

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
    MutableSet<E> msA = getSet();
    MutableSet<E> msB = getSet();
    MutableSet<E> msEmpty = getSet();

    msA.add(element1);
    msA.add(element2);
    msA.add(element3);

    msB.add(element4);
    msB.add(element5);
    msB.add(element1);

    assertTrue(msA.containsSome(msB));
    msB.remove(element1);
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
    MutableSet<E> ms = getSet();

    assertTrue(ms.isEmpty());
    ms.add(element1);
    assertFalse(ms.isEmpty());
  }

  public void testIsEqual() {
    MutableSet<E> msA = getSet();
    MutableSet<E> msB = getSet();

    assertTrue(msA.isEqual(msB));
    msA.add(element1);
    assertFalse(msA.isEqual(msB));
    msB.add(element1);
    assertTrue(msA.isEqual(msB));
    msB.add(element2);
    assertFalse(msA.isEqual(msB));
    assertFalse(msA.isEqual(null));
  }

  public void testKeepAll() {
    MutableSet<E> msA = getSet();
    MutableSet<E> msB = getSet();
    MutableSet<E> msC = getSet();

    msA.add(element1);
    msA.add(element2);
    msA.add(element3);

    msB.add(element1);
    msB.add(element3);
    msB.add(element5);

    msC.add(element1);
    msC.add(element3);

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
    MutableSet<E> msA = getSet();
    MutableSet<E> msB = getSet();

    msA.add(element1);
    msA.add(element2);
    msA.remove(element1);
    msB.add(element2);
    assertTrue(msA.isEqual(msB));
    msA.remove(element3);
    assertTrue(msA.isEqual(msB));
    msA.remove(elementUnsupported);
    assertTrue(msA.isEqual(msB));
    msA.remove(element2);
    assertTrue(msA.isEmpty());
  }

  public void testRemoveAll() {
    MutableSet<E> msA = getSet();
    MutableSet<E> msB = getSet();
    MutableSet<E> msC = getSet();

    msA.add(element1);
    msA.add(element2);
    msA.add(element3);

    msB.add(element1);
    msB.add(element3);
    msB.add(element5);

    msC.add(element2);

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
  
  public abstract MutableSet<E> getSet();
  
}
