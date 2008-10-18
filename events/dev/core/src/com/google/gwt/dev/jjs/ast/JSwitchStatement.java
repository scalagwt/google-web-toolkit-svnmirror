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
package com.google.gwt.dev.jjs.ast;

import com.google.gwt.dev.jjs.SourceInfo;

/**
 * Java switch statement.
 */
public class JSwitchStatement extends JStatement {

  private final JBlock body;
  private JExpression expr;

  public JSwitchStatement(JProgram program, SourceInfo info, JExpression expr,
      JBlock body) {
    super(program, info);
    this.expr = expr;
    this.body = body;
  }

  public JBlock getBody() {
    return body;
  }

  public JExpression getExpr() {
    return expr;
  }

  public void traverse(JVisitor visitor, Context ctx) {
    if (visitor.visit(this, ctx)) {
      expr = visitor.accept(expr);
      visitor.accept(body);
    }
    visitor.endVisit(this, ctx);
  }

}
