/*
 * Copyright 2006 Google Inc.
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
package com.google.gwt.dev.shell.mac;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.dev.shell.BrowserWidget;
import com.google.gwt.dev.shell.BrowserWidgetHost;
import com.google.gwt.dev.shell.ModuleSpace;
import com.google.gwt.dev.shell.ModuleSpaceHost;
import com.google.gwt.dev.shell.profiler.Timer;
import com.google.gwt.dev.shell.mac.LowLevelSaf.DispatchMethod;
import com.google.gwt.dev.shell.mac.LowLevelSaf.DispatchObject;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.WebKit;
import org.eclipse.swt.widgets.Shell;

import java.util.Map;
import java.util.HashMap;

/**
 * Represents an individual browser window and all of its controls.
 */
public class BrowserWidgetSaf extends BrowserWidget {

  /**
   * The profiler API used by the compiler-instrumented JavaScript to record
   * profiling information. We make it available on window.external.
   */
  private LowLevelSaf.DispatchObject profiler = new LowLevelSaf.DispatchObject() {

    private LowLevelSaf.DispatchMethod timingBegin = new LowLevelSaf.DispatchMethod() {
      public int invoke(int execState, int jsthis, int[] jsargs) {
        int jsFalse = LowLevelSaf.convertBoolean(false);
        LowLevelSaf.pushExecState(execState);
        try {
          getProfiler().timingBegin();
        } catch (Exception e) {
          e.printStackTrace();
          return jsFalse;
        }
        finally {
          LowLevelSaf.popExecState(execState);
        }
        return jsFalse;
      }
    };

    private LowLevelSaf.DispatchMethod timingCall = new LowLevelSaf.DispatchMethod() {
      public int invoke(int execState, int jsthis, int[] jsargs) {
        int jsFalse = LowLevelSaf.convertBoolean(false);
        LowLevelSaf.pushExecState(execState);
        try {
          getProfiler().timingCall();
        } catch (Exception e) {
          e.printStackTrace();
          return jsFalse;
        }
        finally {
          LowLevelSaf.popExecState(execState);
        }
        return jsFalse;
      }
    };

    private LowLevelSaf.DispatchMethod timingEnd = new LowLevelSaf.DispatchMethod() {
      public int invoke(int execState, int jsthis, int[] jsargs) {
        int jsFalse = LowLevelSaf.convertBoolean(false);
        LowLevelSaf.pushExecState(execState);
        try {
          getProfiler().timingEnd();
        } catch (Exception e) {
          e.printStackTrace();
          return jsFalse;
        }
        finally {
          LowLevelSaf.popExecState(execState);
        }
        return jsFalse;
      }
    };

    private LowLevelSaf.DispatchMethod onAppLoad = new LowLevelSaf.DispatchMethod() {
      public int invoke(int execState, int jsthis, int[] jsargs) {
        int jsFalse = LowLevelSaf.convertBoolean(false);
        LowLevelSaf.pushExecState(execState);
        try {
          getProfiler().onAppLoad();
        } catch (Exception e) {
          e.printStackTrace();
          return jsFalse;
        }
        finally {
          LowLevelSaf.popExecState(execState);
        }
        return jsFalse;
      }
    };

    private LowLevelSaf.DispatchMethod moduleLoadBegin = new LowLevelSaf.DispatchMethod() {
      public int invoke(int execState, int jsthis, int[] jsargs) {
        int jsFalse = LowLevelSaf.convertBoolean(false);
        LowLevelSaf.pushExecState(execState);
        try {
          String name = LowLevelSaf.coerceToString(execState, jsargs[0]);
          getProfiler().moduleLoadBegin(name);
        } catch (Exception e) {
          e.printStackTrace();
          return jsFalse;
        }
        finally {
          LowLevelSaf.popExecState(execState);
        }
        return jsFalse;
      }
    };

    private LowLevelSaf.DispatchMethod moduleLoadEnd = new LowLevelSaf.DispatchMethod() {
      public int invoke(int execState, int jsthis, int[] jsargs) {
        int jsFalse = LowLevelSaf.convertBoolean(false);
        LowLevelSaf.pushExecState(execState);
        try {
          String name = LowLevelSaf.coerceToString(execState, jsargs[0]);
          getProfiler().moduleLoadEnd(name);
        } catch (Exception e) {
          e.printStackTrace();
          return jsFalse;
        }
        finally {
          LowLevelSaf.popExecState(execState);
        }
        return jsFalse;
      }
    };

    private LowLevelSaf.DispatchMethod enteredMethod = new LowLevelSaf.DispatchMethod() {
      public int invoke(int execState, int jsthis, int[] jsargs) {
        int jsFalse = LowLevelSaf.convertBoolean(false);
        LowLevelSaf.pushExecState(execState);
        try {
          String klass = LowLevelSaf.coerceToString(execState, jsargs[0]);
          String name = LowLevelSaf.coerceToString(execState, jsargs[1]);
          String signature = LowLevelSaf.coerceToString(execState, jsargs[2]);
          getProfiler().methodEntered(klass, name, signature);
        } catch (Exception e) {
          e.printStackTrace();
          return jsFalse;
        }
        finally {
          LowLevelSaf.popExecState(execState);
        }
        return jsFalse;
      }
    };

    private LowLevelSaf.DispatchMethod exitedMethod = new LowLevelSaf.DispatchMethod() {
      public int invoke(int execState, int jsthis, int[] jsargs) {
        int jsFalse = LowLevelSaf.convertBoolean(false);
        LowLevelSaf.pushExecState(execState);
        try {
          String klass = LowLevelSaf.coerceToString(execState, jsargs[0]);
          String name = LowLevelSaf.coerceToString(execState, jsargs[1]);
          String signature = LowLevelSaf.coerceToString(execState, jsargs[2]);
          getProfiler().methodExited(klass, name, signature);
        } catch (Exception e) {
          e.printStackTrace();
          return jsFalse;
        }
        finally {
          LowLevelSaf.popExecState(execState);
        }
        return jsFalse;
      }
    };

    class MethodCache {
      LowLevelSaf.DispatchMethod method;

      int methodId = -1;

      MethodCache(LowLevelSaf.DispatchMethod method) {
        this.method = method;
      }
    }

    private Map methods = new HashMap();

    {
      methods.put("timingBegin", new MethodCache(timingBegin));
      methods.put("timingCall", new MethodCache(timingCall));
      methods.put("timingEnd", new MethodCache(timingEnd));
      methods.put("onAppLoad", new MethodCache(onAppLoad));
      methods.put("moduleLoadBegin", new MethodCache(moduleLoadBegin));
      methods.put("moduleLoadEnd", new MethodCache(moduleLoadEnd));
      methods.put("methodEntered", new MethodCache(enteredMethod));
      methods.put("methodExited", new MethodCache(exitedMethod));
    }


    public int getField(String name) {
      int jsFalse = LowLevelSaf.convertBoolean(false);

      // Can happen if profiling calls somehow get invoked from hosted mode
      // instead of profiling mode. This shouldn't really happen.
      if (getProfiler() == null) {
        getLogger().log(TreeLogger.WARN, "Ignoring a profiling call made in hosted mode.", null);
        return jsFalse;
      }

      MethodCache cache = (MethodCache) methods.get(name);

      if (cache == null) {
        return jsFalse;
      }

      // if ( cache.methodId == -1 ) {
      cache.methodId = LowLevelSaf.wrapFunction(name, cache.method);
      // }

      return cache.methodId;
    }

    public Object getTarget() {
      return null;
    }

    public void setField(String name, int value) {
    }
  };

  private class ExternalObject implements DispatchObject {

    public int getField(String name) {
      if ("gwtonload".equalsIgnoreCase(name)) {
        return LowLevelSaf.wrapFunction("gwtOnload", new GwtOnLoad());
      }
      if ("profiler".equals(name)) {
        getProfiler().setProfileCallBeginNanos(Timer.nanoTime());
        return LowLevelSaf.wrapDispatch(profiler);
      }
      return 0;
    }

    public Object getTarget() {
      return null;
    }

    public boolean gwtOnLoad(int scriptObject, String moduleName) {
      try {
        if (moduleName == null) {
          // Indicates the page is being unloaded.
          //
          onPageUnload();
          return true;
        }

        // Attach a new ModuleSpace to make it programmable.
        //
        ModuleSpaceHost msh = getHost().createModuleSpaceHost(
            BrowserWidgetSaf.this, moduleName);
        ModuleSpace moduleSpace = new ModuleSpaceSaf(msh, scriptObject);
        attachModuleSpace(moduleName, moduleSpace);
        return true;
      } catch (Throwable e) {
        // We do catch Throwable intentionally because there are a ton of things
        // that can go wrong trying to load a module, including Error-dervied
        // things like NoClassDefFoundError.
        //
        getHost().getLogger().log(TreeLogger.ERROR,
            "Failure to load module '" + moduleName + "'", e);
        return false;
      }
    }

    public void setField(String name, int value) {
    }
  }

  private static final class GwtOnLoad implements DispatchMethod {

    public int invoke(int execState, int jsthis, int[] jsargs) {
      int jsFalse = LowLevelSaf.convertBoolean(false);
      LowLevelSaf.pushExecState(execState);
      try {
        if (!LowLevelSaf.isWrappedDispatch(jsthis)) {
          return jsFalse;
        }

        Object thisObj = LowLevelSaf.unwrapDispatch(jsthis);
        if (!(thisObj instanceof ExternalObject)) {
          return jsFalse;
        }

        if (jsargs.length < 2) {
          return jsFalse;
        }

        if (!LowLevelSaf.isObject(jsargs[0])) {
          return jsFalse;
        }
        if (!LowLevelSaf.isString(jsargs[1])) {
          return jsFalse;
        }
        String moduleName = LowLevelSaf.coerceToString(execState, jsargs[1]);

        boolean result = ((ExternalObject) thisObj).gwtOnLoad(jsargs[0],
            moduleName);
        return LowLevelSaf.convertBoolean(result);
      } catch (Throwable e) {
        return jsFalse;
      } finally {
        LowLevelSaf.popExecState(execState);
      }
    }
  }

  private static final int REDRAW_PERIOD = 250;

  static {
    LowLevelSaf.init();
  }

  public BrowserWidgetSaf(Shell shell, BrowserWidgetHost host) {
    super(shell, host);

    Browser.setWebInspectorEnabled(true);
    browser.setUserAgentApplicationName("Safari 419.3");
    browser.addWindowScriptObjectListener(new Browser.WindowScriptObjectListener() {

      public void windowScriptObjectAvailable(int windowScriptObject) {
        int sel = WebKit.sel_registerName("_imp");
        int windowObject = WebKit.objc_msgSend(windowScriptObject, sel);
        try {
          LowLevelSaf.jsLock();
          final int globalExec = LowLevelSaf.getGlobalExecState(windowObject);
          int external = LowLevelSaf.wrapDispatch(new ExternalObject());
          LowLevelSaf.executeScript(globalExec,
              "function __defineExternal(x) {" + "  window.external = x;" + "}");
          LowLevelSaf.invoke(globalExec, windowObject, "__defineExternal",
              windowObject, new int[]{external});
        } finally {
          LowLevelSaf.jsUnlock();
        }
      }

    });

    /*
    * HACK (knorton) - SWT wrapper on WebKit seems to cause unreliable repaints
    * when the DOM changes inside of WebView. To compensate for this, every
    * quarter second, we tell WebView to repaint itself fully.
    */
    getDisplay().timerExec(REDRAW_PERIOD, new Runnable() {
      public void run() {
        if (browser.isDisposed() || isDisposed()) {
          // stop running if we're disposed
          return;
        }
        // Force the browser to refresh
        browser.setNeedsDisplay(true);
        // Reschedule this object to run again
        getDisplay().timerExec(REDRAW_PERIOD, this);
      }
    });
  }

}
