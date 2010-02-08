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
package com.google.gwt.dev.jjs.impl.gflow.inlinevar;

import com.google.gwt.dev.jjs.ast.Context;
import com.google.gwt.dev.jjs.ast.JExpression;
import com.google.gwt.dev.jjs.ast.JModVisitor;
import com.google.gwt.dev.jjs.ast.JNode;
import com.google.gwt.dev.jjs.ast.JProgram;
import com.google.gwt.dev.jjs.impl.CloneExpressionVisitor;
import com.google.gwt.dev.jjs.impl.gflow.Analysis;
import com.google.gwt.dev.jjs.impl.gflow.AssumptionMap;
import com.google.gwt.dev.jjs.impl.gflow.AssumptionUtil;
import com.google.gwt.dev.jjs.impl.gflow.FlowFunction;
import com.google.gwt.dev.jjs.impl.gflow.IntegratedAnalysis;
import com.google.gwt.dev.jjs.impl.gflow.IntegratedFlowFunction;
import com.google.gwt.dev.jjs.impl.gflow.TransformationFunction.Transformation;
import com.google.gwt.dev.jjs.impl.gflow.cfg.Cfg;
import com.google.gwt.dev.jjs.impl.gflow.cfg.CfgBuilder;
import com.google.gwt.dev.jjs.impl.gflow.cfg.CfgEdge;
import com.google.gwt.dev.jjs.impl.gflow.cfg.CfgNode;
import com.google.gwt.dev.jjs.impl.gflow.cfg.CfgReadNode;
import com.google.gwt.dev.jjs.impl.gflow.cfg.CfgTransformer;
import com.google.gwt.dev.jjs.impl.gflow.cfg.CfgUtil;
import com.google.gwt.dev.util.Preconditions;

import java.util.ArrayList;

/**
 * 
 */
public class InlineVarAnalysis implements 
    Analysis<CfgNode<?>, CfgEdge, Cfg, InlineVarAssumption>,
    IntegratedAnalysis<CfgNode<?>, CfgEdge, CfgTransformer, Cfg, InlineVarAssumption> {

  private final InlineVarFlowFunction flowFunction;
  private final JProgram program;

  public InlineVarAnalysis(JProgram program) {
    this.program = program;
    flowFunction = new InlineVarFlowFunction(program);
  }
  
  public FlowFunction<CfgNode<?>, CfgEdge, Cfg, InlineVarAssumption> getFlowFunction() {
    return flowFunction;
  }

  public IntegratedFlowFunction<CfgNode<?>, CfgEdge, CfgTransformer, Cfg, InlineVarAssumption> getIntegratedFlowFunction() {
    return new IntegratedFlowFunction<CfgNode<?>, CfgEdge, CfgTransformer, Cfg, InlineVarAssumption>() {
      public Transformation<CfgTransformer, Cfg> interpretOrReplace(
          final CfgNode<?> node, final Cfg graph,
          AssumptionMap<CfgEdge, InlineVarAssumption> assumptionMap) {
        InlineVarAssumption in = new InlineVarAssumption(AssumptionUtil.join(
            graph.getInEdges(node), assumptionMap));

        if (node instanceof CfgReadNode) {
          final CfgReadNode readNode = (CfgReadNode) node;
          final JExpression value = in.get(readNode.getTarget());
          if (value != null) {
            Transformation<CfgTransformer, Cfg> transformation = new Transformation<CfgTransformer, Cfg>() {
              public CfgTransformer getGraphTransformer() {
                return new CfgTransformer() {
                  public boolean transform(CfgNode<?> node, Cfg cfgGraph) {
                    JModVisitor visitor = new JModVisitor() {
                      @Override
                      public boolean visit(JNode x, Context ctx) {
                        if (x == readNode.getJNode()) {
                          CloneExpressionVisitor cloner = new CloneExpressionVisitor(program);
                          ctx.replaceMe(cloner.cloneExpression(value));
                          return false;
                        }
                        return true;
                      } 
                    };
                    CfgNode<?> parentNode = CfgUtil.findContainingStatement(node);
                    JNode parentJNode = parentNode.getJNode();
                    visitor.accept(parentJNode);
                    Preconditions.checkArgument(visitor.didChange());
                    return true;
                  }
                };
              }
              
              public Cfg getNewSubgraph() {
                Cfg exprCfg = CfgBuilder.buildExpressionCfg(program, value);
                Preconditions.checkArgument(exprCfg.getGraphInEdges().isEmpty());
                Preconditions.checkArgument(exprCfg.getGraphOutEdges().isEmpty());
                ArrayList<CfgNode<?>> newNodes = exprCfg.getNodes();
                CfgUtil.addGraphEdges(graph, node, newNodes.get(0), 
                    newNodes.get(newNodes.size() - 1), exprCfg);
                return exprCfg;
              }
            }; 
     
           return transformation;
          }
        }
        
        getFlowFunction().interpret(node, graph, assumptionMap);
        return null;
      }
    };
  }

  public void setInitialGraphAssumptions(Cfg graph,
      AssumptionMap<CfgEdge, InlineVarAssumption> assumptionMap) {
    // bottom assumptions.
  }
}
