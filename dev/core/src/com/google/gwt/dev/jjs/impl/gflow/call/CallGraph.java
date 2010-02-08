/*
 * Copyright 2009 Google Inc.
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
package com.google.gwt.dev.jjs.impl.gflow.call;

import com.google.gwt.dev.jjs.ast.JMethod;
import com.google.gwt.dev.jjs.ast.JMethodCall;
import com.google.gwt.dev.jjs.impl.gflow.Assumption;
import com.google.gwt.dev.jjs.impl.gflow.Graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 */
public class CallGraph implements
    Graph<JMethod, CallGraph.Edge, CallGraph.CallGraphTransformer> {
  /**
   */
  public static class CallGraphTransformer {
    // not supported
  }

  /**
   */
  public static class Edge {
    public Object data;
    private final JMethodCall call;
    private final JMethod from;
    private final JMethod to;

    public Edge(JMethod from, JMethod to, JMethodCall call) {
      this.from = from;
      this.to = to;
      this.call = call;
    }

    public JMethodCall getCall() {
      return call;
    }

    public JMethod getFrom() {
      return from;
    }

    public JMethod getTo() {
      return to;
    }

    @Override
    public String toString() {
      return from.getName() + "->" + to.getName();
    }
  }
  
  private final Map<JMethod, ArrayList<Edge>> inEdges = new HashMap<JMethod, ArrayList<Edge>>();
  private final ArrayList<JMethod> nodes = new ArrayList<JMethod>();
  private final Map<JMethod, ArrayList<Edge>> outEdges = new HashMap<JMethod, ArrayList<Edge>>(); 

  public CallGraphBuilder createBuilder() {
    return new CallGraphBuilder() {
      public void addCall(JMethod containingMethod, 
          JMethodCall call, JMethod target) {
        addEdge(containingMethod, target, call);
      }

      public void addMethod(JMethod method) {
        nodes.add(method);
      }
    };
  }

  public Object getEdgeData(Edge edge) {
    return edge.data;
  }

  public JMethod getEnd(Edge edge) {
    return edge.to;
  }

  public ArrayList<Edge> getGraphInEdges() {
    // todo: add entry points
    return new ArrayList<Edge>(0);
  }

  public ArrayList<Edge> getGraphOutEdges() {
    return new ArrayList<Edge>(0);
  }

  public ArrayList<Edge> getInEdges(JMethod n) {
    return getMultiMap(inEdges, n);
  }

  public ArrayList<JMethod> getNodes() {
    return nodes;
  }

  public ArrayList<Edge> getOutEdges(JMethod node) {
    return getMultiMap(outEdges, node);
  }

  public JMethod getStart(Edge edge) {
    return edge.from;
  }

  public String print() {
    throw new UnsupportedOperationException();
  }

  public <A extends Assumption<A>> String printWithAssumptions(
      Map<Edge, A> assumptions) {
    throw new UnsupportedOperationException();
  }

  public void setEdgeData(Edge edge, Object data) {
    edge.data = data;
  }

  public boolean transform(JMethod node, CallGraphTransformer transformer) {
    throw new UnsupportedOperationException();
  }
  
  protected void addEdge(JMethod from, JMethod to,  JMethodCall call) {
    Edge edge = new Edge(from, to, call);
    addToMultiMap(outEdges, from, edge);
    addToMultiMap(inEdges, to, edge);
  }

  private void addToMultiMap(Map<JMethod, ArrayList<Edge>> map,
      JMethod node, Edge edge) {
    ArrayList<Edge> list = map.get(node);
    if (list == null) {
      list = new ArrayList<Edge>();
      map.put(node, list);
    }
    list.add(edge);
  }

  private ArrayList<Edge> getMultiMap(Map<JMethod, ArrayList<Edge>> map, JMethod node) {
    ArrayList<Edge> list = map.get(node);
    return list != null ? list : new ArrayList<Edge>();
  }
}
