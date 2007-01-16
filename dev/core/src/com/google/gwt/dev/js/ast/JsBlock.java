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

import java.util.Iterator;

/**
 * Represents a JavaScript block statement.
 */
public class JsBlock extends JsStatement {

  private final JsStatements stmts = new JsStatements();

  public JsBlock() {
  }

  public JsStatements getStatements() {
    return stmts;
  }

  public boolean isGlobalBlock() {
    return false;
  }

  public void traverse(JsVisitor v) {
    if (v.visit(this)) {
      for (Iterator iter = stmts.iterator(); iter.hasNext();) {
        JsStatement stmt = (JsStatement) iter.next();
        stmt.traverse(v);
      }
    }
    v.endVisit(this);
  }
}
