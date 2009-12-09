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

import com.google.gwt.dev.jjs.InternalCompilerException;

/**
 * A type including all the values in some other type except for
 * <code>null</code>.
 */
public class JNonNullType extends JReferenceType {

  private final JReferenceType ref;

  JNonNullType(JReferenceType ref) {
    super(ref.getSourceInfo(), ref.getName());
    assert ref.canBeNull();
    this.ref = ref;
  }

  @Override
  public boolean canBeNull() {
    return false;
  }

  @Override
  public String getClassLiteralFactoryMethod() {
    return ref.getClassLiteralFactoryMethod();
  }

  @Override
  public JClassType getSuperClass() {
    return ref.getSuperClass();
  }

  @Override
  public JReferenceType getUnderlyingType() {
    return ref;
  }

  public boolean isAbstract() {
    return ref.isAbstract();
  }

  public boolean isFinal() {
    return ref.isFinal();
  }

  @Override
  public void setSuperClass(JClassType superClass) {
    throw new InternalCompilerException("should not be called");
  }

  public void traverse(JVisitor visitor, Context ctx) {
    visitor.accept(ref);
  }
}
