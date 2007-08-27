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
 * Java local variable definition.
 */
public class JLocal extends JVariable implements HasEnclosingMethod {

  private final JMethodBody enclosingMethodBody;

  JLocal(JProgram program, SourceInfo info, String name, JType type,
      boolean isFinal, JMethodBody enclosingMethodBody) {
    super(program, info, name, type, isFinal);
    this.enclosingMethodBody = enclosingMethodBody;
  }

  public JMethod getEnclosingMethod() {
    return enclosingMethodBody.method;
  }

  public void traverse(JVisitor visitor, Context ctx) {
    if (visitor.visit(this, ctx)) {
    }
    visitor.endVisit(this, ctx);
  }

}
