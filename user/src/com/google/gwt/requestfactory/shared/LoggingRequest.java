/*
 * Copyright 2010 Google Inc.
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

package com.google.gwt.requestfactory.shared;

import com.google.gwt.requestfactory.server.Logging;

/**
 * "API Generated" request selector interface implemented by objects that give
 * client access to the methods of {@link Logging}.
 */
@Service(Logging.class)
public interface LoggingRequest {

  // Should be returning something better than a Long, but that's all that is
  // supported for now, so using it as a boolean.
  // Should also be passing something better than a series of strings, but
  // that's the only possibility for now.
  RequestObject<Long> logMessage(
      String level, String loggerName, String message);
 
}
