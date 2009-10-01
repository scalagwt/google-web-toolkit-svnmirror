/*
 * Copyright 2009 Google Inc.
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
 * An AST node whose evaluation results in the string name of its node.
 */
public class JNameOf extends JExpression {

  private final HasName node;
  private final JType stringType;

  public JNameOf(SourceInfo info, JProgram program, HasName node) {
    super(info);
    this.node = node;
    stringType = program.getTypeJavaLangString();
  }

  public HasName getNode() {
    return node;
  }

  public JType getType() {
    return stringType;
  }

  @Override
  public boolean hasSideEffects() {
    return false;
  }

  public void traverse(JVisitor visitor, Context ctx) {
    if (visitor.visit(this, ctx)) {
      // Intentionally not visiting referenced node
    }
    visitor.endVisit(this, ctx);
  }

}
