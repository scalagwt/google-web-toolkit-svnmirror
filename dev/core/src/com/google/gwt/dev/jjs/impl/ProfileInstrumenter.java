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

import com.google.gwt.dev.jjs.ast.JProgram;
import com.google.gwt.dev.jjs.ast.JMethod;
import com.google.gwt.dev.jjs.ast.JReferenceType;
import com.google.gwt.dev.jjs.ast.JVisitor;
import com.google.gwt.dev.jjs.ast.JBlock;
import com.google.gwt.dev.jjs.ast.JTryStatement;
import com.google.gwt.dev.jjs.ast.JMethodCall;
import com.google.gwt.dev.jjs.ast.JStatement;
import com.google.gwt.dev.jjs.ast.JExpressionStatement;
import com.google.gwt.dev.jjs.ast.JStringLiteral;
import com.google.gwt.dev.jjs.ast.JLiteral;
import com.google.gwt.dev.jjs.ast.change.ChangeList;

import java.util.List;
import java.util.Collections;
import java.util.Iterator;

/**
 * Instruments the program with calls to the profiler API.
 *
 */
public class ProfileInstrumenter {

  public static void exec(JProgram program) {
    new ProfileInstrumenter(program).execImpl();
  }

  private final JProgram program;
  private JMethod profilerEnterMethod;
  private JMethod profilerExitMethod;

  private class MethodVisitor extends JVisitor {

    private final ChangeList changeList = new ChangeList(
        "Instrument methods with calls to the profiler." );

    public void endVisit(JMethod method) {
      super.endVisit(method);
    }

    public ChangeList getChangeList() {
      return changeList;
    }

    public boolean visit(JMethod method) {

      // TODO(tobyr): implement native methods
      if ( method.isAbstract() || method.isNative() ) {
        return super.visit(method);
      }

      JReferenceType enclosingType = method.getEnclosingType();
      JLiteral typeName = enclosingType == null ? program.getLiteralNull() : (JLiteral) getStringLiteral( enclosingType.getName() );

      JStringLiteral methodName = getStringLiteral(method.getName());
      JStringLiteral methodSignature = getStringLiteral(method.toString());

      JBlock newBody = new JBlock(program);

      JMethodCall enterCall = new JMethodCall(program, null, profilerEnterMethod);
      enterCall.args.add(typeName);
      enterCall.args.add(methodName);
      enterCall.args.add(methodSignature);

      JMethodCall exitCall = new JMethodCall(program, null, profilerExitMethod);
      exitCall.args.add(typeName);
      exitCall.args.add(methodName);
      exitCall.args.add(methodSignature);

      JStatement enterStmt = new JExpressionStatement(program, enterCall);
      JStatement exitStmt = new JExpressionStatement(program, exitCall);

      JBlock tryBlock = new JBlock(program);
      tryBlock.statements.addAll( method.body.statements );
      JBlock finallyBlock = new JBlock(program);
      finallyBlock.statements.add(exitStmt);

      JTryStatement tryStatement = new JTryStatement( program, tryBlock, Collections.EMPTY_LIST,
        Collections.EMPTY_LIST, finallyBlock );

      newBody.statements.add(enterStmt);
      newBody.statements.add( tryStatement );
      method.body.statements.clear();
      method.body.statements.addAll(newBody.statements);

      return super.visit(method);
    }

    private JStringLiteral getStringLiteral( String str ) {
      char[] chars = new char[ str.length() ];
      str.getChars( 0, str.length(), chars, 0 );
      return program.getLiteralString( chars );
    }
  }

  private ProfileInstrumenter(JProgram program) {
    this.program = program;

    // JReferenceType profiler = program.getFromTypeMap( "com.google.gwt.core.client.Profiler" );
    JReferenceType profiler = program.getSpecialProfiler();
    profilerEnterMethod = getMethod( profiler.methods, "methodEntered" );
    profilerExitMethod = getMethod( profiler.methods, "methodExited" );
  }

  private JMethod getMethod( List methods, String name ) {
    for ( Iterator it = methods.iterator(); it.hasNext(); ) {
      JMethod method = (JMethod) it.next();
      if ( method.getName().equals( name ) ) {
        return method;
      }
    }
    throw new RuntimeException( "Unable to find the method com.google.gwt.core.client.Profiler." + name );
  }

  private void execImpl() {
    MethodVisitor visitor = new MethodVisitor();
    program.traverse(visitor);
    ChangeList changes = visitor.getChangeList();
    if (!changes.empty()) {
      changes.apply();
    }
  }
}
