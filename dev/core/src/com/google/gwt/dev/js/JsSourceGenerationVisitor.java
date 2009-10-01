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
package com.google.gwt.dev.js;

import com.google.gwt.dev.js.ast.JsBlock;
import com.google.gwt.dev.js.ast.JsContext;
import com.google.gwt.dev.js.ast.JsProgram;
import com.google.gwt.dev.js.ast.JsProgramFragment;
import com.google.gwt.dev.js.ast.JsStatement;
import com.google.gwt.dev.util.TextOutput;

/**
 * Generates JavaScript source from an AST.
 */
public class JsSourceGenerationVisitor extends JsToStringGenerationVisitor {

  public JsSourceGenerationVisitor(TextOutput out) {
    super(out);
  }

  @Override
  public boolean visit(JsProgram x, JsContext<JsProgram> ctx) {
    // Descend naturally.
    return true;
  }

  @Override
  public boolean visit(JsProgramFragment x, JsContext<JsProgramFragment> ctx) {
    // Descend naturally.
    return true;
  }

  @Override
  public boolean visit(JsBlock x, JsContext<JsStatement> ctx) {
    printJsBlock(x, false, true);
    return false;
  }

}
