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
import com.google.gwt.core.ext.UnableToCompleteException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

/**
 * The interface to the low-level browser, this class serves as a 'domain' for a
 * module, loading all of its classes in a separate, isolated class loader. This
 * allows us to run multiple modules, both in succession and simultaneously.
 */
public abstract class ModuleSpace implements ShellJavaScriptHost {

  private static ThreadLocal<Throwable> sCaughtJavaExceptionObject = new ThreadLocal<Throwable>();

  private static ThreadLocal<Throwable> sThrownJavaExceptionObject = new ThreadLocal<Throwable>();

  /**
   * Logger is thread local.
   */
  private static ThreadLocal<TreeLogger> threadLocalLogger = new ThreadLocal<TreeLogger>();

  public static void setThrownJavaException(Throwable t) {
    sThrownJavaExceptionObject.set(t);
  }

  protected static TreeLogger getLogger() {
    return threadLocalLogger.get();
  }

  /**
   * Create a JavaScriptException object. This must be done reflectively, since
   * this class will have been loaded from a ClassLoader other than the
   * session's thread.
   */
  static RuntimeException createJavaScriptException(ClassLoader cl,
      Object exception) {
    Exception caught;
    try {
      Class<?> javaScriptExceptionClass = Class.forName(
          "com.google.gwt.core.client.JavaScriptException", true, cl);
      Constructor<?> ctor = javaScriptExceptionClass.getDeclaredConstructor(Object.class);
      return (RuntimeException) ctor.newInstance(new Object[] {exception});
    } catch (InstantiationException e) {
      caught = e;
    } catch (IllegalAccessException e) {
      caught = e;
    } catch (SecurityException e) {
      caught = e;
    } catch (ClassNotFoundException e) {
      caught = e;
    } catch (NoSuchMethodException e) {
      caught = e;
    } catch (IllegalArgumentException e) {
      caught = e;
    } catch (InvocationTargetException e) {
      caught = e;
    }
    throw new RuntimeException("Error creating JavaScriptException", caught);
  }

  /**
   * Get the JavaScriptObject wrapped by a JavaScriptException. We have to do
   * this reflectively, since the JavaScriptException object is from an
   * arbitrary classloader. If the object is not a JavaScriptException, or is
   * not from the given ClassLoader, we'll return null.
   */
  static Object getJavaScriptExceptionException(ClassLoader cl,
      Object javaScriptException) {
    if (javaScriptException.getClass().getClassLoader() != cl) {
      return null;
    }

    Exception caught;
    try {
      Class<?> javaScriptExceptionClass = Class.forName(
          "com.google.gwt.core.client.JavaScriptException", true, cl);
      
      if (!javaScriptExceptionClass.isInstance(javaScriptException)) {
        // Not a JavaScriptException
        return null;
      }
      Method getException = javaScriptExceptionClass.getMethod("getException");
      return getException.invoke(javaScriptException);
    } catch (NoSuchMethodException e) {
      caught = e;
    } catch (ClassNotFoundException e) {
      caught = e;
    } catch (IllegalArgumentException e) {
      caught = e;
    } catch (IllegalAccessException e) {
      caught = e;
    } catch (InvocationTargetException e) {
      caught = e;
    }
    throw new RuntimeException("Error getting exception value", caught);
  }

  protected final ModuleSpaceHost host;

  private final Object key;

  private final String moduleName;

  protected ModuleSpace(ModuleSpaceHost host, String moduleName, Object key) {
    this.host = host;
    this.moduleName = moduleName;
    this.key = key;
    TreeLogger hostLogger = host.getLogger();
    threadLocalLogger.set(hostLogger);
  }

  public void dispose() {
    // Clear our class loader.
    getIsolatedClassLoader().clear();
  }

  public void exceptionCaught(Object exception) {
    Throwable caught;
    Throwable thrown = sThrownJavaExceptionObject.get();
    if (thrown != null && isExceptionSame(thrown, exception)) {
      // The caught exception was thrown by us.
      caught = thrown;
      sThrownJavaExceptionObject.set(null);
    } else if (exception instanceof Throwable) {
      caught = (Throwable) exception;
    } else {
      caught = createJavaScriptException(getIsolatedClassLoader(), exception);
      // Remove excess stack frames from the new exception.
      caught.fillInStackTrace();
      StackTraceElement[] trace = caught.getStackTrace();
      assert trace.length > 1;
      assert trace[1].getClassName().equals(JavaScriptHost.class.getName());
      assert trace[1].getMethodName().equals("exceptionCaught");
      StackTraceElement[] newTrace = new StackTraceElement[trace.length - 1];
      System.arraycopy(trace, 1, newTrace, 0, newTrace.length);
      caught.setStackTrace(newTrace);
    }
    sCaughtJavaExceptionObject.set(caught);
  }

  /**
   * Get the unique key for this module.
   * 
   * @return the unique key
   */
  public Object getKey() {
    return key;
  }

  /**
   * Get the module name.
   * 
   * @return the module name
   */
  public String getModuleName() {
    return moduleName;
  }

  public boolean invokeNativeBoolean(String name, Object jthis,
      Class<?>[] types, Object[] args) throws Throwable {
    JsValue result = invokeNative(name, jthis, types, args);
    String msgPrefix = composeResultErrorMsgPrefix(name, "a boolean");
    Boolean value = JsValueGlue.get(result, getIsolatedClassLoader(),
        boolean.class, msgPrefix);
    if (value == null) {
      throw new HostedModeException(msgPrefix
          + ": return value null received, expected a boolean");
    }
    return value.booleanValue();
  }

  public byte invokeNativeByte(String name, Object jthis, Class<?>[] types,
      Object[] args) throws Throwable {
    JsValue result = invokeNative(name, jthis, types, args);
    String msgPrefix = composeResultErrorMsgPrefix(name, "a byte");
    Byte value = JsValueGlue.get(result, null, Byte.TYPE, msgPrefix);
    if (value == null) {
      throw new HostedModeException(msgPrefix
          + ": return value null received, expected a byte");
    }
    return value.byteValue();
  }

  public char invokeNativeChar(String name, Object jthis, Class<?>[] types,
      Object[] args) throws Throwable {
    JsValue result = invokeNative(name, jthis, types, args);
    String msgPrefix = composeResultErrorMsgPrefix(name, "a char");
    Character value = JsValueGlue.get(result, null, Character.TYPE, msgPrefix);
    if (value == null) {
      throw new HostedModeException(msgPrefix
          + ": return value null received, expected a char");
    }
    return value.charValue();
  }

  public double invokeNativeDouble(String name, Object jthis, Class<?>[] types,
      Object[] args) throws Throwable {
    JsValue result = invokeNative(name, jthis, types, args);
    String msgPrefix = composeResultErrorMsgPrefix(name, "a double");
    Double value = JsValueGlue.get(result, null, Double.TYPE, msgPrefix);
    if (value == null) {
      throw new HostedModeException(msgPrefix
          + ": return value null received, expected a double");
    }
    return value.doubleValue();
  }

  public float invokeNativeFloat(String name, Object jthis, Class<?>[] types,
      Object[] args) throws Throwable {
    JsValue result = invokeNative(name, jthis, types, args);
    String msgPrefix = composeResultErrorMsgPrefix(name, "a float");
    Float value = JsValueGlue.get(result, null, Float.TYPE, msgPrefix);
    if (value == null) {
      throw new HostedModeException(msgPrefix
          + ": return value null received, expected a float");
    }
    return value.floatValue();
  }

  public int invokeNativeInt(String name, Object jthis, Class<?>[] types,
      Object[] args) throws Throwable {
    JsValue result = invokeNative(name, jthis, types, args);
    String msgPrefix = composeResultErrorMsgPrefix(name, "an int");
    Integer value = JsValueGlue.get(result, null, Integer.TYPE, msgPrefix);
    if (value == null) {
      throw new HostedModeException(msgPrefix
          + ": return value null received, expected an int");
    }
    return value.intValue();
  }

  public long invokeNativeLong(String name, Object jthis, Class<?>[] types,
      Object[] args) throws Throwable {
    JsValue result = invokeNative(name, jthis, types, args);
    String msgPrefix = composeResultErrorMsgPrefix(name, "a long");
    Long value = JsValueGlue.get(result, null, Long.TYPE, msgPrefix);
    if (value == null) {
      throw new HostedModeException(msgPrefix
          + ": return value null received, expected a long");
    }
    return value.longValue();
  }

  public Object invokeNativeObject(String name, Object jthis, Class<?>[] types,
      Object[] args) throws Throwable {
    JsValue result = invokeNative(name, jthis, types, args);
    String msgPrefix = composeResultErrorMsgPrefix(name, "a Java object");
    return JsValueGlue.get(result, getIsolatedClassLoader(), Object.class,
        msgPrefix);
  }

  public short invokeNativeShort(String name, Object jthis, Class<?>[] types,
      Object[] args) throws Throwable {
    JsValue result = invokeNative(name, jthis, types, args);
    String msgPrefix = composeResultErrorMsgPrefix(name, "a short");
    Short value = JsValueGlue.get(result, null, Short.TYPE, msgPrefix);
    if (value == null) {
      throw new HostedModeException(msgPrefix
          + ": return value null received, expected a short");
    }
    return value.shortValue();
  }

  public void invokeNativeVoid(String name, Object jthis, Class<?>[] types,
      Object[] args) throws Throwable {
    JsValue result = invokeNative(name, jthis, types, args);
    if (!result.isUndefined()) {
      getLogger().log(
          TreeLogger.WARN,
          "JSNI method '"
              + name
              + "' returned a value of type "
              + result.getTypeString()
              + " but was declared void; it should not have returned a value at all",
          null);
    }
  }

  /**
   * Allows client-side code to log to the tree logger.
   */
  public void log(String message, Throwable e) {
    TreeLogger logger = host.getLogger();
    TreeLogger.Type type = TreeLogger.INFO;
    if (e != null) {
      type = TreeLogger.ERROR;
    }
    logger.log(type, message, e);
  }

  /**
   * Runs the module's user startup code.
   */
  public final void onLoad(TreeLogger logger) throws UnableToCompleteException {
    // Tell the host we're ready for business.
    //
    host.onModuleReady(this);

    // Make sure we can resolve JSNI references to static Java names.
    //
    try {
      Object staticDispatch = getStaticDispatcher();
      createNativeMethods("initializeStaticDispatcher",
          "function __defineStatic(__arg0) { window.__static = __arg0; }");
      invokeNativeVoid("__defineStatic", null, new Class[] {Object.class},
          new Object[] {staticDispatch});
    } catch (Throwable e) {
      logger.log(TreeLogger.ERROR, "Unable to initialize static dispatcher", e);
      throw new UnableToCompleteException();
    }

    // Actually run user code.
    //
    String entryPointTypeName = null;
    try {
      String[] entryPoints = host.getEntryPointTypeNames();
      if (entryPoints.length > 0) {
        for (int i = 0; i < entryPoints.length; i++) {
          entryPointTypeName = entryPoints[i];
          Class<?> clazz = loadClassFromSourceName(entryPointTypeName);
          Method onModuleLoad = null;
          try {
            onModuleLoad = clazz.getMethod("onModuleLoad");
            if (!Modifier.isStatic(onModuleLoad.getModifiers())) {
              // it's non-static, so we need to rebind the class
              onModuleLoad = null;
            }
          } catch (NoSuchMethodException e) {
            // okay, try rebinding it; maybe the rebind result will have one
          }
          Object module = null;
          if (onModuleLoad == null) {
            module = rebindAndCreate(entryPointTypeName);
            onModuleLoad = module.getClass().getMethod("onModuleLoad");
            // Record the rebound name of the class for stats (below).
            entryPointTypeName = module.getClass().getName().replace('$', '.');
          }
          onModuleLoad.setAccessible(true);
          invokeNativeVoid("fireOnModuleLoadStart", null,
              new Class[] {String.class}, new Object[] {entryPointTypeName});
          onModuleLoad.invoke(module);
        }
      } else {
        logger.log(
            TreeLogger.WARN,
            "The module has no entry points defined, so onModuleLoad() will never be called",
            null);
      }
    } catch (Throwable e) {
      Throwable caught = e;

      if (e instanceof InvocationTargetException) {
        caught = ((InvocationTargetException) e).getTargetException();
      }

      if (caught instanceof ExceptionInInitializerError) {
        caught = ((ExceptionInInitializerError) caught).getException();
      }

      String unableToLoadMessage = "Unable to load module entry point class "
          + entryPointTypeName;
      if (caught != null) {
        unableToLoadMessage += " (see associated exception for details)";
      }
      logger.log(TreeLogger.ERROR, unableToLoadMessage, caught);
      throw new UnableToCompleteException();
    }
  }

  @SuppressWarnings("unchecked")
  public <T> T rebindAndCreate(String requestedClassName)
      throws UnableToCompleteException {
    Throwable caught = null;
    String msg = null;
    String resultName = null;
    try {
      // Rebind operates on source-level names.
      //
      String sourceName = requestedClassName.replace('$', '.');
      resultName = rebind(sourceName);
      Class<?> resolvedClass = loadClassFromSourceName(resultName);
      if (Modifier.isAbstract(resolvedClass.getModifiers())) {
        msg = "Deferred binding result type '" + resultName
            + "' should not be abstract";
      } else {
        Constructor<?> ctor = resolvedClass.getDeclaredConstructor();
        ctor.setAccessible(true);
        return (T) ctor.newInstance();
      }
    } catch (ClassNotFoundException e) {
      msg = "Could not load deferred binding result type '" + resultName + "'";
      caught = e;
    } catch (InstantiationException e) {
      caught = e;
    } catch (IllegalAccessException e) {
      caught = e;
    } catch (ExceptionInInitializerError e) {
      caught = e.getException();
    } catch (NoSuchMethodException e) {
      msg = "Rebind result '" + resultName
          + "' has no default (zero argument) constructors.";
      caught = e;
    } catch (InvocationTargetException e) {
      caught = e.getTargetException();
    }

    // Always log here because sometimes this method gets called from static
    // initializers and other unusual places, which can obscure the problem.
    //
    if (msg == null) {
      msg = "Failed to create an instance of '" + requestedClassName
          + "' via deferred binding ";
    }
    host.getLogger().log(TreeLogger.ERROR, msg, caught);
    throw new UnableToCompleteException();
  }

  protected abstract void cleanupJsValues();

  protected String createNativeMethodInjector(String jsniSignature,
      String[] paramNames, String js) {
    String newScript = "window[\"" + jsniSignature + "\"] = function(";

    for (int i = 0; i < paramNames.length; ++i) {
      if (i > 0) {
        newScript += ", ";
      }

      newScript += paramNames[i];
    }

    newScript += ") { " + js + " };\n";
    return newScript;
  }

  /**
   * Invokes a native JavaScript function.
   * 
   * @param name the name of the function to invoke
   * @param jthis the function's 'this' context
   * @param types the type of each argument
   * @param args the arguments to be passed
   * @return the return value as a Variant.
   */
  protected abstract JsValue doInvoke(String name, Object jthis,
      Class<?>[] types, Object[] args) throws Throwable;

  protected CompilingClassLoader getIsolatedClassLoader() {
    return host.getClassLoader();
  }

  /**
   * Injects the magic needed to resolve JSNI references from module-space.
   */
  protected abstract Object getStaticDispatcher();

  /**
   * Invokes a native JavaScript function.
   * 
   * @param name the name of the function to invoke
   * @param jthis the function's 'this' context
   * @param types the type of each argument
   * @param args the arguments to be passed
   * @return the return value as a Variant.
   */
  protected final JsValue invokeNative(String name, Object jthis,
      Class<?>[] types, Object[] args) throws Throwable {
    // Whenever a native method is invoked, release any enqueued cleanup objects
    cleanupJsValues();
    try {
      return doInvoke(name, jthis, types, args);
    } catch (Throwable thrown) {
      scrubStackTrace(thrown);
      throw thrown;
    }
  }

  protected boolean isExceptionSame(@SuppressWarnings("unused")
  Throwable original, Object exception) {
    // For most platforms, the null exception means we threw it.
    // IE overrides this.
    return exception == null;
  }

  protected String rebind(String sourceName) throws UnableToCompleteException {
    try {
      String result = host.rebind(host.getLogger(), sourceName);
      if (result != null) {
        return result;
      } else {
        return sourceName;
      }
    } catch (UnableToCompleteException e) {
      String msg = "Deferred binding failed for '" + sourceName
          + "'; expect subsequent failures";
      host.getLogger().log(TreeLogger.ERROR, msg, e);
      throw new UnableToCompleteException();
    }
  }

  private String composeResultErrorMsgPrefix(String name, String typePhrase) {
    return "Something other than " + typePhrase
        + " was returned from JSNI method '" + name + "'";
  }

  private boolean isUserFrame(StackTraceElement element) {
    try {
      CompilingClassLoader cl = getIsolatedClassLoader();
      String className = element.getClassName();
      Class<?> clazz = Class.forName(className, false, cl);
      if (clazz.getClassLoader() == cl) {
        // Lives in user classLoader.
        return true;
      }
      // At this point, it must be a JRE class to qualify.
      if (clazz.getClassLoader() != null || !className.startsWith("java.")) {
        return false;
      }
      if (className.startsWith("java.lang.reflect.")) {
        return false;
      }
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

  /**
   * Handles loading a class that might be nested given a source type name.
   */
  private Class<?> loadClassFromSourceName(String sourceName)
      throws ClassNotFoundException {
    String toTry = sourceName;
    while (true) {
      try {
        return Class.forName(toTry, true, getIsolatedClassLoader());
      } catch (ClassNotFoundException e) {
        // Assume that the last '.' should be '$' and try again.
        //
        int i = toTry.lastIndexOf('.');
        if (i == -1) {
          throw e;
        }

        toTry = toTry.substring(0, i) + "$" + toTry.substring(i + 1);
      }
    }
  }

  /**
   * Clean up the stack trace by removing our hosting frames. But don't do this
   * if our own frames are at the top of the stack, because we may be the real
   * cause of the exception.
   */
  private void scrubStackTrace(Throwable thrown) {
    List<StackTraceElement> trace = new ArrayList<StackTraceElement>(
        Arrays.asList(thrown.getStackTrace()));
    boolean seenUserFrame = false;
    for (ListIterator<StackTraceElement> it = trace.listIterator(); it.hasNext();) {
      StackTraceElement element = it.next();
      if (!isUserFrame(element)) {
        if (seenUserFrame) {
          it.remove();
        }
        continue;
      }
      seenUserFrame = true;

      // Remove a JavaScriptHost.invokeNative*() frame.
      if (element.getClassName().equals(JavaScriptHost.class.getName())) {
        if (element.getMethodName().equals("exceptionCaught")) {
          it.remove();
        } else if (element.getMethodName().startsWith("invokeNative")) {
          it.remove();
          // Also try to convert the next frame to a true native.
          if (it.hasNext()) {
            StackTraceElement next = it.next();
            if (next.getLineNumber() == -1) {
              next = new StackTraceElement(next.getClassName(),
                  next.getMethodName(), next.getFileName(), -2);
              it.set(next);
            }
          }
        }
      }
    }
    thrown.setStackTrace(trace.toArray(new StackTraceElement[trace.size()]));
  }
}
