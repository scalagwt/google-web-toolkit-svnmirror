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
 */
public class MutableSetCustomAdapterTest extends MutableSetAdapterTest<
    Integer> {
  
  @Override
  public void gwtSetUp() {
    super.gwtSetUp();
    element1 = -1;
    element2 = -2;
    element3 = -3;
    element4 = -4;
    element5 = -5;
    elementUnsupported = 1;
    
    adapter1 = new Relation<Object, String>() {
      public String applyTo(Object element) {
        if (!(element instanceof Integer) || (Integer) element >= 0) {
          return null;
        }
        return "__" + ((Integer) element).toString();
      }
    };
    
    adapter2 = new Relation<Object, String>() {
      public String applyTo(Object element) {
        if (!(element instanceof Integer) || (Integer) element >= 0) {
          return null;
        }
        return "_$_" + ((Integer) element).toString();
      }
    };
  }

}
