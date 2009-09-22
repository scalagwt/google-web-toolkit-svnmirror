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
import com.google.gwt.dev.cfg.ModuleDef;
import com.google.gwt.dev.shell.BrowserWidget;
import com.google.gwt.dev.shell.BrowserWidgetHost;
import com.google.gwt.dev.shell.ModuleSpaceHost;
import com.google.gwt.dev.shell.PlatformSpecific;
import com.google.gwt.dev.shell.ShellMainWindow;
import com.google.gwt.dev.shell.ShellModuleSpaceHost;
import com.google.gwt.dev.util.log.AbstractTreeLogger;
import com.google.gwt.util.tools.ToolBase;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.Library;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import java.util.ArrayList;
import java.util.List;

/**
 * The main executable class for hosted mode shells based on SWT.
 */
abstract class SwtHostedModeBase extends HostedModeBase {

  private class SwtBrowserWidgetHostImpl extends BrowserWidgetHostImpl {

    @Override
    public ModuleSpaceHost createModuleSpaceHost(TreeLogger logger,
        BrowserWidget widget, String moduleName)
        throws UnableToCompleteException {
      // Switch to a wait cursor.
      Shell widgetShell = widget.getShell();
      try {
        Cursor waitCursor = display.getSystemCursor(SWT.CURSOR_WAIT);
        widgetShell.setCursor(waitCursor);

        // Try to find an existing loaded version of the module def.
        //
        ModuleDef moduleDef = loadModule(logger, moduleName, true);
        assert (moduleDef != null);

        TypeOracle typeOracle = moduleDef.getTypeOracle(logger);
        ShellModuleSpaceHost host = doCreateShellModuleSpaceHost(
            getTopLogger(), typeOracle, moduleDef);
        return host;
      } finally {
        Cursor normalCursor = display.getSystemCursor(SWT.CURSOR_ARROW);
        widgetShell.setCursor(normalCursor);
      }
    }
  }
  
  static {
    /*
     * The following check must be made before attempting to start SWT, or we'll fail with a
     * less-than-helpful UnsatisfiedLinkError.
     */
    if (!is32BitJvm()) {
      System.err.println("You must use a 32-bit Java runtime to run GWT Hosted Mode.");
      if (isMacOsX()) {
        // Provide an extra hint for Mac users due to previous GWT incompatibiity with Snow Leopard
        System.err.println("  Leopard: Use the Java 1.5 runtime.");
        System.err.println("  Snow Leopard: Use the Java 1.6 runtime and add the -d32 flag.");
      }
      System.exit(1);
    }
    
    // Force ToolBase to clinit, which causes SWT stuff to happen.
    new ToolBase() {
    };
    // Correct menu on Mac OS X
    Display.setAppName("GWT");
  }
  
  /**
   * Determine if we're using a 32 bit runtime.
   */
  private static boolean is32BitJvm() {
    return "32".equals(System.getProperty("sun.arch.data.model"));
  }

  /**
   * Determine if we're using Mac OS X.
   */
  private static boolean isMacOsX() {
    String osName = System.getProperty("os.name").toLowerCase();
    return osName.startsWith("mac os x");
  }

  private BrowserWidgetHostImpl browserHost = new SwtBrowserWidgetHostImpl();

  private final List<Shell> browserShells = new ArrayList<Shell>();

  /**
   * Use the default display; constructing a new one would make instantiating
   * multiple GWTShells fail with a mysterious exception.
   */
  private final Display display = Display.getDefault();

  private ShellMainWindow mainWnd;

  public SwtHostedModeBase() {
    super();
  }

  @Override
  public final void closeAllBrowserWindows() {
    while (!browserShells.isEmpty()) {
      browserShells.get(0).dispose();
    }
  }

  @Override
  public TreeLogger getTopLogger() {
    return mainWnd.getLogger();
  }

  @Override
  public final boolean hasBrowserWindowsOpen() {
    if (browserShells.isEmpty()) {
      return false;
    } else {
      return true;
    }
  }

  /**
   * Launch the arguments as Urls in separate windows.
   */
  @Override
  public void launchStartupUrls(final TreeLogger logger) {
    // Launch a browser window for each startup url.
    String startupURL = "";
    try {
      for (String prenormalized : options.getStartupURLs()) {
        startupURL = normalizeURL(prenormalized);
        logger.log(TreeLogger.TRACE, "Starting URL: " + startupURL, null);
        BrowserWidget bw = openNewBrowserWindow();
        bw.go(startupURL);
      }
    } catch (UnableToCompleteException e) {
      logger.log(TreeLogger.ERROR,
          "Unable to open new window for startup URL: " + startupURL, null);
    }
  }

  /**
   * Called directly by ShellMainWindow and indirectly via BrowserWidgetHost.
   */
  public final BrowserWidget openNewBrowserWindow()
      throws UnableToCompleteException {
    boolean succeeded = false;
    Shell s = createTrackedBrowserShell();
    try {
      BrowserWidget bw = PlatformSpecific.createBrowserWidget(getTopLogger(),
          s, browserHost);

      if (mainWnd != null) {
        Rectangle r = mainWnd.getShell().getBounds();
        int n = browserShells.size() + 1;
        s.setBounds(r.x + n * 50, r.y + n * 50, 800, 600);
      } else {
        s.setSize(800, 600);
      }

      if (!isHeadless()) {
        s.open();
      }

      bw.onFirstShown();
      succeeded = true;
      return bw;
    } finally {
      if (!succeeded) {
        s.dispose();
      }
    }
  }

  protected final BrowserWidgetHost getBrowserHost() {
    return browserHost;
  }

  protected abstract String getTitleText();

  @Override
  protected void initializeLogger() {
    final AbstractTreeLogger logger = mainWnd.getLogger();
    logger.setMaxDetail(options.getLogLevel());
  }

  @Override
  protected void loadRequiredNativeLibs() {
    String libName = null;
    try {
      libName = "swt";
      Library.loadLibrary(libName);
    } catch (UnsatisfiedLinkError e) {
      StringBuffer sb = new StringBuffer();
      sb.append("Unable to load required native library '" + libName + "'");
      sb.append("\n\tPlease specify the JVM startup argument ");
      sb.append("\"-Djava.library.path\"");
      throw new RuntimeException(sb.toString(), e);
    }
  }

  @Override
  protected boolean notDone() {
    if (!mainWnd.isDisposed()) {
      return true;
    }
    if (!browserShells.isEmpty()) {
      return true;
    }
    return false;
  }

  @Override
  protected void openAppWindow() {
    final Shell shell = new Shell(display);

    FillLayout fillLayout = new FillLayout();
    fillLayout.marginWidth = 0;
    fillLayout.marginHeight = 0;
    shell.setLayout(fillLayout);

    shell.setImages(ShellMainWindow.getIcons());

    mainWnd = new ShellMainWindow(this, shell, getTitleText(),
        options.isNoServer() ? 0 : getPort());

    shell.setSize(700, 600);
    if (!isHeadless()) {
      shell.open();
    }
  }

  @Override
  protected void processEvents() throws Exception {
    if (!display.readAndDispatch()) {
      sleep();
    }
  }

  protected void sleep() {
    display.sleep();
  }

  private Shell createTrackedBrowserShell() {
    final Shell shell = new Shell(display);
    FillLayout fillLayout = new FillLayout();
    fillLayout.marginWidth = 0;
    fillLayout.marginHeight = 0;
    shell.setLayout(fillLayout);
    browserShells.add(shell);
    shell.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e) {
        if (e.widget == shell) {
          browserShells.remove(shell);
        }
      }
    });

    shell.setImages(ShellMainWindow.getIcons());

    return shell;
  }
}
