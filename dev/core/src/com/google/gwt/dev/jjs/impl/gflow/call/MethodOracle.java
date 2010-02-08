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

import com.google.gwt.dev.jjs.ast.JMethodCall;
import com.google.gwt.dev.jjs.impl.gflow.call.CallGraph.Edge;
import com.google.gwt.dev.jjs.impl.gflow.call.SideEffectsAnalysis.SideEffectsAssumption;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class MethodOracle  implements Serializable {
  private Set<JMethodCall> callsWithoutSideEffects = new HashSet<JMethodCall>();
  private Set<JMethodCall> callsWithSideEffects = new HashSet<JMethodCall>();

  public boolean hasSideEffects(JMethodCall call) {
    return callsWithSideEffects.contains(call) || 
        !callsWithoutSideEffects.contains(call);
  }

  public void process(Map<Edge, SideEffectsAssumption> solution) {
    for (Edge e : solution.keySet()) {
      SideEffectsAssumption assumption = solution.get(e);
      
      if (assumption.hasSideEffects()) {
        callsWithSideEffects.add(e.getCall());
      } else {
        callsWithoutSideEffects.add(e.getCall());
      }
    }
  }

 
}
