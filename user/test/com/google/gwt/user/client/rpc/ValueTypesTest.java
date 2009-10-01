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
package com.google.gwt.user.client.rpc;

import com.google.gwt.core.client.GWT;
import com.google.gwt.junit.client.GWTTestCase;

/**
 * TODO: document me.
 */
public class ValueTypesTest extends GWTTestCase {

  private static final int TEST_DELAY = 5000;

  private ValueTypesTestServiceAsync primitiveTypeTestService;

  public String getModuleName() {
    return "com.google.gwt.user.RPCSuite";
  }

  public void testBoolean_FALSE() {
    delayTestFinish(TEST_DELAY);
    ValueTypesTestServiceAsync service = getServiceAsync();
    service.echo_FALSE(false, new AsyncCallback() {

      public void onFailure(Throwable caught) {
        TestSetValidator.rethrowException(caught);
      }

      public void onSuccess(Object result) {
        assertNotNull("Was null", result);
        assertFalse("Should have been false", ((Boolean) result).booleanValue());
        finishTest();
      }
    });
  }

  public void testBoolean_TRUE() {
    delayTestFinish(TEST_DELAY);
    ValueTypesTestServiceAsync service = getServiceAsync();
    service.echo_TRUE(true, new AsyncCallback() {

      public void onFailure(Throwable caught) {
        TestSetValidator.rethrowException(caught);
      }

      public void onSuccess(Object result) {
        assertNotNull(result);
        assertTrue(((Boolean) result).booleanValue());
        finishTest();
      }
    });
  }

  public void testByte() {
    delayTestFinish(TEST_DELAY);
    ValueTypesTestServiceAsync service = getServiceAsync();
    service.echo((byte) (Byte.MAX_VALUE / (byte) 2), new AsyncCallback() {

      public void onFailure(Throwable caught) {
        TestSetValidator.rethrowException(caught);
      }

      public void onSuccess(Object result) {
        assertNotNull(result);
        assertEquals(Byte.MAX_VALUE / 2, ((Byte) result).byteValue());
        finishTest();
      }
    });
  }

  public void testByte_MAX_VALUE() {
    delayTestFinish(TEST_DELAY);
    ValueTypesTestServiceAsync service = getServiceAsync();
    service.echo_MAX_VALUE(Byte.MAX_VALUE, new AsyncCallback() {

      public void onFailure(Throwable caught) {
        TestSetValidator.rethrowException(caught);
      }

      public void onSuccess(Object result) {
        assertNotNull(result);
        assertEquals(Byte.MAX_VALUE, ((Byte) result).byteValue());
        finishTest();
      }
    });
  }

  public void testByte_MIN_VALUE() {
    delayTestFinish(TEST_DELAY);
    ValueTypesTestServiceAsync service = getServiceAsync();
    service.echo_MIN_VALUE(Byte.MIN_VALUE, new AsyncCallback() {

      public void onFailure(Throwable caught) {
        TestSetValidator.rethrowException(caught);
      }

      public void onSuccess(Object result) {
        assertNotNull(result);
        assertEquals(Byte.MIN_VALUE, ((Byte) result).byteValue());
        finishTest();
      }
    });
  }

  public void testChar() {
    delayTestFinish(TEST_DELAY);
    ValueTypesTestServiceAsync service = getServiceAsync();
    service.echo((char) (Character.MAX_VALUE / (char) 2), new AsyncCallback() {

      public void onFailure(Throwable caught) {
        TestSetValidator.rethrowException(caught);
      }

      public void onSuccess(Object result) {
        assertNotNull(result);
        assertEquals((char) (Character.MAX_VALUE / (char) 2),
            ((Character) result).charValue());
        finishTest();
      }
    });
  }

  public void testChar_MAX_VALUE() {
    delayTestFinish(TEST_DELAY);
    ValueTypesTestServiceAsync service = getServiceAsync();
    service.echo_MAX_VALUE(Character.MAX_VALUE, new AsyncCallback() {

      public void onFailure(Throwable caught) {
        TestSetValidator.rethrowException(caught);
      }

      public void onSuccess(Object result) {
        assertNotNull(result);
        assertEquals(Character.MAX_VALUE, ((Character) result).charValue());
        finishTest();
      }
    });
  }

  public void testChar_MIN_VALUE() {
    delayTestFinish(TEST_DELAY);
    ValueTypesTestServiceAsync service = getServiceAsync();
    service.echo_MIN_VALUE(Character.MIN_VALUE, new AsyncCallback() {

      public void onFailure(Throwable caught) {
        TestSetValidator.rethrowException(caught);
      }

      public void onSuccess(Object result) {
        assertNotNull(result);
        assertEquals(Character.MIN_VALUE, ((Character) result).charValue());
        finishTest();
      }
    });
  }

  public void testDouble() {
    delayTestFinish(TEST_DELAY);
    ValueTypesTestServiceAsync service = getServiceAsync();
    service.echo(Double.MAX_VALUE / 2, new AsyncCallback() {

      public void onFailure(Throwable caught) {
        TestSetValidator.rethrowException(caught);
      }

      public void onSuccess(Object result) {
        assertNotNull(result);
        assertEquals(Double.MAX_VALUE / 2, ((Double) result).doubleValue(), 0.0);
        finishTest();
      }
    });
  }

  public void testDouble_MAX_VALUE() {
    delayTestFinish(TEST_DELAY);
    ValueTypesTestServiceAsync service = getServiceAsync();
    service.echo_MAX_VALUE(Double.MAX_VALUE, new AsyncCallback() {

      public void onFailure(Throwable caught) {
        TestSetValidator.rethrowException(caught);
      }

      public void onSuccess(Object result) {
        assertNotNull(result);
        assertEquals(Double.MAX_VALUE, ((Double) result).doubleValue(), 0.0);
        finishTest();
      }
    });
  }

  public void testDouble_MIN_VALUE() {
    delayTestFinish(TEST_DELAY);
    ValueTypesTestServiceAsync service = getServiceAsync();
    service.echo_MIN_VALUE(Double.MIN_VALUE, new AsyncCallback() {

      public void onFailure(Throwable caught) {
        TestSetValidator.rethrowException(caught);
      }

      public void onSuccess(Object result) {
        assertNotNull(result);
        assertEquals(Double.MIN_VALUE, ((Double) result).doubleValue(), 0.0);
        finishTest();
      }
    });
  }

  /**
   * Validate that NaNs (not-a-number, such as 0/0) propagate properly via RPC.
   */
  public void testDouble_NaN() {
    delayTestFinish(TEST_DELAY);
    ValueTypesTestServiceAsync service = getServiceAsync();
    service.echo(Double.NaN, new AsyncCallback() {

      public void onFailure(Throwable caught) {
        TestSetValidator.rethrowException(caught);
      }

      public void onSuccess(Object result) {
        assertNotNull(result);
        assertTrue(Double.isNaN(((Double) result).doubleValue()));
        finishTest();
      }
    });
  }

  /**
   * Validate that negative infinity propagates properly via RPC.
   */
  public void testDouble_NegInfinity() {
    delayTestFinish(TEST_DELAY);
    ValueTypesTestServiceAsync service = getServiceAsync();
    service.echo(Double.NEGATIVE_INFINITY, new AsyncCallback() {

      public void onFailure(Throwable caught) {
        TestSetValidator.rethrowException(caught);
      }

      public void onSuccess(Object result) {
        assertNotNull(result);
        double doubleValue = ((Double) result).doubleValue();
        assertTrue(Double.isInfinite(doubleValue) && doubleValue < 0);
        finishTest();
      }
    });
  }

  /**
   * Validate that positive infinity propagates properly via RPC.
   */
  public void testDouble_PosInfinity() {
    delayTestFinish(TEST_DELAY);
    ValueTypesTestServiceAsync service = getServiceAsync();
    service.echo(Double.POSITIVE_INFINITY, new AsyncCallback() {

      public void onFailure(Throwable caught) {
        TestSetValidator.rethrowException(caught);
      }

      public void onSuccess(Object result) {
        assertNotNull(result);
        double doubleValue = ((Double) result).doubleValue();
        assertTrue(Double.isInfinite(doubleValue) && doubleValue > 0);
        finishTest();
      }
    });
  }

  public void testFloat() {
    delayTestFinish(TEST_DELAY);
    ValueTypesTestServiceAsync service = getServiceAsync();
    service.echo(Float.MAX_VALUE / 2, new AsyncCallback() {

      public void onFailure(Throwable caught) {
        TestSetValidator.rethrowException(caught);
      }

      public void onSuccess(Object result) {
        assertNotNull(result);
        assertEquals(Float.MAX_VALUE / 2, ((Float) result).floatValue(), 0.0);
        finishTest();
      }
    });
  }

  public void testFloat_MAX_VALUE() {
    delayTestFinish(TEST_DELAY);
    ValueTypesTestServiceAsync service = getServiceAsync();
    service.echo_MAX_VALUE(Float.MAX_VALUE, new AsyncCallback() {

      public void onFailure(Throwable caught) {
        TestSetValidator.rethrowException(caught);
      }

      public void onSuccess(Object result) {
        assertNotNull(result);
        assertEquals(Float.MAX_VALUE, ((Float) result).floatValue(), 0.0);
        finishTest();
      }
    });
  }

  public void testFloat_MIN_VALUE() {
    delayTestFinish(TEST_DELAY);
    ValueTypesTestServiceAsync service = getServiceAsync();
    service.echo_MIN_VALUE(Float.MIN_VALUE, new AsyncCallback() {

      public void onFailure(Throwable caught) {
        TestSetValidator.rethrowException(caught);
      }

      public void onSuccess(Object result) {
        assertNotNull(result);
        assertEquals(Float.MIN_VALUE, ((Float) result).floatValue(), 0.0);
        finishTest();
      }
    });
  }

  /**
   * Validate that NaNs (not-a-number, such as 0/0) propagate properly via RPC.
   */
  public void testFloat_NaN() {
    delayTestFinish(TEST_DELAY);
    ValueTypesTestServiceAsync service = getServiceAsync();
    service.echo(Float.NaN, new AsyncCallback() {

      public void onFailure(Throwable caught) {
        TestSetValidator.rethrowException(caught);
      }

      public void onSuccess(Object result) {
        assertNotNull(result);
        assertTrue(Float.isNaN(((Float) result).floatValue()));
        finishTest();
      }
    });
  }

  /**
   * Validate that negative infinity propagates properly via RPC.
   */
  public void testFloat_NegInfinity() {
    delayTestFinish(TEST_DELAY);
    ValueTypesTestServiceAsync service = getServiceAsync();
    service.echo(Float.NEGATIVE_INFINITY, new AsyncCallback() {

      public void onFailure(Throwable caught) {
        TestSetValidator.rethrowException(caught);
      }

      public void onSuccess(Object result) {
        assertNotNull(result);
        float floatValue = ((Float) result).floatValue();
        assertTrue(Float.isInfinite(floatValue) && floatValue < 0);
        finishTest();
      }
    });
  }

  /**
   * Validate that positive infinity propagates properly via RPC.
   */
  public void testFloat_PosInfinity() {
    delayTestFinish(TEST_DELAY);
    ValueTypesTestServiceAsync service = getServiceAsync();
    service.echo(Float.POSITIVE_INFINITY, new AsyncCallback() {

      public void onFailure(Throwable caught) {
        TestSetValidator.rethrowException(caught);
      }

      public void onSuccess(Object result) {
        assertNotNull(result);
        float floatValue = ((Float) result).floatValue();
        assertTrue(Float.isInfinite(floatValue) && floatValue > 0);
        finishTest();
      }
    });
  }

  public void testInteger() {
    delayTestFinish(TEST_DELAY);
    ValueTypesTestServiceAsync service = getServiceAsync();
    service.echo(Integer.MAX_VALUE / 2, new AsyncCallback() {

      public void onFailure(Throwable caught) {
        TestSetValidator.rethrowException(caught);
      }

      public void onSuccess(Object result) {
        assertNotNull(result);
        assertEquals(Integer.MAX_VALUE / 2, ((Integer) result).intValue());
        finishTest();
      }
    });
  }

  public void testInteger_MAX_VALUE() {
    delayTestFinish(TEST_DELAY);
    ValueTypesTestServiceAsync service = getServiceAsync();
    service.echo_MAX_VALUE(Integer.MAX_VALUE, new AsyncCallback() {

      public void onFailure(Throwable caught) {
        TestSetValidator.rethrowException(caught);
      }

      public void onSuccess(Object result) {
        assertNotNull(result);
        assertEquals(Integer.MAX_VALUE, ((Integer) result).intValue());
        finishTest();
      }
    });
  }

  public void testInteger_MIN_VALUE() {
    delayTestFinish(TEST_DELAY);
    ValueTypesTestServiceAsync service = getServiceAsync();
    service.echo_MIN_VALUE(Integer.MIN_VALUE, new AsyncCallback() {

      public void onFailure(Throwable caught) {
        TestSetValidator.rethrowException(caught);
      }

      public void onSuccess(Object result) {
        assertNotNull(result);
        assertEquals(Integer.MIN_VALUE, ((Integer) result).intValue());
        finishTest();
      }
    });
  }

  public void testLong() {
    delayTestFinish(TEST_DELAY);
    ValueTypesTestServiceAsync service = getServiceAsync();
    service.echo(Long.MAX_VALUE / 2, new AsyncCallback() {

      public void onFailure(Throwable caught) {
        TestSetValidator.rethrowException(caught);
      }

      public void onSuccess(Object result) {
        assertNotNull(result);
        long expected = Long.MAX_VALUE / 2;
        assertEquals(expected, ((Long) result).longValue());
        finishTest();
      }
    });
  }

  public void testLong_MAX_VALUE() {
    delayTestFinish(TEST_DELAY);
    ValueTypesTestServiceAsync service = getServiceAsync();
    service.echo_MAX_VALUE(Long.MAX_VALUE, new AsyncCallback() {

      public void onFailure(Throwable caught) {
        TestSetValidator.rethrowException(caught);
      }

      public void onSuccess(Object result) {
        assertNotNull(result);
        assertEquals(Long.MAX_VALUE, ((Long) result).longValue());
        finishTest();
      }
    });
  }

  public void testLong_MIN_VALUE() {
    delayTestFinish(TEST_DELAY);
    ValueTypesTestServiceAsync service = getServiceAsync();
    service.echo_MIN_VALUE(Long.MIN_VALUE, new AsyncCallback() {

      public void onFailure(Throwable caught) {
        TestSetValidator.rethrowException(caught);
      }

      public void onSuccess(Object result) {
        assertNotNull(result);
        assertEquals(Long.MIN_VALUE, ((Long) result).longValue());
        finishTest();
      }
    });
  }

  public void testShort() {
    delayTestFinish(TEST_DELAY);
    ValueTypesTestServiceAsync service = getServiceAsync();
    service.echo((short) (Short.MAX_VALUE / (short) 2), new AsyncCallback() {

      public void onFailure(Throwable caught) {
        TestSetValidator.rethrowException(caught);
      }

      public void onSuccess(Object result) {
        assertNotNull(result);
        assertEquals(Short.MAX_VALUE / 2, ((Short) result).shortValue());
        finishTest();
      }
    });
  }

  public void testShort_MAX_VALUE() {
    delayTestFinish(TEST_DELAY);
    ValueTypesTestServiceAsync service = getServiceAsync();
    service.echo_MAX_VALUE(Short.MAX_VALUE, new AsyncCallback() {

      public void onFailure(Throwable caught) {
        TestSetValidator.rethrowException(caught);
      }

      public void onSuccess(Object result) {
        assertNotNull(result);
        assertEquals(Short.MAX_VALUE, ((Short) result).shortValue());
        finishTest();
      }
    });
  }

  public void testShort_MIN_VALUE() {
    delayTestFinish(TEST_DELAY);
    ValueTypesTestServiceAsync service = getServiceAsync();
    service.echo_MIN_VALUE(Short.MIN_VALUE, new AsyncCallback() {

      public void onFailure(Throwable caught) {
        TestSetValidator.rethrowException(caught);
      }

      public void onSuccess(Object result) {
        assertNotNull(result);
        assertEquals(Short.MIN_VALUE, ((Short) result).shortValue());
        finishTest();
      }
    });
  }

  private ValueTypesTestServiceAsync getServiceAsync() {
    if (primitiveTypeTestService == null) {
      primitiveTypeTestService = (ValueTypesTestServiceAsync) GWT.create(ValueTypesTestService.class);
      ((ServiceDefTarget) primitiveTypeTestService).setServiceEntryPoint(GWT.getModuleBaseURL()
          + "valuetypes");
    }
    return primitiveTypeTestService;
  }
}
