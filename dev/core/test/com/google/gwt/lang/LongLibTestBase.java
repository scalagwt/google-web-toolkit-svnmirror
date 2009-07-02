/*
 * Copyright 2008 Google Inc.
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
package com.google.gwt.lang;

import com.google.gwt.lang.LongLib.Const;

import junit.framework.TestCase;

/**
 * Test the LongLib class. The magic expected values were computed by using a
 * Java println on normal Java longs.
 */
public class LongLibTestBase extends TestCase {

  static void assertEquals(double[] expected, double[] actual) {
    assertTrue("expected=" + LongLib.toString(expected) + "  actual="
        + LongLib.toString(actual), LongLib.eq(expected, actual));
  }

  public void testAdditive() {
    {
      final double[] n1 = LongLib.fromInt(1234);
      final double[] n2 = LongLib.fromInt(9876);
      assertEquals(LongLib.fromInt(11110), LongLib.add(n1, n2));
      assertEquals(LongLib.fromInt(-8642), LongLib.sub(n1, n2));
    }

    {
      final double[] n1 = LongLib.fromInt(-1234);
      final double[] n2 = LongLib.fromInt(9876);
      assertEquals(LongLib.fromInt(8642), LongLib.add(n1, n2));
      assertEquals(LongLib.fromInt(-11110), LongLib.sub(n1, n2));
    }

    {
      final double[] n1 = LongLib.fromInt(-1234);
      final double[] n2 = LongLib.fromInt(-9876);
      assertEquals(LongLib.fromInt(-11110), LongLib.add(n1, n2));
      assertEquals(LongLib.fromInt(8642), LongLib.sub(n1, n2));
    }

    {
      final double[] n1 = longFromBits(0x12345678, 0xabcdabcd);
      final double[] n2 = longFromBits(0x77773333, 0x22224444);
      assertEquals(longFromBits(0x89ab89ab, 0xcdeff011), LongLib.add(n1, n2));
      assertEquals(longFromBits(0x9abd2345, 0x89ab6789), LongLib.sub(n1, n2));
    }
  }

  public void testBitOps() {
    {
      final double[] n1 = LongLib.fromInt(1234);
      final double[] n2 = LongLib.fromInt(9876);

      assertEquals(LongLib.fromInt(1168), LongLib.and(n1, n2));
      assertEquals(LongLib.fromInt(9942), LongLib.or(n1, n2));
      assertEquals(LongLib.fromInt(8774), LongLib.xor(n1, n2));
      assertEquals(LongLib.fromInt(-1235), LongLib.not(n1));
      assertEquals(LongLib.fromInt(-9877), LongLib.not(n2));
    }

    {
      final double[] n1 = LongLib.fromInt(-1234);
      final double[] n2 = LongLib.fromInt(9876);
      assertEquals(LongLib.fromInt(8708), LongLib.and(n1, n2));
      assertEquals(LongLib.fromInt(-66), LongLib.or(n1, n2));
      assertEquals(LongLib.fromInt(-8774), LongLib.xor(n1, n2));
      assertEquals(LongLib.fromInt(1233), LongLib.not(n1));
      assertEquals(LongLib.fromInt(-9877), LongLib.not(n2));
    }

    {
      final double[] n1 = LongLib.shl(LongLib.fromInt(0x1234), 32);
      final double[] n2 = LongLib.shl(LongLib.fromInt(0x9876), 32);
      assertEquals(LongLib.shl(LongLib.fromInt(0x1034), 32),
          LongLib.and(n1, n2));
      assertEquals(LongLib.shl(LongLib.fromInt(0x9a76), 32), LongLib.or(n1, n2));
      assertEquals(LongLib.shl(LongLib.fromInt(0x8a42), 32),
          LongLib.xor(n1, n2));
      assertEquals(longFromBits(0xffffedcb, 0xffffffff), LongLib.not(n1));
      assertEquals(longFromBits(0xffff6789, 0xffffffff), LongLib.not(n2));
    }
  }

  public void testComparisons() {
    assertTrue(LongLib.lt(LongLib.fromInt(10), LongLib.fromInt(11)));
    assertTrue(LongLib.lte(LongLib.fromInt(10), LongLib.fromInt(11)));
    assertTrue(!LongLib.eq(LongLib.fromInt(10), LongLib.fromInt(11)));
    assertTrue(!LongLib.gte(LongLib.fromInt(10), LongLib.fromInt(11)));
    assertTrue(!LongLib.gt(LongLib.fromInt(10), LongLib.fromInt(11)));

    assertTrue(!LongLib.lt(LongLib.fromInt(10), LongLib.fromInt(10)));
    assertTrue(LongLib.lte(LongLib.fromInt(10), LongLib.fromInt(10)));
    assertTrue(LongLib.eq(LongLib.fromInt(10), LongLib.fromInt(10)));
    assertTrue(LongLib.gte(LongLib.fromInt(10), LongLib.fromInt(10)));
    assertTrue(!LongLib.gt(LongLib.fromInt(10), LongLib.fromInt(10)));

    assertTrue(!LongLib.lt(LongLib.fromInt(12), LongLib.fromInt(11)));
    assertTrue(!LongLib.lte(LongLib.fromInt(12), LongLib.fromInt(11)));
    assertTrue(!LongLib.eq(LongLib.fromInt(12), LongLib.fromInt(11)));
    assertTrue(LongLib.gte(LongLib.fromInt(12), LongLib.fromInt(11)));
    assertTrue(LongLib.gt(LongLib.fromInt(12), LongLib.fromInt(11)));

    // the following three comparisons cannot be implemented by
    // subtracting the arguments, because the subtraction causes an overflow
    final double[] largeNeg = longFromBits(0x82341234, 0x0);
    final double[] largePos = longFromBits(0x12341234, 0x0);
    assertTrue(LongLib.lt(largeNeg, largePos));

    assertTrue(LongLib.lt(Const.MIN_VALUE, LongLib.fromInt(0)));
    assertTrue(LongLib.gt(LongLib.fromInt(0), Const.MIN_VALUE));

    final double[] largePosPlusOne = LongLib.add(largePos, LongLib.fromInt(1));

    assertTrue(LongLib.lt(largePos, largePosPlusOne));
    assertTrue(LongLib.lte(largePos, largePosPlusOne));
    assertTrue(!LongLib.eq(largePos, largePosPlusOne));
    assertTrue(!LongLib.gte(largePos, largePosPlusOne));
    assertTrue(!LongLib.gt(largePos, largePosPlusOne));

    assertTrue(!LongLib.lt(largePos, largePos));
    assertTrue(LongLib.lte(largePos, largePos));
    assertTrue(LongLib.eq(largePos, largePos));
    assertTrue(LongLib.gte(largePos, largePos));
    assertTrue(!LongLib.gt(largePos, largePos));

    assertTrue(!LongLib.lt(largePosPlusOne, largePos));
    assertTrue(!LongLib.lte(largePosPlusOne, largePos));
    assertTrue(!LongLib.eq(largePosPlusOne, largePos));
    assertTrue(LongLib.gte(largePosPlusOne, largePos));
    assertTrue(LongLib.gt(largePosPlusOne, largePos));
  }

  public void testConversions() {
    assertEquals(10, LongLib.toInt(longFromBits(0, 10)));
    assertEquals(-10, LongLib.toInt(longFromBits(0, -10)));
    assertEquals(-10, LongLib.toInt(longFromBits(100, -10)));
    assertEquals(-10, LongLib.toInt(longFromBits(-100000, -10)));
  }

  public void testDiv() {
    double[] deadBeef = LongLib.typeChange(0xdeadbeefdeadbeefL);
    double[] ten = LongLib.fromInt(10);
    assertEquals(LongLib.typeChange(-240105308887621659L), LongLib.div(
        deadBeef, ten));
    assertEquals(Const.ZERO, LongLib.div(Const.ONE, Const.TWO));
    assertEquals(LongLib.typeChange(4611686018427387903L), LongLib.div(
        Const.MAX_VALUE, Const.TWO));
  }

  public void testFactorial() {
    double[] fact18 = fact(LongLib.fromInt(18));
    double[] fact17 = fact(LongLib.fromInt(17));
    assertEquals(LongLib.fromInt(18), LongLib.div(fact18, fact17));
  }

  public void testFromDouble() {
    // these tests are based on JLS3, section 5.1.3

    assertEquals(LongLib.fromInt(10), LongLib.fromDouble(10.5));
    assertEquals(LongLib.fromInt(-10), LongLib.fromDouble(-10.5));
    assertEquals(LongLib.shl(LongLib.fromInt(1), 55),
        LongLib.fromDouble(Math.pow(2.0, 55) + 0.5));
    assertEquals(LongLib.neg(LongLib.shl(LongLib.fromInt(1), 55)),
        LongLib.fromDouble(-Math.pow(2.0, 55) - 0.5));

    assertEquals(LongLib.fromInt(0), LongLib.fromDouble(Double.NaN));

    assertEquals(Const.MAX_VALUE, LongLib.fromDouble(Math.pow(2.0, 100)));
    assertEquals(Const.MAX_VALUE, LongLib.fromDouble(Double.POSITIVE_INFINITY));
    assertEquals(Const.MIN_VALUE, LongLib.fromDouble(-Math.pow(2.0, 100)));
    assertEquals(Const.MIN_VALUE, LongLib.fromDouble(Double.NEGATIVE_INFINITY));
  }

  public void testMinMax() {
    assertEquals(Const.MIN_VALUE, LongLib.shl(LongLib.fromInt(1), 63));
    assertEquals(Const.MAX_VALUE, LongLib.neg(LongLib.add(Const.MIN_VALUE,
        LongLib.fromInt(1))));
  }

  public void testMultiplicative() {
    assertEquals(LongLib.fromInt(3333), LongLib.mul(LongLib.fromInt(1111),
        LongLib.fromInt(3)));
    assertEquals(LongLib.fromInt(-3333), LongLib.mul(LongLib.fromInt(1111),
        LongLib.fromInt(-3)));
    assertEquals(LongLib.fromInt(-3333), LongLib.mul(LongLib.fromInt(-1111),
        LongLib.fromInt(3)));
    assertEquals(LongLib.fromInt(3333), LongLib.mul(LongLib.fromInt(-1111),
        LongLib.fromInt(-3)));
    assertEquals(LongLib.fromInt(0), LongLib.mul(LongLib.fromInt(100),
        LongLib.fromInt(0)));

    assertEquals(longFromBits(0x7ff63f7c, 0x1df4d840), LongLib.mul(
        longFromBits(0x12345678, 0x12345678), longFromBits(0x1234, 0x12345678)));
    assertEquals(longFromBits(0x7ff63f7c, 0x1df4d840), LongLib.mul(
        longFromBits(0xf2345678, 0x12345678), longFromBits(0x1234, 0x12345678)));
    assertEquals(longFromBits(0x297e3f7c, 0x1df4d840), LongLib.mul(
        longFromBits(0xf2345678, 0x12345678), longFromBits(0xffff1234,
            0x12345678)));

    assertEquals(LongLib.fromInt(0), LongLib.mul(Const.MIN_VALUE,
        LongLib.fromInt(2)));
    assertEquals(Const.MIN_VALUE, LongLib.mul(Const.MIN_VALUE,
        LongLib.fromInt(1)));
    assertEquals(Const.MIN_VALUE, LongLib.mul(Const.MIN_VALUE,
        LongLib.fromInt(-1)));

    assertEquals(LongLib.fromInt(1), LongLib.div(LongLib.fromInt(5),
        LongLib.fromInt(5)));
    assertEquals(LongLib.fromInt(333), LongLib.div(LongLib.fromInt(1000),
        LongLib.fromInt(3)));
    assertEquals(LongLib.fromInt(-333), LongLib.div(LongLib.fromInt(1000),
        LongLib.fromInt(-3)));
    assertEquals(LongLib.fromInt(-333), LongLib.div(LongLib.fromInt(-1000),
        LongLib.fromInt(3)));
    assertEquals(LongLib.fromInt(333), LongLib.div(LongLib.fromInt(-1000),
        LongLib.fromInt(-3)));
    assertEquals(LongLib.fromInt(0), LongLib.div(LongLib.fromInt(3),
        LongLib.fromInt(1000)));
    assertEquals(longFromBits(0x1003d0, 0xe84f5ae8), LongLib.div(longFromBits(
        0x12345678, 0x12345678), longFromBits(0x0, 0x123)));
    assertEquals(longFromBits(0x0, 0x10003), LongLib.div(longFromBits(
        0x12345678, 0x12345678), longFromBits(0x1234, 0x12345678)));
    assertEquals(longFromBits(0xffffffff, 0xffff3dfe), LongLib.div(
        longFromBits(0xf2345678, 0x12345678), longFromBits(0x1234, 0x12345678)));
    assertEquals(longFromBits(0x0, 0xeda), LongLib.div(longFromBits(0xf2345678,
        0x12345678), longFromBits(0xffff1234, 0x12345678)));

    try {
      LongLib.div(LongLib.fromInt(1), LongLib.fromInt(0));
      fail("Expected an ArithmeticException");
    } catch (ArithmeticException e) {
    }

    assertEquals(longFromBits(0xc0000000, 0x00000000), LongLib.div(
        Const.MIN_VALUE, LongLib.fromInt(2)));
    assertEquals(Const.MIN_VALUE, LongLib.div(Const.MIN_VALUE,
        LongLib.fromInt(1)));
    assertEquals(Const.MIN_VALUE, LongLib.div(Const.MIN_VALUE,
        LongLib.fromInt(-1))); // JLS3 section 15.17.2
  }

  public void testNegate() {
    assertEquals(LongLib.fromInt(-1), LongLib.neg(LongLib.fromInt(1)));
    assertEquals(LongLib.fromInt(1), LongLib.neg(LongLib.fromInt(-1)));

    // JLS3 15.15.4
    assertEquals(Const.MIN_VALUE, LongLib.neg(Const.MIN_VALUE));
  }

  public void testShift() {
    assertEquals(longFromBits(0xd048d115, 0x9d159c00), LongLib.shl(
        longFromBits(0x12341234, 0x45674567), 10));
    assertEquals(longFromBits(0x48d04, 0x8d1159d1), LongLib.shr(longFromBits(
        0x12341234, 0x45674567), 10));
    assertEquals(longFromBits(0x48d04, 0x8d1159d1), LongLib.shru(longFromBits(
        0x12341234, 0x45674567), 10));
    assertEquals(longFromBits(0xd048d115, 0x9d159c00), LongLib.shl(
        longFromBits(0x92341234, 0x45674567), 10));
    assertEquals(longFromBits(0xffe48d04, 0x8d1159d1), LongLib.shr(
        longFromBits(0x92341234, 0x45674567), 10));
    assertEquals(longFromBits(0x248d04, 0x8d1159d1), LongLib.shru(longFromBits(
        0x92341234, 0x45674567), 10));

    assertEquals(LongLib.fromInt(-1), LongLib.shr(LongLib.fromInt(-1), 10));

    assertEquals(LongLib.fromInt(-1 << 5), LongLib.shl(LongLib.fromInt(-1), 5));
    assertEquals(LongLib.fromInt(-1), LongLib.shl(LongLib.fromInt(-1), 0));
    assertEquals(LongLib.neg(LongLib.typeChange(0x4000000000000000L)),
        LongLib.shr(LongLib.shl(LongLib.fromInt(1), 63), 1));
    assertEquals(LongLib.fromInt(0), LongLib.shl(LongLib.shl(
        LongLib.fromInt(-1), 32), 32));
    assertEquals(Const.MIN_VALUE, LongLib.shl(Const.MIN_VALUE, 0));
    assertEquals(LongLib.fromInt(0), LongLib.shl(Const.MIN_VALUE, 1));
    assertEquals(longFromBits(0xfffffffc, 0x00000000), LongLib.shr(
        LongLib.neg(longFromBits(8, 0)), 1));
    assertEquals(longFromBits(0x7ffffffc, 0x0), LongLib.shru(
        LongLib.neg(longFromBits(8, 0)), 1));
  }

  // Issue 1198, and also a good exercise of several methods.
  public void testToHexString() {
    double[] deadbeaf12341234 = longFromBits(0xdeadbeaf, 0x12341234);

    assertEquals("deadbeaf12341234", toHexString(deadbeaf12341234));
  }

  public void testToString() {
    assertEquals("0", LongLib.toString(LongLib.fromInt(0)));
    assertEquals("1", LongLib.toString(LongLib.fromInt(1)));
    assertEquals("-1", LongLib.toString(LongLib.fromInt(-1)));
    assertEquals("-10", LongLib.toString(LongLib.fromInt(-10)));
    assertEquals("-9223372036854775808", LongLib.toString(Const.MIN_VALUE));

    int top = 922337201;
    int bottom = 967490662;
    double[] fullnum = LongLib.add(LongLib.mul(LongLib.fromInt(1000000000),
        LongLib.fromInt(top)), LongLib.fromInt(bottom));

    assertEquals("922337201967490662", LongLib.toString(fullnum));
    assertEquals("-922337201967490662", LongLib.toString(LongLib.neg(fullnum)));
  }

  private double[] fact(double[] n) {
    if (LongLib.eq(n, LongLib.fromInt(0))) {
      return LongLib.fromInt(1);
    } else {
      return LongLib.mul(n, fact(LongLib.sub(n, LongLib.fromInt(1))));
    }
  }

  private double[] longFromBits(int top, int bottom) {
    double[] topHalf = LongLib.shl(LongLib.fromInt(top), 32);
    double[] bottomHalf = LongLib.fromInt(bottom);
    if (LongLib.lt(bottomHalf, Const.ZERO)) {
      bottomHalf = LongLib.add(bottomHalf, LongLib.shl(LongLib.fromInt(1), 32));
    }
    double[] total = LongLib.add(topHalf, bottomHalf);
    return total;
  }

  private String toHexString(double[] x) {
    // copied from the GWT Long class and modified for double[]
    double[] zero = LongLib.fromInt(0);

    if (LongLib.eq(x, zero)) {
      return "0";
    }
    String[] hexDigits = new String[] {
        "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d",
        "e", "f"};
    String hexStr = "";
    while (!LongLib.eq(x, zero)) {
      int nibble = LongLib.toInt(x) & 0xF;
      hexStr = hexDigits[nibble] + hexStr;
      x = LongLib.shru(x, 4);
    }
    return hexStr;
  }
}
