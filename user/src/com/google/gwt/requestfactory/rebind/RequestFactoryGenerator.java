/*
 * Copyright 2010 Google Inc.
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
package com.google.gwt.requestfactory.rebind;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JField;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JParameter;
import com.google.gwt.core.ext.typeinfo.JParameterizedType;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.NotFoundException;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.requestfactory.client.impl.AbstractBigDecimalRequest;
import com.google.gwt.requestfactory.client.impl.AbstractBigIntegerRequest;
import com.google.gwt.requestfactory.client.impl.AbstractBooleanRequest;
import com.google.gwt.requestfactory.client.impl.AbstractByteRequest;
import com.google.gwt.requestfactory.client.impl.AbstractCharacterRequest;
import com.google.gwt.requestfactory.client.impl.AbstractDateRequest;
import com.google.gwt.requestfactory.client.impl.AbstractDoubleRequest;
import com.google.gwt.requestfactory.client.impl.AbstractEnumRequest;
import com.google.gwt.requestfactory.client.impl.AbstractFloatRequest;
import com.google.gwt.requestfactory.client.impl.AbstractIntegerRequest;
import com.google.gwt.requestfactory.client.impl.AbstractJsonListRequest;
import com.google.gwt.requestfactory.client.impl.AbstractJsonObjectRequest;
import com.google.gwt.requestfactory.client.impl.AbstractLongRequest;
import com.google.gwt.requestfactory.client.impl.AbstractShortRequest;
import com.google.gwt.requestfactory.client.impl.AbstractStringRequest;
import com.google.gwt.requestfactory.client.impl.AbstractVoidRequest;
import com.google.gwt.requestfactory.client.impl.RecordImpl;
import com.google.gwt.requestfactory.client.impl.RecordJsoImpl;
import com.google.gwt.requestfactory.client.impl.RecordSchema;
import com.google.gwt.requestfactory.client.impl.RecordToTypeMap;
import com.google.gwt.requestfactory.client.impl.RequestFactoryJsonImpl;
import com.google.gwt.requestfactory.server.ReflectionBasedOperationRegistry;
import com.google.gwt.requestfactory.shared.RecordListRequest;
import com.google.gwt.requestfactory.shared.RecordRequest;
import com.google.gwt.requestfactory.shared.RequestData;
import com.google.gwt.requestfactory.shared.RequestFactory;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;
import com.google.gwt.valuestore.shared.Property;
import com.google.gwt.valuestore.shared.Record;
import com.google.gwt.valuestore.shared.RecordChangedEvent;
import com.google.gwt.valuestore.shared.WriteOperation;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * <p> <span style="color:red">Experimental API: This class is still under rapid
 * development, and is very likely to be deleted. Use it at your own risk.
 * </span> </p> Generates implementations of {@link com.google.gwt.requestfactory.shared.RequestFactory
 * RequestFactory} and its nested interfaces.
 */
public class RequestFactoryGenerator extends Generator {

  private final Set<JClassType> generatedRecordTypes
      = new HashSet<JClassType>();

  @Override
  public String generate(TreeLogger logger, GeneratorContext generatorContext,
      String interfaceName) throws UnableToCompleteException {
    // The TypeOracle knows about all types in the type system
    TypeOracle typeOracle = generatorContext.getTypeOracle();

    // Get a reference to the type that the generator should implement
    JClassType interfaceType = typeOracle.findType(interfaceName);

    // Ensure that the requested type exists
    if (interfaceType == null) {
      logger.log(TreeLogger.ERROR,
          "Could not find requested typeName: " + interfaceName);
      throw new UnableToCompleteException();
    }
    if (interfaceType.isInterface() == null) {
      // The incoming type wasn't a plain interface, we don't support
      // abstract base classes
      logger.log(TreeLogger.ERROR,
          interfaceType.getQualifiedSourceName() + " is not an interface.",
          null);
      throw new UnableToCompleteException();
    }

    String packageName = interfaceType.getPackage().getName();

    // the replace protects against inner classes
    String implName = interfaceType.getName().replace('.', '_') + "Impl";
    PrintWriter out = generatorContext.tryCreate(logger, packageName, implName);

    // If an implementation already exists, we don't need to do any work
    if (out != null) {
      generateOnce(typeOracle.findType(RequestFactory.class.getCanonicalName()),
          logger, generatorContext, out, interfaceType, packageName, implName);
    }

    return packageName + "." + implName;
  }

  private String asInnerImplClass(String className, JClassType outerClassName) {
    className = outerClassName.getQualifiedSourceName() + "Impl." + className;
    return className;
  }

  private String capitalize(String name) {
    return name.substring(0, 1).toUpperCase() + name.substring(1);
  }

  private void ensureRecordType(TreeLogger logger,
      GeneratorContext generatorContext, String packageName,
      JClassType publicRecordType) throws UnableToCompleteException {
    TypeOracle typeOracle = generatorContext.getTypeOracle();

    if (!publicRecordType.isAssignableTo(typeOracle.findType(Record.class.getName()))) {
      return;
    }

    if (generatedRecordTypes.contains(publicRecordType)) {
      return;
    }

    String recordImplTypeName = publicRecordType.getName() + "Impl";
    PrintWriter pw = generatorContext.tryCreate(logger, packageName,
        recordImplTypeName);

    Set<JClassType> transitiveDeps = new LinkedHashSet<JClassType>();

    if (pw != null) {
      logger = logger.branch(TreeLogger.DEBUG, "Generating "
          + publicRecordType.getName());

      ClassSourceFileComposerFactory f = new ClassSourceFileComposerFactory(
          packageName, recordImplTypeName);

      String eventTypeName = publicRecordType.getName() + "Changed";
      JClassType eventType = typeOracle.findType(packageName, eventTypeName);
      if (eventType == null) {
        logger.log(TreeLogger.ERROR,
            String.format("Cannot find %s implementation %s.%s",
                RecordChangedEvent.class.getName(), packageName,
                eventTypeName));
        throw new UnableToCompleteException();
      }

      f.addImport(AbstractJsonListRequest.class.getName());
      f.addImport(AbstractJsonObjectRequest.class.getName());
      f.addImport(RequestFactoryJsonImpl.class.getName());
      f.addImport(Property.class.getName());
      f.addImport(Record.class.getName());
      f.addImport(RecordImpl.class.getName());
      f.addImport(RecordJsoImpl.class.getName());
      f.addImport(RecordSchema.class.getName());
      f.addImport(WriteOperation.class.getName().replace("$", "."));

      f.addImport(Collections.class.getName());
      f.addImport(HashSet.class.getName());
      f.addImport(Set.class.getName());
      f.addImport(Map.class.getName());
      f.addImport(HashMap.class.getName());

      f.setSuperclass(RecordImpl.class.getSimpleName());
      f.addImplementedInterface(publicRecordType.getName());

      SourceWriter sw = f.createSourceWriter(generatorContext, pw);
      sw.println();

      JClassType propertyType = printSchema(typeOracle, publicRecordType,
          recordImplTypeName, eventType, sw);

      sw.println();
      String simpleImplName = publicRecordType.getSimpleSourceName() + "Impl";
      printRequestImplClass(sw, publicRecordType, simpleImplName, true);
      printRequestImplClass(sw, publicRecordType, simpleImplName, false);

      sw.println();
      sw.println(String.format(
          "public static final RecordSchema<%s> SCHEMA = new MySchema();",
          recordImplTypeName));

      sw.println();
      sw.println(String.format("private %s(RecordJsoImpl jso, boolean isFuture) {",
          recordImplTypeName));
      sw.indent();
      sw.println("super(jso, isFuture);");
      sw.outdent();
      sw.println("}");

      // getter methods
      for (JField field : publicRecordType.getFields()) {
        JType fieldType = field.getType();
        if (propertyType.getErasedType() == fieldType.getErasedType()) {
          JParameterizedType parameterized = fieldType.isParameterized();
          if (parameterized == null) {
            logger.log(TreeLogger.ERROR,
                fieldType + " must have its param type set.");
            throw new UnableToCompleteException();
          }
          JClassType returnType = parameterized.getTypeArgs()[0];
          sw.println();
          sw.println(String.format("public %s get%s() {",
              returnType.getQualifiedSourceName(),
              capitalize(field.getName())));
          sw.indent();
          sw.println(String.format("return get(%s);", field.getName()));
          sw.outdent();
          sw.println("}");
          /*
           * Because a Proxy A may relate to B which relates to C, we need to
           * ensure transitively.
           */
          if (isRecordType(typeOracle, returnType)) {
            transitiveDeps.add(returnType);
          }
        }
      }

      // setter methods
      for (JField field : publicRecordType.getFields()) {
        JType fieldType = field.getType();
        if (propertyType.getErasedType() == fieldType.getErasedType()) {
          JParameterizedType parameterized = fieldType.isParameterized();
          if (parameterized == null) {
            logger.log(TreeLogger.ERROR, fieldType
                + " must have its param type set.");
            throw new UnableToCompleteException();
          }
          JClassType returnType = parameterized.getTypeArgs()[0];
          sw.println();
          String varName = field.getName();
          sw.println(String.format("public void set%s(%s %s) {",
              capitalize(field.getName()), returnType.getQualifiedSourceName(),
              varName));
          sw.indent();
          sw.println(String.format("set(this.%s, this, %s);", field.getName(), varName));
          sw.outdent();
          sw.println("}");
        }
      }

      sw.outdent();
      sw.println("}");
      generatorContext.commit(logger, pw);
    }

    generatedRecordTypes.add(publicRecordType);
    // ensure generatation of transitive dependencies
    for (JClassType type : transitiveDeps) {
      ensureRecordType(logger, generatorContext, type.getPackage().getName(),
          type);
    }
  }

  private void generateOnce(JClassType requestFactoryType, TreeLogger logger,
      GeneratorContext generatorContext, PrintWriter out,
      JClassType interfaceType, String packageName, String implName)
      throws UnableToCompleteException {

    logger = logger.branch(TreeLogger.DEBUG,
        String.format("Generating implementation of %s",
            interfaceType.getName()));

    ClassSourceFileComposerFactory f = new ClassSourceFileComposerFactory(
        packageName, implName);
    f.addImport(HandlerManager.class.getName());
    f.addImport(RequestFactoryJsonImpl.class.getName());
    f.addImport(interfaceType.getQualifiedSourceName());
    f.addImport(RecordToTypeMap.class.getName());
    f.addImport(Record.class.getName());
    f.addImport(RecordSchema.class.getName());
    f.addImplementedInterface(interfaceType.getName());
    f.setSuperclass(RequestFactoryJsonImpl.class.getSimpleName());

    SourceWriter sw = f.createSourceWriter(generatorContext, out);
    sw.println();

    // Find the requestSelector methods
    // TODO allow getRequest type methods to live directly on the factory, w/o
    // requiring a selector to get to them
    // TODO rename variable this requestBuilders, holding off to avoid merge
    // hell
    Set<JMethod> requestSelectors = new LinkedHashSet<JMethod>();
    for (JMethod method : interfaceType.getOverridableMethods()) {
      if (method.getEnclosingType().equals(requestFactoryType)) {
        continue;
      }
      JType returnType = method.getReturnType();
      if (null == returnType) {
        logger.log(TreeLogger.ERROR, String.format(
            "Illegal return type for %s. Methods of %s must return interfaces, found void",
            method.getName(), interfaceType.getName()));
        throw new UnableToCompleteException();
      }
      JClassType asInterface = returnType.isInterface();
      if (null == asInterface) {
        logger.log(TreeLogger.ERROR, String.format(
            "Illegal return type for %s. Methods of %s must return interfaces",
            method.getName(), interfaceType.getName()));
        throw new UnableToCompleteException();
      }
      requestSelectors.add(method);
    }

    // In addition to the request selectors in the generated interface, there
    // are a few which are in RequestFactory which also need to have
    // implementations generated. Hard coding the addition of these here for now
    JClassType t = generatorContext.getTypeOracle().findType(
        RequestFactory.class.getName());
    try {
      requestSelectors.add(t.getMethod("loggingRequest", new JType[0]));
    } catch (NotFoundException e) {
      e.printStackTrace();
    }

    JClassType recordToTypeInterface = generatorContext.getTypeOracle().findType(RecordToTypeMap.class.getName());
    // TODO: note, this seems like a bug. What if you have 2 RequestFactories?
    String recordToTypeMapName = recordToTypeInterface.getName() + "Impl";

    // write create(Class)
    sw.println("public " + Record.class.getName() + " create(Class token) {");
    sw.indent();
    sw.println("return create(token, new " + recordToTypeMapName + "());");
    sw.outdent();
    sw.println("}");
    sw.println();

    // write getClass(String)
    sw.println("public Class<? extends " + Record.class.getName() + "> getClass(String token) {");
    sw.indent();
    sw.println("return getClass(token, new " + recordToTypeMapName + "());");
    sw.outdent();
    sw.println("}");
    sw.println();

    // write getProxy(String)
    sw.println("public " + Record.class.getName() + " getProxy(String token) {");
    sw.indent();
    sw.println("return getProxy(token, new " + recordToTypeMapName + "());");
    sw.outdent();
    sw.println("}");
    sw.println();

    // write getToken(Record)
    sw.println("public String getToken(Record record) {");
    sw.indent();
    sw.println("return getToken(record, new " + recordToTypeMapName + "());");
    sw.outdent();
    sw.println("}");
    sw.println();

    // write getToken(Class)
    sw.println("public String getToken(Class clazz) {");
    sw.indent();
    sw.println("return new " + recordToTypeMapName + "().getClassToken(clazz);");
    sw.outdent();
    sw.println("}");
    sw.println();

    sw.println(
           "public RecordSchema<? extends Record> getSchema(String schemaToken) {");
    sw.indent();
    sw.println("return new " + recordToTypeMapName + "().getType(schemaToken);");
    sw.outdent();
    sw.println("}");

    // write a method for each request builder and generate it
    for (JMethod requestSelector : requestSelectors) {
      String returnTypeName = requestSelector.getReturnType().getQualifiedSourceName();
      String nestedImplName =
          capitalize(requestSelector.getName().replace('.', '_')) + "Impl";
      String nestedImplPackage = generatorContext.getTypeOracle().findType(
          returnTypeName).getPackage().getName();

      sw.println("public " + returnTypeName + " " + requestSelector.getName()
          + "() {");
      sw.indent();
      sw.println(
          "return new " + nestedImplPackage + "." + nestedImplName + "(this);");
      sw.outdent();
      sw.println("}");
      sw.println();

      PrintWriter pw = generatorContext.tryCreate(logger, nestedImplPackage,
          nestedImplName);
      if (pw != null) {
        generateRequestSelectorImplementation(logger, generatorContext, pw,
            requestSelector, interfaceType, nestedImplPackage, nestedImplName);
      }
    }

    // close the class
    sw.outdent();
    sw.println("}");

    // generate the mapping type implementation
    PrintWriter pw = generatorContext.tryCreate(logger, packageName,
        recordToTypeMapName);
    if (pw != null) {
      generateRecordToTypeMap(logger, generatorContext, pw,
          recordToTypeInterface, packageName, recordToTypeMapName);
    }
    generatorContext.commit(logger, out);
  }

  private void generateRecordToTypeMap(TreeLogger logger,
      GeneratorContext generatorContext, PrintWriter out,
      JClassType interfaceType, String packageName, String implName) {
    logger = logger.branch(TreeLogger.DEBUG,
        String.format("Generating implementation of %s",
            interfaceType.getName()));

    ClassSourceFileComposerFactory f = new ClassSourceFileComposerFactory(
        packageName, implName);
    f.addImport(interfaceType.getQualifiedSourceName());
    f.addImport(Record.class.getName());
    f.addImport(RecordSchema.class.getName());
    f.addImport(interfaceType.getQualifiedSourceName());
    f.addImplementedInterface(interfaceType.getName());

    f.addImplementedInterface(interfaceType.getName());

    SourceWriter sw = f.createSourceWriter(generatorContext, out);
    sw.println();

    sw.println("public <R extends Record> RecordSchema<R> getType(Class<R> recordClass) {");
    sw.indent();
    for (JClassType publicRecordType : generatedRecordTypes) {
      String qualifiedSourceName = publicRecordType.getQualifiedSourceName();
      sw.println("if (recordClass == " + qualifiedSourceName + ".class) {");
      sw.indent();
      sw.println("return (RecordSchema<R>) " + qualifiedSourceName + "Impl.SCHEMA;");
      sw.outdent();
      sw.println("}");
    }
    sw.println(
        "throw new IllegalArgumentException(\"Unknown recordClass \" + recordClass);");
    sw.indent();
    sw.outdent();
    sw.outdent();
    sw.println("}");

    sw.println("public RecordSchema<? extends Record> getType(String token) {");
    sw.indent();
    sw.println("String[] bits = token.split(\"-\");");
    for (JClassType publicRecordType : generatedRecordTypes) {
      String qualifiedSourceName = publicRecordType.getQualifiedSourceName();
      sw.println("if (bits[0].equals(\"" + qualifiedSourceName + "\")) {");
      sw.indent();
      sw.println("return " + qualifiedSourceName + "Impl.SCHEMA;");
      sw.outdent();
      sw.println("}");
    }
    sw.println("throw new IllegalArgumentException(\"Unknown string token: \" + token);");
    sw.outdent();
    sw.println("}");

    sw.println("public String getClassToken(Class<?> recordClass) {");
    sw.indent();
    for (JClassType publicRecordType : generatedRecordTypes) {
      String qualifiedSourceName = publicRecordType.getQualifiedSourceName();
      sw.println("if (recordClass == " + qualifiedSourceName + ".class) {");
      sw.indent();
      sw.println("return \"" + qualifiedSourceName + "\";");
      sw.outdent();
      sw.println("}");
    }
    sw.println(
        "throw new IllegalArgumentException(\"Unknown recordClass \" + recordClass);");
    sw.indent();
    sw.outdent();
    sw.outdent();
    sw.println("}");


    sw.outdent();
    sw.println("}");
    sw.println();

    generatorContext.commit(logger, out);
  }

  private void generateRequestSelectorImplementation(TreeLogger logger,
      GeneratorContext generatorContext, PrintWriter out,
      JMethod selectorMethod, JClassType mainType, String packageName,
      String implName) throws UnableToCompleteException {
    JClassType selectorInterface = selectorMethod.getReturnType().isInterface();
    logger = logger.branch(TreeLogger.DEBUG,
        String.format("Generating implementation of %s",
            selectorInterface.getName()));

    ClassSourceFileComposerFactory f = new ClassSourceFileComposerFactory(
        packageName, implName);
    f.addImport(RequestData.class.getName());
    f.addImport(mainType.getQualifiedSourceName() + "Impl");
    f.addImplementedInterface(selectorInterface.getQualifiedSourceName());

    SourceWriter sw = f.createSourceWriter(generatorContext, out);
    sw.println();

    sw.println("private final " + mainType.getName() + "Impl factory;");
    sw.println();
    // constructor for the class.
    sw.println(
        "public " + implName + "(" + mainType.getName() + "Impl factory) {");
    sw.indent();
    sw.println("this.factory = factory;");
    sw.outdent();
    sw.println("}");
    sw.println();

    // write each method.
    for (JMethod method : selectorInterface.getOverridableMethods()) {
      JClassType returnType = method.getReturnType().isParameterized().getTypeArgs()[0];

      ensureRecordType(logger, generatorContext,
          returnType.getPackage().getName(), returnType);

      String operationName = selectorInterface.getQualifiedBinaryName()
          + ReflectionBasedOperationRegistry.SCOPE_SEPARATOR + method.getName();

      JClassType requestType = method.getReturnType().isClassOrInterface();
      String requestClassName = null;

      TypeOracle typeOracle = generatorContext.getTypeOracle();
      String enumArgument = "";
      // TODO: refactor this into some kind of extensible map lookup
      if (isRecordListRequest(typeOracle, requestType)) {
        requestClassName = asInnerImplClass("ListRequestImpl", returnType);
      } else if (isRecordRequest(typeOracle, requestType)) {
        requestClassName = asInnerImplClass("ObjectRequestImpl", returnType);
      } else if (isStringRequest(typeOracle, requestType)) {
        requestClassName = AbstractStringRequest.class.getName();
      } else if (isLongRequest(typeOracle, requestType)) {
        requestClassName = AbstractLongRequest.class.getName();
      } else if (isIntegerRequest(typeOracle, requestType)) {
        requestClassName = AbstractIntegerRequest.class.getName();
      } else if (isDoubleRequest(typeOracle, requestType)) {
        requestClassName = AbstractDoubleRequest.class.getName();
      } else if (isByteRequest(typeOracle, requestType)) {
        requestClassName = AbstractByteRequest.class.getName();
      } else if (isBooleanRequest(typeOracle, requestType)) {
        requestClassName = AbstractBooleanRequest.class.getName();
      } else if (isShortRequest(typeOracle, requestType)) {
        requestClassName = AbstractShortRequest.class.getName();
      } else if (isFloatRequest(typeOracle, requestType)) {
        requestClassName = AbstractFloatRequest.class.getName();
      } else if (isCharacterRequest(typeOracle, requestType)) {
        requestClassName = AbstractCharacterRequest.class.getName();
      } else if (isDateRequest(typeOracle, requestType)) {
        requestClassName = AbstractDateRequest.class.getName();
      } else if (isBigDecimalRequest(typeOracle, requestType)) {
        requestClassName = AbstractBigDecimalRequest.class.getName();
      } else if (isBigIntegerRequest(typeOracle, requestType)) {
        requestClassName = AbstractBigIntegerRequest.class.getName();
      } else if (isEnumRequest(typeOracle, requestType)) {
        requestClassName = AbstractEnumRequest.class.getName();
        enumArgument = ", " + requestType.isParameterized().getTypeArgs()[0]
            + ".values()";
      } else if (isVoidRequest(typeOracle, requestType)) {
        requestClassName = AbstractVoidRequest.class.getName();
      } else {
        logger.log(TreeLogger.ERROR,
            "Return type " + requestType + " is not yet supported");
        throw new UnableToCompleteException();
      }

      sw.println(getMethodDeclaration(method) + " {");
      sw.indent();
      sw.println(
          "return new " + requestClassName + "(factory" + enumArgument + ") {");
      sw.indent();
      String requestDataName = RequestData.class.getSimpleName();
      sw.println("public " + requestDataName + " getRequestData() {");
      sw.indent();
      sw.println(
          "return new " + requestDataName + "(\"" + operationName + "\", "
              + getParametersAsString(method, typeOracle) + ","
              + "getPropertyRefs());");
      sw.outdent();
      sw.println("}");
      sw.outdent();
      sw.println("};");
      sw.outdent();
      sw.println("}");
    }

    sw.outdent();
    sw.println("}");
    generatorContext.commit(logger, out);
  }

  /**
   * This method is very similar to {@link com.google.gwt.core.ext.typeinfo.JMethod.getReadableDeclaration()}.
   * The only change is that each parameter is final.
   */
  private String getMethodDeclaration(JMethod method) {
    StringBuilder sb = new StringBuilder("public ");
    sb.append(method.getReturnType().getParameterizedQualifiedSourceName());
    sb.append(" ");
    sb.append(method.getName());
    sb.append("(");

    boolean needComma = false;
    for (JParameter param : method.getParameters()) {
      if (needComma) {
        sb.append(", ");
      } else {
        needComma = true;
      }
      sb.append("final "); // so that an anonymous class can refer it
      sb.append(param.getType().getParameterizedQualifiedSourceName());
      sb.append(" ");
      sb.append(param.getName());
    }
    sb.append(")");
    return sb.toString();
  }

  /**
   * Returns the string representation of the parameters to be passed to the
   * server side method.
   */
  private String getParametersAsString(JMethod method, TypeOracle typeOracle) {
    StringBuilder sb = new StringBuilder();
    for (JParameter parameter : method.getParameters()) {
      if (sb.length() > 0) {
        sb.append(", ");
      }
      JClassType classType = parameter.getType().isClassOrInterface();

      JType paramType = parameter.getType();
      boolean isRef =
          "com.google.gwt.valuestore.shared.PropertyReference".equals(
          paramType.getQualifiedBinaryName());
      JParameterizedType params = paramType.isParameterized();
      if (params != null) {
        classType = params.getTypeArgs()[0];
      }
      if (classType != null
          && classType.isAssignableTo(typeOracle.findType(Record.class.getName()))) {
        sb.append("((" + classType.getQualifiedBinaryName() + "Impl" + ")");
      }
      sb.append(parameter.getName());
      // TODO No. This defeats the entire purpose of PropertyReference. It's
      // supposed
      // to be dereferenced server side, not client side.
      if (isRef) {
        sb.append(".get()");
      }
      if (classType != null
          && classType.isAssignableTo(typeOracle.findType(Record.class.getName()))) {
        sb.append(").getUniqueId()");
      }
    }
    return "new Object[] {" + sb.toString() + "}";
  }

  private boolean isBigDecimalRequest(TypeOracle typeOracle,
      JClassType requestType) {
    return requestType.isParameterized().getTypeArgs()[0].isAssignableTo(typeOracle.findType(BigDecimal.class.getName()));
  }

  private boolean isBigIntegerRequest(TypeOracle typeOracle,
      JClassType requestType) {
    return requestType.isParameterized().getTypeArgs()[0].isAssignableTo(typeOracle.findType(BigInteger.class.getName()));
  }

  private boolean isBooleanRequest(TypeOracle typeOracle,
      JClassType requestType) {
    return requestType.isParameterized().getTypeArgs()[0].isAssignableTo(typeOracle.findType(Boolean.class.getName()));
  }

  private boolean isByteRequest(TypeOracle typeOracle, JClassType requestType) {
    return requestType.isParameterized().getTypeArgs()[0].isAssignableTo(typeOracle.findType(Byte.class.getName()));
  }

  private boolean isCharacterRequest(TypeOracle typeOracle,
      JClassType requestType) {
    return requestType.isParameterized().getTypeArgs()[0].isAssignableTo(typeOracle.findType(Character.class.getName()));
  }

  private boolean isDateRequest(TypeOracle typeOracle, JClassType requestType) {
    return requestType.isParameterized().getTypeArgs()[0].isAssignableTo(typeOracle.findType(Date.class.getName()));
  }

  private boolean isDoubleRequest(TypeOracle typeOracle,
      JClassType requestType) {
    return requestType.isParameterized().getTypeArgs()[0].isAssignableTo(typeOracle.findType(Double.class.getName()));
  }

  private boolean isEnumRequest(TypeOracle typeOracle, JClassType requestType) {
    return requestType.isParameterized().getTypeArgs()[0].isAssignableTo(typeOracle.findType(Enum.class.getName()));
  }

  private boolean isFloatRequest(TypeOracle typeOracle,
      JClassType requestType) {
    return requestType.isParameterized().getTypeArgs()[0].isAssignableTo(typeOracle.findType(Float.class.getName()));
  }

  private boolean isIntegerRequest(TypeOracle typeOracle,
      JClassType requestType) {
    return requestType.isParameterized().getTypeArgs()[0].isAssignableTo(typeOracle.findType(Integer.class.getName()));
  }

  private boolean isLongRequest(TypeOracle typeOracle, JClassType requestType) {
    return requestType.isParameterized().getTypeArgs()[0].isAssignableTo(typeOracle.findType(Long.class.getName()));
  }

  private boolean isRecordListRequest(TypeOracle typeOracle,
      JClassType requestType) {
    return requestType.isAssignableTo(typeOracle.findType(RecordListRequest.class.getName()));
  }

  private boolean isRecordRequest(TypeOracle typeOracle,
      JClassType requestType) {
    return requestType.isAssignableTo(typeOracle.findType(RecordRequest.class.getName()));
  }

  private boolean isRecordType(TypeOracle typeOracle, JClassType requestType) {
    return requestType.isAssignableTo(typeOracle.findType(Record.class.getName()));
  }

  private boolean isShortRequest(TypeOracle typeOracle,
      JClassType requestType) {
    return requestType.isParameterized().getTypeArgs()[0].isAssignableTo(typeOracle.findType(Short.class.getName()));
  }

  private boolean isStringRequest(TypeOracle typeOracle,
      JClassType requestType) {
    return requestType.isParameterized().getTypeArgs()[0].isAssignableTo(typeOracle.findType(String.class.getName()));
  }

  private boolean isVoidRequest(TypeOracle typeOracle, JClassType requestType) {
    return requestType.isParameterized().getTypeArgs()[0].isAssignableTo(typeOracle.findType(Void.class.getName()));
  }

  /**
   * Prints a ListRequestImpl or ObjectRequestImpl class.
   */
  private void printRequestImplClass(SourceWriter sw, JClassType returnType,
      String returnImplTypeName, boolean list) {

    String name = list ? "ListRequestImpl" : "ObjectRequestImpl";
    Class<?> superClass = list ? AbstractJsonListRequest.class
        : AbstractJsonObjectRequest.class;

    sw.println("public static abstract class " + name + " extends "
        + superClass.getSimpleName() + "<" + returnType.getName() + ", " + name
        + "> {");
    sw.println();
    sw.indent();

    sw.println(String.format("%s(%s factory) {", name,
        RequestFactoryJsonImpl.class.getSimpleName()));
    sw.indent();
    sw.println("super(" + returnImplTypeName + ".SCHEMA, factory);");
    sw.outdent();
    sw.println("}");
    sw.println();
    sw.println("@Override");
    sw.println("protected " + name + " getThis() {");
    sw.indent();
    sw.println("return this;");
    sw.outdent();
    sw.println("}");

    sw.outdent();
    sw.println("}");
    sw.println();
  }

  /**
   * @param typeOracle
   * @param publicRecordType
   * @param recordImplTypeName
   * @param eventType
   * @param sw
   * @return
   * @throws UnableToCompleteException
   */

  private JClassType printSchema(TypeOracle typeOracle,
      JClassType publicRecordType, String recordImplTypeName,
      JClassType eventType, SourceWriter sw) {
    sw.println(
        String.format("public static class MySchema extends RecordSchema<%s> {",
            recordImplTypeName));

    sw.indent();
    sw.println("private final Set<Property<?>> allProperties;");
    sw.println("{");

    sw.indent();
    sw.println("Set<Property<?>> set = new HashSet<Property<?>>();");
    sw.println("set.addAll(super.allProperties());");

    JClassType propertyType;
    try {
      propertyType = typeOracle.getType(Property.class.getName());
    } catch (NotFoundException e) {
      throw new RuntimeException(e);
    }

    for (JField field : publicRecordType.getFields()) {
      if (propertyType.getErasedType() == field.getType().getErasedType()) {
        sw.println(String.format("set.add(%s);", field.getName()));
      }
    }

    sw.println("allProperties = Collections.unmodifiableSet(set);");
    sw.outdent();
    sw.println("}");

    sw.println();
    sw.println("public Set<Property<?>> allProperties() {");
    sw.indent();
    sw.println("return allProperties;");
    sw.outdent();
    sw.println("}");

    sw.println();
    sw.println("public MySchema() {");
    sw.indent();
    sw.println("super(\"" + publicRecordType.getQualifiedSourceName() + "\");");
    sw.outdent();
    sw.println("}");

    sw.println();
    sw.println("@Override");
    sw.println(String.format("public %s create(RecordJsoImpl jso, boolean isFuture) {",
        recordImplTypeName));
    sw.indent();
    sw.println(String.format("return new %s(jso, isFuture);", recordImplTypeName));
    sw.outdent();
    sw.println("}");

    sw.println();
    sw.println("@Override");
    sw.println(String.format(
        "public %s createChangeEvent(Record record, WriteOperation writeOperation) {",
        eventType.getName()));
    sw.indent();
    sw.println(String.format("return new %s((%s) record, writeOperation);",
        eventType.getName(), publicRecordType.getName()));
    sw.outdent();
    sw.println("}");

    sw.println();
    sw.println("public Class getProxyClass() {");
    sw.indent();
    sw.println("return " + publicRecordType.getQualifiedSourceName() + ".class;"
        + " // special field");
    sw.outdent();
    sw.println("}");

    sw.outdent();
    sw.println("}");
    return propertyType;
  }
}
