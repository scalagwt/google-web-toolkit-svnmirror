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
package com.google.gwt.dev.jjs.impl.gflow.constants;

import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.dev.jjs.ast.JBlock;
import com.google.gwt.dev.jjs.ast.JDeclarationStatement;
import com.google.gwt.dev.jjs.ast.JIntLiteral;
import com.google.gwt.dev.jjs.ast.JMethod;
import com.google.gwt.dev.jjs.ast.JMethodBody;
import com.google.gwt.dev.jjs.ast.JNullLiteral;
import com.google.gwt.dev.jjs.ast.JProgram;
import com.google.gwt.dev.jjs.ast.JReturnStatement;
import com.google.gwt.dev.jjs.ast.JStatement;
import com.google.gwt.dev.jjs.ast.JValueLiteral;
import com.google.gwt.dev.jjs.impl.OptimizerTestBase;
import com.google.gwt.dev.jjs.impl.gflow.constants.ConstantsAssumption;
import com.google.gwt.dev.jjs.impl.gflow.constants.ExpressionEvaluator;

import java.util.List;

/**
 * 
 */
public class AssumptionsBasedEvaluatorTest extends OptimizerTestBase {
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    addSnippetClassDecl("static String foo() { return null; };");
  }

 public void testVariableRef() throws Exception {
    evaluate("i", "int",
             "int i = 1;", new JIntLiteral(null, 1)).into("1");
  }
  
  public void testEq() throws Exception {
    evaluate("i == 1", "boolean",
             "int i = 1;", new JIntLiteral(null, 1)).into("true");
    evaluate("i != 1", "boolean",
             "int i = 1;", new JIntLiteral(null, 1)).into("false");
  }

  public void testNullNotNull() throws Exception {
    evaluate("s == null", "boolean",
             "String s = null;", JNullLiteral.INSTANCE).into("true");
    evaluate("s != null", "boolean",
             "String s = null;", JNullLiteral.INSTANCE).into("false");
    evaluate("null == s", "boolean",
             "String s = null;", JNullLiteral.INSTANCE).into("true");
    evaluate("null != s", "boolean",
             "String s = null;", JNullLiteral.INSTANCE).into("false");
  }

  private static class Result {
    private final JValueLiteral literal;

    public Result(JValueLiteral literal) {
      this.literal = literal;
    }

    public void into(String string) {
      String actual = literal == null ? "<null>" : literal.toSource();
      assertEquals(string, actual);
    }
  }
  
  private Result evaluate(String expr, String type, 
      String decls, JValueLiteral assumption) throws UnableToCompleteException {
    String codeSnippet = decls;
    codeSnippet += "return " + expr + ";";
    JProgram program = compileSnippet(type, codeSnippet);
    JMethod mainMethod = findMainMethod(program);
    JBlock block = ((JMethodBody) mainMethod.getBody()).getBlock();
    List<JStatement> statements = block.getStatements();

    ConstantsAssumption assumptions = new ConstantsAssumption();

    // TODO: clean this mess up.
    for (int i = 0; i < statements.size() - 1; ++i) {
      JDeclarationStatement decl = (JDeclarationStatement) statements.get(i);
      assumptions.set(decl.getVariableRef().getTarget(), assumption);
    }

    JReturnStatement stmt = (JReturnStatement) statements.get(statements.size() - 1);
    return new Result(ExpressionEvaluator.evaluate(stmt.getExpr(), assumptions));
  }
}
