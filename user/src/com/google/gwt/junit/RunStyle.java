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
package com.google.gwt.junit;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;

/**
 * An abstract class that handles the details of launching a browser.
 */
abstract class RunStyle {

  /**
   * The containing shell.
   */
  protected final JUnitShell shell;

  /**
   * @param shell the containing shell
   */
  public RunStyle(JUnitShell shell) {
    this.shell = shell;
  }

  /**
   * Requests initial launch of the browser. This should only be called once per
   * instance of RunStyle.
   * 
   * @param moduleName the module to run
   * @throws UnableToCompleteException
   */
  public abstract void launchModule(String moduleName)
      throws UnableToCompleteException;

  /**
   * Possibly causes a compilation on the specified module.
   * 
   * @param moduleName the module to compile
   * @throws UnableToCompleteException
   */
  public abstract void maybeCompileModule(String moduleName)
      throws UnableToCompleteException;

  /**
   * Set whether or not hosted-mode should be used instead of a web-mode
   * compile.
   */
  public abstract void setHostedMode(boolean useHostedMode);

  /**
   * Tests whether the test was interrupted.
   * 
   * @return <code>true</code> if the test has been interrupted.
   */
  public boolean wasInterrupted() {
    return false;
  }

  /**
   * Gets the shell logger.
   */
  protected TreeLogger getLogger() {
    return shell.getTopLogger();
  }

  /**
   * Gets the suffix of the URL to load.
   * 
   * @param moduleName the module to run
   * @return a URL suffix that should be loaded
   */
  protected String getUrlSuffix(String moduleName) {
    return moduleName + "/junit.html";
  }
}
