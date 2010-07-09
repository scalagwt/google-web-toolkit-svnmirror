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
package java.lang;

/**
 * Wraps a primitive <code>double</code> as an object.
 */
public final class Double extends Number implements Comparable<Double> {
  public static final double MAX_VALUE = 1.7976931348623157e+308;
  public static final double MIN_VALUE = 4.9e-324;
  public static final double MIN_NORMAL = 2.2250738585072014e-308;
  public static final int MAX_EXPONENT = 1023; 
                             // ==Math.getExponent(Double.MAX_VALUE);
  public static final int MIN_EXPONENT = -1022; 
                             // ==Math.getExponent(Double.MIN_NORMAL);;

  public static final double NaN = 0d / 0d;
  public static final double NEGATIVE_INFINITY = -1d / 0d;
  public static final double POSITIVE_INFINITY = 1d / 0d;
  public static final int SIZE = 64;

  public static int compare(double x, double y) {
    if (isNaN(x)) {
      if (isNaN(y)) {
        return 0;
      } else {
        return 1;
      }
    } else if (isNaN(y)) {
      return -1;
    }
    
    if (x < y) {
      return -1;
    } else if (x > y) {
      return 1;
    } else {
      return 0;
    }
  }

  /**
   * @skip Here for shared implementation with Arrays.hashCode
   */
  public static int hashCode(double d) {
    return (int) d;
  }

  public static native boolean isInfinite(double x) /*-{
    return !isFinite(x);
  }-*/;

  public static native boolean isNaN(double x) /*-{
    return isNaN(x);
  }-*/;

  public static double parseDouble(String s) throws NumberFormatException {
    return __parseAndValidateDouble(s);
  }

  public static String toString(double b) {
    return String.valueOf(b);
  }

  public static Double valueOf(double d) {
    return new Double(d);
  }

  public static Double valueOf(String s) throws NumberFormatException {
    return new Double(Double.parseDouble(s));
  }

  private final transient double value;

  public Double(double value) {
    this.value = value;
  }

  public Double(String s) {
    value = parseDouble(s);
  }

  @Override
  public byte byteValue() {
    return (byte) value;
  }

  public int compareTo(Double b) {
    return compare(this.value, b.value);
  }

  @Override
  public double doubleValue() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    return (o instanceof Double) && (((Double) o).value == value);
  }

  @Override
  public float floatValue() {
    return (float) value;
  }

  /**
   * Performance caution: using Double objects as map keys is not recommended.
   * Using double values as keys is generally a bad idea due to difficulty
   * determining exact equality. In addition, there is no efficient JavaScript
   * equivalent of <code>doubleToIntBits</code>. As a result, this method
   * computes a hash code by truncating the whole number portion of the double,
   * which may lead to poor performance for certain value sets if Doubles are
   * used as keys in a {@link java.util.HashMap}.
   */
  @Override
  public int hashCode() {
    return hashCode(value);
  }

  @Override
  public int intValue() {
    return (int) value;
  }

  public boolean isInfinite() {
    return isInfinite(value);
  }

  public boolean isNaN() {
    return isNaN(value);
  }

  @Override
  public long longValue() {
    return (long) value;
  }

  @Override
  public short shortValue() {
    return (short) value;
  }

  @Override
  public String toString() {
    return toString(value);
  }

}
