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
package com.google.gwt.dev.js;

import com.google.gwt.dev.js.ast.CanBooleanEval;
import com.google.gwt.dev.js.ast.JsBinaryOperation;
import com.google.gwt.dev.js.ast.JsBinaryOperator;
import com.google.gwt.dev.js.ast.JsBlock;
import com.google.gwt.dev.js.ast.JsBreak;
import com.google.gwt.dev.js.ast.JsConditional;
import com.google.gwt.dev.js.ast.JsContext;
import com.google.gwt.dev.js.ast.JsContinue;
import com.google.gwt.dev.js.ast.JsDoWhile;
import com.google.gwt.dev.js.ast.JsExprStmt;
import com.google.gwt.dev.js.ast.JsExpression;
import com.google.gwt.dev.js.ast.JsFor;
import com.google.gwt.dev.js.ast.JsFunction;
import com.google.gwt.dev.js.ast.JsIf;
import com.google.gwt.dev.js.ast.JsModVisitor;
import com.google.gwt.dev.js.ast.JsPrefixOperation;
import com.google.gwt.dev.js.ast.JsProgram;
import com.google.gwt.dev.js.ast.JsStatement;
import com.google.gwt.dev.js.ast.JsUnaryOperator;
import com.google.gwt.dev.js.ast.JsVars;
import com.google.gwt.dev.js.ast.JsVisitor;
import com.google.gwt.dev.js.ast.JsWhile;
import com.google.gwt.dev.js.ast.JsVars.JsVar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.Stack;

/**
 * Removes JsFunctions that are never referenced in the program.
 */
public class JsStaticEval {

  /**
   * Examines code to find out whether it contains any break or continue
   * statements.
   * 
   * TODO: We could be more sophisticated with this. A nested while loop with an
   * unlabeled break should not cause this visitor to return false. Nor should a
   * labeled break break to another context.
   */
  public static class FindBreakContinueStatementsVisitor extends JsVisitor {
    private boolean hasBreakContinueStatements = false;

    @Override
    public void endVisit(JsBreak x, JsContext<JsStatement> ctx) {
      hasBreakContinueStatements = true;
    }

    @Override
    public void endVisit(JsContinue x, JsContext<JsStatement> ctx) {
      hasBreakContinueStatements = true;
    }

    protected boolean hasBreakContinueStatements() {
      return hasBreakContinueStatements;
    }
  }

  /**
   * Force all functions to be evaluated at the top of the lexical scope in
   * which they reside. This makes {@link StaticEvalVisitor} simpler in that we
   * no longer have to worry about function declarations within expressions.
   * After this runs, only statements can contain declarations. Moved functions
   * will end up just before the statement in which they presently reside.
   */
  private class EvalFunctionsAtTopScope extends JsModVisitor {
    private final Set<JsFunction> dontMove = new HashSet<JsFunction>();
    private final Stack<JsBlock> scopeStack = new Stack<JsBlock>();
    private final Stack<ListIterator<JsStatement>> itrStack = new Stack<ListIterator<JsStatement>>();

    @Override
    public void endVisit(JsFunction x, JsContext<JsExpression> ctx) {
      scopeStack.pop();
    }

    @Override
    public void endVisit(JsProgram x, JsContext<JsProgram> ctx) {
      scopeStack.pop();
    }

    @Override
    public boolean visit(JsBlock x, JsContext<JsStatement> ctx) {
      if (x == scopeStack.peek()) {
        ListIterator<JsStatement> itr = x.getStatements().listIterator();
        itrStack.push(itr);
        while (itr.hasNext()) {
          JsStatement stmt = itr.next();
          JsFunction func = isFunctionDecl(stmt);
          // Already at the top level.
          if (func != null) {
            dontMove.add(func);
          }
          accept(stmt);
          if (func != null) {
            dontMove.remove(func);
          }
        }
        itrStack.pop();
        // Already visited.
        return false;
      } else {
        // Just do normal visitation.
        return true;
      }
    }

    @Override
    public boolean visit(JsFunction x, JsContext<JsExpression> ctx) {
      /*
       * We do this during visit() to preserve first-to-last evaluation order.
       */
      if (x.getName() != null && !dontMove.contains(x)) {
        /*
         * Reinsert this function into the statement immediately before the
         * current statement. The current statement will have already been
         * returned from the current iterator's next(), so we have to
         * backshuffle one step to get in front of it.
         */
        ListIterator<JsStatement> itr = itrStack.peek();
        itr.previous();
        itr.add(x.makeStmt());
        itr.next();
        ctx.replaceMe(x.getName().makeRef());
      }

      // Dive into the function itself.
      scopeStack.push(x.getBody());
      return true;
    }

    @Override
    public boolean visit(JsProgram x, JsContext<JsProgram> ctx) {
      scopeStack.push(x.getGlobalBlock());
      return true;
    }
  }

  /**
   * Creates a minimalist list of statements that must be run in order to
   * achieve the same declaration effect as the visited statements.
   * 
   * For example, a JsFunction declaration should be run as a JsExprStmt. JsVars
   * should be run without any initializers.
   * 
   * This visitor is called from
   * {@link StaticEvalVisitor#ensureDeclarations(JsStatement)} on any statements
   * that are removed from a function.
   */
  private static class MustExecVisitor extends JsVisitor {

    private final List<JsStatement> mustExec = new ArrayList<JsStatement>();

    public MustExecVisitor() {
    }

    @Override
    public void endVisit(JsExprStmt x, JsContext<JsStatement> ctx) {
      JsFunction func = isFunctionDecl(x);
      if (func != null) {
        mustExec.add(x);
      }
    }

    @Override
    public void endVisit(JsVars x, JsContext<JsStatement> ctx) {
      JsVars strippedVars = new JsVars();
      boolean mustReplace = false;
      for (JsVar var : x) {
        JsVar strippedVar = new JsVar(var.getName());
        strippedVars.add(strippedVar);
        if (var.getInitExpr() != null) {
          mustReplace = true;
        }
      }
      if (mustReplace) {
        mustExec.add(strippedVars);
      } else {
        mustExec.add(x);
      }
    }

    public List<JsStatement> getStatements() {
      return mustExec;
    }

    @Override
    public boolean visit(JsFunction x, JsContext<JsExpression> ctx) {
      // Don't dive into nested functions.
      return false;
    }
  }

  /**
   * Does static evals.
   * 
   * TODO: borrow more concepts from
   * {@link com.google.gwt.dev.jjs.impl.DeadCodeElimination}, such as ignored
   * expression results.
   */
  private class StaticEvalVisitor extends JsModVisitor {

    private Set<JsExpression> evalBooleanContext = new HashSet<JsExpression>();

    @Override
    public void endVisit(JsBinaryOperation x, JsContext<JsExpression> ctx) {
      JsBinaryOperator op = x.getOperator();
      JsExpression arg1 = x.getArg1();
      JsExpression arg2 = x.getArg2();
      if (op == JsBinaryOperator.AND) {
        shortCircuitAnd(arg1, arg2, ctx);
      } else if (op == JsBinaryOperator.OR) {
        shortCircuitOr(arg1, arg2, ctx);
      } else if (op == JsBinaryOperator.COMMA) {
        trySimplifyComma(arg1, arg2, ctx);
      }
    }

    /**
     * Prune dead statements and empty blocks.
     */
    @Override
    public void endVisit(JsBlock x, JsContext<JsStatement> ctx) {
      /*
       * Remove any dead statements after an abrupt change in code flow and
       * promote safe statements within nested blocks to this block.
       */
      List<JsStatement> stmts = x.getStatements();
      for (int i = 0; i < stmts.size(); i++) {
        JsStatement stmt = stmts.get(i);

        if (stmt instanceof JsBlock) {
          // Promote a sub-block's children to the current block.
          JsBlock block = (JsBlock) stmt;
          stmts.remove(i);
          stmts.addAll(i, block.getStatements());
          i--;
          didChange = true;
          continue;
        }

        if (stmt.unconditionalControlBreak()) {
          // Abrupt change in flow, chop the remaining items from this block
          for (int j = i + 1; j < stmts.size();) {
            JsStatement toRemove = stmts.get(j);
            JsStatement toReplace = ensureDeclarations(toRemove);
            if (toReplace == null) {
              stmts.remove(j);
              didChange = true;
            } else if (toReplace == toRemove) {
              ++j;
            } else {
              stmts.set(j, toReplace);
              didChange = true;
            }
          }
        }
      }

      if (ctx.canRemove() && stmts.size() == 0) {
        // Remove blocks with no effect
        ctx.removeMe();
      }
    }

    @Override
    public void endVisit(JsConditional x, JsContext<JsExpression> ctx) {
      evalBooleanContext.remove(x.getTestExpression());

      JsExpression condExpr = x.getTestExpression();
      JsExpression thenExpr = x.getThenExpression();
      JsExpression elseExpr = x.getElseExpression();
      if (condExpr instanceof CanBooleanEval) {
        CanBooleanEval condEval = (CanBooleanEval) condExpr;
        if (condEval.isBooleanTrue()) {
          // e.g. (true() ? then : else) -> true() && then
          JsBinaryOperation binOp = new JsBinaryOperation(JsBinaryOperator.AND,
              condExpr, thenExpr);
          ctx.replaceMe(accept(binOp));
        } else if (condEval.isBooleanFalse()) {
          // e.g. (false() ? then : else) -> false() || else
          JsBinaryOperation binOp = new JsBinaryOperation(JsBinaryOperator.OR,
              condExpr, elseExpr);
          ctx.replaceMe(accept(binOp));
        }
      }
    }

    /**
     * Convert do { } while (false); into a block.
     */
    @Override
    public void endVisit(JsDoWhile x, JsContext<JsStatement> ctx) {
      evalBooleanContext.remove(x.getCondition());

      JsExpression expr = x.getCondition();
      if (expr instanceof CanBooleanEval) {
        CanBooleanEval cond = (CanBooleanEval) expr;

        // If false, replace do with do's body
        if (cond.isBooleanFalse()) {
          // Unless it contains break/continue statements
          FindBreakContinueStatementsVisitor visitor = new FindBreakContinueStatementsVisitor();
          visitor.accept(x.getBody());
          if (!visitor.hasBreakContinueStatements()) {
            JsBlock block = new JsBlock();
            block.getStatements().add(x.getBody());
            block.getStatements().add(expr.makeStmt());
            ctx.replaceMe(accept(block));
          }
        }
      }
    }

    @Override
    public void endVisit(JsExprStmt x, JsContext<JsStatement> ctx) {
      if (!x.getExpression().hasSideEffects()) {
        if (ctx.canRemove()) {
          ctx.removeMe();
        } else {
          ctx.replaceMe(program.getEmptyStmt());
        }
      }
    }

    /**
     * Prune for (X; false(); Y) statements, make sure X and false() are run.
     */
    @Override
    public void endVisit(JsFor x, JsContext<JsStatement> ctx) {
      evalBooleanContext.remove(x.getCondition());

      JsExpression expr = x.getCondition();
      if (expr instanceof CanBooleanEval) {
        CanBooleanEval cond = (CanBooleanEval) expr;

        // If false, replace with initializers and condition.
        if (cond.isBooleanFalse()) {
          JsBlock block = new JsBlock();
          if (x.getInitExpr() != null) {
            block.getStatements().add(x.getInitExpr().makeStmt());
          }
          if (x.getInitVars() != null) {
            block.getStatements().add(x.getInitVars());
          }
          block.getStatements().add(expr.makeStmt());
          JsStatement decls = ensureDeclarations(x.getBody());
          if (decls != null) {
            block.getStatements().add(decls);
          }
          ctx.replaceMe(accept(block));
        }
      }
    }

    /**
     * Simplify if statements.
     */
    @Override
    public void endVisit(JsIf x, JsContext<JsStatement> ctx) {
      evalBooleanContext.remove(x.getIfExpr());

      JsExpression expr = x.getIfExpr();
      JsStatement thenStmt = x.getThenStmt();
      JsStatement elseStmt = x.getElseStmt();
      if (expr instanceof CanBooleanEval) {
        CanBooleanEval cond = (CanBooleanEval) expr;
        JsStatement onlyStmtToExecute;
        JsStatement removed;
        if (cond.isBooleanTrue()) {
          onlyStmtToExecute = thenStmt;
          removed = elseStmt;
        } else if (cond.isBooleanFalse()) {
          onlyStmtToExecute = elseStmt;
          removed = thenStmt;
        } else {
          return;
        }
        JsBlock block = new JsBlock();
        block.getStatements().add(expr.makeStmt());

        if (onlyStmtToExecute != null) {
          // We'll see this if the expression is always false and no else
          block.getStatements().add(onlyStmtToExecute);
        }

        JsStatement decls = ensureDeclarations(removed);
        if (decls != null) {
          block.getStatements().add(decls);
        }
        ctx.replaceMe(accept(block));
      } else if (isEmpty(thenStmt) && isEmpty(elseStmt)) {
        ctx.replaceMe(expr.makeStmt());
      }
    }

    /**
     * Change !!x to x in a boolean context.
     */
    @Override
    public void endVisit(JsPrefixOperation x, JsContext<JsExpression> ctx) {
      if (x.getOperator() == JsUnaryOperator.NOT) {
        evalBooleanContext.remove(x.getArg());
      }

      if (evalBooleanContext.contains(x)) {
        if ((x.getOperator() == JsUnaryOperator.NOT)
            && (x.getArg() instanceof JsPrefixOperation)) {
          JsPrefixOperation arg = (JsPrefixOperation) x.getArg();
          if (arg.getOperator() == JsUnaryOperator.NOT) {
            ctx.replaceMe(arg.getArg());
            return;
          }
        }
      }
    }

    /**
     * Prune while (false) statements.
     */
    @Override
    public void endVisit(JsWhile x, JsContext<JsStatement> ctx) {
      evalBooleanContext.remove(x.getCondition());

      JsExpression expr = x.getCondition();
      if (expr instanceof CanBooleanEval) {
        CanBooleanEval cond = (CanBooleanEval) expr;

        // If false, replace with condition.
        if (cond.isBooleanFalse()) {
          JsBlock block = new JsBlock();
          block.getStatements().add(expr.makeStmt());
          JsStatement decls = ensureDeclarations(x.getBody());
          if (decls != null) {
            block.getStatements().add(decls);
          }
          ctx.replaceMe(accept(block));
        }
      }
    }

    @Override
    public boolean visit(JsConditional x, JsContext<JsExpression> ctx) {
      evalBooleanContext.add(x.getTestExpression());
      return true;
    }

    @Override
    public boolean visit(JsDoWhile x, JsContext<JsStatement> ctx) {
      evalBooleanContext.add(x.getCondition());
      return true;
    }

    @Override
    public boolean visit(JsFor x, JsContext<JsStatement> ctx) {
      evalBooleanContext.add(x.getCondition());
      return true;
    }

    @Override
    public boolean visit(JsIf x, JsContext<JsStatement> ctx) {
      evalBooleanContext.add(x.getIfExpr());
      return true;
    }

    @Override
    public boolean visit(JsPrefixOperation x, JsContext<JsExpression> ctx) {
      if (x.getOperator() == JsUnaryOperator.NOT) {
        evalBooleanContext.add(x.getArg());
      }
      return true;
    }

    @Override
    public boolean visit(JsWhile x, JsContext<JsStatement> ctx) {
      evalBooleanContext.add(x.getCondition());
      return true;
    }

    /**
     * This method MUST be called whenever any statements are removed from a
     * function. This is because some statements, such as JsVars or JsFunction
     * have the effect of defining local variables, no matter WHERE they are in
     * the function. The returned statement (if any), must be executed. It is
     * also possible for stmt to be directly returned, in which case the caller
     * should not perform AST changes that would cause an infinite optimization
     * loop.
     * 
     * Note: EvalFunctionsAtTopScope will have changed any JsFunction
     * declarations into statements before this visitor runs.
     */
    private JsStatement ensureDeclarations(JsStatement stmt) {
      if (stmt == null) {
        return null;
      }
      MustExecVisitor mev = new MustExecVisitor();
      mev.accept(stmt);
      List<JsStatement> stmts = mev.getStatements();
      if (stmts.isEmpty()) {
        return null;
      } else if (stmts.size() == 1) {
        return stmts.get(0);
      } else {
        JsBlock jsBlock = new JsBlock();
        jsBlock.getStatements().addAll(stmts);
        return jsBlock;
      }
    }
  }

  public static boolean exec(JsProgram program) {
    return (new JsStaticEval(program)).execImpl();
  }

  protected static boolean isEmpty(JsStatement stmt) {
    if (stmt == null) {
      return true;
    }
    return (stmt instanceof JsBlock && ((JsBlock) stmt).getStatements().isEmpty());
  }

  /**
   * If the statement is a JsExprStmt that declares a function with no other
   * side effects, returns that function; otherwise <code>null</code>.
   */
  protected static JsFunction isFunctionDecl(JsStatement stmt) {
    if (stmt instanceof JsExprStmt) {
      JsExprStmt exprStmt = (JsExprStmt) stmt;
      JsExpression expr = exprStmt.getExpression();
      if (expr instanceof JsFunction) {
        JsFunction func = (JsFunction) expr;
        if (func.getName() != null) {
          return func;
        }
      }
    }
    return null;
  }

  /**
   * Simplify short circuit AND expressions.
   * 
   * <pre>
   * if (true && isWhatever()) -> if (isWhatever()), unless side effects
   * if (false() && isWhatever()) -> if (false())
   * </pre>
   */
  protected static void shortCircuitAnd(JsExpression arg1, JsExpression arg2,
      JsContext<JsExpression> ctx) {
    if (arg1 instanceof CanBooleanEval) {
      CanBooleanEval eval1 = (CanBooleanEval) arg1;
      if (eval1.isBooleanTrue() && !arg1.hasSideEffects()) {
        ctx.replaceMe(arg2);
      } else if (eval1.isBooleanFalse()) {
        ctx.replaceMe(arg1);
      }
    }
  }

  /**
   * Simplify short circuit OR expressions.
   * 
   * <pre>
   * if (true() || isWhatever()) -> if (true())
   * if (false || isWhatever()) -> if (isWhatever()), unless side effects
   * </pre>
   */
  protected static void shortCircuitOr(JsExpression arg1, JsExpression arg2,
      JsContext<JsExpression> ctx) {
    if (arg1 instanceof CanBooleanEval) {
      CanBooleanEval eval1 = (CanBooleanEval) arg1;
      if (eval1.isBooleanTrue()) {
        ctx.replaceMe(arg1);
      } else if (eval1.isBooleanFalse() && !arg1.hasSideEffects()) {
        ctx.replaceMe(arg2);
      }
    }
  }

  protected static void trySimplifyComma(JsExpression arg1, JsExpression arg2,
      JsContext<JsExpression> ctx) {
    if (!arg1.hasSideEffects()) {
      ctx.replaceMe(arg2);
    }
  }

  private final JsProgram program;

  public JsStaticEval(JsProgram program) {
    this.program = program;
  }

  public boolean execImpl() {
    EvalFunctionsAtTopScope fev = new EvalFunctionsAtTopScope();
    fev.accept(program);
    StaticEvalVisitor sev = new StaticEvalVisitor();
    sev.accept(program);

    return fev.didChange() || sev.didChange();
  }
}
