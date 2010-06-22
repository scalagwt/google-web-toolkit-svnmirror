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

/**
 * Represents a JavaScript expression that references a name.
 */
public final class JsNameRef extends JsExpression implements CanBooleanEval,
    HasName {

  private boolean hasStaticRef;
  private String ident;
  private JsName name;
  private JsExpression qualifier;

  public JsNameRef(SourceInfo sourceInfo, JsName name) {
    super(sourceInfo.makeChild(JsNameRef.class, "Reference"));
    this.name = name;
    maybeUpdateSourceInfo();
  }

  public JsNameRef(SourceInfo sourceInfo, String ident) {
    super(sourceInfo);
    this.ident = ident;
  }

  public String getIdent() {
    return (name == null) ? ident : name.getIdent();
  }

  public JsName getName() {
    return name;
  }

  public JsExpression getQualifier() {
    return qualifier;
  }

  public String getShortIdent() {
    return (name == null) ? ident : name.getShortIdent();
  }

  @Override
  public SourceInfo getSourceInfo() {
    return maybeUpdateSourceInfo();
  }

  @Override
  public boolean hasSideEffects() {
    if (qualifier == null) {
      return false;
    }
    if (!qualifier.isDefinitelyNotNull()) {
      // Could trigger NPE.
      return true;
    }
    return qualifier.hasSideEffects();
  }

  public boolean isBooleanFalse() {
    return isDefinitelyNull();
  }

  public boolean isBooleanTrue() {
    return false;
  }

  @Override
  public boolean isDefinitelyNotNull() {
    // TODO: look for single-assignment of stuff from Java?
    return false;
  }

  @Override
  public boolean isDefinitelyNull() {
    if (name != null) {
      return (name.getEnclosing().getProgram().getUndefinedLiteral().getName() == name);
    }
    return false;
  }

  @Override
  public boolean isLeaf() {
    if (qualifier == null) {
      return true;
    } else {
      return false;
    }
  }

  public boolean isResolved() {
    return name != null;
  }

  public void resolve(JsName name) {
    this.name = name;
    this.ident = null;
  }

  public void setQualifier(JsExpression qualifier) {
    this.qualifier = qualifier;
  }

  public void traverse(JsVisitor v, JsContext<JsExpression> ctx) {
    if (v.visit(this, ctx)) {
      if (qualifier != null) {
        qualifier = v.accept(qualifier);
      }
    }
    v.endVisit(this, ctx);
  }

  /**
   * This corrects the JsNameRef's SourceInfo derivation when the JsName is
   * created with a JsName that has not yet had its static reference set. This
   * is the case in GenerateJavaScriptAST after the names and scopes visitor has
   * been run, but before the AST is fully realized.
   */
  private SourceInfo maybeUpdateSourceInfo() {
    SourceInfo toReturn = super.getSourceInfo();
    if (!hasStaticRef && name != null) {
      JsNode<?> staticRef = name.getStaticRef();
      if (staticRef != null) {
        toReturn.copyMissingCorrelationsFrom(name.getStaticRef().getSourceInfo());
        hasStaticRef = true;
      }
    }
    return toReturn;
  }
}
