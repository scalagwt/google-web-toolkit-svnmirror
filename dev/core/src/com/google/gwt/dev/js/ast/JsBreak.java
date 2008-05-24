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
package com.google.gwt.dev.js.ast;

/**
 * Represents the JavaScript break statement.
 */
public final class JsBreak extends JsStatement {

  private final JsNameRef label;

  public JsBreak() {
    this(null);
  }

  public JsBreak(JsNameRef label) {
    this.label = label;
  }

  public JsNameRef getLabel() {
    return label;
  }

  public void traverse(JsVisitor v, JsContext<JsStatement> ctx) {
    v.visit(this, ctx);
    v.endVisit(this, ctx);
  }

  public boolean unconditionalControlBreak() {
    return true;
  }
}
