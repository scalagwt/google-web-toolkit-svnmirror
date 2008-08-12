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
package com.google.gwt.dev.shell.rewrite;

import com.google.gwt.dev.asm.ClassAdapter;
import com.google.gwt.dev.asm.ClassVisitor;
import com.google.gwt.dev.asm.MethodAdapter;
import com.google.gwt.dev.asm.MethodVisitor;
import com.google.gwt.dev.asm.Opcodes;
import com.google.gwt.dev.asm.Type;
import com.google.gwt.dev.shell.CompilingClassLoader;

import java.lang.reflect.Method;

/**
 * Adds instructions to a class's static initializer to have its JSNI methods,
 * if any, injected by its {@link CompilingClassLoader}.
 */
public class InjectJsniRewriter extends ClassAdapter {

  /**
   * This will add a call to {@link CompilingClassLoader#injectJsniFor(Class)}
   * to the beginning of a wrapped method.
   */
  private class MyMethodVisitor extends MethodAdapter {

    public MyMethodVisitor(MethodVisitor v) {
      super(v);
    }

    @Override
    public void visitCode() {
      super.visitCode();

      /*
       * This sequence is based on a javap decompilation of
       * ((CompilingClassLoader)Foo.class.getClassLoader()).injectJsniFor(Foo.class)
       */

      // Get Foo.class
      visitLdcInsn(Type.getType("L" + className + ";"));

      // Invoke getClassLoader()
      try {
        Method getClassLoader = Class.class.getMethod("getClassLoader");
        visitMethodInsn(Opcodes.INVOKEVIRTUAL,
            Type.getInternalName(Class.class), getClassLoader.getName(),
            Type.getMethodDescriptor(getClassLoader));
      } catch (NoSuchMethodException e) {
        throw new RuntimeException("Class.getClassLoader() does not exist", e);
      }

      // Check the cast to CCL
      String cclInternalName = Type.getInternalName(CompilingClassLoader.class);
      visitTypeInsn(Opcodes.CHECKCAST, cclInternalName);

      // Push Foo.class as an argument
      visitLdcInsn(Type.getType("L" + className + ";"));

      // Invoke CCL.injectJsniFor()
      try {
        Method injectJsniFor = CompilingClassLoader.class.getMethod(
            "injectJsniFor", Class.class);
        visitMethodInsn(Opcodes.INVOKEVIRTUAL, cclInternalName,
            injectJsniFor.getName(), Type.getMethodDescriptor(injectJsniFor));
      } catch (NoSuchMethodException e) {
        throw new RuntimeException(
            "CompilingClassLoader.injectJsniFor does not exist", e);
      }
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
      super.visitMaxs(Math.max(maxStack, 2), maxLocals);
    }
  }

  private String className;
  private boolean hasClinit;

  public InjectJsniRewriter(ClassVisitor v) {
    super(v);
  }

  /**
   * Records the name of the class currently being generated and delegates to
   * super-implementation.
   */
  @Override
  public void visit(int version, int access, String name, String signature,
      String superName, String[] interfaces) {
    className = name;
    super.visit(version, access, name, signature, superName, interfaces);
  }

  /**
   * If not clinit has been seen in the current class, synthesize one.
   */
  @Override
  public void visitEnd() {
    if (!hasClinit) {
      MethodVisitor mv = visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V",
          null, null);
      mv.visitCode();
      mv.visitInsn(Opcodes.RETURN);
      mv.visitMaxs(0, 0);
      mv.visitEnd();
    }
    super.visitEnd();
  }

  /**
   * Wrap visits to the clinit function with a {@link MyMethodVisitor} and
   * delegate all others directly to super-implementation.
   */
  @Override
  public MethodVisitor visitMethod(int access, String name, String desc,
      String signature, String[] exceptions) {
    MethodVisitor mv = super.visitMethod(access, name, desc, signature,
        exceptions);
    if ("<clinit>".equals(name)) {
      hasClinit = true;
      return new MyMethodVisitor(mv);
    } else {
      return mv;
    }
  }

}
