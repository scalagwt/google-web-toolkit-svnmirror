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
import com.google.gwt.dev.jjs.ast.JLiteral;
import com.google.gwt.dev.jjs.ast.JLocal;
import com.google.gwt.dev.jjs.ast.JNewArray;
import com.google.gwt.dev.jjs.ast.JNewInstance;
import com.google.gwt.dev.jjs.ast.JParameter;
import com.google.gwt.dev.jjs.ast.JProgram;
import com.google.gwt.dev.jjs.ast.JVariable;
import com.google.gwt.dev.jjs.ast.JVariableRef;
import com.google.gwt.dev.jjs.ast.JVisitor;
import com.google.gwt.dev.jjs.impl.gflow.AssumptionMap;
import com.google.gwt.dev.jjs.impl.gflow.AssumptionUtil;
import com.google.gwt.dev.jjs.impl.gflow.FlowFunction;
import com.google.gwt.dev.jjs.impl.gflow.cfg.Cfg;
import com.google.gwt.dev.jjs.impl.gflow.cfg.CfgEdge;
import com.google.gwt.dev.jjs.impl.gflow.cfg.CfgMethodCallNode;
import com.google.gwt.dev.jjs.impl.gflow.cfg.CfgNode;
import com.google.gwt.dev.jjs.impl.gflow.cfg.CfgReadWriteNode;
import com.google.gwt.dev.jjs.impl.gflow.cfg.CfgVisitor;
import com.google.gwt.dev.jjs.impl.gflow.cfg.CfgWriteNode;

final class InlineVarFlowFunction implements
    FlowFunction<CfgNode<?>, CfgEdge, Cfg, InlineVarAssumption> {
  private final JProgram program;

  public InlineVarFlowFunction(JProgram program) {
    super();
    this.program = program;
  }

  public void interpret(CfgNode<?> node,
      Cfg g, AssumptionMap<CfgEdge, InlineVarAssumption> assumptionMap) {
    InlineVarAssumption in = AssumptionUtil.join(
        g.getInEdges(node), assumptionMap);
    final InlineVarAssumption out = new InlineVarAssumption(in);

    node.accept(new CfgVisitor() {
      @Override
      public void visitMethodCallNode(CfgMethodCallNode node) {
        if (node.getJNode().hasSideEffects(program.methodOracle)) {
          out.killAll();
        }
      }

      @Override
      public void visitReadWriteNode(CfgReadWriteNode node) {
        out.killAll();
      }

      @Override
      public void visitWriteNode(CfgWriteNode node) {
        final JVariable targetVariable = node.getTargetVariable();
        // Always mark variable as T.
        out.kill(targetVariable);
        // TODO: we can make a more precise analysis of what to kill.
        out.killAll();

        if (isSupportedVar(targetVariable)) {
          JExpression value = node.getValue();
          if (value != null && !(value instanceof JLiteral)
              && !value.hasSideEffects(program.methodOracle)) {
            final boolean[] canInline = new boolean[] {true};
            new JVisitor() {
              @Override
              public void endVisit(JVariableRef x, Context ctx) {
                if (x.getTarget() == targetVariable) {
                  canInline[0] = false;
                }
              }

              @Override
              public void endVisit(JNewArray x, Context ctx) {
                canInline[0] = false;
              }

              @Override
              public void endVisit(JNewInstance x, Context ctx) {
                canInline[0] = false;
              }

            }.accept(value);

            if (canInline[0]) {
              out.setValue(targetVariable, value);
            }
          }
        }
      }

      private boolean isSupportedVar(JVariable targetVariable) {
        return targetVariable instanceof JParameter
            || targetVariable instanceof JLocal;
      }
    });

    AssumptionUtil.setAssumptions(g.getOutEdges(node), out, assumptionMap);
  }
}