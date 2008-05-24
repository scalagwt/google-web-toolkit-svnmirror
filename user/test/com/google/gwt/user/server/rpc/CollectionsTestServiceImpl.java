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
package com.google.gwt.user.server.rpc;

import com.google.gwt.user.client.rpc.CollectionsTestService;
import com.google.gwt.user.client.rpc.TestSetFactory;
import com.google.gwt.user.client.rpc.TestSetValidator;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Vector;

/**
 * TODO: document me.
 */
public class CollectionsTestServiceImpl extends RemoteServiceServlet implements
    CollectionsTestService {

  private static String toString(Object[] values) {
    return Arrays.asList(values).toString();
  }

  @SuppressWarnings("unchecked")
  public ArrayList echo(ArrayList list) throws CollectionsTestServiceException {
    if (!TestSetValidator.isValid(list)) {
      throw new CollectionsTestServiceException();
    }

    return list;
  }

  public boolean[] echo(boolean[] actual)
      throws CollectionsTestServiceException {
    boolean[] expected = TestSetFactory.createPrimitiveBooleanArray();
    if (!TestSetValidator.equals(expected, actual)) {
      throw new CollectionsTestServiceException("expected: "
          + expected.toString() + " actual: " + actual.toString());
    }

    return actual;
  }

  public Boolean[] echo(Boolean[] actual)
      throws CollectionsTestServiceException {
    Boolean[] expected = TestSetFactory.createBooleanArray();
    if (!TestSetValidator.equals(expected, actual)) {
      throw new CollectionsTestServiceException("expected: "
          + expected.toString() + " actual: " + actual.toString());
    }

    return actual;
  }

  public byte[] echo(byte[] actual) throws CollectionsTestServiceException {
    byte[] expected = TestSetFactory.createPrimitiveByteArray();
    if (!TestSetValidator.equals(expected, actual)) {
      throw new CollectionsTestServiceException("expected: "
          + expected.toString() + " actual: " + actual.toString());
    }

    return actual;
  }

  public Byte[] echo(Byte[] actual) throws CollectionsTestServiceException {
    Byte[] expected = TestSetFactory.createByteArray();
    if (!TestSetValidator.equals(expected, actual)) {
      throw new CollectionsTestServiceException("expected: "
          + expected.toString() + " actual: " + actual.toString());
    }

    return actual;
  }

  public char[] echo(char[] actual) throws CollectionsTestServiceException {
    char[] expected = TestSetFactory.createPrimitiveCharArray();
    if (!TestSetValidator.equals(expected, actual)) {
      throw new CollectionsTestServiceException("expected: "
          + expected.toString() + " actual: " + actual.toString());
    }

    return actual;
  }

  public Character[] echo(Character[] actual)
      throws CollectionsTestServiceException {
    Character[] expected = TestSetFactory.createCharArray();
    if (!TestSetValidator.equals(expected, actual)) {
      throw new CollectionsTestServiceException("expected: "
          + expected.toString() + " actual: " + actual.toString());
    }

    return actual;
  }

  public Date[] echo(Date[] actual) throws CollectionsTestServiceException {
    Date[] expected = TestSetFactory.createDateArray();
    if (!TestSetValidator.equals(expected, actual)) {
      throw new CollectionsTestServiceException("expected: "
          + toString(expected) + " actual: " + toString(actual));
    }

    return actual;
  }

  public java.sql.Date[] echo(java.sql.Date[] actual)
      throws CollectionsTestServiceException {
    java.sql.Date[] expected = TestSetFactory.createSqlDateArray();
    if (!TestSetValidator.equals(expected, actual)) {
      throw new CollectionsTestServiceException("expected: "
          + toString(expected) + " actual: " + toString(actual));
    }

    return actual;
  }

  public double[] echo(double[] actual) throws CollectionsTestServiceException {
    double[] expected = TestSetFactory.createPrimitiveDoubleArray();
    if (!TestSetValidator.equals(expected, actual)) {
      throw new CollectionsTestServiceException("expected: "
          + expected.toString() + " actual: " + actual.toString());
    }

    return actual;
  }

  public Double[] echo(Double[] actual) throws CollectionsTestServiceException {
    Double[] expected = TestSetFactory.createDoubleArray();
    if (!TestSetValidator.equals(expected, actual)) {
      throw new CollectionsTestServiceException("expected: "
          + expected.toString() + " actual: " + actual.toString());
    }

    return actual;
  }

  public float[] echo(float[] actual) throws CollectionsTestServiceException {
    float[] expected = TestSetFactory.createPrimitiveFloatArray();
    if (!TestSetValidator.equals(expected, actual)) {
      throw new CollectionsTestServiceException("expected: "
          + expected.toString() + " actual: " + actual.toString());
    }

    return actual;
  }

  public Float[] echo(Float[] actual) throws CollectionsTestServiceException {
    Float[] expected = TestSetFactory.createFloatArray();
    if (!TestSetValidator.equals(expected, actual)) {
      throw new CollectionsTestServiceException("expected: "
          + expected.toString() + " actual: " + actual.toString());
    }

    return actual;
  }

  @SuppressWarnings("unchecked")
  public HashMap echo(HashMap actual) throws CollectionsTestServiceException {
    HashMap expected = TestSetFactory.createHashMap();
    if (!TestSetValidator.isValid(expected, actual)) {
      throw new CollectionsTestServiceException("expected: "
          + expected.toString() + " actual: " + actual.toString());
    }

    return actual;
  }

  @SuppressWarnings("unchecked")
  public LinkedHashMap echo(LinkedHashMap actual)
      throws CollectionsTestServiceException {
    HashMap expected = TestSetFactory.createLinkedHashMap();
    if (!TestSetValidator.isValid(expected, actual)) {
      throw new CollectionsTestServiceException("expected:"
          + expected.toString() + " actual:" + actual.toString());
    }
    return actual;
  }

  @SuppressWarnings("unchecked")
  public HashSet echo(HashSet actual) throws CollectionsTestServiceException {
    HashSet expected = TestSetFactory.createHashSet();
    if (!TestSetValidator.isValid(expected, actual)) {
      throw new CollectionsTestServiceException("expected: "
          + expected.toString() + " actual: " + actual.toString());
    }

    return actual;
  }

  public int[] echo(int[] actual) throws CollectionsTestServiceException {
    int[] expected = TestSetFactory.createPrimitiveIntegerArray();
    if (!TestSetValidator.equals(expected, actual)) {

      // It could be the very large array
      expected = TestSetFactory.createVeryLargeArray();
      if (!TestSetValidator.equals(expected, actual)) {
        throw new CollectionsTestServiceException("expected: "
            + expected.toString() + " actual: " + actual.toString());
      }
    }

    return actual;
  }

  public Integer[] echo(Integer[] actual)
      throws CollectionsTestServiceException {
    Integer[] expected = TestSetFactory.createIntegerArray();
    if (!TestSetValidator.equals(expected, actual)) {
      throw new CollectionsTestServiceException("expected: "
          + expected.toString() + " actual: " + actual.toString());
    }

    return actual;
  }

  public long[] echo(long[] actual) throws CollectionsTestServiceException {
    long[] expected = TestSetFactory.createPrimitiveLongArray();
    if (!TestSetValidator.equals(expected, actual)) {
      throw new CollectionsTestServiceException("expected: "
          + expected.toString() + " actual: " + actual.toString());
    }

    return actual;
  }

  public Long[] echo(Long[] actual) throws CollectionsTestServiceException {
    Long[] expected = TestSetFactory.createLongArray();
    if (!TestSetValidator.equals(expected, actual)) {
      throw new CollectionsTestServiceException("expected: "
          + toString(expected) + " actual: " + toString(actual));
    }

    return actual;
  }

  public short[] echo(short[] actual) throws CollectionsTestServiceException {
    short[] expected = TestSetFactory.createPrimitiveShortArray();
    if (!TestSetValidator.equals(expected, actual)) {
      throw new CollectionsTestServiceException("expected: "
          + expected.toString() + " actual: " + actual.toString());
    }

    return actual;
  }

  public Short[] echo(Short[] actual) throws CollectionsTestServiceException {
    Short[] expected = TestSetFactory.createShortArray();
    if (!TestSetValidator.equals(expected, actual)) {
      throw new CollectionsTestServiceException("expected: "
          + expected.toString() + " actual: " + actual.toString());
    }

    return actual;
  }

  public String[] echo(String[] actual) throws CollectionsTestServiceException {
    String[] expected = TestSetFactory.createStringArray();
    if (!TestSetValidator.equals(expected, actual)) {
      throw new CollectionsTestServiceException("expected: "
          + expected.toString() + " actual: " + actual.toString());
    }

    return actual;
  }

  public String[][] echo(String[][] value)
      throws CollectionsTestServiceException {
    return value;
  }

  public Time[] echo(Time[] actual) throws CollectionsTestServiceException {
    Time[] expected = TestSetFactory.createSqlTimeArray();
    if (!TestSetValidator.equals(expected, actual)) {
      throw new CollectionsTestServiceException("expected: "
          + toString(expected) + " actual: " + toString(actual));
    }

    return actual;
  }

  public Timestamp[] echo(Timestamp[] actual)
      throws CollectionsTestServiceException {
    Timestamp[] expected = TestSetFactory.createSqlTimestampArray();
    if (!TestSetValidator.equals(expected, actual)) {
      throw new CollectionsTestServiceException("expected: "
          + toString(expected) + " actual: " + toString(actual));
    }

    return actual;
  }

  @SuppressWarnings("unchecked")
  public Vector echo(Vector actual) throws CollectionsTestServiceException {
    Vector expected = TestSetFactory.createVector();
    if (!TestSetValidator.isValid(expected, actual)) {
      throw new CollectionsTestServiceException("expected: "
          + expected.toString() + " actual: " + actual.toString());
    }

    return actual;
  }

  /**
   * Return the result of Arrays.asList(Object[]) to force an
   * InvocationException on the client.
   */
  @SuppressWarnings("unchecked")
  public List echoArraysAsList(List value)
      throws CollectionsTestServiceException {
    if (!TestSetValidator.isValidAsList(value)) {
      throw new CollectionsTestServiceException();
    }

    return value;
  }
}
