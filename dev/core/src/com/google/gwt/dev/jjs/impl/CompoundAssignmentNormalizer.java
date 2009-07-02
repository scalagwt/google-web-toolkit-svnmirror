/*
 * Copyright 2008 Google Inc.
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
package com.google.gwt.dev.jjs.impl;

import com.google.gwt.dev.jjs.InternalCompilerException;
import com.google.gwt.dev.jjs.ast.Context;
import com.google.gwt.dev.jjs.ast.JArrayRef;
import com.google.gwt.dev.jjs.ast.JBinaryOperation;
import com.google.gwt.dev.jjs.ast.JBinaryOperator;
import com.google.gwt.dev.jjs.ast.JExpression;
import com.google.gwt.dev.jjs.ast.JFieldRef;
import com.google.gwt.dev.jjs.ast.JLocal;
import com.google.gwt.dev.jjs.ast.JLocalRef;
import com.google.gwt.dev.jjs.ast.JMethodBody;
import com.google.gwt.dev.jjs.ast.JModVisitor;
import com.google.gwt.dev.jjs.ast.JParameterRef;
import com.google.gwt.dev.jjs.ast.JPostfixOperation;
import com.google.gwt.dev.jjs.ast.JPrefixOperation;
import com.google.gwt.dev.jjs.ast.JProgram;
import com.google.gwt.dev.jjs.ast.JThisRef;
import com.google.gwt.dev.jjs.ast.JType;
import com.google.gwt.dev.jjs.ast.JUnaryOperator;
import com.google.gwt.dev.jjs.ast.js.JMultiExpression;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * <p>
 * Replace problematic compound assignments with a sequence of simpler
 * operations, all of which are either simple assignments or are non-assigning
 * operations. When doing so, be careful that side effects happen exactly once
 * and that the order of any side effects is preserved. The choice of which
 * assignments to replace is made in subclasses; they must override the three
 * <code>shouldBreakUp()</code> methods.
 * </p>
 * 
 * <p>
 * Note that because AST nodes are mutable, they cannot be reused in different
 * parts of the same tree. Instead, the node must be cloned before each
 * insertion into a tree other than the first.
 * </p>
 * 
 * <p>
 * If the <code>reuseTemps</code> constructor parameter is set to
 * <code>true</code>, then temps of the correct type will be reused once they
 * become available. To determine when a temp can be reused, the current
 * implementation uses a notion of "temp usage scopes". Every time a temporary
 * variable is allocated, it is recorded in the current temp usage scope. Once
 * the current temp usage scope is exited, all of its temps become available for
 * use for other purposes.
 * </p>
 */
public abstract class CompoundAssignmentNormalizer {

  /**
   * Breaks apart certain complex assignments.
   */
  private class BreakupAssignOpsVisitor extends JModVisitor {

    @Override
    public void endVisit(JBinaryOperation x, Context ctx) {
      JBinaryOperator op = x.getOp();
      if (op.getNonAssignmentOf() == null) {
        return;
      }
      if (!shouldBreakUp(x)) {
        return;
      }

      /*
       * Convert to an assignment and binary operation. Since the left hand size
       * must be computed twice, we have to replace any left-hand side
       * expressions that could have side effects with temporaries, so that they
       * are only run once.
       */
      enterTempUsageScope();
      ReplaceSideEffectsInLvalue replacer = new ReplaceSideEffectsInLvalue(
          new JMultiExpression(program, x.getSourceInfo()));
      JExpression newLhs = replacer.accept(x.getLhs());
      exitTempUsageScope();

      JBinaryOperation operation = new JBinaryOperation(program,
          x.getSourceInfo(), newLhs.getType(), op.getNonAssignmentOf(), newLhs,
          x.getRhs());
      // newLhs is cloned below because it was used in operation
      JBinaryOperation asg = new JBinaryOperation(program, x.getSourceInfo(),
          newLhs.getType(), JBinaryOperator.ASG,
          cloner.cloneExpression(newLhs), operation);

      JMultiExpression multiExpr = replacer.getMultiExpr();
      if (multiExpr.exprs.isEmpty()) {
        // just use the split assignment expression
        ctx.replaceMe(asg);
      } else {
        // add the assignment as the last item in the multi
        multiExpr.exprs.add(asg);
        ctx.replaceMe(multiExpr);
      }
    }

    @Override
    public void endVisit(JMethodBody x, Context ctx) {
      clearLocals();
      currentMethodBody = null;
    }

    @Override
    public void endVisit(JPostfixOperation x, Context ctx) {
      JUnaryOperator op = x.getOp();
      if (!op.isModifying()) {
        return;
      }
      if (!shouldBreakUp(x)) {
        return;
      }

      // Convert into a comma operation, such as:
      // (t = x, x += 1, t)

      // First, replace the arg with a non-side-effect causing one.
      enterTempUsageScope();
      JMultiExpression multi = new JMultiExpression(program, x.getSourceInfo());
      ReplaceSideEffectsInLvalue replacer = new ReplaceSideEffectsInLvalue(
          multi);
      JExpression newArg = replacer.accept(x.getArg());

      JExpression expressionReturn = expressionToReturn(newArg);

      // Now generate the appropriate expressions.
      JLocal tempLocal = getTempLocal(expressionReturn.getType());

      // t = x
      JLocalRef tempRef = new JLocalRef(program, x.getSourceInfo(), tempLocal);
      JBinaryOperation asg = new JBinaryOperation(program, x.getSourceInfo(),
          x.getType(), JBinaryOperator.ASG, tempRef, expressionReturn);
      multi.exprs.add(asg);

      // x += 1
      asg = createAsgOpFromUnary(newArg, op);
      // Break the resulting asg op before adding to multi.
      multi.exprs.add(accept(asg));

      // t
      tempRef = new JLocalRef(program, x.getSourceInfo(), tempLocal);
      multi.exprs.add(tempRef);

      ctx.replaceMe(multi);
      exitTempUsageScope();
    }

    @Override
    public void endVisit(JPrefixOperation x, Context ctx) {
      JUnaryOperator op = x.getOp();
      if (!op.isModifying()) {
        return;
      }
      if (!shouldBreakUp(x)) {
        return;
      }

      // Convert into the equivalent binary assignment operation, such as:
      // x += 1
      JBinaryOperation asg = createAsgOpFromUnary(x.getArg(), op);

      // Visit the result to break it up even more.
      ctx.replaceMe(accept(asg));
    }

    @Override
    public boolean visit(JMethodBody x, Context ctx) {
      currentMethodBody = x;
      clearLocals();
      return true;
    }

    private JBinaryOperation createAsgOpFromUnary(JExpression arg,
        JUnaryOperator op) {
      JBinaryOperator newOp;
      if (op == JUnaryOperator.INC) {
        newOp = JBinaryOperator.ASG_ADD;
      } else if (op == JUnaryOperator.DEC) {
        newOp = JBinaryOperator.ASG_SUB;
      } else {
        throw new InternalCompilerException(
            "Unexpected modifying unary operator: "
                + String.valueOf(op.getSymbol()));
      }

      JExpression one;
      if (arg.getType() == program.getTypePrimitiveLong()) {
        // use an explicit long, so that LongEmulationNormalizer does not get
        // confused
        one = program.getLiteralLong(1);
      } else {
        // int is safe to add to all other types
        one = program.getLiteralInt(1);
      }
      // arg is cloned below because the caller is allowed to use it somewhere
      JBinaryOperation asg = new JBinaryOperation(program, arg.getSourceInfo(),
          arg.getType(), newOp, cloner.cloneExpression(arg), one);
      return asg;
    }
  }

  /**
   * Replaces side effects in lvalue.
   */
  private class ReplaceSideEffectsInLvalue extends JModVisitor {

    private final JMultiExpression multi;

    ReplaceSideEffectsInLvalue(JMultiExpression multi) {
      this.multi = multi;
    }

    public JMultiExpression getMultiExpr() {
      return multi;
    }

    @Override
    public boolean visit(JArrayRef x, Context ctx) {
      JExpression newInstance = possiblyReplace(x.getInstance());
      JExpression newIndexExpr = possiblyReplace(x.getIndexExpr());
      if (newInstance != x.getInstance() || newIndexExpr != x.getIndexExpr()) {
        JArrayRef newExpr = new JArrayRef(program, x.getSourceInfo(),
            newInstance, newIndexExpr);
        ctx.replaceMe(newExpr);
      }
      return false;
    }

    @Override
    public boolean visit(JFieldRef x, Context ctx) {
      if (x.getInstance() != null) {
        JExpression newInstance = possiblyReplace(x.getInstance());
        if (newInstance != x.getInstance()) {
          JFieldRef newExpr = new JFieldRef(program, x.getSourceInfo(),
              newInstance, x.getField(), x.getEnclosingType());
          ctx.replaceMe(newExpr);
        }
      }
      return false;
    }

    @Override
    public boolean visit(JLocalRef x, Context ctx) {
      return false;
    }

    @Override
    public boolean visit(JParameterRef x, Context ctx) {
      return false;
    }

    @Override
    public boolean visit(JThisRef x, Context ctx) {
      return false;
    }

    private JExpression possiblyReplace(JExpression x) {
      if (!x.hasSideEffects()) {
        return x;
      }

      // Create a temp local
      JLocal tempLocal = getTempLocal(x.getType());

      // Create an assignment for this temp and add it to multi.
      JLocalRef tempRef = new JLocalRef(program, x.getSourceInfo(), tempLocal);
      JBinaryOperation asg = new JBinaryOperation(program, x.getSourceInfo(),
          x.getType(), JBinaryOperator.ASG, tempRef, x);
      multi.exprs.add(asg);
      // Update me with the temp
      return cloner.cloneExpression(tempRef);
    }
  }

  /**
   * For some particular type, tracks all usage of temporary local variables of
   * that type within the current method.
   */
  private static class TempLocalTracker {
    /**
     * Temps previously created in nested usage scopes that are now available
     * for reuse by subsequent code.
     */
    private List<JLocal> reusable = new ArrayList<JLocal>();

    /**
     * Temps in use in active scopes. They cannot currently be reused; however,
     * any time a scope is exited, the set of locals at the top of stack is
     * popped off and all contained locals become reusable. The top of stack
     * correlates to the current scope.
     */
    private Stack<List<JLocal>> usageStack = new Stack<List<JLocal>>();

    public TempLocalTracker() {
      /*
       * Due to the lazy-creation nature, we must assume upon creation that
       * we're already in a valid scope (and thus create the initial empty scope
       * ourselves).
       */
      enterScope();
    }

    public void enterScope() {
      usageStack.push(new ArrayList<JLocal>());
    }

    public void exitScope() {
      /*
       * Due to the lazy-creation nature, the program flow might be several
       * levels deep already when this object is created. Since we don't know
       * how many exitScope() calls to accept, we must be empty-tolerant. In
       * other words, this isn't naively defensive programming. :)
       */
      if (!usageStack.isEmpty()) {
        List<JLocal> freed = usageStack.pop();
        reusable.addAll(freed);
      }
    }

    public JLocal tryGetReusableLocal() {
      if (!reusable.isEmpty()) {
        return reusable.remove(reusable.size() - 1);
      }
      return null;
    }

    public void useLocal(JLocal local) {
      usageStack.peek().add(local);
    }
  }

  protected final JProgram program;
  private final CloneExpressionVisitor cloner;

  private JMethodBody currentMethodBody;

  /**
   * Counter to generate a unique name for each temporary within the current
   * method.
   */
  private int localCounter;

  /**
   * Map of type onto lazily-created local tracker.
   */
  private Map<JType, TempLocalTracker> localTrackers = new IdentityHashMap<JType, TempLocalTracker>();

  /**
   * If <code>true</code>, reuse temps. The pre-optimization subclass does
   * not reuse temps because doing so can defeat optimizations because different
   * uses impact each other and we do nothing to disambiguate usage. After
   * optimizations, it makes sense to reuse temps to reduce code size and memory
   * consumption of the output.
   */
  private final boolean reuseTemps;

  protected CompoundAssignmentNormalizer(JProgram program, boolean reuseTemps) {
    this.program = program;
    this.reuseTemps = reuseTemps;
    cloner = new CloneExpressionVisitor(program);
    clearLocals();
  }

  public void breakUpAssignments() {
    BreakupAssignOpsVisitor breaker = new BreakupAssignOpsVisitor();
    breaker.accept(program);
  }

  /**
   * Decide what expression to return when breaking up a compound assignment of
   * the form <code>lhs op= rhs</code>. By default the <code>lhs</code> is
   * returned.
   */
  protected JExpression expressionToReturn(JExpression lhs) {
    return lhs;
  }

  protected abstract String getTempPrefix();

  protected abstract boolean shouldBreakUp(JBinaryOperation x);

  protected abstract boolean shouldBreakUp(JPostfixOperation x);

  protected abstract boolean shouldBreakUp(JPrefixOperation x);

  private void clearLocals() {
    localCounter = 0;
    localTrackers.clear();
  }

  private void enterTempUsageScope() {
    for (TempLocalTracker tracker : localTrackers.values()) {
      tracker.enterScope();
    }
  }

  private void exitTempUsageScope() {
    for (TempLocalTracker tracker : localTrackers.values()) {
      tracker.exitScope();
    }
  }

  /**
   * Allocate a temporary local variable.
   */
  private JLocal getTempLocal(JType type) {
    TempLocalTracker tracker = localTrackers.get(type);
    if (tracker == null) {
      tracker = new TempLocalTracker();
      localTrackers.put(type, tracker);
    }

    JLocal temp = null;
    if (reuseTemps) {
      /*
       * If the return is non-null, we now "own" the returned JLocal; it's
       * important to call tracker.useLocal() on the returned value (below).
       */
      temp = tracker.tryGetReusableLocal();
    }

    if (temp == null) {
      temp = program.createLocal(null,
          (getTempPrefix() + localCounter++).toCharArray(), type, false,
          currentMethodBody);
    }
    tracker.useLocal(temp);
    return temp;
  }
}
