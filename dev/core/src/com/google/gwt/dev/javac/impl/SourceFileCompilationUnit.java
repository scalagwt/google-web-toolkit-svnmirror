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
package com.google.gwt.dev.javac.impl;

import com.google.gwt.dev.javac.CompilationUnit;
import com.google.gwt.dev.resource.Resource;
import com.google.gwt.dev.util.Util;

import java.io.InputStream;

/**
 * A compilation unit that was generated.
 */
public class SourceFileCompilationUnit extends CompilationUnit {

  public static String getTypeName(Resource sourceFile) {
    String path = sourceFile.getPath();
    assert (path.endsWith(".java"));
    path = path.substring(0, path.lastIndexOf('.'));
    return path.replace('/', '.');
  }

  public static String readSource(Resource sourceFile) {
    InputStream contents = sourceFile.openContents();
    return Util.readStreamAsString(contents);
  }

  /**
   * A token to retrieve this object's bytes from the disk cache. It's generally
   * much faster to read from the disk cache than to reread individual
   * resources.
   */
  private long cacheToken = -1;

  private Resource sourceFile;

  public SourceFileCompilationUnit(Resource sourceFile) {
    this.sourceFile = sourceFile;
  }

  @Override
  public String getDisplayLocation() {
    return sourceFile.getLocation();
  }

  @Override
  public long getLastModified() {
    return sourceFile.getLastModified();
  }

  @Override
  public String getSource() {
    if (cacheToken < 0) {
      String sourceCode = readSource(sourceFile);
      cacheToken = diskCache.writeString(sourceCode);
      return sourceCode;
    } else {
      return diskCache.readString(cacheToken);
    }
  }

  public Resource getSourceFile() {
    return sourceFile;
  }

  @Override
  public String getTypeName() {
    return getTypeName(sourceFile);
  }

  @Override
  public boolean isGenerated() {
    return false;
  }

  @Override
  public boolean isSuperSource() {
    return sourceFile.wasRerooted();
  }
}
