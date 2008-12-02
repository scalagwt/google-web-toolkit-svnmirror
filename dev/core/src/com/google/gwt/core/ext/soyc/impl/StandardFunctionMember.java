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
package com.google.gwt.core.ext.soyc.impl;

import com.google.gwt.core.ext.soyc.FunctionMember;
import com.google.gwt.dev.js.ast.JsFunction;

/**
 * An implementation of FunctionMember.
 */
public class StandardFunctionMember extends AbstractMemberWithDependencies
    implements FunctionMember {

  private final String sourceName;

  /**
   * Constructed by {@link MemberFactory#get(JsFunction)}.
   */
  public StandardFunctionMember(MemberFactory factory, JsFunction function) {
    super(function.getSourceInfo());
    this.sourceName = function.getName().getIdent();
  }

  @Override
  public String getSourceName() {
    return sourceName;
  }

  /**
   * For debugging use only.
   */
  @Override
  public String toString() {
    return "FunctionMember " + sourceName;
  }
}
