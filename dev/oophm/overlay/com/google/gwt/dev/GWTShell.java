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
import com.google.gwt.core.ext.linker.EmittedArtifact;
import com.google.gwt.dev.GWTCompiler.GWTCompilerOptionsImpl;
import com.google.gwt.dev.cfg.ModuleDef;
import com.google.gwt.dev.shell.ArtifactAcceptor;
import com.google.gwt.dev.shell.WorkDirs;
import com.google.gwt.dev.shell.tomcat.EmbeddedTomcatServer;
import com.google.gwt.dev.util.Util;
import com.google.gwt.dev.util.arg.ArgHandlerOutDir;
import com.google.gwt.util.tools.ArgHandlerExtra;

import java.io.File;
import java.util.Set;

import javax.swing.ImageIcon;

/**
 * The main executable class for the hosted mode shell.
 */
@SuppressWarnings("deprecation")
@Deprecated
public class GWTShell extends OophmHostedModeBase {

  /**
   * Handles the list of startup urls that can be passed at the end of the
   * command line.
   */
  protected static class ArgHandlerStartupURLsExtra extends ArgHandlerExtra {

    private final OptionStartupURLs options;

    public ArgHandlerStartupURLsExtra(OptionStartupURLs options) {
      this.options = options;
    }

    @Override
    public boolean addExtraArg(String arg) {
      options.addStartupURL(arg);
      return true;
    }

    @Override
    public String getPurpose() {
      return "Automatically launches the specified URL";
    }

    @Override
    public String[] getTagArgs() {
      return new String[] {"url"};
    }
  }

  /**
   * The GWTShell argument processor.
   */
  protected static class ArgProcessor extends OophmHostedModeBase.ArgProcessor {
    public ArgProcessor(ShellOptionsImpl options, boolean forceServer,
        boolean noURLs) {
      super(options, forceServer);
      if (!noURLs) {
        registerHandler(new ArgHandlerStartupURLsExtra(options));
      }
      registerHandler(new ArgHandlerOutDir(options));
    }

    @Override
    protected String getName() {
      return GWTShell.class.getName();
    }
  }

  /**
   * Concrete class to implement all shell options.
   */
  static class ShellOptionsImpl extends OophmHostedModeBaseOptionsImpl
      implements HostedModeBaseOptions, WorkDirs, LegacyCompilerOptions,
      OptionPortHosted {
    private int localWorkers;
    private File outDir;

    public File getCompilerOutputDir(ModuleDef moduleDef) {
      return new File(getOutDir(), moduleDef.getName());
    }

    public int getLocalWorkers() {
      return localWorkers;
    }

    public File getOutDir() {
      return outDir;
    }

    public File getShellPublicGenDir(ModuleDef moduleDef) {
      return new File(getShellBaseWorkDir(moduleDef), "public");
    }

    @Override
    public File getWorkDir() {
      return new File(getOutDir(), ".gwt-tmp");
    }

    public void setLocalWorkers(int localWorkers) {
      this.localWorkers = localWorkers;
    }

    public void setOutDir(File outDir) {
      this.outDir = outDir;
    }
  }

  public static final String GWT_SHELL_PATH = ".gwt-tmp" + File.separator
      + "shell";

  public static String checkHost(String hostUnderConsideration,
      Set<String> hosts) {
    hostUnderConsideration = hostUnderConsideration.toLowerCase();
    for (String rule : hosts) {
      // match on lowercased regex
      if (hostUnderConsideration.matches(".*" + rule + ".*")) {
        return rule;
      }
    }
    return null;
  }

  public static String computeHostRegex(String url) {
    // the entire URL up to the first slash not prefixed by a slash or colon.
    String raw = url.split("(?<![:/])/")[0];
    // escape the dots and put a begin line specifier on the result
    return "^" + raw.replaceAll("[.]", "[.]");
  }

  public static String formatRules(Set<String> invalidHttpHosts) {
    StringBuffer out = new StringBuffer();
    for (String rule : invalidHttpHosts) {
      out.append(rule);
      out.append(" ");
    }
    return out.toString();
  }

  public static void main(String[] args) {
    /*
     * NOTE: main always exits with a call to System.exit to terminate any
     * non-daemon threads that were started in Generators. Typically, this is to
     * shutdown AWT related threads, since the contract for their termination is
     * still implementation-dependent.
     */
    GWTShell gwtShell = new GWTShell();
    ArgProcessor argProcessor = new ArgProcessor(gwtShell.options, false, false);
    if (argProcessor.processArgs(args)) {
      gwtShell.run();
      // Exit w/ success code.
      System.exit(0);
    }
    // Exit w/ non-success code.
    System.exit(-1);
  }

  /**
   * Hiding super field because it's actually the same object, just with a
   * stronger type.
   */
  @SuppressWarnings("hiding")
  protected final ShellOptionsImpl options = (ShellOptionsImpl) super.options;

  protected File outDir;

  public WebServerRestart hasWebServer() {
    return WebServerRestart.NONE;
  }

  public void restartServer(TreeLogger logger) throws UnableToCompleteException {
    // Unimplemented.
  }

  @Override
  protected void compile(TreeLogger logger) throws UnableToCompleteException {
    throw new UnsupportedOperationException();
  }

  /**
   * Compiles a logical module def. The caller can modify the specified module
   * def programmatically in some cases (this is needed for JUnit support, for
   * example).
   */
  @Override
  protected void compile(TreeLogger logger, ModuleDef moduleDef)
      throws UnableToCompleteException {
    LegacyCompilerOptions newOptions = new GWTCompilerOptionsImpl(options);
    newOptions.setCompilationStateRetained(true);
    if (!new GWTCompiler(newOptions).run(logger, moduleDef)) {
      // TODO(jat): error dialog?
    }
  }

  @Override
  protected HostedModeBaseOptions createOptions() {
    return new ShellOptionsImpl();
  }

  @Override
  protected ArtifactAcceptor doCreateArtifactAcceptor(final ModuleDef module) {
    return new ArtifactAcceptor() {
      public void accept(TreeLogger logger, ArtifactSet artifacts)
          throws UnableToCompleteException {

        /*
         * Copied from StandardLinkerContext.produceOutputDirectory() for legacy
         * GWTShellServlet support.
         */
        for (EmittedArtifact artifact : artifacts.find(EmittedArtifact.class)) {
          if (!artifact.isPrivate()) {
            File outFile = new File(options.getShellPublicGenDir(module),
                artifact.getPartialPath());
            Util.copy(logger, artifact.getContents(logger), outFile);
          }
        }
      }
    };
  }

  @Override
  protected void doShutDownServer() {
    // Stop the HTTP server.
    //
    EmbeddedTomcatServer.stop();
  }

  @Override
  protected int doStartUpServer() {
    // TODO(bruce): make tomcat work in terms of the modular launcher
    String whyFailed = EmbeddedTomcatServer.start(isHeadless() ? getTopLogger()
        : webServerLog.getLogger(), getPort(), options);

    if (whyFailed != null) {
      getTopLogger().log(TreeLogger.ERROR, "Starting Tomcat: " + whyFailed);
      return -1;
    }
    return EmbeddedTomcatServer.getPort();
  }

  @Override
  protected ImageIcon getWebServerIcon() {
    return null;
  }

  @Override
  protected String getWebServerName() {
    return "Tomcat";
  }
}
