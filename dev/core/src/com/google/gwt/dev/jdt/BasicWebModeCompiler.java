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
package com.google.gwt.dev.jdt;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.dev.javac.CompilationState;
import com.google.gwt.dev.javac.CompilationUnit;
import com.google.gwt.dev.javac.CompiledClass;
import com.google.gwt.dev.jjs.impl.TypeLinker;
import com.google.gwt.dev.util.JsniRef;
import com.google.gwt.dev.util.Memory;

import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Provides a basic front-end based on the JDT compiler that incorporates
 * GWT-specific concepts such as JSNI.
 */
public class BasicWebModeCompiler extends AbstractCompiler {

  public static CompilationResults getCompilationUnitDeclarations(TreeLogger logger,
      CompilationState state, TypeLinker linker, String... seedTypeNames)
      throws UnableToCompleteException {
    return new BasicWebModeCompiler(state, linker).getCompilationUnitDeclarations(logger,
        seedTypeNames);
  }

  private final TypeLinker linker;

  /**
   * Construct a BasicWebModeCompiler.
   */
  public BasicWebModeCompiler(CompilationState compilationState, TypeLinker linker) {
    super(compilationState, false);
    this.linker = linker;
  }

  /**
   * Build the initial set of compilation units.
   */
  public CompilationResults getCompilationUnitDeclarations(TreeLogger logger,
      String[] seedTypeNames, ICompilationUnit... additionalUnits) throws UnableToCompleteException {

    Map<String, CompiledClass> classMapBySource = compilationState.getClassFileMapBySource();

    /*
     * The alreadyAdded set prevents duplicate CompilationUnits from being added
     * to the icu list in the case of multiple JSO implementations as inner
     * classes in the same top-level class or seed classes as SingleJsoImpls
     * (e.g. JSO itself as the SingleImpl for all tag interfaces).
     */
    Set<CompilationUnit> alreadyAdded = new HashSet<CompilationUnit>();

    List<ICompilationUnit> icus =
        new ArrayList<ICompilationUnit>(seedTypeNames.length + additionalUnits.length);

    Collections.addAll(icus, additionalUnits);

    for (String seedTypeName : seedTypeNames) {
      CompilationUnit unit = getUnitForType(logger, classMapBySource, seedTypeName);

      if (unit == null) {
        continue;
      }

      if (alreadyAdded.add(unit)) {
        icus.add(new CompilationUnitAdapter(unit));
      }
    }

    /*
     * Compile, which will pull in everything else via the
     * doFindAdditionalTypesUsingFoo() methods.
     */
    CompilationResults units = compile(logger, icus.toArray(new ICompilationUnit[icus.size()]));
    Memory.maybeDumpMemory("WebModeCompiler");
    return units;
  }

  /**
   * Pull in types referenced only via JSNI.
   */
  @Override
  protected String[] doFindAdditionalTypesUsingJsni(TreeLogger logger,
      CompilationUnitDeclaration cud) {
    FindJsniRefVisitor v = new FindJsniRefVisitor();
    cud.traverse(v, cud.scope);
    Set<String> jsniRefs = v.getJsniRefs();
    Set<String> dependentTypeNames = new HashSet<String>();
    for (String jsniRef : jsniRefs) {
      JsniRef parsed = JsniRef.parse(jsniRef);
      if (parsed != null) {
        // If we fail to parse, don't add a class reference.
        dependentTypeNames.add(parsed.className());
      }
    }
    return dependentTypeNames.toArray(new String[dependentTypeNames.size()]);
  }

  /**
   * Get the CompilationUnit for a named type or throw an
   * UnableToCompleteException.
   */
  private CompilationUnit getUnitForType(TreeLogger logger,
      Map<String, CompiledClass> classMapBySource, String typeName)
      throws UnableToCompleteException {

    CompiledClass compiledClass = classMapBySource.get(typeName);
    if (compiledClass == null) {
      if (linker.isExternalType(typeName)) {
        return null;
      }
      logger.log(TreeLogger.ERROR, "Unable to find compilation unit for type '" + typeName + "'");
      throw new UnableToCompleteException();
    }

    assert compiledClass.getUnit() != null;
    return compiledClass.getUnit();
  }
}
