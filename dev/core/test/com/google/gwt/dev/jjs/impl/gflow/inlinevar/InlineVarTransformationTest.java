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
package com.google.gwt.dev.jjs.impl.gflow.inlinevar;

import com.google.gwt.dev.jjs.ast.JProgram;
import com.google.gwt.dev.jjs.impl.gflow.CfgIntegratedAnalysisTestBase;
import com.google.gwt.dev.jjs.impl.gflow.IntegratedAnalysis;
import com.google.gwt.dev.jjs.impl.gflow.cfg.Cfg;
import com.google.gwt.dev.jjs.impl.gflow.cfg.CfgEdge;
import com.google.gwt.dev.jjs.impl.gflow.cfg.CfgNode;
import com.google.gwt.dev.jjs.impl.gflow.cfg.CfgTransformer;

/**
 * 
 */
public class InlineVarTransformationTest extends CfgIntegratedAnalysisTestBase<InlineVarAssumption> {
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    addSnippetClassDecl("static class Foo { int i; int j; int k; }");
    addSnippetClassDecl("static Foo createFoo() {return null;}");
    addSnippetClassDecl("static Foo staticFooInstance;");
    addSnippetClassDecl("static boolean b;");
  }

  public void testLinearStatements() throws Exception {
    transform("int", 
        "int i = staticFooInstance.i + 1;",
        "return i;").into(
        "int i = EntryPoint.staticFooInstance.i + 1;",
        "return EntryPoint.staticFooInstance.i + 1;");
  }
  
  public void testMethodCallKillsAll() throws Exception {
    transform("int", 
        "int i = staticFooInstance.i + 1;",
        "createFoo();",
        "return i;").into(
        "int i = EntryPoint.staticFooInstance.i + 1;",
        "EntryPoint.createFoo();",
        "return i;");
  }

  public void testFieldWriteAll() throws Exception {
    transform("int", 
        "int i = staticFooInstance.i + 1;",
        "b = false;",
        "return i;").into(
        "int i = EntryPoint.staticFooInstance.i + 1;",
        "EntryPoint.b = false;",
        "return i;");
  }

  public void testLoop() throws Exception {
    transform("int", 
        "int i = 0; int j = 0;",
        "while (b) {",
        "  j = i + 2;",
        "  i = j + 1;",
        "}",
        "return i;").into(
        "int i = 0;",
        "int j = 0;",
        "while (EntryPoint.b) {",
        "  j = i + 2;",
        "  i = i + 2 + 1;",
        "}",
        "return i;");
  }

  public void testFunctionCall() throws Exception {
    transform("int", 
        "Foo foo = createFoo();",
        "return foo.i;").into(
        "EntryPoint$Foo foo = EntryPoint.createFoo();",
        "return foo.i;"); 
  }
  
  @Override
  protected IntegratedAnalysis<CfgNode<?>, CfgEdge, CfgTransformer, Cfg, InlineVarAssumption> createIntegratedAnalysis(
      JProgram program) {
    return new InlineVarAnalysis(program);
  }
}
