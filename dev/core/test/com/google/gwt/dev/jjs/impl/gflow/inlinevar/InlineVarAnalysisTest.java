package com.google.gwt.dev.jjs.impl.gflow.inlinevar;

import com.google.gwt.dev.jjs.ast.JProgram;
import com.google.gwt.dev.jjs.impl.gflow.Analysis;
import com.google.gwt.dev.jjs.impl.gflow.CfgAnalysisTestBase;
import com.google.gwt.dev.jjs.impl.gflow.cfg.Cfg;
import com.google.gwt.dev.jjs.impl.gflow.cfg.CfgEdge;
import com.google.gwt.dev.jjs.impl.gflow.cfg.CfgNode;

public class InlineVarAnalysisTest extends CfgAnalysisTestBase<InlineVarAssumption> {
  @Override
  protected Analysis<CfgNode<?>, CfgEdge, Cfg, InlineVarAssumption> createAnalysis(
      JProgram program) {
    return new InlineVarAnalysis(program);
  }
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    addSnippetClassDecl("static boolean b;");
    addSnippetClassDecl("static class Foo { int i; int j; int k; }");
    addSnippetClassDecl("static Foo createFoo() {return null;}");
    addSnippetClassDecl("static Foo staticFooInstance;");
  }

  public void testLinearStatements() throws Exception {
    analyze("int", 
        "int i = staticFooInstance.i + 1;",
        "return i;").into(
            "BLOCK -> [* {}]",
            "STMT -> [* {}]",
            "READ(i) -> [* {}]",
            "READ(staticFooInstance) -> [* {}]",
            "WRITE(i, EntryPoint.staticFooInstance.i + 1) -> [* {i = EntryPoint.staticFooInstance.i + 1}]",
            "STMT -> [* {i = EntryPoint.staticFooInstance.i + 1}]",
            "READ(i) -> [* {i = EntryPoint.staticFooInstance.i + 1}]",
            "GOTO -> [* {i = EntryPoint.staticFooInstance.i + 1}]",
            "END");
  }
  
  public void testMethodCallKillsAll() throws Exception {
    analyze("int", 
        "int i = staticFooInstance.i + 1;",
        "createFoo();",
        "return i;").into(
            "BLOCK -> [* {}]",
            "STMT -> [* {}]",
            "READ(i) -> [* {}]",
            "READ(staticFooInstance) -> [* {}]",
            "WRITE(i, EntryPoint.staticFooInstance.i + 1) -> [* {i = EntryPoint.staticFooInstance.i + 1}]",
            "STMT -> [* {i = EntryPoint.staticFooInstance.i + 1}]",
            "OPTTHROW(createFoo()) -> [NOTHROW=* {i = EntryPoint.staticFooInstance.i + 1}, RE=1 {i = EntryPoint.staticFooInstance.i + 1}]",
            "CALL(createFoo) -> [* {i = T}]",
            "STMT -> [* {i = T}]",
            "READ(i) -> [* {i = T}]",
            "GOTO -> [* {i = T}]",
            "1: END");
  }

  public void testLoop() throws Exception {
    analyze("int", 
        "int i = 0; int j = 0;",
        "while (b) {",
        "  j = i + 2;",
        "  i = j + 1;",
        "}",
        "return i;").into(
            "BLOCK -> [* {}]",
            "STMT -> [* {}]",
            "WRITE(i, 0) -> [* {i = T}]",
            "STMT -> [* {i = T}]",
            "WRITE(j, 0) -> [* {i = T, j = T}]",
            "STMT -> [* {i = T, j = T}]",
            "1: READ(b) -> [* {i = T, j = T}]",
            "COND (EntryPoint.b) -> [THEN=* {i = T, j = T}, ELSE=2 {i = T, j = T}]",
            "BLOCK -> [* {i = T, j = T}]",
            "STMT -> [* {i = T, j = T}]",
            "READ(i) -> [* {i = T, j = T}]",
            "WRITE(j, i + 2) -> [* {i = T, j = i + 2}]",
            "STMT -> [* {i = T, j = i + 2}]",
            "READ(j) -> [* {i = T, j = i + 2}]",
            "WRITE(i, j + 1) -> [1 {i = j + 1, j = T}]",
            "2: STMT -> [* {i = T, j = T}]",
            "READ(i) -> [* {i = T, j = T}]",
            "GOTO -> [* {i = T, j = T}]",
            "END");
  }
}
