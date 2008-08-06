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
package com.google.gwt.dev.util.log;

import com.google.gwt.core.ext.TreeLogger;

import javax.swing.JFrame;

/**
 * Useful for debugging, this class manages to standalone window
 * and provides access to a logger you can use to write to it.
 * 
 */
public class DetachedTreeLoggerWindow implements Runnable {
  private static DetachedTreeLoggerWindow singleton;

  /**
   * Provides a reference to a singleton <code>DetachedTreeLoggerWindow</code>.
   * 
   * @param caption the text to appear in the windows title bar.
   * @param width the widget of the window
   * @param height the height of the window
   * @param autoScroll whether or not the window should autoscroll as output is
   *          produced
   * @return a proxy object providing limited control of the window.
   */
  public static synchronized DetachedTreeLoggerWindow getInstance(
      final String caption, final int width, final int height,
      final boolean autoScroll) {
    if (singleton == null) {
      singleton = new DetachedTreeLoggerWindow(caption, width, height,
          autoScroll);
    }
    return singleton;
  }

  private final JFrame frame;
  private final AbstractTreeLogger logger;
  private boolean isRunning = false;

  private DetachedTreeLoggerWindow(final String caption, final int width,
      final int height, final boolean autoScroll) {
    frame = new JFrame(caption);
    final SwingLoggerPanel treeLoggerWidget = new SwingLoggerPanel(TreeLogger.INFO);
    logger = treeLoggerWidget.getLogger();
    treeLoggerWidget.setAutoScroll(autoScroll);
    frame.getContentPane().add(treeLoggerWidget);
    frame.setSize(950, 700);

//    frame.setImage(LowLevel.loadImage("gwt.ico"));
    frame.setSize(width, height);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setVisible(true);
  }

  public AbstractTreeLogger getLogger() {
    return logger;
  }

  public synchronized boolean isRunning() {
    return isRunning;
  }
   
  public void run() {
    if (!maybeStart()) {
      throw new IllegalStateException(
          "DetachedTreeLogger window is already running.");
    }
  }

  private synchronized boolean maybeStart() {
    if (isRunning) {
      return false;
    }
    isRunning = true;
    return true;
  }
}
