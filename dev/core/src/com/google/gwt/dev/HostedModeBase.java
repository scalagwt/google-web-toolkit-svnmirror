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
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.dev.Precompile.PrecompileOptionsImpl;
import com.google.gwt.dev.cfg.ModuleDef;
import com.google.gwt.dev.cfg.ModuleDefLoader;
import com.google.gwt.dev.jjs.JJSOptions;
import com.google.gwt.dev.shell.ArtifactAcceptor;
import com.google.gwt.dev.shell.BrowserChannelServer;
import com.google.gwt.dev.shell.BrowserListener;
import com.google.gwt.dev.shell.BrowserWidgetHost;
import com.google.gwt.dev.shell.BrowserWidgetHostChecker;
import com.google.gwt.dev.shell.CheckForUpdates;
import com.google.gwt.dev.shell.ModuleSpaceHost;
import com.google.gwt.dev.shell.OophmSessionHandler;
import com.google.gwt.dev.shell.ShellModuleSpaceHost;
import com.google.gwt.dev.ui.DevModeUI;
import com.google.gwt.dev.ui.DoneCallback;
import com.google.gwt.dev.ui.DoneEvent;
import com.google.gwt.dev.ui.DevModeUI.ModuleHandle;
import com.google.gwt.dev.util.BrowserInfo;
import com.google.gwt.dev.util.Util;
import com.google.gwt.dev.util.arg.ArgHandlerDisableAggressiveOptimization;
import com.google.gwt.dev.util.arg.ArgHandlerDisableCastChecking;
import com.google.gwt.dev.util.arg.ArgHandlerDisableClassMetadata;
import com.google.gwt.dev.util.arg.ArgHandlerDraftCompile;
import com.google.gwt.dev.util.arg.ArgHandlerEnableAssertions;
import com.google.gwt.dev.util.arg.ArgHandlerGenDir;
import com.google.gwt.dev.util.arg.ArgHandlerLogLevel;
import com.google.gwt.dev.util.arg.ArgHandlerScriptStyle;
import com.google.gwt.dev.util.arg.OptionGenDir;
import com.google.gwt.dev.util.arg.OptionLogLevel;
import com.google.gwt.util.tools.ArgHandlerFlag;
import com.google.gwt.util.tools.ArgHandlerString;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Semaphore;

/**
 * The main executable class for the hosted mode shell. This class must not have
 * any GUI dependencies.
 */
abstract class HostedModeBase implements DoneCallback {

  /**
   * Implementation of BrowserWidgetHost that supports the abstract UI
   * interface.
   */
  public class UiBrowserWidgetHostImpl implements BrowserWidgetHost {

    public void compile() throws UnableToCompleteException {
      if (isLegacyMode()) {
        throw new UnsupportedOperationException();
      }
      HostedModeBase.this.compile(getLogger());
    }

    @Deprecated
    public void compile(String[] moduleNames) throws UnableToCompleteException {
      if (!isLegacyMode()) {
        throw new UnsupportedOperationException();
      }
      for (int i = 0; i < moduleNames.length; i++) {
        String moduleName = moduleNames[i];
        ModuleDef moduleDef = loadModule(getLogger(), moduleName, true);
        HostedModeBase.this.compile(getLogger(), moduleDef);
      }
    }

    public ModuleSpaceHost createModuleSpaceHost(TreeLogger mainLogger,
        String moduleName, String userAgent, String url, String tabKey,
        String sessionKey, BrowserChannelServer serverChannel,
        byte[] userAgentIcon) throws UnableToCompleteException {
      if (sessionKey == null) {
        // if we don't have a unique session key, make one up
        sessionKey = randomString();
      }
      TreeLogger logger = mainLogger;
      TreeLogger.Type maxLevel = options.getLogLevel();
      String agentTag = BrowserInfo.getShortName(userAgent);
      String remoteSocket = serverChannel.getRemoteEndpoint();
      ModuleHandle module = ui.loadModule(userAgent, remoteSocket, url, tabKey,
          moduleName, sessionKey, agentTag, userAgentIcon, maxLevel);
      // TODO(jat): add support for closing an active module
      logger = module.getLogger();
      try {
        // Try to find an existing loaded version of the module def.
        ModuleDef moduleDef = loadModule(logger, moduleName, true);
        assert (moduleDef != null);

        // Create a sandbox for the module.
        // TODO(jat): consider multiple instances of the same module open at
        // once
        TypeOracle typeOracle = moduleDef.getTypeOracle(logger);
        ShellModuleSpaceHost host = doCreateShellModuleSpaceHost(logger,
            typeOracle, moduleDef);

        loadedModules.put(host, module);
        return host;
      } catch (RuntimeException e) {
        logger.log(TreeLogger.ERROR, "Exception initializing module", e);
        ui.unloadModule(module);
        throw e;
      }
    }

    public TreeLogger getLogger() {
      return getTopLogger();
    }

    public boolean initModule(String moduleName) {
      return HostedModeBase.this.initModule(moduleName);
    }

    @Deprecated
    public boolean isLegacyMode() {
      return HostedModeBase.this instanceof GWTShell;
    }

    public String normalizeURL(String whatTheUserTyped) {
      return HostedModeBase.this.normalizeURL(whatTheUserTyped);
    }

    public void unloadModule(ModuleSpaceHost moduleSpaceHost) {
      ModuleHandle module = loadedModules.remove(moduleSpaceHost);
      if (module != null) {
        ui.unloadModule(module);
      }
    }
  }

  /**
   * Handles the -blacklist command line argument.
   */
  protected static class ArgHandlerBlacklist extends ArgHandlerString {
    @Override
    public String getPurpose() {
      return "Prevents the user browsing URLs that match the specified regexes (comma or space separated)";
    }

    @Override
    public String getTag() {
      return "-blacklist";
    }

    @Override
    public String[] getTagArgs() {
      return new String[] {"blacklist-string"};
    }

    @Override
    public boolean setString(String blacklistStr) {
      return BrowserWidgetHostChecker.blacklistRegexes(blacklistStr);
    }
  }

  /**
   * Handles the -logdir command line option.
   */
  protected static class ArgHandlerLogDir extends ArgHandlerString {
    private final OptionLogDir options;

    public ArgHandlerLogDir(OptionLogDir options) {
      this.options = options;
    }

    @Override
    public String getPurpose() {
      return "Logs to a file in the given directory, as well as graphically";
    }

    @Override
    public String getTag() {
      return "-logdir";
    }
    @Override
    public String[] getTagArgs() {
      return new String[] {"directory"};
    }

    @Override
    public boolean setString(String value) {
      options.setLogFile(value);
      return true;
    }
  }

  /**
   * Handles the -noserver command line flag.
   */
  protected static class ArgHandlerNoServerFlag extends ArgHandlerFlag {
    private final OptionNoServer options;

    public ArgHandlerNoServerFlag(OptionNoServer options) {
      this.options = options;
    }

    @Override
    public String getPurpose() {
      return "Prevents the embedded web server from running";
    }

    @Override
    public String getTag() {
      return "-noserver";
    }

    @Override
    public boolean setFlag() {
      options.setNoServer(true);
      return true;
    }
  }

  /**
   * Handles the -port command line flag.
   */
  protected static class ArgHandlerPort extends ArgHandlerString {

    private final OptionPort options;

    public ArgHandlerPort(OptionPort options) {
      this.options = options;
    }

    @Override
    public String[] getDefaultArgs() {
      return new String[] {getTag(), "8888"};
    }

    @Override
    public String getPurpose() {
      return "Specifies the TCP port for the embedded web server (defaults to 8888)";
    }

    @Override
    public String getTag() {
      return "-port";
    }

    @Override
    public String[] getTagArgs() {
      return new String[] {"port-number | \"auto\""};
    }

    @Override
    public boolean setString(String value) {
      if (value.equals("auto")) {
        options.setPort(0);
      } else {
        try {
          options.setPort(Integer.parseInt(value));
        } catch (NumberFormatException e) {
          System.err.println("A port must be an integer or \"auto\"");
          return false;
        }
      }
      return true;
    }
  }

  /**
   * Handles the -portHosted command line flag.
   */
  protected static class ArgHandlerPortHosted extends ArgHandlerString {

    private final OptionPortHosted options;

    public ArgHandlerPortHosted(OptionPortHosted options) {
      this.options = options;
    }

    @Override
    public String[] getDefaultArgs() {
      return new String[] {"-portHosted", "9997"};
    }

    @Override
    public String getPurpose() {
      return "Listens on the specified port for hosted mode connections";
    }

    @Override
    public String getTag() {
      return "-portHosted";
    }

    @Override
    public String[] getTagArgs() {
      return new String[] {"port-number | \"auto\""};
    }

    @Override
    public boolean setString(String value) {
      if (value.equals("auto")) {
        options.setPortHosted(0);
      } else {
        try {
          options.setPortHosted(Integer.parseInt(value));
        } catch (NumberFormatException e) {
          System.err.println("A port must be an integer or \"auto\"");
          return false;
        }
      }
      return true;
    }
  }

  /**
   * Handles the -whitelist command line flag.
   */
  protected static class ArgHandlerWhitelist extends ArgHandlerString {
    @Override
    public String getPurpose() {
      return "Allows the user to browse URLs that match the specified regexes (comma or space separated)";
    }

    @Override
    public String getTag() {
      return "-whitelist";
    }

    @Override
    public String[] getTagArgs() {
      return new String[] {"whitelist-string"};
    }

    @Override
    public boolean setString(String whitelistStr) {
      return BrowserWidgetHostChecker.whitelistRegexes(whitelistStr);
    }
  }

  protected interface HostedModeBaseOptions extends JJSOptions, OptionLogDir,
      OptionLogLevel, OptionGenDir, OptionNoServer, OptionPort,
      OptionPortHosted, OptionStartupURLs {

    /**
     * The base shell work directory.
     * 
     * @param moduleDef 
     * @return working directory base 
     */
    File getShellBaseWorkDir(ModuleDef moduleDef);
  }

  /**
   * Concrete class to implement all hosted mode base options.
   */
  protected static class HostedModeBaseOptionsImpl extends
      PrecompileOptionsImpl implements HostedModeBaseOptions {

    private boolean isNoServer;
    private File logDir;
    private int port;
    private int portHosted;
    private final List<String> startupURLs = new ArrayList<String>();

    public void addStartupURL(String url) {
      startupURLs.add(url);
    }

    public boolean alsoLogToFile() {
      return logDir != null;
    }

    public File getLogDir() {
      return logDir;
    }

    public File getLogFile(String sublog) {
      if (logDir == null) {
        return null;
      }
      return new File(logDir, sublog);
    }

    public int getPort() {
      return port;
    }

    public int getPortHosted() {
      return portHosted;
    }

    public File getShellBaseWorkDir(ModuleDef moduleDef) {
      return new File(new File(getWorkDir(), moduleDef.getName()), "shell");
    }

    public List<String> getStartupURLs() {
      return Collections.unmodifiableList(startupURLs);
    }

    public boolean isNoServer() {
      return isNoServer;
    }

    public void setLogFile(String filename) {
      logDir = new File(filename);
    }

    public void setNoServer(boolean isNoServer) {
      this.isNoServer = isNoServer;
    }

    public void setPort(int port) {
      this.port = port;
    }

    public void setPortHosted(int port) {
      portHosted = port;
    }
  }

  /**
   * Controls whether and where to log data to file.
   * 
   */
  protected interface OptionLogDir {
    boolean alsoLogToFile();

    File getLogDir();

    File getLogFile(String subfile);

    void setLogFile(String filename);
  }


  /**
   * Controls whether to run a server or not.
   * 
   */
  protected interface OptionNoServer {
    boolean isNoServer();

    void setNoServer(boolean isNoServer);
  }

  /**
   * Controls what port to use.
   * 
   */
  protected interface OptionPort {
    int getPort();

    void setPort(int port);
  }

  protected interface OptionPortHosted {
    int getPortHosted();

    void setPortHosted(int portHosted);
  }

  /**
   * Controls the startup URLs.
   */
  protected interface OptionStartupURLs {
    void addStartupURL(String url);

    List<String> getStartupURLs();
  }

  abstract static class ArgProcessor extends ArgProcessorBase {
    public ArgProcessor(HostedModeBaseOptions options, boolean forceServer) {
      if (!forceServer) {
        registerHandler(new ArgHandlerNoServerFlag(options));
      }
      registerHandler(new ArgHandlerPort(options));
      registerHandler(new ArgHandlerWhitelist());
      registerHandler(new ArgHandlerBlacklist());
      registerHandler(new ArgHandlerLogDir(options));
      registerHandler(new ArgHandlerLogLevel(options));
      registerHandler(new ArgHandlerGenDir(options));
      registerHandler(new ArgHandlerScriptStyle(options));
      registerHandler(new ArgHandlerEnableAssertions(options));
      registerHandler(new ArgHandlerDisableAggressiveOptimization(options));
      registerHandler(new ArgHandlerDisableClassMetadata(options));
      registerHandler(new ArgHandlerDisableCastChecking(options));
      registerHandler(new ArgHandlerDraftCompile(options));
      registerHandler(new ArgHandlerPortHosted(options));
      // TODO(rdayal): implement
      // registerHandler(new ArgHandlerRemoteUI(options));
    }
  }

  private static final Random RNG = new Random();

  public static String normalizeURL(String unknownUrlText, int port, String host) {
    if (unknownUrlText.indexOf(":") != -1) {
      // Assume it's a full url.
      return unknownUrlText;
    }

    // Assume it's a trailing url path.
    if (unknownUrlText.length() > 0 && unknownUrlText.charAt(0) == '/') {
      unknownUrlText = unknownUrlText.substring(1);
    }

    if (port != 80) {
      // CHECKSTYLE_OFF: Not really an assembled error message, so no space
      // after ':'.
      return "http://" + host + ":" + port + "/" + unknownUrlText;
      // CHECKSTYLE_ON
    } else {
      return "http://" + host + "/" + unknownUrlText;
    }
  }

  /**
   * Produce a random string that has low probability of collisions.
   * 
   * <p>In this case, we use 16 characters, each drawn from a pool of 94,
   * so the number of possible values is 94^16, leading to an expected number
   * of values used before a collision occurs as sqrt(pi/2) * 94^8 (treated the
   * same as a birthday attack), or a little under 10^16.
   * 
   * <p>This algorithm is also implemented in hosted.html, though it is not
   * technically important that they match.
   * 
   * @return a random string
   */
  protected static String randomString() {
    StringBuilder buf = new StringBuilder(16);
    for (int i = 0; i < 16; ++i) {
      buf.append((char) RNG.nextInt('~' - '!' + 1) + '!');
    }
    return buf.toString();
  }

  protected int codeServerPort;

  protected BrowserListener listener;

  protected final HostedModeBaseOptions options;

  protected DevModeUI ui = null;

  /**
   * Cheat on the first load's refresh by assuming the module loaded by
   * {@link com.google.gwt.dev.shell.GWTShellServlet} is still fresh. This
   * prevents a double-refresh on startup. Subsequent refreshes will trigger a
   * real refresh.
   */
  private Set<String> alreadySeenModules = new HashSet<String>();

  private final Semaphore blockUntilDone = new Semaphore(0);

  private BrowserWidgetHost browserHost = new UiBrowserWidgetHostImpl();

  private boolean headlessMode = false;

  private final Map<ModuleSpaceHost, ModuleHandle> loadedModules = new IdentityHashMap<ModuleSpaceHost, ModuleHandle>();

  private boolean started;

  private TreeLogger topLogger;

  public HostedModeBase() {
    // Set any platform specific system properties.
    BootStrapPlatform.initHostedMode();
    BootStrapPlatform.applyPlatformHacks();
    options = createOptions();
  }

  public final void addStartupURL(String url) {
    options.addStartupURL(url);
  }

  public final int getPort() {
    return options.getPort();
  }

  public TreeLogger getTopLogger() {
    return topLogger;
  }
  
  /**
   * Launch the arguments as Urls in separate windows.
   * 
   * @param logger TreeLogger instance to use 
   */
  public void launchStartupUrls(final TreeLogger logger) {
    ensureCodeServerListener();
    String startupURL = "";
    try {
      for (String prenormalized : options.getStartupURLs()) {
        startupURL = normalizeURL(prenormalized);
        logger.log(TreeLogger.INFO, "Starting URL: " + startupURL, null);
        launchURL(startupURL);
      }
    } catch (UnableToCompleteException e) {
      logger.log(TreeLogger.ERROR,
          "Unable to open new window for startup URL: " + startupURL, null);
    }
  }

  public final String normalizeURL(String unknownUrlText) {
    return normalizeURL(unknownUrlText, getPort(), getHost());
  }
 
  /**
   * Callback for the UI to indicate it is done.
   */
  public void onDone() {
    setDone();
  }

  /**
   * Sets up all the major aspects of running the shell graphically, including
   * creating the main window and optionally starting an embedded web server.
   */
  public final void run() {
    try {
      if (startUp()) {
        // Eager AWT init for OS X to ensure safe coexistence with SWT.
        BootStrapPlatform.initGui();

        // The web server is running now, so launch browsers for startup urls.
        launchStartupUrls(getTopLogger());
      }

      blockUntilDone.acquire();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      shutDown();
    }
  }

  public final void setPort(int port) {
    options.setPort(port);
  }

  public final void setRunTomcat(boolean run) {
    options.setNoServer(!run);
  }

  /**
   * Derived classes can override to lengthen ping delay.
   */
  protected long checkForUpdatesInterval() {
    return CheckForUpdates.ONE_MINUTE;
  }

  /**
   * Compiles all modules.
   * 
   * @throws UnableToCompleteException
   */
  protected abstract void compile(TreeLogger logger)
      throws UnableToCompleteException;

  /**
   * Compiles a module (legacy only).
   */
  @Deprecated
  protected abstract void compile(TreeLogger logger, ModuleDef moduleDef)
      throws UnableToCompleteException;

  protected abstract HostedModeBaseOptions createOptions();

  protected abstract ArtifactAcceptor doCreateArtifactAcceptor(ModuleDef module);

  /**
   * Creates an instance of ShellModuleSpaceHost (or a derived class) using the
   * specified constituent parts. This method is made to be overridden for
   * subclasses that need to change the behavior of ShellModuleSpaceHost.
   * 
   * @param logger TreeLogger to use
   * @param typeOracle
   * @param moduleDef
   * @return ShellModuleSpaceHost instance
   */
  protected final ShellModuleSpaceHost doCreateShellModuleSpaceHost(
      TreeLogger logger, TypeOracle typeOracle, ModuleDef moduleDef) {
    // Clear out the shell temp directory.
    Util.recursiveDelete(options.getShellBaseWorkDir(moduleDef), true);
    return new ShellModuleSpaceHost(logger, typeOracle, moduleDef,
        options.getGenDir(), new File(options.getShellBaseWorkDir(moduleDef),
            "gen"), doCreateArtifactAcceptor(moduleDef));
  }

  protected abstract void doShutDownServer();

  /**
   * Perform any startup tasks, including initializing the UI (if any) and the
   * logger, updates checker, and the development mode code server.
   * 
   * <p>Subclasses that override this method should be careful what facilities
   * are used before the super implementation is called.
   * 
   * @return true if startup was successful
   */
  protected boolean doStartup() {
    // Create the main app window.
    ui.initialize(options.getLogLevel());
    topLogger = ui.getTopLogger();

    // Set done callback
    ui.setCallback(DoneEvent.getType(), this);
    
    // Check for updates
    final TreeLogger logger = getTopLogger();
    final CheckForUpdates updateChecker
        = CheckForUpdates.createUpdateChecker(logger);
    if (updateChecker != null) {
      Thread checkerThread = new Thread("GWT Update Checker") {
        @Override
        public void run() {
          CheckForUpdates.logUpdateAvailable(logger,
              updateChecker.check(checkForUpdatesInterval()));
        }
      };
      checkerThread.setDaemon(true);
      checkerThread.start();
    }

    // Accept connections from OOPHM clients
    ensureCodeServerListener();

    return true;
  }

  protected abstract int doStartUpServer();

  protected void ensureCodeServerListener() {
    if (listener == null) {
      codeServerPort = options.getPortHosted();
      listener = new BrowserListener(getTopLogger(), codeServerPort,
          new OophmSessionHandler(browserHost));
      listener.start();
      try {
        // save the port we actually used if it was auto
        codeServerPort = listener.getSocketPort();
      } catch (UnableToCompleteException e) {
        // ignore errors listening, we will catch them later
      }
    }
  }

  protected String getHost() {
    return "localhost";
  }

  /**
   * Called from a selection script as it begins to load in hosted mode. This
   * triggers a hosted mode link, which might actually update the running
   * selection script.
   * 
   * @param moduleName the module to link
   * @return <code>true</code> if the selection script was overwritten; this
   *         will trigger a full-page refresh by the calling (out of date)
   *         selection script
   */
  protected boolean initModule(String moduleName) {
    /*
     * Not used in legacy mode due to GWTShellServlet playing this role.
     * 
     * TODO: something smarter here and actually make GWTShellServlet less
     * magic?
     */
    return false;
  }

  /**
   * By default we will open the application window.
   * 
   * @return true if we are running in headless mode
   */
  protected final boolean isHeadless() {
    return headlessMode;
  }

  protected void launchURL(String url) throws UnableToCompleteException {
    /*
     * TODO(jat): properly support launching arbitrary browsers; waiting on
     * Freeland's work with BrowserScanner and the trunk merge to get it.
     */
    try {
      URL parsedUrl = new URL(url);
      String path = parsedUrl.getPath();
      String query = parsedUrl.getQuery();
      String hash = parsedUrl.getRef();
      String hostedParam =  "gwt.hosted=" + listener.getEndpointIdentifier();
      if (query == null) {
        query = hostedParam;
      } else {
        query += '&' + hostedParam;
      }
      path += '?' + query;
      if (hash != null) {
        path += '#' + hash;
      }
      url = new URL(parsedUrl.getProtocol(), parsedUrl.getHost(),
          parsedUrl.getPort(), path).toExternalForm();
    } catch (MalformedURLException e) {
      getTopLogger().log(TreeLogger.ERROR, "Invalid URL " + url, e);
      throw new UnableToCompleteException();
    }
    System.err.println(
        "Using a browser with the GWT Development Plugin, please browse to");
    System.err.println("the following URL:");
    System.err.println("  " + url);
    getTopLogger().log(TreeLogger.INFO,
        "Waiting for browser connection to " + url, null);
  }

  /**
   * Load a module.
   * 
   * @param moduleName name of the module to load
   * @param logger TreeLogger to use
   * @param refresh if <code>true</code>, refresh the module from disk
   * @return the loaded module
   * @throws UnableToCompleteException
   */
  protected ModuleDef loadModule(TreeLogger logger, String moduleName,
      boolean refresh) throws UnableToCompleteException {
    refresh &= alreadySeenModules.contains(moduleName);
    ModuleDef moduleDef = ModuleDefLoader.loadFromClassPath(logger, moduleName,
        refresh);
    alreadySeenModules.add(moduleName);
    assert (moduleDef != null) : "Required module state is absent";
    return moduleDef;
  }

  protected final void setDone() {
    blockUntilDone.release();
  }

  protected final void setHeadless(boolean headlessMode) {
    this.headlessMode = headlessMode;
  }

  protected final void shutDown() {
    if (options.isNoServer()) {
      return;
    }
    doShutDownServer();
  }

  protected final boolean startUp() {
    if (started) {
      throw new IllegalStateException("Startup code has already been run");
    }

    // If no UI was specified by command-line args, then create one.
    if (ui == null) {
      if (headlessMode) {
        ui = new HeadlessUI(options);
      } else {
        ui = new SwingUI(options);
      }
    }

    started = true;

    if (!doStartup()) {
      return false;
    }

    if (!options.isNoServer()) {
      int resultPort = doStartUpServer();
      if (resultPort < 0) {
        return false;
      }
      options.setPort(resultPort);
      getTopLogger().log(TreeLogger.INFO, "Started web server on port "
          + resultPort);
    }

    return true;
  }
}
