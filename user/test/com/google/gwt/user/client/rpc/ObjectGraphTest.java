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
package com.google.gwt.user.client.rpc;

import com.google.gwt.core.client.GWT;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.rpc.TestSetFactory.SerializableDoublyLinkedNode;

/**
 * TODO: document me.
 */
public class ObjectGraphTest extends GWTTestCase {
  private static final int TEST_DELAY = 5000;

  public String getModuleName() {
    return "com.google.gwt.user.RPCSuite";
  }

  public void testAcyclicGraph() {
    delayTestFinish(TEST_DELAY);

    ObjectGraphTestServiceAsync service = getServiceAsync();
    service.echo_AcyclicGraph(TestSetFactory.createAcyclicGraph(),
        new AsyncCallback() {
          public void onFailure(Throwable caught) {
            TestSetValidator.rethrowException(caught);
          }

          public void onSuccess(Object result) {
            assertNotNull(result);
            assertTrue(TestSetValidator.isValidAcyclicGraph((SerializableDoublyLinkedNode) result));
            finishTest();
          }
        });
  }

  public void testComplexCyclicGraph() {
    delayTestFinish(TEST_DELAY);

    ObjectGraphTestServiceAsync service = getServiceAsync();
    service.echo_ComplexCyclicGraph(TestSetFactory.createComplexCyclicGraph(),
        new AsyncCallback() {
          public void onFailure(Throwable caught) {
            TestSetValidator.rethrowException(caught);
          }

          public void onSuccess(Object result) {
            assertNotNull(result);
            assertTrue(TestSetValidator.isValidComplexCyclicGraph((SerializableDoublyLinkedNode) result));
            finishTest();
          }
        });
  }

  public void testComplexCyclicGraph2() {
    delayTestFinish(TEST_DELAY);

    ObjectGraphTestServiceAsync service = getServiceAsync();
    final SerializableDoublyLinkedNode node = TestSetFactory.createComplexCyclicGraph();
    service.echo_ComplexCyclicGraph(node, node, new AsyncCallback() {
      public void onFailure(Throwable caught) {
        TestSetValidator.rethrowException(caught);
      }

      public void onSuccess(Object result) {
        assertNotNull(result);
        assertTrue(TestSetValidator.isValidComplexCyclicGraph((SerializableDoublyLinkedNode) result));
        finishTest();
      }
    });
  }

  public void testTrivialCyclicGraph() {
    delayTestFinish(TEST_DELAY);

    ObjectGraphTestServiceAsync service = getServiceAsync();
    service.echo_TrivialCyclicGraph(TestSetFactory.createTrivialCyclicGraph(),
        new AsyncCallback() {
          public void onFailure(Throwable caught) {
            TestSetValidator.rethrowException(caught);
          }

          public void onSuccess(Object result) {
            assertNotNull(result);
            assertTrue(TestSetValidator.isValidTrivialCyclicGraph((SerializableDoublyLinkedNode) result));
            finishTest();
          }
        });
  }

  private ObjectGraphTestServiceAsync getServiceAsync() {
    if (objectGraphTestService == null) {
      objectGraphTestService = (ObjectGraphTestServiceAsync) GWT.create(ObjectGraphTestService.class);
      ((ServiceDefTarget) objectGraphTestService).setServiceEntryPoint(GWT.getModuleBaseURL()
          + "objectgraphs");
    }
    return objectGraphTestService;
  }

  private ObjectGraphTestServiceAsync objectGraphTestService;
}
