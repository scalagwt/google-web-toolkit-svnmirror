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
package com.google.gwt.junit.rebind;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.ext.BadPropertyValueException;
import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.NotFoundException;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.dev.util.PerfLogger;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.junit.client.impl.GWTRunner;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;

import java.io.PrintWriter;
import java.util.Set;
import java.util.TreeSet;

/**
 * This class generates a stub class for classes that derive from GWTTestCase.
 * This stub class provides the necessary bridge between our Hosted or Hybrid
 * mode classes and the JUnit system.
 * 
 */
public class GWTRunnerGenerator extends Generator {

  private static final String GWT_RUNNER_NAME = GWTRunner.class.getName();

  /**
   * Create a new type that satisfies the rebind request.
   */
  @Override
  public String generate(TreeLogger logger, GeneratorContext context,
      String typeName) throws UnableToCompleteException {

    if (!GWT_RUNNER_NAME.equals(typeName)) {
      logger.log(TreeLogger.ERROR, "This generator may only be used with "
          + GWT_RUNNER_NAME, null);
      throw new UnableToCompleteException();
    }

    JClassType requestedClass;
    try {
      requestedClass = context.getTypeOracle().getType(typeName);
    } catch (NotFoundException e) {
      logger.log(
          TreeLogger.ERROR,
          "Could not find type '"
              + typeName
              + "'; please see the log, as this usually indicates a previous error ",
          e);
      throw new UnableToCompleteException();
    }

    String moduleName;
    try {
      moduleName = context.getPropertyOracle().getPropertyValue(logger,
          "junit.moduleName");
    } catch (BadPropertyValueException e) {
      logger.log(TreeLogger.ERROR,
          "Could not resolve junit.moduleName property", e);
      throw new UnableToCompleteException();
    }

    // Get the stub class name, and see if its source file exists.
    //
    String generatedClass = requestedClass.getSimpleSourceName() + "Impl";
    String packageName = requestedClass.getPackage().getName();
    String qualifiedStubClassName = packageName + "." + generatedClass;

    SourceWriter sourceWriter = getSourceWriter(logger, context, packageName,
        generatedClass, GWT_RUNNER_NAME);

    if (sourceWriter != null) {
      PerfLogger.start("GWTRunnerGenerator");
      JClassType[] allTestTypes = getAllTestTypes(context.getTypeOracle());
      Set<String> testClasses = getTestTypesForModule(logger, moduleName,
          allTestTypes);
      writeCreateNewTestCaseMethod(testClasses, sourceWriter);
      sourceWriter.commit(logger);
      PerfLogger.end();
    }

    return qualifiedStubClassName;
  }

  private JClassType[] getAllTestTypes(TypeOracle typeOracle) {
    PerfLogger.start("GWTRunnerGenerator.getAllTestTypes");
    JClassType gwtTestType = typeOracle.findType(GWTTestCase.class.getName());
    PerfLogger.end();
    if (gwtTestType != null) {
      return gwtTestType.getSubtypes();
    } else {
      return new JClassType[0];
    }
  }

  private SourceWriter getSourceWriter(TreeLogger logger, GeneratorContext ctx,
      String packageName, String className, String superclassName) {
    PrintWriter printWriter = ctx.tryCreate(logger, packageName, className);
    if (printWriter == null) {
      return null;
    }

    ClassSourceFileComposerFactory composerFactory = new ClassSourceFileComposerFactory(
        packageName, className);
    composerFactory.setSuperclass(superclassName);
    composerFactory.addImport(GWTTestCase.class.getName());
    composerFactory.addImport(GWT.class.getName());
    return composerFactory.createSourceWriter(ctx, printWriter);
  }

  private Set<String> getTestTypesForModule(TreeLogger logger,
      String moduleName, JClassType[] allTestTypes) {
    PerfLogger.start("GWTRunnerGenerator.getTestTypesForModule");
    // Must use sorted set to prevent nondeterminism.
    Set<String> testClasses = new TreeSet<String>();
    for (JClassType classType : allTestTypes) {
      if (!classType.isPublic() || classType.isAbstract()
          || !classType.isDefaultInstantiable()) {
        continue;
      }

      String className = classType.getQualifiedSourceName();

      try {
        Class<?> testClass = Class.forName(className);
        GWTTestCase instantiated = (GWTTestCase) testClass.newInstance();
        if (!moduleName.equals(instantiated.getModuleName())) {
          continue;
        }
      } catch (Throwable e) {
        logger.log(
            TreeLogger.INFO,
            "Error determining if test class '"
                + className
                + "' is a part of the current module; skipping; expect subsequent errors if this test class is run",
            e);
        continue;
      }
      testClasses.add(className);
    }
    PerfLogger.end();
    return testClasses;
  }

  private void writeCreateNewTestCaseMethod(Set<String> testClasses,
      SourceWriter sw) {
    PerfLogger.start("GWTRunnerGenerator.writeCreateNewTestCaseMethod");
    sw.println();
    sw.println("protected final GWTTestCase createNewTestCase(String testClass) {");
    sw.indent();
    boolean isFirst = true;
    for (String className : testClasses) {
      if (isFirst) {
        isFirst = false;
      } else {
        sw.print("else ");
      }

      sw.println("if (testClass.equals(\"" + className + "\")) {");
      sw.indentln("return GWT.create(" + className + ".class);");
      sw.println("}");
    }
    sw.println("return null;");
    sw.outdent();
    sw.println("}");
    PerfLogger.end();
  }
}
