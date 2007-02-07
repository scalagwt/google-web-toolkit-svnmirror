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

import java.util.LinkedHashMap;

/**
 * A single node in the call graph tree. Aggregates the results of several
 * MethodInvokes.
 *
 */
public class Node {

  private Node parent;
  private LinkedHashMap children;
  private MethodInvoke data;

  public Node() {
  }

  public Node( Node parent, MethodInvoke data ) {
    this.parent = parent;
    this.data = data;
  }

  public Node add( MethodInvoke newInvoke ) {
    if ( children == null ) {
      children = new LinkedHashMap();
    }
    String invokeKey = newInvoke.getKey();
    Node matchingNode = (Node) children.get( invokeKey );

    // New existing invocation, keep as is
    if ( matchingNode == null ) {
      matchingNode = new Node( this, newInvoke );
      children.put( invokeKey, matchingNode );
    } else {
      // Collapse the method invocations into a single one
      MethodInvoke matchingInvoke = matchingNode.getData();
      matchingInvoke.numInvocations++;
      matchingInvoke.lastEntryTimeNanos = newInvoke.lastEntryTimeNanos;
    }

    return matchingNode;
  }

  public LinkedHashMap getChildren() {
    return children;
  }

  public MethodInvoke getData() {
    return data;
  }

  public Node getParent() {
    return parent;
  }
}
