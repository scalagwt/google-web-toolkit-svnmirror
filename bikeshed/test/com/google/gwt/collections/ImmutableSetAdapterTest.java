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
 * Tests a ImmutableSet with String elements.
 */
public class ImmutableSetAdapterTest extends ImmutableSetTest<Integer> {
  
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

  @Override
  protected MutableSet<Integer> getSet() {
    return CollectionFactory.createMutableSet(adapter);
  }
  
  @Override
  public void gwtSetUp() {
    super.gwtSetUp();
    element1 = null;
    element2 = -2;
    element3 = -3;
    elementUnsupported = 1;
  }

}
