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
package com.google.gwt.dev.jjs.ast;

import com.google.gwt.dev.jjs.SourceInfo;
import com.google.gwt.dev.util.StringInterner;
import com.google.gwt.dev.util.collect.Lists;

import java.util.List;

/**
 * Base class for any storage location.
 */
public abstract class JVariable extends JNode implements CanBeSetFinal,
    CanHaveInitializer, HasAnnotations, HasName, HasType {

  protected List<JAnnotation> annotations = Lists.create();
  protected JDeclarationStatement declStmt = null;
  private boolean isFinal;
  private final String name;
  private JType type;

  JVariable(SourceInfo info, String name, JType type, boolean isFinal) {
    super(info);
    assert type != null;
    this.name = StringInterner.get().intern(name);
    this.type = type;
    this.isFinal = isFinal;
  }

  public void addAnnotation(JAnnotation annotation) {
    annotations = Lists.add(annotations, annotation);
  }

  public JAnnotation findAnnotation(String className) {
    return JAnnotation.findAnnotation(this, className);
  }

  public List<JAnnotation> getAnnotations() {
    return Lists.normalizeUnmodifiable(annotations);
  }

  public JLiteral getConstInitializer() {
    JExpression initializer = getInitializer();
    if (isFinal() && initializer instanceof JLiteral) {
      return (JLiteral) initializer;
    }
    return null;
  }

  public JDeclarationStatement getDeclarationStatement() {
    return declStmt;
  }

  public JExpression getInitializer() {
    if (declStmt != null) {
      return declStmt.getInitializer();
    }
    return null;
  }

  public String getName() {
    return name;
  }

  public JType getType() {
    return type;
  }

  public boolean hasInitializer() {
    return declStmt != null;
  }

  public boolean isFinal() {
    return isFinal;
  }

  public void setFinal() {
    isFinal = true;
  }

  public void setType(JType newType) {
    assert newType != null;
    type = newType;
  }

}
