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

import com.google.gwt.benchmarks.client.Benchmark;
import com.google.gwt.benchmarks.client.IntRange;
import com.google.gwt.benchmarks.client.Operator;
import com.google.gwt.benchmarks.client.RangeField;

import java.util.ArrayList;

/**
 * Benchmarks the performance of various MutableArray methods.
 */
public class MutableArrayComparisonBenchmark extends Benchmark {

  final IntRange elemRange = new IntRange(5, 30005, Operator.ADD, 5000);
  final ArrayBuildType[] testKind = {ArrayBuildType.GWT_ADD,
      ArrayBuildType.GWT_SET, ArrayBuildType.GWT_SET_INIT,
      ArrayBuildType.JRE_ARRAY, ArrayBuildType.JRE_LIST}; 
  
  /**
   * Type of array construction to benchmark
   */
  protected enum ArrayBuildType {
    GWT_ADD("GWT Array add()"), GWT_SET("GWT sized create"), 
    GWT_SET_INIT("GWT sized create + initialization"), JRE_ARRAY("JRE Array"),
    JRE_LIST("JRE ArrayList");

    public String description;

    private ArrayBuildType(String description) {
      this.description = description;
    }
  }

  @Override
  public String getModuleName() {
    return "com.google.gwt.collections.Collections";
  }
  
  /**
   * Required by benchmarking framework.
   */
  public void testComparativePerformance() {
  }
  
  public void testComparativePerformance(
      @RangeField("elemRange") Integer numElements,
      @RangeField("testKind") ArrayBuildType test) {
    switch (test) {
      case GWT_ADD:
        gwtCollectionsArrayAddGrowth(numElements);
        break;
        
      case GWT_SET:
        gwtCollectionsArraySetSizeGrowth(numElements);
        break;
        
      case GWT_SET_INIT:
        gwtCollectionsArraySetSizeInitGrowth(numElements);
        break;
        
      case JRE_ARRAY:
        javaArraySetGrowth(numElements);
        break;
        
      case JRE_LIST:
        jreArrayListAddGrowth(numElements);
        break;

      default:
        break;
    }
  }
  
  private void gwtCollectionsArrayAddGrowth(Integer numElements) {
    MutableArray<Integer> ma = CollectionFactory.createMutableArray();
    
    for (int i = 0; i < numElements; i++) {
      ma.add(i);
    }
  }
  
  private void gwtCollectionsArraySetSizeGrowth(Integer numElements) {
    MutableArray<Integer> ma = CollectionFactory.createMutableArray(numElements);
    
    for (int i = 0; i < numElements; i++) {
      ma.set(i, i);
    }    
  }
  
  private void gwtCollectionsArraySetSizeInitGrowth(Integer numElements) {
    MutableArray<Integer> ma = 
      CollectionFactory.createMutableArray(numElements, new Integer(0));
    
    for (int i = 0; i < numElements; i++) {
      ma.set(i, i);
    }    
  }
  
  private void javaArraySetGrowth(Integer numElements) {
    Integer[] ia = new Integer[numElements];
    
    for (int i = 0; i < numElements; i++) {
      ia[i] = i;
    }
  }
  
  private void jreArrayListAddGrowth(Integer numElements) {
    ArrayList<Integer> al = new ArrayList<Integer>();
    
    for (int i = 0; i < numElements; i++) {
      al.add(i);
    }
  }

}
