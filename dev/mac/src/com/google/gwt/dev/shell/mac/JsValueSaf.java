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
package com.google.gwt.dev.shell.mac;

import com.google.gwt.dev.shell.CompilingClassLoader;
import com.google.gwt.dev.shell.JsValue;
import com.google.gwt.dev.shell.mac.LowLevelSaf.DispatchObject;

/**
 * Represents a Safari JavaScript value.
 * 
 * The basic rule is that any JSValue passed to Java code from native code will
 * always be GC-protected in the native code and Java will always unprotect it
 * when the value is finalized. It should always be stored in a JsValue object
 * immediately to make sure it is cleaned up properly when it is no longer
 * needed. This approach is required to avoid a race condition where the value
 * is allocated in JNI code but could be garbage collected before Java takes
 * ownership of the value. Java values passed into JavaScript store a GlobalRef
 * of a WebKitDispatchAdapter or MethodDispatch objects, which are freed when
 * the JS value is finalized.
 */
public class JsValueSaf extends JsValue {

  private static class JsCleanupSaf implements JsCleanup {
    private final int jsval;
    private final Throwable creationStackTrace;

    /**
     * Create a cleanup object which takes care of cleaning up the underlying JS
     * object.
     * 
     * @param jsval JSValue pointer as an integer
     */
    public JsCleanupSaf(int jsval, Throwable creationStackTrace) {
      this.jsval = jsval;
      this.creationStackTrace = creationStackTrace;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.google.gwt.dev.shell.JsValue.JsCleanup#doCleanup()
     */
    public void doCleanup() {
      LowLevelSaf.gcUnlock(jsval, creationStackTrace);
    }
  }

  /* 
   * Underlying JSValue* as an integer.
   */
  private int jsval;
  
  /*
   * Stores a stack trace of the creation site for debugging.
   */
  private Throwable creationStackTrace;

  /**
   * Create a Java wrapper around an undefined JSValue.
   */
  public JsValueSaf() {
    init(LowLevelSaf.jsUndefined());
  }

  /**
   * Create a Java wrapper around the underlying JSValue.
   * 
   * @param jsval a pointer to the underlying JSValue object as an integer
   */
  public JsValueSaf(int jsval) {
    init(jsval);
  }

  public boolean getBoolean() {
    int curExecState = LowLevelSaf.getExecState();
    return LowLevelSaf.coerceToBoolean(curExecState, jsval);
  }

  public int getInt() {
    int curExecState = LowLevelSaf.getExecState();
    return LowLevelSaf.coerceToInt(curExecState, jsval);
 }

  public int getJsValue() {
    return jsval;
  }

  public double getNumber() {
    int curExecState = LowLevelSaf.getExecState();
    return LowLevelSaf.coerceToDouble(curExecState, jsval);
  }

  public String getString() {
    int curExecState = LowLevelSaf.getExecState();
    return LowLevelSaf.coerceToString(curExecState, jsval);
  }

  public String getTypeString() {
    return LowLevelSaf.getTypeString(jsval);
  }

  public Object getWrappedJavaObject() {
    DispatchObject obj = LowLevelSaf.unwrapDispatch(jsval);
    return obj.getTarget();
  }

  public boolean isBoolean() {
    return LowLevelSaf.isBoolean(jsval);
  }

  public boolean isInt() {
    // Safari doesn't have integers, so this is always false
    return false;
  }

  public boolean isJavaScriptObject() {
    return LowLevelSaf.isObject(jsval) && !LowLevelSaf.isWrappedDispatch(jsval);
  }

  public boolean isNull() {
    return LowLevelSaf.isNull(jsval);
  }

  public boolean isNumber() {
    return LowLevelSaf.isNumber(jsval);
  }

  public boolean isObject() {
    return LowLevelSaf.isObject(jsval);
  }

  public boolean isString() {
    return LowLevelSaf.isString(jsval);
  }

  public boolean isUndefined() {
    return LowLevelSaf.isUndefined(jsval);
  }

  public boolean isWrappedJavaObject() {
    return LowLevelSaf.isWrappedDispatch(jsval);
  }

  public void setBoolean(boolean val) {
    setJsVal(LowLevelSaf.convertBoolean(val));
  }

  public void setByte(byte val) {
    setJsVal(LowLevelSaf.convertDouble(val));
  }

  public void setChar(char val) {
    setJsVal(LowLevelSaf.convertDouble(val));
  }

  public void setDouble(double val) {
    setJsVal(LowLevelSaf.convertDouble(val));
  }

  public void setInt(int val) {
    setJsVal(LowLevelSaf.convertDouble(val));
  }

  /**
   * Set a new value.  Unlock the previous value, but do *not* lock the new
   * value (see class comment).
   * 
   * @param jsval the new value to set
   */
  public void setJsVal(int jsval) {
    LowLevelSaf.gcUnlock(this.jsval, creationStackTrace);
    init(jsval);
  }
  
  public void setNull() {
    setJsVal(LowLevelSaf.jsNull());
  }

  public void setShort(short val) {
    setJsVal(LowLevelSaf.convertDouble(val));
  }

  public void setString(String val) {
    setJsVal(LowLevelSaf.convertString(val));
  }

  public void setUndefined() {
    setJsVal(LowLevelSaf.jsUndefined());
  }

  public void setValue(JsValue other) {
    int jsvalOther = ((JsValueSaf)other).jsval;
    /*
     * Add another lock to this jsval, since both the other object and this
     * one will eventually release it.
     */
    LowLevelSaf.gcLock(jsvalOther);
    setJsVal(jsvalOther);
  }

  public void setWrappedJavaObject(CompilingClassLoader cl, Object val) {
    DispatchObject dispObj;
    if (val == null) {
      setNull();
      return;
    } else if (val instanceof DispatchObject) {
      dispObj = (DispatchObject)val;
    } else {
      dispObj = new WebKitDispatchAdapter(cl, val);
    }
    setJsVal(LowLevelSaf.wrapDispatch(dispObj));
  }

  protected JsCleanup createCleanupObject() {
    return new JsCleanupSaf(jsval, creationStackTrace);
  }

  /**
   * Initialization helper method.
   * @param jsval underlying JSValue*
   */
  private void init(int jsval) {
    this.jsval = jsval;
    
    // only create and fill in the stack trace if we are debugging
    if (LowLevelSaf.debugObjectCreation) {
      this.creationStackTrace = new Throwable();
      this.creationStackTrace.fillInStackTrace();
    } else {
      this.creationStackTrace = null;
    }
  }

}
