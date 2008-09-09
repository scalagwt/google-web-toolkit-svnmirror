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

import com.google.gwt.core.ext.UnableToCompleteException;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Runs tests in an external browser. This is the default and only case in
 * OOPHM.
 */
abstract class RunStyleRemote extends RunStyle {

  private boolean useHostedMode;

  public RunStyleRemote(JUnitShell shell) {
    super(shell);
  }

  /**
   * Compiles the named module if hosted-mode tests are not enabled.
   */
  @Override
  public void maybeCompileModule(String moduleName)
      throws UnableToCompleteException {
    if (!useHostedMode) {
      shell.compileForWebMode(moduleName, null);
    }
  }

  @Override
  public void setHostedMode(boolean useHostedMode) {
    this.useHostedMode = useHostedMode;
  }

  protected String getMyUrl(String moduleName) {
    try {
      String localhost = InetAddress.getLocalHost().getHostAddress();
      return "http://" + localhost + ":" + shell.getPort() + "/"
          + getUrlSuffix(moduleName)
          + (useHostedMode ? shell.getHostedUrlSuffix() : "");
    } catch (UnknownHostException e) {
      throw new RuntimeException("Unable to determine my ip address", e);
    }
  }
}
