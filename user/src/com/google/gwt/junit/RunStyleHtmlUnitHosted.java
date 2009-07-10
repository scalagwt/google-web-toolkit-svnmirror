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
import com.gargoylesoftware.htmlunit.TopLevelWindow;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.javascript.host.Window;

import net.sourceforge.htmlunit.corejs.javascript.Context;
import net.sourceforge.htmlunit.corejs.javascript.NativeJavaMethod;
import net.sourceforge.htmlunit.corejs.javascript.Scriptable;
import net.sourceforge.htmlunit.corejs.javascript.ScriptableObject;

import java.lang.reflect.Method;

/**
 * Runstyle for HTMLUnit in hosted mode.
 */
public class RunStyleHtmlUnitHosted extends RunStyleHtmlUnit {

  private static class PluginObject implements Scriptable {

    private Scriptable parent;
    private Scriptable prototype;

    public PluginObject(Scriptable parent, Scriptable prototype) {
      this.parent = parent;
      this.prototype = prototype;
    }

    public void delete(String name) {
    }

    public void delete(int index) {
    }

    public Object get(String name, Scriptable start) {
      if (name.equals("connect")) {
        Method connectMethod = null;
        try {
          connectMethod = PluginObject.class.getMethod("connect",
              String.class, String.class, Object.class);
        } catch (SecurityException e) {
          // TODO(jat) Auto-generated catch block
          e.printStackTrace();
        } catch (NoSuchMethodException e) {
          // TODO(jat) Auto-generated catch block
          e.printStackTrace();
        }
        return new NativeJavaMethod(connectMethod, "connect");
      }
      return Context.getUndefinedValue();
    }

    public Object get(int index, Scriptable start) {
      return Context.getUndefinedValue();
    }

    public String getClassName() {
      return "Plugin";
    }

    public Object getDefaultValue(Class<?> hint) {
      // TODO(jat) Auto-generated method stub
      return Context.getUndefinedValue();
    }

    public Object[] getIds() {
      return new Object[] { "connect" };
    }

    public Scriptable getParentScope() {
      return parent;
    }

    public Scriptable getPrototype() {
      return prototype;
    }

    public boolean has(String name, Scriptable start) {
      return name.equals("connect");
    }

    public boolean has(int index, Scriptable start) {
      return false;
    }

    public boolean hasInstance(Scriptable instance) {
      return false;
    }

    public void put(String name, Scriptable start, Object value) {
    }

    public void put(int index, Scriptable start, Object value) {
    }

    public void setParentScope(Scriptable parent) {
      this.parent = parent;
    }

    public void setPrototype(Scriptable prototype) {
      this.prototype = prototype;
    }
  }

  protected class HtmlUnitHostedThread extends HtmlUnitThread {

    public HtmlUnitHostedThread(BrowserVersion browser, String url) {
      super(browser, url);
    }

    @Override
    protected void setupWebClient(WebClient webClient) {
      TopLevelWindow topLevelWindow = new TopLevelWindow("", webClient);
      Window scriptObject = (Window) topLevelWindow.getScriptObject();
      Scriptable objectPrototype = ScriptableObject.getObjectPrototype(
          scriptObject);
      Scriptable pluginObject = new PluginObject(scriptObject,
          objectPrototype);
      scriptObject.defineProperty("__gwt_HostedModePlugin", pluginObject,
          ScriptableObject.READONLY);
      webClient.setCurrentWindow(topLevelWindow);
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
}
