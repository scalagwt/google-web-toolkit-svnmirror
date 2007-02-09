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
package com.google.gwt.dev.shell.profiler;

import com.google.gwt.core.ext.TreeLogger;

/**
 * Implements the public Profiler API. Receives profiling information gathered
 * during the execution of a GWT application and dispatches it to a
 * profiling Agent.
 *
 */
public class ProfilerImpl implements Profiler {

  // Potentially a list of agents in the future?
  private Agent agent;

  private TreeLogger logger;

  private long numTimingCalls;

  /**
   * Records the time at which a profiling call was first received,
   * before being dispatched to the profiler. Helps the profiler
   * account for any overhead involved in profiling.
   */
  private long profileCallBeginNanos;
  private long timingBeginNanos;
  private long timingOverheadNanos;

  public ProfilerImpl() {
  }

  // Should probably be JClassType
  // Maybe this signature should change to be a JavaScript object?
  // In general, what is our policy going to be on viewing live values
  // during execution time?
  public void exceptionCaught( String type ) {
    agent.exceptionCaught( type );
  }

  public void exceptionThrown( String type ) {
    agent.exceptionThrown( type );
  }

  public TreeLogger getTopLogger() {
    return logger;
  }

  // Do we need to know from what line of code these were made?
  // How do we tie HTTP requests and responses together?
  public void httpRequest( String requestUrl ) {
    agent.httpRequest( requestUrl );
  }

  public void httpResponse( String requestUrl ) {
    agent.httpResponse( requestUrl );
  }

  // These should be changed to accept JMethod
  // But first we need to make the TypeOracle generally accessible
  public void methodEntered( String klass, String name, String signature ) {
    long overhead = timingOverheadNanos;
    if ( profileCallBeginNanos != 0 ) {
      overhead += Timer.nanoTime() - profileCallBeginNanos;
    }
    agent.methodEntered( klass, name, signature, overhead );
    profileCallBeginNanos = 0;
  }

  public void methodExited( String klass, String name, String signature ) {
    long overhead = timingOverheadNanos;
    if ( profileCallBeginNanos != 0 ) {
      overhead = Timer.nanoTime() - profileCallBeginNanos;
    }
    agent.methodExited( klass, name, signature, overhead );
    profileCallBeginNanos = 0;
  }

  // Module events
  public void moduleLoadBegin( String name ) {
    agent.moduleLoadBegin( name );
  }

  public void moduleLoadEnd( String name ) {
    agent.moduleLoadEnd( name );
  }

  public void onAppLoad() {
    agent.onAppLoad();
  }

  public void register( int event, boolean enable, String filter ) {
  }

  // Do we need to know from what line of code these were made?
  // How do we tie RPC requests and responses together?
  public void rpcRequest( String klass, String name, String signature ) {
    agent.rpcRequest(klass, name, signature);
  }

  public void rpcResponse( String klass, String name, String signature ) {
    agent.rpcResponse(klass, name, signature);
  }

  public void setAgent( Agent agent ) {
    this.agent = agent;
  }

  public void setProfileCallBeginNanos( long nanos ) {
    profileCallBeginNanos = nanos;
  }

  public void setTopLogger( TreeLogger logger ) {
    this.logger = logger;
  }

  public void timingBegin() {
    timingBeginNanos = Timer.nanoTime();
    numTimingCalls = 0;
  }

  public void timingCall() {
    ++numTimingCalls;
  }

  public void timingEnd() {
    timingOverheadNanos = (Timer.nanoTime() - timingBeginNanos) / numTimingCalls;
  }
}
