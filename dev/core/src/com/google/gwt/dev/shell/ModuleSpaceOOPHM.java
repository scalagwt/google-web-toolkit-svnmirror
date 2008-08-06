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
package com.google.gwt.dev.shell;

import com.google.gwt.core.ext.TreeLogger;

import java.util.Set;

/**
 */
public class ModuleSpaceOOPHM extends ModuleSpace {

  private BrowserChannelServer channel;

  public ModuleSpaceOOPHM(ModuleSpaceHost msh, String moduleName,
      BrowserChannelServer channel) {
    super(msh, moduleName, moduleName);
    this.channel = channel;
    msh.getLogger().log(TreeLogger.INFO,
        "Created ModuleSpaceOOPHM for " + moduleName, null);
  }

  public void createNativeMethods(String sourceName, String js) {
    channel.loadJsni(js);
  }

  @Override
  protected void cleanupJsValues() {
    Set<Integer> refIdsForCleanup = channel.getRefIdsForCleanup();
    if (refIdsForCleanup.isEmpty()) {
      // nothing to do
      return;
    }
    int[] ids = new int[refIdsForCleanup.size()];
    int i = 0;
    for (Integer id : refIdsForCleanup) {
      ids[i++] = id;
    }
    channel.freeJsValue(ids);
  }

  /**
   * Invoke a JS method and return its value.
   * 
   * @param name method name to invoke
   * @param jthis object to invoke method on, null if static method
   * @param types argument types
   * @param args argument values
   */
  @Override
  protected JsValue doInvoke(String name, Object jthis, Class<?>[] types,
      Object[] args) throws Throwable {
    TreeLogger branch = host.getLogger().branch(TreeLogger.TRACE,
        "Invoke native method " + name, null);
    CompilingClassLoader isolatedClassLoader = getIsolatedClassLoader();
    JsValueOOPHM jsthis = new JsValueOOPHM();
    Class<?> jthisType = (jthis == null) ? Object.class : jthis.getClass();
    JsValueGlue.set(jsthis, isolatedClassLoader, jthisType, jthis);
    branch.log(TreeLogger.SPAM, "  this=" + jsthis);

    int argc = args.length;
    JsValueOOPHM argv[] = new JsValueOOPHM[argc];
    for (int i = 0; i < argc; ++i) {
      argv[i] = new JsValueOOPHM();
      JsValueGlue.set(argv[i], isolatedClassLoader, types[i], args[i]);
      branch.log(TreeLogger.SPAM, "  arg[" + i + "]=" + argv[i]);
    }
    JsValueOOPHM returnVal = new JsValueOOPHM();
    try {
      channel.invokeJavascript(isolatedClassLoader, jsthis, name, argv,
          returnVal);
      branch.log(TreeLogger.SPAM, "  returned " + returnVal);
    } catch (Throwable t) {
      branch.log(TreeLogger.TRACE, "exception thrown", t);
      throw t;
    }
    return returnVal;
  }

  @Override
  protected Object getStaticDispatcher() {
    return new JsValueOOPHM.DispatchObjectOOPHM(getIsolatedClassLoader());
  }
}
