package com.google.gwt.dev.jjs.impl.gflow.copy;

import com.google.gwt.dev.jjs.ast.JProgram;
import com.google.gwt.dev.jjs.impl.gflow.Analysis;
import com.google.gwt.dev.jjs.impl.gflow.CfgAnalysisTestBase;
import com.google.gwt.dev.jjs.impl.gflow.cfg.Cfg;
import com.google.gwt.dev.jjs.impl.gflow.cfg.CfgEdge;
import com.google.gwt.dev.jjs.impl.gflow.cfg.CfgNode;

public class CopyAnalysisTest extends CfgAnalysisTestBase<CopyAssumption> {
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    addSnippetClassDecl("static boolean b;");
  }

  public void testCopyCreation() throws Exception {
    analyze("void", "int i = 1; int j = i;").into(
        "BLOCK -> [*]",
        "STMT -> [*]",
        "WRITE(i, 1) -> [* {i = T}]",
        "STMT -> [* {i = T}]",
        "READ(i) -> [* {i = T}]",
        "WRITE(j, i) -> [* {i = T, j = i}]",
        "END");
  }

  public void testCopyKill1() throws Exception {
    analyze("void", "int i = 1; int j = i; j = 1;").into(
        "BLOCK -> [*]",
        "STMT -> [*]",
        "WRITE(i, 1) -> [* {i = T}]",
        "STMT -> [* {i = T}]",
        "READ(i) -> [* {i = T}]",
        "WRITE(j, i) -> [* {i = T, j = i}]",
        "STMT -> [* {i = T, j = i}]",
        "WRITE(j, 1) -> [* {i = T, j = T}]",
        "END");
  }

  public void testCopyKill2() throws Exception {
    analyze("void", "int i = 1; int j = i; i = 2;").into(
        "BLOCK -> [*]",
        "STMT -> [*]",
        "WRITE(i, 1) -> [* {i = T}]",
        "STMT -> [* {i = T}]",
        "READ(i) -> [* {i = T}]",
        "WRITE(j, i) -> [* {i = T, j = i}]",
        "STMT -> [* {i = T, j = i}]",
        "WRITE(i, 2) -> [* {i = T, j = T}]",
        "END");
  }
  
  public void testConditionalKill() throws Exception {
    analyze("void", "int i = 1; int j = i; if (b) { j = 1; } int k = j;").into(
        "BLOCK -> [*]",
        "STMT -> [*]",
        "WRITE(i, 1) -> [* {i = T}]",
        "STMT -> [* {i = T}]",
        "READ(i) -> [* {i = T}]",
        "WRITE(j, i) -> [* {i = T, j = i}]",
        "STMT -> [* {i = T, j = i}]",
        "READ(b) -> [* {i = T, j = i}]",
        "COND (EntryPoint.b) -> [THEN=* {i = T, j = i}, ELSE=1 {i = T, j = i}]",
        "BLOCK -> [* {i = T, j = i}]",
        "STMT -> [* {i = T, j = i}]",
        "WRITE(j, 1) -> [* {i = T, j = T}]",
        "1: STMT -> [* {i = T, j = T}]",
        "READ(j) -> [* {i = T, j = T}]",
        "WRITE(k, j) -> [* {i = T, j = T, k = j}]",
        "END");
  }

  @Override
  protected Analysis<CfgNode<?>, CfgEdge, Cfg, CopyAssumption> createAnalysis(
      JProgram program) {
    return new CopyAnalysis();
  }
}
