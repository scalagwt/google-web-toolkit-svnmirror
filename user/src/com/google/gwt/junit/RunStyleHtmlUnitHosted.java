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

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebWindow;
import com.gargoylesoftware.htmlunit.javascript.JavaScriptEngine;
import com.gargoylesoftware.htmlunit.javascript.host.Window;

import net.sourceforge.htmlunit.corejs.javascript.ScriptableObject;

/**
 * Runstyle for HTMLUnit in hosted mode.
 */
public class RunStyleHtmlUnitHosted extends RunStyleHtmlUnit {

  /**
   * JavaScriptEngine subclass that provides a hook of initializing the
   * __gwt_HostedModePlugin property on any new window, so it acts just like
   * Firefox with the XPCOM plugin installed.
   */
  private static class HostedJavaScriptEngine extends JavaScriptEngine {

    private static final long serialVersionUID = 3594816610842448691L;

    public HostedJavaScriptEngine(WebClient webClient) {
      super(webClient);
    }

    @Override
    public void initialize(WebWindow webWindow) {
      // Hook in the hosted-mode plugin after initializing the JS engine.
      super.initialize(webWindow);
      Window window = (Window) webWindow.getScriptObject();
      window.defineProperty("__gwt_HostedModePlugin",
          new HostedModePluginObject(), ScriptableObject.READONLY);
    }
  }

  /**
   * Run HMTLUnit in a separate thread, replacing the default JavaScriptEngine
   * with one that has the necessary hosted mode hooks.
   */
  protected class HtmlUnitHostedThread extends HtmlUnitThread {

    public HtmlUnitHostedThread(BrowserVersion browser, String url) {
      super(browser, url);
    }

    @Override
    protected void setupWebClient(WebClient webClient) {
      JavaScriptEngine hostedEngine = new HostedJavaScriptEngine(webClient);
      webClient.setJavaScriptEngine(hostedEngine);
    }
  }

  public RunStyleHtmlUnitHosted(JUnitShell unitShell, String[] targets) {
    super(unitShell, targets);
  }
  
  @Override
  public void maybeCompileModule(String moduleName) {
    // No compilation needed for hosted mode
  }
  
  @Override
  protected HtmlUnitThread createHtmlUnitThread(BrowserVersion browser,
      String url) {
    return new HtmlUnitHostedThread(browser, url);
  }

  @Override
  protected String getMyUrl(String moduleName) {
    // TODO(jat): get the correct address/port
    return super.getMyUrl(moduleName) + "?gwt.hosted=localhost:9997";
  }
}
