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

import com.google.gwt.dev.jjs.ast.JBlock;
import com.google.gwt.dev.jjs.ast.JExpressionStatement;
import com.google.gwt.dev.jjs.ast.JLiteral;
import com.google.gwt.dev.jjs.ast.JMethod;
import com.google.gwt.dev.jjs.ast.JMethodCall;
import com.google.gwt.dev.jjs.ast.JProgram;
import com.google.gwt.dev.jjs.ast.JReferenceType;
import com.google.gwt.dev.jjs.ast.JStatement;
import com.google.gwt.dev.jjs.ast.JStringLiteral;
import com.google.gwt.dev.jjs.ast.JTryStatement;
import com.google.gwt.dev.jjs.ast.JVisitor;
import com.google.gwt.dev.jjs.ast.Context;
import com.google.gwt.dev.jjs.ast.JSourceInfo;
import com.google.gwt.dev.jjs.ast.js.JsniMethod;
import com.google.gwt.dev.js.ast.JsFunction;
import com.google.gwt.dev.js.ast.JsBlock;
import com.google.gwt.dev.js.ast.JsTry;
import com.google.gwt.dev.js.ast.JsExpression;
import com.google.gwt.dev.js.ast.JsInvocation;
import com.google.gwt.dev.js.ast.JsScope;
import com.google.gwt.dev.js.ast.JsStatements;
import com.google.gwt.dev.js.ast.JsExpressions;
import com.google.gwt.dev.js.ast.JsName;
import com.google.gwt.dev.js.ast.JsNameRef;
import com.google.gwt.dev.js.ast.JsProgram;
import com.google.gwt.dev.js.ast.JsStringLiteral;
import com.google.gwt.dev.js.ast.JsExprStmt;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Instruments the program with calls to the profiler API.
 */
public class ProfileInstrumenter {

  private class MethodVisitor extends JVisitor {

    private boolean didChange = false;

    public boolean didChange() {
      return didChange;
    }

    public void endVisit(JMethod method, Context ctx) {
      super.endVisit(method, ctx);
    }

    public boolean visit(JsniMethod method, Context ctx) {

      // Add calls to
      // $wnd.__gwt_methodEntered( klass, name, signature );
      // $wnd.__gwt_methodExited( klass, name, signature );

      if (method.getEnclosingType().getName()
          .equals("com.google.gwt.lang.Profiler")) {
        return super.visit(method, ctx);
      }

      didChange = true;

      JsStringLiteral methodName = jsProgram.getStringLiteral(method.getName());
      JReferenceType enclosingType = method.getEnclosingType();
      JsExpression typeName = enclosingType == null ? jsProgram.getNullLiteral()
          : (JsExpression) jsProgram.getStringLiteral(enclosingType.getName());
      JsExpression signature = jsProgram.getStringLiteral("");

      JsFunction func = method.getFunc();
      JsBlock block = func.getBody();
      JsScope scope = func.getScope();

      JsInvocation methodEnteredCall = createProfileInvoke(scope,
          "__gwt_methodEntered", typeName, methodName, signature);

      JsTry tryBlock = new JsTry();
      tryBlock.setTryBlock(block);
      JsBlock finallyBlock = new JsBlock();
      tryBlock.setFinallyBlock(finallyBlock);

      JsInvocation methodExitedCall = createProfileInvoke(scope,
          "__gwt_methodExited", typeName, methodName, signature);
      finallyBlock.getStatements().add(0, new JsExprStmt(methodExitedCall));

      JsBlock newMethodBody = new JsBlock();
      JsStatements statements = newMethodBody.getStatements();
      statements.add(new JsExprStmt(methodEnteredCall));
      statements.add(tryBlock);
      func.setBody(newMethodBody);

      return super.visit(method, ctx);
    }

    public boolean visit(JMethod method, Context ctx) {
      if (method.isAbstract() || method.isNative()) {
        return super.visit(method, ctx);
      }

      JSourceInfo sourceInfo = method.getSourceInfo();

      JReferenceType enclosingType = method.getEnclosingType();
      JLiteral typeName = enclosingType == null ? program.getLiteralNull()
          : (JLiteral) getStringLiteral(enclosingType.getName());

      JStringLiteral methodName = getStringLiteral(method.getName());
      JStringLiteral methodSignature = getStringLiteral(method.toString());

      JBlock newBody = new JBlock(program, sourceInfo);

      JMethodCall enterCall = new JMethodCall(program, sourceInfo, null,
          profilerEnterMethod);
      enterCall.getArgs().add(typeName);
      enterCall.getArgs().add(methodName);
      enterCall.getArgs().add(methodSignature);

      JMethodCall exitCall = new JMethodCall(program, sourceInfo, null,
          profilerExitMethod);
      exitCall.getArgs().add(typeName);
      exitCall.getArgs().add(methodName);
      exitCall.getArgs().add(methodSignature);

      JStatement enterStmt = new JExpressionStatement(program, sourceInfo,
          enterCall);
      JStatement exitStmt = new JExpressionStatement(program, sourceInfo,
          exitCall);

      JBlock tryBlock = new JBlock(program, sourceInfo);
      tryBlock.statements.addAll(method.body.statements);
      JBlock finallyBlock = new JBlock(program, sourceInfo);
      finallyBlock.statements.add(exitStmt);

      JTryStatement tryStatement = new JTryStatement(program, sourceInfo,
          tryBlock, Collections.EMPTY_LIST,
          Collections.EMPTY_LIST, finallyBlock);

      newBody.statements.add(enterStmt);
      newBody.statements.add(tryStatement);
      method.body.statements.clear();
      method.body.statements.addAll(newBody.statements);

      didChange = true;

      return super.visit(method, ctx);
    }

    private JsInvocation createProfileInvoke(JsScope scope,
        String profilerMethod, JsExpression typeName, JsExpression methodName,
        JsExpression signature) {
      JsName wnd = scope.getOrCreateUnobfuscatableName("$wnd");
      JsName profileMethodName = scope
          .getOrCreateUnobfuscatableName(profilerMethod);
      JsNameRef methodTarget = new JsNameRef(profileMethodName);
      JsNameRef wndTarget = new JsNameRef(wnd);
      methodTarget.setQualifier(wndTarget);

      JsInvocation call = new JsInvocation();
      call.setQualifier(methodTarget);
      JsExpressions args = call.getArguments();
      args.add(typeName);
      args.add(methodName);
      args.add(signature);

      return call;
    }

    private JStringLiteral getStringLiteral(String str) {
      char[] chars = new char[ str.length() ];
      str.getChars(0, str.length(), chars, 0);
      return program.getLiteralString(chars);
    }
  }

  public static void exec(JProgram program, JsProgram jsProgram) {
    new ProfileInstrumenter(program, jsProgram).execImpl();
  }

  private final JsProgram jsProgram;

  private final JProgram program;

  private JMethod profilerEnterMethod;

  private JMethod profilerExitMethod;

  private ProfileInstrumenter(JProgram program, JsProgram jsProgram) {
    this.program = program;
    this.jsProgram = jsProgram;

    // JReferenceType profiler = program.getFromTypeMap( "com.google.gwt.core.client.Profiler" );
    JReferenceType profiler = program.getSpecialProfiler();
    profilerEnterMethod = getMethod(profiler.methods, "methodEntered");
    profilerExitMethod = getMethod(profiler.methods, "methodExited");
  }

  private boolean execImpl() {
    MethodVisitor visitor = new MethodVisitor();
    visitor.accept(program);
    return visitor.didChange;
  }

  private JMethod getMethod(List methods, String name) {
    for (Iterator it = methods.iterator(); it.hasNext();) {
      JMethod method = (JMethod) it.next();
      if (method.getName().equals(name)) {
        return method;
      }
    }
    throw new RuntimeException(
        "Unable to find the method com.google.gwt.core.client.Profiler."
            + name);
  }
}
