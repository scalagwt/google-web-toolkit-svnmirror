/*
 * Copyright 2007 Google Inc.
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
package com.google.gwt.dev.shell;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.dev.GWTShell;
import com.google.gwt.dev.util.Util;
import com.google.gwt.dev.util.log.AbstractTreeLogger;
import com.google.gwt.dev.util.log.TreeLoggerWidget;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolItem;

/**
 * Implements the GWTShell's main window control.
 */
public class ShellMainWindow extends Composite implements DisposeListener,
    ShellListener {

  private class Toolbar extends HeaderBarBase {

    private ToolItem about;

    private ToolItem clearLog;
    private ToolItem collapseAll;
    private ToolItem expandAll;
    private ToolItem newWindow;

    public Toolbar(Composite parent) {
      super(parent);

      newWindow = newItem("new-window.gif", "&Hosted Browser",
          "Opens a new hosted mode browser window for debugging");
      newWindow.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent event) {
          String startupUrl = serverWindow.normalizeURL("/");
          try {
            BrowserWidget bw = serverWindow.openNewBrowserWindow();
            bw.go(startupUrl);
          } catch (UnableToCompleteException e) {
            getLogger().log(TreeLogger.ERROR,
                "Unable to open a new hosted browser window", e);
          }
        }
      });

      newSeparator();

      collapseAll = newItem("collapse.gif", "&Collapse All",
          "Collapses all log entries");
      collapseAll.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          logPane.collapseAll();
        }
      });

      expandAll = newItem("expand.gif", "&Expand All",
          "Expands all log entries");
      expandAll.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          logPane.expandAll();
        }
      });

      clearLog = newItem("clear-log.gif", "Clear &Log",
          "Removes all log entries");
      clearLog.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          logPane.removeAll();
        }
      });

      newSeparator();

      about = newItem("about.gif", "    &About    ", "About...");
      about.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          String aboutHtml = Util.getFileFromInstallPath("about.html");
          if (aboutHtml != null) {
            String serial = verify("TwysxNpVumPBvFyBoxzLy");
            StringBuffer sb = new StringBuffer();
            sb.append("<div style='overflow:hidden;width:100%;white-space:nowrap;font-size:1px'><br/><br/><br/><br/><font style='background-color:gray;color:lightgrey'>");
            for (int i = 0; i < 100; ++i) {
              sb.append(serial);
            }
            sb.append("</font></div>");
            serial = sb.toString();
            int pos;
            while ((pos = aboutHtml.indexOf("<hr/>")) >= 0) {
              aboutHtml = aboutHtml.substring(0, pos) + serial
                  + aboutHtml.substring(pos + 5);
            }
            while ((pos = aboutHtml.indexOf("<body>")) >= 0) {
              aboutHtml = aboutHtml.substring(0, pos)
                  + "<body oncontextmenu='return false'>"
                  + aboutHtml.substring(pos + 6);
            }
          } else {
            aboutHtml = "Could not locate 'about.html' in installation directory.";
          }
          BrowserDialog browserDialog = new BrowserDialog(getShell(),
              getLogger(), aboutHtml);
          browserDialog.open(true);
        }
      });
    }
  }

  private static String verify(String hash) {
    char[] in = hash.toCharArray();
    char[] ou = new char[in.length];
    for (int i = 0, c = 0; i < in.length; ++i) {
      if (in[i] < 'a') {
        c += in[i] - 'A';
      } else {
        c += in[i] - 'a' - 26;
      }

      if (c == 0) {
        ou[i] = ' ';
      } else {
        ou[i] = (char) ('@' + c);
      }
    }
    return String.valueOf(ou);
  }

  private Color colorWhite;

  private TreeLoggerWidget logPane;

  private GWTShell serverWindow;

  private Toolbar toolbar;

  public ShellMainWindow(GWTShell serverWindow, final Shell parent,
      int serverPort, boolean checkForUpdates) {
    super(parent, SWT.NONE);

    this.serverWindow = serverWindow;

    colorWhite = new Color(null, 255, 255, 255);

    addDisposeListener(this);
    parent.addShellListener(this);

    setLayout(new FillLayout());
    if (serverPort > 0) {
      parent.setText("Google Web Toolkit Development Shell / Port "
          + serverPort);
    } else {
      parent.setText("Google Web Toolkit Development Shell");
    }

    GridLayout gridLayout = new GridLayout(1, true);
    gridLayout.marginWidth = 0;
    gridLayout.marginHeight = 0;
    gridLayout.horizontalSpacing = 0;
    gridLayout.verticalSpacing = 0;
    setLayout(gridLayout);

    // Create the toolbar.
    //
    {
      toolbar = new Toolbar(this);
      GridData data = new GridData();
      data.grabExcessHorizontalSpace = true;
      data.horizontalAlignment = GridData.FILL;
      toolbar.setLayoutData(data);
    }

    // Create the log pane.
    //
    {
      logPane = new TreeLoggerWidget(this);
      GridData data = new GridData();
      data.grabExcessHorizontalSpace = true;
      data.grabExcessVerticalSpace = true;
      data.horizontalAlignment = GridData.FILL;
      data.verticalAlignment = GridData.FILL;
      logPane.setLayoutData(data);
    }

    // check for updates
    if (checkForUpdates) {
      try {
        final CheckForUpdates updateChecker = PlatformSpecific.createUpdateChecker();
        if (updateChecker != null) {
          final CheckForUpdates.UpdateAvailableCallback callback = new CheckForUpdates.UpdateAvailableCallback() {
            public void onUpdateAvailable(final String html) {
              // Do this on the main thread.
              //
              parent.getDisplay().asyncExec(new Runnable() {
                public void run() {
                  new BrowserDialog(parent, getLogger(), html).open(true);
                }

              });
            }
          };

          // Run the update checker on a background thread.
          //
          Thread checkerThread = new Thread() {
            @Override
            public void run() {
              updateChecker.check(callback);
            }
          };

          checkerThread.setDaemon(true);
          checkerThread.start();
        }
      } catch (Throwable e) {
        // Always silently ignore any errors.
      }
    }
  }

  public AbstractTreeLogger getLogger() {
    return logPane.getLogger();
  }

  public void shellActivated(ShellEvent e) {
  }

  public void shellClosed(ShellEvent e) {
    if (serverWindow.hasBrowserWindowsOpen()) {
      boolean closeWindows = true;
      if (System.getProperty("gwt.shell.endquick") == null) {
        closeWindows = DialogBase.confirmAction((Shell) e.widget,
            "Closing the development shell will close "
                + "all hosted mode browsers.  Continue?", "Confirm close");
      }

      if (closeWindows) {
        serverWindow.closeAllBrowserWindows();
        e.doit = true;
      } else {
        e.doit = false;
      }
    }
  }

  public void shellDeactivated(ShellEvent e) {
  }

  public void shellDeiconified(ShellEvent e) {
  }

  public void shellIconified(ShellEvent e) {
  }

  public void widgetDisposed(DisposeEvent e) {
    colorWhite.dispose();
  }
}
