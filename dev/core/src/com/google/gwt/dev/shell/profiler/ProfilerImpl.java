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

/**
 * Implements the public Profiler API. Receives profiling information gathered
 * during the execution of a GWT application and dispatches it to a
 * profiling Agent.
 *
 */
public class ProfilerImpl implements Profiler {

  // Potentially a list of agents in the future?
  private Agent agent;

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
    agent.methodEntered( klass, name, signature );
  }

  public void methodExited( String klass, String name, String signature ) {
    agent.methodExited( klass, name, signature );
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
}
