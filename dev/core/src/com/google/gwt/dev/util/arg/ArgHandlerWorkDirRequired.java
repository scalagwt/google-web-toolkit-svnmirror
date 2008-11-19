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
package com.google.gwt.dev.util.arg;

import com.google.gwt.util.tools.ArgHandlerDir;

import java.io.File;

/**
 * Argument handler for processing a required work directory.
 */
public final class ArgHandlerWorkDirRequired extends ArgHandlerDir {

  private final OptionWorkDir option;

  public ArgHandlerWorkDirRequired(OptionWorkDir option) {
    this.option = option;
  }

  public String getPurpose() {
    return "The compiler work directory (must be writeable)";
  }

  public String getTag() {
    return "-workDir";
  }

  @Override
  public boolean isRequired() {
    return true;
  }

  @Override
  public void setDir(File dir) {
    option.setWorkDir(dir);
  }

}
