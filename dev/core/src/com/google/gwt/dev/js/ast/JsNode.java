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

import com.google.gwt.dev.jjs.HasSourceInfo;
import com.google.gwt.dev.jjs.SourceInfo;
import com.google.gwt.dev.js.JsSourceGenerationVisitor;
import com.google.gwt.dev.js.JsToStringGenerationVisitor;
import com.google.gwt.dev.util.DefaultTextOutput;

/**
 * Base class for all JS AST elements.
 */
public abstract class JsNode implements JsVisitable, HasSourceInfo {

  public SourceInfo getSourceInfo() {
    // TODO: make this real
    return null;
  }

  // Causes source generation to delegate to the one visitor
  public final String toSource() {
    DefaultTextOutput out = new DefaultTextOutput(false);
    JsSourceGenerationVisitor v = new JsSourceGenerationVisitor(out);
    v.accept(this);
    return out.toString();
  }

  // Causes source generation to delegate to the one visitor
  public final String toString() {
    DefaultTextOutput out = new DefaultTextOutput(false);
    JsToStringGenerationVisitor v = new JsToStringGenerationVisitor(out);
    v.accept(this);
    return out.toString();
  }
}
