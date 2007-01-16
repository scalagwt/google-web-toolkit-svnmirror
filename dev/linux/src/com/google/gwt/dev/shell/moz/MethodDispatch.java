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
package com.google.gwt.dev.shell.moz;

import com.google.gwt.dev.shell.CompilingClassLoader;
import com.google.gwt.dev.shell.moz.LowLevelMoz.DispatchMethod;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Wraps an arbitrary Java Method as a Dispatchable component. The class was
 * motivated by the need to expose Java objects into JavaScript.
 */
class MethodDispatch implements DispatchMethod {

  private final CompilingClassLoader classLoader;

  private final Method method;

  private final int scriptObject;

  public MethodDispatch(CompilingClassLoader classLoader, Method method,
      int scriptObject) {
    this.scriptObject = scriptObject;
    this.classLoader = classLoader;
    this.method = method;
  }

  public int invoke(int jsthis, int[] jsargs) {
    Class[] paramTypes = method.getParameterTypes();
    int argc = paramTypes.length;
    Object args[] = new Object[argc];
    if (jsargs.length < argc) {
      throw new RuntimeException("Not enough arguments to " + method);
    }
    Object jthis = null;
    if ((method.getModifiers() & Modifier.STATIC) == 0) {
      jthis = SwtGeckoGlue.convertJSValToObject(scriptObject,
          method.getDeclaringClass(), jsthis);
    }
    for (int i = 0; i < argc; ++i) {
      args[i] = SwtGeckoGlue.convertJSValToObject(scriptObject, paramTypes[i],
          jsargs[i]);
    }
    try {
      Object result;
      try {
        result = method.invoke(jthis, args);
      } catch (IllegalAccessException e) {
        // should never, ever happen
        e.printStackTrace();
        throw new RuntimeException(e);
      }
      return SwtGeckoGlue.convertObjectToJSVal(scriptObject, classLoader,
          method.getReturnType(), result);
    } catch (InvocationTargetException e) {
      // If we get here, it means an exception is being thrown from
      // Java back into JavaScript
      Throwable t = e.getTargetException();
      RuntimeException re;
      if (t instanceof RuntimeException) {
        re = (RuntimeException) t;
      } else {
        re = new RuntimeException("Checked exception thrown into JavaScript"
            + " (Web Mode behavior may differ)", t);
      }
      ModuleSpaceMoz.setThrownJavaException(re);
      LowLevelMoz.raiseJavaScriptException(scriptObject, LowLevelMoz.JSVAL_NULL);
      return LowLevelMoz.JSVAL_VOID;
    }
  }
}