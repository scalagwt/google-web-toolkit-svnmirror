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
package com.google.gwt.dev.shell.profiler;

import com.google.gwt.core.ext.TreeLogger;

/**
 * The public Profiler API for profiling Agents. Provides Agents with
 * the capability to register or de-register from profiling events
 * using a filter.
 *
 */
public interface Profiler {

  int EVENT_METHOD_ENTER = 0;
  int EVENT_METHOD_EXIT = 1;
  int EVENT_MODULE_LOAD_BEGIN = 2;
  int EVENT_MODULE_LOAD_END = 3;
  int EVENT_EXCEPTION_THROW = 4;
  int EVENT_EXCEPTION_CATCH = 5;
  int EVENT_HTTP_REQUEST = 6;
  int EVENT_HTTP_RESPONSE = 7;
  int EVENT_RPC_REQUEST = 8;
  int EVENT_RPC_RESPONSE = 9;

  public TreeLogger getTopLogger();

  public void register( int event, boolean enable, String filter );
}
