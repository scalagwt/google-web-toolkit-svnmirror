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
package com.google.gwt.core.ext.linker.impl;

import com.google.gwt.core.ext.Linker;
import com.google.gwt.core.ext.LinkerContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.linker.ArtifactSet;
import com.google.gwt.core.ext.linker.ConfigurationProperty;
import com.google.gwt.core.ext.linker.EmittedArtifact;
import com.google.gwt.core.ext.linker.LinkerOrder;
import com.google.gwt.core.ext.linker.PublicResource;
import com.google.gwt.core.ext.linker.SelectionProperty;
import com.google.gwt.core.ext.linker.LinkerOrder.Order;
import com.google.gwt.dev.cfg.BindingProperty;
import com.google.gwt.dev.cfg.ModuleDef;
import com.google.gwt.dev.cfg.Property;
import com.google.gwt.dev.cfg.Script;
import com.google.gwt.dev.jjs.InternalCompilerException;
import com.google.gwt.dev.jjs.JJSOptions;
import com.google.gwt.dev.js.JsObfuscateNamer;
import com.google.gwt.dev.js.JsParser;
import com.google.gwt.dev.js.JsParserException;
import com.google.gwt.dev.js.JsPrettyNamer;
import com.google.gwt.dev.js.JsSourceGenerationVisitor;
import com.google.gwt.dev.js.JsStringInterner;
import com.google.gwt.dev.js.JsSymbolResolver;
import com.google.gwt.dev.js.JsUnusedFunctionRemover;
import com.google.gwt.dev.js.JsVerboseNamer;
import com.google.gwt.dev.js.ast.JsContext;
import com.google.gwt.dev.js.ast.JsExpression;
import com.google.gwt.dev.js.ast.JsFunction;
import com.google.gwt.dev.js.ast.JsModVisitor;
import com.google.gwt.dev.js.ast.JsName;
import com.google.gwt.dev.js.ast.JsProgram;
import com.google.gwt.dev.js.ast.JsScope;
import com.google.gwt.dev.util.DefaultTextOutput;
import com.google.gwt.dev.util.Util;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;

/**
 * An implementation of {@link LinkerContext} that is initialized from a
 * {@link ModuleDef}.
 */
public class StandardLinkerContext extends Linker implements LinkerContext {

  /**
   * Applies the {@link JsStringInterner} optimization to each top-level
   * function defined within a JsProgram.
   */
  private static class TopFunctionStringInterner extends JsModVisitor {

    public static boolean exec(JsProgram program) {
      TopFunctionStringInterner v = new TopFunctionStringInterner();
      v.accept(program);
      return v.didChange();
    }

    @Override
    public boolean visit(JsFunction x, JsContext<JsExpression> ctx) {
      didChange |= JsStringInterner.exec(x.getBody(), x.getScope());
      return false;
    }
  }

  static final Comparator<ConfigurationProperty> CONFIGURATION_PROPERTY_COMPARATOR = new Comparator<ConfigurationProperty>() {
    public int compare(ConfigurationProperty o1, ConfigurationProperty o2) {
      return o1.getName().compareTo(o2.getName());
    }
  };

  static final Comparator<SelectionProperty> SELECTION_PROPERTY_COMPARATOR = new Comparator<SelectionProperty>() {
    public int compare(SelectionProperty o1, SelectionProperty o2) {
      return o1.getName().compareTo(o2.getName());
    }
  };

  private final ArtifactSet artifacts = new ArtifactSet();

  private final SortedSet<ConfigurationProperty> configurationProperties;
  private final JJSOptions jjsOptions;
  private final List<Class<? extends Linker>> linkerClasses;
  private final Map<Class<? extends Linker>, String> linkerShortNames = new HashMap<Class<? extends Linker>, String>();

  private final String moduleFunctionName;
  private final String moduleName;

  private final Map<String, StandardSelectionProperty> propertiesByName = new HashMap<String, StandardSelectionProperty>();
  private final Map<String, StandardCompilationResult> resultsByStrongName = new HashMap<String, StandardCompilationResult>();
  private final SortedSet<SelectionProperty> selectionProperties;

  public StandardLinkerContext(TreeLogger logger, ModuleDef module,
      JJSOptions jjsOptions) {
    logger = logger.branch(TreeLogger.DEBUG,
        "Constructing StandardLinkerContext", null);

    this.jjsOptions = jjsOptions;
    this.moduleFunctionName = module.getFunctionName();
    this.moduleName = module.getName();
    this.linkerClasses = new ArrayList<Class<? extends Linker>>(
        module.getActiveLinkers());
    linkerClasses.add(module.getActivePrimaryLinker());

    for (Map.Entry<String, Class<? extends Linker>> entry : module.getLinkers().entrySet()) {
      linkerShortNames.put(entry.getValue(), entry.getKey());
    }

    /*
     * This will make all private PublicResources and GeneratedResources appear
     * in the root of the module auxiliary directory.
     */
    linkerShortNames.put(this.getClass(), "");

    // Break ModuleDef properties out into LinkerContext interfaces
    {
      SortedSet<ConfigurationProperty> mutableConfigurationProperties = new TreeSet<ConfigurationProperty>(
          CONFIGURATION_PROPERTY_COMPARATOR);
      SortedSet<SelectionProperty> mutableSelectionProperties = new TreeSet<SelectionProperty>(
          SELECTION_PROPERTY_COMPARATOR);
      for (Property p : module.getProperties()) {
        // Create a new view
        if (p instanceof com.google.gwt.dev.cfg.ConfigurationProperty) {
          StandardConfigurationProperty newProp = new StandardConfigurationProperty(
              (com.google.gwt.dev.cfg.ConfigurationProperty) p);
          mutableConfigurationProperties.add(newProp);
          logger.log(TreeLogger.SPAM,
              "Added configuration property " + newProp, null);
        } else if (p instanceof BindingProperty) {
          StandardSelectionProperty newProp = new StandardSelectionProperty(
              (BindingProperty) p);
          mutableSelectionProperties.add(newProp);
          propertiesByName.put(newProp.getName(), newProp);
          logger.log(TreeLogger.SPAM, "Added selection property " + newProp,
              null);
        } else {
          logger.log(TreeLogger.ERROR, "Unknown property type "
              + p.getClass().getName());
        }
      }
      selectionProperties = Collections.unmodifiableSortedSet(mutableSelectionProperties);
      configurationProperties = Collections.unmodifiableSortedSet(mutableConfigurationProperties);
    }

    {
      int index = 0;
      for (Script script : module.getScripts()) {
        artifacts.add(new StandardScriptReference(script.getSrc(), index++));
        logger.log(TreeLogger.SPAM, "Added script " + script.getSrc(), null);
      }
    }

    {
      int index = 0;
      for (String style : module.getStyles()) {
        artifacts.add(new StandardStylesheetReference(style, index++));
        logger.log(TreeLogger.SPAM, "Added style " + style, null);
      }
    }

    // Generated files should be passed in via addArtifacts()

    for (String path : module.getAllPublicFiles()) {
      String partialPath = path.replace(File.separatorChar, '/');
      PublicResource resource = new StandardPublicResource(partialPath,
          module.findPublicFile(path));
      artifacts.add(resource);
      logger.log(TreeLogger.SPAM, "Added public resource " + resource, null);
    }
  }

  /**
   * Adds or replaces Artifacts in the ArtifactSet that will be passed into the
   * Linkers invoked.
   */
  public void addOrReplaceArtifacts(ArtifactSet artifacts) {
    this.artifacts.removeAll(artifacts);
    this.artifacts.addAll(artifacts);
  }

  /**
   * Returns the ArtifactSet that will passed into the invoke Linkers.
   */
  public ArtifactSet getArtifacts() {
    return artifacts;
  }

  /**
   * Gets or creates a CompilationResult for the given JavaScript program.
   */
  public StandardCompilationResult getCompilation(TreeLogger logger, File jsFile)
      throws UnableToCompleteException {
    byte[] bytes = Util.readFileAsBytes(jsFile);
    if (bytes == null) {
      logger.log(TreeLogger.ERROR, "Unable to read file '"
          + jsFile.getAbsolutePath() + "'");
      throw new UnableToCompleteException();
    }
    String strongName = Util.computeStrongName(bytes);
    StandardCompilationResult result = resultsByStrongName.get(strongName);
    if (result == null) {
      result = new StandardCompilationResult(Util.toString(bytes), strongName,
          jsFile);
      resultsByStrongName.put(result.getStrongName(), result);
      artifacts.add(result);
    }
    return result;
  }

  public SortedSet<ConfigurationProperty> getConfigurationProperties() {
    return configurationProperties;
  }

  @Override
  public String getDescription() {
    return "Root Linker";
  }

  public String getModuleFunctionName() {
    return moduleFunctionName;
  }

  public String getModuleName() {
    return moduleName;
  }

  public SortedSet<SelectionProperty> getProperties() {
    return selectionProperties;
  }

  public StandardSelectionProperty getProperty(String name) {
    return propertiesByName.get(name);
  }

  /**
   * Run the linker stack.
   */
  public ArtifactSet invokeLinkerStack(TreeLogger logger)
      throws UnableToCompleteException {
    ArtifactSet workingArtifacts = new ArtifactSet(artifacts);
    Stack<Linker> linkerStack = new Stack<Linker>();

    EnumSet<Order> phasePre = EnumSet.of(Order.PRE, Order.PRIMARY);
    EnumSet<Order> phasePost = EnumSet.of(Order.POST);

    // Instantiate instances of the Linkers
    for (Class<? extends Linker> clazz : linkerClasses) {
      Linker linker;

      // Create an instance of the Linker
      try {
        linker = clazz.newInstance();
        linkerStack.push(linker);
      } catch (InstantiationException e) {
        logger.log(TreeLogger.ERROR, "Unable to create LinkerContextShim", e);
        throw new UnableToCompleteException();
      } catch (IllegalAccessException e) {
        logger.log(TreeLogger.ERROR, "Unable to create LinkerContextShim", e);
        throw new UnableToCompleteException();
      }

      // Detemine if we need to invoke the Linker in the current link phase
      Order order = clazz.getAnnotation(LinkerOrder.class).value();
      if (!phasePre.contains(order)) {
        continue;
      }

      // The primary Linker is guaranteed to be last in the order
      if (order == Order.PRIMARY) {
        assert linkerClasses.get(linkerClasses.size() - 1).equals(clazz);
      }

      TreeLogger linkerLogger = logger.branch(TreeLogger.TRACE,
          "Invoking Linker " + linker.getDescription(), null);

      workingArtifacts.freeze();
      try {
        workingArtifacts = linker.link(linkerLogger, this, workingArtifacts);
      } catch (Exception e) {
        linkerLogger.log(TreeLogger.ERROR, "Failed to link", e);
        throw new UnableToCompleteException();
      }
    }

    // Pop the primary linker off of the stack
    linkerStack.pop();

    // Unwind the stack
    while (!linkerStack.isEmpty()) {
      Linker linker = linkerStack.pop();
      Class<? extends Linker> linkerType = linker.getClass();

      // See if the Linker should be run in the current phase
      Order order = linkerType.getAnnotation(LinkerOrder.class).value();
      if (phasePost.contains(order)) {
        TreeLogger linkerLogger = logger.branch(TreeLogger.TRACE,
            "Invoking Linker " + linker.getDescription(), null);

        workingArtifacts.freeze();
        try {
          workingArtifacts = linker.link(linkerLogger, this, workingArtifacts);
        } catch (Exception e) {
          linkerLogger.log(TreeLogger.ERROR, "Failed to link", e);
          throw new UnableToCompleteException();
        }
      }
    }

    return workingArtifacts;
  }

  public boolean isOutputCompact() {
    return jjsOptions.getOutput().shouldMinimize();
  }

  @Override
  public ArtifactSet link(TreeLogger logger, LinkerContext context,
      ArtifactSet artifacts) throws UnableToCompleteException {
    throw new UnsupportedOperationException();
  }

  public String optimizeJavaScript(TreeLogger logger, String program)
      throws UnableToCompleteException {
    logger = logger.branch(TreeLogger.DEBUG, "Attempting to optimize JS", null);
    JsParser parser = new JsParser();
    Reader r = new StringReader(program);
    JsProgram jsProgram = new JsProgram();
    JsScope topScope = jsProgram.getScope();
    JsName funcName = topScope.declareName(getModuleFunctionName());
    funcName.setObfuscatable(false);

    try {
      parser.parseInto(topScope, jsProgram.getGlobalBlock(), r, 1);
    } catch (IOException e) {
      logger.log(TreeLogger.ERROR, "Unable to parse JavaScript", e);
      throw new UnableToCompleteException();
    } catch (JsParserException e) {
      logger.log(TreeLogger.ERROR, "Unable to parse JavaScript", e);
      throw new UnableToCompleteException();
    }

    JsSymbolResolver.exec(jsProgram);
    JsUnusedFunctionRemover.exec(jsProgram);

    switch (jjsOptions.getOutput()) {
      case OBFUSCATED:
        /*
         * We can't apply the regular JsStringInterner to the JsProgram that
         * we've just created. In the normal case, the JsStringInterner adds an
         * additional statement to the program's global JsBlock, however we
         * don't know exactly what the form and structure of our JsProgram are,
         * so we'll limit the scope of the modifications to each top-level
         * function within the program.
         */
        TopFunctionStringInterner.exec(jsProgram);
        JsObfuscateNamer.exec(jsProgram);
        break;
      case PRETTY:
        // We don't intern strings in pretty mode to improve readability
        JsPrettyNamer.exec(jsProgram);
        break;
      case DETAILED:
        // As above with OBFUSCATED
        TopFunctionStringInterner.exec(jsProgram);
        JsVerboseNamer.exec(jsProgram);
        break;
      default:
        throw new InternalCompilerException("Unknown output mode");
    }

    DefaultTextOutput out = new DefaultTextOutput(
        jjsOptions.getOutput().shouldMinimize());
    JsSourceGenerationVisitor v = new JsSourceGenerationVisitor(out);
    v.accept(jsProgram);
    return out.toString();
  }

  public void produceOutputDirectory(TreeLogger logger, ArtifactSet artifacts,
      File moduleOutDir, File moduleAuxDir) throws UnableToCompleteException {

    logger = logger.branch(TreeLogger.INFO, "Linking compilation into "
        + moduleOutDir.getPath(), null);

    for (EmittedArtifact artifact : artifacts.find(EmittedArtifact.class)) {
      TreeLogger artifactLogger = logger.branch(TreeLogger.DEBUG,
          "Emitting resource " + artifact.getPartialPath(), null);

      File outFile;
      if (artifact.isPrivate()) {
        outFile = new File(getLinkerAuxDir(moduleAuxDir, artifact.getLinker()),
            artifact.getPartialPath());
      } else {
        outFile = new File(moduleOutDir, artifact.getPartialPath());
      }

      assert !outFile.exists() : "Attempted to overwrite " + outFile.getPath();
      Util.copy(logger, artifact.getContents(artifactLogger), outFile);
    }
  }

  /**
   * Creates a linker-specific subdirectory in the module's auxiliary output
   * directory.
   */
  private File getLinkerAuxDir(File moduleAuxDir,
      Class<? extends Linker> linkerType) {
    // The auxiliary directory is create lazily
    if (!moduleAuxDir.exists()) {
      moduleAuxDir.mkdirs();
    }
    assert linkerShortNames.containsKey(linkerType) : linkerType.getName()
        + " unknown";
    File toReturn = new File(moduleAuxDir, linkerShortNames.get(linkerType));
    if (!toReturn.exists()) {
      toReturn.mkdirs();
    }
    return toReturn;
  }
}
