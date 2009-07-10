// Copyright 2009 Google Inc. All Rights Reserved.

package com.google.gwt.junit;

import net.sourceforge.htmlunit.corejs.javascript.Context;
import net.sourceforge.htmlunit.corejs.javascript.NativeJavaMethod;
import net.sourceforge.htmlunit.corejs.javascript.Scriptable;

import java.lang.reflect.Method;

public class HostedModePluginObject implements Scriptable {

  private Scriptable parent;
  private Scriptable prototype;

  public HostedModePluginObject(Scriptable parent, Scriptable prototype) {
    this.parent = parent;
    this.prototype = prototype;
  }

  public void delete(int index) {
  }

  public void delete(String name) {
  }

  public Object get(int index, Scriptable start) {
    return Context.getUndefinedValue();
  }

  public Object get(String name, Scriptable start) {
    if (name.equals("connect")) {
      Method connectMethod = null;
      try {
        connectMethod = HostedModePluginObject.class.getMethod("connect",
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

  public boolean has(int index, Scriptable start) {
    return false;
  }

  public boolean has(String name, Scriptable start) {
    return name.equals("connect");
  }

  public boolean hasInstance(Scriptable instance) {
    return false;
  }

  public void put(int index, Scriptable start, Object value) {
  }

  public void put(String name, Scriptable start, Object value) {
  }

  public void setParentScope(Scriptable parent) {
    this.parent = parent;
  }

  public void setPrototype(Scriptable prototype) {
    this.prototype = prototype;
  }
}