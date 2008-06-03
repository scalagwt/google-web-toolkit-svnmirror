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
package com.google.gwt.junit.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
import com.google.gwt.junit.client.impl.ExceptionWrapper;
import com.google.gwt.junit.client.impl.GWTRunner;
import com.google.gwt.junit.client.impl.JUnitResult;
import com.google.gwt.user.client.Timer;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

/**
 * The translatable implementation of {@link GWTTestCase}.
 */
public abstract class GWTTestCase extends TestCase {

  /**
   * A watchdog class for use with asynchronous mode. On construction,
   * immediately schedules itself for the specified timeout. If the timeout
   * expires before this timer is canceled, causes the enclosing test case to
   * fail with {@link TimeoutException}.
   */
  private final class KillTimer extends Timer {

    /**
     * Stashed so the timeout can be reported via {@link TimeoutException}.
     */
    private final int timeoutMillis;

    public KillTimer(int timeoutMillis) {
      this.timeoutMillis = timeoutMillis;
      schedule(timeoutMillis);
    }

    public void run() {
      if (timer == this) {
        // The test has failed due to timeout
        reportResultsAndRunNextMethod(new TimeoutException(timeoutMillis));
      } else {
        // Something happened so that we are no longer the active timer.
        // Just do nothing.
      }
    }
  }

  /**
   * The collected checkpoint messages.
   */
  private List<String> checkPoints;

  /**
   * Tracks whether the main test body has run (for asynchronous mode).
   */
  private boolean mainTestHasRun = false;

  /**
   * Test result.
   */
  private JUnitResult result;

  /**
   * Tracks whether this test is completely done.
   */
  private boolean testIsFinished = false;

  /**
   * If non-null, a timer to kill the current test case (for asynchronous mode).
   */
  private KillTimer timer;

  private final UncaughtExceptionHandler uncaughtHandler = new UncaughtExceptionHandler() {
    /**
     * An uncaught exception escaped to the browser; what we should do depends
     * on what state we're in.
     */
    public void onUncaughtException(Throwable ex) {
      if (mainTestHasRun && timer != null) {
        // Asynchronous mode; uncaught exceptions cause an immediate failure.
        assert (!testIsFinished);
        reportResultsAndRunNextMethod(ex);
      } else {
        // just ignore it
      }
    }
  };

  // CHECKSTYLE_OFF
  /**
   * Actually run the user's test. Called from {@link GWTRunner}.
   */
  public void __doRunTest() {
    Throwable caught = null;

    if (shouldCatchExceptions()) {
      // Make sure no exceptions escape
      GWT.setUncaughtExceptionHandler(uncaughtHandler);
      try {
        runBare();
      } catch (Throwable e) {
        caught = e;
      }
    } else {
      // Special; make sure all exceptions escape to the browser (for debugging)
      GWT.setUncaughtExceptionHandler(null);
      runBareTestCaseAvoidingExceptionDecl();
    }

    // Mark that the main test body has now run. From this point, if
    // timer != null we are in true asynchronous mode.
    mainTestHasRun = true;

    if (caught != null) {
      // Test failed; finish test no matter what state we're in.
      reportResultsAndRunNextMethod(caught);
    } else if (timer != null) {
      // Test is still running; wait for asynchronous completion.
    } else {
      // Test is really done; report success.
      reportResultsAndRunNextMethod(null);
    }
  }
  // CHECKSTYLE_ON

  /**
   * Implementation of {@link GWTTestCase#addCheckpoint(String)}.
   */
  public void addCheckpoint(String msg) {
    if (checkPoints == null) {
      checkPoints = new ArrayList<String>();
    }
    checkPoints.add(msg);
  }

  public boolean catchExceptions() {
    return true;
  }

  public void clearCheckpoints() {
    checkPoints = null;
  }

  public String[] getCheckpoints() {
    if (checkPoints == null) {
      return new String[0];
    } else {
      int len = checkPoints.size();
      String[] retval = new String[len];
      for (int i = 0; i < len; ++i) {
        retval[i] = checkPoints.get(i);
      }
      return retval;
    }
  }

  public abstract String getModuleName();

  // CHECKSTYLE_OFF
  protected JUnitResult __getOrCreateTestResult() {
    if (result == null) {
      result = new JUnitResult();
    }
    return result;
  }
  // CHECKSTYLE_ON

  protected final void delayTestFinish(int timeoutMillis) {
    if (supportsAsync()) {
      if (timer != null) {
        // Cancel the pending timer
        timer.cancel();
      }

      // Set a new timer for the specified new timeout
      timer = new KillTimer(timeoutMillis);
    } else {
      throw new UnsupportedOperationException(
          "This test case does not support asynchronous mode.");
    }
  }

  protected final void finishTest() {
    if (supportsAsync()) {
      if (testIsFinished) {
        // This test is totally done already, just ignore the call.
        return;
      }

      if (timer == null) {
        throw new IllegalStateException(
            "This test is not in asynchronous mode; call delayTestFinish() first");
      }

      if (mainTestHasRun) {
        // This is a correct, successful async finish.
        reportResultsAndRunNextMethod(null);
      } else {
        // The user tried to finish the test before the main body returned!
        // Just let the test continue running normally.
        resetAsyncState();
      }
    } else {
      throw new UnsupportedOperationException(
          "This test case does not support asynchronous mode.");
    }
  }

  protected void gwtSetUp() throws Exception {
  }

  protected void gwtTearDown() throws Exception {
  }

  @Override
  protected final void setUp() throws Exception {
    gwtSetUp();
  }

  protected boolean supportsAsync() {
    return true;
  }

  @Override
  protected final void tearDown() throws Exception {
    gwtTearDown();
  }

  /**
   * Cleans up any outstanding state, reports ex to the remote runner, and kicks
   * off the next test.
   * 
   * @param ex The results of this test.
   */
  private void reportResultsAndRunNextMethod(Throwable ex) {
    JUnitResult myResult = __getOrCreateTestResult();
    if (ex != null) {
      ExceptionWrapper ew = new ExceptionWrapper(ex);
      if (checkPoints != null) {
        for (int i = 0, c = checkPoints.size(); i < c; ++i) {
          ew.message += "\n" + checkPoints.get(i);
        }
      }
      myResult.setExceptionWrapper(ew);
    }

    testIsFinished = true;
    resetAsyncState();
    GWTRunner.get().reportResultsAndGetNextMethod(myResult);
  }

  /**
   * Cleans up any asynchronous mode state.
   */
  private void resetAsyncState() {
    // clear our timer if there is one
    if (timer != null) {
      timer.cancel();
      timer = null;
    }
  }

  /**
   * In the mode where we need to let uncaught exceptions escape to the browser,
   * this method serves as a hack to avoid "throws" clause problems.
   */
  private native void runBareTestCaseAvoidingExceptionDecl() /*-{
    this.@junit.framework.TestCase::runBare()();
  }-*/;

  /**
   * A helper method to determine if we should catch exceptions. Wraps the call
   * into user code with a try/catch; if the user's code throws an exception, we
   * just ignore the exception and use the default behavior.
   * 
   * @return <code>true</code> if exceptions should be handled normally,
   *         <code>false</code> if they should be allowed to escape.
   */
  private boolean shouldCatchExceptions() {
    try {
      return catchExceptions();
    } catch (Throwable e) {
      return true;
    }
  }
}
