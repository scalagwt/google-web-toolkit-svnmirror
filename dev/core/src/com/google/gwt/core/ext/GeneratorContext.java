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
package com.google.gwt.core.ext;

import com.google.gwt.core.ext.typeinfo.TypeOracle;

import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * Provides metadata to deferred binding generators.
 */
public interface GeneratorContext {

  /**
   * Commits resource generation begun with
   * {@link #tryCreateResource(TreeLogger, String, boolean)}.
   * 
   * @throws UnableToCompleteException if the resource cannot be written to disk
   */
  void commit(TreeLogger logger, OutputStream os)
      throws UnableToCompleteException;

  /**
   * Commits source generation begun with
   * {@link #tryCreate(TreeLogger, String, String)}.
   */
  void commit(TreeLogger logger, PrintWriter pw);

// TODO(bruce): support this in next iteration
//  /**
//   * Finds a resource having the specified name in the module's source path.
//   * 
//   * @param name the name of the resource to find, formatted as desribed for
//   *          {@link Class#getResource(String)}.
//   * @return the URL referring to the resource or <code>null</code> if the
//   *         resource was not found
//   */
//  URL findOnModuleSourcePath(String name);

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
   * {@link #commit(TreeLogger, OutputStream)} is called.
   * 
   * @param logger a logger; normally the logger passed into
   *          {@link Generator#generate(TreeLogger, GeneratorContext, String)}
   *          or a branch thereof
   * @param name the name of the file whose contents are to be written; the name
   *          can include subdirectories
   * @param overwriteExisting if <code>true</code>, an existing resource of
   *          the same name will be overwritten if the OutputStream is
   *          committed; if <code>false</code> and the specified resource
   *          already exists, <code>null</code> will be returned
   * @return an <code>OutputStream</code> into which file contents can be
   *         written, or <code>null</code> if either (1) a resource by that
   *         name is already pending or (2) the resource already exists and
   *         <code>overwriteExisting</code> is <code>false</code>
   * @throws UnableToCompleteException if the specified name is invalid
   */
  OutputStream tryCreateResource(TreeLogger logger, String name,
      boolean overwriteExisting) throws UnableToCompleteException;
}
