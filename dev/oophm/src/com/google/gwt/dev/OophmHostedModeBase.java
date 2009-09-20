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
import com.google.gwt.dev.WebServerPanel.RestartAction;
import com.google.gwt.dev.cfg.ModuleDef;
import com.google.gwt.dev.shell.BrowserListener;
import com.google.gwt.dev.shell.BrowserWidgetHost;
import com.google.gwt.dev.shell.ModuleSpaceHost;
import com.google.gwt.dev.shell.OophmSessionHandler;
import com.google.gwt.dev.shell.ShellMainWindow;
import com.google.gwt.dev.shell.ShellModuleSpaceHost;
import com.google.gwt.dev.util.BrowserInfo;
import com.google.gwt.dev.util.collect.HashMap;
import com.google.gwt.dev.util.log.AbstractTreeLogger;
import com.google.gwt.dev.util.log.PrintWriterTreeLogger;
import com.google.gwt.util.tools.ArgHandlerString;

import java.awt.Cursor;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;

/**
 * Base class for OOPHM hosted mode shells.
 */
abstract class OophmHostedModeBase extends HostedModeBase {

  /**
   * Interface to group activities related to adding and deleting tabs.
   */
  public interface TabPanelCollection {
    
    /**
     * Add a new tab containing a ModuleTabPanel.
     * 
     * @param tabPanel
     * @param icon
     * @param title
     * @param tooltip
     */
    void addTab(ModuleTabPanel tabPanel, ImageIcon icon, String title,
        String tooltip);
    
    /**
     * Remove the tab containing a ModuleTabpanel.
     * 
     * @param tabPanel
     */
    void removeTab(ModuleTabPanel tabPanel);
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
  
  protected interface OptionPortHosted {
    int getPortHosted();

    void setPortHosted(int portHosted);
  }

  abstract static class ArgProcessor extends HostedModeBase.ArgProcessor {
    public ArgProcessor(OophmHostedModeBaseOptions options, boolean forceServer) {
      super(options, forceServer);
      registerHandler(new ArgHandlerPortHosted(options));
    }
  }

  interface OophmHostedModeBaseOptions extends HostedModeBaseOptions,
      OptionPortHosted {
  }

  /**
   * Concrete class to implement all shell options.
   */
  static class OophmHostedModeBaseOptionsImpl extends HostedModeBaseOptionsImpl
      implements OophmHostedModeBaseOptions {
    private int portHosted;

    public int getPortHosted() {
      return portHosted;
    }

    public void setPortHosted(int port) {
      portHosted = port;
    }
  }
  
  private class OophmBrowserWidgetHostImpl extends BrowserWidgetHostImpl {
    private final Map<ModuleSpaceHost, ModulePanel> moduleTabs = new IdentityHashMap<ModuleSpaceHost, ModulePanel>();
    private final Map<DevelModeTabKey, ModuleTabPanel> tabPanels = new HashMap<DevelModeTabKey, ModuleTabPanel>();
    
    public ModuleSpaceHost createModuleSpaceHost(TreeLogger mainLogger,
        String moduleName, String userAgent, String url, String tabKey,
        String sessionKey, String remoteSocket)
        throws UnableToCompleteException {
      if (sessionKey == null) {
        // if we don't have a unique session key, make one up
        sessionKey = randomString();
      }
      TreeLogger logger = mainLogger;
      TreeLogger.Type maxLevel = TreeLogger.INFO;
      if (mainLogger instanceof AbstractTreeLogger) {
        maxLevel = ((AbstractTreeLogger) mainLogger).getMaxDetail();
      }
      ModuleTabPanel tabPanel = null;
      ModulePanel tab = null;
      if (!isHeadless()) {
        tabPanel = findModuleTab(userAgent, remoteSocket, url, tabKey,
            moduleName);
        String agentTag = BrowserInfo.getShortName(userAgent).toLowerCase();
        tab = tabPanel.addModuleSession(maxLevel, moduleName, sessionKey,
            options.getLogFile(String.format("%s-%s-%d.log", moduleName,
                agentTag, getNextSessionCounter(options.getLogDir()))));
        logger = tab.getLogger();
        TreeLogger branch = logger.branch(TreeLogger.INFO, "Loading module "
            + moduleName);
        if (url != null) {
          branch.log(TreeLogger.INFO, "Top URL: " + url);
        }
        branch.log(TreeLogger.INFO, "User agent: " + userAgent);
        branch.log(TreeLogger.TRACE, "Remote socket: " + remoteSocket);
        if (tabKey != null) {
          branch.log(TreeLogger.DEBUG, "Tab key: " + tabKey);
        }
        if (sessionKey != null) {
          branch.log(TreeLogger.DEBUG, "Session key: " + sessionKey);
        }

        // Switch to a wait cursor.
        frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      }

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

        if (tab != null) {
          moduleTabs.put(host, tab);
        }
        return host;
      } catch (RuntimeException e) {
        logger.log(TreeLogger.ERROR, "Exception initializing module", e);
        throw e;
      } finally {
        if (!isHeadless()) {
          frame.setCursor(Cursor.getDefaultCursor());
        }
      }
    }

    public void unloadModule(ModuleSpaceHost moduleSpaceHost) {
      Disconnectable tab = moduleTabs.remove(moduleSpaceHost);
      if (tab != null) {
        tab.disconnect();
      }
    }

    private ModuleTabPanel findModuleTab(String userAgent, String remoteSocket,
        String url, String tabKey, String moduleName) {
      int hostEnd = remoteSocket.indexOf(':');
      if (hostEnd < 0) {
        hostEnd = remoteSocket.length();
      }
      String remoteHost = remoteSocket.substring(0, hostEnd);
      final DevelModeTabKey key = new DevelModeTabKey(userAgent, url, tabKey,
          remoteHost);
      ModuleTabPanel moduleTabPanel = tabPanels.get(key);
      if (moduleTabPanel == null) {
        moduleTabPanel = new ModuleTabPanel(userAgent, remoteSocket, url,
            new TabPanelCollection() {
              public void addTab(ModuleTabPanel tabPanel, ImageIcon icon,
                  String title, String tooltip) {
                synchronized (tabs) {
                  tabs.addTab(title, icon, tabPanel, tooltip);
                  tabPanels.put(key, tabPanel);
                }
              }
    
              public void removeTab(ModuleTabPanel tabPanel) {
                synchronized (tabs) {
                  tabs.remove(tabPanel);
                  tabPanels.remove(key);
                }
              }
            }, moduleName);
      }
      return moduleTabPanel;
    }
  }

  protected static final String PACKAGE_PATH = OophmHostedModeBase.class.getPackage().getName().replace(
      '.', '/').concat("/shell/");

  private static final Random RNG = new Random();
  
  private static int sessionCounter = 0;

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

  /**
   * Loads an image from the classpath in this package.
   */
  static ImageIcon loadImageIcon(String name) {
    return loadImageIcon(name, true);
  }

  /**
   * Loads an image from the classpath, optionally prepending this package.
   * 
   * @param name name of an image file.
   * @param prependPackage true if {@link #PACKAGE_PATH} should be prepended to
   *          this name.
   */
  static ImageIcon loadImageIcon(String name, boolean prependPackage) {
    ClassLoader cl = OophmHostedModeBase.class.getClassLoader();
    if (prependPackage) {
      name = PACKAGE_PATH + name;
    }
    
    URL url = (name == null) ? null : cl.getResource(name);
    if (url != null) {
      ImageIcon image = new ImageIcon(url);
      return image;
    } else {
      // Bad image.
      return new ImageIcon();
    }
  }

  protected int codeServerPort;

  protected BrowserListener listener;

  /**
   * Hiding super field because it's actually the same object, just with a
   * stronger type.
   */
  @SuppressWarnings("hiding")
  protected final OophmHostedModeBaseOptionsImpl options = (OophmHostedModeBaseOptionsImpl) super.options;

  // TODO(jat): clean up access to this field
  protected WebServerPanel webServerLog;

  private BrowserWidgetHostImpl browserHost = new OophmBrowserWidgetHostImpl();

  private JFrame frame;

  private volatile boolean mainWindowClosed;

  private ShellMainWindow mainWnd;

  private JTabbedPane tabs;

  private AbstractTreeLogger topLogger;

  public OophmHostedModeBase() {
    super();
  }

  @Override
  public void closeAllBrowserWindows() {
  }

  @Override
  public TreeLogger getTopLogger() {
    return topLogger;
  }

  @Override
  public boolean hasBrowserWindowsOpen() {
    return false;
  }

  /**
   * Launch the arguments as Urls in separate windows.
   */
  @Override
  public void launchStartupUrls(final TreeLogger logger) {
    ensureOophmListener();
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

  public void launchURL(String url) throws UnableToCompleteException {
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
   * @throws UnableToCompleteException
   */
  @Override
  protected void compile(TreeLogger logger) throws UnableToCompleteException {
    throw new UnsupportedOperationException();
  }

  @Override
  protected boolean doStartup() {
    if (super.doStartup()) {
      // Accept connections from OOPHM clients
      ensureOophmListener();
      return true;
    }
    return false;
  }

  protected final BrowserWidgetHost getBrowserHost() {
    return browserHost;
  }

  protected int getNextSessionCounter(File logdir) {
    if (sessionCounter == 0 && logdir != null) {
      // first time only, figure out the "last" session count already in use
      for (String filename : logdir.list()) {
        if (filename.matches("^[A-Za-z0-9_$]*-[a-z]*-[0-9]*.log$")) {
          String substring = filename.substring(filename.lastIndexOf('-') + 1,
              filename.length() - 4);
          int number = Integer.parseInt(substring);
          if (number > sessionCounter) {
            sessionCounter = number;
          }
        }
      }
    }
    return ++sessionCounter;
  }

  /**
   * @return the icon to use for the web server tab
   */
  protected ImageIcon getWebServerIcon() {
    return null;
  }

  /**
   * @return the name of the web server tab
   */
  protected String getWebServerName() {
    return "Server";
  }

  @Override
  protected void initializeLogger() {
    if (mainWnd != null) {
      topLogger = mainWnd.getLogger();
    } else {
      topLogger = new PrintWriterTreeLogger(new PrintWriter(System.out));
    }
    topLogger.setMaxDetail(options.getLogLevel());
  }

  @Override
  protected boolean initModule(String moduleName) {
    /*
     * Not used in legacy mode due to GWTShellServlet playing this role.
     * 
     * TODO: something smarter here and actually make GWTShellServlet less
     * magic?
     */
    return false;
  }

  @Override
  protected synchronized boolean notDone() {
    return !mainWindowClosed;
  }

  @Override
  protected void openAppWindow() {
    if (isHeadless()) {
      return;
    }
    ImageIcon gwtIcon = loadImageIcon("icon24.png");
    frame = new JFrame("GWT Development Mode");
    tabs = new JTabbedPane();
    if (options.alsoLogToFile()) {
      options.getLogDir().mkdirs();
    }
    mainWnd = new ShellMainWindow(options.getLogLevel(),
        options.getLogFile("main.log"));
    tabs.addTab("Development Mode", gwtIcon, mainWnd, "GWT Development mode");
    if (!options.isNoServer()) {
      webServerLog = new WebServerPanel(getPort(), options.getLogLevel(),
          options.getLogFile("webserver.log"),
          new RestartAction() {
            public void restartServer(TreeLogger logger) {
              try {
                OophmHostedModeBase.this.restartServer(logger);
              } catch (UnableToCompleteException e) {
                // Already logged why it failed
              }
            }
          });
      tabs.addTab(getWebServerName(), getWebServerIcon(), webServerLog);
    }
    frame.getContentPane().add(tabs);
    frame.setSize(950, 700);
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosed(WindowEvent e) {
        setMainWindowClosed();
      }
    });
    frame.setIconImage(loadImageIcon("icon16.png").getImage());
    frame.setVisible(true);
  }

  @Override
  protected void processEvents() throws Exception {
    Thread.sleep(10);
  }

  protected synchronized void setMainWindowClosed() {
    mainWindowClosed = true;
  }

  private void ensureOophmListener() {
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
}
