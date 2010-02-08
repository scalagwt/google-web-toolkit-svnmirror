/*
 * Copyright 2009 Google Inc.
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
package com.google.gwt.dev.jjs.impl.gflow.call;

import com.google.gwt.dev.jjs.ast.Context;
import com.google.gwt.dev.jjs.ast.JDeclaredType;
import com.google.gwt.dev.jjs.ast.JMethod;
import com.google.gwt.dev.jjs.ast.JMethodCall;
import com.google.gwt.dev.jjs.ast.JProgram;
import com.google.gwt.dev.jjs.ast.JVisitor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 */
public class CallGraphGenerator {
  private final class CollectOverridesVisitor extends JVisitor {
    private final CallGraphBuilder builder;

    private CollectOverridesVisitor(CallGraphBuilder builder) {
      this.builder = builder;
    }

    @Override
    public void endVisit(JMethod x, Context ctx) {
      JDeclaredType enclosingType = x.getEnclosingType();
      if (program.typeOracle.isInstantiatedType(enclosingType)) {
        for (JMethod method : program.typeOracle.getAllOverrides(x)) {
          addOverrider(method, x);
        }
      } 
    }

    @Override
    public boolean visit(JMethod x, Context ctx) {
      builder.addMethod(x);
      return true;
    }
  }

  private final class GeneratorVisitor extends JVisitor {
    private final CallGraphBuilder builder;
    private JMethod currentMethod;

    public GeneratorVisitor(CallGraphBuilder builder) {
      this.builder = builder;
    }

    @Override
    public void endVisit(JMethod x, Context ctx) {
      currentMethod = null;
    }

    @Override
    public void endVisit(JMethodCall x, Context ctx) {
      JMethod target = x.getTarget();
      builder.addCall(currentMethod, x, target);
      for (JMethod m : program.typeOracle.getAllOverrides(target)) {
        builder.addCall(currentMethod, x, m);
      }

      if (overriders.containsKey(target)) {
        for (JMethod m : overriders.get(target)) {
          builder.addCall(currentMethod, x, m);
        }
      }
      
      JMethod staticImpl = program.staticImplFor(target);
      if (staticImpl != null
          && staticImpl.getEnclosingType().getMethods().contains(staticImpl)) {
        // instance method is still alive.
        builder.addCall(currentMethod, x, staticImpl);
      }
    }

    @Override
    public boolean visit(JMethod x, Context ctx) {
      currentMethod = x;
      return true;
    }
  }

  public static void generate(JProgram program, CallGraphBuilder builder) {
    new CallGraphGenerator(program).generate(builder);
  }

  private static <T, V> void add(T target, V value, Map<T, Set<V>> map) {
    Set<V> set = map.get(target);
    if (set == null) {
      set = new HashSet<V>();
      map.put(target, set);
    }
    set.add(value);
  }

  private final JProgram program;
  private Map<JMethod, Set<JMethod>> overriders = new HashMap<JMethod, Set<JMethod>>();

  private CallGraphGenerator(JProgram program) {
    this.program = program;
  }

  private void addOverrider(JMethod method, JMethod overrider) {
    add(method, overrider, overriders);
  }

  private void generate(final CallGraphBuilder builder) {
    new CollectOverridesVisitor(builder).accept(program);
    new GeneratorVisitor(builder).accept(program);
  }
}
