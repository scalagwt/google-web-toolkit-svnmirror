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
package com.google.gwt.dev.shell.ie;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.dev.shell.BrowserWidget;
import com.google.gwt.dev.shell.BrowserWidgetHost;
import com.google.gwt.dev.shell.ModuleSpaceHost;
import com.google.gwt.dev.shell.CompilingClassLoader;
import com.google.gwt.dev.shell.profiler.ProfilerImpl;

import org.eclipse.swt.internal.ole.win32.COM;
import org.eclipse.swt.internal.ole.win32.IDispatch;
import org.eclipse.swt.ole.win32.Variant;
import org.eclipse.swt.widgets.Shell;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.HashMap;

/**
 * Represents an individual browser window and all of its controls.
 */
public class BrowserWidgetIE6 extends BrowserWidget {

  CompilingClassLoader loader;

  /**
   * Implements dispatch for the profiling subsystem.
   *
   */
  public class ProfilerDispatch extends IDispatchImpl {

    private int methodId = 2;

    private MethodInfo[] methods = new MethodInfo[]{
        new MethodInfo(methodId++, "timingBegin"),
        new MethodInfo(methodId++, "timingCall"),
        new MethodInfo(methodId++, "timingEnd"),
        new MethodInfo(methodId++, "onAppLoad"),
        new MethodInfo(methodId++, "moduleLoadBegin"),
        new MethodInfo(methodId++, "moduleLoadEnd"),
        new MethodInfo(methodId++, "methodEntered"),
        new MethodInfo(methodId++, "methodExited"),
        new MethodInfo(methodId++, "exceptionCaught"),
        new MethodInfo(methodId++, "exceptionThrown"),
        new MethodInfo(methodId++, "httpRequest"),
        new MethodInfo(methodId++, "httpResponse"),
        new MethodInfo(methodId++, "rpcRequest"),
        new MethodInfo(methodId++, "rpcResponse"),
    };

    class MethodInfo {

      int id;

      Method method;

      public MethodInfo(int id, String name) {
        this.id = id;
        this.method = getMethod(ProfilerImpl.class, name);
      }
    }

    private Map methodsByName = new HashMap();

    {
      for (int i = 0; i < methods.length; ++i) {
        String name = methods[i].method.getName();
        methodsByName.put(name.toLowerCase(), methods[i]);
      }
    }

    /**
     * Finds the method named, <code>name</code>. Does not handle overloading.
     * Only searches the class's local methods, not the inheritance hierarchy.
     *
     * @param klass The class to search
     * @param name  The name of the method
     * @return The method, if it could be found.
     */
    private Method getMethod(Class klass, String name) {
      Method[] localMethods = klass.getDeclaredMethods();

      for (int i = 0; i < localMethods.length; ++i) {
        Method m = localMethods[i];
        if (m.getName().equals(name)) {
          return m;
        }
      }

      throw new RuntimeException(
          "Unable to find method: " + klass + "." + name);
    }

    protected void getIDsOfNames(String[] names, int[] ids)
        throws HResultException {

      // check args... for now we'll just ignore this
      // since profiler calls are only initiated by the compiler
      /*
      if (names.length >= 2) {
        throw new HResultException(DISP_E_UNKNOWNNAME);
      }
      */

      String name = names[0].toLowerCase();
      MethodInfo info = (MethodInfo) methodsByName.get(name);

      if (info == null) {
        throw new HResultException(DISP_E_UNKNOWNNAME);
      }

      ids[0] = info.id;
      // I believe we only have to fill in the other IDs if we were being called
      // through some scripting language that supported named args
    }

    protected Variant invoke(int dispId, int flags, Variant[] params)
        throws HResultException, InvocationTargetException {

      if ((dispId == 0
          || dispId == 1) /* && (flags & COM.DISPATCH_PROPERTYGET) != 0 */) {
        // MAGIC: this is the default property, let's just do toString()
        return new Variant(toString());
      }

      if (dispId >= methods.length + 2) {
        throw new HResultException(COM.DISP_E_MEMBERNOTFOUND);
      }

      final MethodInfo info = methods[dispId - 2];

      if ((flags & COM.DISPATCH_METHOD) != 0) {
        return callMethod(loader, getProfiler(), params, info.method);
      } else if ((flags & COM.DISPATCH_PROPERTYGET) != 0) {
        // property get on the method itself
        try {
          IDispatchImpl funcObj = new IDispatchImpl() {
            ProfilerImpl profiler = getProfiler();

            protected void getIDsOfNames(String[] names, int[] ids)
                throws HResultException {
              throw new HResultException(COM.E_NOTSUPPORTED);
            }

            protected Variant invoke(int id, int flags, Variant[] params)
                throws HResultException, InvocationTargetException {
              switch (id) {
                case 0:
                  if ((flags & COM.DISPATCH_METHOD) != 0) {
                    // implicit call -- "m()"
                    return callMethod(loader, profiler, params, info.method);
                  } else if ((flags & COM.DISPATCH_PROPERTYGET) != 0) {
                    // implicit toString -- "'foo' + m"
                    return new Variant(toString());
                  }
                  break;
                case 1:
                  // "m.toString()"
                  if ((flags & (COM.DISPATCH_METHOD | COM.DISPATCH_PROPERTYGET))
                      != 0) {
                    return new Variant(toString());
                  }
                  break;
                case 2:
                  // "m.call(thisObj, arg)"
                  if ((flags & COM.DISPATCH_METHOD) != 0) {
                    /*
                    * First param must be a this object of the correct type (for instance
                    * methods). If method is static, it can be null.
                    */
                    Object jthis = SwtOleGlue.convertVariantToObject(
                        info.method.getDeclaringClass(), params[0], "this");
                    Variant[] otherParams = new Variant[params.length - 1];
                    System.arraycopy(params, 1, otherParams, 0,
                        otherParams.length);
                    return callMethod(loader, jthis, otherParams, info.method);
                  }
                  break;
                default:
                  // The specified member id is out of range.
                  throw new HResultException(COM.DISP_E_MEMBERNOTFOUND);
              }
              throw new HResultException(COM.E_NOTSUPPORTED);
            }
          };
          IDispatch disp = new IDispatch(funcObj.getAddress());
          disp.AddRef();
          return new Variant(disp);
        } catch (Exception e) {
          // just return VT_EMPTY
          return new Variant();
        }
      }

      throw new HResultException(COM.E_NOTSUPPORTED);
    }
  }

  /**
   * IDispatch implementation of the window.external object.
   */
  public class External extends IDispatchImpl {

    /**
     * Called by the loaded HTML page to activate a new module.
     *
     * @param frameWnd a reference to the IFRAME in which the module's injected
     *                 JavaScript will live
     */
    public boolean gwtOnLoad(IDispatch frameWnd, String moduleName) {
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
            BrowserWidgetIE6.this, moduleName);
        ModuleSpaceIE6 moduleSpace = new ModuleSpaceIE6(msh, frameWnd);
        attachModuleSpace(moduleName, moduleSpace);
        loader = msh.getClassLoader();
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

    protected void getIDsOfNames(String[] names, int[] ids)
        throws HResultException {

      if (names.length >= 2) {
        throw new HResultException(DISP_E_UNKNOWNNAME);
      }

      String name = names[0].toLowerCase();
      if (name.equals("gwtonload")) {
        ids[0] = 1;
        return;
      }
      if (name.equals("profiler")) {
        ids[0] = 2;
        return;
      }

      throw new HResultException(DISP_E_UNKNOWNNAME);
    }

    protected Variant invoke(int dispId, int flags, Variant[] params)
        throws HResultException, InvocationTargetException {

      if (dispId == 0 && (flags & COM.DISPATCH_PROPERTYGET) != 0) {
        // MAGIC: this is the default property, let's just do toString()
        return new Variant(toString());
      } else if (dispId == 1) {
        if ((flags & COM.DISPATCH_METHOD) != 0) {
          // Invoke
          Object[] javaParams = SwtOleGlue.convertVariantsToObjects(
              new Class[]{
                  IDispatch.class, String.class, String.class, String.class},
              params, "Calling method 'gwtOnLoad'");

          IDispatch frameWnd = (IDispatch) javaParams[0];
          String moduleName = (String) javaParams[1];
          boolean success = gwtOnLoad(frameWnd, moduleName);

          // boolean return type
          return new Variant(success);
        } else if ((flags & COM.DISPATCH_PROPERTYGET) != 0) {
          // property get on the method itself
          try {
            Method gwtOnLoadMethod = getClass().getMethod("gwtOnLoad",
                new Class[]{IDispatch.class, String.class});
            IDispatchImpl funcObj = new MethodDispatch(null, gwtOnLoadMethod);
            IDispatch disp = new IDispatch(funcObj.getAddress());
            disp.AddRef();
            return new Variant(disp);
          } catch (Exception e) {
            // just return VT_EMPTY
            return new Variant();
          }
        }
        throw new HResultException(COM.E_NOTSUPPORTED);
      } else if (dispId == 2) {
        IDispatchImpl impl = new ProfilerDispatch();
        IDispatch disp = new IDispatch(impl.getAddress());
        disp.AddRef();
        return new Variant(disp);
      }

      // The specified member id is out of range.
      throw new HResultException(COM.DISP_E_MEMBERNOTFOUND);
    }
  }

  public BrowserWidgetIE6(Shell shell, BrowserWidgetHost host) {
    super(shell, host);

    // Expose a 'window.external' object. This object's onLoad() method will
    // be called when a hosted mode application's wrapper HTML is done loading.
    //
    SwtOleGlue.injectBrowserScriptExternalObject(browser, new External());

    // Make sure that the LowLevelIE6 magic is properly initialized.
    //
    LowLevelIE6.init();
  }
}
