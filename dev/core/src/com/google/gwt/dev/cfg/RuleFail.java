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
package com.google.gwt.dev.cfg;

import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;

public class RuleFail extends Rule {

  public String realize(TreeLogger logger, GeneratorContext context,
      String typeName) throws UnableToCompleteException {
    logger.log(TreeLogger.ERROR, "Deferred binding request failed for type '"
        + typeName + "'", null);
    throw new UnableToCompleteException();
  }

  public String toString() {
    return "<fail>";
  }

}
