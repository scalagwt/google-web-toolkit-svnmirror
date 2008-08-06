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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

/**
 * Class for wrapping Java things for JavaScript.
 */
public class JavaDispatchImpl implements JavaDispatch {

  private final DispatchIdOracle dispatchInfo;

  private final Object target;

  /**
   * This constructor initializes a dispatcher for handling static members.
   * 
   * @param dispatchInfo class loader to use for dispatching member access
   */
  public JavaDispatchImpl(DispatchIdOracle dispatchInfo) {
    this.dispatchInfo = dispatchInfo;
    target = null;
  }

  /**
   * This constructor initializes a dispatcher around a particular instance.
   * 
   * @param dispatchInfo class loader to use for dispatching member access
   * @param target the instance object to use for dispatching member accesses
   * 
   * @throws NullPointerException if target is null
   */
  public JavaDispatchImpl(DispatchIdOracle dispatchInfo, Object target) {
    if (target == null) {
      throw new NullPointerException("target cannot be null");
    }

    this.dispatchInfo = dispatchInfo;
    this.target = target;
  }

  /**
   * @param dispId the unique number of a field
   * @return the field
   */
  public Field getField(int dispId) {
    return (Field) getMember(dispId);
  }

  /**
   * @param dispId the unique number of a field
   * @return true the value of the field
   * @throws IllegalArgumentException
   */
  public Object getFieldValue(int dispId) {
    Field field = (Field) getMember(dispId);
    try {
      return field.get(target);
    } catch (IllegalAccessException e) {
      // should never, ever happen
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  /**
   * @param dispId the unique number of a method
   * @return the method
   */
  public MethodAdaptor getMethod(int dispId) {
    Member m = getMember(dispId);
    if (m instanceof Method) {
      return new MethodAdaptor((Method) m);
    } else if (m instanceof Constructor) {
      return new MethodAdaptor((Constructor<?>) m);
    } else {
      throw new RuntimeException();
    }
  }

  public Object getTarget() {
    return target;
  }

  /**
   * @param dispId the unique number of a method or field
   * @return true if the dispId represents a field
   */
  public boolean isField(int dispId) {
    if (dispId < 0) {
      return false;
    }

    return getMember(dispId) instanceof Field;
  }

  /**
   * @param dispId the unique number of a method or field
   * @return true if the dispId represents a method
   */
  public boolean isMethod(int dispId) {
    if (dispId < 0) {
      return false;
    }

    Member m = getMember(dispId);
    return (m instanceof Method) || (m instanceof Constructor);
  }

  /**
   * @param dispId the unique number of a field
   * @param value the value to assign to the field
   * @throws IllegalArgumentException
   */
  public void setFieldValue(int dispId, Object value) {
    Field field = (Field) getMember(dispId);
    try {
      field.set(target, value);
    } catch (IllegalAccessException e) {
      // should never, ever happen
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  /**
   * @param dispId the unique number of a method or field
   * @return the member
   */
  protected Member getMember(int dispId) {
    DispatchClassInfo clsInfo = dispatchInfo.getClassInfoByDispId(dispId);
    return clsInfo.getMember(dispId);
  }
}
