/*
 * Copyright 2007 Google Inc.
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
package com.google.gwt.junit.client;

import java.util.Iterator;

/**
 * A {@link com.google.gwt.junit.client.Range} that iterates over a start and
 * end value by a stepping function. Typically used by benchmarks to supply a
 * range of values over an integral parameter, such as size or length.
 *
 */
public class IntRange implements Range {

  /**
   * Implementation of the Iterator.
   *
   */
  private static class IntRangeIterator extends RangeIterator {

    int end;

    Operator operator;

    int start;

    int step;

    int value;

    IntRangeIterator(IntRange r) {
      this.value = this.start = r.start;
      this.end = r.end;
      this.operator = r.operator;
      if (operator == null) {
        throw new IllegalArgumentException("operator must be \"*\" or \"+\"");
      }
      this.step = r.step;
    }

    public boolean hasNext() {
      return value <= end;
    }

    public Object next() {
      int currentValue = value;
      value = step();
      return new Integer(currentValue);
    }

    public int step() {
      if (operator == Operator.MULTIPLY) {
        return value * step;
      } else {
        return value + step;
      }
    }
  }

  int end;

  Operator operator;

  int start;

  int step;

  /**
   * Creates a new range that produces Iterators which begin at
   * <code>start</code>, end at <code>end</code> and increment by the
   * stepping function described by <code>operator</code> and
   * <code>step</code>.
   *
   * @param start Initial starting value, inclusive.
   * @param end Ending value, inclusive.
   * @param operator The function used to step.
   * @param step The amount to step by, for each iteration.
   */
  public IntRange(int start, int end, Operator operator, int step) {
    this.start = start;
    this.end = end;
    this.operator = operator;
    this.step = step;
    if (step <= 0) {
      throw new IllegalArgumentException("step must be > 0");
    }
  }

  public Iterator iterator() {
    return new IntRangeIterator(this);
  }
}
