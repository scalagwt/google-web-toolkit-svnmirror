/*
 * Copyright 2009 Google Inc.
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

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;


/**
 * Runstyle for HTMLUnit in hosted mode.
 */
public class RunStyleHtmlUnitHosted extends RunStyleHtmlUnit {

  protected class HtmlUnitHostedThread extends HtmlUnitThread {

    public HtmlUnitHostedThread(BrowserVersion browser, String url) {
      super(browser, url);
    }

    @Override
    protected void setupWebClient(WebClient webClient) {
      HostedModePluginObject.setInjectHostedMode(true);
    }
  }

  protected RunStyleHtmlUnitHosted(JUnitShell shell,
      BrowserVersion[] browsers) {
    super(shell, browsers);
  }

  @Override
  public void maybeCompileModule(String moduleName)
      throws UnableToCompleteException {
    // No compilation needed for hosted mode
  }

  @Override
  protected HtmlUnitThread createHtmlUnitThread(BrowserVersion browser,
      String url) {
    return new HtmlUnitHostedThread(browser, url);
  }

  @Override
  protected String getMyUrl(String moduleName) {
    return super.getMyUrl(moduleName) + "?gwt.hosted=localhost:9997";
  }
}
