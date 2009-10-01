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
package com.google.gwt.core.ext.typeinfo;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.dev.cfg.ModuleDef;
import com.google.gwt.dev.cfg.ModuleDefLoader;

/**
 * Helper for loading modules from the classpath.
 */
class ModuleContext {
  private final TypeOracle oracle;
  private final ModuleDef moduleDef;

  ModuleContext(TreeLogger logger, String moduleName)
      throws UnableToCompleteException {
    moduleDef = ModuleDefLoader.loadFromClassPath(logger, moduleName);
    oracle = moduleDef.getTypeOracle(logger);
  }

  public TypeOracle getOracle() {
    return oracle;
  }
}