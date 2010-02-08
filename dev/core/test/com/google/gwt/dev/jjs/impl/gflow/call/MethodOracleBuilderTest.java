package com.google.gwt.dev.jjs.impl.gflow.call;

import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.dev.jjs.ast.JProgram;
import com.google.gwt.dev.jjs.impl.OptimizerTestBase;
import com.google.gwt.dev.jjs.impl.gflow.call.MethodOracle;
import com.google.gwt.dev.jjs.impl.gflow.call.MethodOracleBuilder;
import com.google.gwt.dev.util.Strings;

/**
 */
public class MethodOracleBuilderTest extends OptimizerTestBase {
  public void testSmoke() throws Exception {
    addSnippetClassDecl("static int i;");
    addSnippetClassDecl("static void foo() { i = 1; }");
    addSnippetClassDecl("static void bar() { foo(); }");
    addSnippetClassDecl("static class Foo { int i; }");
    addSnippetClassDecl("static void baz(Foo f) { f.i = 0; }");
    addSnippetClassDecl("static void bax(Foo f) {}");
    addSnippetClassDecl("static void bax(Foo f) {}");
/*    compute().into(
        "test.EntryPoint.bar=T",
        "test.EntryPoint.baz=T",
        "test.EntryPoint.foo=T"
        );
*/  }

  public void testInterface() throws Exception {
    addSnippetClassDecl("static int i;");
    addSnippetClassDecl("interface Runnable { void run(); }");
    addSnippetClassDecl("static class RunnableImpl1 implements Runnable { " +
    		"public void run() { } }");
    addSnippetClassDecl("static class RunnableImpl2 implements Runnable { " +
    		"public void run() { i = 1; } }");
    addSnippetClassDecl("static void foo(RunnableImpl1 r) { r.run(); }");
    addSnippetClassDecl("static void bar(Runnable r) { r.run(); }");
    addSnippetClassDecl("static void baz(RunnableImpl2 r) { r.run(); }");
/*    compute().into(
        "test.EntryPoint$RunnableImpl2.run=T",
        "test.EntryPoint.bar=T",
        "test.EntryPoint.baz=T"
        );
*/  }
    
  private class Result {
    private final MethodOracle buildMethodOracle;

    public Result(MethodOracle buildMethodOracle) {
      this.buildMethodOracle = buildMethodOracle;
    }

    public void into(String...strings) {
      String actual = buildMethodOracle.toString();
      actual = actual.replaceAll("java\\.lang\\.String\\..*\n?", "");
      actual = actual.replaceAll("com\\.google\\..*\n?", "");
      assertEquals(Strings.join(strings, "\n"), actual);
    }
  }
  
  private Result compute()
      throws UnableToCompleteException {
    JProgram program = compileSnippet("void", "");
    return new Result(MethodOracleBuilder.buildMethodOracle(program));
  }
}
