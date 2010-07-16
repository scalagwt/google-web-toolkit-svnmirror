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
package com.google.gwt.sample.collections.gwt.client;

import com.google.gwt.collections.Array;
import com.google.gwt.collections.CollectionFactory;
import com.google.gwt.collections.Map;
import com.google.gwt.collections.MutableArray;
import com.google.gwt.collections.MutableMap;
import com.google.gwt.user.client.Window;

import java.math.BigDecimal;

/**
 * A few snippets of code showing what we want to do with LWCs in terms of
 * supporting storage of unboxed primitive types.
 */
public class LwcPrimitiveTypesShowcase {
  @SuppressWarnings("unchecked")
  public static void clientNonPortableBehavior() {
    MutableArray<Double> jsDoubleArray = getJsNumberArray();
    
    if (jsDoubleArray.get(0) != 1) {
      Window.alert("jsDoubleArray.get(0) == 1 failed!");
    }

    if (jsDoubleArray.get(1) != 1.5) {
      Window.alert("jsDoubleArray.get(1) == 1.5 failed!");
    }
 
    MutableArray<Object> jsMixedArray = getJsMixedArray();
    
    if (((Integer) jsMixedArray.get(0)) !=  1) {
      Window.alert("(int) (jsMixedArray.get(0) ==  1 failed!");
    }
    
    if (((Double) jsMixedArray.get(1)) !=  1.5) {
      Window.alert("(int) (jsMixedArray.get(1) ==  1.5 failed!");
    }
    
    if (((Array<Double>) jsMixedArray.get(2)).get(0) != 1d) {
      Window.alert("((Array<Double>) jsMixedArray.get(2)).get(0) == 1d failed!");      
    }
    
    if (!((String) jsMixedArray.get(2)).equals("apple")) {
      Window.alert("((String) jsMixedArray.get(2)).equals(\"apple\") failed!");      
    }
    
    Object o = jsMixedArray.get(4);
    if (o != null && o instanceof Map<?,?>) {
      Map<String, Object> roMap = (Map<String, Object>) o;
      if (!((String) roMap.get("fruit")).equals("peach")) {
        Window.alert("((String) roMap.get(\"fruit\")).equals(\"peach\") failed!");      
      }
      if (((Double) roMap.get("weight")) != 1.5) {
        Window.alert("((Double) roMap.get(\"weight\")) == 1.5 failed!");      
      }
    } else {
      Window.alert("anythingGoesArray.get(4) instance of Map failed");      
    }    
  }

  public static native MutableArray<Object> getJsMixedArray() /*-{
    return [1, 1.5, [1], "apple", {"fruit": "pear", "weight": 1.5}];
  }-*/;

  public static native MutableArray<Double> getJsNumberArray() /*-{
    return [1, 1.5];
  }-*/;

  @SuppressWarnings("unchecked")
  public static void jrePortableBehavior() {
    MutableArray<Integer> intArray = CollectionFactory.createMutableArray();

    intArray.add(1);
    intArray.add((int) 1.5);
    intArray.add(new Integer(1));

    if (intArray.get(0) != 1) {
      Window.alert("intArray.get(0) == 1 failed!");
    }

    if (intArray.get(1) != 1) {
      Window.alert("intArray.get(1) == 1 failed!");
    }

    if (intArray.get(2) != 1) {
      Window.alert("intArray.get(2) == 1 failed!");
    }

    MutableArray<Double> doubleArray = CollectionFactory.createMutableArray();

    doubleArray.add(1.5);
    doubleArray.add(new Double(1.5));

    if (doubleArray.get(0) != 1.5) {
      Window.alert("doubleArray.get(0) == 1.5 failed!");
    }

    if (doubleArray.get(1) != 1.5) {
      Window.alert("doubleArray.get(1) == 1.5 failed!");
    }

    MutableArray<Number> numberArray = CollectionFactory.createMutableArray();

    numberArray.add(1);
    numberArray.add(new Integer(1));
    numberArray.add(1.5);
    numberArray.add(new Double(1.5));
    numberArray.add(5000000000L);
    numberArray.add(new Long(5000000000L));
    numberArray.add(new BigDecimal("1E1000"));

    if (!numberArray.get(0).equals(1)) {
      Window.alert("numberArray.get(0).equals(1) failed!");
    }

    if (!numberArray.get(1).equals(1)) {
      Window.alert("numberArray.get(1).equals(1) failed!");
    }

    if (!numberArray.get(2).equals(1.5)) {
      Window.alert("numberArray.get(2).equals(1.5) failed!");
    }

    if (!numberArray.get(3).equals(1.5)) {
      Window.alert("numberArray.get(3).equals(1.5) failed!");
    }

    if (!numberArray.get(4).equals(5000000000L)) {
      Window.alert("numberArray.get(2).equals(1.5) failed!");
    }

    if (!numberArray.get(5).equals(5000000000L)) {
      Window.alert("numberArray.get(3).equals(1.5) failed!");
    }

    if (!numberArray.get(6).equals(new BigDecimal("1E1000"))) {
      Window.alert(
          "numberArray.get(4).equals(new BigDecimal(\"1E1000\")) failed!");
    }

    MutableArray<String> stringArray = CollectionFactory.createMutableArray();

    stringArray.add("peach");

    if (!stringArray.get(0).equals("peach")) {
      Window.alert("stringArray.get(0).equals(\"peach\") failed!");
    }

    MutableArray<Object> anythingGoesArray = CollectionFactory.createMutableArray();
    
    // Like getJsMixedArray():
    // [1, 1.5, [1], "apple", {"fruit": "pear", "weight": 1.5}]
    anythingGoesArray.add(1);
    anythingGoesArray.add(1.5);
    MutableArray<Integer> array = CollectionFactory.createMutableArray();
    array.add(1);
    anythingGoesArray.add(array);
    anythingGoesArray.add("apple");
    MutableMap<String, Object> map = CollectionFactory.createMutableMap();
    map.put("fruit", "pear");
    map.put("weight", 1.5);
    anythingGoesArray.add(map);
    
    if (((Integer) anythingGoesArray.get(0)) !=  1) {
      Window.alert("((Integer) anythingGoesArray.get(0)) ==  1 failed!");
    }
    
    if (((Double) anythingGoesArray.get(1)) !=  1.5) {
      Window.alert("((Double) anythingGoesArray.get(1)) ==  1.5 failed!");
    }
    
    if (((Array<Integer>) anythingGoesArray.get(2)).get(0) != 1) {
      Window.alert("((Array<Integer>) anythingGoesArray.get(2)).get(0) == 1 failed!");      
    }
    
    if (!((String) anythingGoesArray.get(3)).equals("apple")) {
      Window.alert("((String) anythingGoesArray.get(3)).equals(\"apple\") failed!");      
    }
    
    Object o = anythingGoesArray.get(4);
    if (o != null && o instanceof Map<?,?>) {
      Map<String, Object> roMap = (Map<String, Object>) o;
      if (!((String) roMap.get("fruit")).equals("pear")) {
        Window.alert("((String) roMap.get(\"fruit\")).equals(\"pear\") failed!");      
      }
      if (((Double) roMap.get("weight")) != 1.5) {
        Window.alert("((Double) roMap.get(\"weight\")) == 1.5 failed!");      
      }
    } else {
      Window.alert("anythingGoesArray.get(3) instance of Map failed");      
    }
    
    Window.alert("Done!");
  }

}
