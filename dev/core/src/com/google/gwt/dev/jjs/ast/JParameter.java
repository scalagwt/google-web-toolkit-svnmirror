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
package com.google.gwt.dev.jjs.ast;

/**
 * Java method parameter definition.
 */
public class JParameter extends JVariable implements HasEnclosingMethod {

  private final JMethod enclosingMethod;

  JParameter(JProgram program, JSourceInfo info, String name, JType type,
      boolean isFinal, JMethod enclosingMethod) {
    super(program, info, name, type, isFinal);
    this.enclosingMethod = enclosingMethod;
  }

  public JMethod getEnclosingMethod() {
    return enclosingMethod;
  }

  public void traverse(JVisitor visitor, Context ctx) {
    if (visitor.visit(this, ctx)) {
    }
    visitor.endVisit(this, ctx);
  }
}
