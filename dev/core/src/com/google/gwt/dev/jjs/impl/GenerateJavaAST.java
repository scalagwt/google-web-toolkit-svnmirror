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
package com.google.gwt.dev.jjs.impl;

import com.google.gwt.dev.javac.JsniCollector;
import com.google.gwt.dev.jjs.HasSourceInfo;
import com.google.gwt.dev.jjs.InternalCompilerException;
import com.google.gwt.dev.jjs.JJSOptions;
import com.google.gwt.dev.jjs.SourceInfo;
import com.google.gwt.dev.jjs.SourceOrigin;
import com.google.gwt.dev.jjs.ast.Context;
import com.google.gwt.dev.jjs.ast.HasAnnotations;
import com.google.gwt.dev.jjs.ast.JAnnotation;
import com.google.gwt.dev.jjs.ast.JAnnotation.Property;
import com.google.gwt.dev.jjs.ast.JAnnotationArgument;
import com.google.gwt.dev.jjs.ast.JArrayLength;
import com.google.gwt.dev.jjs.ast.JArrayRef;
import com.google.gwt.dev.jjs.ast.JArrayType;
import com.google.gwt.dev.jjs.ast.JAssertStatement;
import com.google.gwt.dev.jjs.ast.JBinaryOperation;
import com.google.gwt.dev.jjs.ast.JBinaryOperator;
import com.google.gwt.dev.jjs.ast.JBlock;
import com.google.gwt.dev.jjs.ast.JBooleanLiteral;
import com.google.gwt.dev.jjs.ast.JBreakStatement;
import com.google.gwt.dev.jjs.ast.JCaseStatement;
import com.google.gwt.dev.jjs.ast.JCastOperation;
import com.google.gwt.dev.jjs.ast.JCharLiteral;
import com.google.gwt.dev.jjs.ast.JClassLiteral;
import com.google.gwt.dev.jjs.ast.JClassType;
import com.google.gwt.dev.jjs.ast.JConditional;
import com.google.gwt.dev.jjs.ast.JConstructor;
import com.google.gwt.dev.jjs.ast.JContinueStatement;
import com.google.gwt.dev.jjs.ast.JDeclarationStatement;
import com.google.gwt.dev.jjs.ast.JDeclaredType;
import com.google.gwt.dev.jjs.ast.JDoStatement;
import com.google.gwt.dev.jjs.ast.JDoubleLiteral;
import com.google.gwt.dev.jjs.ast.JEnumField;
import com.google.gwt.dev.jjs.ast.JEnumType;
import com.google.gwt.dev.jjs.ast.JExpression;
import com.google.gwt.dev.jjs.ast.JExpressionStatement;
import com.google.gwt.dev.jjs.ast.JField;
import com.google.gwt.dev.jjs.ast.JField.Disposition;
import com.google.gwt.dev.jjs.ast.JFieldRef;
import com.google.gwt.dev.jjs.ast.JFloatLiteral;
import com.google.gwt.dev.jjs.ast.JForStatement;
import com.google.gwt.dev.jjs.ast.JIfStatement;
import com.google.gwt.dev.jjs.ast.JInstanceOf;
import com.google.gwt.dev.jjs.ast.JIntLiteral;
import com.google.gwt.dev.jjs.ast.JInterfaceType;
import com.google.gwt.dev.jjs.ast.JLabel;
import com.google.gwt.dev.jjs.ast.JLabeledStatement;
import com.google.gwt.dev.jjs.ast.JLiteral;
import com.google.gwt.dev.jjs.ast.JLocal;
import com.google.gwt.dev.jjs.ast.JLocalRef;
import com.google.gwt.dev.jjs.ast.JLongLiteral;
import com.google.gwt.dev.jjs.ast.JMethod;
import com.google.gwt.dev.jjs.ast.JMethodBody;
import com.google.gwt.dev.jjs.ast.JMethodCall;
import com.google.gwt.dev.jjs.ast.JModVisitor;
import com.google.gwt.dev.jjs.ast.JNewArray;
import com.google.gwt.dev.jjs.ast.JNewInstance;
import com.google.gwt.dev.jjs.ast.JNode;
import com.google.gwt.dev.jjs.ast.JNonNullType;
import com.google.gwt.dev.jjs.ast.JParameter;
import com.google.gwt.dev.jjs.ast.JParameterRef;
import com.google.gwt.dev.jjs.ast.JPostfixOperation;
import com.google.gwt.dev.jjs.ast.JPrefixOperation;
import com.google.gwt.dev.jjs.ast.JPrimitiveType;
import com.google.gwt.dev.jjs.ast.JProgram;
import com.google.gwt.dev.jjs.ast.JReferenceType;
import com.google.gwt.dev.jjs.ast.JReturnStatement;
import com.google.gwt.dev.jjs.ast.JStatement;
import com.google.gwt.dev.jjs.ast.JStringLiteral;
import com.google.gwt.dev.jjs.ast.JSwitchStatement;
import com.google.gwt.dev.jjs.ast.JThisRef;
import com.google.gwt.dev.jjs.ast.JThrowStatement;
import com.google.gwt.dev.jjs.ast.JTryStatement;
import com.google.gwt.dev.jjs.ast.JType;
import com.google.gwt.dev.jjs.ast.JTypeOracle;
import com.google.gwt.dev.jjs.ast.JUnaryOperator;
import com.google.gwt.dev.jjs.ast.JVariable;
import com.google.gwt.dev.jjs.ast.JVariableRef;
import com.google.gwt.dev.jjs.ast.JWhileStatement;
import com.google.gwt.dev.jjs.ast.js.JsniFieldRef;
import com.google.gwt.dev.jjs.ast.js.JsniMethodBody;
import com.google.gwt.dev.jjs.ast.js.JsniMethodRef;
import com.google.gwt.dev.js.ast.JsContext;
import com.google.gwt.dev.js.ast.JsExpression;
import com.google.gwt.dev.js.ast.JsModVisitor;
import com.google.gwt.dev.js.ast.JsNameRef;
import com.google.gwt.dev.util.JsniRef;
import com.google.gwt.dev.util.collect.Lists;
import com.google.gwt.dev.util.collect.Maps;
import com.google.gwt.dev.util.log.speedtracer.CompilerEventType;
import com.google.gwt.dev.util.log.speedtracer.SpeedTracerLogger;
import com.google.gwt.dev.util.log.speedtracer.SpeedTracerLogger.Event;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.AND_AND_Expression;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.ArrayAllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.ArrayInitializer;
import org.eclipse.jdt.internal.compiler.ast.ArrayReference;
import org.eclipse.jdt.internal.compiler.ast.AssertStatement;
import org.eclipse.jdt.internal.compiler.ast.Assignment;
import org.eclipse.jdt.internal.compiler.ast.BinaryExpression;
import org.eclipse.jdt.internal.compiler.ast.Block;
import org.eclipse.jdt.internal.compiler.ast.BreakStatement;
import org.eclipse.jdt.internal.compiler.ast.CaseStatement;
import org.eclipse.jdt.internal.compiler.ast.CastExpression;
import org.eclipse.jdt.internal.compiler.ast.ClassLiteralAccess;
import org.eclipse.jdt.internal.compiler.ast.CombinedBinaryExpression;
import org.eclipse.jdt.internal.compiler.ast.CompoundAssignment;
import org.eclipse.jdt.internal.compiler.ast.ConditionalExpression;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ContinueStatement;
import org.eclipse.jdt.internal.compiler.ast.DoStatement;
import org.eclipse.jdt.internal.compiler.ast.EmptyStatement;
import org.eclipse.jdt.internal.compiler.ast.EqualExpression;
import org.eclipse.jdt.internal.compiler.ast.ExplicitConstructorCall;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.eclipse.jdt.internal.compiler.ast.ForStatement;
import org.eclipse.jdt.internal.compiler.ast.ForeachStatement;
import org.eclipse.jdt.internal.compiler.ast.IfStatement;
import org.eclipse.jdt.internal.compiler.ast.Initializer;
import org.eclipse.jdt.internal.compiler.ast.InstanceOfExpression;
import org.eclipse.jdt.internal.compiler.ast.LabeledStatement;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.NullLiteral;
import org.eclipse.jdt.internal.compiler.ast.OR_OR_Expression;
import org.eclipse.jdt.internal.compiler.ast.PostfixExpression;
import org.eclipse.jdt.internal.compiler.ast.PrefixExpression;
import org.eclipse.jdt.internal.compiler.ast.QualifiedAllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedSuperReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedThisReference;
import org.eclipse.jdt.internal.compiler.ast.ReturnStatement;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.SuperReference;
import org.eclipse.jdt.internal.compiler.ast.SwitchStatement;
import org.eclipse.jdt.internal.compiler.ast.SynchronizedStatement;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.ast.ThrowStatement;
import org.eclipse.jdt.internal.compiler.ast.TryStatement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.UnaryExpression;
import org.eclipse.jdt.internal.compiler.ast.WhileStatement;
import org.eclipse.jdt.internal.compiler.impl.BooleanConstant;
import org.eclipse.jdt.internal.compiler.impl.ByteConstant;
import org.eclipse.jdt.internal.compiler.impl.CharConstant;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.impl.DoubleConstant;
import org.eclipse.jdt.internal.compiler.impl.FloatConstant;
import org.eclipse.jdt.internal.compiler.impl.IntConstant;
import org.eclipse.jdt.internal.compiler.impl.LongConstant;
import org.eclipse.jdt.internal.compiler.impl.ShortConstant;
import org.eclipse.jdt.internal.compiler.impl.StringConstant;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.ElementValuePair;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.NestedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.SyntheticArgumentBinding;
import org.eclipse.jdt.internal.compiler.lookup.SyntheticMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.compiler.lookup.VariableBinding;
import org.eclipse.jdt.internal.compiler.util.Util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This is the big kahuna where most of the nitty gritty of creating our AST
 * happens. BuildTypeMap must have already run so we have valid mappings between
 * JDT nodes and our already-created AST nodes.
 */
public class GenerateJavaAST {

  /**
   * Visit the JDT AST and produce our own AST into the passed-in TypeMap's
   * JProgram. By the end of this pass, the produced AST should contain every
   * piece of information we'll ever need about the code. The JDT nodes should
   * never again be referenced after this.
   * 
   * This is implemented as a reflective visitor for JDT's AST. The advantage of
   * doing it reflectively is that if we run into any JDT nodes we can't handle,
   * we'll automatically throw an exception. If we had subclassed
   * {@link org.eclipse.jdt.internal.compiler.ast.ASTNode} we'd have to override
   * every single method and explicitly throw an exception to get the same
   * behavior.
   * 
   * NOTE ON JDT FORCED OPTIMIZATIONS - If JDT statically determines that a
   * section of code in unreachable, it won't fully resolve that section of
   * code. This invalid-state code causes us major problems. As a result, we
   * have to optimize out those dead blocks early and never try to translate
   * them to our AST.
   */
  // Reflective invocation causes unused warnings.
  @SuppressWarnings("unused")
  private static class JavaASTGenerationVisitor {

    /**
     * Used to cache {@link Method} lookups.
     */
    private static class MethodKey {
      private Class<? extends Object> childClass;
      private String name;

      public MethodKey(String name, Class<? extends Object> childClass) {
        this.name = name;
        this.childClass = childClass;
      }

      @Override
      public boolean equals(Object obj) {
        if (obj instanceof MethodKey) {
          MethodKey otherKey = (MethodKey) obj;
          return name.equals(otherKey.name)
              && childClass.equals(otherKey.childClass);
        }
        return super.equals(obj);
      }

      @Override
      public int hashCode() {
        return name.hashCode() + (101 * childClass.hashCode());
      }
    }

    /**
     * Used to cache {@link Method} lookups.
     */
    private static class MethodValue {
      private final NoSuchMethodException ex;
      private final Method method;

      public MethodValue(Method method) {
        this.method = method;
        this.ex = null;
      }

      public MethodValue(NoSuchMethodException ex) {
        this.ex = ex;
        this.method = null;
      }

      public Method getMethod() throws NoSuchMethodException {
        if (this.ex != null) {
          throw (ex);
        }
        return method;
      }
    }

    /**
     * The literal for the JLS identifier that represents the length field on an
     * array.
     */
    private static final String ARRAY_LENGTH_FIELD = "length";

    private static InternalCompilerException translateException(JNode node,
        Throwable e) {
      if (e instanceof VirtualMachineError) {
        // Always rethrow VM errors (an attempt to wrap may fail).
        throw (VirtualMachineError) e;
      }
      InternalCompilerException ice;
      if (e instanceof InternalCompilerException) {
        ice = (InternalCompilerException) e;
        ice.addNode(node);
      } else {
        ice = new InternalCompilerException(node,
            "Error constructing Java AST", e);
      }
      return ice;
    }

    private final AutoboxUtils autoboxUtils;

    private JDeclaredType currentClass;

    private ClassScope currentClassScope;

    private String currentFileName;

    private JMethod currentMethod;

    private JMethodBody currentMethodBody;

    private MethodScope currentMethodScope;

    private Map<JField, JParameter> currentOuterThisRefParams;

    private int[] currentSeparatorPositions;

    private final boolean disableClassMetadata;

    private final boolean enableAsserts;

    private final Map<JsniMethodBody, AbstractMethodDeclaration> jsniMethodMap = new HashMap<JsniMethodBody, AbstractMethodDeclaration>();

    private final Map<JMethod, Map<String, JLabel>> labelMap = new IdentityHashMap<JMethod, Map<String, JLabel>>();

    private final Map<MethodKey, MethodValue> methodCache = new HashMap<MethodKey, MethodValue>();

    private final JProgram program;

    private final TypeMap typeMap;

    public JavaASTGenerationVisitor(TypeMap typeMap, JProgram program,
        JJSOptions options) {
      this.typeMap = typeMap;
      this.program = program;
      this.enableAsserts = options.isEnableAssertions();

      /*
       * TODO: Determine if this should be controlled by a compiler flag or a
       * module property.
       */
      this.disableClassMetadata = options.isClassMetadataDisabled();
      autoboxUtils = new AutoboxUtils(program);
    }

    /**
     * <p>
     * Add a bridge method to <code>clazzBinding</code> for any method it
     * inherits that implements an interface method but that has a different
     * erased signature from the interface method.
     * </p>
     * 
     * <p>
     * The need for these bridges was pointed out in issue 3064. The goal is
     * that virtual method calls through an interface type are translated to
     * JavaScript that will function correctly. If the interface signature
     * matches the signature of the implementing method, then nothing special
     * needs to be done. If they are different, due to the use of generics, then
     * GenerateJavaScriptAST is careful to do the right thing. There is a
     * remaining case, though, that GenerateJavaScriptAST is not in a good
     * position to fix: a method could be inherited from a superclass, used to
     * implement an interface method that has a different type signature, and
     * does not have the interface method in its list of overrides. In that
     * case, a bridge method should be added that overrides the interface method
     * and then calls the implementation method.
     * </p>
     * 
     * <p>
     * This method should only be called once all regular, non-bridge methods
     * have been installed on the GWT types.
     * </p>
     */
    public void addBridgeMethods(SourceTypeBinding clazzBinding) {
      if (clazzBinding.isInterface()) {
        // Only add bridges in classes, to simplify matters.
        return;
      }

      JClassType clazz = (JClassType) typeMap.get(clazzBinding);

      /*
       * The JDT adds bridge methods in all the places GWT needs them. Look
       * through the bridge methods the JDT added.
       */
      if (clazzBinding.syntheticMethods() != null) {
        for (SyntheticMethodBinding synthmeth : clazzBinding.syntheticMethods()) {
          if (synthmeth.purpose == SyntheticMethodBinding.BridgeMethod
              && !synthmeth.isStatic()) {
            JMethod implmeth = (JMethod) typeMap.get(synthmeth.targetMethod);

            createBridgeMethod(clazz, synthmeth, implmeth);
          }
        }
      }
    }

    public void processEnumType(JEnumType type) {
      // Generate the synthetic values() method.
      JField valuesField = null;
      for (JMethod method : type.getMethods()) {
        currentMethod = method;
        if ("values".equals(method.getName())) {
          if (method.getParams().size() != 0) {
            continue;
          }
          currentMethodBody = (JMethodBody) method.getBody();
          valuesField = writeEnumValuesMethod(type);
        }
        currentMethodBody = null;
        currentMethod = null;
      }
      // Generate the synthetic valueOf() method.
      if (isScript(program)) {
        for (JMethod method : type.getMethods()) {
          currentMethod = method;
          if ("valueOf".equals(method.getName())) {
            if (method.getParams().size() != 1) {
              continue;
            }
            if (method.getParams().get(0).getType() != program.getTypeJavaLangString()) {
              continue;
            }
            currentMethodBody = (JMethodBody) method.getBody();
            writeEnumValueOfMethod(type, valuesField);
          }
          currentMethodBody = null;
          currentMethod = null;
        }
      }
    }

    /**
     * We emulate static initializers and instance initializers as methods. As
     * in other cases, this gives us: simpler AST, easier to optimize, more like
     * output JavaScript.
     */
    public void processType(TypeDeclaration x) {
      currentClass = (JDeclaredType) typeMap.get(x.binding);
      processHasAnnotations(currentClass, x.annotations);
      try {
        currentClassScope = x.scope;
        currentSeparatorPositions = x.compilationResult.lineSeparatorPositions;
        currentFileName = String.valueOf(x.compilationResult.fileName);

        /*
         * Make clinits chain to super class (JDT doesn't write code to do
         * this). Call super class $clinit; $clinit is always in position 0.
         */
        if (currentClass.getSuperClass() != null) {
          JMethod myClinit = currentClass.getMethods().get(0);
          JMethod superClinit = currentClass.getSuperClass().getMethods().get(0);
          JMethodCall superClinitCall = new JMethodCall(
              myClinit.getSourceInfo(), null, superClinit);
          JMethodBody body = (JMethodBody) myClinit.getBody();
          body.getBlock().addStmt(0, superClinitCall.makeStatement());
        }

        if (x.fields != null) {
          // Process fields
          for (int i = 0, n = x.fields.length; i < n; ++i) {
            FieldDeclaration fieldDeclaration = x.fields[i];
            if (fieldDeclaration.isStatic()) {
              // clinit
              currentMethod = currentClass.getMethods().get(0);
              currentMethodBody = (JMethodBody) currentMethod.getBody();
              currentMethodScope = x.staticInitializerScope;
            } else {
              // init
              currentMethod = currentClass.getMethods().get(1);
              currentMethodBody = (JMethodBody) currentMethod.getBody();
              currentMethodScope = x.initializerScope;
            }

            if (fieldDeclaration instanceof Initializer) {
              assert (currentClass instanceof JClassType);
              processInitializer((Initializer) fieldDeclaration);
            } else {
              processField(fieldDeclaration);
            }
          }
        }

        currentMethodScope = null;
        currentMethod = null;

        if (x.methods != null) {
          // Process methods
          for (int i = 0, n = x.methods.length; i < n; ++i) {
            if (x.methods[i].isConstructor()) {
              assert (currentClass instanceof JClassType);
              processConstructor((ConstructorDeclaration) x.methods[i]);
            } else if (x.methods[i].isClinit()) {
              // nothing to do
            } else {
              processMethod(x.methods[i]);
            }
          }
        }

        // Write the body of the getClass() override.
        if (currentClass instanceof JClassType
            && currentClass != program.getTypeJavaLangObject()) {
          JMethod method = currentClass.getMethods().get(2);
          assert ("getClass".equals(method.getName()));

          if (program.isJavaScriptObject(currentClass)
              && currentClass != program.getJavaScriptObject()) {
            // Just use JavaScriptObject's implementation for all subclasses.
            currentClass.getMethods().remove(2);
          } else {
            tryFindUpRefs(method);
            SourceInfo info = method.getSourceInfo();
            if (isScript(program)
                && currentClass == program.getIndexedType("Array")) {
              // Special implementation: return this.arrayClass
              implementMethod(
                  method,
                  new JFieldRef(info, new JThisRef(info,
                      (JClassType) currentClass),
                      program.getIndexedField("Array.arrayClass"), currentClass));
            } else {
              implementMethod(method, new JClassLiteral(info.makeChild(),
                  currentClass));
            }
          }
        }

        // Reimplement GWT.isClient(), GWT.isProdMode(), GWT.isScript().
        if (currentClass == program.getIndexedType("GWT")) {
          JMethod method = program.getIndexedMethod("GWT.isClient");
          implementMethod(method, program.getLiteralBoolean(true));

          method = program.getIndexedMethod("GWT.isProdMode");
          implementMethod(method, program.getLiteralBoolean(true));

          method = program.getIndexedMethod("GWT.isScript");
          implementMethod(method, program.getLiteralBoolean(isScript(program)));
        }

        // Implement various methods on Class
        if (currentClass == program.getTypeJavaLangClass()) {
          JMethod method = program.getIndexedMethod("Class.desiredAssertionStatus");
          implementMethod(method, program.getLiteralBoolean(enableAsserts));

          if (disableClassMetadata) {
            JMethod isMetadataEnabledMethod = program.getIndexedMethod("Class.isClassMetadataEnabled");
            implementMethod(isMetadataEnabledMethod,
                program.getLiteralBoolean(false));
          }
        }

        if (currentClass instanceof JEnumType) {
          processEnumType((JEnumType) currentClass);
        }

        currentClassScope = null;
        currentClass = null;
        currentSeparatorPositions = null;
        currentFileName = null;
      } catch (Throwable e) {
        throw translateException(currentClass, e);
      }
    }

    /**
     * This is the guts of the "reflective" part of this visitor. Try to find a
     * "process" method that exactly matches the run-time type of the argument.
     */
    protected JNode dispatch(String name, Object child) {
      if (child == null) {
        return null;
      }

      try {
        Method method = getCachedMethod(name, child.getClass());
        return (JNode) method.invoke(this, child);
      } catch (Throwable e) {
        if (e instanceof InvocationTargetException) {
          e = ((InvocationTargetException) e).getTargetException();
        }
        throw translateException(child, e);
      }
    }

    /**
     * Process an Expression type node reflectively; must return a JExpression.
     */
    protected JExpression dispProcessExpression(Expression x) {
      /*
       * Note that we always prefer a JDT-computed constant value to the actual
       * written expression. (Let's hope JDT is always right.) This means we
       * don't have to write processExpression methods for the numerous JDT
       * literal nodes because they ALWAYS have a constant value.
       */
      JExpression result = null;
      if (x != null && x.constant != null
          && x.constant != Constant.NotAConstant) {
        result = (JExpression) dispatch("processConstant", x.constant);
      }

      if (result == null) {
        // The expression was not a constant, so use the general logic.
        result = (JExpression) dispatch("processExpression", x);
      }

      // Check if we need to box the resulting expression.
      if (x != null) {
        if ((x.implicitConversion & TypeIds.BOXING) != 0) {
          result = autoboxUtils.box(result, implicitConversionTargetType(x));
        } else if ((x.implicitConversion & TypeIds.UNBOXING) != 0) {
          // This code can actually leave an unbox operation in
          // an lvalue position, for example ++(x.intValue()).
          // Such trees are cleaned up in FixAssignmentToUnbox.
          JType typeToUnbox = (JType) typeMap.get(x.resolvedType);
          if (!(typeToUnbox instanceof JClassType)) {
            throw new InternalCompilerException(result,
                "Attempt to unbox a non-class type: " + typeToUnbox.getName(),
                null);
          }

          result = unbox(result, (JClassType) typeToUnbox);
        }
      }
      return result;
    }

    /**
     * Process an Statement type node reflectively; must return a JStatement.
     */
    protected JStatement dispProcessStatement(Statement x) {
      JStatement stmt;
      if (x instanceof Expression) {
        JExpression expr = dispProcessExpression((Expression) x);
        if (expr == null) {
          return null;
        }
        stmt = expr.makeStatement();
      } else {
        stmt = (JStatement) dispatch("processStatement", x);
      }
      return stmt;
    }

    Map<JsniMethodBody, AbstractMethodDeclaration> getJsniMethodMap() {
      return jsniMethodMap;
    }

    JBooleanLiteral processConstant(BooleanConstant x) {
      return program.getLiteralBoolean(x.booleanValue());
    }

    JIntLiteral processConstant(ByteConstant x) {
      return program.getLiteralInt(x.byteValue());
    }

    JCharLiteral processConstant(CharConstant x) {
      return program.getLiteralChar(x.charValue());
    }

    JDoubleLiteral processConstant(DoubleConstant x) {
      return program.getLiteralDouble(x.doubleValue());
    }

    JFloatLiteral processConstant(FloatConstant x) {
      return program.getLiteralFloat(x.floatValue());
    }

    JIntLiteral processConstant(IntConstant x) {
      return program.getLiteralInt(x.intValue());
    }

    JLongLiteral processConstant(LongConstant x) {
      return program.getLiteralLong(x.longValue());
    }

    JIntLiteral processConstant(ShortConstant x) {
      return program.getLiteralInt(x.shortValue());
    }

    JStringLiteral processConstant(StringConstant x) {
      // May be processing an annotation
      SourceInfo info = currentMethod == null ? currentClass.getSourceInfo()
          : currentMethod.getSourceInfo();
      return program.getLiteralString(info.makeChild(),
          x.stringValue().toCharArray());
    }

    /**
     * This is slightly different from the normal Java language. Specifically,
     * we explicitly handle synthetic fields that are part of nested/local
     * classes. It boils down to the fact that we really HAVE to assign
     * synthetic arguments to synthetic fields BEFORE calling the superclass
     * constructor (because it might call you back polymorphically). In straight
     * Java that glue code is a semantic error, because a this/super call must
     * be the first statement of your constructor. On the upside, optimizations
     * work the same on our synthetic fields as with any user fields.
     * 
     * The order of emulation is: - assign all synthetic fields from synthetic
     * args - call our super constructor emulation method - call our instance
     * initializer emulation method - run user code
     */
    void processConstructor(ConstructorDeclaration x) {
      JConstructor ctor = (JConstructor) typeMap.get(x.binding);
      try {
        processHasAnnotations(ctor, x.annotations);
        SourceInfo info = ctor.getSourceInfo();

        currentMethod = ctor;
        currentMethodBody = ctor.getBody();
        currentMethodScope = x.scope;

        /*
         * Determine if we have an explicit this call. The presence of an
         * explicit this call indicates we can skip certain initialization steps
         * (as the callee will perform those steps for us). These skippable
         * steps are 1) assigning synthetic args to fields and 2) running
         * initializers.
         */
        boolean hasExplicitThis = (x.constructorCall != null)
            && !x.constructorCall.isSuperAccess();

        JClassType enclosingType = ctor.getEnclosingType();
        JBlock block = currentMethodBody.getBlock();
        currentOuterThisRefParams = Maps.create();

        /*
         * All synthetic fields must be assigned, unless we have an explicit
         * this constructor call, in which case the callee will assign them for
         * us.
         */
        ReferenceBinding declaringClass = x.binding.declaringClass;
        if (declaringClass instanceof NestedTypeBinding) {
          Iterator<JParameter> paramIt = currentMethod.getParams().iterator();
          NestedTypeBinding nestedBinding = (NestedTypeBinding) declaringClass;
          if (nestedBinding.enclosingInstances != null) {
            for (SyntheticArgumentBinding arg : nestedBinding.enclosingInstances) {
              JParameter param = paramIt.next();
              JField field = (JField) typeMap.get(arg);
              if (!hasExplicitThis) {
                block.addStmt(JProgram.createAssignmentStmt(info,
                    createVariableRef(info, field),
                    createVariableRef(info, param)));
              }
              currentOuterThisRefParams = Maps.put(currentOuterThisRefParams,
                  field, param);
            }
          }

          if (!hasExplicitThis) {
            paramIt = getSyntheticLocalsIterator();
            if (nestedBinding.outerLocalVariables != null) {
              for (SyntheticArgumentBinding arg : nestedBinding.outerLocalVariables) {
                JParameter param = paramIt.next();
                JField field = (JField) typeMap.get(arg);
                block.addStmt(JProgram.createAssignmentStmt(info,
                    createVariableRef(info, field),
                    createVariableRef(info, param)));
              }
            }
          }
        }

        // optional this or super constructor call
        if (x.constructorCall != null) {
          JMethodCall superOrThisCall = (JMethodCall) dispatch(
              "processExpression", x.constructorCall);
          // Enums: wire up synthetic name/ordinal params to the super method.
          if (enclosingType.isEnumOrSubclass() != null) {
            JVariableRef enumNameRef = createVariableRef(
                superOrThisCall.getSourceInfo(), ctor.getParams().get(0));
            superOrThisCall.addArg(0, enumNameRef);
            JVariableRef enumOrdinalRef = createVariableRef(
                superOrThisCall.getSourceInfo(), ctor.getParams().get(1));
            superOrThisCall.addArg(1, enumOrdinalRef);
          }

          superOrThisCall.setStaticDispatchOnly();
          block.addStmt(superOrThisCall.makeStatement());
        }

        JExpression thisRef = createThisRef(info, enclosingType);

        /*
         * Call the synthetic instance initializer method, unless we have an
         * explicit this constructor call, in which case the callee will.
         */
        if (!hasExplicitThis) {
          // $init is always in position 1 (clinit is in 0)
          JMethod initMethod = enclosingType.getMethods().get(1);
          JMethodCall initCall = new JMethodCall(info, thisRef, initMethod);
          block.addStmt(initCall.makeStatement());
        }

        // user code (finally!)
        block.addStmts(processStatements(x.statements));

        currentOuterThisRefParams = null;
        currentMethodScope = null;
        currentMethod = null;
      } catch (Throwable e) {
        throw translateException(ctor, e);
      }
    }

    JExpression processExpression(AllocationExpression x) {
      SourceInfo info = makeSourceInfo(x);
      TypeBinding typeBinding = erasure(x.resolvedType);
      if (typeBinding.constantPoolName() == null) {
        /*
         * Weird case: if JDT determines that this local class is totally
         * uninstantiable, it won't bother allocating a local name.
         */
        return program.getLiteralNull();
      }
      JClassType newType = (JClassType) typeMap.get(typeBinding);
      MethodBinding b = x.binding;
      JConstructor ctor = (JConstructor) typeMap.get(b);
      JMethodCall call;
      JClassType javaLangString = program.getTypeJavaLangString();
      if (newType == javaLangString && !newType.isExternal()) {
        /*
         * MAGIC: java.lang.String is implemented as a JavaScript String
         * primitive with a modified prototype. This requires funky handling of
         * constructor calls. We find a method named _String() whose signature
         * matches the requested constructor
         */
        int ctorArgc = ctor.getParams().size();
        JMethod targetMethod = null;
        outer : for (JMethod method : javaLangString.getMethods()) {
          if (method.getName().equals("_String")
              && method.getParams().size() == ctorArgc) {
            for (int i = 0; i < ctorArgc; ++i) {
              JParameter mparam = method.getParams().get(i);
              JParameter cparam = ctor.getParams().get(i);
              if (mparam.getType() != cparam.getType()) {
                continue outer;
              }
            }
            targetMethod = method;
            break;
          }
        }
        if (targetMethod == null) {
          throw new InternalCompilerException(
              "String constructor error; no matching implementation.");
        }
        call = new JMethodCall(makeSourceInfo(x), null, targetMethod);
      } else {
        call = new JNewInstance(info, ctor, currentClass);
      }

      // Enums: hidden arguments for the name and id.
      if (x.enumConstant != null) {
        call.addArgs(program.getLiteralString(info, x.enumConstant.name),
            program.getLiteralInt(x.enumConstant.binding.original().id));
      }

      // Synthetic args for inner classes
      ReferenceBinding targetBinding = b.declaringClass;
      if (targetBinding.isNestedType() && !targetBinding.isStatic()) {
        NestedTypeBinding nestedBinding = (NestedTypeBinding) erasure(targetBinding);
        // Synthetic this args for inner classes
        if (nestedBinding.enclosingInstances != null) {
          for (SyntheticArgumentBinding arg : nestedBinding.enclosingInstances) {
            JClassType syntheticThisType = (JClassType) typeMap.get(arg.type);
            call.addArg(createThisRef(info, syntheticThisType));
          }
        }
      }

      // Plain old regular user arguments
      addCallArgs(x.arguments, call, b);

      // Synthetic args for inner classes
      if (targetBinding.isNestedType() && !targetBinding.isStatic()) {
        NestedTypeBinding nestedBinding = (NestedTypeBinding) erasure(targetBinding);
        // Synthetic locals for local classes
        if (nestedBinding.outerLocalVariables != null) {
          for (SyntheticArgumentBinding arg : nestedBinding.outerLocalVariables) {
            JVariable variable = (JVariable) typeMap.get(arg.actualOuterLocalVariable);
            call.addArg(createVariableRef(info, variable,
                arg.actualOuterLocalVariable));
          }
        }
      }

      return call;
    }

    JExpression processExpression(AND_AND_Expression x) {
      JType type = (JType) typeMap.get(x.resolvedType);
      SourceInfo info = makeSourceInfo(x);
      return processBinaryOperation(info, JBinaryOperator.AND, type, x.left,
          x.right);
    }

    JExpression processExpression(ArrayAllocationExpression x) {
      SourceInfo info = makeSourceInfo(x);
      JArrayType type = (JArrayType) typeMap.get(x.resolvedType);

      if (x.initializer != null) {
        List<JExpression> initializers = new ArrayList<JExpression>();
        if (x.initializer.expressions != null) {
          for (Expression expression : x.initializer.expressions) {
            initializers.add(dispProcessExpression(expression));
          }
        }
        return JNewArray.createInitializers(info, type, initializers);
      } else {
        List<JExpression> dims = new ArrayList<JExpression>();
        for (Expression dimension : x.dimensions) {
          // can be null if index expression was empty
          if (dimension == null) {
            dims.add(program.getLiteralAbsentArrayDimension());
          } else {
            dims.add(dispProcessExpression(dimension));
          }
        }
        return JNewArray.createDims(info, type, dims);
      }
    }

    JExpression processExpression(ArrayInitializer x) {
      SourceInfo info = makeSourceInfo(x);
      JArrayType type = (JArrayType) typeMap.get(x.resolvedType);

      List<JExpression> initializers = new ArrayList<JExpression>();
      if (x.expressions != null) {
        for (Expression expression : x.expressions) {
          initializers.add(dispProcessExpression(expression));
        }
      }
      return JNewArray.createInitializers(info, type, initializers);
    }

    JExpression processExpression(ArrayReference x) {
      SourceInfo info = makeSourceInfo(x);
      JArrayRef arrayRef = new JArrayRef(info,
          dispProcessExpression(x.receiver), dispProcessExpression(x.position));
      return arrayRef;
    }

    JExpression processExpression(Assignment x) {
      JType type = (JType) typeMap.get(x.resolvedType);
      SourceInfo info = makeSourceInfo(x);
      return processBinaryOperation(info, JBinaryOperator.ASG, type, x.lhs,
          x.expression);
    }

    JExpression processExpression(BinaryExpression x) {
      JBinaryOperator op;

      int binOp = (x.bits & ASTNode.OperatorMASK) >> ASTNode.OperatorSHIFT;
      switch (binOp) {
        case BinaryExpression.LEFT_SHIFT:
          op = JBinaryOperator.SHL;
          break;
        case BinaryExpression.RIGHT_SHIFT:
          op = JBinaryOperator.SHR;
          break;
        case BinaryExpression.UNSIGNED_RIGHT_SHIFT:
          op = JBinaryOperator.SHRU;
          break;
        case BinaryExpression.PLUS:
          if (program.isJavaLangString((JType) typeMap.get(x.resolvedType))) {
            op = JBinaryOperator.CONCAT;
          } else {
            op = JBinaryOperator.ADD;
          }
          break;
        case BinaryExpression.MINUS:
          op = JBinaryOperator.SUB;
          break;
        case BinaryExpression.REMAINDER:
          op = JBinaryOperator.MOD;
          break;
        case BinaryExpression.XOR:
          op = JBinaryOperator.BIT_XOR;
          break;
        case BinaryExpression.AND:
          op = JBinaryOperator.BIT_AND;
          break;
        case BinaryExpression.MULTIPLY:
          op = JBinaryOperator.MUL;
          break;
        case BinaryExpression.OR:
          op = JBinaryOperator.BIT_OR;
          break;
        case BinaryExpression.DIVIDE:
          op = JBinaryOperator.DIV;
          break;
        case BinaryExpression.LESS_EQUAL:
          op = JBinaryOperator.LTE;
          break;
        case BinaryExpression.GREATER_EQUAL:
          op = JBinaryOperator.GTE;
          break;
        case BinaryExpression.GREATER:
          op = JBinaryOperator.GT;
          break;
        case BinaryExpression.LESS:
          op = JBinaryOperator.LT;
          break;
        default:
          throw new InternalCompilerException(
              "Unexpected operator for BinaryExpression");
      }

      JType type = (JType) typeMap.get(x.resolvedType);
      SourceInfo info = makeSourceInfo(x);
      return processBinaryOperation(info, op, type, x.left, x.right);
    }

    JExpression processExpression(CastExpression x) {
      SourceInfo info = makeSourceInfo(x);
      JType type = (JType) typeMap.get(x.resolvedType);
      JCastOperation cast = new JCastOperation(info, type,
          dispProcessExpression(x.expression));
      return cast;
    }

    JExpression processExpression(ClassLiteralAccess x) {
      SourceInfo info = makeSourceInfo(x);
      JType type = (JType) typeMap.get(x.targetType);
      return new JClassLiteral(info, type);
    }

    JExpression processExpression(CombinedBinaryExpression x) {
      return processExpression((BinaryExpression) x);
    }

    JExpression processExpression(CompoundAssignment x) {
      JBinaryOperator op;

      switch (x.operator) {
        case CompoundAssignment.PLUS:
          if (program.isJavaLangString((JType) typeMap.get(x.resolvedType))) {
            op = JBinaryOperator.ASG_CONCAT;
          } else {
            op = JBinaryOperator.ASG_ADD;
          }
          break;
        case CompoundAssignment.MINUS:
          op = JBinaryOperator.ASG_SUB;
          break;
        case CompoundAssignment.MULTIPLY:
          op = JBinaryOperator.ASG_MUL;
          break;
        case CompoundAssignment.DIVIDE:
          op = JBinaryOperator.ASG_DIV;
          break;
        case CompoundAssignment.AND:
          op = JBinaryOperator.ASG_BIT_AND;
          break;
        case CompoundAssignment.OR:
          op = JBinaryOperator.ASG_BIT_OR;
          break;
        case CompoundAssignment.XOR:
          op = JBinaryOperator.ASG_BIT_XOR;
          break;
        case CompoundAssignment.REMAINDER:
          op = JBinaryOperator.ASG_MOD;
          break;
        case CompoundAssignment.LEFT_SHIFT:
          op = JBinaryOperator.ASG_SHL;
          break;
        case CompoundAssignment.RIGHT_SHIFT:
          op = JBinaryOperator.ASG_SHR;
          break;
        case CompoundAssignment.UNSIGNED_RIGHT_SHIFT:
          op = JBinaryOperator.ASG_SHRU;
          break;
        default:
          throw new InternalCompilerException(
              "Unexpected operator for CompoundAssignment");
      }

      JType type = (JType) typeMap.get(x.resolvedType);
      SourceInfo info = makeSourceInfo(x);
      return processBinaryOperation(info, op, type, x.lhs, x.expression);
    }

    JExpression processExpression(ConditionalExpression x) {
      SourceInfo info = makeSourceInfo(x);
      JType type = (JType) typeMap.get(x.resolvedType);
      JExpression ifTest = dispProcessExpression(x.condition);
      JExpression thenExpr = dispProcessExpression(x.valueIfTrue);
      JExpression elseExpr = dispProcessExpression(x.valueIfFalse);
      JConditional conditional = new JConditional(info, type, ifTest, thenExpr,
          elseExpr);
      return conditional;
    }

    JExpression processExpression(EqualExpression x) {
      JBinaryOperator op;
      switch ((x.bits & BinaryExpression.OperatorMASK) >> BinaryExpression.OperatorSHIFT) {
        case BinaryExpression.EQUAL_EQUAL:
          op = JBinaryOperator.EQ;
          break;
        case BinaryExpression.NOT_EQUAL:
          op = JBinaryOperator.NEQ;
          break;
        default:
          throw new InternalCompilerException(
              "Unexpected operator for EqualExpression");
      }

      JType type = (JType) typeMap.get(x.resolvedType);
      SourceInfo info = makeSourceInfo(x);
      return processBinaryOperation(info, op, type, x.left, x.right);
    }

    /**
     * How we have to treat super calls vs. this calls is so different, they may
     * as well have been two different JDT nodes.
     */
    JMethodCall processExpression(ExplicitConstructorCall x) {
      if (x.isSuperAccess()) {
        return processSuperConstructorCall(x);
      } else {
        return processThisConstructorCall(x);
      }
    }

    JExpression processExpression(FieldReference x) {
      FieldBinding fieldBinding = x.binding;
      SourceInfo info = makeSourceInfo(x);
      JExpression instance = dispProcessExpression(x.receiver);
      JExpression expr;
      if (fieldBinding.declaringClass == null) {
        if (!ARRAY_LENGTH_FIELD.equals(String.valueOf(fieldBinding.name))) {
          throw new InternalCompilerException("Expected [array].length.");
        }
        expr = new JArrayLength(info, instance);
      } else {
        JField field = (JField) typeMap.get(fieldBinding);
        expr = new JFieldRef(info, instance, field, currentClass);
      }

      if (x.genericCast != null) {
        JType castType = (JType) typeMap.get(x.genericCast);
        /*
         * Note, this may result in an invalid AST due to an LHS cast operation.
         * We fix this up in FixAssignmentToUnbox.
         */
        return maybeCast(castType, expr);
      }
      return expr;
    }

    JExpression processExpression(InstanceOfExpression x) {
      SourceInfo info = makeSourceInfo(x);
      JExpression expr = dispProcessExpression(x.expression);
      JReferenceType testType = (JReferenceType) typeMap.get(x.type.resolvedType);
      return new JInstanceOf(info, testType, expr);
    }

    JExpression processExpression(MessageSend x) {
      SourceInfo info = makeSourceInfo(x);
      JMethod method = (JMethod) typeMap.get(x.binding);

      JExpression qualifier;
      if (x.receiver instanceof ThisReference) {
        if (method.isStatic()) {
          // don't bother qualifying it, it's a no-op
          qualifier = null;
        } else if (x.receiver instanceof QualifiedThisReference) {
          // use the supplied qualifier
          qualifier = dispProcessExpression(x.receiver);
        } else {
          /*
           * In cases where JDT had to synthesize a this ref for us, it could
           * actually be the wrong type, if the target method is in an enclosing
           * class. We have to synthesize our own ref of the correct type.
           */
          qualifier = createThisRef(info, method.getEnclosingType());
        }
      } else {
        qualifier = dispProcessExpression(x.receiver);
      }

      JMethodCall call = new JMethodCall(info, qualifier, method);

      // On a super ref, don't allow polymorphic dispatch. Oddly enough,
      // QualifiedSuperReference not derived from SuperReference!
      boolean isSuperRef = x.receiver instanceof SuperReference
          || x.receiver instanceof QualifiedSuperReference;
      if (isSuperRef) {
        call.setStaticDispatchOnly();
      }

      // The arguments come first...
      addCallArgs(x.arguments, call, x.binding);

      if (x.valueCast != null) {
        JType castType = (JType) typeMap.get(x.valueCast);
        return maybeCast(castType, call);
      }
      return call;
    }

    JExpression processExpression(NullLiteral x) {
      return program.getLiteralNull();
    }

    JExpression processExpression(OR_OR_Expression x) {
      JType type = (JType) typeMap.get(x.resolvedType);
      SourceInfo info = makeSourceInfo(x);
      return processBinaryOperation(info, JBinaryOperator.OR, type, x.left,
          x.right);
    }

    JExpression processExpression(PostfixExpression x) {
      SourceInfo info = makeSourceInfo(x);
      JUnaryOperator op;

      switch (x.operator) {
        case PostfixExpression.MINUS:
          op = JUnaryOperator.DEC;
          break;

        case PostfixExpression.PLUS:
          op = JUnaryOperator.INC;
          break;

        default:
          throw new InternalCompilerException("Unexpected postfix operator");
      }

      JPostfixOperation postOp = new JPostfixOperation(info, op,
          dispProcessExpression(x.lhs));
      return postOp;
    }

    JExpression processExpression(PrefixExpression x) {
      SourceInfo info = makeSourceInfo(x);
      JUnaryOperator op;

      switch (x.operator) {
        case PrefixExpression.MINUS:
          op = JUnaryOperator.DEC;
          break;

        case PrefixExpression.PLUS:
          op = JUnaryOperator.INC;
          break;

        default:
          throw new InternalCompilerException("Unexpected prefix operator");
      }

      JPrefixOperation preOp = new JPrefixOperation(info, op,
          dispProcessExpression(x.lhs));
      return preOp;
    }

    JExpression processExpression(QualifiedAllocationExpression x) {
      /*
       * Weird: sometimes JDT will create a QualifiedAllocationExpression with
       * no qualifier. I guess this is supposed to let us know that we need to
       * synthesize a synthetic this arg based on our own current "this"? But
       * plain old regular AllocationExpression also must be treated as if it
       * might be be implicitly qualified, so I'm not sure what the point is.
       * Let's just defer to the AllocationExpression logic if there's no
       * qualifier.
       */
      if (x.enclosingInstance() == null) {
        return processExpression((AllocationExpression) x);
      }

      SourceInfo info = makeSourceInfo(x);
      MethodBinding b = x.binding;
      JConstructor ctor = (JConstructor) typeMap.get(b);
      JNewInstance newInstance = new JNewInstance(info, ctor, currentClass);
      JExpression qualifier = dispProcessExpression(x.enclosingInstance);
      List<JExpression> qualList = new ArrayList<JExpression>();
      qualList.add(qualifier);

      /*
       * Really weird: Sometimes an allocation expression needs both its
       * explicit qualifier AND its implicit enclosing class! We add this second
       * because the explicit qualifier takes precedence.
       */
      if (!currentMethod.isStatic()) {
        JExpression implicitOuter = new JThisRef(info,
            (JClassType) currentClass);
        qualList.add(implicitOuter);
      }

      // Synthetic this args for inner classes
      ReferenceBinding targetBinding = b.declaringClass;
      if (targetBinding.isNestedType() && !targetBinding.isStatic()) {
        NestedTypeBinding nestedBinding = (NestedTypeBinding) erasure(targetBinding);
        if (nestedBinding.enclosingInstances != null) {
          for (SyntheticArgumentBinding arg : nestedBinding.enclosingInstances) {
            JClassType syntheticThisType = (JClassType) typeMap.get(arg.type);
            newInstance.addArg(createThisRef(syntheticThisType, qualList));
          }
        }
      }

      // Plain old regular arguments
      addCallArgs(x.arguments, newInstance, b);

      // Synthetic locals for local classes
      if (targetBinding.isNestedType() && !targetBinding.isStatic()) {
        NestedTypeBinding nestedBinding = (NestedTypeBinding) erasure(targetBinding);
        if (nestedBinding.outerLocalVariables != null) {
          for (SyntheticArgumentBinding arg : nestedBinding.outerLocalVariables) {
            JVariable variable = (JVariable) typeMap.get(arg.actualOuterLocalVariable);
            newInstance.addArg(createVariableRef(info, variable,
                arg.actualOuterLocalVariable));
          }
        }
      }

      return newInstance;
    }

    JExpression processExpression(QualifiedNameReference x) {
      SourceInfo info = makeSourceInfo(x);
      Binding binding = x.binding;
      JNode node = typeMap.get(binding);
      if (!(node instanceof JVariable)) {
        return null;
      }
      JVariable variable = (JVariable) node;

      JExpression curRef = createVariableRef(info, variable, binding);
      if (x.genericCast != null) {
        JType castType = (JType) typeMap.get(x.genericCast);
        curRef = maybeCast(castType, curRef);
      }

      /*
       * Wackiness: JDT represents multiple field access as an array of fields,
       * each qualified by everything to the left. So each subsequent item in
       * otherBindings takes the current expression as a qualifier.
       */
      if (x.otherBindings != null) {
        for (int i = 0; i < x.otherBindings.length; ++i) {
          FieldBinding fieldBinding = x.otherBindings[i];
          if (fieldBinding.declaringClass == null) {
            // probably array.length
            if (!ARRAY_LENGTH_FIELD.equals(String.valueOf(fieldBinding.name))) {
              throw new InternalCompilerException("Expected [array].length.");
            }
            curRef = new JArrayLength(info, curRef);
          } else {
            JField field = (JField) typeMap.get(fieldBinding);
            curRef = new JFieldRef(info, curRef, field, currentClass);
          }
          if (x.otherGenericCasts != null && x.otherGenericCasts[i] != null) {
            JType castType = (JType) typeMap.get(x.otherGenericCasts[i]);
            curRef = maybeCast(castType, curRef);
          }
        }
      }

      return curRef;
    }

    JExpression processExpression(QualifiedSuperReference x) {
      JClassType refType = (JClassType) typeMap.get(x.resolvedType);
      JClassType qualType = (JClassType) typeMap.get(x.qualification.resolvedType);
      assert (refType == qualType.getSuperClass());
      // Oddly enough, super refs can be modeled as this refs, because whatever
      // expression they qualify has already been resolved.
      return processQualifiedThisOrSuperRef(x, qualType);
    }

    JExpression processExpression(QualifiedThisReference x) {
      JClassType refType = (JClassType) typeMap.get(x.resolvedType);
      JClassType qualType = (JClassType) typeMap.get(x.qualification.resolvedType);
      assert (refType == qualType);
      return processQualifiedThisOrSuperRef(x, qualType);
    }

    JExpression processExpression(SingleNameReference x) {
      SourceInfo info = makeSourceInfo(x);
      Binding binding = x.binding;
      Object target = typeMap.get(binding);
      if (!(target instanceof JVariable)) {
        return null;
      }
      JVariable variable = (JVariable) target;

      /*
       * Wackiness: if a field happens to have synthetic accessors (only fields
       * can have them, apparently), this is a ref to a field in an enclosing
       * instance. CreateThisRef should compute a "this" access of the
       * appropriate type, unless the field is static.
       */
      JExpression result = null;
      if (x.syntheticAccessors != null) {
        JField field = (JField) variable;
        if (!field.isStatic()) {
          JExpression instance = createThisRef(info, field.getEnclosingType());
          result = new JFieldRef(info, instance, field, currentClass);
        }
      }
      if (result == null) {
        result = createVariableRef(info, variable, binding);
      }
      if (x.genericCast != null) {
        JType castType = (JType) typeMap.get(x.genericCast);
        result = maybeCast(castType, result);
      }
      return result;
    }

    JExpression processExpression(SuperReference x) {
      JClassType type = (JClassType) typeMap.get(x.resolvedType);
      assert (type == currentClass.getSuperClass());
      SourceInfo info = makeSourceInfo(x);
      // Oddly enough, super refs can be modeled as a this refs.
      JExpression superRef = createThisRef(info, currentClass);
      return superRef;
    }

    JExpression processExpression(ThisReference x) {
      JClassType type = (JClassType) typeMap.get(x.resolvedType);
      assert (type == currentClass);
      SourceInfo info = makeSourceInfo(x);
      JExpression thisRef = createThisRef(info, currentClass);
      return thisRef;
    }

    JExpression processExpression(UnaryExpression x) {
      SourceInfo info = makeSourceInfo(x);
      JUnaryOperator op;
      int operator = ((x.bits & UnaryExpression.OperatorMASK) >> UnaryExpression.OperatorSHIFT);

      switch (operator) {
        case UnaryExpression.MINUS:
          op = JUnaryOperator.NEG;
          break;

        case UnaryExpression.NOT:
          op = JUnaryOperator.NOT;
          break;

        case UnaryExpression.PLUS:
          // Odd case.. a useless + operator; just return the operand
          return dispProcessExpression(x.expression);

        case UnaryExpression.TWIDDLE:
          op = JUnaryOperator.BIT_NOT;
          break;

        default:
          throw new InternalCompilerException(
              "Unexpected operator for unary expression");
      }

      JPrefixOperation preOp = new JPrefixOperation(info, op,
          dispProcessExpression(x.expression));
      return preOp;
    }

    List<JExpressionStatement> processExpressionStatements(
        Statement[] statements) {
      List<JExpressionStatement> jstatements = new ArrayList<JExpressionStatement>();
      if (statements != null) {
        for (int i = 0, n = statements.length; i < n; ++i) {
          JStatement jstmt = dispProcessStatement(statements[i]);
          if (jstmt != null) {
            jstatements.add((JExpressionStatement) jstmt);
          }
        }
      }
      return jstatements;
    }

    void processField(FieldDeclaration declaration) {
      JField field = (JField) typeMap.tryGet(declaration.binding);
      processHasAnnotations(field, declaration.annotations);
      if (field == null) {
        /*
         * When anonymous classes declare constant fields, the field declaration
         * is not visited by JDT. Just bail since any references to that field
         * are guaranteed to be replaced with literals.
         */
        return;
      }
      try {
        JExpression initializer = null;
        if (declaration.initialization != null) {
          initializer = dispProcessExpression(declaration.initialization);
        }

        if (field instanceof JEnumField) {
          // An enum field must be initialized!
          assert (initializer instanceof JMethodCall);
        }

        if (initializer != null) {
          SourceInfo info = makeSourceInfo(declaration);
          // JDeclarationStatement's ctor sets up the field's initializer.
          JStatement decl = new JDeclarationStatement(info, createVariableRef(
              info, field), initializer);
          // will either be init or clinit
          currentMethodBody.getBlock().addStmt(decl);
        }
      } catch (Throwable e) {
        throw translateException(field, e);
      }
    }

    void processInitializer(Initializer initializer) {
      JBlock block = (JBlock) dispProcessStatement(initializer.block);
      try {
        // will either be init or clinit
        currentMethodBody.getBlock().addStmt(block);
      } catch (Throwable e) {
        throw translateException(initializer, e);
      }
    }

    void processMethod(AbstractMethodDeclaration x) {
      MethodBinding b = x.binding;
      JMethod method = (JMethod) typeMap.get(b);
      try {
        processHasAnnotations(method, x.annotations);
        if (x.arguments != null) {
          for (int i = 0, j = x.arguments.length; i < j; i++) {
            JParameter p = (JParameter) typeMap.get(x.arguments[i].binding);
            processHasAnnotations(p, x.arguments[i].annotations);
          }
        }

        if (!b.isStatic() && (b.isImplementing() || b.isOverriding())) {
          tryFindUpRefs(method, b);
        }

        if (x.isNative()) {
          processNativeMethod(x, (JsniMethodBody) method.getBody());
          return;
        }

        currentMethod = method;
        currentMethodBody = (JMethodBody) method.getBody();
        currentMethodScope = x.scope;

        if (currentMethodBody != null) {
          currentMethodBody.getBlock().addStmts(processStatements(x.statements));
        }
        currentMethodScope = null;
        currentMethodBody = null;
        currentMethod = null;
      } catch (Throwable e) {
        throw translateException(method, e);
      }
    }

    void processNativeMethod(AbstractMethodDeclaration x,
        JsniMethodBody nativeMethodBody) {
      // Squirrel away a reference to the JDT node to enable error reporting.
      jsniMethodMap.put(nativeMethodBody, x);
    }

    JStatement processStatement(AssertStatement x) {
      SourceInfo info = makeSourceInfo(x);
      JExpression expr = dispProcessExpression(x.assertExpression);
      JExpression arg = dispProcessExpression(x.exceptionArgument);
      return new JAssertStatement(info, expr, arg);
    }

    JBlock processStatement(Block x) {
      if (x == null) {
        return null;
      }

      SourceInfo info = makeSourceInfo(x);
      JBlock block = new JBlock(info);
      block.addStmts(processStatements(x.statements));
      return block;
    }

    JStatement processStatement(BreakStatement x) {
      SourceInfo info = makeSourceInfo(x);
      return new JBreakStatement(info, getOrCreateLabel(info, currentMethod,
          x.label));
    }

    JStatement processStatement(CaseStatement x) {
      SourceInfo info = makeSourceInfo(x);
      JExpression expression = dispProcessExpression(x.constantExpression);
      if (expression != null && x.constantExpression.resolvedType.isEnum()) {
        // TODO: propagate enum information?
        assert (expression instanceof JFieldRef);
        JFieldRef fieldRef = (JFieldRef) expression;
        JEnumField field = (JEnumField) fieldRef.getField();
        return new JCaseStatement(info, program.getLiteralInt(field.ordinal()));
      } else {
        return new JCaseStatement(info, (JLiteral) expression);
      }
    }

    JStatement processStatement(ContinueStatement x) {
      SourceInfo info = makeSourceInfo(x);
      return new JContinueStatement(info, getOrCreateLabel(info, currentMethod,
          x.label));
    }

    JStatement processStatement(DoStatement x) {
      SourceInfo info = makeSourceInfo(x);
      JExpression loopTest = dispProcessExpression(x.condition);
      JStatement loopBody = dispProcessStatement(x.action);
      JDoStatement stmt = new JDoStatement(info, loopTest, loopBody);
      return stmt;
    }

    JStatement processStatement(EmptyStatement x) {
      return null;
    }

    JStatement processStatement(ForeachStatement x) {
      SourceInfo info = makeSourceInfo(x);

      JBlock body;
      JStatement action = dispProcessStatement(x.action);
      if (action instanceof JBlock) {
        body = (JBlock) action;
      } else {
        body = new JBlock(info);
        body.addStmt(action);
      }

      JLocal elementVar = (JLocal) typeMap.get(x.elementVariable.binding);
      String elementVarName = elementVar.getName();

      JDeclarationStatement elementDecl = (JDeclarationStatement) processStatement(x.elementVariable);
      assert (elementDecl.initializer == null);

      JForStatement result;
      if (x.collectionVariable != null) {
        /**
         * <pre>
         * for (final T[] i$array = collection,
         *          int i$index = 0,
         *          final int i$max = i$array.length;
         *      i$index < i$max; ++i$index) {
         *   T elementVar = i$array[i$index];
         *   // user action
         * }
         * </pre>
         */
        JLocal arrayVar = JProgram.createLocal(info, elementVarName + "$array",
            ((JType) typeMap.get(x.collection.resolvedType)), true,
            currentMethodBody);
        JLocal indexVar = JProgram.createLocal(info, elementVarName + "$index",
            program.getTypePrimitiveInt(), false, currentMethodBody);
        JLocal maxVar = JProgram.createLocal(info, elementVarName + "$max",
            program.getTypePrimitiveInt(), true, currentMethodBody);

        List<JStatement> initializers = new ArrayList<JStatement>(3);
        // T[] i$array = arr
        initializers.add(createDeclaration(info, arrayVar,
            dispProcessExpression(x.collection)));
        // int i$index = 0
        initializers.add(createDeclaration(info, indexVar,
            program.getLiteralInt(0)));
        // int i$max = i$array.length
        initializers.add(createDeclaration(info, maxVar, new JArrayLength(info,
            new JLocalRef(info, arrayVar))));

        // i$index < i$max
        JExpression condition = new JBinaryOperation(info,
            program.getTypePrimitiveBoolean(), JBinaryOperator.LT,
            createVariableRef(info, indexVar), createVariableRef(info, maxVar));

        // ++i$index
        List<JExpressionStatement> increments = new ArrayList<JExpressionStatement>(
            1);
        increments.add(new JPrefixOperation(info, JUnaryOperator.INC,
            createVariableRef(info, indexVar)).makeStatement());

        // T elementVar = i$array[i$index];
        elementDecl.initializer = new JArrayRef(info, createVariableRef(info,
            arrayVar), createVariableRef(info, indexVar));
        body.addStmt(0, elementDecl);

        result = new JForStatement(info, initializers, condition, increments,
            body);
      } else {
        /**
         * <pre>
         * for (Iterator<T> i$iterator = collection.iterator(); i$iterator.hasNext();) {
         *   T elementVar = i$iterator.next();
         *   // user action
         * }
         * </pre>
         */
        JLocal iteratorVar = JProgram.createLocal(info,
            (elementVarName + "$iterator"), program.getIndexedType("Iterator"),
            false, currentMethodBody);

        List<JStatement> initializers = new ArrayList<JStatement>(1);
        // Iterator<T> i$iterator = collection.iterator()
        initializers.add(createDeclaration(
            info,
            iteratorVar,
            new JMethodCall(info, dispProcessExpression(x.collection),
                program.getIndexedMethod("Iterable.iterator"))));

        // i$iterator.hasNext()
        JExpression condition = new JMethodCall(info, createVariableRef(info,
            iteratorVar), program.getIndexedMethod("Iterator.hasNext"));

        // T elementVar = (T) i$iterator.next();
        elementDecl.initializer = new JMethodCall(info, createVariableRef(info,
            iteratorVar), program.getIndexedMethod("Iterator.next"));

        // Perform any implicit reference type casts (due to generics).
        // Note this occurs before potential unboxing.
        if (elementVar.getType() != program.getTypeJavaLangObject()) {
          TypeBinding collectionType;
          try {
            // TODO: This is slow! Cache lookup.
            Field privateField = ForeachStatement.class.getDeclaredField("collectionElementType");
            privateField.setAccessible(true);
            collectionType = (TypeBinding) privateField.get(x);
          } catch (Exception e) {
            throw new InternalCompilerException(elementDecl,
                "Failed to retreive collectionElementType through reflection",
                e);
          }
          JType toType = (JType) typeMap.get(collectionType);
          assert (toType instanceof JReferenceType);
          elementDecl.initializer = maybeCast(toType, elementDecl.initializer);
        }

        body.addStmt(0, elementDecl);

        result = new JForStatement(info, initializers, condition,
            Collections.<JExpressionStatement> emptyList(), body);
      }

      // May need to box or unbox the element assignment.
      if (x.elementVariableImplicitWidening != -1) {
        if ((x.elementVariableImplicitWidening & TypeIds.BOXING) != 0) {
          /*
           * Boxing is necessary. In this special case of autoboxing, the boxed
           * expression cannot be a constant, so the box type must be exactly
           * that associated with the expression.
           */
          elementDecl.initializer = autoboxUtils.box(elementDecl.initializer,
              ((JPrimitiveType) elementDecl.initializer.getType()));
        } else if ((x.elementVariableImplicitWidening & TypeIds.UNBOXING) != 0) {
          elementDecl.initializer = unbox(elementDecl.initializer,
              (JClassType) elementDecl.initializer.getType());
        }
      }
      return result;
    }

    JStatement processStatement(ForStatement x) {
      SourceInfo info = makeSourceInfo(x);
      // SEE NOTE ON JDT FORCED OPTIMIZATIONS
      // If the condition is false, don't process the body
      boolean removeBody = isOptimizedFalse(x.condition);

      List<JStatement> init = processStatements(x.initializations);
      JExpression expr = dispProcessExpression(x.condition);
      List<JExpressionStatement> incr = processExpressionStatements(x.increments);
      JStatement body = removeBody ? null : dispProcessStatement(x.action);
      return new JForStatement(info, init, expr, incr, body);
    }

    JStatement processStatement(IfStatement x) {
      // SEE NOTE ON JDT FORCED OPTIMIZATIONS
      // If the condition is false, don't process the then statement
      // If the condition is false, don't process the else statement
      boolean removeThen = isOptimizedFalse(x.condition);
      boolean removeElse = isOptimizedTrue(x.condition);

      SourceInfo info = makeSourceInfo(x);
      JExpression expr = dispProcessExpression(x.condition);
      JStatement thenStmt = removeThen ? null
          : dispProcessStatement(x.thenStatement);
      JStatement elseStmt = removeElse ? null
          : dispProcessStatement(x.elseStatement);
      JIfStatement ifStmt = new JIfStatement(info, expr, thenStmt, elseStmt);
      return ifStmt;
    }

    JStatement processStatement(LabeledStatement x) {
      JStatement body = dispProcessStatement(x.statement);
      if (body == null) {
        return null;
      }
      SourceInfo info = makeSourceInfo(x);
      return new JLabeledStatement(info, getOrCreateLabel(info, currentMethod,
          x.label), body);
    }

    JStatement processStatement(LocalDeclaration x) {
      SourceInfo info = makeSourceInfo(x);
      JLocal local = (JLocal) typeMap.get(x.binding);
      processHasAnnotations(local, x.annotations);
      JLocalRef localRef = new JLocalRef(info, local);
      JExpression initializer = dispProcessExpression(x.initialization);
      return new JDeclarationStatement(info, localRef, initializer);
    }

    JStatement processStatement(ReturnStatement x) {
      SourceInfo info = makeSourceInfo(x);
      return new JReturnStatement(info, dispProcessExpression(x.expression));
    }

    JStatement processStatement(SwitchStatement x) {
      SourceInfo info = makeSourceInfo(x);
      JExpression expression = dispProcessExpression(x.expression);
      if (isEnumType(expression.getType())) {
        // Must be an enum; synthesize a call to ordinal().
        expression = new JMethodCall(info, expression,
            program.getIndexedMethod("Enum.ordinal"));
      }
      JBlock block = new JBlock(info);
      // Don't use processStatements here, because it stops at control breaks
      if (x.statements != null) {
        for (Statement stmt : x.statements) {
          JStatement jstmt = dispProcessStatement(stmt);
          if (jstmt != null) {
            block.addStmt(jstmt);
          }
        }
      }
      return new JSwitchStatement(info, expression, block);
    }

    JStatement processStatement(SynchronizedStatement x) {
      JBlock block = (JBlock) dispProcessStatement(x.block);
      JExpression expr = dispProcessExpression(x.expression);
      block.addStmt(0, expr.makeStatement());
      return block;
    }

    JStatement processStatement(ThrowStatement x) {
      SourceInfo info = makeSourceInfo(x);
      JExpression toThrow = dispProcessExpression(x.exception);
      return new JThrowStatement(info, toThrow);
    }

    JStatement processStatement(TryStatement x) {
      SourceInfo info = makeSourceInfo(x);
      JBlock tryBlock = (JBlock) dispProcessStatement(x.tryBlock);
      List<JLocalRef> catchArgs = new ArrayList<JLocalRef>();
      List<JBlock> catchBlocks = new ArrayList<JBlock>();
      if (x.catchBlocks != null) {
        for (int i = 0, c = x.catchArguments.length; i < c; ++i) {
          JLocal local = (JLocal) typeMap.get(x.catchArguments[i].binding);
          catchArgs.add((JLocalRef) createVariableRef(info, local));
        }
        for (int i = 0, c = x.catchBlocks.length; i < c; ++i) {
          catchBlocks.add((JBlock) dispProcessStatement(x.catchBlocks[i]));
        }
      }
      JBlock finallyBlock = (JBlock) dispProcessStatement(x.finallyBlock);
      return new JTryStatement(info, tryBlock, catchArgs, catchBlocks,
          finallyBlock);
    }

    JStatement processStatement(TypeDeclaration x) {
      // do nothing -- the local class is treated at the program level
      return null;
    }

    JStatement processStatement(WhileStatement x) {
      // SEE NOTE ON JDT FORCED OPTIMIZATIONS
      // If the condition is false, don't process the body
      boolean removeBody = isOptimizedFalse(x.condition);

      SourceInfo info = makeSourceInfo(x);
      JExpression loopTest = dispProcessExpression(x.condition);
      JStatement loopBody = removeBody ? null : dispProcessStatement(x.action);
      JWhileStatement stmt = new JWhileStatement(info, loopTest, loopBody);
      return stmt;
    }

    List<JStatement> processStatements(Statement[] statements) {
      List<JStatement> jstatements = new ArrayList<JStatement>();
      if (statements != null) {
        for (Statement stmt : statements) {
          JStatement jstmt = dispProcessStatement(stmt);
          if (jstmt != null) {
            jstatements.add(jstmt);
            if (jstmt.unconditionalControlBreak()) {
              /*
               * Stop processing statements, because the remaining ones are
               * unreachable. The JDT compiler might not have fully fleshed out
               * the unreachable statements.
               */
              break;
            }
          }
        }
      }

      return jstatements;
    }

    JMethodCall processSuperConstructorCall(ExplicitConstructorCall x) {
      SourceInfo info = makeSourceInfo(x);
      JMethod ctor = (JMethod) typeMap.get(x.binding);
      JExpression trueQualifier = createThisRef(info, currentClass);
      JMethodCall call = new JMethodCall(info, trueQualifier, ctor);

      // We have to find and pass through any synthetics our supertype needs
      ReferenceBinding superClass = x.binding.declaringClass;
      if (superClass.isNestedType() && !superClass.isStatic()) {
        ReferenceBinding myBinding = currentClassScope.referenceType().binding;
        ReferenceBinding superBinding = superClass;

        // enclosing types
        if (superBinding.syntheticEnclosingInstanceTypes() != null) {
          JExpression qualifier = dispProcessExpression(x.qualification);
          for (ReferenceBinding arg : superBinding.syntheticEnclosingInstanceTypes()) {
            JClassType classType = (JClassType) typeMap.get(arg);
            if (qualifier == null) {
              /*
               * Got to be one of my params; it would be illegal to use a this
               * ref at this moment-- we would most likely be passing in a
               * supertype field that HASN'T BEEN INITIALIZED YET.
               * 
               * Unfortunately, my params might not work as-is, so we have to
               * check each one to see if any will make a suitable this ref.
               */
              List<JExpression> workList = new ArrayList<JExpression>();
              Iterator<JParameter> paramIt = currentMethod.getParams().iterator();
              for (ReferenceBinding b : myBinding.syntheticEnclosingInstanceTypes()) {
                workList.add(createVariableRef(info, paramIt.next()));
              }
              call.addArg(createThisRef(classType, workList));
            } else {
              call.addArg(createThisRef(classType, qualifier));
            }
          }
        }
      }

      addCallArgs(x.arguments, call, x.binding);

      // We have to find and pass through any synthetics our supertype needs
      if (superClass.isNestedType() && !superClass.isStatic()) {
        ReferenceBinding superBinding = superClass;

        // outer locals
        if (superBinding.syntheticOuterLocalVariables() != null) {
          for (SyntheticArgumentBinding arg : superBinding.syntheticOuterLocalVariables()) {
            // Got to be one of my params
            JType varType = (JType) typeMap.get(arg.type);
            String varName = String.valueOf(arg.name);
            JParameter param = null;
            for (JParameter paramIt : currentMethod.getParams()) {
              if (varType == paramIt.getType()
                  && varName.equals(paramIt.getName())) {
                param = paramIt;
              }
            }
            if (param == null) {
              throw new InternalCompilerException(
                  "Could not find matching local arg for explicit super ctor call.");
            }
            call.addArg(createVariableRef(info, param));
          }
        }
      }

      return call;
    }

    JMethodCall processThisConstructorCall(ExplicitConstructorCall x) {
      SourceInfo info = makeSourceInfo(x);
      JMethod ctor = (JMethod) typeMap.get(x.binding);
      JExpression trueQualifier = createThisRef(info, currentClass);
      JMethodCall call = new JMethodCall(info, trueQualifier, ctor);

      assert (x.qualification == null);

      // All synthetic this args must be passed through to the target ctor
      ReferenceBinding declaringClass = x.binding.declaringClass;
      if (declaringClass.isNestedType() && !declaringClass.isStatic()) {
        Iterator<JParameter> paramIt = currentMethod.getParams().iterator();
        NestedTypeBinding nestedBinding = (NestedTypeBinding) erasure(declaringClass);
        if (nestedBinding.enclosingInstances != null) {
          for (SyntheticArgumentBinding unused : nestedBinding.enclosingInstances) {
            call.addArg(createVariableRef(info, paramIt.next()));
          }
        }
      }

      addCallArgs(x.arguments, call, x.binding);

      // All synthetic locals must be passed through to the target ctor
      if (declaringClass.isNestedType() && !declaringClass.isStatic()) {
        Iterator<JParameter> paramIt = getSyntheticLocalsIterator();
        NestedTypeBinding nestedBinding = (NestedTypeBinding) erasure(declaringClass);
        if (nestedBinding.outerLocalVariables != null) {
          for (int i = 0; i < nestedBinding.outerLocalVariables.length; ++i) {
            call.addArg(createVariableRef(info, paramIt.next()));
          }
        }
      }

      return call;
    }

    private void addAllOuterThisRefs(List<? super JVariableRef> list,
        JExpression expr, JClassType classType) {
      for (JField field : classType.getFields()) {
        // This fields are always first.
        if (!field.isThisRef()) {
          break;
        }
        // In a constructor, use the local param instead of the field.
        JParameter param = null;
        if (currentOuterThisRefParams != null && expr instanceof JThisRef) {
          param = currentOuterThisRefParams.get(field);
        }
        if (param != null) {
          list.add(new JParameterRef(expr.getSourceInfo(), param));
        } else {
          list.add(new JFieldRef(expr.getSourceInfo(), expr, field,
              currentClass));
        }
      }
    }

    private void addAllOuterThisRefsPlusSuperChain(
        List<? super JVariableRef> workList, JExpression expr,
        JClassType classType) {
      for (; classType != null; classType = classType.getSuperClass()) {
        addAllOuterThisRefs(workList, expr, classType);
      }
    }

    private void addCallArgs(Expression[] jdtArgs, JMethodCall call,
        MethodBinding binding) {
      JExpression[] args = new JExpression[jdtArgs == null ? 0 : jdtArgs.length];
      for (int i = 0; i < args.length; ++i) {
        args[i] = dispProcessExpression(jdtArgs[i]);
      }
      if (!binding.isVarargs()) {
        call.addArgs(args);
        return;
      }

      // Handle the odd var-arg case.
      TypeBinding[] params = binding.parameters;
      int varArg = params.length - 1;

      // Everything but the last arg.
      for (int i = 0; i < varArg; ++i) {
        call.addArg(args[i]);
      }

      // Handle the last arg.

      // See if there's a single varArg which is already an array.
      if (args.length == params.length) {
        if (jdtArgs[varArg].resolvedType.isCompatibleWith(params[varArg])) {
          // Already the correct array type.
          call.addArg(args[varArg]);
          return;
        }
      }

      // Need to synthesize an appropriately-typed array.
      List<JExpression> initializers = new ArrayList<JExpression>();
      for (int i = varArg; i < args.length; ++i) {
        initializers.add(args[i]);
      }
      JArrayType lastParamType = (JArrayType) typeMap.get(params[varArg]);
      JNewArray newArray = JNewArray.createInitializers(SourceOrigin.UNKNOWN,
          lastParamType, initializers);
      call.addArg(newArray);
    }

    private void addThrownExceptions(MethodBinding methodBinding, JMethod method) {
      for (ReferenceBinding exceptionReference : methodBinding.thrownExceptions) {
        method.addThrownException((JClassType) typeMap.get(exceptionReference.erasure()));
      }
    }

    /**
     * Create a bridge method. It calls a same-named method with the same
     * arguments, but with a different type signature.
     * 
     * @param clazz The class to put the bridge method in
     * @param jdtBridgeMethod The corresponding bridge method added in the JDT
     * @param implmeth The implementation method to bridge to
     */
    private void createBridgeMethod(JClassType clazz,
        SyntheticMethodBinding jdtBridgeMethod, JMethod implmeth) {
      SourceInfo info = implmeth.getSourceInfo().makeChild();
      // create the method itself
      JMethod bridgeMethod = program.createMethod(info,
          String.valueOf(jdtBridgeMethod.selector), clazz,
          (JType) typeMap.get(jdtBridgeMethod.returnType.erasure()), false,
          false, implmeth.isFinal(), false, false);
      bridgeMethod.setSynthetic();
      int paramIdx = 0;
      List<JParameter> implParams = implmeth.getParams();
      for (TypeBinding jdtParamType : jdtBridgeMethod.parameters) {
        JParameter param = implParams.get(paramIdx++);
        JType paramType = (JType) typeMap.get(jdtParamType.erasure());
        JParameter newParam = new JParameter(
            info.makeChild(param.getSourceInfo().getOrigin()), param.getName(),
            paramType, true, false, bridgeMethod);
        bridgeMethod.addParam(newParam);
      }
      addThrownExceptions(jdtBridgeMethod, bridgeMethod);
      bridgeMethod.freezeParamTypes();
      info.addCorrelation(info.getCorrelator().by(bridgeMethod));

      // create a call
      JMethodCall call = new JMethodCall(info, new JThisRef(info, clazz),
          implmeth);

      for (int i = 0; i < bridgeMethod.getParams().size(); i++) {
        JParameter param = bridgeMethod.getParams().get(i);
        JParameterRef paramRef = new JParameterRef(info, param);
        call.addArg(maybeCast(implParams.get(i).getType(), paramRef));
      }

      // wrap it in a return if necessary
      JStatement callOrReturn;
      if (bridgeMethod.getType() == program.getTypeVoid()) {
        callOrReturn = call.makeStatement();
      } else {
        callOrReturn = new JReturnStatement(info, call);
      }

      // create a body that is just that call
      JMethodBody body = (JMethodBody) bridgeMethod.getBody();
      body.getBlock().addStmt(callOrReturn);

      // Add overrides.
      List<JMethod> overrides = new ArrayList<JMethod>();
      tryFindUpRefs(bridgeMethod, overrides);
      assert !overrides.isEmpty();
      for (JMethod over : overrides) {
        bridgeMethod.addOverride(over);
        /*
         * TODO(scottb): with a diamond-shape inheritance hierarchy, it may be
         * possible to get dups in this way. Really, method.overrides should
         * probably just be an IdentitySet to avoid having to check contains in
         * various places. Left as a todo because I don't think dups is super
         * harmful.
         */
        bridgeMethod.addOverrides(over.getOverrides());
      }
    }

    private JDeclarationStatement createDeclaration(SourceInfo info,
        JLocal local, JExpression value) {
      return new JDeclarationStatement(info, new JLocalRef(info, local), value);
    }

    /**
     * Helper to create a qualified "this" ref (really a synthetic this field
     * access) of the appropriate type. Always use this method instead of
     * creating a naked JThisRef or you won't get the synthetic accesses right.
     */
    private JExpression createQualifiedThisRef(SourceInfo info,
        JClassType targetType) {
      assert (currentClass instanceof JClassType);
      JExpression expr = new JThisRef(info, ((JClassType) currentClass));
      List<JExpression> list = new ArrayList<JExpression>();
      addAllOuterThisRefsPlusSuperChain(list, expr, (JClassType) currentClass);
      return createThisRef(targetType, list);
    }

    /**
     * Helper to create an expression of the target type, possibly by accessing
     * synthetic this fields on the passed-in expression. This is needed by a
     * QualifiedAllocationExpression, because the qualifier may not be the
     * correct type, and we may need use one of its fields.
     */
    private JExpression createThisRef(JReferenceType qualType, JExpression expr) {
      List<JExpression> list = new ArrayList<JExpression>();
      list.add(expr);
      return createThisRef(qualType, list);
    }

    /**
     * Helper to create an expression of the target type, possibly by accessing
     * synthetic this fields on ANY of several passed-in expressions. Why in the
     * world would we need to do this? It turns out that when making an
     * unqualified explicit super constructor call to something that needs a
     * synthetic outer this arg, the correct value to pass in can be one of
     * several of the calling constructor's own synthetic args. The catch is,
     * it's possible none of the args are exactly the right type-- we have to
     * make one of them the right type by following each of their synthetic this
     * refs up an arbitrarily big tree of enclosing classes and
     * supertypes-with-enclosing-classes until we find something that's the
     * right type.
     * 
     * We have this implemented as a Breadth-First Search to minimize the number
     * of derefs required, and this seems to be correct. Note that we explicitly
     * prefer the current expression as one of its supertypes over a synthetic
     * this ref rooted off the current expression that happens to be the correct
     * type. We have observed this to be consistent with how Java handles it.
     * 
     * TODO(scottb): could we get this info directly from JDT?
     */
    private JExpression createThisRef(JReferenceType qualType,
        List<JExpression> list) {
      LinkedList<JExpression> workList = new LinkedList<JExpression>();
      workList.addAll(list);
      while (!workList.isEmpty()) {
        JExpression expr = workList.removeFirst();
        JClassType classType = (JClassType) ((JReferenceType) expr.getType()).getUnderlyingType();
        for (; classType != null; classType = classType.getSuperClass()) {
          // prefer myself or myself-as-supertype over any of my this$ fields
          // that may have already been added to the work list
          if (program.typeOracle.canTriviallyCast(classType, qualType)) {
            return expr;
          }
          addAllOuterThisRefs(workList, expr, classType);
        }
      }

      throw new InternalCompilerException(
          "Cannot create a ThisRef of the appropriate type.");
    }

    /**
     * Helper to creates this ref (or maybe a synthetic this field access) of
     * the appropriate type. Always use this method instead of creating a naked
     * JThisRef or you won't get the synthetic accesses right.
     */
    private JExpression createThisRef(SourceInfo info, JReferenceType targetType) {
      assert (currentClass instanceof JClassType);
      return createThisRef(targetType, new JThisRef(info,
          ((JClassType) currentClass)));
    }

    /**
     * Creates an appropriate JVariableRef for the polymorphic type of the
     * requested JVariable.
     */
    private JVariableRef createVariableRef(SourceInfo info, JVariable variable) {
      if (variable instanceof JLocal) {
        JLocal local = (JLocal) variable;
        if (local.getEnclosingMethod() != currentMethod) {
          throw new InternalCompilerException(
              "LocalRef referencing local in a different method.");
        }
        return new JLocalRef(info, local);
      } else if (variable instanceof JParameter) {
        JParameter parameter = (JParameter) variable;
        if (parameter.getEnclosingMethod() != currentMethod) {
          throw new InternalCompilerException(
              "ParameterRef referencing param in a different method.");
        }
        return new JParameterRef(info, parameter);
      } else if (variable instanceof JField) {
        JField field = (JField) variable;
        JExpression instance = null;
        if (!field.isStatic()) {
          JClassType fieldEnclosingType = (JClassType) field.getEnclosingType();
          instance = createThisRef(info, fieldEnclosingType);
          if (!program.typeOracle.canTriviallyCast(
              (JReferenceType) instance.getType(), fieldEnclosingType)) {
            throw new InternalCompilerException(
                "FieldRef referencing field in a different type.");
          }
        }
        return new JFieldRef(info, instance, field, currentClass);
      }
      throw new InternalCompilerException("Unknown JVariable subclass.");
    }

    /**
     * Creates an appropriate JVariableRef for the polymorphic type of the
     * requested JVariable.
     */
    private JVariableRef createVariableRef(SourceInfo info, JVariable variable,
        Binding binding) {
      // Fix up the reference if it's to an outer local/param
      variable = possiblyReferenceOuterLocal(variable, binding);
      if (variable == null) {
        /*
         * Strange case: in certain circumstances, JDT will fail to provide an
         * emulation path to an outer local variable. In the case I know of, the
         * reference is a spurious qualifier to a static method call. Let's just
         * return null and ditch the expression.
         */
        return null;
      }
      return createVariableRef(info, variable);
    }

    private TypeBinding erasure(TypeBinding typeBinding) {
      if (typeBinding instanceof ParameterizedTypeBinding) {
        typeBinding = typeBinding.erasure();
      }
      return typeBinding;
    }

    private Method getCachedMethod(String name,
        Class<? extends Object> childClass) throws NoSuchMethodException {
      MethodKey key = new MethodKey(name, childClass);
      MethodValue value = methodCache.get(key);
      if (value == null) {
        try {
          Method method = getClass().getDeclaredMethod(name, childClass);
          value = new MethodValue(method);
        } catch (NoSuchMethodException ex) {
          value = new MethodValue(ex);
        }
        methodCache.put(key, value);
      }
      // Might throw an exception here.
      return value.getMethod();
    }

    private JInterfaceType getOrCreateExternalType(SourceInfo info,
        char[][] compoundName) {
      String name = BuildTypeMap.dotify(compoundName);
      JInterfaceType external = (JInterfaceType) program.getFromTypeMap(name);
      if (external == null) {
        external = program.createInterface(info, name);
        external.setExternal(true);
      }
      return external;
    }

    /**
     * Get a new label of a particular name, or create a new one if it doesn't
     * exist already.
     */
    private JLabel getOrCreateLabel(SourceInfo info, JMethod enclosingMethod,
        char[] name) {
      if (name == null) {
        return null;
      }
      String sname = String.valueOf(name);
      Map<String, JLabel> lblMap = this.labelMap.get(enclosingMethod);
      if (lblMap == null) {
        lblMap = new HashMap<String, JLabel>();
        this.labelMap.put(enclosingMethod, lblMap);
      }
      JLabel jlabel = lblMap.get(sname);
      if (jlabel == null) {
        jlabel = new JLabel(info, sname);
        lblMap.put(sname, jlabel);
      }
      return jlabel;
    }

    private JPrimitiveType getPrimitiveTypeForWrapperType(JClassType wrapperType) {
      String wrapperTypeName = wrapperType.getName();
      if ("java.lang.Integer".equals(wrapperTypeName)) {
        return program.getTypePrimitiveInt();
      } else if ("java.lang.Boolean".equals(wrapperTypeName)) {
        return program.getTypePrimitiveBoolean();
      } else if ("java.lang.Character".equals(wrapperTypeName)) {
        return program.getTypePrimitiveChar();
      } else if ("java.lang.Long".equals(wrapperTypeName)) {
        return program.getTypePrimitiveLong();
      } else if ("java.lang.Short".equals(wrapperTypeName)) {
        return program.getTypePrimitiveShort();
      } else if ("java.lang.Byte".equals(wrapperTypeName)) {
        return program.getTypePrimitiveByte();
      } else if ("java.lang.Double".equals(wrapperTypeName)) {
        return program.getTypePrimitiveDouble();
      } else if ("java.lang.Float".equals(wrapperTypeName)) {
        return program.getTypePrimitiveFloat();
      } else {
        return null;
      }
    }

    /**
     * Gets a JParameter iterator for a constructor method over its synthetic
     * local parameters.
     */
    private Iterator<JParameter> getSyntheticLocalsIterator() {
      return currentMethod.getParams().listIterator(
          currentMethod.getOriginalParamTypes().size());
    }

    private void implementMethod(JMethod method, JExpression returnValue) {
      assert method != null;
      JMethodBody body = (JMethodBody) method.getBody();
      JBlock block = body.getBlock();

      SourceInfo info;
      if (block.getStatements().size() > 0) {
        info = block.getStatements().get(0).getSourceInfo();
      } else {
        info = method.getSourceInfo();
      }

      block.clear();
      block.addStmt(new JReturnStatement(info, returnValue));
    }

    /*
     * Determine the destination type for an implicit conversion of the given
     * expression. Beware that when autoboxing, the type of the expression is
     * not necessarily the same as the type of the box to be created. The JDT
     * figures out what the necessary conversion is, depending on the context
     * the expression appears in, and stores it in
     * <code>x.implicitConversion</code>, so extract it from there.
     */
    private JPrimitiveType implicitConversionTargetType(Expression x)
        throws InternalCompilerException {
      /*
       * This algorithm for finding the target type is copied from
       * org.eclipse.jdt
       * .internal.compiler.codegen.CodeStream.generateReturnBytecode() .
       */
      switch ((x.implicitConversion & TypeIds.IMPLICIT_CONVERSION_MASK) >> 4) {
        case TypeIds.T_boolean:
          return program.getTypePrimitiveBoolean();
        case TypeIds.T_byte:
          return program.getTypePrimitiveByte();
        case TypeIds.T_char:
          return program.getTypePrimitiveChar();
        case TypeIds.T_double:
          return program.getTypePrimitiveDouble();
        case TypeIds.T_float:
          return program.getTypePrimitiveFloat();
        case TypeIds.T_int:
          return program.getTypePrimitiveInt();
        case TypeIds.T_long:
          return program.getTypePrimitiveLong();
        case TypeIds.T_short:
          return program.getTypePrimitiveShort();
        default:
          throw new InternalCompilerException(
              "Could not determine the desired box type");
      }
    }

    /**
     * Check whether the specified type is definitely for an enum class.
     * 
     * @param type The type being tested
     * @return whether it is certainly an enum
     */
    private boolean isEnumType(JType type) {
      if (type instanceof JClassType) {
        return ((JClassType) type).isEnumOrSubclass() != null;
      }

      if (type instanceof JNonNullType) {
        return isEnumType(((JNonNullType) type).getUnderlyingType());
      }

      return false;
    }

    private SourceInfo makeSourceInfo(Statement x) {
      int startLine = Util.getLineNumber(x.sourceStart,
          currentSeparatorPositions, 0, currentSeparatorPositions.length - 1);
      SourceOrigin toReturn = SourceOrigin.create(x.sourceStart, x.sourceEnd,
          startLine, currentFileName);
      if (currentMethod != null) {
        return currentMethod.getSourceInfo().makeChild(toReturn);
      }
      return toReturn;
    }

    private JExpression maybeCast(JType expected, JExpression expression) {
      if (expected != expression.getType()) {
        // Must be a generic; insert a cast operation.
        JReferenceType toType = (JReferenceType) expected;
        return new JCastOperation(expression.getSourceInfo(), toType,
            expression);
      } else {
        return expression;
      }
    }

    /**
     * Sometimes a variable reference can be to a local or parameter in an an
     * enclosing method. This is a tricky situation to detect. There's no
     * obvious way to tell, but the clue we can get from JDT is that the local's
     * containing method won't be the same as the method we're currently
     * processing.
     * 
     * Once we have this clue, we can use getEmulationPath to compute the
     * current class's binding for that field.
     */
    private JVariable possiblyReferenceOuterLocal(JVariable variable,
        Binding binding) {

      if (variable instanceof JLocal || variable instanceof JParameter) {
        LocalVariableBinding localBinding = (LocalVariableBinding) binding;
        if (localBinding.declaringScope.methodScope() != currentMethodScope) {
          variable = null;
          VariableBinding[] vars = currentMethodScope.getEmulationPath(localBinding);
          if (vars == null) {
            return null;
          }
          assert (vars.length == 1);
          VariableBinding varBinding = vars[0];

          // See if there's an available parameter
          if (varBinding instanceof SyntheticArgumentBinding) {
            JType type = (JType) typeMap.get(varBinding.type);
            String name = String.valueOf(varBinding.name);
            for (int i = 0; i < currentMethod.getParams().size(); ++i) {
              JParameter param = currentMethod.getParams().get(i);
              if (type == param.getType() && name.equals(param.getName())) {
                variable = param;
                break;
              }
            }
          }

          // just use the field
          if (variable == null) {
            variable = (JField) typeMap.get(varBinding);
          }

          // now we have an updated variable that we can create our ref from
        }
      } else {
        assert variable instanceof JField;
        // In a constructor, prefer the ctor arg rather than the field.
        if (currentOuterThisRefParams != null) {
          JParameter ctorArg = currentOuterThisRefParams.get(variable);
          if (ctorArg != null) {
            variable = ctorArg;
          }
        }
      }
      return variable;
    }

    private void processAnnotationProperties(SourceInfo sourceInfo,
        JAnnotation annotation, ElementValuePair[] elementValuePairs) {
      if (elementValuePairs == null) {
        return;
      }

      for (ElementValuePair pair : elementValuePairs) {
        String name = CharOperation.charToString(pair.getName());
        List<JAnnotationArgument> values = processAnnotationPropertyValue(
            sourceInfo, pair.getValue());
        annotation.addValue(new Property(sourceInfo, name, values));
      }
    }

    private List<JAnnotationArgument> processAnnotationPropertyValue(
        SourceInfo info, Object value) {
      if (value instanceof TypeBinding) {
        JType type = (JType) typeMap.tryGet((TypeBinding) value);
        if (type == null) {
          // Indicates a binary-only class literal
          type = getOrCreateExternalType(info,
              ((ReferenceBinding) value).compoundName);
        }
        return Lists.<JAnnotationArgument> create(new JClassLiteral(
            info.makeChild(), type));

      } else if (value instanceof Constant) {
        return Lists.create((JAnnotationArgument) dispatch("processConstant",
            value));

      } else if (value instanceof Object[]) {
        Object[] array = (Object[]) value;
        List<JAnnotationArgument> toReturn = Lists.create();
        for (int i = 0, j = array.length; i < j; i++) {
          toReturn = Lists.add(toReturn,
              processAnnotationPropertyValue(info, array[i]).get(0));
        }
        return toReturn;

      } else if (value instanceof AnnotationBinding) {
        AnnotationBinding annotationBinding = (AnnotationBinding) value;
        ReferenceBinding annotationType = annotationBinding.getAnnotationType();
        JInterfaceType type = (JInterfaceType) typeMap.tryGet(annotationType);
        JAnnotation toReturn;
        if (type != null) {
          toReturn = new JAnnotation(info, type);
        } else {
          JInterfaceType external = getOrCreateExternalType(info,
              annotationType.compoundName);
          toReturn = new JAnnotation(info, external);
        }

        // Load the properties for the annotation value
        processAnnotationProperties(info, toReturn,
            annotationBinding.getElementValuePairs());

        return Lists.<JAnnotationArgument> create(toReturn);
      } else if (value instanceof FieldBinding) {
        FieldBinding fieldBinding = (FieldBinding) value;
        assert fieldBinding.constant() != null : "Expecting constant-valued field";
        return Lists.create((JAnnotationArgument) dispatch("processConstant",
            fieldBinding.constant()));
      }

      throw new InternalCompilerException("Unable to process value "
          + value.getClass().getName());
    }

    /**
     * Helper for creating all JBinaryOperation. Several different JDT nodes can
     * result in binary operations: AND_AND_Expression, Assignment,
     * BinaryExpresion, CompoundAssignment, EqualExpression, and
     * OR_OR_Expression. Hopefully the specific operators that can result in
     * each different JDT type won't change between releases, because we only
     * look for the specific operators that we think should match each JDT node,
     * and throw an error if there's a mismatch.
     */
    private JExpression processBinaryOperation(SourceInfo info,
        JBinaryOperator op, JType type, Expression arg1, Expression arg2) {
      JExpression exprArg1 = dispProcessExpression(arg1);
      JExpression exprArg2 = dispProcessExpression(arg2);
      JBinaryOperation binaryOperation = new JBinaryOperation(info, type, op,
          exprArg1, exprArg2);
      return binaryOperation;
    }

    /**
     * It is safe to pass a null array.
     */
    private <T extends HasAnnotations & HasSourceInfo> void processHasAnnotations(
        T x, Annotation[] annotations) {
      if (annotations == null) {
        return;
      }

      for (Annotation a : annotations) {
        JAnnotation annotation;
        ReferenceBinding binding = (ReferenceBinding) a.resolvedType;
        String name = CharOperation.toString(binding.compoundName);
        boolean record = false;
        for (String prefix : JProgram.RECORDED_ANNOTATION_PACKAGES) {
          if (name.startsWith(prefix + ".")) {
            record = true;
            break;
          }
        }
        if (!record) {
          continue;
        }
        JInterfaceType annotationType = (JInterfaceType) typeMap.tryGet(binding);
        if (annotationType != null) {
          annotation = new JAnnotation(x.getSourceInfo(), annotationType);
        } else {
          // Indicates a binary-only annotation type
          JInterfaceType externalType = getOrCreateExternalType(
              x.getSourceInfo(), binding.compoundName);
          annotation = new JAnnotation(x.getSourceInfo(), externalType);
        }
        processAnnotationProperties(x.getSourceInfo(), annotation,
            a.computeElementValuePairs());
        x.addAnnotation(annotation);
      }
    }

    private JExpression processQualifiedThisOrSuperRef(
        QualifiedThisReference x, JClassType qualType) {
      /*
       * WEIRD: If a thisref or superref is qualified with the EXACT type of the
       * innermost type (in other words, a needless qualifier), it must refer to
       * that innermost type, because a class can never be nested inside of
       * itself. In this case, we must treat it as if it were not qualified.
       * 
       * In all other cases, the qualified thisref or superref cannot possibly
       * refer to the innermost type (even if the innermost type could be cast
       * to a compatible type), so we must create a reference to some outer
       * type.
       */
      SourceInfo info = makeSourceInfo(x);
      if (qualType == currentClass) {
        return createThisRef(info, qualType);
      } else {
        return createQualifiedThisRef(info, qualType);
      }
    }

    private InternalCompilerException translateException(Object node,
        Throwable e) {
      if (e instanceof VirtualMachineError) {
        // Always rethrow VM errors (an attempt to wrap may fail).
        throw (VirtualMachineError) e;
      }
      InternalCompilerException ice;
      if (e instanceof InternalCompilerException) {
        ice = (InternalCompilerException) e;
      } else {
        ice = new InternalCompilerException("Error constructing Java AST", e);
      }
      String className = node.getClass().getName();
      String description = node.toString();
      SourceInfo sourceInfo = null;
      if (node instanceof Statement) {
        sourceInfo = makeSourceInfo((Statement) node);
      }
      ice.addNode(className, description, sourceInfo);
      return ice;
    }

    /**
     * For a given method, try to find all methods that it overrides/implements.
     * This version does not use JDT.
     */
    private void tryFindUpRefs(JMethod method) {
      List<JMethod> overrides = new ArrayList<JMethod>();
      tryFindUpRefs(method, overrides);
      method.addOverrides(overrides);
    }

    private void tryFindUpRefs(JMethod method, List<JMethod> overrides) {
      if (method.getEnclosingType() != null) {
        tryFindUpRefsRecursive(method, method.getEnclosingType(), overrides);
      }
    }

    /**
     * For a given method(and method binding), try to find all methods that it
     * overrides/implements.
     */
    private void tryFindUpRefs(JMethod method, MethodBinding binding) {
      // Should never get a parameterized instance here.
      assert binding == binding.original();
      tryFindUpRefsRecursive(method, binding, binding.declaringClass);
    }

    /**
     * For a given method(and method binding), recursively try to find all
     * methods that it overrides/implements.
     */
    private void tryFindUpRefsRecursive(JMethod method,
        JDeclaredType searchThisType, List<JMethod> overrides) {

      // See if this class has any uprefs, unless this class is myself
      if (method.getEnclosingType() != searchThisType) {
        for (JMethod upRef : searchThisType.getMethods()) {
          if (JTypeOracle.methodsDoMatch(method, upRef)
              && !overrides.contains(upRef)) {
            overrides.add(upRef);
            break;
          }
        }
      }

      // recurse super class
      if (searchThisType.getSuperClass() != null) {
        tryFindUpRefsRecursive(method, searchThisType.getSuperClass(),
            overrides);
      }

      // recurse super interfaces
      for (JInterfaceType intf : searchThisType.getImplements()) {
        tryFindUpRefsRecursive(method, intf, overrides);
      }
    }

    /**
     * For a given method(and method binding), recursively try to find all
     * methods that it overrides/implements.
     */
    private void tryFindUpRefsRecursive(JMethod method, MethodBinding binding,
        ReferenceBinding searchThisType) {
      /*
       * Always look for uprefs in the original, so we can correctly compare
       * erased signatures. The general design for uprefs is to model what the
       * JVM does in terms of matching up overrides based on binary match.
       */
      searchThisType = (ReferenceBinding) searchThisType.original();

      // See if this class has any uprefs, unless this class is myself
      if (binding.declaringClass != searchThisType) {
        for (MethodBinding tryMethod : searchThisType.getMethods(binding.selector)) {
          if (binding.returnType.erasure() == tryMethod.returnType.erasure()
              && binding.areParameterErasuresEqual(tryMethod)) {
            JMethod upRef = (JMethod) typeMap.get(tryMethod);
            if (!method.getOverrides().contains(upRef)) {
              method.addOverride(upRef);
              break;
            }
          }
        }
      }

      // recurse super class
      if (searchThisType.superclass() != null) {
        tryFindUpRefsRecursive(method, binding, searchThisType.superclass());
      }

      // recurse super interfaces
      if (searchThisType.superInterfaces() != null) {
        for (int i = 0; i < searchThisType.superInterfaces().length; i++) {
          ReferenceBinding intf = searchThisType.superInterfaces()[i];
          tryFindUpRefsRecursive(method, binding, intf);
        }
      }
    }

    private JExpression unbox(JExpression toUnbox, JClassType wrapperType) {
      JPrimitiveType primitiveType = getPrimitiveTypeForWrapperType(wrapperType);
      if (primitiveType == null) {
        throw new InternalCompilerException(toUnbox,
            "Attempt to unbox unexpected type '" + wrapperType.getName() + "'",
            null);
      }

      String valueMethodName = primitiveType.getName() + "Value";
      JMethod valueMethod = null;
      for (Object element : wrapperType.getMethods()) {
        JMethod method = (JMethod) element;
        if (method.getName().equals(valueMethodName)) {
          if (method.getParams().isEmpty()) {
            // It's a match!
            valueMethod = method;
            break;
          }
        }
      }

      if (valueMethod == null) {
        throw new InternalCompilerException(toUnbox,
            "Expected to find a method on '" + wrapperType.getName()
                + "' whose signature matches 'public "
                + primitiveType.getName() + " " + valueMethodName + "()'", null);
      }

      JMethodCall unboxCall = new JMethodCall(toUnbox.getSourceInfo(), toUnbox,
          valueMethod);
      return unboxCall;
    }

    private void writeEnumValueOfMethod(JEnumType type, JField valuesField) {
      JField mapField;
      {
        /*
         * Make an inner class to hold a lazy-init name-value map. We use a
         * class to take advantage of its clinit.
         * 
         * class Map { $MAP = Enum.createValueOfMap($VALUES); }
         */
        SourceInfo typeInfo = type.getSourceInfo().makeChild();
        JClassType mapClass = program.createClass(typeInfo, type.getName()
            + "$Map", false, true);
        typeInfo.addCorrelation(typeInfo.getCorrelator().by(mapClass));
        mapClass.setSuperClass(program.getTypeJavaLangObject());
        SourceInfo fieldInfo = typeInfo.makeChild();
        mapField = program.createField(fieldInfo, "$MAP", mapClass,
            program.getJavaScriptObject(), true, Disposition.FINAL);
        fieldInfo.addCorrelation(fieldInfo.getCorrelator().by(mapField));

        SourceInfo methodInfo = typeInfo.makeChild();
        JMethodCall call = new JMethodCall(methodInfo, null,
            program.getIndexedMethod("Enum.createValueOfMap"));
        call.addArg(new JFieldRef(methodInfo, null, valuesField, type));
        JFieldRef mapRef = new JFieldRef(methodInfo, null, mapField, type);
        JDeclarationStatement declStmt = new JDeclarationStatement(methodInfo,
            mapRef, call);
        JMethod clinit = program.createMethod(methodInfo, "$clinit", mapClass,
            program.getTypeVoid(), false, true, true, true, false);
        clinit.freezeParamTypes();
        methodInfo.addCorrelation(methodInfo.getCorrelator().by(clinit));
        JBlock clinitBlock = ((JMethodBody) clinit.getBody()).getBlock();
        clinitBlock.addStmt(declStmt);
        mapField.setInitializer(declStmt);
      }

      /*
       * return Enum.valueOf(Enum$Map.Map.$MAP, name);
       */
      {
        SourceInfo sourceInfo = currentMethodBody.getSourceInfo();
        JFieldRef mapRef = new JFieldRef(sourceInfo, null, mapField, type);
        JVariableRef nameRef = createVariableRef(sourceInfo,
            currentMethod.getParams().get(0));
        JMethod delegateTo = program.getIndexedMethod("Enum.valueOf");
        JMethodCall call = new JMethodCall(sourceInfo, null, delegateTo);
        call.addArgs(mapRef, nameRef);

        currentMethodBody.getBlock().addStmt(
            new JReturnStatement(sourceInfo, call));
      }
    }

    private JField writeEnumValuesMethod(JEnumType type) {
      JField valuesField;
      {
        // $VALUES = new E[]{A,B,B};
        SourceInfo fieldInfo = type.getSourceInfo().makeChild();
        JArrayType enumArrayType = program.getTypeArray(type);
        valuesField = program.createField(fieldInfo, "$VALUES", type,
            enumArrayType, true, Disposition.FINAL);
        fieldInfo.addCorrelation(fieldInfo.getCorrelator().by(valuesField));
        List<JExpression> initializers = new ArrayList<JExpression>();
        for (JEnumField field : type.getEnumList()) {
          JFieldRef fieldRef = new JFieldRef(fieldInfo, null, field, type);
          initializers.add(fieldRef);
        }
        JNewArray newExpr = JNewArray.createInitializers(fieldInfo,
            enumArrayType, initializers);
        JFieldRef valuesRef = new JFieldRef(fieldInfo, null, valuesField, type);
        JDeclarationStatement declStmt = new JDeclarationStatement(fieldInfo,
            valuesRef, newExpr);
        JBlock clinitBlock = ((JMethodBody) type.getMethods().get(0).getBody()).getBlock();

        /*
         * HACKY: the $VALUES array must be initialized immediately after all of
         * the enum fields, but before any user initialization (which might rely
         * on $VALUES). The "1 + " is the statement containing the call to
         * Enum.$clinit().
         */
        int insertionPoint = 1 + initializers.size();
        assert clinitBlock.getStatements().size() >= initializers.size() + 1;
        clinitBlock.addStmt(insertionPoint, declStmt);
        valuesField.setInitializer(declStmt);
      }
      {
        // return $VALUES;
        SourceInfo sourceInfo = currentMethod.getSourceInfo();
        JFieldRef valuesRef = new JFieldRef(sourceInfo, null, valuesField, type);
        currentMethodBody.getBlock().addStmt(
            new JReturnStatement(sourceInfo, valuesRef));
      }
      return valuesField;
    }
  }

  /**
   * Resolve JSNI refs; replace with compile-time constants where appropriate.
   */
  private static class JsniRefGenerationVisitor extends JModVisitor {

    private class JsniRefResolver extends JsModVisitor {
      private final AbstractMethodDeclaration methodDecl;
      private final JsniMethodBody nativeMethodBody;

      private JsniRefResolver(AbstractMethodDeclaration methodDecl,
          JsniMethodBody nativeMethodBody) {
        this.methodDecl = methodDecl;
        this.nativeMethodBody = nativeMethodBody;
      }

      @Override
      public void endVisit(JsNameRef x, JsContext ctx) {
        String ident = x.getIdent();
        if (ident.charAt(0) == '@') {
          processNameRef(x, ctx);
        }
      }

      private JNode findJsniRefTarget(final SourceInfo info, String ident) {
        JsniRef parsed = JsniRef.parse(ident);
        if (parsed == null) {
          JsniCollector.reportJsniError(info, methodDecl,
              "Badly formatted native reference '" + ident + "'");
          return null;
        }

        JProgram prog = program;

        return JsniRefLookup.findJsniRefTarget(parsed, prog,
            new JsniRefLookup.ErrorReporter() {
              public void reportError(String error) {
                JsniCollector.reportJsniError(info, methodDecl, error);
              }
            });
      }

      private void processClassLiteral(JClassLiteral classLiteral, JsContext ctx) {
        assert !ctx.isLvalue();
        nativeMethodBody.addClassRef(classLiteral);
      }

      private void processField(JsNameRef nameRef, SourceInfo info,
          JField field, JsContext ctx) {
        /*
         * We must replace any compile-time constants with the constant value of
         * the field.
         */
        if (field.isCompileTimeConstant()) {
          assert !ctx.isLvalue();
          JLiteral initializer = field.getConstInitializer();
          JType type = initializer.getType();
          if (type instanceof JPrimitiveType || program.isJavaLangString(type)) {
            GenerateJavaScriptLiterals generator = new GenerateJavaScriptLiterals();
            generator.accept(initializer);
            JsExpression result = generator.peek();
            assert (result != null);
            ctx.replaceMe(result);
            return;
          }
        }

        // Normal: create a jsniRef.
        JsniFieldRef fieldRef = new JsniFieldRef(info, nameRef.getIdent(),
            field, currentClass, ctx.isLvalue());
        nativeMethodBody.addJsniRef(fieldRef);
      }

      private void processMethod(JsNameRef nameRef, SourceInfo info,
          JMethod method, JsContext ctx) {
        assert !ctx.isLvalue();
        JsniMethodRef methodRef = new JsniMethodRef(info, nameRef.getIdent(),
            method, program.getJavaScriptObject());
        nativeMethodBody.addJsniRef(methodRef);
      }

      private void processNameRef(JsNameRef nameRef, JsContext ctx) {
        SourceInfo info = nativeMethodBody.getSourceInfo();
        // TODO: make this tighter when we have real source info
        // JSourceInfo info = translateInfo(nameRef.getInfo());
        String ident = nameRef.getIdent();
        JNode node = program.jsniMap.get(ident);
        if (node == null) {
          node = findJsniRefTarget(info, ident);
          if (node == null) {
            return; // already reported error
          }
          if (node instanceof JType) {
            node = new JClassLiteral(info.makeChild(), (JType) node);
          }
          program.jsniMap.put(ident, node);
        }

        if (node instanceof JField) {
          processField(nameRef, info, (JField) node, ctx);
        } else if (node instanceof JMethod) {
          processMethod(nameRef, info, (JMethod) node, ctx);
        } else if (node instanceof JClassLiteral) {
          processClassLiteral((JClassLiteral) node, ctx);
        } else {
          throw new InternalCompilerException(node,
              "JSNI reference to something other than a class, field, or method?", null);
        }
      }
    }

    private JDeclaredType currentClass;

    private final Map<JsniMethodBody, AbstractMethodDeclaration> jsniMethodMap;

    private final JProgram program;

    public JsniRefGenerationVisitor(JProgram program,
        Map<JsniMethodBody, AbstractMethodDeclaration> jsniMethodMap) {
      this.program = program;
      this.jsniMethodMap = jsniMethodMap;
    }

    @Override
    public void endVisit(JClassType x, Context ctx) {
      currentClass = null;
    }

    @Override
    public void endVisit(JsniMethodBody x, Context ctx) {
      new JsniRefResolver(jsniMethodMap.get(x), x).accept(x.getFunc());
    }

    @Override
    public boolean visit(JClassType x, Context ctx) {
      currentClass = x;
      return true;
    }

    @Override
    public boolean visit(JMethodBody x, Context ctx) {
      return false;
    }
  }

  /**
   * Combines the information from the JDT type nodes and the type map to create
   * a JProgram structure.
   */
  public static void exec(TypeDeclaration[] types, TypeMap typeMap,
      JProgram jprogram, JJSOptions options) {
    Event generateJavaAstEvent = SpeedTracerLogger.start(CompilerEventType.GENERATE_JAVA_AST);
    // Construct the basic AST.
    JavaASTGenerationVisitor v = new JavaASTGenerationVisitor(typeMap,
        jprogram, options);
    for (TypeDeclaration type : types) {
      v.processType(type);
    }
    for (TypeDeclaration type : types) {
      v.addBridgeMethods(type.binding);
    }
    Collections.sort(jprogram.getDeclaredTypes(), new HasNameSort());

    // Process JSNI.
    Map<JsniMethodBody, AbstractMethodDeclaration> jsniMethodMap = v.getJsniMethodMap();
    new JsniRefGenerationVisitor(jprogram, jsniMethodMap).accept(jprogram);
    generateJavaAstEvent.end();
  }

  /**
   * Returns <code>true</code> if JDT optimized the condition to
   * <code>false</code>.
   */
  private static boolean isOptimizedFalse(Expression condition) {
    if (condition != null) {
      Constant cst = condition.optimizedBooleanConstant();
      if (cst != Constant.NotAConstant) {
        if (cst.booleanValue() == false) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Returns <code>true</code> if JDT optimized the condition to
   * <code>true</code>.
   */
  private static boolean isOptimizedTrue(Expression condition) {
    if (condition != null) {
      Constant cst = condition.optimizedBooleanConstant();
      if (cst != Constant.NotAConstant) {
        if (cst.booleanValue()) {
          return true;
        }
      }
    }
    return false;
  }

  private static boolean isScript(JProgram program) {
    return !program.getTypeJavaLangObject().isExternal();
  }
}
