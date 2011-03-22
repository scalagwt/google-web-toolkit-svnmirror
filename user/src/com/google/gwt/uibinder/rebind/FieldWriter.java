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
package com.google.gwt.uibinder.rebind;

import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JType;

/**
 * Models a field to be written in the generated binder code. Note that this is
 * not necessarily a field that the user has declared. It's basically any
 * variable the generated UiBinder#createAndBindUi implementation will need.
 * <p>
 * A field can have a custom initialization statement, set via
 * {@link #setInitializer}. Without one it will be initialized via a
 * {@link com.google.gwt.core.client.GWT#create} call. (In the rare case that
 * you need a field not to be initialized, initialize it to "null".)
 * <p>
 * Dependencies can be declared between fields via {@link #needs}, to ensure
 * that one can be initialized via reference to another. Circular references are
 * not supported, nor detected.
 */
public interface FieldWriter {

  /**
   * Returns the type of this field, or for generated types the type it extends.
   */
  // TODO(rjrjr) When ui:style is able to implement multiple interfaces,
  // this will need to become a set
  JClassType getAssignableType();

  /**
   * Returns the custom initializer for this field, or null if it is not set.
   */
  String getInitializer();

  /**
   * Returns the type of this field, or null if this field is of a type that has
   * not yet been generated.
   */
  JClassType getInstantiableType();

  /**
   * Returns the qualified source name of this type.
   */
  String getQualifiedSourceName();

  /**
   * Returns the return type found at the end of the given method call
   * path, which must begin with the receiver's name, or null if the
   * path is invalid.
   */
  JType getReturnType(String[] path, MonitoredLogger logger);

  /**
   * Declares that the receiver depends upon the given field.
   */
  void needs(FieldWriter f);

  /**
   * Used to provide an initializer string to use instead of a
   * {@link com.google.gwt.core.client.GWT#create} call. Note that this is an
   * RHS expression. Don't include the leading '=', and don't end it with ';'.
   *
   * @throws IllegalStateException on second attempt to set the initializer
   */
  void setInitializer(String initializer);

  /**
   * Write the field declaration.
   */
  void write(IndentedWriter w) throws UnableToCompleteException;
}
