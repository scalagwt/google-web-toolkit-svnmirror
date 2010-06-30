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
 * Tests MutableSet when used providing a {@link Relation} with
 * custom {@link MutableSet#setAdapter(Relation)} adapters.
 * 
 * @param <E> type of elements to test
 */
public class MutableSetAdapterTest<E> extends MutableSetTest<E> {
  
  protected Relation<Object, String> adapter1;

  protected Relation<Object, String> adapter2;
  
  public void gwtSetUp() {
    assertionsEnabled = this.getClass().desiredAssertionStatus();
  }

  public void testAddAllCrossAdapter() {
    MutableSet<E> msA = getSet();

    msA.add(element1);
    msA.add(element2);
    msA.add(element3);

    MutableSet<E> msC = getSetAdapter2();
    msC.addAll(msA);

    assertTrue(msC.isEqual(msA));
  }

  public void testContainsAllCrossAdapter() {
    MutableSet<E> msA = getSet();

    msA.add(element1);
    msA.add(element2);
    msA.add(element3);

    MutableSet<E> msC = getSetAdapter2();

    msC.add(element1);
    msC.add(element2);
    
    assertTrue(msA.containsAll(msC));
    assertFalse(msC.containsAll(msA));
  }

  public void testContainsSomeCrossAdapter() {
    MutableSet<E> msA = getSet();

    msA.add(element1);
    msA.add(element2);
    msA.add(element3);

    MutableSet<E> msC = getSetAdapter2();
    msC.add(element4);
    msC.add(element5);
    msC.add(element1);

    assertTrue(msA.containsSome(msC));
    msC.remove(element1);
    assertFalse(msC.containsSome(msA));
  }

  public void testIsEqualCrossAdapter() {
    MutableSet<E> msC = getSet();
    MutableSet<E> msD = getSetAdapter2();

    assertTrue(msC.isEqual(msD));
    msC.add(element1);
    assertFalse(msC.isEqual(msD));
    msD.add(element1);
    assertTrue(msC.isEqual(msD));
    msD.add(element2);
    assertFalse(msC.isEqual(msD));
  }

  public void testKeepAllCrossAdapter() {
    MutableSet<E> msB = getSet();
    MutableSet<E> msC = getSet();

    msB.add(element1);
    msB.add(element2);
    msB.add(element4);

    msC.add(element1);
    msC.add(element2);

    MutableSet<E> msD = getSetAdapter2();

    msD.add(element1);
    msD.add(element2);
    msD.add(element3);

    msD.keepAll(msB);
    
    assertTrue(msC.isEqual(msD));
  }

  public void testRemoveAllCrossAdapter() {
    MutableSet<E> msB = getSet();
    MutableSet<E> msC = getSet();

    msB.add(element1);
    msB.add(element3);
    msB.add(element4);

    msC.add(element2);

    MutableSet<E> msD = getSetAdapter2();

    msD.add(element1);
    msD.add(element2);
    msD.add(element3);
    
    msD.removeAll(msB);
    assertTrue(msC.isEqual(msD));
    msD.removeAll(msC);
    assertTrue(msD.isEmpty());
}

  @Override
  public String getModuleName() {
    return null;
  }

  @Override
  public MutableSet<E> getSet() {
    return CollectionFactory.createMutableSet(adapter1);
  }
  
  public MutableSet<E> getSetAdapter2() {
    return CollectionFactory.createMutableSet(adapter2);
  }

}
