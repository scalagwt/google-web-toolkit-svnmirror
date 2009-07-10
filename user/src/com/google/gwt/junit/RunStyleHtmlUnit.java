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

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;

import com.gargoylesoftware.htmlunit.AlertHandler;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.IncorrectnessListener;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.OnbeforeunloadHandler;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Launches a web-mode test via HTMLUnit.
 */
public class RunStyleHtmlUnit extends RunStyleRemote {

  protected class HtmlUnitThread extends Thread implements AlertHandler,
      IncorrectnessListener, OnbeforeunloadHandler {

    private BrowserVersion browser;
    private String url;
    private Object waitForUnload = new Object();

    public HtmlUnitThread(BrowserVersion browser, String url) {
      this.browser = browser;
      this.url = url;
      start();
    }

    public void handleAlert(Page page, String message) {
      shell.getTopLogger().log(TreeLogger.ERROR, "Alert: " + message);
    }

    public boolean handleEvent(Page page, String returnValue) {
      synchronized (waitForUnload) {
        waitForUnload.notifyAll();
      }
      return true;
    }

    public void notify(String message, Object origin) {
      shell.getTopLogger().log(TreeLogger.WARN, message);
    }

    @Override
    public void run() {
      WebClient webClient = new WebClient(browser);
      webClient.setAlertHandler(this);
      webClient.setIncorrectnessListener(this);
      webClient.setThrowExceptionOnFailingStatusCode(false);
      webClient.setThrowExceptionOnScriptError(true);
      webClient.setOnbeforeunloadHandler(this);
      webClient.setAjaxController(new NicelyResynchronizingAjaxController());
      setupWebClient(webClient);
      try {
        Page page = webClient.getPage(url);
        // TODO(jat): is this necessary?
        webClient.waitForBackgroundJavaScriptStartingBefore(2000);
        page.getEnclosingWindow().getJobManager().waitForJobs(60000);
        shell.getTopLogger().log(TreeLogger.INFO, "getPage returned "
            + ((HtmlPage) page).asXml());
      } catch (FailingHttpStatusCodeException e) {
        shell.getTopLogger().log(TreeLogger.ERROR,
            "HTTP request failed", e);
        return;
      } catch (MalformedURLException e) {
        shell.getTopLogger().log(TreeLogger.ERROR,
            "Bad URL", e);
        return;
      } catch (IOException e) {
        shell.getTopLogger().log(TreeLogger.ERROR,
            "I/O error on HTTP request", e);
        return;
      }
//      synchronized (waitForUnload) {
//        try {
//          waitForUnload.wait();
//        } catch (InterruptedException e) {
//          shell.getTopLogger().log(TreeLogger.ERROR, "Interrupted wait", e);
//        }
//      }
    }

    /**
     * Additional setup of the WebClient before starting test.
     * 
     * @param webClient
     */
    protected void setupWebClient(WebClient webClient) {
    }
  }

  /**
   * Create a RunStyleHtmlUnit instance with a list of browsers
   * 
   * @param shell
   * @param targetsIn
   * @return RunStyle instance
   */
  public static RunStyle create(JUnitShell shell, String[] targetsIn) {
    BrowserVersion[] browsers = new BrowserVersion[targetsIn.length];
    for (int i = 0; i < targetsIn.length; ++i) {
      String browserName = targetsIn[i];
      BrowserVersion browser = BrowserVersion.FIREFOX_2;
      // TODO(jat): find the browser name in BrowserVersion
      browsers[i] = browser;
    }
    RunStyleHtmlUnit runStyle = new RunStyleHtmlUnit(shell, browsers);
    return runStyle;
  }

  private BrowserVersion[] browsers;
  private List<Thread> threads = new ArrayList<Thread>();

  protected RunStyleHtmlUnit(JUnitShell shell, BrowserVersion[] browsers) {
    super(shell);
    this.browsers = browsers;
  }

  @Override
  public void launchModule(String moduleName) throws UnableToCompleteException {
    for (BrowserVersion browser : browsers) {
      String url = getMyUrl(moduleName);
      shell.getTopLogger().log(TreeLogger.INFO, "Starting " + url
          + " on browser " + browser);
      threads.add(new HtmlUnitThread(browser, url));
    }
  }

  @Override
  public void maybeCompileModule(String moduleName)
      throws UnableToCompleteException {
    // TODO(jat): substitute appropriate user agent
    shell.compileForWebMode(moduleName, "gecko1_8");
  }
}
