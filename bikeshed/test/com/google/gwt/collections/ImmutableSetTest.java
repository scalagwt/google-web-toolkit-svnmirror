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
 * Test ImmutableSets. Override {@link ImmutableSetTest#getSet()} and
 * {@link GWTTestCase#gwtSetUp()}. In {@code gwtSetUp()} initialize {@code
 * element1}, {@code element2}, {@code element3}.
 *
 * @param <E> type of elements to test
 */
public abstract class ImmutableSetTest<E> extends GWTTestCase {

  protected boolean assertionsEnabled;
  
  protected E element1;
  
  protected E element2;
  
  protected E element3;
  
  protected E elementUnsupported;

  @Override
  public String getModuleName() {
    return null;
  }

  @Override
  public void gwtSetUp() {
    assertionsEnabled = this.getClass().desiredAssertionStatus();
  }

  public void testImmutableNonEmpty() {
    MutableSet<E> msA = getSet();
    MutableSet<E> msB = getSet();
    msA.add(element1);
    msA.add(element2);

    ImmutableSet<E> is = msA.freeze();

    assertFalse(is.isEmpty());
    assertTrue(is.contains(element1));
    assertTrue(is.contains(element2));
    
    // msB starts empty
    assertTrue(is.containsAll(msB));
    assertFalse(is.containsSome(msB));
    assertFalse(is.isEqual(msB));
    msB.add(element1);
    assertTrue(is.containsAll(msB));
    assertTrue(is.containsSome(msB));
    assertFalse(is.isEqual(msB));
    msB.add(element2);
    assertTrue(is.containsAll(msB));
    assertTrue(is.containsSome(msB));
    assertTrue(is.isEqual(msB));
    msB.add(element3);
    assertFalse(is.containsAll(msB));
    assertTrue(is.containsSome(msB));
    assertFalse(is.isEqual(msB));
  }

  public void testImmutableEmpty() {
    MutableSet<E> msA = getSet();
    MutableSet<E> msB = getSet();

    ImmutableSet<E> is = msA.freeze();

    assertTrue(is.isEmpty());
    
    // msB starts empty
    assertTrue(is.containsAll(msB));
    assertFalse(is.containsSome(msB));
    assertTrue(is.isEqual(msB));
    msB.add(element1);
    assertFalse(is.containsAll(msB));
    assertFalse(is.containsSome(msB));
    assertFalse(is.isEqual(msB));
  }

  public void testNull() {
    MutableSet<E> ms = getSet();
    ms.add(element1);

    ImmutableSet<E> is = ms.freeze();

    assertFalse(is.isEqual(null));
    
    try {
      is.containsSome(null);
      fail("Expected null pointer exception");
    } catch (NullPointerException e) {
      // Good case
    }

    try {
      is.containsAll(null);
      fail("Expected null pointer exception");
    } catch (NullPointerException e) {
      // Good case
    }
  }

  public void testFreezeMutableSet() {
    // Do not test undefined behavior with assertions disabled
    if (!assertionsEnabled) {
      return;
    }

    MutableSet<E> msA = getSet();
    MutableSet<E> msB = getSet();
    msA.add(element1);
    msA.freeze();

    try {
      msA.add(element2);
      fail("Expected an assertion failure");
    } catch (AssertionError e) {
      assertEquals(("This operation is illegal on a frozen collection"),
          e.getMessage());
    }

    try {
      msA.remove(element2);
      fail("Expected an assertion failure");
    } catch (AssertionError e) {
      assertEquals(("This operation is illegal on a frozen collection"),
          e.getMessage());
    }

    try {
      msA.addAll(msB);
      fail("Expected an assertion failure");
    } catch (AssertionError e) {
      assertEquals(("This operation is illegal on a frozen collection"),
          e.getMessage());
    }
    
    try {
      msA.keepAll(msB);
      fail("Expected an assertion failure");
    } catch (AssertionError e) {
      assertEquals(("This operation is illegal on a frozen collection"),
          e.getMessage());
    }

    try {
      msA.removeAll(msB);
      fail("Expected an assertion failure");
    } catch (AssertionError e) {
      assertEquals(("This operation is illegal on a frozen collection"),
          e.getMessage());
    }
  }

  public void testUnsupportedValue() {
    MutableSet<E> ms = getSet();
    
    ImmutableSet<E> im = ms.freeze();
    
    assertFalse(im.contains(elementUnsupported));
  }
  
  protected abstract MutableSet<E> getSet();

}
