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
import com.google.gwt.dev.util.arg.ArgHandlerExtraDir;
import com.google.gwt.dev.util.arg.ArgHandlerWarDir;
import com.google.gwt.dev.util.arg.OptionExtraDir;
import com.google.gwt.dev.util.arg.OptionOutDir;
import com.google.gwt.dev.util.arg.OptionWarDir;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Performs the last phase of compilation, merging the compilation outputs.
 */
public class Link {
  /**
   * Options for Link.
   */
  public interface LegacyLinkOptions extends CompileTaskOptions, OptionOutDir {
  }

  /**
   * Options for Link.
   */
  public interface LinkOptions extends CompileTaskOptions, OptionExtraDir,
      OptionWarDir {
  }

  static class ArgProcessor extends CompileArgProcessor {
    public ArgProcessor(LinkOptions options) {
      super(options);
      registerHandler(new ArgHandlerExtraDir(options));
      registerHandler(new ArgHandlerWarDir(options));
    }

    @Override
    protected String getName() {
      return Link.class.getName();
    }
  }

  /**
   * Concrete class to implement link options.
   */
  static class LinkOptionsImpl extends CompileTaskOptionsImpl implements
      LinkOptions {

    private File extraDir;
    private File warDir;

    public LinkOptionsImpl() {
    }

    public LinkOptionsImpl(LinkOptions other) {
      copyFrom(other);
    }

    public void copyFrom(LinkOptions other) {
      super.copyFrom(other);
      setExtraDir(other.getExtraDir());
      setWarDir(other.getWarDir());
    }

    public File getExtraDir() {
      return extraDir;
    }

    public File getWarDir() {
      return warDir;
    }

    public void setExtraDir(File extraDir) {
      this.extraDir = extraDir;
    }

    public void setWarDir(File warDir) {
      this.warDir = warDir;
    }
  }

  public static void legacyLink(TreeLogger logger, ModuleDef module,
      Precompilation precompilation, File[] resultFiles, File outDir)
      throws UnableToCompleteException {
    StandardLinkerContext linkerContext = new StandardLinkerContext(logger,
        module, precompilation.getUnifiedAst().getOptions());
    ArtifactSet artifacts = doLink(logger, linkerContext, precompilation,
        resultFiles);
    doProduceLegacyOutput(logger, artifacts, linkerContext, module, outDir);
  }

  public static ArtifactSet link(TreeLogger logger, ModuleDef module,
      Precompilation precompilation, File[] resultFiles)
      throws UnableToCompleteException {
    StandardLinkerContext linkerContext = new StandardLinkerContext(logger,
        module, precompilation.getUnifiedAst().getOptions());
    return doLink(logger, linkerContext, precompilation, resultFiles);
  }

  public static void link(TreeLogger logger, ModuleDef module,
      Precompilation precompilation, File[] resultFiles, File outDir,
      File extrasDir) throws UnableToCompleteException {
    StandardLinkerContext linkerContext = new StandardLinkerContext(logger,
        module, precompilation.getUnifiedAst().getOptions());
    ArtifactSet artifacts = doLink(logger, linkerContext, precompilation,
        resultFiles);
    doProduceOutput(logger, artifacts, linkerContext, module, outDir, extrasDir);
  }

  public static void main(String[] args) {
    /*
     * NOTE: main always exits with a call to System.exit to terminate any
     * non-daemon threads that were started in Generators. Typically, this is to
     * shutdown AWT related threads, since the contract for their termination is
     * still implementation-dependent.
     */
    final LinkOptions options = new LinkOptionsImpl();
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
      File[] resultFiles) throws UnableToCompleteException {
    Permutation[] perms = precompilation.getPermutations();
    if (perms.length != resultFiles.length) {
      throw new IllegalArgumentException(
          "Mismatched resultFiles.length and permutation count");
    }

    for (int i = 0; i < perms.length; ++i) {
      finishPermuation(logger, perms[i], resultFiles[i], linkerContext);
    }

    linkerContext.addOrReplaceArtifacts(precompilation.getGeneratedArtifacts());
    return linkerContext.invokeLink(logger);
  }

  private static void doProduceLegacyOutput(TreeLogger logger,
      ArtifactSet artifacts, StandardLinkerContext linkerContext,
      ModuleDef module, File outDir) throws UnableToCompleteException {
    File moduleOutDir = new File(outDir, module.getName());
    File moduleExtraDir = new File(outDir, module.getName() + "-aux");
    Util.recursiveDelete(moduleOutDir, true);
    Util.recursiveDelete(moduleExtraDir, true);
    linkerContext.produceOutputDirectory(logger, artifacts, moduleOutDir,
        moduleExtraDir);
    logger.log(TreeLogger.INFO, "Link succeeded");
  }

  private static void doProduceOutput(TreeLogger logger, ArtifactSet artifacts,
      StandardLinkerContext linkerContext, ModuleDef module, File outDir,
      File extraDir) throws UnableToCompleteException {
    File moduleOutDir = new File(outDir, module.getName());
    File moduleExtraDir = (extraDir == null) ? null : new File(extraDir,
        module.getName());
    Util.recursiveDelete(moduleOutDir, true);
    if (moduleExtraDir != null) {
      Util.recursiveDelete(moduleExtraDir, true);
    }
    linkerContext.produceOutputDirectory(logger, artifacts, moduleOutDir,
        moduleExtraDir);
    logger.log(TreeLogger.INFO, "Link succeeded");
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

  private final LinkOptionsImpl options;

  public Link(LinkOptions options) {
    this.options = new LinkOptionsImpl(options);
  }

  public boolean run(TreeLogger logger) throws UnableToCompleteException {
    for (String moduleName : options.getModuleNames()) {
      File compilerWorkDir = options.getCompilerWorkDir(moduleName);
      ModuleDef module = ModuleDefLoader.loadFromClassPath(logger, moduleName);

      File precompilationFile = new File(compilerWorkDir,
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
      File[] resultFiles = new File[perms.length];
      for (int i = 0; i < perms.length; ++i) {
        resultFiles[i] = CompilePerms.makePermFilename(compilerWorkDir, i);
        if (!resultFiles[i].exists()) {
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
          resultFiles);

      doProduceOutput(branch, artifacts, linkerContext, module,
          options.getWarDir(), options.getExtraDir());
    }
    return true;
  }
}
