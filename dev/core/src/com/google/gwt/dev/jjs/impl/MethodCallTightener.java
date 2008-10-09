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
package com.google.gwt.dev.jjs.impl;

import com.google.gwt.dev.jjs.ast.Context;
import com.google.gwt.dev.jjs.ast.JArrayType;
import com.google.gwt.dev.jjs.ast.JClassType;
import com.google.gwt.dev.jjs.ast.JExpression;
import com.google.gwt.dev.jjs.ast.JInterfaceType;
import com.google.gwt.dev.jjs.ast.JMethod;
import com.google.gwt.dev.jjs.ast.JMethodCall;
import com.google.gwt.dev.jjs.ast.JModVisitor;
import com.google.gwt.dev.jjs.ast.JNullType;
import com.google.gwt.dev.jjs.ast.JProgram;
import com.google.gwt.dev.jjs.ast.JReferenceType;
import com.google.gwt.dev.jjs.ast.JType;

/**
 * Update polymorphic method calls to tighter bindings based on the type of the
 * qualifier. For a given polymorphic method call to a non-final target, see if
 * the static type of the qualifer would let us target an override instead.
 * 
 * This is possible because the qualifier might have been tightened by
 * {@link com.google.gwt.dev.jjs.impl.TypeTightener}.
 */
public class MethodCallTightener {

  /**
   * Updates polymorphic method calls to tighter bindings based on the type of
   * the qualifier.
   */
  public class MethodCallTighteningVisitor extends JModVisitor {

    @Override
    public void endVisit(JMethodCall x, Context ctx) {
      JMethod method = x.getTarget();
      JExpression instance = x.getInstance();

      // The method call is already known statically
      if (!x.canBePolymorphic()) {
        return;
      }

      JType instanceType = instance.getType();
      JReferenceType enclosingType = method.getEnclosingType();

      if (instanceType == enclosingType
          || instanceType instanceof JInterfaceType) {
        // This method call is as tight as it can be for the type of the
        // qualifier
        return;
      }

      if (instanceType instanceof JArrayType) {
        // shouldn't get here; arrays don't have extra methods
        return;
      }

      if (instanceType instanceof JNullType) {
        // TypeTightener will handle this case
        return;
      }

      assert (instanceType instanceof JClassType);

      /*
       * Search myself and all my super types to find a tighter implementation
       * of the called method, if possible.
       */
      JMethod foundMethod = null;
      JClassType type;
      outer : for (type = (JClassType) instanceType; type != null
          && type != enclosingType; type = type.extnds) {
        for (int i = 0; i < type.methods.size(); ++i) {
          JMethod methodIt = type.methods.get(i);
          if (methodOverrides(methodIt, method)) {
            foundMethod = methodIt;
            break outer;
          }
        }
      }

      if (foundMethod == null) {
        return;
      }

      /*
       * Replace the call to the original method with a call to the same method
       * on the tighter type.
       */
      JMethodCall call = new JMethodCall(program, x.getSourceInfo(),
          x.getInstance(), foundMethod);
      call.getArgs().addAll(x.getArgs());
      ctx.replaceMe(call);
    }

    /**
     * Check whether <code>subMethod</code> overrides <code>supMethod</code>.
     * For the purposes of this method, indirect overrides are considered
     * overrides. For example, if method A.m overrides B.m, and B.m overrides
     * C.m, then A.m is considered to override C.m. Additionally, implementing
     * an interface is considered
     * <q>overriding</q>
     * for the purposes of this method.
     * 
     */
    private boolean methodOverrides(JMethod subMethod, JMethod supMethod) {
      if (subMethod.params.size() != supMethod.params.size()) {
        // short cut: check the number of parameters
        return false;
      }

      if (!subMethod.getName().equals(supMethod.getName())) {
        // short cut: check the method names
        return false;
      }

      // long way: get all overrides and see if supMethod is included
      return program.typeOracle.getAllOverrides(subMethod).contains(supMethod);
    }
  }

  public static boolean exec(JProgram program) {
    return new MethodCallTightener(program).execImpl();
  }

  private final JProgram program;

  private MethodCallTightener(JProgram program) {
    this.program = program;
  }

  private boolean execImpl() {
    MethodCallTighteningVisitor tightener = new MethodCallTighteningVisitor();
    tightener.accept(program);
    return tightener.didChange();
  }

}
