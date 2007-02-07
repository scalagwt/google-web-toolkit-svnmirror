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
package com.google.gwt.dev.shell.profiler.agent;

import com.google.gwt.dev.shell.profiler.Agent;
import com.google.gwt.dev.shell.profiler.Profiler;
import com.google.gwt.dev.shell.profiler.Timer;

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.ArrayList;
import java.text.NumberFormat;

/**
 * An out of the box profiling agent that should meet most GWT users' needs.
 * Provides profiling information on the full gamut of events.
 *
 */
public class ProfilingAgent implements Agent {

  Profiler profiler;

  ArrayList roots;
  Node currentRoot;
  Node currentContext;
  Map moduleEntries;

  public ProfilingAgent() {
  }

  public void exceptionCaught(String type) {
  }

  // Should probably be JClassType
  // Maybe this signature should change to be a JavaScript object?
  // In general, what is our policy going to be on viewing live values
  // during execution time?
  public void exceptionThrown(String type) {
  }

  // Do we need to know from what line of code these were made?
  // How do we tie HTTP requests and responses together?
  public void httpRequest(String requestUrl) {
  }

  public void httpResponse(String requestUrl) {
  }

  public void methodEntered(String klass, String name, String signature) {
    long entryTime = Timer.nanoTime();
    MethodInvoke invoke = new MethodInvoke( klass, name, signature, entryTime );
    if ( currentRoot == null ) {
      currentContext = currentRoot = new Node( null, invoke );
      roots.add( currentRoot );
    } else {
      currentContext = currentContext.add( invoke );
    }
  }

  public void methodExited(String klass, String name, String signature) {
    long exitTime = Timer.nanoTime();
    MethodInvoke invoke = currentContext.getData();
    invoke.aggregateExecutionTimeNanos += exitTime - invoke.lastEntryTimeNanos;
    Node parent = currentContext.getParent();

    if ( parent == null ) {
      dumpStatistics( currentRoot );
      currentRoot = null;
    }

    currentContext = parent;
  }

  public void moduleLoadBegin(String name) {
    moduleEntries.put( name, new Long( Timer.nanoTime() ) );
  }

  public void moduleLoadEnd(String name) {
    double exitTime = Timer.nanoTime();
    double enterTime = ((Long) moduleEntries.remove( name )).longValue();
    double totalTime = (exitTime - enterTime) / 1000000.0;
    System.out.println( "Time spent loading module " + name + ": " + totalTime + "(ms)");
  }

  public void onAppLoad() {
    roots = new ArrayList();
    moduleEntries = new HashMap();
  }

  public void onLoad( Profiler profiler ) {
    this.profiler = profiler;
    // register for events, filter, blah blah, blah
  }

  // Do we need to know from what line of code these were made?
  // How do we tie RPC requests and responses together?
  public void rpcRequest(String klass, String name, String signature) {
  }

  public void rpcResponse(String klass, String name, String signature) {
  }

  private void dumpStatistics( Node callGraph ) {

    // We could also remove callGraph from the root to save memory,
    // but that may not work so well for real-time statistics

    // Print out total accumulated time for all methods.
    // May have to visit the graph to determine what the total elapsed time is

    NumberFormat format = NumberFormat.getInstance();
    format.setMaximumFractionDigits( 1 );

    printStats( callGraph, 0, callGraph.getData().aggregateExecutionTimeNanos, format );
  }

  private void printStats( Node node, int indentLevel, long totalGraphTime, NumberFormat format ) {

    MethodInvoke invoke = node.getData();
    double exeTimeDouble = invoke.aggregateExecutionTimeNanos;
    double totalTimeDouble = totalGraphTime;
    double percentage = ( exeTimeDouble / totalTimeDouble ) * 100;

    for ( int i = 0; i < indentLevel; ++i ) {
      System.out.print( "  " );
    }
    System.out.print( "- " );

    System.out.println(
        format.format( percentage ) + "% - " +
        format.format( exeTimeDouble / 1000000 ) + "(ms) - " +
        invoke.numInvocations + " calls " +
        invoke.klass + "." + invoke.name );

    indentLevel++;

    Map children = node.getChildren();

    if ( children != null ) {
      for ( Iterator it = children.values().iterator(); it.hasNext(); ) {
        printStats( (Node) it.next(), indentLevel, totalGraphTime, format );
      }
    }
  }
}
