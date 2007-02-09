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
import com.google.gwt.core.ext.TreeLogger;

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.ArrayList;
import java.text.NumberFormat;

/**
 * An out of the box profiling agent that should meet most GWT users' needs.
 * Provides profiling information on the full gamut of events.
 */
public class ProfilingAgent implements Agent {

  // To make method timings as precise as possible, I've included overhead
  // accounts from
  //   a) JSNI to BrowserWidget dispatch
  //   b) BrowserWidget to agent dispatch
  //   c) general profiling accounting
  //
  // I've put comment markers around overhead that I don't account for, and
  // which I believe should generally be so low that it shouldn't affect
  // the results (e.g. < 1 microsecond). Anyone maintaining this class
  // must be careful to not introduce unaccounted overhead of any
  // significance.

  Node currentContext;

  Node currentRoot;

  Map moduleEntries;

  Profiler profiler;

  ArrayList roots;

  public ProfilingAgent() {
  }

  public void exceptionCaught(String type) {
  }

  public void exceptionThrown(String type) {
  }

  public void httpRequest(String requestUrl) {
  }

  public void httpResponse(String requestUrl) {
  }

  public void methodEntered(String klass, String name, String signature,
      long overhead) {
    long entryTime = Timer.nanoTime();
    MethodInvoke invoke = new MethodInvoke(klass, name, signature, entryTime,
        overhead);

    // Begin unaccounted overhead
    if (currentRoot == null) {
      currentContext = currentRoot = new Node(null, invoke);
      roots.add(currentRoot);
    } else {
      currentContext = currentContext.add(invoke);
    }
    // End unaccounted overhead
  }

  public void methodExited(String klass, String name, String signature,
      long overhead) {

    // Begin unaccounted overhead
    MethodInvoke invoke = currentContext.getData();
    Node parent = currentContext.getParent();
    // End unaccounted overhead

    long exitTime = Timer.nanoTime();
    long absTime = Math.abs(
        exitTime - invoke.lastEntryTimeNanos - invoke.overheadNanos - overhead);
    invoke.aggregateExecutionTimeNanos += absTime;

    if (parent == null) {
      // safe to do this hard work outside of timer overhead measurement,
      // because the next entry time is going to be from a new root.
      dumpStatistics(currentRoot);
      currentRoot = null;
    }

    // Begin unaccounted overhead
    currentContext = parent;
    // End unaccounted overhead
  }

  public void moduleLoadBegin(String name) {
    moduleEntries.put(name, new Long(Timer.nanoTime()));
  }

  public void moduleLoadEnd(String name) {
    double exitTime = Timer.nanoTime();
    double enterTime = ((Long) moduleEntries.remove(name)).longValue();
    double totalTime = (exitTime - enterTime) / 1000000.0;
    System.out.println(
        "Time spent loading module " + name + ": " + totalTime + "(ms)");
  }

  public void onAppLoad() {
    roots = new ArrayList();
    moduleEntries = new HashMap();
  }

  public void onLoad(Profiler profiler) {
    this.profiler = profiler;
    // register for events, filter, blah blah, blah
  }

  // Do we need to know from what line of code these were made?
  // How do we tie RPC requests and responses together?
  public void rpcRequest(String klass, String name, String signature) {
  }

  public void rpcResponse(String klass, String name, String signature) {
  }

  private void dumpStatistics(Node callGraph) {

    // We could also remove callGraph from the root to save memory,
    // but that may not work so well for real-time statistics

    // Print out total accumulated time for all methods.
    // May have to visit the graph to determine what the total elapsed time is

    TreeLogger logger = profiler.getTopLogger();

    NumberFormat format = NumberFormat.getInstance();
    format.setMaximumFractionDigits(1);

    fixupOverhead(callGraph);
    printStats(callGraph, 0, callGraph.getData().aggregateExecutionTimeNanos,
        format, logger);
  }

  // Corrects for overhead throughout the entire graph by adding up overhead
  // from the bottom up, all the way to <code>node</code>.
  private long fixupOverhead(Node node) {
    Map children = node.getChildren();

    long totalOverhead = 0;

    if (children != null) {
      for (Iterator it = children.values().iterator(); it.hasNext();) {
        totalOverhead += fixupOverhead((Node) it.next());
      }
    }

    MethodInvoke invoke = node.getData();
    totalOverhead += invoke.overheadNanos;
    invoke.aggregateExecutionTimeNanos -= totalOverhead;

    return totalOverhead;
  }

  private void printStats(Node node, int indentLevel, long totalGraphTime,
      NumberFormat format, TreeLogger logger) {

    MethodInvoke invoke = node.getData();
    double exeTimeDouble = invoke.aggregateExecutionTimeNanos;
    double totalTimeDouble = totalGraphTime;
    double percentage = (exeTimeDouble / totalTimeDouble) * 100;

    for (int i = 0; i < indentLevel; ++i) {
      System.out.print("  ");
    }
    System.out.print("- ");

    String msg =
        format.format(percentage) + "% - " +
        format.format(exeTimeDouble / 1000000) + "(ms) - " +
        invoke.numInvocations + " calls " +
        invoke.klass + "." + invoke.name;

    System.out.println(msg);
    TreeLogger branchedLogger = logger.branch(TreeLogger.DEBUG,msg,null);

    indentLevel++;

    Map children = node.getChildren();

    if (children != null) {
      for (Iterator it = children.values().iterator(); it.hasNext();) {
        printStats((Node) it.next(), indentLevel, totalGraphTime, format, branchedLogger);
      }
    }
  }
}
