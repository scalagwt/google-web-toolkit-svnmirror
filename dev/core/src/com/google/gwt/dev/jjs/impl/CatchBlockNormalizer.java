/*
 * Copyright 2006 Google Inc.
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

import com.google.gwt.dev.jjs.ast.Context;
import com.google.gwt.dev.jjs.ast.JBlock;
import com.google.gwt.dev.jjs.ast.JExpression;
import com.google.gwt.dev.jjs.ast.JExpressionStatement;
import com.google.gwt.dev.jjs.ast.JIfStatement;
import com.google.gwt.dev.jjs.ast.JInstanceOf;
import com.google.gwt.dev.jjs.ast.JLocal;
import com.google.gwt.dev.jjs.ast.JLocalRef;
import com.google.gwt.dev.jjs.ast.JMethod;
import com.google.gwt.dev.jjs.ast.JMethodCall;
import com.google.gwt.dev.jjs.ast.JModVisitor;
import com.google.gwt.dev.jjs.ast.JProgram;
import com.google.gwt.dev.jjs.ast.JReferenceType;
import com.google.gwt.dev.jjs.ast.JStatement;
import com.google.gwt.dev.jjs.ast.JThrowStatement;
import com.google.gwt.dev.jjs.ast.JTryStatement;
import com.google.gwt.dev.jjs.ast.JSourceInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Merge multi-catch blocks into a single catch block that uses instanceof tests
 * to determine which user block to run.
 */
public class CatchBlockNormalizer {

  /**
   * Collapses all multi-catch blocks into a single catch block.
   */
  private class CollapseCatchBlocks extends JModVisitor {

    // @Override
    public void endVisit(JMethod x, Context ctx) {
      clearLocals();
      currentMethod = null;
    }

    // @Override
    public void endVisit(JTryStatement x, Context ctx) {
      if (x.getCatchBlocks().isEmpty()) {
        return;
      }

      JSourceInfo catchInfo = ((JBlock) x.getCatchBlocks().get(0)).getSourceInfo();

      JLocal exObj = popTempLocal();
      JLocalRef exRef = new JLocalRef(program, catchInfo, exObj);
      JBlock newCatchBlock = new JBlock(program, catchInfo);
      // $e = Exceptions.caught($e)
      JMethod caughtMethod = program.getSpecialMethod("Exceptions.caught");
      JMethodCall call = new JMethodCall(program, catchInfo, null, caughtMethod);
      call.getArgs().add(exRef);
      JExpressionStatement asg = program.createAssignmentStmt(catchInfo, exRef,
          call);
      newCatchBlock.statements.add(asg);

      /*
       * Build up a series of if, else if statements to test the type of the
       * exception object against the type of the user's catch block.
       * 
       * Go backwards so we can nest the else statements in the correct order!
       */
      // rethrow the current exception if no one caught it
      JStatement cur = new JThrowStatement(program, null, exRef);
      for (int i = x.getCatchBlocks().size() - 1; i >= 0; --i) {
        JBlock block = (JBlock) x.getCatchBlocks().get(i);
        JLocalRef arg = (JLocalRef) x.getCatchArgs().get(i);
        catchInfo = block.getSourceInfo();
        JReferenceType argType = (JReferenceType) arg.getType();
        // if ($e instanceof Argtype) { userVar = $e; <user code> }
        JExpression ifTest = new JInstanceOf(program, catchInfo, argType, exRef);
        asg = program.createAssignmentStmt(catchInfo, arg, exRef);
        if (!block.statements.isEmpty()) {
          // Only bother adding the assingment if the block is non-empty
          block.statements.add(0, asg);
        }
        // nest the previous as an else for me
        cur = new JIfStatement(program, catchInfo, ifTest, block, cur);
      }

      newCatchBlock.statements.add(cur);
      x.getCatchArgs().clear();
      x.getCatchArgs().add(exRef);
      x.getCatchBlocks().clear();
      x.getCatchBlocks().add(newCatchBlock);
    }

    // @Override
    public boolean visit(JMethod x, Context ctx) {
      currentMethod = x;
      clearLocals();
      return true;
    }

    // @Override
    public boolean visit(JTryStatement x, Context ctx) {
      if (!x.getCatchBlocks().isEmpty()) {
        pushTempLocal();
      }
      return true;
    }
  }

  public static void exec(JProgram program) {
    new CatchBlockNormalizer(program).execImpl();
  }

  private JMethod currentMethod;
  private int localIndex;
  private final JProgram program;
  private final List/* <JLocal> */tempLocals = new ArrayList/* <JLocal> */();

  private CatchBlockNormalizer(JProgram program) {
    this.program = program;
  }

  private void clearLocals() {
    tempLocals.clear();
    localIndex = 0;
  }

  private void execImpl() {
    CollapseCatchBlocks collapser = new CollapseCatchBlocks();
    collapser.accept(program);
  }

  private JLocal popTempLocal() {
    return (JLocal) tempLocals.get(--localIndex);
  }

  private void pushTempLocal() {
    if (localIndex == tempLocals.size()) {
      JLocal newTemp = program.createLocal(null,
          ("$e" + localIndex).toCharArray(), program.getTypeJavaLangObject(),
          false, currentMethod);
      tempLocals.add(newTemp);
    }
    ++localIndex;
  }

}
