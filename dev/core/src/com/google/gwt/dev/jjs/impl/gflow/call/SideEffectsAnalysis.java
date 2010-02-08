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

import com.google.gwt.dev.jjs.ast.Context;
import com.google.gwt.dev.jjs.ast.JBinaryOperation;
import com.google.gwt.dev.jjs.ast.JDeclarationStatement;
import com.google.gwt.dev.jjs.ast.JExpression;
import com.google.gwt.dev.jjs.ast.JFieldRef;
import com.google.gwt.dev.jjs.ast.JGwtCreate;
import com.google.gwt.dev.jjs.ast.JLocalRef;
import com.google.gwt.dev.jjs.ast.JMethod;
import com.google.gwt.dev.jjs.ast.JMethodCall;
import com.google.gwt.dev.jjs.ast.JParameterRef;
import com.google.gwt.dev.jjs.ast.JThrowStatement;
import com.google.gwt.dev.jjs.ast.JUnaryOperation;
import com.google.gwt.dev.jjs.ast.JVisitor;
import com.google.gwt.dev.jjs.ast.js.JsniMethodRef;
import com.google.gwt.dev.jjs.impl.gflow.Analysis;
import com.google.gwt.dev.jjs.impl.gflow.Assumption;
import com.google.gwt.dev.jjs.impl.gflow.AssumptionMap;
import com.google.gwt.dev.jjs.impl.gflow.AssumptionUtil;
import com.google.gwt.dev.jjs.impl.gflow.FlowFunction;
import com.google.gwt.dev.jjs.impl.gflow.call.CallGraph.Edge;

import java.util.HashSet;
import java.util.Set;

/**
 * 
 */
public class SideEffectsAnalysis implements Analysis<JMethod, CallGraph.Edge, CallGraph, SideEffectsAnalysis.SideEffectsAssumption> {
  /**
   * 
   */
  public static class SideEffectsAssumption implements Assumption<SideEffectsAssumption> {
    private boolean value;

    public SideEffectsAssumption(boolean value) {
      this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      SideEffectsAssumption other = (SideEffectsAssumption) obj;
      if (value != other.value) {
        return false;
      }
      return true;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + (value ? 1231 : 1237);
      return result;
    }

    public boolean hasSideEffects() {
      return value;
    }

    public SideEffectsAssumption join(SideEffectsAssumption other) {
      if (value || other == null) {
        return this;
      }
      
      if (other.value) {
        return new SideEffectsAssumption(other.value);
      }
      
      return this;
    }

    @Override
    public String toString() {
      return value ? "T" : "";
    }
  }

  private final class PreprocessVisitor extends JVisitor {
    private boolean hasSideEffects = false;

    @Override
    public void endVisit(JBinaryOperation x, Context ctx) {
      if (!x.isAssignment()) {
        return;
      }
      
      if (!isPartOfGlobalState(x.getLhs())) {
        return;
      }
      
      hasSideEffects = true;
    }

    @Override
    public void endVisit(JDeclarationStatement x, Context ctx) {
      hasSideEffects |= isPartOfGlobalState(x.getVariableRef());
    }

    @Override
    public void endVisit(JFieldRef x, Context ctx) {
      hasSideEffects |= x.hasClinit();
    }

    @Override
    public void endVisit(JGwtCreate x, Context ctx) {
      hasSideEffects = true;
    }

    @Override
    public void endVisit(JMethodCall x, Context ctx) {
      hasSideEffects |= x.getTarget().isNative(); 
    }

    @Override
    public void endVisit(JMethod x, Context ctx) {
      if (hasSideEffects) {
        methodsWithSideEffects.add(x);
      }
    }

    @Override
    public void endVisit(JsniMethodRef x, Context ctx) {
      hasSideEffects = true;
    }

    @Override
    public void endVisit(JThrowStatement x, Context ctx) {
      hasSideEffects = true;
    }

    @Override
    public void endVisit(JUnaryOperation x, Context ctx) {
      hasSideEffects |= (x.getOp().isModifying() && 
          isPartOfGlobalState(x.getArg()));
    }

    @Override
    public boolean visit(JMethod x, Context ctx) {
      hasSideEffects = x.isNative();
      return !hasSideEffects;
    }

    private boolean isPartOfGlobalState(JExpression expr) {
      if (expr instanceof JLocalRef ||
          expr instanceof JParameterRef) {
        return false;
      }
      return true;
    }
  }
  
  private Set<JMethod> methodsWithSideEffects = new HashSet<JMethod>();

  public FlowFunction<JMethod, CallGraph.Edge, CallGraph, SideEffectsAssumption> getFlowFunction() {
    return new FlowFunction<JMethod, CallGraph.Edge, CallGraph, SideEffectsAssumption>() {
      public void interpret(JMethod node, CallGraph g,
          AssumptionMap<Edge, SideEffectsAssumption> assumptionMap) {
        SideEffectsAssumption assumption;
        if (methodsWithSideEffects.contains(node)) {
          assumption = new SideEffectsAssumption(true);
        } else {
          assumption = AssumptionUtil.join(g.getGraphOutEdges(), assumptionMap);
        }
        AssumptionUtil.setAssumptions(g.getGraphInEdges(), assumption, assumptionMap);
      }
    };
  }

  public Set<JMethod> getMethodsWithSideEffects() {
    return methodsWithSideEffects;
  }

  public void preprocess(CallGraph graph) {
    new PreprocessVisitor().accept(graph.getNodes());
  }

  public void setInitialGraphAssumptions(CallGraph graph,
      AssumptionMap<Edge, SideEffectsAssumption> assumptionMap) {
    // bottom assumptions
  }
}
