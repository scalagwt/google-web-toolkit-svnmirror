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
package com.google.gwt.dev.js.ast;

import com.google.gwt.dev.jjs.SourceInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the JavaScript new expression.
 */
public final class JsNew extends JsExpression implements HasArguments {

  private final List<JsExpression> args = new ArrayList<JsExpression>();

  private JsExpression ctorExpr;

  public JsNew(SourceInfo sourceInfo) {
    super(sourceInfo);
  }

  public List<JsExpression> getArguments() {
    return args;
  }

  public JsExpression getConstructorExpression() {
    return ctorExpr;
  }

  @Override
  public boolean hasSideEffects() {
    return true;
  }

  @Override
  public boolean isDefinitelyNotNull() {
    // Sadly, in JS it can be!
    // TODO: analysis could probably determine most instances cannot be null.
    return false;
  }

  @Override
  public boolean isDefinitelyNull() {
    return false;
  }

  public void setConstructorExpression(JsExpression ctorExpr) {
    this.ctorExpr = ctorExpr;
  }

  public void traverse(JsVisitor v, JsContext<JsExpression> ctx) {
    if (v.visit(this, ctx)) {
      ctorExpr = v.accept(ctorExpr);
      v.acceptList(args);
    }
    v.endVisit(this, ctx);
  }
}
