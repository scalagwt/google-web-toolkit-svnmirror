
package com.google.gwt.dev.shell.profiler;

import junit.framework.TestCase;

/*
 * Copyright 2006 Google Inc.
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

/**
 * Tests the Timer implementation.
 */
public class TestTimer extends TestCase {

  public final void testTimer() {
    long sleepMillis = 100;

    long startTime = Timer.nanoTime();

    try {
      Thread.sleep( sleepMillis );
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    long endTime = Timer.nanoTime();
    long elapsedTime = endTime - startTime;
    assertTrue( elapsedTime >= sleepMillis * 1000000 );
  }

  // Benchmark the timer itself
  public final void testTimer2() {
    long totalNanos = 0;
    long currentNanos = Timer.nanoTime();
    int totalIterations = 1000000;
    for ( int i = 0; i < totalIterations; ++i ) {
      long newNanos = Timer.nanoTime();
      totalNanos += newNanos - currentNanos;
      currentNanos = newNanos;
    }
    double totalNanosDouble = totalNanos;
    System.out.println( "Timer invoke cost: " + totalNanosDouble / totalIterations + "(ns)" );
  }
}
