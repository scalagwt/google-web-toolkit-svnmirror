/**
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
import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.dev.util.log.AbstractTreeLogger;
import com.google.gwt.dev.util.log.SwingLoggerPanel;
import com.google.gwt.dev.util.log.SwingLoggerPanel.CloseListener;

import java.awt.BorderLayout;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 */
public class ModulePanel extends JPanel {

  private static ImageIcon firefoxIcon = GWTShell.loadImageIcon("firefox24.png");

  private static ImageIcon ieIcon = GWTShell.loadImageIcon("ie24.png");

  private static ImageIcon safariIcon = GWTShell.loadImageIcon("safari24.png");

  private SwingLoggerPanel loggerPanel;

  private final JTabbedPane tabs;

  public ModulePanel(Type maxLevel, String moduleName, String userAgent,
      String remoteSocket, final JTabbedPane tabs) {
    super(new BorderLayout());
    this.tabs = tabs;
    JPanel topPanel = new JPanel();
    topPanel.add(new JLabel(moduleName));
    JButton compileButton = new JButton("Compile");
    topPanel.add(compileButton);
    add(topPanel, BorderLayout.NORTH);
    loggerPanel = new SwingLoggerPanel(maxLevel, new CloseListener() {
      public void onClose() {
        synchronized (tabs) {
          tabs.remove(ModulePanel.this);
        }
      }
    });
    add(loggerPanel);
    AbstractTreeLogger logger = loggerPanel.getLogger();
    ImageIcon browserIcon = null;
    String lcAgent = userAgent.toLowerCase();
    if (lcAgent.contains("msie")) {
      browserIcon = ieIcon;
    } else if (lcAgent.contains("webkit") || lcAgent.contains("safari")) {
      browserIcon = safariIcon;
    } else if (lcAgent.contains("firefox")) {
      browserIcon = firefoxIcon;
    }
    String shortModuleName = moduleName;
    int lastDot = shortModuleName.lastIndexOf('.');
    if (lastDot >= 0) {
      shortModuleName = shortModuleName.substring(lastDot + 1);
    }
    synchronized (tabs) {
      tabs.addTab(shortModuleName, browserIcon, this, moduleName + " from "
          + remoteSocket);
    }
    logger.log(TreeLogger.INFO, "Request for module " + moduleName
        + " by user agent '" + userAgent + "' from " + remoteSocket);
  }

  public void disconnect() {
    synchronized (tabs) {
      int index = tabs.indexOfComponent(this);
      if (index > -1) {
        tabs.setTitleAt(index, "Disconnected");
        // TODO(jat): closed icon?
        tabs.setIconAt(index, null);
      }
    }
    loggerPanel.disconnected();
  }

  public AbstractTreeLogger getLogger() {
    return loggerPanel.getLogger();
  }
}
