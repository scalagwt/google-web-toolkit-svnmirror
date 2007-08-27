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
package com.google.gwt.core.ext;

import com.google.gwt.core.ext.typeinfo.TypeOracle;

import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * Provides metadata to deferred binding generators.
 */
public interface GeneratorContext {

  /**
   * Commits source generation begun with
   * {@link #tryCreate(TreeLogger, String, String)}.
   */
  void commit(TreeLogger logger, PrintWriter pw);

  /**
   * Commits resource generation begun with
   * {@link #tryCreateResource(TreeLogger, String)}.
   * 
   * @throws UnableToCompleteException if the resource cannot be written to
   *           disk, if the specified stream is unknown, or if the stream has
   *           already been committed
   */
  void commitResource(TreeLogger logger, OutputStream os)
      throws UnableToCompleteException;

  /**
   * Gets the property oracle for the current generator context. Generators can
   * use the property oracle to query deferred binding properties.
   */
  PropertyOracle getPropertyOracle();

  /**
   * Gets the type oracle for the current generator context. Generators can use
   * the type oracle to ask questions about the entire translatable code base.
   * 
   * @return a TypeOracle over all the relevant translatable compilation units
   *         in the source path
   */
  TypeOracle getTypeOracle();

  /**
   * Attempts to get a <code>PrintWriter</code> so that the caller can
   * generate the source code for the named type. If the named types already
   * exists, <code>null</code> is returned to indicate that no work needs to
   * be done. The file is not committed until
   * {@link #commit(TreeLogger, PrintWriter)} is called.
   * 
   * @param logger a logger; normally the logger passed into
   *          {@link Generator#generate(TreeLogger, GeneratorContext, String)}
   *          or a branch thereof
   * @param packageName the name of the package to which the create type belongs
   * @param simpleName the unqualified source name of the type being generated
   * @return <code>null</code> if the package and class already exists,
   *         otherwise a <code>PrintWriter</code> is returned.
   */
  PrintWriter tryCreate(TreeLogger logger, String packageName, String simpleName);

  /**
   * Attempts to get an <code>OutputStream</code> so that the caller can write
   * file contents into the named file underneath the compilation output
   * directory. The file is not committed until
   * {@link #commitResource(TreeLogger, OutputStream)} is called.
   * 
   * @param logger a logger; normally the logger passed into
   *          {@link Generator#generate(TreeLogger, GeneratorContext, String)}
   *          or a branch thereof
   * @param partialPath the name of the file whose contents are to be written;
   *          the name can include subdirectories separated by forward slashes
   *          ('/')
   * @return an <code>OutputStream</code> into which file contents can be
   *         written, or <code>null</code> if a resource by that name is
   *         already pending or already exists
   * @throws UnableToCompleteException if the resource could not be initialized
   *           for some reason, such as if the specified partial path is invalid
   */
  OutputStream tryCreateResource(TreeLogger logger, String partialPath)
      throws UnableToCompleteException;
}
