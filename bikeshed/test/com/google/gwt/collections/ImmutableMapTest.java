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
 * Common tests for all classes inheriting from {@link MutableMap}related to
 * freezing and obtaining immutable versions. Override {@link #gwtSetUp} (and
 * call {@code super.gwtSetUp()}) to initialize {@code keyA} and {@code keyB},
 * {@link #getMap()} with the map constructor. Optionally override
 * {@link #testNullKey()} to test special behavior (such as null keys not being
 * allowed) related to null keys as this test case assumes null keys are a valid
 * case.
 * 
 * @param <K> type of keys to test.
 */
public abstract class ImmutableMapTest<K> extends GWTTestCase {

  protected boolean assertionsEnabled;

  protected K keyA;

  protected K keyB;

  protected K keyUnsupported;
  
  @Override
  public String getModuleName() {
    return null;
  }

  @Override
  public void gwtSetUp() {
    assertionsEnabled = this.getClass().desiredAssertionStatus();
  }

  public void testImmutableNonEmpty() {
    MutableMap<K, String> mm = getMap();
    mm.put(keyA, "1");

    ImmutableMap<K, String> im = mm.freeze();

    assertFalse(im.isEmpty());
    assertEquals("1", im.get(keyA));
    assertTrue(im.containsKey(keyA));
    assertNull(im.get(keyB));
    assertFalse(im.containsKey(keyB));
  }

  public void testImmutableEmpty() {
    MutableMap<K, String> mm = getMap();

    ImmutableMap<K, String> im = mm.freeze();

    assertTrue(im.isEmpty());
    assertNull(im.get(keyA));
    assertFalse(im.containsKey(keyA));
  }

  public void testNullKey() {
    MutableMap<K, String> mm = getMap();
    mm.put(null, "2");

    ImmutableMap<K, String> im = mm.freeze();

    assertFalse(im.isEmpty());
    assertEquals("2", im.get(null));
    assertTrue(im.containsKey(null));
    assertNull(im.get(keyB));
    assertFalse(im.containsKey(keyB));
  }

  public void testFreezeMutableMap() {
    // Do not test undefined behavior with assertions disabled
    if (!assertionsEnabled) {
      return;
    }

    MutableMap<K, String> mm = getMap();
    mm.put(keyA, "1");
    mm.freeze();

    try {
      mm.clear();
      fail("Expected an assertion failure");
    } catch (AssertionError e) {
      assertEquals(("This operation is illegal on a frozen collection"),
          e.getMessage());
    }

    try {
      mm.remove(keyA);
      fail("Expected an assertion failure");
    } catch (AssertionError e) {
      assertEquals(("This operation is illegal on a frozen collection"),
          e.getMessage());
    }

    try {
      mm.put(keyA, "orange");
      fail("Expected an assertion failure");
    } catch (AssertionError e) {
      assertEquals(("This operation is illegal on a frozen collection"),
          e.getMessage());
    }
  }

  public void testUnsupportedValue() {
    MutableMap<K, String> mm = getMap();
    
    ImmutableMap<K, String> im = mm.freeze();
    
    assertNull(im.get(keyUnsupported));
  }
  
  protected abstract MutableMap<K, String> getMap();

}
