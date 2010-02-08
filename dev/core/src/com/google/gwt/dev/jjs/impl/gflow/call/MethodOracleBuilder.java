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

import com.google.gwt.dev.jjs.ast.JProgram;
import com.google.gwt.dev.jjs.impl.gflow.AnalysisSolver;
import com.google.gwt.dev.jjs.impl.gflow.call.SideEffectsAnalysis.SideEffectsAssumption;
import com.google.gwt.dev.util.PerfLogger;

import java.util.Map;

/**
 *
 */
public class MethodOracleBuilder {
  public static MethodOracle buildMethodOracle(JProgram program) {
    PerfLogger.start("MethodOracleBuilder");
    MethodOracle oracle = new MethodOracle();

    SideEffectsAnalysis analysis = new SideEffectsAnalysis();
    CallGraph graph = new CallGraph();
    CallGraphGenerator.generate(program, graph.createBuilder());
    
    analysis.preprocess(graph);
    Map<CallGraph.Edge, SideEffectsAssumption> solution = 
      AnalysisSolver.solve(graph, analysis, true);
    
    oracle.process(solution);
    
    PerfLogger.end();
    return oracle;
  }
}
