/*
 * Copyright 2006 Google Inc.
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
package com.google.gwt.dev.shell.profiler;

import com.google.gwt.dev.GWTShell;
import com.google.gwt.dev.BootStrapPlatform;
import com.google.gwt.dev.GWTCompiler;
import com.google.gwt.dev.cfg.ModuleDef;
import com.google.gwt.dev.shell.BrowserWidget;
import com.google.gwt.dev.shell.profiler.agent.ProfilingAgent;
import com.google.gwt.util.tools.ArgHandlerString;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.TreeLogger;

/**
 * A shell for profiling GWT applications.
 *
 *   - The application is run in "gwt.hybrid mode" which means it
 * is sent through the GWT compiler and GWTShellServlet does not generate
 * a bogus nocache.html.
 *
 *   - The compiler is instructed to instrument the application with calls
 * to the profiling API (only available from window.external.profiler
 * in the HostedBrowser).
 *
 */
public class ProfilerShell extends GWTShell {

  /**
   * Handles the -module command line flag.
   */
  protected class ArgHandlerModule extends ArgHandlerString {
    public String[] getDefaultArgs() {
      return new String[] {"-module", ""};
    }

    public String getPurpose() {
      return "Specifies the application module";
    }

    public String getTag() {
      return "-module";
    }

    public String[] getTagArgs() {
      return new String[] {"module-string"};
    }

    public boolean isRequired() {
      return true;
    }

    public boolean setString(String module) {
      ProfilerShell.this.module = module;
      return true;
    }
  }

  // private final static String PROP_GWT_HYBRID_MODE = "gwt.hybrid";
  private static final String PROP_GWT_HYBRID_MODE = "compiled";

  public static void main(String[] args) {
    BootStrapPlatform.go();
    ProfilerShell shell = new ProfilerShell();
    if (shell.processArgs(args)) {
      shell.run();
    }
  }

  private String module;

  /**
   * Creates a new ProfilerShell.
   *
   */
  private ProfilerShell() {
    super(true,false);
    registerHandler(new ArgHandlerModule());
    setRunTomcat(true);
    setHeadless(false);
  }

  public BrowserWidget openNewBrowserWindow() throws UnableToCompleteException {
    BrowserWidget widget = super.openNewBrowserWindow();
    getBrowserHost().compile( new String[] { module } );
    ProfilerImpl profiler = new ProfilerImpl();
    profiler.setTopLogger(getTopLogger());
    Agent agent = createProfilingAgent();
    profiler.setAgent( agent );
    agent.onLoad( profiler );
    widget.setProfiler( profiler );

    return widget;
  }

  public void run() {
    // Change the startupUrls so that they will refer to hybrid mode
    for ( int i = 0; i < startupUrls.size(); ++i ) {
      String url = (String) startupUrls.get( i );
      String newUrl = url + "?" + PROP_GWT_HYBRID_MODE;
      startupUrls.set( i, newUrl );
    }
    super.run();
  }

  /**
   * Override GWTShell so that we can feed the -profile switch to the compiler.
   * This probably needs to be refactored in GWTShell so we don't have to
   * reproduce this entire function here.
   */
  protected void compile(TreeLogger logger, ModuleDef moduleDef)
      throws UnableToCompleteException {
    GWTCompiler compiler = new GWTCompiler(moduleDef.getCacheManager());
    compiler.setGenDir( getGenDir() );
    compiler.setOutDir(outDir);
    compiler.setModuleName(moduleDef.getName());
    compiler.setLogLevel(getLogLevel());
    if (obfuscate) {
      compiler.setStyleObfuscated();
    } else if (prettyNames) {
      compiler.setStylePretty();
    } else {
      compiler.setStyleDetailed();
    }
    compiler.setProfile(true);
    compiler.distill(logger, moduleDef);
  }

  protected String doGetDefaultLogLevel() {
    return "WARN";
  }

  protected boolean doShouldCheckForUpdates() {
    return false;
  }

  private Agent createProfilingAgent() {
    // TODO(tobyr): Currently using the default Agent, but we
    // can look up an Agent implementation via configuration in the future.
    return new ProfilingAgent();
  }
}
