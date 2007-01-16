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
package com.google.gwt.dev.js.ast;

/**
 * A JavaScript switch statement.
 */
public class JsSwitch extends JsStatement {

  private final JsSwitchMembers cases = new JsSwitchMembers();

  private JsExpression expr;

  public JsSwitch() {
  }

  public JsSwitchMembers getCases() {
    return cases;
  }

  public JsExpression getExpr() {
    return expr;
  }

  public void setExpr(JsExpression expr) {
    this.expr = expr;
  }

  public void traverse(JsVisitor v) {
    if (v.visit(this)) {
      expr.traverse(v);
      cases.traverse(v);
    }
    v.endVisit(this);
  }
}
