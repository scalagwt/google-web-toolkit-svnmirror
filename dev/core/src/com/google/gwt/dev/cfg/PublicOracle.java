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
package com.google.gwt.dev.cfg;

import com.google.gwt.dev.resource.Resource;

/**
 * Abstracts the process of querying for public files.
 * 
 * @deprecated with no replacement, just use {@link ModuleDef} directly
 */
@Deprecated
public interface PublicOracle {

  /**
   * Finds a file on the public path.
   * 
   * @param partialPath a file path relative to the root of any public package
   * @return the url of the file, or <code>null</code> if no such file exists
   */
  Resource findPublicFile(String partialPath);

  /**
   * Returns all available public files.
   * 
   * @return an array containing the partial path to each available public file
   */
  String[] getAllPublicFiles();
}
