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
 * A JavaScript regular expression.
 */
public class JsRegExp extends JsExpression {

  private String flags;

  private String pattern;

  public JsRegExp() {
  }

  public String getFlags() {
    return flags;
  }

  public String getPattern() {
    return pattern;
  }

  public void setFlags(String suffix) {
    this.flags = suffix;
  }

  public void setPattern(String re) {
    this.pattern = re;
  }

  public void traverse(JsVisitor v) {
    v.visit(this);
    v.endVisit(this);
  }
}
