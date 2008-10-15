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
package com.google.gwt.dev;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.linker.ArtifactSet;
import com.google.gwt.core.ext.linker.SelectionProperty;
import com.google.gwt.core.ext.linker.impl.StandardCompilationResult;
import com.google.gwt.core.ext.linker.impl.StandardLinkerContext;
import com.google.gwt.dev.CompileTaskRunner.CompileTask;
import com.google.gwt.dev.cfg.BindingProperty;
import com.google.gwt.dev.cfg.ModuleDef;
import com.google.gwt.dev.cfg.ModuleDefLoader;
import com.google.gwt.dev.cfg.StaticPropertyOracle;
import com.google.gwt.dev.util.Util;
import com.google.gwt.dev.util.arg.ArgHandlerLogLevel;
import com.google.gwt.dev.util.arg.ArgHandlerModuleName;
import com.google.gwt.dev.util.arg.ArgHandlerOutDir;
import com.google.gwt.dev.util.arg.ArgHandlerTreeLoggerFlag;
import com.google.gwt.util.tools.ToolBase;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Performs the first phase of compilation, generating the set of permutations
 * to compile, and a ready-to-compile AST.
 */
public class Link {

  static class ArgProcessor extends ToolBase {
    public ArgProcessor(CompileTaskOptions options) {
      registerHandler(new ArgHandlerLogLevel(options));
      registerHandler(new ArgHandlerTreeLoggerFlag(options));
      registerHandler(new ArgHandlerOutDir(options));
      registerHandler(new ArgHandlerModuleName(options));
    }

    /*
     * Overridden to make public.
     */
    @Override
    public boolean processArgs(String[] args) {
      return super.processArgs(args);
    }

    @Override
    protected String getName() {
      return Link.class.getName();
    }
  }

  public static ArtifactSet link(TreeLogger logger, ModuleDef module,
      Precompilation precompilation, File[] jsFiles)
      throws UnableToCompleteException {
    StandardLinkerContext linkerContext = new StandardLinkerContext(logger,
        module, precompilation.getUnifiedAst().getOptions());
    return doLink(logger, linkerContext, precompilation, jsFiles);
  }

  public static void main(String[] args) {
    /*
     * NOTE: main always exits with a call to System.exit to terminate any
     * non-daemon threads that were started in Generators. Typically, this is to
     * shutdown AWT related threads, since the contract for their termination is
     * still implementation-dependent.
     */
    final CompileTaskOptions options = new CompileTaskOptionsImpl();
    if (new ArgProcessor(options).processArgs(args)) {
      CompileTask task = new CompileTask() {
        public boolean run(TreeLogger logger) throws UnableToCompleteException {
          return new Link(options).run(logger);
        }
      };
      if (CompileTaskRunner.runWithAppropriateLogger(options, task)) {
        // Exit w/ success code.
        System.exit(0);
      }
    }
    // Exit w/ non-success code.
    System.exit(1);
  }

  private static ArtifactSet doLink(TreeLogger logger,
      StandardLinkerContext linkerContext, Precompilation precompilation,
      File[] jsFiles) throws UnableToCompleteException {
    Permutation[] perms = precompilation.getPermutations();
    if (perms.length != jsFiles.length) {
      throw new IllegalArgumentException(
          "Mismatched jsFiles.length and permutation count");
    }

    for (int i = 0; i < perms.length; ++i) {
      finishPermuation(logger, perms[i], jsFiles[i], linkerContext);
    }

    linkerContext.addOrReplaceArtifacts(precompilation.getGeneratedArtifacts());
    return linkerContext.invokeLinkerStack(logger);
  }

  private static void finishPermuation(TreeLogger logger, Permutation perm,
      File jsFile, StandardLinkerContext linkerContext)
      throws UnableToCompleteException {
    StandardCompilationResult compilation = linkerContext.getCompilation(
        logger, jsFile);
    StaticPropertyOracle[] propOracles = perm.getPropertyOracles();
    for (StaticPropertyOracle propOracle : propOracles) {
      BindingProperty[] orderedProps = propOracle.getOrderedProps();
      String[] orderedPropValues = propOracle.getOrderedPropValues();
      Map<SelectionProperty, String> unboundProperties = new HashMap<SelectionProperty, String>();
      for (int i = 0; i < orderedProps.length; i++) {
        SelectionProperty key = linkerContext.getProperty(orderedProps[i].getName());
        if (key.tryGetValue() != null) {
          /*
           * The view of the Permutation doesn't include properties with defined
           * values.
           */
          continue;
        }
        unboundProperties.put(key, orderedPropValues[i]);
      }
      compilation.addSelectionPermutation(unboundProperties);
    }
  }

  private ModuleDef module;

  /**
   * This is the output directory for private files.
   */
  private File moduleAuxDir;

  /**
   * This is the output directory for public files.
   */
  private File moduleOutDir;

  private final CompileTaskOptionsImpl options;

  public Link(CompileTaskOptions options) {
    this.options = new CompileTaskOptionsImpl(options);
  }

  public boolean run(TreeLogger logger) throws UnableToCompleteException {
    init(logger);
    File precompilationFile = new File(options.getCompilerWorkDir(),
        Precompile.PRECOMPILATION_FILENAME);
    if (!precompilationFile.exists()) {
      logger.log(TreeLogger.ERROR, "File not found '"
          + precompilationFile.getAbsolutePath()
          + "'; please run Precompile first");
      return false;
    }

    Precompilation precompilation;
    try {
      precompilation = Util.readFileAsObject(precompilationFile,
          Precompilation.class);
    } catch (ClassNotFoundException e) {
      logger.log(TreeLogger.ERROR, "Unable to deserialize '"
          + precompilationFile.getAbsolutePath() + "'", e);
      return false;
    }
    Permutation[] perms = precompilation.getPermutations();
    File[] jsFiles = new File[perms.length];
    for (int i = 0; i < perms.length; ++i) {
      jsFiles[i] = CompilePerms.makePermFilename(options.getCompilerWorkDir(),
          i);
      if (!jsFiles[i].exists()) {
        logger.log(TreeLogger.ERROR, "File not found '"
            + precompilationFile.getAbsolutePath()
            + "'; please compile all permutations");
        return false;
      }
    }

    TreeLogger branch = logger.branch(TreeLogger.INFO, "Linking module "
        + module.getName());
    StandardLinkerContext linkerContext = new StandardLinkerContext(branch,
        module, precompilation.getUnifiedAst().getOptions());
    ArtifactSet artifacts = doLink(branch, linkerContext, precompilation,
        jsFiles);
    if (artifacts != null) {
      linkerContext.produceOutputDirectory(branch, artifacts, moduleOutDir,
          moduleAuxDir);
      branch.log(TreeLogger.INFO, "Link succeeded");
      return true;
    }
    branch.log(TreeLogger.ERROR, "Link failed");
    return false;
  }

  private void init(TreeLogger logger) throws UnableToCompleteException {
    module = ModuleDefLoader.loadFromClassPath(logger, options.getModuleName());
    moduleOutDir = new File(options.getOutDir(), module.getName());
    Util.recursiveDelete(moduleOutDir, true);
    moduleAuxDir = new File(options.getOutDir(), module.getName() + "-aux");
    Util.recursiveDelete(moduleAuxDir, false);
  }
}
