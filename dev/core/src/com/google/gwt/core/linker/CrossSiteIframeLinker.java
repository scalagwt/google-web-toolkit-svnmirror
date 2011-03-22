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

package com.google.gwt.core.linker;

import com.google.gwt.core.ext.LinkerContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.linker.ArtifactSet;
import com.google.gwt.core.ext.linker.CompilationResult;
import com.google.gwt.core.ext.linker.ConfigurationProperty;
import com.google.gwt.core.ext.linker.EmittedArtifact;
import com.google.gwt.core.ext.linker.EmittedArtifact.Visibility;
import com.google.gwt.core.ext.linker.LinkerOrder;
import com.google.gwt.core.ext.linker.LinkerOrder.Order;
import com.google.gwt.core.ext.linker.ScriptReference;
import com.google.gwt.core.ext.linker.Shardable;
import com.google.gwt.core.ext.linker.impl.PropertiesMappingArtifact;
import com.google.gwt.core.ext.linker.impl.PropertiesUtil;
import com.google.gwt.core.ext.linker.impl.ResourceInjectionUtil;
import com.google.gwt.core.ext.linker.impl.SelectionScriptLinker;
import com.google.gwt.dev.About;
import com.google.gwt.dev.js.JsToStringGenerationVisitor;
import com.google.gwt.dev.util.DefaultTextOutput;
import com.google.gwt.dev.util.TextOutput;
import com.google.gwt.util.tools.Utility;

import java.io.IOException;
import java.util.SortedSet;

/**
 * This linker uses an iframe to hold the code and a script tag to download the
 * code. It can download code cross-site, because it uses a script tag to
 * download it and because it never uses XHR. The iframe, meanwhile, makes it
 * trivial to install additional code as the app runs.
 */
@LinkerOrder(Order.PRIMARY)
@Shardable
public class CrossSiteIframeLinker extends SelectionScriptLinker {
  /**
   * A configuration property that can be used to have the linker ignore the
   * script tags in gwt.xml rather than fail to compile if they are present
   */
  private static final String FAIL_IF_SCRIPT_TAG_PROPERTY = "xsiframe.failIfScriptTag";

  @Override
  public String getDescription() {
    return "Cross-Site-Iframe";
  }

  @Override
  protected String fillSelectionScriptTemplate(StringBuffer ss, TreeLogger logger,
      LinkerContext context, ArtifactSet artifacts, CompilationResult result)
      throws UnableToCompleteException {

    // Must do installScript before installLocation and waitForBodyLoaded
    includeJs(ss, logger, getJsInstallScript(context), "__INSTALL_SCRIPT__");
    includeJs(ss, logger, getJsInstallLocation(context), "__INSTALL_LOCATION__");
    includeJs(ss, logger, getJsWaitForBodyLoaded(context), "__WAIT_FOR_BODY_LOADED__");

    // Must do permutations before providers
    includeJs(ss, logger, getJsPermutations(context), "__PERMUTATIONS__");
    includeJs(ss, logger, getJsProperties(context), "__PROPERTIES__");
    includeJs(ss, logger, getJsProcessMetas(context), "__PROCESS_METAS__");
    includeJs(ss, logger, getJsComputeScriptBase(context), "__COMPUTE_SCRIPT_BASE__");
    includeJs(ss, logger, getJsComputeUrlForResource(context), "__COMPUTE_URL_FOR_RESOURCE__");
    includeJs(ss, logger, getJsLoadExternalStylesheets(context), "__LOAD_STYLESHEETS__");

    // This Linker does not support <script> tags in the gwt.xml
    SortedSet<ScriptReference> scripts = artifacts.find(ScriptReference.class);
    if (!scripts.isEmpty()) {
      String list = "";
      for (ScriptReference script : scripts) {
        list += (script.getSrc() + "\n");
      }
      boolean failIfScriptTags = true;
      for (ConfigurationProperty prop : context.getConfigurationProperties()) {
        if (prop.getName().equalsIgnoreCase(FAIL_IF_SCRIPT_TAG_PROPERTY)) {
          if (prop.getValues().get(0).equalsIgnoreCase("false")) {
            failIfScriptTags = false;
          }
        }
      }
      if (failIfScriptTags) {
        String msg =
            "The " + getDescription()
                + " linker does not support <script> tags in the gwt.xml files, but the"
                + " gwt.xml file (or the gwt.xml files which it includes) contains the"
                + " following script tags: \n" + list
                + "In order for your application to run correctly, you will need to"
                + " include these tags in your host page directly. In order to avoid"
                + " this error, you will need to remove the script tags from the"
                + " gwt.xml file, or add this property to the gwt.xml file:"
                + " <set-configuration-property name='xsiframe.failIfScriptTag' value='FALSE'/>";
        logger.log(TreeLogger.ERROR, msg);
        throw new UnableToCompleteException();
      } else {
        String msg = "Ignoring the following script tags in the gwt.xml " + "file\n" + list;
        logger.log(TreeLogger.INFO, msg);
      }
    }

    ss = ResourceInjectionUtil.injectStylesheets(ss, artifacts);
    ss = permutationsUtil.addPermutationsJs(ss, logger, context);

    if (result != null) {
      replaceAll(ss, "__KNOWN_PROPERTIES__", PropertiesUtil.addKnownPropertiesJs(logger, result));
    }
    replaceAll(ss, "__MODULE_FUNC__", context.getModuleFunctionName());
    replaceAll(ss, "__MODULE_NAME__", context.getModuleName());
    replaceAll(ss, "__HOSTED_FILENAME__", getHostedFilename());

    return ss.toString();
  }

  @Override
  protected String getCompilationExtension(TreeLogger logger, LinkerContext context) {
    return ".cache.js";
  }

  @Override
  protected String getHostedFilename() {
    return "devmode.js";
  }

  protected String getJsComputeScriptBase(LinkerContext context) {
    return "com/google/gwt/core/ext/linker/impl/computeScriptBase.js";
  }

  protected String getJsComputeUrlForResource(LinkerContext context) {
    return "com/google/gwt/core/ext/linker/impl/computeUrlForResource.js";
  }

  protected String getJsInstallLocation(LinkerContext context) {
    return "com/google/gwt/core/ext/linker/impl/installLocationIframe.js";
  }

  // If you override this to return installScriptDirect.js, then you should
  // also override shouldInstallCode() to return false
  protected String getJsInstallScript(LinkerContext context) {
    return "com/google/gwt/core/ext/linker/impl/installScriptEarlyDownload.js";
  }

  protected String getJsLoadExternalStylesheets(LinkerContext context) {
    return "com/google/gwt/core/ext/linker/impl/loadExternalStylesheets.js";
  }

  protected String getJsPermutations(LinkerContext context) {
    return "com/google/gwt/core/ext/linker/impl/permutations.js";
  }

  protected String getJsProcessMetas(LinkerContext context) {
    return "com/google/gwt/core/ext/linker/impl/processMetas.js";
  }

  protected String getJsProperties(LinkerContext context) {
    return "com/google/gwt/core/ext/linker/impl/properties.js";
  }

  protected String getJsWaitForBodyLoaded(LinkerContext context) {
    return "com/google/gwt/core/ext/linker/impl/waitForBodyLoaded.js";
  }

  @Override
  protected String getModulePrefix(TreeLogger logger, LinkerContext context, String strongName) {
    TextOutput out = new DefaultTextOutput(context.isOutputCompact());

    // We assume that the $wnd has been set in the same scope as this code is
    // executing in. $wnd is the main window which the GWT code is affecting. It
    // is also usually the location the bootstrap function was defined in.
    // In iframe based linkers, $wnd = window.parent;
    // Usually, in others, $wnd = window;

    out.print("var __gwtModuleFunction = $wnd." + context.getModuleFunctionName() + ";");
    out.newlineOpt();
    out.print("var $sendStats = __gwtModuleFunction.__sendStats;");
    out.newlineOpt();
    out.print("$sendStats('moduleStartup', 'moduleEvalStart');");
    out.newlineOpt();
    out.print("var $gwt_version = \"" + About.getGwtVersionNum() + "\";");
    out.newlineOpt();
    out.print("var $strongName = '" + strongName + "';");
    out.newlineOpt();
    out.print("var $doc = $wnd.document;");

    // Even though we call the $sendStats function in the code written in this
    // linker, some of the compilation code still needs the $stats and
    // $sessionId
    // variables to be available.
    out.print("var $stats = $wnd.__gwtStatsEvent ? function(a) {return $wnd.__gwtStatsEvent(a);} : null;");
    out.newlineOpt();
    out.print("var $sessionId = $wnd.__gwtStatsSessionId ? $wnd.__gwtStatsSessionId : null;");
    out.newlineOpt();

    return out.toString();
  }

  @Override
  protected String getModuleSuffix(TreeLogger logger, LinkerContext context) {
    DefaultTextOutput out = new DefaultTextOutput(context.isOutputCompact());

    out.print("$sendStats('moduleStartup', 'moduleEvalEnd');");
    out.newlineOpt();
    out.print("gwtOnLoad(" + "__gwtModuleFunction.__errFn, " + "__gwtModuleFunction.__moduleName, "
        + "__gwtModuleFunction.__moduleBase, " + "__gwtModuleFunction.__softPermutationId,"
        + "__gwtModuleFunction.__computePropValue);");
    out.newlineOpt();
    out.print("$sendStats('moduleStartup', 'end');");

    return out.toString();
  }

  @Override
  protected String getSelectionScriptTemplate(TreeLogger logger, LinkerContext context) {
    return "com/google/gwt/core/linker/CrossSiteIframeTemplate.js";
  }

  protected void includeJs(StringBuffer selectionScript, TreeLogger logger, String jsSource,
      String templateVar) throws UnableToCompleteException {
    String js;
    if (jsSource.endsWith(".js")) {
      try {
        js = Utility.getFileFromClassPath(jsSource);
      } catch (IOException e) {
        logger.log(TreeLogger.ERROR, "Unable to read file: " + jsSource, e);
        throw new UnableToCompleteException();
      }
    } else {
      js = jsSource;
    }
    replaceAll(selectionScript, templateVar, js);
  }

  @Override
  protected void maybeAddHostedModeFile(TreeLogger logger, LinkerContext context,
      ArtifactSet artifacts, CompilationResult result) throws UnableToCompleteException {
    String filename = getHostedFilename();
    if ("".equals(filename)) {
      return;
    }

    // when we're including bootstrap in the primary fragment, we should be
    // generating devmode files for each permutation. Otherwise, we generate it
    // only in the final link stage.
    boolean isSinglePermutation = (result != null);
    if (isSinglePermutation != shouldIncludeBootstrapInPrimaryFragment(context)) {
      return;
    }

    long lastModified = System.currentTimeMillis();
    StringBuffer buffer =
        readFileToStringBuffer("com/google/gwt/core/ext/linker/impl/" + filename, logger);

    String outputFilename = filename;
    if (result != null) {
      outputFilename = result.getStrongName() + "." + outputFilename;
    }

    String script =
        generatePrimaryFragmentString(logger, context, result, buffer.toString(), 1, artifacts);

    EmittedArtifact devArtifact = emitString(logger, script, outputFilename, lastModified);
    artifacts.add(devArtifact);
  }

  // Output compilation-mappings.txt
  @Override
  protected void maybeOutputPropertyMap(TreeLogger logger, LinkerContext context,
      ArtifactSet toReturn) {
    if (!shouldOutputPropertyMap(context) || permutationsUtil.getPermutationsMap() == null
        || permutationsUtil.getPermutationsMap().isEmpty()) {
      return;
    }

    PropertiesMappingArtifact mappingArtifact =
        new PropertiesMappingArtifact(CrossSiteIframeLinker.class, permutationsUtil
            .getPermutationsMap());

    toReturn.add(mappingArtifact);
    EmittedArtifact serializedMap;
    try {
      String mappings = mappingArtifact.getSerialized();
      mappings = mappings.concat("Devmode:" + getHostedFilename());
      serializedMap = emitString(logger, mappings, "compilation-mappings.txt");
      // TODO(unnurg): make this Deploy
      serializedMap.setVisibility(Visibility.Public);
      toReturn.add(serializedMap);
    } catch (UnableToCompleteException e) {
      e.printStackTrace();
    }
  }

  // If you set this to return true, you should also override
  // getJsPermutations() to return permutationsNull.js and
  // getJsInstallScript() to return installScriptAlreadyIncluded.js
  protected boolean shouldIncludeBootstrapInPrimaryFragment(LinkerContext context) {
    return false;
  }

  protected boolean shouldInstallCode(LinkerContext context) {
    return true;
  }

  protected boolean shouldOutputPropertyMap(LinkerContext context) {
    return false;
  }

  @Override
  protected String wrapPrimaryFragment(TreeLogger logger, LinkerContext context, String script,
      ArtifactSet artifacts, CompilationResult result) {
    StringBuffer out = new StringBuffer();
    if (shouldIncludeBootstrapInPrimaryFragment(context)) {
      try {
        out.append(generateSelectionScript(logger, context, artifacts, result));
      } catch (UnableToCompleteException e) {
        logger.log(TreeLogger.ERROR, "Problem setting up selection script", e);
        e.printStackTrace();
      }
    }

    if (shouldInstallCode(context)) {
      // Rewrite the code so it can be installed with
      // __MODULE_FUNC__.onScriptDownloaded
      out.append(context.getModuleFunctionName());
      out.append(".onScriptDownloaded(");
      out.append(JsToStringGenerationVisitor.javaScriptString(script.toString()));
      out.append(")");
    } else {
      out.append(script.toString());
    }
    return out.toString();
  }

}
