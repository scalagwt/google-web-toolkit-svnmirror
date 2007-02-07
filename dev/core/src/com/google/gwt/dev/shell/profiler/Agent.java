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
 * A profiling agent. Registers with the Profiler to receive profiling events.
 *
 */
public interface Agent {

  // Should probably be JClassType
  // Maybe this signature should change to be a JavaScript object?
  // In general, what is our policy going to be on viewing live values
  // during execution time?
  void exceptionThrown( String type );
  void exceptionCaught( String type );

  // Do we need to know from what line of code these were made?
  // How do we tie HTTP requests and responses together?
  void httpRequest( String requestUrl );
  void httpResponse( String requestUrl );

  // These should be changed to accept JMethod
  // But first we need to make the TypeOracle generally accessible

  /**
   * Called when a method has been entered.
   *
   * @param klass The name of the class.
   * @param name The name of the method.
   * @param signature The type signature of the method.
   */
  void methodEntered( String klass, String name, String signature );

  /**
   * Called when a method has been exited, normally or by exception.
   *
   * @param klass The name of the class.
   * @param name The name of the method.
   * @param signature The type signature of the method.
   */
  void methodExited( String klass, String name, String signature );

  void moduleLoadBegin( String name );
  void moduleLoadEnd( String name );

  /**
   * Called upon application load. This gives the Agent the chance to
   * cleanly initialize any data structures necessary for profiling
   * before application execution. (For example, the Agent may have
   * left-over data hanging around from a previous profile run).
   */
  void onAppLoad();

  /**
   * Called before a profiling session is about to begin. This gives
   * the Agent the chance to initialize profiler settings, such as filters,
   * before the application is compiled.
   *
   * @param profiler
   */
  void onLoad( Profiler profiler );
    // We might pass a TypeOracle in here, or make it available from the Profiler.
    // We might need to pass in some general module information.

  // Do we need to know from what line of code these were made?
  // How do we tie RPC requests and responses together?
  void rpcRequest( String klass, String name, String signature );
  void rpcResponse( String klass, String name, String signature );
}
