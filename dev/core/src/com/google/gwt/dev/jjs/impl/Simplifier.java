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

import com.google.gwt.dev.jjs.SourceInfo;
import com.google.gwt.dev.jjs.ast.JBinaryOperation;
import com.google.gwt.dev.jjs.ast.JBinaryOperator;
import com.google.gwt.dev.jjs.ast.JBlock;
import com.google.gwt.dev.jjs.ast.JBooleanLiteral;
import com.google.gwt.dev.jjs.ast.JCastOperation;
import com.google.gwt.dev.jjs.ast.JConditional;
import com.google.gwt.dev.jjs.ast.JExpression;
import com.google.gwt.dev.jjs.ast.JIfStatement;
import com.google.gwt.dev.jjs.ast.JPrefixOperation;
import com.google.gwt.dev.jjs.ast.JPrimitiveType;
import com.google.gwt.dev.jjs.ast.JProgram;
import com.google.gwt.dev.jjs.ast.JStatement;
import com.google.gwt.dev.jjs.ast.JType;
import com.google.gwt.dev.jjs.ast.JUnaryOperation;
import com.google.gwt.dev.jjs.ast.JUnaryOperator;
import com.google.gwt.dev.jjs.ast.JValueLiteral;
import com.google.gwt.dev.jjs.ast.js.JMultiExpression;

import java.util.List;

/**
 * Methods that both construct and try to simplify AST nodes. If simplification
 * fails, then the methods will return an original, unmodified version of the
 * node if one is supplied. The routines do not recurse into their arguments;
 * the arguments are assumed to already be simplified as much as possible.
 */
public class Simplifier {
  /**
   * TODO: if the AST were normalized, we wouldn't need this.
   */
  public static boolean isEmpty(JStatement stmt) {
    if (stmt == null) {
      return true;
    }
    return (stmt instanceof JBlock && ((JBlock) stmt).statements.isEmpty());
  }

  /**
   * Negate the supplied expression if negating it makes the expression shorter.
   * Otherwise, return null.
   */
  static JExpression maybeUnflipBoolean(JExpression expr) {
    if (expr instanceof JUnaryOperation) {
      JUnaryOperation unop = (JUnaryOperation) expr;
      if (unop.getOp() == JUnaryOperator.NOT) {
        return unop.getArg();
      }
    }
    return null;
  }

  private static <T> List<T> allButLast(List<T> list) {
    return list.subList(0, list.size() - 1);
  }

  private static <T> T last(List<T> list) {
    return list.get(list.size() - 1);
  }

  private final JProgram program;

  public Simplifier(JProgram program) {
    this.program = program;
  }

  public JExpression cast(JExpression original, SourceInfo sourceInfo,
      JType type, JExpression exp) {
    if (type == exp.getType()) {
      return exp;
    }
    if ((type instanceof JPrimitiveType) && (exp instanceof JValueLiteral)) {
      // Statically evaluate casting literals.
      JPrimitiveType typePrim = (JPrimitiveType) type;
      JValueLiteral expLit = (JValueLiteral) exp;
      JValueLiteral casted = typePrim.coerceLiteral(expLit);
      if (casted != null) {
        return casted;
      }
    }

    /*
     * Discard casts from byte or short to int, because such casts are always
     * implicit anyway. Cannot coerce char since that would change the semantics
     * of concat.
     */
    if (type == program.getTypePrimitiveInt()) {
      JType expType = exp.getType();
      if ((expType == program.getTypePrimitiveShort())
          || (expType == program.getTypePrimitiveByte())) {
        return exp;
      }
    }

    // no simplification made
    if (original != null) {
      return original;
    }
    return new JCastOperation(program, exp.getSourceInfo(), type, exp);
  }

  public JExpression cast(JType type, JExpression exp) {
    return cast(null, exp.getSourceInfo(), type, exp);
  }

  public JExpression conditional(JConditional original, SourceInfo sourceInfo,
      JType type, JExpression condExpr, JExpression thenExpr,
      JExpression elseExpr) {
    if (condExpr instanceof JMultiExpression) {
      // (a,b,c)?d:e -> a,b,(c?d:e)
      // TODO(spoon): do this outward multi movement for all AST nodes
      JMultiExpression condMulti = (JMultiExpression) condExpr;
      JMultiExpression newMulti = new JMultiExpression(program, sourceInfo);
      newMulti.exprs.addAll(allButLast(condMulti.exprs));
      newMulti.exprs.add(conditional(null, sourceInfo, type,
          last(condMulti.exprs), thenExpr, elseExpr));
      // TODO(spoon): immediately simplify the resulting multi
      return newMulti;
    }
    if (condExpr instanceof JBooleanLiteral) {
      if (((JBooleanLiteral) condExpr).getValue()) {
        // e.g. (true ? then : else) -> then
        return thenExpr;
      } else {
        // e.g. (false ? then : else) -> else
        return elseExpr;
      }
    } else if (thenExpr instanceof JBooleanLiteral) {
      if (((JBooleanLiteral) thenExpr).getValue()) {
        // e.g. (cond ? true : else) -> cond || else
        JBinaryOperation binOp = new JBinaryOperation(program,
            original.getSourceInfo(), original.getType(), JBinaryOperator.OR,
            condExpr, elseExpr);
        return binOp;
      } else {
        // e.g. (cond ? false : else) -> !cond && else
        JPrefixOperation notCondExpr = new JPrefixOperation(program,
            condExpr.getSourceInfo(), JUnaryOperator.NOT, condExpr);
        JBinaryOperation binOp = new JBinaryOperation(program,
            original.getSourceInfo(), original.getType(), JBinaryOperator.AND,
            notCondExpr, elseExpr);
        return binOp;
      }
    } else if (elseExpr instanceof JBooleanLiteral) {
      if (((JBooleanLiteral) elseExpr).getValue()) {
        // e.g. (cond ? then : true) -> !cond || then
        JPrefixOperation notCondExpr = new JPrefixOperation(program,
            condExpr.getSourceInfo(), JUnaryOperator.NOT, condExpr);
        JBinaryOperation binOp = new JBinaryOperation(program,
            original.getSourceInfo(), original.getType(), JBinaryOperator.OR,
            notCondExpr, thenExpr);
        return binOp;
      } else {
        // e.g. (cond ? then : false) -> cond && then
        JBinaryOperation binOp = new JBinaryOperation(program,
            original.getSourceInfo(), original.getType(), JBinaryOperator.AND,
            condExpr, thenExpr);
        return binOp;
      }
    } else {
      // e.g. (!cond ? then : else) -> (cond ? else : then)
      JExpression unflipped = maybeUnflipBoolean(condExpr);
      if (unflipped != null) {
        return new JConditional(program, original.getSourceInfo(),
            original.getType(), unflipped, elseExpr, thenExpr);
      }
    }

    // no simplification made
    if (original != null) {
      return original;
    }
    return new JConditional(program, sourceInfo, type, condExpr, thenExpr,
        elseExpr);
  }

  public JStatement ifStatement(JIfStatement original, SourceInfo sourceInfo,
      JExpression condExpr, JStatement thenStmt, JStatement elseStmt) {
    if (condExpr instanceof JMultiExpression) {
      // if(a,b,c) d else e -> {a; b; if(c) d else e; }
      JMultiExpression condMulti = (JMultiExpression) condExpr;
      JBlock newBlock = new JBlock(program, sourceInfo);
      for (JExpression expr : allButLast(condMulti.exprs)) {
        newBlock.statements.add(expr.makeStatement());
      }
      newBlock.statements.add(ifStatement(null, sourceInfo,
          last(condMulti.exprs), thenStmt, elseStmt));
      // TODO(spoon): immediately simplify the resulting block
      return newBlock;
    }

    if (condExpr instanceof JBooleanLiteral) {
      JBooleanLiteral booleanLiteral = (JBooleanLiteral) condExpr;
      boolean boolVal = booleanLiteral.getValue();
      if (boolVal && !isEmpty(thenStmt)) {
        // If true, replace myself with then statement
        return thenStmt;
      } else if (!boolVal && !isEmpty(elseStmt)) {
        // If false, replace myself with else statement
        return elseStmt;
      } else {
        // just prune me
        return condExpr.makeStatement();
      }
    }

    if (isEmpty(thenStmt) && isEmpty(elseStmt)) {
      return condExpr.makeStatement();
    }

    if (!isEmpty(elseStmt)) {
      // if (!cond) foo else bar -> if (cond) bar else foo
      JExpression unflipped = Simplifier.maybeUnflipBoolean(condExpr);
      if (unflipped != null) {
        // Force sub-parts to blocks, otherwise we break else-if chains.
        // TODO: this goes away when we normalize the Java AST properly.
        thenStmt = ensureBlock(thenStmt);
        elseStmt = ensureBlock(elseStmt);
        return ifStatement(null, sourceInfo, unflipped, elseStmt, thenStmt);
      }
    }

    // no simplification made
    if (original != null) {
      return original;
    }
    return new JIfStatement(program, condExpr.getSourceInfo(), condExpr,
        thenStmt, elseStmt);
  }

  public JExpression not(JPrefixOperation original, SourceInfo sourceInfo,
      JExpression arg) {
    if (arg instanceof JMultiExpression) {
      // !(a,b,c) -> (a,b,!c)
      JMultiExpression argMulti = (JMultiExpression) arg;
      JMultiExpression newMulti = new JMultiExpression(program, sourceInfo);
      newMulti.exprs.addAll(allButLast(argMulti.exprs));
      newMulti.exprs.add(not(null, sourceInfo, last(argMulti.exprs)));
      // TODO(spoon): immediately simplify the newMulti
      return newMulti;
    }
    if (arg instanceof JBinaryOperation) {
      // try to invert the binary operator
      JBinaryOperation argOp = (JBinaryOperation) arg;
      JBinaryOperator op = argOp.getOp();
      JBinaryOperator newOp = null;
      if (op == JBinaryOperator.EQ) {
        // e.g. !(x == y) -> x != y
        newOp = JBinaryOperator.NEQ;
      } else if (op == JBinaryOperator.NEQ) {
        // e.g. !(x != y) -> x == y
        newOp = JBinaryOperator.EQ;
      } else if (op == JBinaryOperator.GT) {
        // e.g. !(x > y) -> x <= y
        newOp = JBinaryOperator.LTE;
      } else if (op == JBinaryOperator.LTE) {
        // e.g. !(x <= y) -> x > y
        newOp = JBinaryOperator.GT;
      } else if (op == JBinaryOperator.GTE) {
        // e.g. !(x >= y) -> x < y
        newOp = JBinaryOperator.LT;
      } else if (op == JBinaryOperator.LT) {
        // e.g. !(x < y) -> x >= y
        newOp = JBinaryOperator.GTE;
      }
      if (newOp != null) {
        JBinaryOperation newBinOp = new JBinaryOperation(program,
            argOp.getSourceInfo(), argOp.getType(), newOp, argOp.getLhs(),
            argOp.getRhs());
        return newBinOp;
      }
    } else if (arg instanceof JPrefixOperation) {
      // try to invert the unary operator
      JPrefixOperation argOp = (JPrefixOperation) arg;
      JUnaryOperator op = argOp.getOp();
      // e.g. !!x -> x
      if (op == JUnaryOperator.NOT) {
        return argOp.getArg();
      }
    }

    // no simplification made
    if (original != null) {
      return original;
    }
    return new JPrefixOperation(program, arg.getSourceInfo(),
        JUnaryOperator.NOT, arg);
  }

  private JStatement ensureBlock(JStatement stmt) {
    if (!(stmt instanceof JBlock)) {
      JBlock block = new JBlock(program, stmt.getSourceInfo());
      block.statements.add(stmt);
      stmt = block;
    }
    return stmt;
  }
}
