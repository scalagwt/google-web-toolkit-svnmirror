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
package com.google.gwt.dev.jjs.impl.gflow;

import com.google.gwt.dev.jjs.ast.Context;
import com.google.gwt.dev.jjs.ast.JDeclaredType;
import com.google.gwt.dev.jjs.ast.JMethod;
import com.google.gwt.dev.jjs.ast.JMethodBody;
import com.google.gwt.dev.jjs.ast.JModVisitor;
import com.google.gwt.dev.jjs.ast.JNode;
import com.google.gwt.dev.jjs.ast.JProgram;
import com.google.gwt.dev.jjs.impl.DeadCodeElimination;
import com.google.gwt.dev.jjs.impl.gflow.cfg.Cfg;
import com.google.gwt.dev.jjs.impl.gflow.cfg.CfgBuilder;
import com.google.gwt.dev.jjs.impl.gflow.cfg.CfgEdge;
import com.google.gwt.dev.jjs.impl.gflow.cfg.CfgNode;
import com.google.gwt.dev.jjs.impl.gflow.cfg.CfgTransformer;
import com.google.gwt.dev.jjs.impl.gflow.constants.ConstantsAnalysis;
import com.google.gwt.dev.jjs.impl.gflow.copy.CopyAnalysis;
import com.google.gwt.dev.jjs.impl.gflow.liveness.LivenessAnalysis;
import com.google.gwt.dev.jjs.impl.gflow.unreachable.UnreachableAnalysis;
import com.google.gwt.dev.util.PerfLogger;
import com.google.gwt.dev.util.Preconditions;

/**
 */
public class DataflowOptimizer {
  public static boolean exec(JProgram jprogram, JNode node) {
    PerfLogger.start("DataflowOptimizer");
    boolean didChange = new DataflowOptimizer(jprogram).execImpl(node);
    PerfLogger.end();
    return didChange;
  }

  public static boolean exec(JProgram jprogram) {
    return exec(jprogram, jprogram);
  }

  private final JProgram program;

  public DataflowOptimizer(JProgram program) {
    this.program = program;
  }

  private class DataflowOptimizerVisitor extends JModVisitor {
    @Override
    public boolean visit(JMethodBody methodBody, Context ctx) {
      Cfg cfgGraph = CfgBuilder.build(program, methodBody.getBlock());

      JMethod method = methodBody.getMethod();
      JDeclaredType enclosingType = method.getEnclosingType();
      String methodName = enclosingType.getName() + "." + method.getName();

      Preconditions.checkNotNull(cfgGraph, "Can't build flow for %s", methodName);
      
      CombinedIntegratedAnalysis<CfgNode<?>, CfgEdge, CfgTransformer, Cfg> fwdAnalysis = 
        new CombinedIntegratedAnalysis<CfgNode<?>, CfgEdge, CfgTransformer, Cfg>();
      
      fwdAnalysis.addAnalysis(new UnreachableAnalysis());
      fwdAnalysis.addAnalysis(new ConstantsAnalysis(program));
      fwdAnalysis.addAnalysis(new CopyAnalysis());
      // fwdAnalysis.addAnalysis(new InlineVarAnalysis(program));
    
      boolean madeChanges = false;
      
      madeChanges = AnalysisSolver.solveIntegrated(cfgGraph, fwdAnalysis, true) || madeChanges; 
      
      cfgGraph = CfgBuilder.build(program, methodBody.getBlock());
      Preconditions.checkNotNull(cfgGraph);
      
      CombinedIntegratedAnalysis<CfgNode<?>, CfgEdge, CfgTransformer, Cfg> bkwAnalysis = 
        new CombinedIntegratedAnalysis<CfgNode<?>, CfgEdge, CfgTransformer, Cfg>();
      
      bkwAnalysis.addAnalysis(new LivenessAnalysis(program));
      
      madeChanges = AnalysisSolver.solveIntegrated(cfgGraph, bkwAnalysis, false) || madeChanges; 

      if (madeChanges) {
        didChange = true;
        
        DeadCodeElimination.exec(program, methodBody);
      }

      return true;
    }
  }

  private boolean execImpl(JNode node) {
    DataflowOptimizerVisitor visitor = new DataflowOptimizerVisitor();
    visitor.accept(node);
    return visitor.didChange();
  }
}
