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

import com.google.gwt.dev.jjs.ast.Context;
import com.google.gwt.dev.jjs.ast.JArrayRef;
import com.google.gwt.dev.jjs.ast.JArrayType;
import com.google.gwt.dev.jjs.ast.JBinaryOperation;
import com.google.gwt.dev.jjs.ast.JBinaryOperator;
import com.google.gwt.dev.jjs.ast.JCastOperation;
import com.google.gwt.dev.jjs.ast.JCharLiteral;
import com.google.gwt.dev.jjs.ast.JClassType;
import com.google.gwt.dev.jjs.ast.JExpression;
import com.google.gwt.dev.jjs.ast.JInstanceOf;
import com.google.gwt.dev.jjs.ast.JIntLiteral;
import com.google.gwt.dev.jjs.ast.JMethod;
import com.google.gwt.dev.jjs.ast.JMethodCall;
import com.google.gwt.dev.jjs.ast.JModVisitor;
import com.google.gwt.dev.jjs.ast.JNullLiteral;
import com.google.gwt.dev.jjs.ast.JNullType;
import com.google.gwt.dev.jjs.ast.JPrimitiveType;
import com.google.gwt.dev.jjs.ast.JProgram;
import com.google.gwt.dev.jjs.ast.JReferenceType;
import com.google.gwt.dev.jjs.ast.JType;
import com.google.gwt.dev.jjs.ast.JTypeOracle;
import com.google.gwt.dev.jjs.ast.JVisitor;
import com.google.gwt.dev.jjs.ast.js.JsonObject;
import com.google.gwt.dev.jjs.ast.js.JsonObject.JsonPropInit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Replace cast and instanceof operations with calls to the Cast class. Depends
 * on {@link CatchBlockNormalizer}, {@link CompoundAssignmentNormalizer},
 * {@link JsoDevirtualizer}, and {@link LongCastNormalizer} having already run.
 * 
 * <p>
 * Object and String always get a typeId of 1 and 2, respectively. 0 is reserved
 * as the typeId for any classes that can never be instance type of a successful
 * dynamic cast.
 * </p>
 * <p>
 * Object and String always get a queryId of 0 and 1, respectively. The 0
 * queryId always means "always succeeds". In practice, we never generate an
 * explicit cast with a queryId of 0; it is only used for array store checking,
 * where the 0 queryId means that anything can be stored into an Object[].
 * </p>
 * <p>
 * JavaScriptObject and subclasses have no typeId at all. JavaScriptObject has a
 * queryId of -1, which again is only used for array store checking, to ensure
 * that a non-JSO is not stored into a JavaScriptObject[].
 * </p>
 */
public class CastNormalizer {

  private class AssignTypeIdsVisitor extends JVisitor {

    Set<JClassType> alreadyRan = new HashSet<JClassType>();
    private Map<JReferenceType, Set<JReferenceType>> queriedTypes = new IdentityHashMap<JReferenceType, Set<JReferenceType>>();
    private int nextQueryId = 0;
    private final List<JArrayType> instantiatedArrayTypes = new ArrayList<JArrayType>();
    private List<JClassType> classes = new ArrayList<JClassType>();
    private List<JsonObject> jsonObjects = new ArrayList<JsonObject>();

    {
      JTypeOracle typeOracle = program.typeOracle;
      for (JArrayType arrayType : program.getAllArrayTypes()) {
        if (typeOracle.isInstantiatedType(arrayType)) {
          instantiatedArrayTypes.add(arrayType);
        }
      }

      // Reserve query id 0 for java.lang.Object (for array stores on JSOs).
      recordCastInternal(program.getTypeJavaLangObject(),
          program.getTypeJavaLangObject());

      // Reserve query id 1 for java.lang.String to facilitate the mashup case.
      // Multiple GWT modules need to modify String's prototype the same way.
      recordCastInternal(program.getTypeJavaLangString(),
          program.getTypeJavaLangObject());
    }

    public void computeTypeIds() {

      // the 0th entry is the "always false" entry
      classes.add(null);
      jsonObjects.add(new JsonObject(program));

      /*
       * Do String first to reserve typeIds 1 and 2 for Object and String,
       * respectively. This ensures consistent modification of String's
       * prototype.
       */
      computeSourceClass(program.getTypeJavaLangString());
      assert (classes.size() == 3);

      /*
       * Compute the list of classes than can successfully satisfy cast
       * requests, along with the set of types they can be successfully cast to.
       * Do it in super type order.
       */
      for (JReferenceType type : program.getDeclaredTypes()) {
        if (type instanceof JClassType) {
          computeSourceClass((JClassType) type);
        }
      }

      for (JArrayType type : program.getAllArrayTypes()) {
        computeSourceClass(type);
      }

      // pass our info to JProgram
      program.initTypeInfo(classes, jsonObjects);

      // JSO's maker queryId is -1 (used for array stores).
      JClassType jsoType = program.getJavaScriptObject();
      if (jsoType != null) {
        queryIds.put(jsoType, -1);
      }
      program.recordQueryIds(queryIds);
    }

    /*
     * If this expression could possibly generate an ArrayStoreException, we
     * must record a query on the element type being assigned to.
     */
    @Override
    public void endVisit(JBinaryOperation x, Context ctx) {
      if (x.getOp() == JBinaryOperator.ASG && x.getLhs() instanceof JArrayRef) {

        // first, calculate the transitive closure of all possible runtime types
        // the lhs could be
        JExpression instance = ((JArrayRef) x.getLhs()).getInstance();
        if (instance.getType() instanceof JNullType) {
          // will generate a null pointer exception instead
          return;
        }
        JArrayType lhsArrayType = (JArrayType) instance.getType();
        JType elementType = lhsArrayType.getElementType();

        // primitives are statically correct
        if (!(elementType instanceof JReferenceType)) {
          return;
        }

        // element type being final means the assignment is statically correct
        if (((JReferenceType) elementType).isFinal()) {
          return;
        }

        /*
         * For every instantiated array type that could -in theory- be the
         * runtime type of the lhs, we must record a cast from the rhs to the
         * prospective element type of the lhs.
         */
        JTypeOracle typeOracle = program.typeOracle;
        JType rhsType = x.getRhs().getType();
        assert (rhsType instanceof JReferenceType);
        JReferenceType refRhsType = (JReferenceType) rhsType;

        for (JArrayType arrayType : instantiatedArrayTypes) {
          if (typeOracle.canTheoreticallyCast(arrayType, lhsArrayType)) {
            JType itElementType = arrayType.getElementType();
            if (itElementType instanceof JReferenceType) {
              recordCastInternal((JReferenceType) itElementType, refRhsType);
            }
          }
        }
      }
    }

    @Override
    public void endVisit(JCastOperation x, Context ctx) {
      if (x.getCastType() != program.getTypeNull()) {
        recordCast(x.getCastType(), x.getExpr());
      }
    }

    @Override
    public void endVisit(JInstanceOf x, Context ctx) {
      assert (x.getTestType() != program.getTypeNull());
      recordCast(x.getTestType(), x.getExpr());
    }

    /**
     * Create the data for JSON table to capture the mapping from a class to its
     * query types.
     */
    private void computeSourceClass(JClassType type) {
      if (type == null || alreadyRan.contains(type)) {
        return;
      }

      alreadyRan.add(type);

      /*
       * IMPORTANT: Visit my supertype first. The implementation of
       * com.google.gwt.lang.Cast.wrapJSO() depends on all superclasses having
       * typeIds that are less than all their subclasses. This allows the same
       * JSO to be wrapped stronger but not weaker.
       */
      computeSourceClass(type.extnds);

      if (!program.typeOracle.isInstantiatedType(type)
          || program.isJavaScriptObject(type)) {
        return;
      }

      // Find all possible query types which I can satisfy
      Set<JReferenceType> yesSet = null;

      // NOTE: non-deterministic iteration over HashSet and HashMap. This is
      // okay here because we're just adding things to another HashSet.
      for (JReferenceType qType : queriedTypes.keySet()) {

        Set<JReferenceType> querySet = queriedTypes.get(qType);
        if (program.typeOracle.canTriviallyCast(type, qType)) {

          for (JReferenceType argType : querySet) {

            if (program.typeOracle.canTriviallyCast(type, argType)) {
              if (yesSet == null) {
                yesSet = new HashSet<JReferenceType>();
              }
              yesSet.add(qType);
              break;
            }
          }
        }
      }

      // Use a sparse array to sort my yes set.
      JReferenceType[] yesArray = new JReferenceType[nextQueryId];
      if (yesSet != null) {
        for (JReferenceType yesType : yesSet) {
          yesArray[queryIds.get(yesType)] = yesType;
        }
      }

      // Create a sparse lookup object.
      JsonObject jsonObject = new JsonObject(program);
      // Start at 1; 0 is Object and always true.
      for (int i = 1; i < nextQueryId; ++i) {
        if (yesArray[i] != null) {
          JIntLiteral labelExpr = program.getLiteralInt(i);
          JIntLiteral valueExpr = program.getLiteralInt(1);
          jsonObject.propInits.add(new JsonPropInit(program, labelExpr,
              valueExpr));
        }
      }

      /*
       * Don't add an entry for empty answer sets, except for Object and String
       * which require typeIds.
       */
      if (jsonObject.propInits.isEmpty()
          && type != program.getTypeJavaLangObject()
          && type != program.getTypeJavaLangString()) {
        return;
      }

      // add an entry for me
      classes.add(type);
      jsonObjects.add(jsonObject);
    }

    private void recordCast(JType targetType, JExpression rhs) {
      if (targetType instanceof JReferenceType) {
        // unconditional cast b/c it would've been a semantic error earlier
        JReferenceType rhsType = (JReferenceType) rhs.getType();
        // don't record a type for trivial casts that won't generate code
        if (rhsType instanceof JClassType) {
          if (program.typeOracle.canTriviallyCast(rhsType,
              (JReferenceType) targetType)) {
            return;
          }
        }

        // If the target type is a JavaScriptObject, don't record an id.
        if (program.isJavaScriptObject(targetType)) {
          return;
        }

        recordCastInternal((JReferenceType) targetType, rhsType);
      }
    }

    private void recordCastInternal(JReferenceType targetType,
        JReferenceType rhsType) {
      JReferenceType toType = targetType;
      Set<JReferenceType> querySet = queriedTypes.get(toType);
      if (querySet == null) {
        queryIds.put(toType, nextQueryId++);
        querySet = new HashSet<JReferenceType>();
        queriedTypes.put(toType, querySet);
      }
      querySet.add(rhsType);
    }
  }

  /**
   * Explicitly convert any char or long type expressions within a concat
   * operation into strings because normal JavaScript conversion does not work
   * correctly.
   */
  private class ConcatVisitor extends JModVisitor {

    @Override
    public void endVisit(JBinaryOperation x, Context ctx) {
      if (x.getType() != program.getTypeJavaLangString()) {
        return;
      }

      if (x.getOp() == JBinaryOperator.ADD) {
        JExpression newLhs = convertString(x.getLhs());
        JExpression newRhs = convertString(x.getRhs());
        if (newLhs != x.getLhs() || newRhs != x.getRhs()) {
          JBinaryOperation newExpr = new JBinaryOperation(program,
              x.getSourceInfo(), program.getTypeJavaLangString(),
              JBinaryOperator.ADD, newLhs, newRhs);
          ctx.replaceMe(newExpr);
        }
      } else if (x.getOp() == JBinaryOperator.ASG_ADD) {
        JExpression newRhs = convertString(x.getRhs());
        if (newRhs != x.getRhs()) {
          JBinaryOperation newExpr = new JBinaryOperation(program,
              x.getSourceInfo(), program.getTypeJavaLangString(),
              JBinaryOperator.ASG_ADD, x.getLhs(), newRhs);
          ctx.replaceMe(newExpr);
        }
      }
    }

    private JExpression convertString(JExpression expr) {
      JPrimitiveType charType = program.getTypePrimitiveChar();
      if (expr.getType() == charType) {
        if (expr instanceof JCharLiteral) {
          JCharLiteral charLit = (JCharLiteral) expr;
          return program.getLiteralString(new char[] {charLit.getValue()});
        } else {
          // Replace with Cast.charToString(c)
          JMethodCall call = new JMethodCall(program, expr.getSourceInfo(),
              null, program.getIndexedMethod("Cast.charToString"));
          call.getArgs().add(expr);
          return call;
        }
      } else if (expr.getType() == program.getTypePrimitiveLong()) {
        // Replace with LongLib.toString(l)
        JMethodCall call = new JMethodCall(program, expr.getSourceInfo(), null,
            program.getIndexedMethod("LongLib.toString"));
        call.getArgs().add(expr);
        return call;
      }
      return expr;
    }
  }

  /**
   * Handle integral divide operations which may have floating point results.
   */
  private class DivVisitor extends JModVisitor {

    @Override
    public void endVisit(JBinaryOperation x, Context ctx) {
      JType type = x.getType();
      if (x.getOp() == JBinaryOperator.DIV
          && type != program.getTypePrimitiveFloat()
          && type != program.getTypePrimitiveDouble()) {
        /*
         * If the numerator was already in range, we can assume the output is
         * also in range. Therefore, we don't need to do the full conversion,
         * but rather a narrowing int conversion instead.
         */
        String methodName = "Cast.narrow_" + type.getName();
        JMethod castMethod = program.getIndexedMethod(methodName);
        JMethodCall call = new JMethodCall(program, x.getSourceInfo(), null,
            castMethod, type);
        x.setType(program.getTypePrimitiveDouble());
        call.getArgs().add(x);
        ctx.replaceMe(call);
      }
    }
  }

  /**
   * Replaces all casts and instanceof operations with calls to implementation
   * methods.
   */
  private class ReplaceTypeChecksVisitor extends JModVisitor {

    @Override
    public void endVisit(JCastOperation x, Context ctx) {
      JExpression replaceExpr;
      JType toType = x.getCastType();
      JExpression expr = x.getExpr();
      if (toType instanceof JNullType) {
        /*
         * Magic: a null type cast means the user tried a cast that couldn't
         * possibly work. Typically this means either the statically resolvable
         * arg type is incompatible with the target type, or the target type was
         * globally uninstantiable. We handle this cast by throwing a
         * ClassCastException, unless the argument is null.
         */
        JMethod method = program.getIndexedMethod("Cast.throwClassCastExceptionUnlessNull");
        /*
         * Override the type of the magic method with the null type.
         */
        JMethodCall call = new JMethodCall(program, x.getSourceInfo(), null,
            method, program.getTypeNull());
        call.getArgs().add(expr);
        replaceExpr = call;
      } else if (toType instanceof JReferenceType) {
        JExpression curExpr = expr;
        JReferenceType refType = (JReferenceType) toType;
        JReferenceType argType = (JReferenceType) expr.getType();
        if (program.typeOracle.canTriviallyCast(argType, refType)) {
          // just remove the cast
          replaceExpr = curExpr;
        } else {
          boolean isJsoCast = program.isJavaScriptObject(toType);
          JMethod method = program.getIndexedMethod(isJsoCast
              ? "Cast.dynamicCastJso" : "Cast.dynamicCast");
          // override the type of the called method with the target cast type
          JMethodCall call = new JMethodCall(program, x.getSourceInfo(), null,
              method, toType);
          call.getArgs().add(curExpr);
          if (!isJsoCast) {
            JIntLiteral qId = program.getLiteralInt(queryIds.get(refType));
            call.getArgs().add(qId);
          }
          replaceExpr = call;
        }
      } else {
        /*
         * See JLS 5.1.3: if a cast narrows from one type to another, we must
         * call a narrowing conversion function. EXCEPTION: we currently have no
         * way to narrow double to float, so don't bother.
         */
        JPrimitiveType tByte = program.getTypePrimitiveByte();
        JPrimitiveType tChar = program.getTypePrimitiveChar();
        JPrimitiveType tShort = program.getTypePrimitiveShort();
        JPrimitiveType tInt = program.getTypePrimitiveInt();
        JPrimitiveType tLong = program.getTypePrimitiveLong();
        JPrimitiveType tFloat = program.getTypePrimitiveFloat();
        JPrimitiveType tDouble = program.getTypePrimitiveDouble();
        JType fromType = expr.getType();

        String methodName = null;

        if (tLong == fromType && tLong != toType) {
          if (tByte == toType || tShort == toType || tChar == toType) {
            /*
             * We need a double call here, one to convert long->int, and another
             * one to narrow. Construct the inner call here and fall through to
             * do the narrowing conversion.
             */
            JMethod castMethod = program.getIndexedMethod("LongLib.toInt");
            JMethodCall call = new JMethodCall(program, x.getSourceInfo(),
                null, castMethod);
            call.getArgs().add(expr);
            expr = call;
            fromType = tInt;
          } else if (tInt == toType) {
            methodName = "LongLib.toInt";
          } else if (tFloat == toType || tDouble == toType) {
            methodName = "LongLib.toDouble";
          }
        }

        if (toType == tLong && fromType != tLong) {
          // Longs get special treatment.
          if (tByte == fromType || tShort == fromType || tChar == fromType
              || tInt == fromType) {
            methodName = "LongLib.fromInt";
          } else if (tFloat == fromType || tDouble == fromType) {
            methodName = "LongLib.fromDouble";
          }
        } else if (tByte == fromType) {
          if (tChar == toType) {
            methodName = "Cast.narrow_" + toType.getName();
          }
        } else if (tShort == fromType) {
          if (tByte == toType || tChar == toType) {
            methodName = "Cast.narrow_" + toType.getName();
          }
        } else if (tChar == fromType) {
          if (tByte == toType || tShort == toType) {
            methodName = "Cast.narrow_" + toType.getName();
          }
        } else if (tInt == fromType) {
          if (tByte == toType || tShort == toType || tChar == toType) {
            methodName = "Cast.narrow_" + toType.getName();
          }
        } else if (tFloat == fromType || tDouble == fromType) {
          if (tByte == toType || tShort == toType || tChar == toType
              || tInt == toType) {
            methodName = "Cast.round_" + toType.getName();
          }
        }

        if (methodName != null) {
          JMethod castMethod = program.getIndexedMethod(methodName);
          JMethodCall call = new JMethodCall(program, x.getSourceInfo(), null,
              castMethod, toType);
          call.getArgs().add(expr);
          replaceExpr = call;
        } else {
          // Just remove the cast
          replaceExpr = expr;
        }
      }
      ctx.replaceMe(replaceExpr);
    }

    @Override
    public void endVisit(JInstanceOf x, Context ctx) {
      JReferenceType argType = (JReferenceType) x.getExpr().getType();
      JReferenceType toType = x.getTestType();
      if (program.typeOracle.canTriviallyCast(argType, toType)) {
        // trivially true if non-null; replace with a null test
        JNullLiteral nullLit = program.getLiteralNull();
        JBinaryOperation eq = new JBinaryOperation(program, x.getSourceInfo(),
            program.getTypePrimitiveBoolean(), JBinaryOperator.NEQ,
            x.getExpr(), nullLit);
        ctx.replaceMe(eq);
      } else {
        boolean isJsoCast = program.isJavaScriptObject(toType);
        JMethod method = program.getIndexedMethod(isJsoCast
            ? "Cast.instanceOfJso" : "Cast.instanceOf");
        JMethodCall call = new JMethodCall(program, x.getSourceInfo(), null,
            method);
        call.getArgs().add(x.getExpr());
        if (!isJsoCast) {
          JIntLiteral qId = program.getLiteralInt(queryIds.get(toType));
          call.getArgs().add(qId);
        }
        ctx.replaceMe(call);
      }
    }
  }

  public static void exec(JProgram program) {
    new CastNormalizer(program).execImpl();
  }

  private Map<JReferenceType, Integer> queryIds = new IdentityHashMap<JReferenceType, Integer>();

  private final JProgram program;

  private CastNormalizer(JProgram program) {
    this.program = program;
  }

  private void execImpl() {
    {
      ConcatVisitor visitor = new ConcatVisitor();
      visitor.accept(program);
    }
    {
      DivVisitor visitor = new DivVisitor();
      visitor.accept(program);
    }
    {
      AssignTypeIdsVisitor assigner = new AssignTypeIdsVisitor();
      assigner.accept(program);
      assigner.computeTypeIds();
    }
    {
      ReplaceTypeChecksVisitor replacer = new ReplaceTypeChecksVisitor();
      replacer.accept(program);
    }
  }

}
