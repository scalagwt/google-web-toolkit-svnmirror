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
package com.google.gwt.dev.shell.ie;

import com.google.gwt.dev.shell.JsValue;
import com.google.gwt.dev.shell.ModuleSpace;
import com.google.gwt.dev.shell.ModuleSpaceHost;
import com.google.gwt.dev.shell.ie.IDispatchImpl.HResultException;

import org.eclipse.swt.internal.ole.win32.IDispatch;
import org.eclipse.swt.ole.win32.OleAutomation;
import org.eclipse.swt.ole.win32.Variant;

/**
 * An implementation of {@link com.google.gwt.dev.shell.ModuleSpace} for
 * Internet Explorer 6.
 */
public class ModuleSpaceIE6 extends ModuleSpace {
  /**
   * Invoke a JavaScript function. The static function exists to allow
   * platform-dependent code to make JavaScript calls without having a
   * ModuleSpaceIE6 (and all that entails) if it is not required.
   * 
   * @param window the window containing the function
   * @param name the name of the function
   * @param vArgs the array of arguments. vArgs[0] is the this parameter
   *          supplied to the function, which must be null if it is static.
   * @return the return value of the JavaScript function
   */
  protected static Variant doInvokeOnWindow(OleAutomation window, String name,
      Variant[] vArgs) {
    OleAutomation funcObj = null;
    Variant funcObjVar = null;
    try {

      // Get the function object and its 'call' method.
      //
      int[] ids = window.getIDsOfNames(new String[] {name});
      if (ids == null) {
        throw new RuntimeException(
            "Could not find a native method with the signature '" + name + "'");
      }
      int functionId = ids[0];
      funcObjVar = window.getProperty(functionId);
      funcObj = funcObjVar.getAutomation();
      int callDispId = funcObj.getIDsOfNames(new String[] {"call"})[0];

      // Invoke it and return the result.
      //
      return funcObj.invoke(callDispId, vArgs);

    } finally {
      if (funcObjVar != null) {
        funcObjVar.dispose();
      }

      if (funcObj != null) {
        funcObj.dispose();
      }
    }
  }

  // CHECKSTYLE_OFF
  private static int CODE(int hresult) {
    return hresult & 0xFFFF;
  }
  // CHECKSTYLE_ON

  private final OleAutomation window;

  /**
   * Constructs a browser interface for use with an IE6 'window' automation
   * object.
   * 
   * @param moduleName
   */
  public ModuleSpaceIE6(ModuleSpaceHost host, IDispatch scriptFrameWindow,
      String moduleName, Object key) {
    super(host, moduleName, key);

    window = new OleAutomation(scriptFrameWindow);
  }

  public void createNative(String file, int line, String jsniSignature,
      String[] paramNames, String js) {
    // Execute the function definition within the browser, which will define
    // a new top-level function.
    //
    String newScript = createNativeMethodInjector(jsniSignature, paramNames, js);
    try {
      // TODO: somehow insert file/line info into the script
      Variant result = execute(newScript);
      if (result != null) {
        result.dispose();
      }
    } catch (RuntimeException e) {
      throw new RuntimeException(file + "(" + line
          + "): Failed to create JSNI method with signature '" + jsniSignature
          + "'", e);
    }
  }

  public void dispose() {
    // Dispose everything else.
    if (window != null) {
      window.dispose();
    }
    super.dispose();
  }

  /**
   * Invokes a native javascript function.
   * 
   * @param name the name of the function to invoke
   * @param jthis the function's 'this' context
   * @param types the type of each argument
   * @param args the arguments to be passed
   * @return the return value as a Variant.
   */
  protected JsValue doInvoke(String name, Object jthis, Class[] types,
      Object[] args) throws Throwable {
    Variant[] vArgs = null;
    try {
      // Build the argument list, including 'jthis'.
      //
      int len = args.length;
      vArgs = new Variant[len + 1];
      JsValueIE6 jsValue = new JsValueIE6();
      jsValue.setWrappedJavaObject(getIsolatedClassLoader(), jthis);
      vArgs[0] = jsValue.getVariant();

      for (int i = 0; i < len; ++i) {
        vArgs[i + 1] = SwtOleGlue.convertObjectToVariant(
            getIsolatedClassLoader(), types[i], args[i]);
      }

      Variant result = doInvokeOnWindow(window, name, vArgs);
      try {
        return new JsValueIE6(result);
      } finally {
        if (result != null) {
          result.dispose();
        }
      }
    } finally {
      // We allocated variants for all arguments, so we must dispose them all.
      //
      for (int i = 0; i < vArgs.length; ++i) {
        if (vArgs[i] != null) {
          vArgs[i].dispose();
        }
      }
    }
  }
  
  protected Object getStaticDispatcher() {
    return new IDispatchProxy(getIsolatedClassLoader());
  }

  protected boolean isExceptionSame(Throwable original, int number, String name, String message) {
    HResultException hre = new HResultException(original);
    return CODE(hre.getHResult()) == CODE(number) && hre.getMessage().equals(message);
  }

  private Variant execute(String code) {
    int[] dispIds = window.getIDsOfNames(new String[] {"execScript", "code"});
    Variant[] vArgs = new Variant[1];
    vArgs[0] = new Variant(code);
    int[] namedArgs = new int[1];
    namedArgs[0] = dispIds[1];
    Variant result = window.invoke(dispIds[0], vArgs, namedArgs);
    vArgs[0].dispose();
    if (result == null) {
      String lastError = window.getLastError();
      throw new RuntimeException("Error (" + lastError
          + ") executing JavaScript:\n" + code);
    }
    return result;
  }
}
