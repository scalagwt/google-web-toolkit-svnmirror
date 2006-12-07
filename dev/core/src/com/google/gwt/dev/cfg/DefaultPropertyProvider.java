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
package com.google.gwt.dev.cfg;

import com.google.gwt.dev.js.JsParser;
import com.google.gwt.dev.js.JsParserException;
import com.google.gwt.dev.js.ast.JsBlock;
import com.google.gwt.dev.js.ast.JsExprStmt;
import com.google.gwt.dev.js.ast.JsFunction;
import com.google.gwt.dev.js.ast.JsProgram;
import com.google.gwt.dev.js.ast.JsStatements;

import java.io.IOException;
import java.io.StringReader;

public class DefaultPropertyProvider extends PropertyProvider {

  public DefaultPropertyProvider(Property property) {
    super(property);
    String src = "function () {";
    src += "return parent.__gwt_getMetaProperty(\"";
    src += property.getName();
    src += "\"); }";
    setBody(parseFunction(src));
  }

  private JsBlock parseFunction(String jsniSrc) {
    Throwable caught = null;
    try {
      JsProgram jsPgm = new JsProgram();
      JsParser jsParser = new JsParser();
      StringReader r = new StringReader(jsniSrc);
      JsStatements stmts = jsParser.parse(jsPgm.getScope(), r, 1);
      JsFunction fn = (JsFunction) ((JsExprStmt) stmts.get(0)).getExpression();
      return fn.getBody();
    } catch (IOException e) {
      caught = e;
    } catch (JsParserException e) {
      caught = e;
    }
    throw new RuntimeException(
        "Internal error parsing source for default property provider", caught);
  }
}
