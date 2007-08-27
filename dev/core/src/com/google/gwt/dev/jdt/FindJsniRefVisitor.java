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
package com.google.gwt.dev.jdt;

import com.google.gwt.dev.jjs.InternalCompilerException;
import com.google.gwt.dev.js.JsParser;
import com.google.gwt.dev.js.JsParserException;
import com.google.gwt.dev.js.ast.JsContext;
import com.google.gwt.dev.js.ast.JsNameRef;
import com.google.gwt.dev.js.ast.JsProgram;
import com.google.gwt.dev.js.ast.JsStatements;
import com.google.gwt.dev.js.ast.JsVisitor;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;

import java.io.IOException;
import java.io.StringReader;
import java.util.Set;

/**
 * Walks the AST to find references to Java identifiers from within JSNI blocks.
 */
public class FindJsniRefVisitor extends ASTVisitor {

  private final Set/* <String> */jsniClasses;
  private final JsParser jsParser = new JsParser();
  private final JsProgram jsProgram = new JsProgram();

  public FindJsniRefVisitor(Set/* <String> */jsniClasses) {
    this.jsniClasses = jsniClasses;
  }

  public boolean visit(MethodDeclaration methodDeclaration, ClassScope scope) {
    if (!methodDeclaration.isNative()) {
      return false;
    }

    // Handle JSNI block
    char[] source = methodDeclaration.compilationResult().getCompilationUnit().getContents();
    String jsniCode = String.valueOf(source, methodDeclaration.bodyStart,
        methodDeclaration.bodyEnd - methodDeclaration.bodyStart + 1);
    int startPos = jsniCode.indexOf("/*-{");
    int endPos = jsniCode.lastIndexOf("}-*/");
    if (startPos < 0 || endPos < 0) {
      return false; // ignore the error
    }

    startPos += 3; // move up to open brace
    endPos += 1; // move past close brace

    jsniCode = jsniCode.substring(startPos, endPos);

    String syntheticFnHeader = "function(";
    boolean first = true;
    if (methodDeclaration.arguments != null) {
      for (int i = 0, c = methodDeclaration.arguments.length; i < c; ++i) {
        Argument arg = methodDeclaration.arguments[i];
        if (first) {
          first = false;
        } else {
          syntheticFnHeader += ',';
        }
        syntheticFnHeader += String.valueOf(arg.name);
      }
    }
    syntheticFnHeader += ')';
    StringReader sr = new StringReader(syntheticFnHeader + '\n' + jsniCode);
    try {
      // start at -1 to avoid counting our synthetic header
      JsStatements result = jsParser.parse(jsProgram.getScope(), sr, -1);
      new JsVisitor() {
        public void endVisit(JsNameRef x, JsContext ctx) {
          String ident = x.getIdent();
          if (ident.charAt(0) == '@') {
            String className = ident.substring(1, ident.indexOf(':'));
            jsniClasses.add(className);
          }
        }
      }.accept(result);
    } catch (IOException e) {
      throw new InternalCompilerException(
          "Internal error searching for JSNI references", e);
    } catch (JsParserException e) {
      // ignore, we only care about finding valid references
    }

    return false;
  }

}
