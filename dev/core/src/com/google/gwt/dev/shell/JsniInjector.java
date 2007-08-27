/*
 * Copyright 2007 Google Inc.
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
package com.google.gwt.dev.shell;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.CompilationUnitProvider;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JParameter;
import com.google.gwt.core.ext.typeinfo.JPrimitiveType;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.NotFoundException;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.dev.jdt.CompilationUnitProviderWithAlternateSource;
import com.google.gwt.dev.js.ast.JsBlock;
import com.google.gwt.dev.util.Jsni;
import com.google.gwt.dev.util.StringCopier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapts compilation units containing JSNI-accessible code by rewriting the
 * source.
 */
public class JsniInjector {

  /**
   * A consolidated way to get all expected types and succeed or fail
   * atomically.
   */
  private class CoreTypes {
    static final String PKG_JSOBJECT = "com.google.gwt.core.client";
    static final String CLS_JSOBJECT = "JavaScriptObject";
    static final String PKG_STRING = "java.lang";
    static final String CLS_STRING = "String";

    public final JClassType javaLangString;

    public final JClassType javaScriptObject;

    public CoreTypes(TreeLogger logger) throws UnableToCompleteException {
      javaScriptObject = getCoreType(logger, PKG_JSOBJECT, CLS_JSOBJECT);
      javaLangString = getCoreType(logger, PKG_STRING, CLS_STRING);
    }

    private JClassType getCoreType(TreeLogger logger, String pkg, String cls)
        throws UnableToCompleteException {
      try {
        return oracle.getType(pkg, cls);
      } catch (NotFoundException e) {
        String msg = "Unable to find core type '" + pkg + "." + cls + "'";
        logger.log(TreeLogger.ERROR, msg, e);
        throw new UnableToCompleteException();
      }
    }
  }

  /**
   * A chunk of replacement text and where to put it.
   */
  private static class Replacement implements Comparable {
    public final int end;

    public final int start;

    public final char[] text;

    public Replacement(int start, int end, char[] text) {
      this.start = start;
      this.end = end;
      this.text = text;
    }

    public int compareTo(Object o) {
      Replacement other = (Replacement) o;
      if (start < other.start) {
        assert (end <= other.start) : "Overlapping changes not supported";
        return -1;
      } else if (start > other.start) {
        assert (start >= other.end) : "Overlapping changes not supported";
        return 1;
      } else {
        return 0;
      }
    }
  }

  private final TypeOracle oracle;

  private CoreTypes coreTypes;

  private final Map parsedJsByMethod = new IdentityHashMap();

  public JsniInjector(TypeOracle oracle) {
    this.oracle = oracle;
  }

  public CompilationUnitProvider inject(TreeLogger logger,
      CompilationUnitProvider cup) throws UnableToCompleteException {

    logger = logger.branch(TreeLogger.SPAM,
        "Checking for JavaScript native methods", null);

    // Make sure the core types exist.
    //
    if (coreTypes == null) {
      coreTypes = new CoreTypes(logger);
    }

    // Analyze the source and build a list of changes.
    //
    char[] source = cup.getSource();
    List changes = new ArrayList();
    rewriteCompilationUnit(logger, source, changes, cup);

    // Sort and apply the changes.
    //
    int n = changes.size();
    if (n > 0) {
      Replacement[] repls = (Replacement[]) changes.toArray(new Replacement[n]);
      Arrays.sort(repls);
      StringCopier copier = new StringCopier(source);
      for (int i = 0; i < n; ++i) {
        Replacement repl = repls[i];
        copier.commit(repl.text, repl.start, repl.end);
      }

      char[] results = copier.finish();

      return new CompilationUnitProviderWithAlternateSource(cup, results);
    } else {
      // No changes were made, so we return the original.
      //
      logger.log(TreeLogger.SPAM, "No JavaScript native methods were found",
          null);
      return cup;
    }
  }

  /**
   * Static initialization: generate one call to 'JavaScriptHost.createNative()'
   * for each native method, to define the JavaScript code that will be invoked
   * later.
   */
  private char[] genInitializerBlock(String file, char[] source,
      JMethod[] methods) {

    String escapedFile = Jsni.escapeQuotesAndSlashes(file);

    StringBuffer sb = new StringBuffer();
    sb.append(" static {");
    for (int i = 0; i < methods.length; ++i) {
      JMethod method = methods[i];

      JsBlock jsniBody = (JsBlock) parsedJsByMethod.get(method);
      if (jsniBody == null) {
        // Not a JSNI method.
        //
        continue;
      }

      JParameter[] params = method.getParameters();
      String paramNamesArray = getParamNamesArrayExpr(params);

      final String jsTry = "try ";
      final String jsCatch = " catch (e) {\\n"
          + "  __static[\\\"@"
          + Jsni.JAVASCRIPTHOST_NAME
          + "::exceptionCaught"
          + "(ILjava/lang/String;Ljava/lang/String;)\\\"]"
          + "((e && e.number) || 0, (e && e.name) || null , (e && e.message) || null);\\n"
          + "}\\n";

      // Surround the original JS body statements with a try/catch so that
      // we can map JavaScript exceptions back into Java.
      // Note that the method body itself will print curly braces, so we don't
      // need them around the try/catch.
      //
      String js = jsTry + Jsni.generateEscapedJavaScriptForHostedMode(jsniBody)
          + jsCatch;
      String jsniSig = Jsni.getJsniSignature(method);

      // figure out starting line number
      int bodyStart = method.getBodyStart();
      int line = Jsni.countNewlines(source, 0, bodyStart) + 1;

      sb.append("  " + Jsni.JAVASCRIPTHOST_NAME + ".createNative(\""
          + escapedFile + "\", " + line + ", " + "\"@" + jsniSig + "\", "
          + paramNamesArray + ", \"" + js + "\");");
    }
    sb.append("}");
    return sb.toString().toCharArray();
  }

  /**
   * Create a legal Java method call that will result in a JSNI invocation.
   * 
   * @param method
   * @param expectedHeaderLines
   * @param expectedBodyLines
   * @return a String of the Java code to call a JSNI method, using
   *         JavaScriptHost.invokeNative*
   */
  private String genNonNativeVersionOfJsniMethod(JMethod method,
      int expectedHeaderLines, int expectedBodyLines) {
    StringBuffer sb = new StringBuffer();

    // Add extra lines at the start to match comments + declaration
    //
    for (int i = 0; i < expectedHeaderLines; ++i) {
      sb.append('\n');
    }

    String methodDecl = method.getReadableDeclaration(false, true, false,
        false, false);

    sb.append(methodDecl + " {");
    // wrap the call in a try-catch block
    sb.append("try {");

    // Write the Java call to the property invoke method, adding
    // downcasts where necessary.
    //
    JType returnType = method.getReturnType();
    boolean isJavaScriptObject = isJavaScriptObject(returnType);
    JPrimitiveType primType;
    if (isJavaScriptObject) {
      // Add a downcast from Handle to the originally-declared type.
      //
      String returnTypeName = returnType.getQualifiedSourceName();
      sb.append("return (" + returnTypeName + ")" + Jsni.JAVASCRIPTHOST_NAME
          + ".invokeNativeHandle");
    } else if (null != (primType = returnType.isPrimitive())) {
      // Primitives have special overloads.
      //
      char[] primTypeSuffix = primType.getSimpleSourceName().toCharArray();
      primTypeSuffix[0] = Character.toUpperCase(primTypeSuffix[0]);
      String invokeMethodName = "invokeNative" + String.valueOf(primTypeSuffix);
      if (primType != JPrimitiveType.VOID) {
        sb.append("return ");
      }
      sb.append(Jsni.JAVASCRIPTHOST_NAME);
      sb.append(".");
      sb.append(invokeMethodName);
    } else if (returnType == coreTypes.javaLangString) {
      sb.append("return ");
      sb.append(Jsni.JAVASCRIPTHOST_NAME);
      sb.append(".invokeNativeString");
    } else {
      // Some reference type.
      // We need to add a downcast to the originally-declared type.
      //
      String returnTypeName = returnType.getQualifiedSourceName();
      sb.append("return (");
      sb.append(returnTypeName);
      sb.append(")");
      sb.append(Jsni.JAVASCRIPTHOST_NAME);
      sb.append(".invokeNativeObject");
    }

    // Write the argument list for the invoke call.
    //
    sb.append("(\"@");
    String jsniSig = Jsni.getJsniSignature(method);
    sb.append(jsniSig);
    if (method.isStatic()) {
      sb.append("\", null, ");
    } else {
      sb.append("\", this, ");
    }

    if (isJavaScriptObject) {
      // Handle-oriented calls also need the return type as an argument.
      //
      String returnTypeName = returnType.getQualifiedSourceName();
      sb.append(returnTypeName);
      sb.append(".class, ");
    }

    // Build an array of classes that tells the invoker how to adapt the
    // incoming arguments for calling into JavaScript.
    //
    sb.append(Jsni.buildTypeList(method));
    sb.append(',');

    // Build an array containing the arguments based on the names of the
    // parameters.
    //
    sb.append(Jsni.buildArgList(method));
    sb.append(");");

    // Catch exceptions; rethrow if the exception is RTE or declared.
    sb.append("} catch (java.lang.Throwable __gwt_exception) {");
    sb.append("if (__gwt_exception instanceof java.lang.RuntimeException) throw (java.lang.RuntimeException) __gwt_exception;");
    JType[] throwTypes = method.getThrows();
    for (int i = 0; i < throwTypes.length; ++i) {
      String typeName = throwTypes[i].getQualifiedSourceName();
      sb.append("if (__gwt_exception instanceof " + typeName + ") throw (" + typeName
          + ") __gwt_exception;");
    }
    sb.append("throw new java.lang.RuntimeException(\"Undeclared checked exception thrown out of JavaScript; web mode behavior may differ.\", __gwt_exception);");
    sb.append("}");

    sb.append("}");

    // Add extra lines at the end to match JSNI body.
    //
    for (int i = 0; i < expectedBodyLines; ++i) {
      sb.append('\n');
    }

    return sb.toString();
  }

  private String getParamNamesArrayExpr(JParameter[] params) {
    StringBuffer sb = new StringBuffer();
    sb.append("new String[] {");
    for (int i = 0, n = params.length; i < n; ++i) {
      if (i > 0) {
        sb.append(", ");
      }

      JParameter param = params[i];
      sb.append('\"');
      sb.append(param.getName());
      sb.append('\"');
    }
    sb.append("}");
    return sb.toString();
  }

  private boolean isJavaScriptObject(JType type) {
    JClassType classType = type.isClass();
    if (classType == null) {
      return false;
    }

    if (classType.isAssignableTo(coreTypes.javaScriptObject)) {
      return true;
    } else {
      return false;
    }
  }

  private void rewriteCompilationUnit(TreeLogger logger, char[] source,
      List changes, CompilationUnitProvider cup)
      throws UnableToCompleteException {

    // Hit all the types in the compilation unit.
    //
    JClassType[] types = oracle.getTypesInCompilationUnit(cup);
    for (int i = 0; i < types.length; i++) {
      JClassType type = types[i];
      rewriteType(logger, source, changes, type);
    }
  }

  private void rewriteType(TreeLogger logger, char[] source, List changes,
      JClassType type) throws UnableToCompleteException {

    String loc = type.getCompilationUnit().getLocation();

    // Examine each method for JSNIness.
    //
    List patchedMethods = new ArrayList();
    JMethod[] methods = type.getMethods();
    for (int i = 0; i < methods.length; i++) {
      JMethod method = methods[i];
      if (method.isNative()) {
        Jsni.Interval interval = Jsni.findJsniSource(method);
        if (interval != null) {
          // The method itself needs to be replaced.
          //

          // Parse it.
          //
          String js = new String(source, interval.start, interval.end
              - interval.start);
          int startLine = Jsni.countNewlines(source, 0, interval.start) + 1;
          JsBlock body = Jsni.parseAsFunctionBody(logger, js, loc, startLine);

          // Remember this as being a valid JSNI method.
          //
          parsedJsByMethod.put(method, body);

          // Replace the method.
          final int declStart = method.getDeclStart();
          final int declEnd = method.getDeclEnd();

          int expectedHeaderLines = Jsni.countNewlines(source, declStart,
              interval.start);
          int expectedBodyLines = Jsni.countNewlines(source, interval.start,
              interval.end);
          String newDecl = genNonNativeVersionOfJsniMethod(method,
              expectedHeaderLines, expectedBodyLines);

          final char[] newSource = newDecl.toCharArray();
          changes.add(new Replacement(declStart, declEnd, newSource));
          patchedMethods.add(method);
        } else {
          // report error
          String msg = "No JavaScript body found for native method '" + method
              + "' in type '" + type + "'";
          logger.log(TreeLogger.ERROR, msg, null);
          throw new UnableToCompleteException();
        }
      }
    }

    if (!patchedMethods.isEmpty()) {
      JMethod[] patched = new JMethod[patchedMethods.size()];
      patched = (JMethod[]) patchedMethods.toArray(patched);

      TreeLogger branch = logger.branch(TreeLogger.SPAM, "Patched methods in '"
          + type.getQualifiedSourceName() + "'", null);

      for (int i = 0; i < patched.length; i++) {
        branch.log(TreeLogger.SPAM, patched[i].getReadableDeclaration(), null);
      }

      // Insert an initializer block immediately after the opening brace of the
      // class.
      //
      char[] block = genInitializerBlock(loc, source, patched);

      // If this is a non-static inner class, actually put the initializer block
      // in the first enclosing static or top-level class instead.
      while (type.getEnclosingType() != null && !type.isStatic()) {
        type = type.getEnclosingType();
      }

      int bodyStart = type.getBodyStart();
      changes.add(new Replacement(bodyStart, bodyStart, block));
    }
  }
}
